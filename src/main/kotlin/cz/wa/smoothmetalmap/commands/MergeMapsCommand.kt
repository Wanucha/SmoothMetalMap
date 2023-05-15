package cz.wa.smoothmetalmap.commands

import cz.wa.smoothmetalmap.gui.math.ColorUtils
import cz.wa.smoothmetalmap.gui.utils.ImageUtils
import cz.wa.smoothmetalmap.image.Texture
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

class MergeMapsCommand(private val metallicMap: BufferedImage, private val smoothnessMap: BufferedImage, private val roughness: Boolean) {

    fun generateMap(): BufferedImage {
        check(metallicMap.width == smoothnessMap.width && metallicMap.height == smoothnessMap.height) { "The input images must have same dimensions" }

        val w = metallicMap.width
        val h = metallicMap.height

        val ret = ImageUtils.createEmptyImage(w, h)
        val inTexM = Texture(metallicMap)
        val inTexS = Texture(smoothnessMap)
        val outTex = Texture(ret)

        for (y in 0 until h) {
            for (x in 0 until w) {
                convertPixel(inTexM, inTexS, x, y, outTex)
            }
        }
        return ret
    }

    private fun convertPixel(inTexM: Texture, inTexS: Texture, x: Int, y: Int, outTex: Texture) {
        var pM = inTexM.getPoint(x, y)
        var pS = inTexS.getPoint(x, y)
        var r = getAverageColor(pM)
        var a = getAverageColor(pS)
        if (roughness) {
            a = 255 - a
        }
        outTex.setPoint(x, y, ColorUtils.fromRGBA(r, 0, 0, a))
    }

    private fun getAverageColor(c: Int): Int {
        return ((ColorUtils.getRed(c) + ColorUtils.getGreen(c) + ColorUtils.getBlue(c)) / 3f).roundToInt()
    }
}