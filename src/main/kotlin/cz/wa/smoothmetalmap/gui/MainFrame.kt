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
import java.awt.GridLayout
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

    private var simplePanel = JPanel()
    private var advancedPanel = JPanel(GridLayout(4, 1))

    private var fieldR = JTextField("R1G1B1")
    private var fieldG = JTextField("0")
    private var fieldB = JTextField("0")
    private var fieldA = JTextField("-R2-G2-B2")

    private var simpleCB = JCheckBox("Preset for Unity")
    private var roughnessCB = JCheckBox("Roughness")
    private var noAlpha0CB = JCheckBox("Min alpha = 1")

    private val simpleMapFrame = SimpleMapFrame(leftImage, rightImage)

    init {
        instance = this
        title = "Smooth Metal Map v${SmoothMetalMapMain.VERSION}"
        defaultCloseOperation = EXIT_ON_CLOSE
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
        boundsCb.addActionListener { contentHolder.settings.gui.showBounds = boundsCb.isSelected }
        menu.add(boundsCb)

        // bg color
        val bgColor = ColorSlider()
        bgColor.toolTipText = "Change background color"
        bgColor.addListener { bgColorChanged(it) }
        menu.add(bgColor)

        // Images
        // Left
        leftImage.addListener { file, image -> leftImageUpdated(file, image) }

        val leftPanel = JPanel(BorderLayout())
        leftPanel.add(BorderLayout.NORTH, leftLabel)
        leftPanel.add(BorderLayout.CENTER, leftImage)

        // Right
        rightImage.addListener { file, image -> rightImageUpdated(file, image) }

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
        var panel1 = JPanel(GridLayout(2, 1))
        panel1.add(JLabel("Drag images to metallic and smoothness"))
        simpleCB.isSelected = true
        simpleCB.addChangeListener { onSimpleChanged() }
        panel1.add(simpleCB)
        toolPanel.add(panel1)

        // Generate map button
        val generateMapB = JButton("Generate missing")
        generateMapB.addActionListener { openGenerateMap() }
        toolPanel.add(generateMapB)

        // Simple controls
        simplePanel.add(JLabel("                     "))

        // Advanced controls
        initAdvancedPanel()

        toolPanel.add(simplePanel)
        advancedPanel.isVisible = false
        toolPanel.add(advancedPanel)

        // check boxes
        val cbPanel = JPanel(GridLayout(2, 1))

        roughnessCB.isSelected = true
        roughnessCB.toolTipText = "Check if the input smoothness texture is roughness, inverts alpha"
        cbPanel.add(roughnessCB)

        noAlpha0CB.toolTipText = "If the alpha value is 0, will change it to 1 to prevent discarding color"
        cbPanel.add(noAlpha0CB)

        toolPanel.add(cbPanel)

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

    private fun initAdvancedPanel() {
        var p1 = JPanel()
        p1.add(JLabel("R ="))
        fieldR.columns = 8
        p1.add(fieldR)
        advancedPanel.add(p1)

        var p2 = JPanel()
        p2.add(JLabel("G ="))
        fieldG.columns = 8
        p2.add(fieldG)
        advancedPanel.add(p2)

        var p3 = JPanel()
        p3.add(JLabel("B ="))
        fieldB.columns = 8
        p3.add(fieldB)
        advancedPanel.add(p3)

        var p4 = JPanel()
        p4.add(JLabel("A ="))
        fieldA.columns = 8
        p4.add(fieldA)
        advancedPanel.add(p4)
    }

    private fun onSimpleChanged() {
        if (simpleCB.isSelected) {
            advancedPanel.isVisible = false
            simplePanel.isVisible = true
            roughnessCB.isEnabled = true
        } else {
            simplePanel.isVisible = false
            advancedPanel.isVisible = true
            roughnessCB.isEnabled = false
        }
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
            val img = MergeMapsCommand(
                leftImage.getImage()!!,
                rightImage.getImage()!!,
                roughnessCB.isSelected,
                noAlpha0CB.isSelected
            ).generateMap()
            contentHolder.outputImage = img
            resultImage.setImage(img)
            resultImage.refresh()
        }
    }

    private fun bgColorChanged(value: Int) {
        contentHolder.settings.gui.backgroundColor = Color(value, value, value)
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
