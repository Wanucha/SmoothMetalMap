package cz.wa.smoothmetalmap.gui.simplemap

import cz.wa.smoothmetalmap.gui.math.ColorUtils
import cz.wa.smoothmetalmap.gui.texturecanvas.DropTextureViewer
import cz.wa.smoothmetalmap.gui.utils.GuiUtils
import cz.wa.smoothmetalmap.gui.utils.ImageUtils
import cz.wa.smoothmetalmap.image.Texture
import java.awt.BorderLayout
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.image.BufferedImage
import javax.swing.*

class SimpleMapFrame(
    val metallicViewer: DropTextureViewer,
    val smoothnessViewer: DropTextureViewer
) : JFrame("Generate uniform map") {

    private val intensityTf = JTextField()
    private val intensitySlider = JSlider()

    init {
        initComponents()
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val initW = 340
        val initH = 120
        isResizable = false
        bounds = Rectangle((screenSize.width - initW) / 2, (screenSize.height - initH) / 2, initW, initH)
    }

    private fun initComponents() {
        val p1 = JPanel()
        add(p1, BorderLayout.CENTER)

        // text field
        intensityTf.columns = 4
        intensityTf.text = "0"
        p1.add(GuiUtils.createValuePanel("Intensity", intensityTf))

        // slider
        intensitySlider.minimum = 0
        intensitySlider.maximum = 255
        intensitySlider.value = 0
        intensitySlider.addChangeListener { intensityTf.text = intensitySlider.value.toString() }
        p1.add(intensitySlider)

        // buttons
        val p2 = JPanel()

        val generateMetallicB = JButton("Generate metallic")
        generateMetallicB.addActionListener { generateMetallic() }
        p2.add(generateMetallicB)

        val generateSmoothnessB = JButton("Generate smoothness")
        generateSmoothnessB.addActionListener { generateSmoothness() }
        p2.add(generateSmoothnessB)

        p1.add(p2)
    }

    private fun generateMetallic() {
        GuiUtils.runCatch(this) {
            val otherImg = smoothnessViewer.getCustomImage1()
            if (otherImg != null) {
                val img = generateMap(otherImg)
                metallicViewer.setCustomImage1(img)
            } else {
                JOptionPane.showMessageDialog(this, "Load smoothness image first!")
            }
        }
    }

    private fun generateSmoothness() {
        GuiUtils.runCatch(this) {
            val otherImg = metallicViewer.getCustomImage1()
            if (otherImg != null) {
                val img = generateMap(otherImg)
                smoothnessViewer.setCustomImage1(img)
            } else {
                JOptionPane.showMessageDialog(this, "Load metallic image first!")
            }
        }
    }

    private fun generateMap(otherImg: BufferedImage): BufferedImage {
        val img = ImageUtils.createEmptyImage(otherImg.width, otherImg.height)
        val tex = Texture(img)
        val intensity = intensityTf.text.toInt()

        if (intensity < 0 || intensity > 255) {
            throw IllegalArgumentException("Intensity must be 0..255, but is '$intensity'")
        }

        for (y in 0 until tex.height) {
            for (x in 0 until tex.width) {
                tex.setPoint(x, y, ColorUtils.fromRGB(intensity, intensity, intensity))
            }
        }
        return img
    }
}