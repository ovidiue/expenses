package controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import helpers.repositories.CategoryRepository;
import helpers.repositories.ExpenseRepository;
import helpers.repositories.TagRepository;
import helpers.ui.DialogBuilder;
import helpers.ui.ModalBuilder;
import helpers.ui.ModalCategoryContent;
import helpers.ui.ModalTagContent;
import helpers.ui.Notification;
import helpers.ui.TextUtils;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import model.Category;
import model.Expense;
import model.Rate;
import model.Tag;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.PopOver;


/**
 * Created by Ovidiu on 18-May-18.
 */
@Slf4j
public class AddExpenseController implements Initializable {

  private static final CategoryRepository CATEGORY_REPOSITORY = new CategoryRepository();
  private static final ExpenseRepository EXPENSE_REPOSITORY = new ExpenseRepository();
  private static final TagRepository TAG_REPOSITORY = new TagRepository();
  @FXML
  JFXTextField textfieldTitle;
  @FXML
  JFXTextArea textareaDescription;
  @FXML
  JFXTextField textfieldAmount;
  @FXML
  JFXDatePicker datepickerDueDate;
  @FXML
  JFXComboBox<Category> choiceboxCategory;
  @FXML
  JFXCheckBox checkboxRecurrent;
  @FXML
  CheckComboBox<Tag> checkcomboboxTag;
  @FXML
  JFXProgressBar progressBar;
  @FXML
  JFXButton btnSave;
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
        e.addRate(rate);
      }
    }

    return e;
  }

  @FXML
  public void saveExpense() {
    Expense e = getExpense();
    Category z = choiceboxCategory.getValue();
    e.setCategory(z);

    EXPENSE_REPOSITORY.save(e);
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

  @FXML
  public void displayAddCategoryDialog() {
    ModalCategoryContent cat = new ModalCategoryContent();
    ModalBuilder modal = new ModalBuilder(cat,
        (Stage) this.btnSave.getScene().getWindow());
    modal.show()
        .filter(resp -> ((ButtonType) resp).getText().equals("Save"))
        .ifPresent(catResponse -> {
          Category category = cat.getResult();
          CATEGORY_REPOSITORY.save(category);
          this.categoriesList.add(category);
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

  private void attachErrorsForMandatoryFields() {

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
          popOver.setOpacity(0.8);
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
    categoriesList = FXCollections.observableArrayList(CATEGORY_REPOSITORY.fetchAll());
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
    tagsList = FXCollections.observableArrayList(TAG_REPOSITORY.fetchAll());
    checkcomboboxTag.getItems().clear();
    checkcomboboxTag.getItems().addAll(tagsList);
  }

  @FXML
  public void displayAddTagDialog() {
    ModalTagContent tagContent = new ModalTagContent();
    ModalBuilder modalTag = new ModalBuilder(tagContent,
        (Stage) this.btnSave.getScene().getWindow());

    modalTag.show()
        .filter(resp -> ((ButtonType) resp).getText().equals("Save"))
        .ifPresent(e -> {
          Tag tag = tagContent.getResult();
          TAG_REPOSITORY.save(tag);
          this.tagsList.add(tag);
          Notification.create("Added new tag:\n" + tag.getName(),
              "Success", null);
        });
  }

  @FXML
  public void displayAddPayedRatesDialog() {
    DialogBuilder dialogBuilder = new DialogBuilder();
    TextField textFieldAmountRate = new TextField();
    textFieldAmountRate.textProperty().addListener(TextUtils.getDigitListener());

    DatePicker datePicker = new DatePicker();
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

    dialogBuilder
        .setCallerPane(rootBorderPane)
        .setHeader("Enter at least AMOUNT in order to add a rate")
        .addFormField("Amount:", "amount", textFieldAmountRate, true)
        .addFormField("Date:", "date", datePicker, true)
        .addFormField("Obsevations:", "observation", new TextArea(), false)
        .show()
        .ifPresent(result -> {
          if (result == dialogBuilder.getConfirmAction()) {
            Rate rate = new Rate(
                Double.parseDouble(textFieldAmountRate.getText()),
                new Date(datePicker.getEditor().getText()),
                ((TextArea) dialogBuilder.getControl("observation")).getText());

            rates.add(rate);
            displayPayProgress();

            Notification.create("Added partial pay, textfieldAmount:\n" + rate.getAmount(),
                "Success", null);
          }
        });
  }

  private void displayPayProgress() {
    if (textfieldAmount.getText().trim().length() != 0) {
      double amountPayed = getTotalValuePayed();
      Double payed = (amountPayed * 100) / Double.parseDouble(textfieldAmount.getText());
      log.info("TOTAL: " + Double.parseDouble(textfieldAmount.getText()));
      log.info("PAYED: " + payed);
      progressBar.setProgress(payed / 100);

      DecimalFormat df = new DecimalFormat("#.##");
      payed = Double.valueOf(df.format(payed));
      log.info("FORMATED: " + payed);
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
