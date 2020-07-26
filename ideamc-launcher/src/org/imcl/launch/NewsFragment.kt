package org.imcl.launch

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import javafx.application.Platform
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import org.imcl.constraints.logger
import org.imcl.core.http.HttpRequestSender
import org.imcl.lang.Translator
import java.awt.Desktop
import java.net.URI

object NewsFragment {
    @JvmStatic
    fun get(translator: Translator) = BorderPane().apply {
        logger.info("Generating NewsFragment")
        top = HBox().apply {
            spacing = 20.0
            children.addAll(Label(translator.get("news")).apply {
                font = Font.font(18.0)
            }, Label("${translator.get("source")} MCBBS").apply {
                font = Font.font(12.0)
            })
        }
        center = Label(translator.get("loading"))
        val box = VBox()
        box.spacing = 10.0
        Thread {
            logger.info("Loading news from https://authentication.x-speed.cc/mcbbsNews/")
            val news = JSON.parseArray(HttpRequestSender.get("https://authentication.x-speed.cc/mcbbsNews/") {})
            logger.info("News loading done")
            if (news==null) {
                Platform.runLater {
                    center = Label("Cannot get news")
                }
                return@Thread
            }
            val iterator = news.iterator()
            val selected = Background(BackgroundFill(Color.DARKGRAY, null, null))
            val default = Background.EMPTY
            while (iterator.hasNext()) {
                val obj = iterator.next() as JSONObject
                val index = news.indexOf(obj)
                box.children.add(VBox().apply {
                    setOnMousePressed {
                        logger.info("MousePressed item@$index, set color to selected color")
                        background = selected
                    }
                    setOnMouseReleased {
                        logger.info("MouseReleased item@$index, set color to default color")
                        background = default
                    }
                    setOnMouseExited {
                        logger.info("MouseExited item@$index, set color to default color")
                        background = default
                    }
                    setOnMouseClicked {
                        val link = obj.getString("link")
                        logger.info("MouseClicked item@$index, open link at $link")
                        Desktop.getDesktop().browse(URI(link))
                    }
                    children.addAll(
                        Label(obj.getString("title")),
                        HBox().apply {
                            spacing = 10.0
                            children.addAll(Label(obj.getString("classify")).apply {
                                font = Font.font(10.0)
                                textFill = Color.GRAY
                            }, Label(obj.getString("time")).apply {
                                font = Font.font(10.0)
                                textFill = Color.GRAY
                            })
                        }
                    )
                })
            }
            Platform.runLater {
                center = ScrollPane(box)
            }
        }.start()
    }
}