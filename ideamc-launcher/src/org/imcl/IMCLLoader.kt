package org.imcl

import org.imcl.widget.SwingImagePanel
import java.awt.Toolkit
import javax.swing.ImageIcon
import javax.swing.JFrame

class IMCLLoader {
    fun show(): JFrame {
        val jframe = JFrame("IDEA Minecraft Launcher").apply {
            val screen = Toolkit.getDefaultToolkit().screenSize
            setBounds(screen.width/2-420, screen.height/2-251, 840, 502)
            contentPane = SwingImagePanel(ImageIcon(IMCLLoader::class.java.getResource("/org/imcl/bg/loading.png")))
            isUndecorated = true
            isVisible = true
        }
        return jframe
    }
}