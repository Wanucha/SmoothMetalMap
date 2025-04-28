package cz.wa.smoothmetalmap.gui

import cz.wa.smoothmetalmap.settings.Settings
import cz.wa.smoothmetalmap.gui.utils.ImageUtils
import java.awt.image.BufferedImage
import java.io.File

class ContentHolder(
    var settings: Settings,
    var settingsFile: File?
) {

    var sourceLeftImage: BufferedImage? = null
    var sourceRightImage: BufferedImage? = null

    var outputImage: BufferedImage? = null

    var lastFile: File? = null

    private val settingsListeners = HashSet<(Settings) -> Unit>()

    init {
        sourceLeftImage = ImageUtils.createEmptyImage(1, 1)
        sourceRightImage = ImageUtils.createEmptyImage(1, 1)
    }

    fun addSettingsListener(l: (Settings) -> Unit) {
        settingsListeners.add(l)
    }

    fun removeSettingsListener(l: (Settings) -> Unit) {
        settingsListeners.remove(l)
    }

    fun callSettingsListeners() {
        for (l in settingsListeners) {
            l.invoke(settings)
        }
    }
}
