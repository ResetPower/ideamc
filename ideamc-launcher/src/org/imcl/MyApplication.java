package org.imcl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.imcl.constraints.Toolkit;
import org.imcl.core.authentication.YggdrasilAuthenticator;
import org.imcl.lang.Translator;
import org.imcl.launch.LauncherScene;
import org.imcl.main.MainScene;
import org.imcl.users.OfflineUserInformation;
import org.imcl.users.YggdrasilUserInformation;

import java.io.FileInputStream;
import java.util.Properties;

public class MyApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("IDEA Minecraft Launcher");
        primaryStage.setOnCloseRequest(event -> Platform.exit());
        if (Toolkit.isLoggedIn()) {
            FileInputStream fis = new FileInputStream("imcl/account/acinf.text");
            Properties prop = new Properties();
            prop.load(fis);
            fis.close();
            if (prop.getProperty("uuid").trim().equals("none")) {
                primaryStage.setScene(LauncherScene.get(new Translator(Toolkit.getCurrentLanguage()), new OfflineUserInformation(prop.getProperty("username")), primaryStage));
            } else {
                if (YggdrasilAuthenticator.validate(prop.getProperty("accessToken"))) {
                    primaryStage.setScene(LauncherScene.get(new Translator(Toolkit.getCurrentLanguage()), new YggdrasilUserInformation(prop.getProperty("username"), prop.getProperty("uuid"), prop.getProperty("accessToken")), primaryStage));
                } else {
                    final String newToken = YggdrasilAuthenticator.refresh(prop.getProperty("accessToken"));
                    primaryStage.setScene(LauncherScene.get(new Translator(Toolkit.getCurrentLanguage()), new YggdrasilUserInformation(prop.getProperty("username"), prop.getProperty("uuid"), newToken), primaryStage));
                }
            }
        } else {
            primaryStage.setScene(MainScene.get(primaryStage));
        }
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
