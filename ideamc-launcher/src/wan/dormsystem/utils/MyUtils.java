package wan.dormsystem.utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.stage.Stage;

/**
 * @author StarsOne
 * @date Create in  2019/6/5 0005 14:01
 * @description
 */
public class MyUtils {

    /**
     * 每次点击链接之后都会，链接不会变成灰色
     *
     * @param hyperlink
     * @param hander
     */
    public static void setLinkAction(Hyperlink hyperlink, LinkActionHander hander) {

        hyperlink.setBorder(Border.EMPTY);
        hyperlink.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hander.setAction();
                hyperlink.setVisited(false);
            }
        });
    }

    public static void setLinkAutoAction(Hyperlink hyperlink) {
        String text = hyperlink.getText();

        if (text.contains("www") || text.contains("com") || text.contains(".")) {
            try {
                Desktop.getDesktop().browse(new URI(text));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else if (text.contains(File.separator)) {
            try {
                Desktop.getDesktop().open(new File(text));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //打开QQ
            try {
                Desktop.getDesktop().browse(new URI("http://wpa.qq.com/msgrd?v=3&amp;uin=" + text + "&amp;site=qq&amp;menu=yes"));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭当前窗口
     * @param control
     */
    public static void closeWindow(Control control) {
        Stage stage = (Stage) control.getScene().getWindow();
        stage.close();
    }
    /**
     * 创建stage并显示
     *
     * @param o
     * @param primaryStage
     * @param title
     * @param fxmlName
     * @param iconName
     * @param width
     * @param height
     * @return
     * @throws IOException
     */
    public static MessageUtil createAndShowStage(Object o, Stage primaryStage, String title, String fxmlName, String iconName, int width, int height) throws IOException {
        FXMLLoader loader = new FXMLLoader();    // 创建对象
        loader.setBuilderFactory(new JavaFXBuilderFactory());    // 设置BuilderFactory
        loader.setLocation(getFxmlPath(o, fxmlName));
        InputStream inputStream = getFxmlFile(o, fxmlName);
        Object object = loader.load(inputStream);

        //        Parent root = FXMLLoader.load(MyUtils.getFxmlPath(this,"scene_main"));
        Parent root = (Parent) object;
        if (primaryStage == null) {
            primaryStage = new Stage();
        }
        if (iconName != null) {
            primaryStage.getIcons().add(getImg(o, iconName));
        }


        primaryStage.setTitle(title);
        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.show();
        Object controller = loader.getController();
        return new MessageUtil(controller);
    }

    /**
     * 获得图片文件，
     *
     * @param o        当前的class，传入this即可
     * @param fileName 图片名+扩展名
     * @return 图片image
     */
    public static Image getImg(Object o, String fileName) {
        URL res = null;
        if (o.getClass().getName().contains("controller")) {
            res = o.getClass().getResource("../img");
        } else {
            res = o.getClass().getResource("img");
        }

        if (fileName.contains(".")) {
            String temp = res.toString() + "/" + fileName;
            return new Image(temp);
        }
        return null;
    }


    /**
     * 获得fxml文件路径
     *
     * @param o        class文件，传入this
     * @param fileName 文件名
     * @return
     */
    public static URL getFxmlPath(Object o, String fileName) {
        if (o.getClass().getName().contains("controller")) {
            return o.getClass().getResource("../fxml/" + fileName + ".fxml");
        } else {
            return o.getClass().getResource("fxml/" + fileName + ".fxml");
        }
    }

    public static InputStream getFxmlFile(Object o, String fileName) {
        if (o.getClass().getName().contains("controller")) {
            return o.getClass().getResourceAsStream("../fxml/" + fileName + ".fxml");
        } else {
            return o.getClass().getResourceAsStream("fxml/" + fileName + ".fxml");
        }
    }

    public interface LinkActionHander {
        void setAction();
    }
}
