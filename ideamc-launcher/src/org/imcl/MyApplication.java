package org.imcl;

import com.alibaba.fastjson.JSONObject;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.imcl.constraints.ConstraintsKt;
import org.imcl.constraints.Toolkit;
import org.imcl.files.FileChecker;
import org.imcl.lang.Translator;
import org.imcl.launch.LaunchSceneState;
import org.imcl.launch.LauncherScene;
import org.imcl.main.MainScene;
import org.imcl.plugin.PluginLoader;
import org.imcl.users.OfflineUserInformation;
import org.imcl.users.YggdrasilUserInformation;

public class MyApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        org.imcl.constraints.ConstraintsKt.initLogger();
        org.imcl.core.constraints.ConstraintsKt.initLogger();
        Logger logger = ConstraintsKt.getLogger();
        primaryStage.setResizable(false);
        logger.info("Checking file");
        FileChecker.check();
        logger.info("Initializing Window");
        Toolkit.init();
        primaryStage.setTitle("IDEA Minecraft Launcher");
        primaryStage.setOnCloseRequest(event -> System.exit(0));
        PluginLoader.preLoad();
        if (Toolkit.isLoggedIn()) {
            logger.info("Is logged in, using default account");
            JSONObject account = Toolkit.obj.getJSONObject("account");
            if (account.getString("uuid").trim().equals("none")) {
                logger.info("Offline account. Player name: "+account.getString("username"));
                primaryStage.setScene(LauncherScene.get(new Translator(Toolkit.getCurrentLanguage()), new OfflineUserInformation(account.getString("username")), primaryStage, LaunchSceneState.DEFAULT));
            } else {
                if (!account.containsKey("email")) {
                    logger.info("Unable to find email in imcl.json, go to login page");
                    JSONObject settings = Toolkit.obj.getJSONObject("settings");
                    settings.put("isLoggedIn", "false");
                    Toolkit.save();
                    primaryStage.setScene(MainScene.get(primaryStage));
                }
                logger.info("Online account. Player name: "+account.getString("username"));
                primaryStage.setScene(LauncherScene.get(new Translator(Toolkit.getCurrentLanguage()), new YggdrasilUserInformation(account.getString("username"), account.getString("uuid"), account.getString("accessToken"), account.getString("email")), primaryStage, LaunchSceneState.DEFAULT));
            }
            Toolkit.save();
        } else {
            logger.info("Not logged in, go to login page");
            primaryStage.setScene(MainScene.get(primaryStage));
        }
        primaryStage.show();
        logger.info("primaryStage showed");
    }
    public static void main(String[] args) {
        launch(args);
    }
}
