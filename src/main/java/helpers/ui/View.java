package helpers.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Created by Ovidiu on 24-Jun-18.
 */
public class View {
    public static void loadScreen(String name) throws IOException {
        Pane root = FXMLLoader.load(View.class.getResource(name));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
}
