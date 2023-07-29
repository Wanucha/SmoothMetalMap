package cz.wa.smoothmetalmap

import cz.wa.smoothmetalmap.gui.MainFrame
import java.io.File

class SmoothMetalMapMain {

    companion object {
        const val VERSION = "0.2.2"
        val IMAGE_OPEN_EXTS = arrayOf("png", "jpg", "jpeg", "gif", "bmp")
        val IMAGE_SAVE_EXTS = arrayOf("png", "gif", "bmp")

        @JvmStatic
        fun main(args: Array<String>) {
            printTitle()
            printUsage()
            MainFrame()
        }

        private fun printMessages(e: Throwable) {
            var ex: Throwable? = e
            while (ex != null) {
                println("${ex.javaClass.simpleName}: ${ex.message}")
                ex = ex.cause
            }
        }

        private fun parseFiles(args: Array<String>): List<String> {
            return args.filter { IMAGE_OPEN_EXTS.contains(File(it).extension.lowercase()) }
        }

        fun printTitle(): String {
            return "Smooth Metal Map v$VERSION\n" +
                    "Created by Ondřej Milenovský\n" +
                    "----------------------------\n\n"
        }

        fun printUsage(): String {
            return "Usage:\n" +
                    "TODO implement commands for batching"
        }
    }
}