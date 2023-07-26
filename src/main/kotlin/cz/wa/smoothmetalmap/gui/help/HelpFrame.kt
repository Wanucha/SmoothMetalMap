package cz.wa.smoothmetalmap.gui.help

import java.awt.BorderLayout
import java.awt.Rectangle
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JScrollPane

class HelpFrame: JFrame("Smooth Metal Map Help") {
    init {
        bounds = Rectangle(200, 200, 500, 300)

        layout = BorderLayout()

        var text1 = JLabel()
        text1.text = "<html>" +
                "Merges metallic and smoothness/roughness map to be used in Unity" +
                "<ol>" +
                "<li>Drag image to metallic window</li>" +
                "<li>Drag image to smoothness window" +
                "<ul>" +
                "<li>If the map represents roughness, keep the checkbox selected</li>" +
                "<li>If the map represents smoothness, uncheck the checkbox</li>" +
                "</ul></li>" +
                "<li>If you don't have one of the maps, you can generate it" +
                "<ul>" +
                "<li>Load the map that you have</li>" +
                "<li>Click Generate map</li>" +
                "<li>Choose intensity</li>" +
                "<li>Generate the missing map, the dimensions will be the same</li>" +
                "</ul></li>" +
                "<li>Click Generate</li>" +
                "<li>Save the file as .png</li>" +
                "</ol>" +
                "</html>"

        var scroll = JScrollPane(text1)
        add(scroll, BorderLayout.CENTER)
    }
}