package org.imcl.main

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXComboBox
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.*
import javafx.stage.Stage
import org.imcl.constraints.Toolkit
import org.imcl.constraints.logger
import org.imcl.lang.Translator

object MainScene {
    val emailLabel = Label("Email or username")
    val passwordLabel = Label("Password")
    val loginButton = JFXButton("Login")
    val offlineButton = JFXButton("Offline")
    @JvmStatic
    fun get(primaryStage: Stage) : Scene {
        logger.info("Initializing MainScene (login page)")
        val scene = Scene(BorderPane().apply {
            var translator = updateLanguage()
            logger.info("Getting language: ${translator.languageName}")
            background = Background(BackgroundImage(
                Image(MainScene::class.java.getResourceAsStream("/org/imcl/bg/bg.png"), 840.0, 502.5, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
            ))
            val list = FXCollections.observableArrayList("English", "简体中文", "繁體中文", "Esperanto", "日本語")
            top = JFXComboBox(list).apply {
                selectionModel.select(Toolkit.getLanguageNameInThatLanguage(translator.languageName))
                selectionModel.selectedIndexProperty().addListener { observable, oldValue, newValue ->
                    logger.info("Updating language, old: ${Toolkit.getLanguageEnglishName(list.get(oldValue as Int))}, new: ${Toolkit.getLanguageEnglishName(list.get(newValue as Int))}")
                    Toolkit.updateLanguage(Toolkit.getLanguageEnglishName(list.get(newValue as Int)))
                    translator = Translator(Toolkit.getCurrentLanguage())
                    updateLanguage()
                }
                BorderPane.setMargin(this, Insets(20.0, 0.0, 100.0, 20.0))
            }
            center = LoginPane(translator, primaryStage)
            bottom = Label("").apply {
                BorderPane.setMargin(this, Insets(250.0, 0.0, 0.0, 0.0))
            }
            left = Label("").apply {
                BorderPane.setMargin(this, Insets(0.0, 220.0, 0.0, 0.0))
            }
            right = Label("").apply {
                BorderPane.setMargin(this, Insets(0.0, 0.0, 0.0, 220.0))
            }
        }, 840.0, 502.0)
        return scene
    }
    fun updateLanguage() : Translator {
        val translator = Translator(Toolkit.getCurrentLanguage())
        emailLabel.text = translator.get("email")
        passwordLabel.text = translator.get("password")
        loginButton.text = translator.get("login")
        offlineButton.text = translator.get("offline")
        return translator
    }
}