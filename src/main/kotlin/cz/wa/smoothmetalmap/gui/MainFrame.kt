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
import cz.wa.smoothmetalmap.settings.Settings
import cz.wa.smoothmetalmap.settings.io.SettingsIO
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class MainFrame(settings: Settings, settingsFile: File?) : JFrame() {
    private val menu: JMenuBar = JMenuBar()
    private val help: HelpFrame = HelpFrame()
    private val propsLabel = JMenuItem("= ")
    private val advancedHelp: HelpFrame = HelpFrame()
    private val imageSaveChooser = ConfirmFileChooser()
    private val propsOpenChooser = JFileChooser()
    private val propsSaveChooser = ConfirmFileChooser()

    private val imagesFilter = FileNameExtensionFilter("Images (PNG)", "png")
    private val splitSource = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
    private val splitMain = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
    private val leftLabel = JLabel("Metallic map [none]")
    private val rightLabel = JLabel("Smoothness map [none]")

    private val contentHolder: ContentHolder
    private val leftImage: DropTextureViewer
    private val rightImage: DropTextureViewer
    private val resultImage: TextureViewer

    private val simplePanel = JPanel()
    private val advancedPanel = JPanel(GridLayout(4, 1))

    private val fieldR = JTextField("R1G1B1")
    private val fieldG = JTextField("0")
    private val fieldB = JTextField("0")
    private val fieldA = JTextField("-R2-G2-B2")
    private val advHelpB = JButton("Advanced help")

    private val simpleCB = JCheckBox("Preset for Unity")
    private val roughnessCB = JCheckBox("Roughness")
    private val alphaMin1CB = JCheckBox("Min alpha = 1")

    private val simpleMapFrame: SimpleMapFrame

    init {
        instance = this
        title = "Smooth Metal Map v${SmoothMetalMapMain.VERSION}"
        defaultCloseOperation = EXIT_ON_CLOSE
        try {
            iconImages = loadIcons()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        contentHolder = ContentHolder(settings, settingsFile)
        leftImage = DropTextureViewer(contentHolder)
        rightImage = DropTextureViewer(contentHolder)
        resultImage = TextureViewer(contentHolder)
        simpleMapFrame = SimpleMapFrame(leftImage, rightImage)

        initComponents()
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val initW = 850
        val initH = 600
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

        // settings
        val settingsMenu = JMenu("Settings")
        menu.add(settingsMenu)

        val openProp = JMenuItem("Open")
        openProp.addActionListener { openSettings() }
        openProp.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK)
        settingsMenu.add(openProp)
        propsOpenChooser.fileFilter = FileNameExtensionFilter("Settings (.yml, .yaml, .properties)", "yml", "yaml", "properties")

        val saveProp = JMenuItem("Save as")
        saveProp.addActionListener { saveSettings() }
        saveProp.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK)
        settingsMenu.add(saveProp)
        propsSaveChooser.fileFilter = FileNameExtensionFilter("Settings (.yml, .yaml)", "yml", "yaml")

        // props label
        propsLabel.isEnabled = false
        propsLabel.text = "= ${contentHolder.settingsFile?.name}"
        menu.add(propsLabel)

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
        val panel1 = JPanel(GridLayout(3, 1))
        panel1.add(JLabel("Drag images to metallic and smoothness"))
        simpleCB.isSelected = true
        simpleCB.addChangeListener { onSimpleChanged() }
        panel1.add(simpleCB)

        advHelpB.isVisible = false
        advHelpB.addActionListener { showAdvancedHelp() }
        panel1.add(advHelpB)

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

        alphaMin1CB.toolTipText = "If the alpha value is 0, will change it to 1 to prevent discarding color"
        cbPanel.add(alphaMin1CB)

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

        SwingUtilities.invokeLater { initComponentsLater() }
    }

    private fun initAdvancedPanel() {
        val p1 = JPanel()
        p1.add(JLabel("R ="))
        fieldR.columns = 8
        p1.add(fieldR)
        advancedPanel.add(p1)

        val p2 = JPanel()
        p2.add(JLabel("G ="))
        fieldG.columns = 8
        p2.add(fieldG)
        advancedPanel.add(p2)

        val p3 = JPanel()
        p3.add(JLabel("B ="))
        fieldB.columns = 8
        p3.add(fieldB)
        advancedPanel.add(p3)

        val p4 = JPanel()
        p4.add(JLabel("A ="))
        fieldA.columns = 8
        p4.add(fieldA)
        advancedPanel.add(p4)

        advancedHelp.size = Dimension(550, 350)
        advancedHelp.setText("<html>" +
                "For each output color channel define its source:\n" +
                "<ul>" +
                "<li>Either write exact number value 0..255</li>" +
                "<li>Or define channels from source textures:</li>" +
                "<ul>" +
                "<li>Channel with texture index: R1, G1, B1, A1 or R2, G2, B2, A2</li>" +
                "<li>Each channel can be inverted using -: -R1, -G2</li>" +
                "<li>If multiple source channels are defined, the result will be averaged</li>" +
                "</ul>" +
                "<li>Examples:</li>" +
                "<ul>" +
                "<li>0 - sets value to 0</li>" +
                "<li>255 - sets value to 255</li>" +
                "<li>G2 - takes green channel from second texture</li>" +
                "<li>-R1 - takes inverted red channel from first texture</li>" +
                "<li>R1G1B1 - takes RGB values from first texture and averages</li>" +
                "<li>R2A2 - takes red and alpha from second texture and averages</li>" +
                "</ul>" +
                "</ul>" +
                "</html>")
    }

    private fun initComponentsLater() {
        propsOpenChooser.currentDirectory = contentHolder.settingsFile
        propsSaveChooser.currentDirectory = contentHolder.settingsFile
    }

    private fun onSimpleChanged() {
        if (simpleCB.isSelected) {
            advancedPanel.isVisible = false
            simplePanel.isVisible = true
            roughnessCB.isEnabled = true
            advHelpB.isVisible = false
        } else {
            simplePanel.isVisible = false
            advancedPanel.isVisible = true
            roughnessCB.isEnabled = false
            advHelpB.isVisible = true
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
        applySettings()
        GuiUtils.runCatch(this) {
            val img = MergeMapsCommand(contentHolder.settings)
                .generateMap(leftImage.getImage()!!, rightImage.getImage()!!)
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

    private fun applySettings() {
        with (contentHolder.settings.channels) {
            simpleDefinition = simpleCB.isSelected
            simpleRoughness = roughnessCB.isSelected
            targetR = fieldR.text
            targetG = fieldG.text
            targetB = fieldB.text
            targetA = fieldA.text
            alphaMin1 = alphaMin1CB.isSelected
        }
    }

    private fun loadIcons(): List<BufferedImage> {
        return listOf(
            ImageIO.read(MainFrame::class.java.getResourceAsStream("/icon16.png")),
            ImageIO.read(MainFrame::class.java.getResourceAsStream("/icon32.png")),
            ImageIO.read(MainFrame::class.java.getResourceAsStream("/icon64.png"))
        )
    }

    private fun openSettings() {
        if (propsOpenChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            GuiUtils.runCatch(this) {
                val file = propsOpenChooser.selectedFile
                contentHolder.settings = SettingsIO.load(file)
                propsLabel.text = "= ${contentHolder.settingsFile?.name}"
                SwingUtilities.invokeLater {
                    contentHolder.callSettingsListeners()
                }
            }
        }
    }

    private fun saveSettings() {
        if (propsSaveChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            var file = propsSaveChooser.selectedFile
            if (file.extension.isBlank()) {
                file = File(file.path + ".yml")
            }
            GuiUtils.runCatch(this) {
                SettingsIO.save(file, contentHolder.settings)
                if (!file.isFile) {
                    JOptionPane.showMessageDialog(this@MainFrame, "File not saved: ${file.absolutePath}")
                }
            }
        }
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

    private fun showAdvancedHelp() {
        advancedHelp.isVisible = true
    }

    private fun showHelp() {
        help.isVisible = true
    }

    companion object {
        var instance: MainFrame? = null
    }
}
