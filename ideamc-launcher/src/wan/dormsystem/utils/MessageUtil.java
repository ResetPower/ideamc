package wan.dormsystem.utils;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author StarsOne
 * @date Create in  2019/6/14 0014 16:43
 * @description
 */
public class MessageUtil {
    private Stage stage;
    private Scene scene;
    private Object controller;

    public MessageUtil(Object controller) {
        this.controller = controller;
    }

    public MessageUtil(Stage stage, Scene scene, Object controller) {
        this.stage = stage;
        this.scene = scene;
        this.controller = controller;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }
}
