package cz.wa.smoothmetalmap.gui

import cz.wa.smoothmetalmap.Settings
import cz.wa.smoothmetalmap.gui.utils.ImageUtils
import java.awt.image.BufferedImage
import java.io.File

class ContentHolder() {

    var settings: Settings = Settings()
    var sourceLeftImage: BufferedImage? = null
    var sourceRightImage: BufferedImage? = null

    var outputImage: BufferedImage? = null

    var lastFile: File? = null

    init {
        sourceLeftImage = ImageUtils.createEmptyImage(1, 1)
        sourceRightImage = ImageUtils.createEmptyImage(1, 1)
    }
}