import helpers.HibernateHlp;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 15-May-18.
 */
public class Main extends Application implements Initializable {
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
        //scene.getStylesheets().add("css/style.css");

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

    public void viewAllTags() throws IOException {
        AnchorPane table = loader.load(getClass().getResource("fxml_views/all_tags.fxml"));
        table.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setCenter(table);
    }

    public void viewAllCategories() throws IOException {
        AnchorPane table = loader.load(getClass().getResource("fxml_views/all_categories.fxml"));
        root.setCenter(table);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        HibernateHlp.buildSessionFactory();
    }
}
