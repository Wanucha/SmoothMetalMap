package cz.wa.smoothmetalmap.settings

data class ChannelsSettings (
    val simpleDefinition: Boolean = true,
    val simpleRoughness: Boolean = true,
    val targetR: String = "R1G1B1",
    val targetG: String = "0",
    val targetB: String = "0",
    val targetA: String = "-R2-G2-B2",
    val alphaMin1: Boolean = false,
)
