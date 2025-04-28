package cz.wa.smoothmetalmap

import cz.wa.smoothmetalmap.gui.MainFrame
import cz.wa.smoothmetalmap.settings.Settings
import cz.wa.smoothmetalmap.settings.io.SettingsIO
import java.io.File

class SmoothMetalMapMain {

    companion object {
        const val VERSION = "0.2.3"
        val IMAGE_OPEN_EXTS = arrayOf("png", "jpg", "jpeg", "gif", "bmp")
        val IMAGE_SAVE_EXTS = arrayOf("png", "gif", "bmp")

        @JvmStatic
        fun main(args: Array<String>) {
            println(printTitle())
            var settings = Settings()
            var files = emptyList<String>()
            var settingsFile: File? = null
            try {
                if (args.isNotEmpty()) {
                    val settingsFileName = findSettings(args)
                    if (settingsFileName.isNotEmpty()) {
                        settingsFile = File(settingsFileName)
                        settings = SettingsIO.load(settingsFile)
                    }
                }
            } catch (e: Throwable) {
                println(printUsage())
                println()
                println("Error parsing arguments:")
                printMessages(e)
                throw e
            }
            MainFrame(settings, settingsFile)
        }

        private fun findSettings(args: Array<String>): String {
            var ret = ""
            for (arg in args) {
                if (arg.endsWith(".yml", true) || arg.endsWith(".yaml", true)                    ) {
                    if (ret.isEmpty()) {
                        ret = arg
                    } else {
                        println("Multiple settings, ignored: $arg")
                    }
                }
            }
            return ret
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
                    "https://github.com/Wanucha/SmoothMetalMap\n" +
                    "----------------------------\n\n"
        }

        fun printUsage(): String {
            return "Usage:\n" +
                    "file path as an argument - open the file at start (only supports settings)\n" +
                    "TODO implement commands for batching"
        }
    }
}
