package cz.wa.smoothmetalmap.commands.channelitem

import java.util.Locale.getDefault

object ChannelParser {
    val regex = Regex("[\\-RGBA12]+")

    fun parseChannels(strR: String, strG: String, strB: String, strA: String): Channels {
        return Channels(
            parseChannel(strR, "R"),
            parseChannel(strG, "G"),
            parseChannel(strB, "B"),
            parseChannel(strA, "A"),
        )
    }

    private fun parseChannel(str: String, channel: String): ChannelSource {
        val str = str.replace(" ", "").replace("\t", "").uppercase(getDefault())
        if (str.isBlank()) {
            throw IllegalArgumentException("${getErrorPrefix(channel)} is empty")
        }

        // try parse single value
        if (str[0].isDigit()) {
            val value = Integer.parseInt(str)
            if (value < 0 || value > 255) {
                throw IllegalArgumentException("${getErrorPrefix(channel)} value is outside range 0..255 $value")
            }
            return ChannelSource(listOf(ChannelItemValue(value)))
        }

        // try parse multiple source channels
        if (!str.matches(regex)) {
            throw IllegalArgumentException("${getErrorPrefix(channel)} can only contain characters: -RGBA12")
        }

        return parseChannelSource(str, channel)
    }

    private fun parseChannelSource(input: String, channelDesc: String): ChannelSource {
        val items = mutableListOf<ChannelItem>()
        var i = 0
        val length = input.length

        while (i < length) {
            var positive = true
            if (input[i] == '-') {
                positive = false
                i++
                if (i >= length) {
                    throw IllegalArgumentException("${getErrorPrefix(channelDesc)} Invalid input: dangling '-'")
                }
            }

            val channelChar = input[i++]
            val channel = when (channelChar) {
                'R' -> 0
                'G' -> 1
                'B' -> 2
                'A' -> 3
                else -> throw IllegalArgumentException("${getErrorPrefix(channelDesc)} Invalid channel character: $channelChar")
            }

            if (i >= length) {
                throw IllegalArgumentException("${getErrorPrefix(channelDesc)} Missing texture index after $channelChar")
            }

            val textureChar = input[i++]
            val sourceTexture = when (textureChar) {
                '1' -> 0
                '2' -> 1
                else -> throw IllegalArgumentException("${getErrorPrefix(channelDesc)} Invalid texture index: $textureChar")
            }

            items.add(ChannelRGBAItem(channel, positive, sourceTexture))
        }

        if (items.isEmpty()) {
            throw IllegalArgumentException("${getErrorPrefix(channelDesc)} empty definition")
        }
        return ChannelSource(items)
    }

    private fun getErrorPrefix(channel: String): String {
        return "Channel definition for $channel:"
    }
}
