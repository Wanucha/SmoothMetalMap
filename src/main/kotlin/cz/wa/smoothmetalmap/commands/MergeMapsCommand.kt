package cz.wa.smoothmetalmap.commands

import cz.wa.smoothmetalmap.commands.channelitem.AggrFunc
import cz.wa.smoothmetalmap.commands.channelitem.ChannelParser
import cz.wa.smoothmetalmap.commands.channelitem.ChannelRGBAItem
import cz.wa.smoothmetalmap.commands.channelitem.ChannelSource
import cz.wa.smoothmetalmap.commands.channelitem.ChannelValueItem
import cz.wa.smoothmetalmap.commands.channelitem.Channels
import cz.wa.smoothmetalmap.gui.math.ColorUtils
import cz.wa.smoothmetalmap.gui.utils.ImageUtils
import cz.wa.smoothmetalmap.image.Texture
import cz.wa.smoothmetalmap.settings.Settings
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

class MergeMapsCommand(val settings: Settings) {

	val channels: Channels?
	val valueList = ArrayList<Int>(0)

	init {
		with(settings.channels) {
			if (simpleDefinition) {
				channels = null
			} else {
				channels = ChannelParser.parseChannels(targetR, targetG, targetB, targetA)
				channels.sourceR.aggrFunc = settings.channels.aggrFuncR
				channels.sourceG.aggrFunc = settings.channels.aggrFuncG
				channels.sourceB.aggrFunc = settings.channels.aggrFuncB
				channels.sourceA.aggrFunc = settings.channels.aggrFuncA
			}
		}
	}

	fun generateMap(metallicMap: BufferedImage, smoothnessMap: BufferedImage): BufferedImage {
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
		if (settings.channels.simpleDefinition) {
			convertPixelSimple(inTexM, inTexS, x, y, outTex)
		} else {
			convertPixelAdvanced(inTexM, inTexS, x, y, outTex)
		}
	}

	private fun convertPixelSimple(inTexM: Texture, inTexS: Texture, x: Int, y: Int, outTex: Texture) {
		val pM = inTexM.getPoint(x, y)
		val pS = inTexS.getPoint(x, y)
		val r = getAverageColor(pM)
		var a = getAverageColor(pS)
		if (settings.channels.simpleRoughness) {
			a = 255 - a
		}
		if (settings.channels.alphaMin1 && a <= 0) {
			a = 1
		}
		outTex.setPoint(x, y, ColorUtils.fromRGBA(r, 0, 0, a))
	}

	private fun convertPixelAdvanced(inTexM: Texture, inTexS: Texture, x: Int, y: Int, outTex: Texture) {
		val pM = inTexM.getPoint(x, y)
		val pS = inTexS.getPoint(x, y)

		channels!!
		val r = transformColor(pM, pS, channels.sourceR)
		val g = transformColor(pM, pS, channels.sourceG)
		val b = transformColor(pM, pS, channels.sourceB)
		var a = transformColor(pM, pS, channels.sourceA)

		if (settings.channels.alphaMin1 && a <= 0) {
			a = 1
		}
		outTex.setPoint(x, y, ColorUtils.fromRGBA(r, g, b, a))
	}

	private fun getAverageColor(c: Int): Int {
		return ((ColorUtils.getRed(c) + ColorUtils.getGreen(c) + ColorUtils.getBlue(c)) / 3f).roundToInt()
	}

	private fun transformColor(pM: Int, pS: Int, channel: ChannelSource): Int {
		val item = channel.items[0]
		// single value item
		if (item is ChannelValueItem) {
			return item.value
		}

		valueList.clear()
		// definition
		for (item in channel.items) {
			if (item is ChannelRGBAItem) {
				valueList.add(getValue(pM, pS, item))
			} else {
				throw IllegalArgumentException("Unexpected item type $item")
			}
		}
		val ret = when (channel.aggrFunc) {
			AggrFunc.AVG -> valueList.average().roundToInt()
			AggrFunc.MIN -> valueList.min()
			AggrFunc.MAX -> valueList.max()
		}
		valueList.clear()
		return ret
	}

	private fun getValue(pM: Int, pS: Int, item: ChannelRGBAItem): Int {
		val c = if (item.sourceTexture == 0) pM else pS
		var ret = ColorUtils.getChannelValue(c, item.channel)
		if (!item.positive) {
			ret = 255 - ret
		}
		return ret
	}
}
