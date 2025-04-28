package cz.wa.smoothmetalmap.commands.channelitem

class ChannelRGBAItem (
    // 0..3 representing R/G/B/A
    val channel: Int,
    // false means inverted
    val positive: Boolean,
    // 0 - metallic, 1 - smoothness
    val sourceTexture: Int,
): ChannelItem()
