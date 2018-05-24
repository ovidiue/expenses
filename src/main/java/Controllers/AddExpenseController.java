package Controllers;

import com.sun.xml.internal.bind.v2.TODO;
import helpers.CategoryDBHelper;
import helpers.HibernateHelper;
import helpers.TagDBHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.Pair;
import model.Category;
import model.Expense;
import model.Tag;

import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by Ovidiu on 18-May-18.
 */
public class AddExpenseController implements Initializable {
    @FXML
    TextField title;
    @FXML
    TextArea description;
    @FXML
    TextField amount;
    @FXML
    DatePicker dueDate;
    @FXML
    ChoiceBox<Category> catCtrl;
    @FXML
    CheckBox recurrent;
    @FXML
    ChoiceBox<Tag> tagCtrl;

    private ObservableList<Category> categoriesList;
    private ObservableList<Tag> tagsList;

    private Expense getExpense() {
        return new Expense(title.getText(),
                description.getText(),
                recurrent.isSelected(),
                new Date(dueDate.getEditor().getText()),
                Double.parseDouble(amount.getText()));
    }

    public void saveExpense() {
        Expense e = getExpense();
        Category z = catCtrl.getValue();
        e.setCategory(z);

        HibernateHelper.save(e);
        clearFieldSelections();
    }

    private void clearFieldSelections() {
        title.setText("");
        description.setText("");
        amount.setText("");
        dueDate.getEditor().setText("");
        recurrent.setSelected(false);
        catCtrl.setValue(null);
    }

    public void displayAddCategoryDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add new category");
        dialog.setHeaderText("Enter at least title in order to add a new category");

        // Set the button types.
        ButtonType confirmBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField categoryTitle = new TextField();
        categoryTitle.setPromptText("title");
        TextArea catDescription = new TextArea();
        catDescription.setPromptText("description");
        ColorPicker catColor = new ColorPicker();

        catColor.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("Title *:"), 0, 0);
        grid.add(categoryTitle, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(catDescription, 1, 1);
        grid.add(new Label("Color:"), 0, 2);
        grid.add(catColor, 1, 2);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(confirmBtn);
        loginButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        categoryTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> categoryTitle.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmBtn) {
                return new Pair<>(categoryTitle.getText(), catDescription.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
            CategoryDBHelper categoryDBHelper = new CategoryDBHelper();
            categoryDBHelper.save(new Category(categoryTitle.getText(),
                    catDescription.getText(),
                    catColor.getValue().toString().replace("0x", "#")));
            populateCategories();
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateCategories();
        populateTags();
    }

    private void populateCategories() {
        CategoryDBHelper c = new CategoryDBHelper();
        categoriesList = FXCollections.observableArrayList(c.fetchAll());
        /*System.out.println("*****************");
        System.out.println("CATEGORIES in choicebox\n");
        for (Category a: list) {
            System.out.println(a.getName());
            System.out.println(a.getColor());
            System.out.println(a.getDescription());
            System.out.println(a.getId());
            System.out.println("*****************\n");
        }*/
        catCtrl.setItems(categoriesList);

    }

    private void populateTags() {
        tagsList = FXCollections.observableArrayList(new TagDBHelper().fetchAll());
        tagCtrl.setItems(tagsList);
    }

    private Category getSelectedCategory(String catName) {
        return categoriesList
                .stream()
                .filter(
                        cat -> cat.getName().equalsIgnoreCase(catName)
                ).collect(Collectors.toList())
                .get(0);
    }

    public void displayAddTagDialog() {
        /*TODO to implement add tag dialog*/
    }
}
