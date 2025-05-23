package cz.wa.smoothmetalmap.gui.utils

import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

class ImageUtils {
    companion object {
        fun createEmptyImage(newWidth: Int, newHeight: Int): BufferedImage {
            val ret = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
            val array = getImageArray(ret)
            array.fill(0)
            return ret
        }

        fun copyImage(img: BufferedImage): BufferedImage {
            val ret = createEmptyImage(img.width, img.height)
            ret.graphics.drawImage(img, 0, 0, null)
            return ret
        }

        fun getImageArray(img: BufferedImage): IntArray {
            return (img.raster.dataBuffer as DataBufferInt).data
        }

        fun getImageWithIntBuffer(img: BufferedImage) : BufferedImage {
            return try {
                if (img.raster.dataBuffer is DataBufferInt) {
                    img
                } else {
                    convertToIntBuffer(img)
                }
            } catch (e: Exception) {
                throw RuntimeException("Failed to convert image buffer, try saving the image in different program.", e)
            }
        }

        private fun convertToIntBuffer(img: BufferedImage): BufferedImage {
            val ret = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_ARGB)

            val srcPixels = IntArray(img.width)
            for (y in 0 until img.height) {
                img.getRGB(0, y, img.width, 1, srcPixels, 0, img.width)
                ret.setRGB(0, y, img.width, 1, srcPixels, 0, img.width)
            }
            return ret
        }
    }
}
