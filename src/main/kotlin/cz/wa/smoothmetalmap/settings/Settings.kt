package cz.wa.smoothmetalmap.settings

data class Settings(
    var gui: GuiSettings = GuiSettings(),
    var channels: ChannelsSettings = ChannelsSettings(),
)
