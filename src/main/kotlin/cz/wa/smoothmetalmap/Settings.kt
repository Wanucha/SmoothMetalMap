package cz.wa.smoothmetalmap

import java.awt.Color

class Settings(
    var guiBgColor: Color = Color.BLACK,
    var guiShowBounds: Boolean = true,
) {
    companion object {
        const val GUI_BG_COLOR = "gui-bg-color"
        const val GUI_SHOW_BOUNDS = "gui-show_bounds"
    }
}
