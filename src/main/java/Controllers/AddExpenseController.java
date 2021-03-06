package Controllers;

import helpers.HibernateHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import model.Category;
import model.Expense;

import java.util.Date;
import java.util.Optional;

/**
 * Created by Ovidiu on 18-May-18.
 */
public class AddExpenseController {
    @FXML
    TextField title;
    @FXML
    TextArea description;
    @FXML
    TextField amount;
    @FXML
    DatePicker dueDate;
    @FXML
    ChoiceBox<Category> category;
    @FXML
    CheckBox recurrent;

    private Expense getExpense() {
        return new Expense(title.getText(),
                description.getText(),
                recurrent.isSelected(),
                new Date(dueDate.getEditor().getText()),
                Double.parseDouble(amount.getText()));
    }

    public void saveExpense() {
        Expense e = getExpense();
        HibernateHelper.save(e);
        clearFieldSelections();
    }

    private void clearFieldSelections() {
        title.setText("");
        description.setText("");
        amount.setText("");
        dueDate.getEditor().setText("");
        recurrent.setSelected(false);
    }

    public void displayAddCategoryDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add new category");
        dialog.setHeaderText("Enter at least title in order to add a new category");


        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField categoryTitle = new TextField();
        categoryTitle.setPromptText("title");
        TextArea catDescription = new TextArea();
        catDescription.setPromptText("description");

        grid.add(new Label("Title *:"), 0, 0);
        grid.add(categoryTitle, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(catDescription, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
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
            if (dialogButton == loginButtonType) {
                return new Pair<>(categoryTitle.getText(), catDescription.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
        });
    }
}
