package cz.wa.smoothmetalmap.settings

data class ChannelsSettings (
    var simpleDefinition: Boolean = true,
    var simpleRoughness: Boolean = true,
    var targetR: String = "R1G1B1",
    var targetG: String = "0",
    var targetB: String = "0",
    var targetA: String = "-R2-G2-B2",
    var alphaMin1: Boolean = false,
)
