package cz.wa.smoothmetalmap.commands.channelitem

class ChannelSource(
    val items: List<ChannelItem>,
    var aggrFunc: AggrFunc = AggrFunc.AVG,
)
