package org.imcl.launch

import javafx.collections.FXCollections
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import org.imcl.core.LaunchOptions
import org.imcl.core.Launcher
import org.imcl.core.authentication.OfflineAuthenticator
import org.imcl.lang.Translator
import org.imcl.struct.StructedList
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*


object LauncherScene {
    @JvmStatic
    fun get(translator: Translator) : Scene {
        val tabPane = TabPane()

        val pro0 = Properties()
        val in0 = FileInputStream("properties/offline.properties")
        pro0.load(in0)
        val acprop = pro0.getProperty("accounts")
        val accounts = StructedList<String>(if (acprop=="") Vector<String>() else Vector(acprop.split(",")), ",")
        in0.close()
        val tab0 = Tab(translator.get("accounts"))
        tab0.isClosable = false
        val borderPane0 = BorderPane()
        val accountList = ListView<Label>()
        accountList.setPrefSize(840.0, 502.0)
        for (i in accounts.list) {
            accountList.items.add(Label(i))
        }
        accountList.selectionModel.selectFirst()
        borderPane0.center = accountList
        val gridPane0 = GridPane()
        gridPane0.hgap = 10.0
        gridPane0.vgap = 10.0
        gridPane0.add(Button(translator.get("add")).apply {
            setOnAction {
                val dialog = TextInputDialog()
                dialog.headerText = translator.get("pleaseinputyourusername")
                dialog.contentText = translator.get("username")
                dialog.title = translator.get("pleaseinputyourusername")
                val result = dialog.showAndWait()
                if (result.isPresent) {
                    val un = result.get()
                    accounts.list.add(un)
                    accountList.items.add(Label(un))
                    pro0.setProperty("accounts", accounts.toString())
                    val out = FileOutputStream("properties/offline.properties")
                    pro0.store(out, "")
                    out.close()
                }
            }
        }, 0, 0)
        gridPane0.add(Button(translator.get("remove")).apply {
            setOnAction {
                val selectedIndex = accountList.selectionModel.selectedIndex
                val selectedItem = accountList.selectionModel.selectedItem
                accounts.list.removeAt(selectedIndex)
                accountList.items.remove(selectedItem)
                pro0.setProperty("accounts", accounts.toString())
                val out = FileOutputStream("properties/offline.properties")
                pro0.store(out, "")
                out.close()
            }
        }, 1, 0)
        borderPane0.bottom = gridPane0
        tab0.content = borderPane0

        val pro1 = Properties()
        val in1 = FileInputStream("properties/game.properties")
        pro1.load(in1)
        val gmprop = pro1.getProperty("gamePaths")
        val gamePaths = StructedList<String>(if (gmprop=="") Vector<String>() else Vector(gmprop.split(",")), ",")
        in1.close()
        val tab1 = Tab(translator.get("game"))
        tab1.isClosable = false
        val borderPane1 = BorderPane()
        val gamePathList = ListView<Label>()
        gamePathList.setPrefSize(840.0, 502.0)
        for (i in gamePaths.list) {
            gamePathList.items.add(Label(i))
        }
        gamePathList.selectionModel.selectFirst()
        borderPane1.center = gamePathList
        val gridPane1 = GridPane()
        gridPane1.hgap = 10.0
        gridPane1.vgap = 10.0
        gridPane1.add(Button(translator.get("add")).apply {
            setOnAction {
                val dialog = TextInputDialog()
                dialog.headerText = translator.get("pleaseinputgamepath")
                dialog.contentText = translator.get("gamepath")//gamepath
                dialog.title = translator.get("pleaseinputgamepath")
                val result = dialog.showAndWait()
                if (result.isPresent) {
                    val un = result.get()
                    gamePaths.list.add(un)
                    gamePathList.items.add(Label(un))
                    pro1.setProperty("gamePaths", gamePaths.toString())
                    val out = FileOutputStream("properties/game.properties")
                    pro1.store(out, "")
                    out.close()
                }
            }
        }, 0, 0)
        gridPane1.add(Button(translator.get("remove")).apply {
            setOnAction {
                val selectedIndex = gamePathList.selectionModel.selectedIndex
                val selectedItem = gamePathList.selectionModel.selectedItem
                gamePaths.list.removeAt(selectedIndex)
                gamePathList.items.remove(selectedItem)
                pro1.setProperty("gamePaths", gamePaths.toString())
                val out = FileOutputStream("properties/game.properties")
                pro1.store(out, "")
                out.close()
            }
        }, 1, 0)
        borderPane1.bottom = gridPane1
        tab1.content = borderPane1

        val tab2 = Tab(translator.get("launch"))
        tab2.isClosable = false
        tab2.content = BorderPane().apply {
            center = Button(translator.get("launch")).apply {
                setOnAction {
                    if (gamePathList.items.size==0||gamePathList.selectionModel.selectedItem==null) {
                        val alert = Alert(Alert.AlertType.INFORMATION)
                        alert.contentText = "Game path not selected"
                        alert.showAndWait()
                    } else if (accountList.items.size==0||accountList.selectionModel.selectedItem==null) {
                        val alert = Alert(Alert.AlertType.INFORMATION)
                        alert.contentText = "Account not selected"
                        alert.showAndWait()
                    } else {
                        val alert = Alert(Alert.AlertType.INFORMATION)
                        alert.title = translator.get("wefoundtheseversions")
                        val pane = Pane()
                        val list = FXCollections.observableArrayList<String>()
                        File(gamePathList.selectionModel.selectedItem.text+"/versions").listFiles().forEach {
                            if (it.isDirectory) {
                                list.add(it.name)
                            }
                        }
                        val choiceBox = ChoiceBox(list)
                        pane.children.add(GridPane().apply {
                            add(choiceBox, 1, 0)
                        })
                        alert.dialogPane.content = pane
                        alert.buttonTypes.addAll(ButtonType(translator.get("launch"), ButtonBar.ButtonData.YES))
                        val buttonType = alert.showAndWait()
                        if (buttonType.get().buttonData==ButtonBar.ButtonData.YES){
                            Launcher.launch(LaunchOptions(gamePathList.selectionModel.selectedItem.text, choiceBox.selectionModel.selectedItem, OfflineAuthenticator(accountList.selectionModel.selectedItem.text)))
                        }
                    }
                }
            }
        }

        tabPane.tabs.addAll(tab0, tab1, tab2)

        return Scene(tabPane)
    }
}