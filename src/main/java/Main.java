import helpers.HibernateHlp;
import helpers.TagDBHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.Tag;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
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

    public void displayAddTagDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add new tag");
        dialog.setHeaderText("Enter at least title in order to add a new tag");


        // Set the button types.
        ButtonType confirmBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.setMaxWidth(Double.MAX_VALUE);

        TextField tagName = new TextField();
        tagName.setPromptText("name");
        tagName.setMaxWidth(Double.MAX_VALUE);
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setMaxWidth(Double.MAX_VALUE);
        colorPicker.setPromptText("choose color");

        grid.add(new Label("Name *:"), 0, 0);
        grid.add(tagName, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorPicker, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        Node saveBtn = dialog.getDialogPane().lookupButton(confirmBtn);
        saveBtn.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        tagName.textProperty().addListener((observable, oldValue, newValue) -> {
            saveBtn.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> tagName.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmBtn) {
                return new Pair<String, String>(tagName.getText(), colorPicker.getValue().toString());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            String color = extractRGB(colorPicker.getValue());
            System.out.println(color);

            new TagDBHelper().save(new Tag(tagName.getText(), color));

           /* TagDBHelper tagDBHelper = new TagDBHelper();
            Color col = colorPicker.getValue();
            Double red = col.getRed();
            Double green = col.getGreen();
            Double blue = col.getBlue();
            String resultCol = String.valueOf(red)+","+String.valueOf(green)+","+String.valueOf(blue);
            //tagDBHelper.save(new Tag(tagName.getText(), colorPicker.getStyle().toString()));
            tagDBHelper.save(new Tag(tagName.getText(), resultCol));*/
        });

    }

    private String extractRGB(Color color) {
        int red,
                green,
                blue;

        double opacity;

        String result;

        red = (int) (color.getRed() * 255);
        blue = (int) (color.getBlue() * 255);
        green = (int) (color.getGreen() * 255);
        //opacity = color.getOpacity();

        //return "rgb("+red+","+green+","+blue+","+opacity+")";
        return "rgb(" + red + "," + green + "," + blue + ")";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        HibernateHlp.buildSessionFactory();
    }
}
