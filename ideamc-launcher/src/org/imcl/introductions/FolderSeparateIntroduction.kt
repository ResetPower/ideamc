package org.imcl.introductions

import com.jfoenix.controls.JFXTabPane
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.stage.Stage

class FolderSeparateIntroduction : Stage() {
    init {
        scene = Scene(JFXTabPane().apply {
            tabs.addAll(Tab("简体中文").apply {
                content = Label("""
                    文件夹分割功能可以使你的版本文件与资源文件仍存在原文件夹，而模组文件夹和地图文件夹等则存放在你输入的「游戏文件夹」内。
                """.trimIndent())
            }, Tab("繁體中文").apply {
                content = Label("""
                    文件夾分割功能可以使你的版本文件與資源文件仍存在原文件夾，而模組文件夾和地圖文件夾等則存放在你輸入的「遊戲文件夾」內。
                """.trimIndent())
            }, Tab("English").apply {
                content = Label("""
                    The Folder Separate Function can make your versions and assets files still save in the formerly folder,
                    but mods and maps etc. files save in the "game directory" that you input.
                """.trimIndent())
            }, Tab("日本語").apply {
                content = Label("""
                    フォルダー分離機能では、バージョンとアセットファイルを前のフォルダーに保持できますが、
                    モジュールやマップなどのファイルは、入力した「ゲームディレクトリ」に保存されます。
                """.trimIndent())
            }, Tab("Esperanto").apply {
                content = Label("""
                    La aparta dosieruja funkcio permesas vin konservi version kaj aktivajn dosierojn en la antaŭa dosierujo,
                    sed dosieroj kiel mods kaj mapoj estos konservitaj en la "luddosierujo", kiun vi enigas.
                """.trimIndent())
            })
        }, 850.0, 600.0)
    }
}