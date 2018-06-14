package helpers.ui;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Ovidiu on 08-Jun-18.
 */
public class DialogBuilder {
    private static final String DIALOG_CSS = "css/dialog_builder.css";
    private final ButtonType CLOSE_BUTTON = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    private final ButtonType CONFIRM_ACTION = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
    private Dialog dialog;
    private VBox vBoxMain;
    private VBox vBoxForm;
    private Pane caller;
    private Map<String, Control> controls;

    public DialogBuilder() {
        initDialogControls();
    }


    public DialogBuilder setTitle(String title) {
        dialog.setTitle(title);
        return this;
    }

    public DialogBuilder setHeader(String header) {
        dialog.setHeaderText(header);
        return this;
    }

    public Map<String, Control> getControls() {
        return controls;
    }

    public DialogBuilder addFormField(String label, String key, Control control, boolean mandatory) {
        VBox vBox = new VBox(new Label(label), control);
        vBoxForm.getChildren().add(vBox);
        control.setUserData(mandatory);
        controls.put(key, control);
        control.setMaxWidth(Double.MAX_VALUE);

        return this;
    }

    private boolean areEmpty() {

        for (Control c : controls.values()) {
            if (c.getUserData().toString().equalsIgnoreCase("true")) {
                System.out.println("BIG IF");
                if (c instanceof TextField && ((TextField) c).getText().trim().length() == 0) {
                    System.out.println("SMALL IF");
                    return true;
                }

                if (c instanceof ColorPicker && ((ColorPicker) c).getValue().toString().trim().length() == 0) {
                    return true;
                }

                if (c instanceof TextArea && ((TextArea) c).getText().toString().trim().length() == 0) {
                    return true;
                }

            }
        }

        return false;

    }

    public Control getControl(String controlKey) {
        return controls.get(controlKey);
    }

    public Optional show() {
        dialog.setOnShowing(e -> ControlEffect.setBlur(getCallerPane(), true));
        dialog.setOnHiding(e -> ControlEffect.setBlur(getCallerPane(), false));
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(.3));
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setNode(dialog.getDialogPane());
        fadeTransition.play();

        return dialog.showAndWait();
    }

    private Pane getCallerPane() {
        return caller;
    }

    public DialogBuilder setCallerPane(Pane pane) {
        caller = pane;
        return this;
    }

    private void initDialogControls() {
        vBoxForm = new VBox(15);

        vBoxMain = new VBox(vBoxForm);
        vBoxMain.setSpacing(30);
        vBoxMain.setPadding(new Insets(20));
        vBoxMain.setPrefWidth(300);

        dialog = new Dialog();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.getDialogPane().getButtonTypes().addAll(CLOSE_BUTTON, CONFIRM_ACTION);
        dialog.getDialogPane().setContent(vBoxMain);
        controls = new HashMap<>();
        dialog.getDialogPane().getStylesheets().add(DIALOG_CSS);

        Button confirmDialog = getConfirmBtn();
        confirmDialog.addEventFilter(ActionEvent.ACTION, event -> {
            if (areEmpty())
                event.consume();
        });

    }

    private Button getConfirmBtn() {
        return (Button) dialog.getDialogPane().lookupButton(CONFIRM_ACTION);
    }

    public ButtonType getConfirmAction() {
        return CONFIRM_ACTION;
    }

}
