package org.imcl.main

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXPasswordField
import com.jfoenix.controls.JFXTextField
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.stage.Stage
import org.imcl.constraints.Toolkit
import org.imcl.constraints.logger
import org.imcl.core.authentication.YggdrasilAuthenticator
import org.imcl.core.network.NetworkState
import org.imcl.lang.Translator
import org.imcl.launch.LauncherScene
import org.imcl.users.OfflineUserInformation
import org.imcl.users.UserInformation
import org.imcl.users.YggdrasilUserInformation

class LoginPane(translator: Translator, primaryStage: Stage, state: LoginPaneState = LoginPaneState.DIRECT, callback: (UserInformation) -> Unit = { }): GridPane() {
    init {
        opacity = 0.8
        background = Background(BackgroundFill(Color.WHITE, null, null))
        alignment = Pos.CENTER
        hgap = 10.0
        vgap = 10.0
        padding = Insets(25.0, 25.0, 25.0, 25.0)
        val scenetitle = Text("IMCL")
        scenetitle.font = Font.font("Tahoma", FontWeight.NORMAL, 20.0)
        add(scenetitle, 0, 0, 2, 1)
        add(MainScene.emailLabel, 0, 1)
        val userTextField = JFXTextField()
        add(userTextField, 1, 1)
        add(MainScene.passwordLabel, 0, 2)
        val pwBox = JFXPasswordField()
        add(pwBox, 1, 2)
        add(MainScene.loginButton.apply {
            buttonType = JFXButton.ButtonType.RAISED
            background = Background(BackgroundFill(Color.ALICEBLUE, null, null))
            setOnAction {
                logger.info("Login button clicked")
                if (userTextField.text.trim()==""||pwBox.text.trim()=="") {
                    logger.info("Username or password not input")
                    val alert = Alert(Alert.AlertType.INFORMATION)
                    alert.title = translator.get("usernameorpasswordnotinput")
                    alert.contentText = translator.get("usernameorpasswordnotinput")
                    alert.showAndWait()
                } else {
                    logger.info("Authenticating")
                    val result = YggdrasilAuthenticator.authenticate(userTextField.text, pwBox.text).split(" ")
                    if (result[0]=="true") {
                        logger.info("Authentication successful")
                        Toolkit.obj.getJSONObject("settings").put("isLoggedIn", "true")
                        val acinf = Toolkit.obj.getJSONObject("account")
                        val username = result[1]
                        val uuid = result[2]
                        val accessToken = result[3]
                        acinf.put("username", username)
                        acinf.put("uuid", uuid)
                        acinf.put("accessToken", accessToken)
                        acinf.put("email", userTextField.text)
                        Toolkit.save()
                        if (state==LoginPaneState.DIRECT) {
                            primaryStage.scene = LauncherScene.get(translator, YggdrasilUserInformation(username, uuid, accessToken, userTextField.text), primaryStage)
                        } else if (state==LoginPaneState.CALLBACK) {
                            callback(YggdrasilUserInformation(username, uuid, accessToken, userTextField.text))
                        }
                    } else {
                        logger.info("Unable to authenticate")
                        logger.info("Checking network state")
                        if (NetworkState.isConnectedToInternet()) {
                            logger.info("Network normal. Password error.")
                            val alert = Alert(Alert.AlertType.INFORMATION)
                            alert.title = translator.get("passworderror")
                            alert.contentText = translator.get("passworderror")
                            alert.show()
                        } else {
                            logger.info("Network bad. Network error.")
                            val alert = Alert(Alert.AlertType.INFORMATION)
                            alert.title = translator.get("networkerror")
                            alert.contentText = translator.get("networkerror")
                            alert.show()
                        }
                    }
                }
            }
        }, 0, 3)
        add(MainScene.offlineButton.apply {
            buttonType = JFXButton.ButtonType.RAISED
            background = Background(BackgroundFill(Color.ALICEBLUE, null, null))
            setOnAction {
                logger.info("Offline button clicked")
                if (userTextField.text.trim()=="") {
                    logger.info("Username not input")
                    val alert = Alert(Alert.AlertType.INFORMATION)
                    alert.title = translator.get("usernamenotinput")
                    alert.contentText = translator.get("usernamenotinput")
                    alert.showAndWait()
                } else {
                    logger.info("Logging in. Writing \"isLoggedIn: true\" to json")
                    Toolkit.obj.getJSONObject("settings").put("isLoggedIn", "true")
                    val acinf = Toolkit.obj.getJSONObject("account")
                    acinf.put("username", userTextField.text)
                    acinf.put("uuid", "none")
                    acinf.put("accessToken", "none")
                    Toolkit.save()
                    if (state==LoginPaneState.DIRECT) {
                        primaryStage.scene = LauncherScene.get(translator, OfflineUserInformation(userTextField.text), primaryStage)
                    } else if (state==LoginPaneState.CALLBACK) {
                        callback(OfflineUserInformation(userTextField.text))
                    }
                }
            }
        }, 1, 3)
    }
}