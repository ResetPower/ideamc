package org.imcl.lang

object LanguageResource {
    @JvmStatic
    fun get(language: Language) : Map<String, String> {
        return when (language) {
            Language.ENGLISH -> englishResource()
            Language.ESPERANTO -> esperantoResource()
            Language.JAPANESE -> japaneseResource()
            Language.CHINESE_SIMPLIFIED -> chineseSimplifiedResource()
            Language.CHINESE_TRADITIONAL -> chineseTraditionalResource()
        }
    }
    @JvmStatic
    fun englishResource() = mapOf(
        Pair("email", "Email or username"),
        Pair("password", "Password"),
        Pair("login", "Login"),
        Pair("offline", "Offline"),
        Pair("accounts", "Accounts"),
        Pair("game", "Game"),
        Pair("add", "Add"),
        Pair("edit", "Edit"),
        Pair("remove", "Remove"),
        Pair("launch", "Launch"),
        Pair("username", "Username"),
        Pair("pleaseinputyourusername", "Please input your username"),
        Pair("newprofile", "New Profile"),
        Pair("name", "Name"),
        Pair("ver", "Version"),
        Pair("dir", "Directory"),
        Pair("cancel", "Cancel"),
        Pair("refresh", "Refresh"),
        Pair("enable", "Enable"),
        Pair("disable", "Disable"),
        Pair("modsfolder", "Mods Folder"),
        Pair("news", "News"),
        Pair("settings", "Settings"),
        Pair("about", "About"),
        Pair("play", "Play"),
        Pair("installations", "Installations"),
        Pair("skin", "Skin"),
        Pair("mods", "Mods"),
        Pair("download", "Download"),
        Pair("logout", "Log out"),
        Pair("bmclapi", "Our download service is provided by BMCLAPI, please donate it to support its stable operation."),
        Pair("clickit", "Click it:"),
        Pair("release", "Release"),
        Pair("snapshot", "Snapshot"),
        Pair("old", "Old"),
        Pair("installer", "Installer"),
        Pair("install", "Install"),
        Pair("browse", "Browse"),
        Pair("installpath", "Install Path"),
        Pair("customizing", "Customizing"),
        Pair("width", "Width"),
        Pair("height", "Height"),
        Pair("jvm-args", "JVM Arguments"),
        Pair("auto-connect", "Auto connect to server"),
        Pair("auto-connect-server", "Server address to connect"),
        Pair("launching", "Launching")
    )
    @JvmStatic
    fun esperantoResource() = mapOf(
        Pair("email", "Retpoŝto aŭ uzantnomon"),
        Pair("password", "Pasvorto"),
        Pair("login", "Ensaluti"),
        Pair("offline", "Eksterreta reĝimo"),
        Pair("accounts", "Konto"),
        Pair("game", "Ludo"),
        Pair("add", "Aldoni"),
        Pair("edit", "Redakti"),
        Pair("remove", "Forigi"),
        Pair("launch", "Lanĉu"),
        Pair("username", "Uzantnomo"),
        Pair("pleaseinputyourusername", "Bonvolu enmeti vian uzantnomon"),
        Pair("newprofile", "Nova Profilo"),
        Pair("name", "Nomo"),
        Pair("ver", "Versio"),
        Pair("dir", "Pado"),
        Pair("cancel", "Nuligi"),
        Pair("refresh", "Refreŝigi"),
        Pair("enable", "Ebligi"),
        Pair("disable", "Malŝalti"),
        Pair("modsfolder", "Modula dosierujo"),
        Pair("news", "Novaĵoj"),
        Pair("settings", "Agordi"),
        Pair("about", "Pri"),
        Pair("play", "Ludi"),
        Pair("installations", "Instalaĵoj"),
        Pair("skin", "Haŭto"),
        Pair("mods", "Modulo"),
        Pair("download", "Malsupreŝuti"),
        Pair("logout", "Elsaluti"),
        Pair("bmclapi", "Nia elŝuta servo estas donita de BMCLAPI, bonvolu donaci ĝin por subteni sian stabilan funkciadon."),
        Pair("clickit", "Bonvolu alklaki:"),
        Pair("release", "Liberigo"),
        Pair("snapshot", "Kaptiĝo"),
        Pair("old", "Antikva"),
        Pair("installer", "Instalilo"),
        Pair("install", "Instali"),
        Pair("browse", "Foliumi"),
        Pair("installpath", "Instala vojo"),
        Pair("customizing", "Agordado"),
        Pair("width", "Larĝo"),
        Pair("height", "Alteco"),
        Pair("jvm-args", "JVM Parametro"),
        Pair("auto-connect", "Aŭtomate konekti al la servilo"),
        Pair("auto-connect-server", "Aŭtomate konektita servila adreso"),
        Pair("launching", "Lanĉo")
    )
    @JvmStatic
    fun japaneseResource() = mapOf(
        Pair("email", "メールまたはユーザー名"),
        Pair("password", "パスワード"),
        Pair("login", "ログイン"),
        Pair("offline", "オフラインモード"),
        Pair("accounts", "アカウント"),
        Pair("game", "ゲーム"),
        Pair("add", "追加"),
        Pair("edit", "編集"),
        Pair("remove", "削除"),
        Pair("launch", "起動"),
        Pair("username", "ユーザー名"),
        Pair("pleaseinputyourusername", "ユーザー名を入力してください"),
        Pair("newprofile", "新しいプロファイル"),
        Pair("name", "名前"),
        Pair("ver", "バージョン"),
        Pair("dir", "パス"),
        Pair("cancel", "取り消し"),
        Pair("refresh", "リフレッシュ"),
        Pair("enable", "有効にする"),
        Pair("disable", "無効にする"),
        Pair("modsfolder", "モジュールフォルダ"),
        Pair("news", "お知らせ"),
        Pair("settings", "設定"),
        Pair("about", "について"),
        Pair("play", "遊ぶ"),
        Pair("installations", "構成"),
        Pair("skin", "皮膚"),
        Pair("mods", "モジュール"),
        Pair("download", "ダウンロード"),
        Pair("logout", "ログアウト"),
        Pair("bmclapi", "ダウンロードサービスはBMCLAPIによって提供されます。安定した動作をサポートするために寄付してください。"),
        Pair("clickit", "クリックしてください:"),
        Pair("release", "安定版"),
        Pair("snapshot", "スナップショット"),
        Pair("old", "古いバージョン"),
        Pair("installer", "インストーラー"),
        Pair("install", "インストール"),
        Pair("browse", "閲覧する"),
        Pair("installpath", "インストールパス"),
        Pair("customizing", "カストマイズ"),
        Pair("width", "幅"),
        Pair("height", "高さ"),
        Pair("jvm-args", "JVMパラメータ"),
        Pair("auto-connect", "自動的にサーバーに接続"),
        Pair("auto-connect-server", "自動的に接続されたサーバーアドレス"),
        Pair("launching", "起動しています")
    )
    @JvmStatic
    fun chineseSimplifiedResource() = mapOf(
        Pair("email", "邮箱或用户名"),
        Pair("password", "密码"),
        Pair("login", "登录"),
        Pair("offline", "离线模式"),
        Pair("accounts", "账户"),
        Pair("game", "游戏"),
        Pair("add", "添加"),
        Pair("edit", "编辑"),
        Pair("remove", "移除"),
        Pair("launch", "启动"),
        Pair("username", "用户名"),
        Pair("pleaseinputyourusername", "请输入你的用户名"),
        Pair("newprofile", "新档案"),
        Pair("name", "名称"),
        Pair("ver", "版本"),
        Pair("dir", "路径"),
        Pair("cancel", "取消"),
        Pair("refresh", "刷新"),
        Pair("enable", "启用"),
        Pair("disable", "禁用"),
        Pair("modsfolder", "模组文件夹"),
        Pair("news", "新闻"),
        Pair("settings", "设置"),
        Pair("about", "关于"),
        Pair("play", "开始游戏"),
        Pair("installations", "配置"),
        Pair("skin", "皮肤"),
        Pair("mods", "模组"),
        Pair("download", "下载"),
        Pair("logout", "退出登录"),
        Pair("bmclapi", "我们的下载服务由BMCLAPI提供，请捐赠它以支持其稳定运行。"),
        Pair("clickit", "请点击:"),
        Pair("release", "正式版"),
        Pair("snapshot", "快照版"),
        Pair("old", "远古版"),
        Pair("installer", "安装器"),
        Pair("install", "安装"),
        Pair("browse", "浏览"),
        Pair("installpath", "安装路径"),
        Pair("customizing", "自定义"),
        Pair("width", "宽"),
        Pair("height", "高"),
        Pair("jvm-args", "JVM参数"),
        Pair("auto-connect", "是否自动连接服务器"),
        Pair("auto-connect-server", "自动连接的服务器地址"),
        Pair("launching", "正在启动中")
    )
    @JvmStatic
    fun chineseTraditionalResource() = mapOf(
        Pair("email", "郵箱或用戶名"),
        Pair("password", "密碼"),
        Pair("login", "登錄"),
        Pair("offline", "離線模式"),
        Pair("accounts", "賬戶"),
        Pair("game", "遊戲"),
        Pair("add", "添加"),
        Pair("edit", "編輯"),
        Pair("remove", "移除"),
        Pair("launch", "啟動"),
        Pair("username", "用戶名"),
        Pair("pleaseinputyourusername", "請輸入你的用戶名"),
        Pair("newprofile", "新檔案"),
        Pair("name", "名稱"),
        Pair("ver", "版本"),
        Pair("dir", "路徑"),
        Pair("cancel", "取消"),
        Pair("refresh", "刷新"),
        Pair("enable", "啟用"),
        Pair("disable", "禁用"),
        Pair("modsfolder", "模組文件夾"),
        Pair("news", "新聞"),
        Pair("settings", "設置"),
        Pair("about", "關於"),
        Pair("play", "開始遊戲"),
        Pair("installations", "配置"),
        Pair("skin", "皮膚"),
        Pair("mods", "模組"),
        Pair("download", "下載"),
        Pair("logout", "退出登錄"),
        Pair("bmclapi", "我們的下載服務由BMCLAPI提供，請捐贈它以支持其穩定運行。"),
        Pair("clickit", "請點擊:"),
        Pair("release", "正式版"),
        Pair("snapshot", "快照版"),
        Pair("old", "遠古版"),
        Pair("installer", "安裝器"),
        Pair("install", "安裝"),
        Pair("browse", "瀏覽"),
        Pair("installpath", "安裝路徑"),
        Pair("customizing", "自定義"),
        Pair("width", "寬"),
        Pair("height", "高"),
        Pair("jvm-args", "JVM參數"),
        Pair("auto-connect", "是否自動連結服務器"),
        Pair("auto-connect-server", "自動連結的服務器地址"),
        Pair("launching", "正在啟動中")
    )
}