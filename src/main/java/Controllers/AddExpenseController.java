package Controllers;

import helpers.db.CategoryDBHelper;
import helpers.db.HibernateHelper;
import helpers.db.TagDBHelper;
import helpers.ui.ControlEffect;
import helpers.ui.Notification;
import helpers.ui.TextUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import model.Category;
import model.Expense;
import model.Rate;
import model.Tag;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.PopOver;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Ovidiu on 18-May-18.
 */
public class AddExpenseController implements Initializable {
    @FXML
    TextField textfieldTitle;
    @FXML
    TextArea textareaDescription;
    @FXML
    TextField textfieldAmount;
    @FXML
    DatePicker datepickerDueDate;
    @FXML
    ChoiceBox<Category> choiceboxCategory;
    @FXML
    CheckBox checkboxRecurrent;
    @FXML
    CheckComboBox<Tag> checkcomboboxTag;
    @FXML
    ProgressBar progressBar;
    @FXML
    Button btnSave;
    @FXML
    AnchorPane anchorPane;

    private ObservableList<Category> categoriesList;
    private ObservableList<Tag> tagsList;
    private List<Rate> rates = new ArrayList<>();

    private PopOver popOver;
    private BorderPane rootBorderPane;

    private Expense getExpense() {
        Date dueDateValue = datepickerDueDate.getEditor().getText().trim().length() == 0 ?
                null :
                new Date(datepickerDueDate.getEditor().getText());
        Expense e = new Expense(textfieldTitle.getText(),
                textareaDescription.getText(),
                checkboxRecurrent.isSelected(),
                dueDateValue,
                Double.parseDouble(textfieldAmount.getText()),
                choiceboxCategory.getValue());
        List<Tag> tags = new ArrayList<>(checkcomboboxTag.getCheckModel().getCheckedItems());
        e.setTags(tags);
        if (rates.size() > 0) {
            for (Rate rate : rates) {
                //new RateDBHelper().save(rate);
                e.addRate(rate);
            }
            //e.setPayedRates(rates);
        }

        return e;
    }

    public void saveExpense() {
        Expense e = getExpense();
        Category z = choiceboxCategory.getValue();
        e.setCategory(z);

        HibernateHelper.save(e);
        clearFieldSelections();

        Notification.create("Added new expense:\n" + e.getTitle(),
                "Success",
                null);
    }

    public void clearFieldSelections() {
        textfieldTitle.setText("");
        textareaDescription.setText("");
        textfieldAmount.setText("");
        datepickerDueDate.getEditor().setText("");
        checkboxRecurrent.setSelected(false);
        choiceboxCategory.setValue(null);
        checkcomboboxTag.getCheckModel().clearChecks();
        progressBar.setProgress(0);
        rates = null;
        rates = new ArrayList<>();
    }

    public void displayAddCategoryDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add new category");
        dialog.setHeaderText("Enter at least textfieldTitle in order to add a new category");

        ButtonType confirmBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);

        TextField categoryTitle = new TextField();
        categoryTitle.setPromptText("textfieldTitle");
        VBox vBoxTitle = new VBox(new Label("Title:"), categoryTitle);

        TextArea catDescription = new TextArea();
        catDescription.setPromptText("textareaDescription");
        catDescription.setWrapText(true);
        VBox vBoxDescription = new VBox(new Label("Description"), catDescription);

        ColorPicker catColor = new ColorPicker();
        catColor.setMaxWidth(Double.MAX_VALUE);
        VBox vBoxColor = new VBox(new Label("Color:"), catColor);

        VBox vBoxMain = new VBox(vBoxTitle, vBoxDescription, vBoxColor);
        vBoxMain.setSpacing(15);
        vBoxMain.setPadding(new Insets(10, 10, 20, 10));
        StackPane stackPane = new StackPane(vBoxMain);

        Node loginButton = dialog.getDialogPane().lookupButton(confirmBtn);
        loginButton.setDisable(true);

        categoryTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(stackPane);

        dialog.setOnShowing(event -> ControlEffect.setBlur(rootBorderPane, true));
        dialog.setOnCloseRequest(event -> ControlEffect.setBlur(rootBorderPane, false));

        Platform.runLater(() -> categoryTitle.requestFocus());

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
            Category category = new Category(categoryTitle.getText(),
                    catDescription.getText(),
                    catColor.getValue().toString().replace("0x", "#"));
            categoryDBHelper.save(category);
            populateCategories();

            Notification.create("Added category: \n" +
                    category.getName(), "Success", null);
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateCategories();
        populateNewTags();
        linkSaveBtnToMandatoryFields();
        attachPopOver();
        textfieldAmount.textProperty().addListener(TextUtils.getDigitListener());
        Platform.runLater(() -> rootBorderPane = (BorderPane) anchorPane.getParent());
    }

    private void attachPopOver() {
        popOver = new PopOver();

        Platform.runLater(() -> {
            progressBar.setOnMouseEntered(e -> {
                if (rates.size() > 0 && !popOver.isDetached()) {
                    popOver.setContentNode(getPopOverContent());
                    popOver.show(progressBar);
                    popOver.setArrowLocation(PopOver.ArrowLocation.RIGHT_CENTER);
                    popOver.setAutoHide(true);
                    popOver.setOpacity(0.7);
                    popOver.prefWidth(400);
                    popOver.setTitle("TOTAL: " + getTotalValuePayed());
                    popOver.setHeaderAlwaysVisible(true);
                }
            });
        });
    }

    private ScrollPane getPopOverContent() {
        VBox vBox = new VBox(10);
        vBox.prefWidth(Double.MAX_VALUE);
        vBox.setPadding(new Insets(20));
        vBox.getChildren().removeAll();

        if (rates.size() > 0) {
            for (Rate r : rates) {
                Text rateAmount = new Text(r.getAmount().toString());
                Text rateDate = new Text(new SimpleDateFormat("dd.MM.yyyy").format(r.getDate()));
                Text rateDesc = new Text(r.getObservation());
                Button deleteBtn = new Button("Delete");
                deleteBtn.prefWidth(Double.MAX_VALUE);
                Separator separator = new Separator();
                separator.minWidthProperty().bind(vBox.widthProperty());
                deleteBtn.setUserData(r);
                deleteBtn.setOnAction(e -> {
                    // TODO - bug when hovering on delete btn after implementing scrollable pane
                    vBox.getChildren().removeAll(rateAmount, rateDate, rateDesc, deleteBtn, separator);
                    Rate toremove = (Rate) deleteBtn.getUserData();
                    rates.remove(toremove);
                    displayPayProgress();
                    popOver.setTitle("TOTAL: " + getTotalValuePayed());
                });
                vBox.getChildren().addAll(rateAmount, rateDate, rateDesc, deleteBtn, separator);
            }
        }

        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setMinWidth(400);
        scrollPane.setMaxHeight(400);
        vBox.setMinWidth(350);

        return scrollPane;
    }

    private void populateCategories() {
        CategoryDBHelper c = new CategoryDBHelper();
        categoriesList = FXCollections.observableArrayList(c.fetchAll());
        choiceboxCategory.setItems(categoriesList);
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
        checkcomboboxTag.getItems().clear();
        checkcomboboxTag.getItems().addAll(tagsList);
    }

    @FXML
    public void displayAddTagDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add new tag");
        dialog.setHeaderText("Enter at least TITLE in order to add a new tag");

        ButtonType confirmBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);

        Label labelName = new Label("Name:");
        TextField tagName = new TextField();
        tagName.setPromptText("name");
        tagName.setMaxWidth(Double.MAX_VALUE);
        VBox vBoxTitle = new VBox(labelName, tagName);

        Label labelColor = new Label("Color:");
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setMaxWidth(Double.MAX_VALUE);
        colorPicker.setPromptText("choose color");
        VBox vBoxColor = new VBox(labelColor, colorPicker);

        VBox vBoxMain = new VBox(vBoxTitle, vBoxColor);
        vBoxMain.setSpacing(15);
        vBoxMain.setPadding(new Insets(10, 10, 20, 10));

        Node saveBtn = dialog.getDialogPane().lookupButton(confirmBtn);
        saveBtn.setDisable(true);

        tagName.textProperty().addListener((observable, oldValue, newValue) -> {
            saveBtn.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(new StackPane(vBoxMain));

        dialog.setOnShowing(event -> ControlEffect.setBlur(rootBorderPane, true));
        dialog.setOnCloseRequest(event -> ControlEffect.setBlur(rootBorderPane, false));

        Platform.runLater(() -> tagName.requestFocus());

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

            Notification.create("Added new tag:\n" + tag.getName(),
                    "Success", null);
        });

    }

    @FXML
    public void displayAddPayedRatesDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add payed rate");
        dialog.setHeaderText("Enter at least AMOUNT in order to add a rate");

        ButtonType confirmBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);

        Label labelAmount = new Label("Amount:");
        TextField textFieldAmountRate = new TextField();
        textFieldAmountRate.setPromptText("rate amount");
        textFieldAmountRate.setMaxWidth(Double.MAX_VALUE);
        textFieldAmountRate.textProperty().addListener(TextUtils.getDigitListener());
        VBox vBoxAmount = new VBox(labelAmount, textFieldAmountRate);

        Label labelDate = new Label("Date:");
        DatePicker datePicker = new DatePicker();
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setPromptText("set date");
        datePicker.getEditor().setDisable(true);
        datePicker.setDayCellFactory((DatePicker datePickerz) -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #999A9D;");
                }
            }
        });
        VBox vBoxDate = new VBox(labelDate, datePicker);

        Label labelObservations = new Label("Observations:");
        TextArea textAreaObservations = new TextArea();
        textAreaObservations.setMaxWidth(Double.MAX_VALUE);
        textAreaObservations.setPromptText("observations");
        textAreaObservations.setWrapText(true);
        VBox vBoxObs = new VBox(labelObservations, textAreaObservations);

        VBox vBoxMain = new VBox(vBoxAmount, vBoxDate, vBoxObs);
        vBoxMain.setSpacing(15);
        vBoxMain.setPadding(new Insets(10, 10, 20, 10));

        Node saveBtn = dialog.getDialogPane().lookupButton(confirmBtn);
        saveBtn.setDisable(true);

        saveBtn.disableProperty().bind(
                Bindings.or(
                        textFieldAmountRate.textProperty().isEmpty(),
                        datePicker.valueProperty().isNull()
                ));

        dialog.getDialogPane().setContent(new StackPane(vBoxMain));
        dialog.setOnShowing(event -> ControlEffect.setBlur(rootBorderPane, true));
        dialog.setOnCloseRequest(event -> ControlEffect.setBlur(rootBorderPane, false));

        Platform.runLater(() -> textFieldAmountRate.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmBtn) {
                return new Pair<String, String>(textFieldAmountRate.getText(), datePicker.getValue().toString());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            Rate rate = new Rate(Double.parseDouble(textFieldAmountRate.getText()),
                    new Date(datePicker.getEditor().getText()),
                    textAreaObservations.getText());
            // new RateDBHelper().save(rate);
            rates.add(rate);
            displayPayProgress();

            Notification.create("Added partial pay, textfieldAmount:\n" + rate.getAmount(),
                    "Success", null);
        });

    }

    private void displayPayProgress() {
        if (textfieldAmount.getText().trim().length() != 0) {
            double amountPayed = getTotalValuePayed();
            Double payed = (amountPayed * 100) / Double.parseDouble(textfieldAmount.getText());
            System.out.println("TOTAL: " + Double.parseDouble(textfieldAmount.getText()));
            System.out.println("PAYED: " + payed);
            progressBar.setProgress(payed / 100);

            DecimalFormat df = new DecimalFormat("#.##");
            payed = Double.valueOf(df.format(payed));
            System.out.println("FORMATED: " + payed);
            progressBar.getTooltip().setText(getRatesInfoString());
        }
    }

    private double getTotalValuePayed() {
        double total = 0;
        if (rates.size() > 0) {
            for (Rate r : rates) {
                total += r.getAmount();
            }
        }
        return total;
    }

    private String getRatesInfoString() {
        String info = "";
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

        if (rates.size() > 0) {
            for (Rate r : rates) {
                info += "\nRate\n" +
                        "value: " + r.getAmount() + "\n" +
                        "Date: " + format.format(r.getDate()) + "\n" +
                        "Info: " + r.getObservation() + "\n" +
                        "****************************";
            }
        }

        info += "\nTotal: \t" + getTotalValuePayed();
        return info;
    }

    private void linkSaveBtnToMandatoryFields() {
        btnSave.disableProperty().bind(
                Bindings.or(
                        textfieldTitle.textProperty().isEmpty(),
                        textfieldAmount.textProperty().isEmpty()
                ));
    }

}
