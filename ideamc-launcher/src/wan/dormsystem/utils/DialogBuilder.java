package wan.dormsystem.utils;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.sun.istack.internal.Nullable;

import java.util.Optional;

import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * @author StarsOne
 * @date Create in  2019/6/2 0002 20:51
 * @description
 */
public class DialogBuilder {
    private String title, message;
    private JFXButton negativeBtn = null;
    private JFXButton positiveBtn = null;
    private Window window;
    private Paint negativeBtnPaint = Paint.valueOf("#747474");//否定按钮文字颜色，默认灰色
    private Paint positiveBtnPaint = Paint.valueOf("#0099ff");
    private Hyperlink hyperlink = null;
    private TextField textField = null;
    private JFXAlert<String> alert;
    private OnInputListener onInputListener = null;

    /**
     * 构造方法
     *
     * @param control 任意一个控件
     */
    public DialogBuilder(Control control) {
        window = control.getScene().getWindow();
    }

    public DialogBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public DialogBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public DialogBuilder setPositiveBtn(String positiveBtnText) {
        return setPositiveBtn(positiveBtnText, null, null);
    }

    public DialogBuilder setNegativeBtn(String negativeBtnText) {
        return setNegativeBtn(negativeBtnText, null, null);
    }

    /**
     * 设置否定按钮文字和文字颜色
     *
     * @param negativeBtnText 文字
     * @param color           文字颜色 十六进制 #fafafa
     * @return
     */
    public DialogBuilder setNegativeBtn(String negativeBtnText, String color) {
        return setNegativeBtn(negativeBtnText, null, color);
    }

    /**
     * 设置按钮文字和按钮文字颜色，按钮监听器和
     *
     * @param negativeBtnText
     * @param negativeBtnOnclickListener
     * @param color                      文字颜色 十六进制 #fafafa
     * @return
     */
    public DialogBuilder setNegativeBtn(String negativeBtnText, @Nullable OnClickListener negativeBtnOnclickListener, String color) {
        if (color != null) {
            this.negativeBtnPaint = Paint.valueOf(color);
        }
        return setNegativeBtn(negativeBtnText, negativeBtnOnclickListener);
    }


    /**
     * 设置按钮文字和点击监听器
     *
     * @param negativeBtnText            按钮文字
     * @param negativeBtnOnclickListener 点击监听器
     * @return
     */
    public DialogBuilder setNegativeBtn(String negativeBtnText, @Nullable OnClickListener negativeBtnOnclickListener) {

        negativeBtn = new JFXButton(negativeBtnText);
        negativeBtn.setCancelButton(true);
        negativeBtn.setTextFill(negativeBtnPaint);
        negativeBtn.setButtonType(JFXButton.ButtonType.FLAT);
        negativeBtn.setOnAction(addEvent -> {
            alert.hideWithAnimation();
            if (negativeBtnOnclickListener != null) {
                negativeBtnOnclickListener.onClick();
            }
        });
        return this;
    }

    /**
     * 设置按钮文字和颜色
     *
     * @param positiveBtnText 文字
     * @param color           颜色 十六进制 #fafafa
     * @return
     */
    public DialogBuilder setPositiveBtn(String positiveBtnText, String color) {
        return setPositiveBtn(positiveBtnText, null, color);
    }

    /**
     * 设置按钮文字，颜色和点击监听器
     *
     * @param positiveBtnText            文字
     * @param positiveBtnOnclickListener 点击监听器
     * @param color                      颜色 十六进制 #fafafa
     * @return
     */
    public DialogBuilder setPositiveBtn(String positiveBtnText, @Nullable OnClickListener positiveBtnOnclickListener, String color) {
        if (color != null) {
            this.positiveBtnPaint = Paint.valueOf(color);
        }
        return setPositiveBtn(positiveBtnText, positiveBtnOnclickListener);
    }

    /**
     * 设置按钮文字和监听器
     *
     * @param positiveBtnText            文字
     * @param positiveBtnOnclickListener 点击监听器
     * @return
     */
    public DialogBuilder setPositiveBtn(String positiveBtnText, @Nullable OnClickListener positiveBtnOnclickListener) {
        positiveBtn = new JFXButton(positiveBtnText);
        positiveBtn.setDefaultButton(true);
        positiveBtn.setTextFill(positiveBtnPaint);
        positiveBtn.setOnAction(closeEvent -> {
            alert.hideWithAnimation();
            if (positiveBtnOnclickListener != null) {
                positiveBtnOnclickListener.onClick();//回调onClick方法
            }
        });
        return this;
    }

    /**
     * 设置超链接（文件输出路径，网址跳转），会自动打开指定浏览器或者是资源管理器执行操作
     *
     * @param text 文件的路径，或者是网址，
     * @return
     */
    public DialogBuilder setHyperLink(String text) {
        hyperlink = new Hyperlink(text);
        hyperlink.setBorder(Border.EMPTY);
        hyperlink.setOnMouseClicked(event -> {
            MyUtils.setLinkAutoAction(hyperlink);
        });
        return this;
    }

    public DialogBuilder setTextFieldText(OnInputListener onInputListener) {

        this.textField = new TextField();
        this.onInputListener = onInputListener;
        return this;
    }

    /**
     * 创建对话框并显示
     *
     * @return JFXAlert<String>
     */
    public JFXAlert<String> create() {
        alert = new JFXAlert<>((Stage) (window));
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setOverlayClose(false);

        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label(title));
        //添加hyperlink超链接文本或者是输入框
        if (hyperlink != null) {
            layout.setBody(new HBox(new Label(this.message), hyperlink));
        } else if (textField != null) {
            layout.setBody(new VBox(new Label(this.message), textField));
            positiveBtn.setOnAction(event -> {
                alert.setResult(textField.getText());
                alert.hideWithAnimation();
            });
        } else {
            layout.setBody(new VBox(new Label(this.message)));
        }

        //添加确定和取消按钮
        if (negativeBtn != null && positiveBtn != null) {
            layout.setActions(negativeBtn, positiveBtn);
        } else {
            if (negativeBtn != null) {
                layout.setActions(negativeBtn);
            } else if (positiveBtn != null) {
                layout.setActions(positiveBtn);
            }
        }

        alert.setContent(layout);
        Optional<String> input = alert.showAndWait();
        //不为空，则回调接口
        if (input.isPresent()) {
            if (textField!=null) {
                onInputListener.onGetText(input.get());
            }
        }

        return alert;
    }


    public interface OnClickListener {
        void onClick();
    }

    public interface OnInputListener {
        void onGetText(String result);
    }

}
