package helpers.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;

/**
 * Created by Ovidiu on 22-May-18.
 */
public class AlertHelper {

    public static Alert build(Alert.AlertType type,
                                  String title,
                                  String header,
                                  String contentText,
                                  Object param) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(contentText);

        Button okBtn = new Button("OK");
        Button cancelBtn = new Button("OK");
        alert.getDialogPane().getChildren().addAll(cancelBtn, okBtn);

        return alert;

//        alert.showAndWait();
    }
}
