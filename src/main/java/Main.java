import helpers.db.HibernateHlp;
import helpers.ui.Preloader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 15-May-18.
 */
public class Main extends Application implements Initializable {
    final static HashMap<String, String> AVAILABLE_THEMES = new HashMap<>();

    static {
        AVAILABLE_THEMES.put("light", "css/style.css");
        AVAILABLE_THEMES.put("dark", "css/style_dark.css");
    }

    @FXML
    BorderPane root;
    @FXML
    RadioMenuItem radioMenuItemLight;
    @FXML
    RadioMenuItem radioMenuItemDark;
    @FXML
    ToggleGroup theme;
    FXMLLoader loader;


    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setScene(new Scene(new Label("Connecting to DB ..."), 600, 600));
        Preloader preloader = new Preloader();
        Task<Void> taskDB = initializeDBConnection();
        preloader.progressProperty().bind(taskDB.progressProperty());

        taskDB.setOnSucceeded(e -> {
            preloader.hide();

            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml_views/main_screen.fxml"));
            loader.setResources(ResourceBundle.getBundle("lang/translations"));

            try {
                root = loader.load();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add("css/style.css");
            scene.getStylesheets().add("css/custom_notifications_white.css");

            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(event -> System.exit(1));
            primaryStage.show();
        });

        preloader.show();
        new Thread(taskDB).start();


    }

    private void initMenuActions() {
        initThemeSettings();
    }

    private void initThemeSettings() {
        Scene scene = root.getScene();
        radioMenuItemDark.setOnAction(e -> {
            clearStyleSheets();
            scene.getStylesheets().add(AVAILABLE_THEMES.get("dark"));
        });

        radioMenuItemLight.setOnAction(e -> {
            clearStyleSheets();
            scene.getStylesheets().add(AVAILABLE_THEMES.get("light"));
        });
    }

    private void clearStyleSheets() {
        root.getScene().getStylesheets().clear();
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

    @FXML
    public void viewReports() throws IOException {
        AnchorPane table = loader.load(getClass().getResource("fxml_views/reports.fxml"));
        root.setCenter(table);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*HibernateHlp.buildSessionFactory();*/
        Platform.runLater(() -> initMenuActions());
    }

    private Task<Void> initializeDBConnection() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                HibernateHlp.buildSessionFactory();
                return null;
            }
        };
    }
}
