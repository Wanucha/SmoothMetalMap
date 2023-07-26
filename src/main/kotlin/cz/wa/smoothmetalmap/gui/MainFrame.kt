package cz.wa.smoothmetalmap.gui

import cz.wa.smoothmetalmap.SmoothMetalMapMain
import cz.wa.smoothmetalmap.commands.MergeMapsCommand
import cz.wa.smoothmetalmap.gui.help.HelpFrame
import cz.wa.smoothmetalmap.gui.simplemap.SimpleMapFrame
import cz.wa.smoothmetalmap.gui.texturecanvas.DropTextureViewer
import cz.wa.smoothmetalmap.gui.texturecanvas.TextureViewer
import cz.wa.smoothmetalmap.gui.utils.ColorSlider
import cz.wa.smoothmetalmap.gui.utils.ConfirmFileChooser
import cz.wa.smoothmetalmap.gui.utils.GuiUtils
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter


class MainFrame : JFrame() {
    private val menu: JMenuBar = JMenuBar()
    private val help: HelpFrame = HelpFrame()
    private val imageSaveChooser = ConfirmFileChooser()
    private val imagesFilter = FileNameExtensionFilter("Images (PNG)", "png")
    private val splitSource = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
    private val splitMain = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
    private val leftLabel = JLabel("Metallic map [none]")
    private val rightLabel = JLabel("Smoothness map [none]")

    private val contentHolder = ContentHolder()
    private var leftImage = DropTextureViewer(contentHolder)
    private var rightImage = DropTextureViewer(contentHolder)
    private var resultImage = TextureViewer(contentHolder)
    private var roughnessCB = JCheckBox("Roughness (invert smooth)")

    private val simpleMapFrame = SimpleMapFrame(leftImage, rightImage)

    init {
        instance = this
        title = "Smooth Metal Map v${SmoothMetalMapMain.VERSION}"
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        try {
            iconImages = loadIcons()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        initComponents()
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val initW = 800
        val initH = 500
        bounds = Rectangle((screenSize.width - initW) / 2, (screenSize.height - initH) / 2, initW, initH)
        splitSource.dividerLocation = bounds.width / 3
        splitMain.dividerLocation = bounds.width * 2 / 3
        isVisible = true
    }

    private fun initComponents() {
        // Menu
        jMenuBar = menu

        // image
        val imageMenu = JMenu("Image")
        menu.add(imageMenu)

        val saveImage = JMenuItem("Save as")
        saveImage.addActionListener { saveImage() }
        saveImage.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)
        imageMenu.add(saveImage)
        imageSaveChooser.fileFilter = imagesFilter

        // args help
        val argsHelp = JMenuItem("Program args")
        argsHelp.addActionListener { showArgsHelp() }
        menu.add(argsHelp)

        // help
        val help = JMenuItem("Help")
        help.addActionListener { showHelp() }
        menu.add(help)

        // show bounds
        val boundsCb = JCheckBox("Show bounds")
        boundsCb.isSelected = true
        boundsCb.addActionListener { contentHolder.settings.guiShowBounds = boundsCb.isSelected }
        menu.add(boundsCb)

        // bg color
        val bgColor = ColorSlider()
        bgColor.toolTipText = "Change background color"
        bgColor.addListener{bgColorChanged(it)}
        menu.add(bgColor)

        // Images
        // Left
        leftImage.addListener{file, image -> leftImageUpdated(file, image)}

        val leftPanel = JPanel(BorderLayout())
        leftPanel.add(BorderLayout.NORTH, leftLabel)
        leftPanel.add(BorderLayout.CENTER, leftImage)

        // Right
        rightImage.addListener{file, image -> rightImageUpdated(file, image)}

        val rightPanel = JPanel(BorderLayout())
        rightPanel.add(BorderLayout.NORTH, rightLabel)
        rightPanel.add(BorderLayout.CENTER, rightImage)

        // Result
        val resultPanel = JPanel(BorderLayout())
        resultPanel.add(BorderLayout.NORTH, JLabel("Result map"))
        resultPanel.add(BorderLayout.CENTER, resultImage)

        // Panels
        splitSource.leftComponent = leftPanel
        splitSource.rightComponent = rightPanel

        splitMain.leftComponent = splitSource
        splitMain.rightComponent = resultPanel

        // Controls
        val toolPanel = JPanel()
        toolPanel.add(JLabel("Drag images to metallic and smoothness"))

        // Generate map button
        val generateMapB = JButton("Generate missing")
        generateMapB.addActionListener { openGenerateMap() }
        toolPanel.add(generateMapB)

        toolPanel.add(JLabel("                     "))

        roughnessCB.isSelected = true
        toolPanel.add(roughnessCB)

        val generateButton = JButton("Generate")
        generateButton.addActionListener { generateMap() }
        toolPanel.add(generateButton)

        val saveButton = JButton("Save as")
        saveButton.addActionListener { saveImage() }
        toolPanel.add(saveButton)

        layout = BorderLayout()
        add(toolPanel, BorderLayout.NORTH)
        add(splitMain, BorderLayout.CENTER)
    }

    private fun leftImageUpdated(file: File?, image: BufferedImage) {
        if (file != null) {
            updateSaveFile(file)
            contentHolder.lastFile = file
        }
        contentHolder.sourceLeftImage = image
        leftLabel.text = "Metallic map [${image.width}x${image.height}]"
    }

    private fun updateSaveFile(file: File) {
        if (contentHolder.lastFile == null) {
            imageSaveChooser.currentDirectory = file.parentFile
        }
    }

    private fun rightImageUpdated(file: File?, image: BufferedImage) {
        if (file != null) {
            contentHolder.lastFile = file
        }
        contentHolder.sourceRightImage = image
        rightLabel.text = "Smoothness map [${image.width}x${image.height}]"
    }

    private fun openGenerateMap() {
        simpleMapFrame.isVisible = true
    }

    private fun generateMap() {
        GuiUtils.runCatch(this) {
            val img = MergeMapsCommand(leftImage.getImage()!!, rightImage.getImage()!!, roughnessCB.isSelected).generateMap()
            contentHolder.outputImage = img
            resultImage.setImage(img)
            resultImage.refresh()
        }
    }

    private fun bgColorChanged(value: Int) {
        contentHolder.settings.guiBgColor = Color(value, value, value)
        leftImage.refresh()
        rightImage.refresh()
        resultImage.refresh()
    }

    private fun loadIcons(): List<BufferedImage> {
        return listOf(
            ImageIO.read(MainFrame::class.java.getResourceAsStream("/icon16.png")),
            ImageIO.read(MainFrame::class.java.getResourceAsStream("/icon32.png")),
            ImageIO.read(MainFrame::class.java.getResourceAsStream("/icon64.png"))
        )
    }

    private fun saveImage() {
        if (contentHolder.outputImage != null) {
            if (imageSaveChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                var file = imageSaveChooser.selectedFile
                if (file.extension.isBlank()) {
                   file = File(file.absolutePath + ".png")
                }

                if (!SmoothMetalMapMain.IMAGE_SAVE_EXTS.contains(file.extension.lowercase())) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Unknown image extension: '${file.extension}'" +
                                "\nType file name with one of supported extensions: ${SmoothMetalMapMain.IMAGE_SAVE_EXTS.joinToString(", ")}"
                    )
                    return
                }
                GuiUtils.runCatch(this) {
                    ImageIO.write(contentHolder.outputImage, file.extension, file)
                    if (!file.isFile) {
                        JOptionPane.showMessageDialog(this@MainFrame, "File not saved: ${file.absolutePath}")
                    }
                }
            }
        }
    }

    private fun showArgsHelp() {
        JOptionPane.showMessageDialog(
            this, "${SmoothMetalMapMain.printTitle()}${SmoothMetalMapMain.printUsage()}"
        )
    }

    private fun showHelp() {
        help.isVisible = true
    }

    companion object {
        var instance: MainFrame? = null
    }
}
