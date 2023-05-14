package cz.wa.smoothmetalmap.gui.help

import java.awt.BorderLayout
import java.awt.Rectangle
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JScrollPane

class HelpFrame: JFrame("Smooth Metal Map Help") {
    init {
        bounds = Rectangle(200, 200, 540, 620)

        layout = BorderLayout()

        var text1 = JLabel()
        text1.text = "<html>" +
                "TODO" +
                "</html>"

        var scroll = JScrollPane(text1)
        add(scroll, BorderLayout.CENTER)
    }
}