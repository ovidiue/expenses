import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.border.Border;
import javax.swing.text.TableView;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 15-May-18.
 */
public class Main extends Application {
    @FXML
    BorderPane root;

    FXMLLoader loader;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
         loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml_views/main_screen.fxml"));
        loader.setResources(ResourceBundle.getBundle("translations"));
        root = loader.load();

        Scene scene = new Scene(root);
        //scene.getStylesheets().add("css/main_css.css");

        //primaryStage.setTitle("FXML Welcome");
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> System.exit(1));


        primaryStage.show();
    }

    public void viewAllExpenses() throws IOException {
        AnchorPane table = loader.load(getClass().getResource("fxml_views/all_expenses.fxml"));
        root.setCenter(table);
    }

    public void addExpense() throws IOException {
        AnchorPane table = loader.load(getClass().getResource("fxml_views/add_expense.fxml"));
        root.setCenter(table);
    }
}
