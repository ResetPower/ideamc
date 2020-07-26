package org.imcl.launch

import javafx.geometry.Insets
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.imcl.constraints.VERSION_CODE
import org.imcl.constraints.VERSION_NAME
import org.imcl.constraints.logger
import org.imcl.lang.Translator
import java.awt.Desktop
import java.net.URI

object AboutFragment {
    @JvmStatic
    fun get(translator: Translator) = BorderPane().apply {
        logger.info("Generating AboutFragment")
        top = Label("")
        bottom = Label("")
        left = Label("")
        right = Label("")
        BorderPane.setMargin(this, Insets(100.0, 100.0, 150.0, 100.0))
        center = VBox().apply {
            background = Background(BackgroundFill(Color(1.0, 1.0, 1.0, 0.5), null, null))
            children.addAll(
                Label("IDEA Minecraft Launcher"),
                HBox().apply {
                    children.addAll(
                        Label("GitHub: "),
                        Hyperlink("https://github.com/resetpower/ideamc").apply {
                            setOnAction {
                                logger.info("Clicked Link [GitHub](https://github.com/resetpower/ideamc)")
                                val desktop = Desktop.getDesktop()
                                if (Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE)) {
                                    val uri = URI(text)
                                    desktop.browse(uri)
                                }
                            }
                        }
                    )
                },
                Label("${translator.get("versionname")}: $VERSION_NAME"),
                Label("${translator.get("versioncode")}: $VERSION_CODE"),
                HBox().apply {
                    children.addAll(
                        Label("${translator.get("submiterroratgithub")}"),
                        Hyperlink("https://github.com/resetpower/ideamc/issues").apply {
                            setOnAction {
                                logger.info("Clicked Link [Submit Error at GitHub](https://github.com/resetpower/ideamc/issues)")
                                val desktop = Desktop.getDesktop()
                                if (Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE)) {
                                    val uri = URI(text)
                                    desktop.browse(uri)
                                }
                            }
                        }
                    )
                },
                HBox().apply {
                    children.addAll(
                        Label("${translator.get("seewikiatgithub")}"),
                        Hyperlink("https://github.com/resetpower/ideamc/wiki").apply {
                            setOnAction {
                                logger.info("Clicked Link [See Wiki at GitHub](https://github.com/resetpower/ideamc/wiki)")
                                val desktop = Desktop.getDesktop()
                                if (Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE)) {
                                    val uri = URI(text)
                                    desktop.browse(uri)
                                }
                            }
                        }
                    )
                },
                Label("${translator.get("opensourcesoftware")}")
            )
        }
    }
}