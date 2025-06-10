package cz.wa.smoothmetalmap.gui.channel

import cz.wa.smoothmetalmap.commands.channelitem.ChannelFunc
import java.awt.LayoutManager
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Line with channel name, text field for input values, combo box for function
 */
class ChannelSelector : JPanel {
    constructor(layout: LayoutManager?, isDoubleBuffered: Boolean) : super(layout, isDoubleBuffered)
    constructor(layout: LayoutManager?) : super(layout)
    constructor(isDoubleBuffered: Boolean) : super(isDoubleBuffered)
    constructor() : super()
    constructor(labelText: String, channelText: String, aggrFunc: ChannelFunc) {
        label.text = labelText
        channelDef.text = channelText
        funcCb.selectedItem = aggrFunc
    }

    private val label = JLabel("C")
    private val channelDef = JTextField("C1C2C3")
    private val funcCb = JComboBox(ChannelFunc.values())

    var labelText: String
        get() = label.text
        set(value) {
            label.text = value
        }

    var channelText: String
        get() = channelDef.text
        set(value) {
            channelDef.text = value
        }

    var aggrFunc: ChannelFunc
        get() = funcCb.selectedItem as ChannelFunc
        set(value) {
            funcCb.selectedItem = value
        }

    init {
        add(label)
        channelDef.columns = 8
        add(channelDef)
        funcCb.selectedIndex = 0
        add(funcCb)
    }
}
