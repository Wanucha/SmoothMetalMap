package cz.wa.smoothmetalmap.commands.channelitem

import java.util.Locale
import java.util.Locale.getDefault
import javax.management.Query.or

object ChannelParser {
    val regex = Regex("[-RGBA12]")
    val rgbaCharacters = listOf('R', 'G', 'B', 'A')

    fun parseChannels(strR: String, strG: String, strB: String, strA: String): Channels {
        return Channels(
            parseChannel(strR),
            parseChannel(strG),
            parseChannel(strB),
            parseChannel(strA),
        )
    }

    private fun parseChannel(str: String): ChannelSource {
        val str = str.replace(" ", "").replace("\t", "").uppercase(getDefault())
        if (str.isNullOrBlank()) {
            throw IllegalArgumentException("Channel definition is empty")
        }

        // try parse single value
        if (str[0].isDigit()) {
            val value = Integer.parseInt(str)
            if (value < 0 || value > 255) {
                throw IllegalArgumentException("Channel value is outside range 0..255 $value")
            }
            return ChannelSource(listOf(ChannelItemValue(value)))
        }

        // try parse multiple source channels
        if (!str.matches(regex)) {
            throw IllegalArgumentException("Channel definition can only contain characters: R G B A - 1 2")
        }

        var items = ArrayList<ChannelRGBAItem>(4)

        val usedChannels = ArrayList<String>(8)

        var itemChannel: Int? = null
        var itemInverted: Boolean? = null
        var itemTexture: Int? = null
        for ((i, c) in str.withIndex()) {
            if (c in rgbaCharacters) {
                if (i + 1 >= str.length) {
                    throw IllegalArgumentException("Channel definition: unspecified texture for channel $c")
                }
                val t = str[i + 1]
                if (!t.isDigit()) {
                    throw IllegalArgumentException("Channel definition: texture must be a number for $c")
                }
            }
        }

        return ChannelSource(items)
    }
}
