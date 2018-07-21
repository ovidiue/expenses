package helpers.ui;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Ovidiu on 26-May-18.
 */
@Slf4j
public class Preloader {
    private final String PRELOADER_CSS = "css/preloader.css";
    private ProgressBar bar;
    private Stage stage;

    public Preloader() {
        this.stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(createPreloaderScene());
    }

    private Scene createPreloaderScene() {
        bar = new ProgressBar(0);
        BorderPane p = new BorderPane();
        p.setCenter(bar);

        Scene scene = new Scene(p, 400, 200);
        scene.setCursor(Cursor.WAIT);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(PRELOADER_CSS);

        return scene;
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        stage.hide();
    }

    public DoubleProperty progressProperty() {
        return bar.progressProperty();
    }
}
