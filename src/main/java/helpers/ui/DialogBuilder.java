package helpers.ui;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Ovidiu on 08-Jun-18.
 */
public class DialogBuilder {
    private static final String DIALOG_CSS = "css/dialog_builder.css";
    private final ButtonType buttonTypeClose = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    private final ButtonType buttonTypeConfirm = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
    private Dialog dialog;
    private VBox vBoxMain;
    private VBox vBoxForm;
    private Pane caller;
    private Map<String, Control> controls;
    private ValidationSupport validationSupport;

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

    public DialogBuilder addFormField(String label, String key, Control control, boolean mandatory) {
        final String MSG = "MANDATORY";
        VBox vBox = new VBox(new Label(label), control);
        vBoxForm.getChildren().add(vBox);
        control.setUserData(mandatory);
        controls.put(key, control);
        control.setMaxWidth(Double.MAX_VALUE);
        if (mandatory == true)
            Platform.runLater(
                    () -> validationSupport.registerValidator(control,
                            Validator.createEmptyValidator(MSG, Severity.ERROR)));

        return this;
    }

    public DialogBuilder addFormField(String label, String key, Control control, boolean mandatory, String msg) {
        VBox vBox = new VBox(new Label(label), control);
        vBoxForm.getChildren().add(vBox);
        control.setUserData(mandatory);
        controls.put(key, control);
        control.setMaxWidth(Double.MAX_VALUE);

        if (mandatory == true)
            Platform.runLater(
                    () -> validationSupport.registerValidator(control,
                            Validator.createEmptyValidator(msg, Severity.ERROR)));

        return this;
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

        if (controls.size() > 0) {
            Button confirmDialog = getConfirmBtn();
            confirmDialog.disableProperty().bind(validationSupport.invalidProperty());
        }

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
        validationSupport = new ValidationSupport();

        vBoxForm = new VBox(15);

        vBoxMain = new VBox(vBoxForm);
        vBoxMain.setSpacing(30);
        vBoxMain.setPadding(new Insets(20));
        vBoxMain.setPrefWidth(300);

        dialog = new Dialog();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeClose, buttonTypeConfirm);
        dialog.getDialogPane().setContent(vBoxMain);
        controls = new HashMap<>();
        dialog.getDialogPane().getStylesheets().add(DIALOG_CSS);
    }

    private Button getConfirmBtn() {
        return (Button) dialog.getDialogPane().lookupButton(buttonTypeConfirm);
    }

    public ButtonType getConfirmAction() {
        return buttonTypeConfirm;
    }

}
