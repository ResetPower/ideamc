package org.imcl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.imcl.main.MainScene;

public class MyApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("IDEA Minecraft Launcher");
        primaryStage.setScene(MainScene.get(primaryStage));
        primaryStage.setOnCloseRequest(event -> Platform.exit());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
