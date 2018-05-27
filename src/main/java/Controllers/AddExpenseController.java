package Controllers;

import helpers.CategoryDBHelper;
import helpers.HibernateHelper;
import helpers.RateDBHelper;
import helpers.TagDBHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import model.Category;
import model.Expense;
import model.Rate;
import model.Tag;
import org.controlsfx.control.CheckComboBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
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
    CheckComboBox<Tag> newTagsCtrl;
    @FXML
    ProgressBar progressBar;

    private ObservableList<Category> categoriesList;
    private ObservableList<Tag> tagsList;

    private Expense getExpense() {
        Expense e = new Expense(title.getText(),
                description.getText(),
                recurrent.isSelected(),
                new Date(dueDate.getEditor().getText()),
                Double.parseDouble(amount.getText()),
                catCtrl.getValue());
        List<Tag> tags = new ArrayList<>(newTagsCtrl.getCheckModel().getCheckedItems());
        e.setTags(tags);
        return e;
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
        newTagsCtrl.getCheckModel().clearChecks();
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
        //populateTags();
        populateNewTags();
    }


    private void populateCategories() {
        CategoryDBHelper c = new CategoryDBHelper();
        categoriesList = FXCollections.observableArrayList(c.fetchAll());
        catCtrl.setItems(categoriesList);
    }

    private Category getSelectedCategory(String catName) {
        return categoriesList
                .stream()
                .filter(
                        cat -> cat.getName().equalsIgnoreCase(catName)
                ).collect(Collectors.toList())
                .get(0);
    }

    private void populateNewTags() {
        tagsList = FXCollections.observableArrayList(new TagDBHelper().fetchAll());
        newTagsCtrl.getItems().clear();
        newTagsCtrl.getItems().addAll(tagsList);
    }

    @FXML
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
            String color = colorPicker.getValue().toString().replace("0x", "#");
            System.out.println(color);

            Tag tag = new Tag(tagName.getText(), color);
            new TagDBHelper().save(tag);
            populateNewTags();
        });

    }

    @FXML
    public void displayAddPayedRatesDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add payed rate");
        dialog.setHeaderText("Enter at least amount in order to add a rate");

        // Set the button types.
        ButtonType confirmBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.setMaxWidth(Double.MAX_VALUE);

        TextField textFieldAmount = new TextField();
        textFieldAmount.setPromptText("amount");
        textFieldAmount.setMaxWidth(Double.MAX_VALUE);
        DatePicker datePicker = new DatePicker();
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setPromptText("set date");
        TextArea textAreaObserVations = new TextArea();
        textAreaObserVations.setMaxWidth(Double.MAX_VALUE);
        textAreaObserVations.setPromptText("observations");

        grid.add(new Label("Amount *:"), 0, 0);
        grid.add(textFieldAmount, 1, 0);
        grid.add(new Label("Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Observations"), 0, 2);
        grid.add(textAreaObserVations, 1, 2);

        // Enable/Disable login button depending on whether a username was entered.
        Node saveBtn = dialog.getDialogPane().lookupButton(confirmBtn);
        saveBtn.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        textFieldAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            saveBtn.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> textFieldAmount.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmBtn) {
                return new Pair<String, String>(textFieldAmount.getText(), datePicker.getValue().toString());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            String color = datePicker.getValue().toString().replace("0x", "#");
            System.out.println(color);

            Rate rate = new Rate(Double.parseDouble(textFieldAmount.getText()),
                    new Date(datePicker.getEditor().getText()),
                    textAreaObserVations.getText());
            new RateDBHelper().save(rate);
            displayPayProgress(Double.parseDouble(textFieldAmount.getText()));
        });

    }

    private void displayPayProgress(double value) {
        //Double payed = (value / Double.parseDouble(amount.getText())) / 100;
        Double payed = (value * 100) / Double.parseDouble(amount.getText());
        System.out.println("TOTAL: " + Double.parseDouble(amount.getText()));
        System.out.println("PAYED: " + payed);
        progressBar.setProgress(payed / 100);

        DecimalFormat df = new DecimalFormat("#.##");
        payed = Double.valueOf(df.format(payed));

        progressBar.getTooltip().setText("Payed: "+ payed+"%");
    }

}
