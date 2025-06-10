package cz.wa.smoothmetalmap.settings

import cz.wa.smoothmetalmap.commands.channelitem.AggrFunc

data class ChannelsSettings (
    var simpleDefinition: Boolean = true,
    var simpleRoughness: Boolean = true,
    var targetR: String = "R1G1B1",
    var targetG: String = "0",
    var targetB: String = "0",
    var targetA: String = "-R2-G2-B2",
    var aggrFuncR: AggrFunc = AggrFunc.AVG,
    var aggrFuncG: AggrFunc = AggrFunc.AVG,
    var aggrFuncB: AggrFunc = AggrFunc.AVG,
    var aggrFuncA: AggrFunc = AggrFunc.AVG,
    var alphaMin1: Boolean = false,
)
