package cz.wa.smoothmetalmap.gui.texturecanvas

import cz.wa.smoothmetalmap.gui.ContentHolder
import cz.wa.smoothmetalmap.gui.math.ColorUtils
import cz.wa.smoothmetalmap.gui.math.Vec2d
import cz.wa.smoothmetalmap.gui.math.Vec2i
import cz.wa.smoothmetalmap.gui.utils.CanvasBuffer
import cz.wa.smoothmetalmap.gui.utils.GuiUtils
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

/**
 * Displays texture. Takes care of moving, zooming, mapping positions.
 */
open class TextureViewer(val contentHolder: ContentHolder) : Canvas(),
    MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    val zoomSpeed = 1.1

    protected var drawInfo = true
    protected var customImage: BufferedImage? = null

    var posX = 0.0
    var posY = 0.0
    private var lastX = 0
    private var lastY = 0
    protected var mouseRDown = false
    protected var mouseLDown = false
    protected var mouseInside = false
    var zoom = 1.0
    protected var currMousePos = Vec2i.NEGATIVE

    // settings
    protected var infoBgColor: Color = Color.GRAY
    protected var infoTextColor: Color = Color.BLACK
    protected var infoGap = 2
    protected var infoFontSize = 12
    protected var infoFont = Font("Courier new", Font.PLAIN, infoFontSize)
    protected var infoWidth = 300

    private val canvasBuffer = CanvasBuffer(this)

    init {
        addMouseListener(this)
        addMouseMotionListener(this)
        addMouseWheelListener(this)
        addKeyListener(this)

        minimumSize = Dimension(16, 16)
        maximumSize = Dimension(4096, 4096)
    }

    fun setImage(img: BufferedImage) {
        customImage = img
    }

    private fun paintComponent(g: Graphics) {
        g.color = contentHolder.settings.gui.backgroundColor
        g.fillRect(0, 0, width, height)
        if (customImage == null) {
            return
        }
        if (contentHolder.settings.gui.showBounds) {
            drawBounds(g)
        }
        drawBefore(g)
        drawSourceImage(g)
        drawAfter(g)
        if (mouseInside && drawInfo) {
            drawTileInfo(g)
        }
    }

    private fun drawBounds(g: Graphics) {
        val c = contentHolder.settings.gui.backgroundColor
        g.color = Color((c.red + 128) % 256, (c.green + 128) % 256, (c.blue + 128) % 256)
        val image = getImage()!!
        val w = image.width
        val h = image.height

        var p1 = imgToScr(0, -BOUNDS_LENGTH)
        var p2 = imgToScr(0, h + BOUNDS_LENGTH)
        g.drawLine(p1.x - 1, p1.y, p2.x - 1, p2.y)

        p1 = imgToScr(-BOUNDS_LENGTH, 0)
        p2 = imgToScr(w + BOUNDS_LENGTH, 0)
        g.drawLine(p1.x, p1.y - 1, p2.x, p2.y - 1)

        p1 = imgToScr(w, -BOUNDS_LENGTH)
        p2 = imgToScr(w, h + BOUNDS_LENGTH)
        g.drawLine(p1.x, p1.y, p2.x, p2.y)

        p1 = imgToScr(-BOUNDS_LENGTH, h)
        p2 = imgToScr(w + BOUNDS_LENGTH, h)
        g.drawLine(p1.x, p1.y, p2.x, p2.y)
    }

    protected open fun drawSourceImage(g: Graphics) {
        val img = getImage()
        if (img != null) {
            drawImage(img, g)
        }
    }

    fun getImage(): BufferedImage? {
        return customImage
    }

    protected fun drawImage(img: BufferedImage, g: Graphics) {
        drawImage(img, 0, 0, g)
    }

    protected fun drawImage(img: BufferedImage, x: Int, y: Int, g: Graphics) {
        val p1 = imgToScr(Vec2i(x, y))
        val p2 = Vec2i(imgToScr(img.width), imgToScr(img.height))
        g.drawImage(img, p1.x, p1.y, p2.x, p2.y, null)
    }

    protected fun drawLine(g: Graphics, x1: Int, y1: Int, x2: Int, y2: Int) {
        val p1 = imgToScr(x1, y1)
        val p2 = imgToScr(x2, y2)
        g.drawLine(p1.x, p1.y, p2.x, p2.y)
    }

    protected open fun drawBefore(g: Graphics) {
        // empty
    }

    protected open fun drawAfter(g: Graphics) {
        // empty
    }

    protected open fun drawTileInfo(g: Graphics) {
        val p = scrToImg(currMousePos)
        val text = getInfoText(p)

        g.font = infoFont

        g.color = infoBgColor
        val gap2 = 2 * infoGap
        g.fillRect(0, height - infoFontSize - gap2, infoWidth, infoFontSize + gap2)

        g.color = infoTextColor
        g.drawString(text, infoGap, height - infoGap)
    }

    protected open fun getInfoText(p: Vec2i): String {
        var text = "coords: $p"

        val image = getImage()
        if (image != null && p.x >= 0 && p.x < image.width && p.y >= 0 && p.y < image.height) {
            val color = image.getRGB(p.x, p.y)
            text += ", color: ${ColorUtils.toString(color)}"
        }
        return text
    }

    protected fun getImageWidth(): Int {
        return customImage!!.width
    }

    protected fun getImageHeight(): Int {
        return customImage!!.height
    }

    protected fun scrToImg(p: Vec2i): Vec2i {
        if (customImage == null) {
            return Vec2i.NEGATIVE
        }
        val x = (p.x - width / 2.0) / zoom
        val y = (p.y - height / 2.0) / zoom
        return Vec2i(
            Vec2d(
                x - posX + getImageWidth() / 2.0,
                y - posY + getImageHeight() / 2.0
            )
        )
    }

    protected fun imgToScr(p: Vec2i): Vec2i {
        return imgToScr(p.toDouble())
    }

    protected fun imgToScr(p: Vec2d): Vec2i {
        if (customImage == null) {
            return Vec2i.ZERO
        }
        val x = p.x + posX - getImageWidth() / 2.0
        val y = p.y + posY - getImageHeight() / 2.0
        return Vec2i((x * zoom + width / 2.0).roundToInt(), (y * zoom + height / 2.0).roundToInt())
    }

    protected fun scrToImg(x: Int): Int {
        return (x / zoom).toInt()
    }

    protected fun imgToScr(x: Double): Int {
        return (x * zoom).roundToInt()
    }

    protected fun imgToScr(x: Int): Int {
        return imgToScr(x.toDouble())
    }

    protected fun imgToScr(x: Int, y: Int): Vec2i {
        return imgToScr(Vec2d(x.toDouble(), y.toDouble()))
    }

    override fun mouseEntered(e: MouseEvent) {
        mouseInside = true
    }

    override fun mouseExited(e: MouseEvent) {
        mouseInside = false
    }

    override fun mousePressed(e: MouseEvent) {
        if (e.button == MouseEvent.BUTTON1) {
            mouseLDown = true
        } else if (e.button == MouseEvent.BUTTON3) {
            mouseRDown = true
            lastX = e.x
            lastY = e.y
        }
    }

    override fun mouseReleased(e: MouseEvent) {
        if (e.button == MouseEvent.BUTTON1) {
            mouseLDown = false
        } else if (e.button == MouseEvent.BUTTON3) {
            mouseRDown = false
        }
    }

    override fun mouseClicked(e: MouseEvent) {
        // empty
    }

    override fun mouseMoved(e: MouseEvent) {
        lastX = e.x
        lastY = e.y
        currMousePos = Vec2i(e.x, e.y)
        if (drawInfo) {
            drawTileInfo(graphics)
        }
    }

    override fun mouseDragged(e: MouseEvent) {
        if (mouseRDown) {
            posX += (e.x - lastX) / zoom
            posY += (e.y - lastY) / zoom
            refresh()
        }
        lastX = e.x
        lastY = e.y
        currMousePos = Vec2i(e.x, e.y)
        if (drawInfo) {
            drawTileInfo(graphics)
        }
    }

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        val rotated = e.wheelRotation
        if (rotated > 0) {
            zoom /= zoomSpeed
            refresh()
        } else if (rotated < 0) {
            zoom *= zoomSpeed
            refresh()
        }
    }

    override fun keyTyped(e: KeyEvent) {
        // empty
    }

    override fun keyPressed(e: KeyEvent) {
        GuiUtils.runCatch(this) {
            if (e.keyCode == KeyEvent.VK_HOME) {
                zoom = 1.0
                posX = 0.0
                posY = 0.0
                refresh()
            }
        }
    }

    override fun keyReleased(e: KeyEvent) {
        // empty
    }

    /** Redraw component using buffer, used when user drew/selected something */
    fun refresh() {
        val g = canvasBuffer.start()
        paintComponent(g)
        canvasBuffer.finish()
        g.dispose()
    }

    /** Redraw component without buffer, used by swing */
    override fun paint(g: Graphics) {
        paintComponent(g)
    }

    companion object {
        private const val BOUNDS_LENGTH = 20
    }
}
