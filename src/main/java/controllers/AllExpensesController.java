package controllers;

import helpers.repositories.CategoryRepository;
import helpers.repositories.ExpenseRepository;
import helpers.repositories.RateRepository;
import helpers.repositories.TagRepository;
import helpers.ui.DialogBuilder;
import helpers.ui.Notification;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import model.Category;
import model.Expense;
import model.Rate;
import model.Tag;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.control.PopOver;

/**
 * Created by Ovidiu on 18-May-18.
 */

@Slf4j
public class AllExpensesController implements Initializable {

  private static final ExpenseRepository EXPENSE_REPOSITORY = new ExpenseRepository();
  private static final RateRepository RATE_REPOSITORY = new RateRepository();
  private static final CategoryRepository CATEGORY_REPOSITORY = new CategoryRepository();
  private static final TagRepository TAG_REPOSITORY = new TagRepository();
    @FXML
    AnchorPane anchorPane;
    BorderPane rootBorderPane;
    @FXML
    MasterDetailPane masterDetailPane;
    private PopOver popOver = new PopOver();
    private TableView<Rate> tableViewDetail = getRateTable();
    private TableView<Expense> tableViewMaster = getExpenseTable();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
      Platform.runLater(() -> {
        rootBorderPane = (BorderPane) anchorPane.getParent();
        initMasterDetailPane();
      });
    }

    private void initMasterDetailPane() {
        masterDetailPane.setMasterNode(tableViewMaster);
        masterDetailPane.setDetailNode(buildDetailContent());
        /*masterDetailPane.getDetailNode().minHeight(200);
        masterDetailPane.getDetailNode().prefHeight(500);*/

        setClickBehaviourMasterDetailsPane();
    }

    private AnchorPane buildDetailContent() {
        AnchorPane anchorPane = new AnchorPane();
        GridPane gridPane = new GridPane();
        Label labelTitle = new Label("Rates");
        Button buttonAddRate = new Button("Add");
        buttonAddRate.disableProperty().bind(tableViewMaster.getSelectionModel().selectedItemProperty().isNull());
        buttonAddRate.setOnAction(e -> displayAddRateToExpense());

        gridPane.add(labelTitle, 0, 0);
        gridPane.add(buttonAddRate, 1, 0);
        gridPane.setHgrow(labelTitle, Priority.ALWAYS);
        gridPane.setHgrow(buttonAddRate, Priority.ALWAYS);
        gridPane.setHalignment(labelTitle, HPos.LEFT);
        gridPane.setHalignment(buttonAddRate, HPos.RIGHT);

        anchorPane.getChildren().addAll(gridPane, tableViewDetail);

        labelTitle.setStyle("-fx-font-size: 30");
        buttonAddRate.setMinWidth(100);
        buttonAddRate.setStyle("-fx-font-weight: bold");

        anchorPane.setTopAnchor(gridPane, 0.0);
        anchorPane.setTopAnchor(tableViewDetail, 50.0);
        anchorPane.setBottomAnchor(tableViewDetail, 0.0);
        anchorPane.setLeftAnchor(tableViewDetail, 0.0);
        anchorPane.setLeftAnchor(gridPane, 0.0);
        anchorPane.setRightAnchor(gridPane, 0.0);
        anchorPane.setRightAnchor(tableViewDetail, 0.0);

        return anchorPane;
    }

    private void displayAddRateToExpense() {
        int expenseId = tableViewMaster.getSelectionModel().getSelectedItem().getId();
        Expense expense = EXPENSE_REPOSITORY.findById(expenseId);
        DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.setCallerPane(rootBorderPane)
                .setHeader("Add rate to " + expense.getTitle() + " expense")
                .addFormField("Amount:", "amount", new TextField(), true)
                .addFormField("Date:", "date", new DatePicker(), true)
                .addFormField("Observation:", "observation", new TextArea(), false)
                .show()
                .ifPresent(result -> {
                    if (result == dialogBuilder.getConfirmAction()) {
                        String amountString = ((TextField) dialogBuilder.getControl("amount")).getText();
                        String dateString = ((DatePicker) dialogBuilder.getControl("date")).getEditor().getText();
                        Rate rate = new Rate(
                                Double.parseDouble(amountString),
                                new Date(dateString),
                                ((TextArea) dialogBuilder.getControl("observation")).getText()
                        );

                        expense.addRate(rate);
                        EXPENSE_REPOSITORY.update(expense);
                        tableViewDetail.getItems().add(rate);
                        tableViewDetail.refresh();
                        Notification.create("Added rate:\n" + rate.getAmount(),
                                "Success",
                                null);

                    }
                });

    }


    private void setClickBehaviourMasterDetailsPane() {
        masterDetailPane.setAnimated(true);
        tableViewMaster.setRowFactory(e -> {
            TableRow<Expense> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Expense expense = row.getItem();
                    masterDetailPane.setShowDetailNode(true);
                    ObservableList<Rate> ratesList = FXCollections.observableArrayList(RATE_REPOSITORY.fetchAll(expense));
                    updateDetailsPane(ratesList);
                }
            });
            return row;
        });
    }

    private void updateDetailsPane(ObservableList<Rate> rates) {
        tableViewDetail.setItems(rates);
        tableViewDetail.refresh();
    }

    private TableView<Expense> getExpenseTable() {
        tableViewMaster = new TableView<>();
        tableViewMaster.setEditable(true);

        TableColumn<Expense, String> titleCol,
                descriptionCol,
            deleteCol,
            editCol;

        TableColumn<Expense, Category> categoryCol;

        TableColumn<Expense, Date> createdOnCol,
                dueDateCol;

        TableColumn<Expense, Double> amountCol;
        TableColumn<Expense, Boolean> recurrentCol;

        titleCol = new TableColumn<>("Title");
        descriptionCol = new TableColumn<>("Description");
        recurrentCol = new TableColumn<>("Is recurrent");
        createdOnCol = new TableColumn<>("Created on");
        dueDateCol = new TableColumn<>("Due date");
        amountCol = new TableColumn<>("Amount");
        deleteCol = new TableColumn<>("Delete");
        categoryCol = new TableColumn<>("Category");
      editCol = new TableColumn<>("Edit");


        descriptionCol.setCellFactory((TableColumn<Expense, String> column) -> new TableCell<Expense, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                this.setFocused(true);

              if (item != null) {
                    setText(item.substring(0, item.length() > 15 ? 15 : item.length()));
                setOnMouseClicked(e -> {
                  if (e.getClickCount() == 2) {
                    TextArea textArea = getTextArea(item);

                    popOver.setContentNode(textArea);
                    popOver.show((Text) e.getTarget());
                    popOver.setAutoHide(true);
                    popOver.setHeaderAlwaysVisible(true);
                    popOver.setDetachable(true);
                    popOver.setAnimated(true);
                    popOver.setTitle(((Expense) getTableRow().getItem()).getTitle());

                    popOver.setOnHidden(event -> {
                      Expense ex = (Expense) getTableRow().getItem();
                      if (!ex.getDescription().equals(textArea.getText())) {
                        ex.setDescription(textArea.getText());
                        EXPENSE_REPOSITORY.update(ex);
                        tableViewMaster.refresh();
                      }
                    });
                  }

                    });
                }
            }
        });

        titleCol.setCellFactory(TextFieldTableCell.forTableColumn());
        titleCol.setOnEditCommit(e -> {
            final String value = e.getNewValue() != null ?
                    e.getNewValue() :
                    e.getOldValue();

            Expense expense = tableViewMaster.getSelectionModel().getSelectedItem();
            expense.setTitle(value);
            EXPENSE_REPOSITORY.update(expense);
            tableViewMaster.refresh();
        });

        amountCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return String.valueOf(object);
            }

            @Override
            public Double fromString(String string) {
                return Double.parseDouble(string);
            }
        }));
        amountCol.setOnEditCommit(e -> {
            final Double value = e.getNewValue() != null ?
                    e.getNewValue() :
                    e.getOldValue();

            Expense expense = tableViewMaster.getSelectionModel().getSelectedItem();
            expense.setAmount(value);
            EXPENSE_REPOSITORY.update(expense);
            tableViewMaster.refresh();
        });

      titleCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(9));
      descriptionCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(9));
      recurrentCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(9));
      createdOnCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(9));
      dueDateCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(9));
      amountCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(9));
      deleteCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(9));
      categoryCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(9));

        createdOnCol.setCellFactory(column -> {
            TableCell<Expense, Date> cell = new TableCell<Expense, Date>() {
                private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        this.setText(format.format(item));
                        this.setTooltip(new Tooltip("Time\n" + item.getHours() + ":" + item.getMinutes()));
                    }
                }
            };

            return cell;
        });

        dueDateCol.setCellFactory(column -> {
            TableCell<Expense, Date> cell = new TableCell<Expense, Date>() {
                private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        final DatePicker dp = new DatePicker();
                        dp.valueProperty().addListener((observable, oldValue, newValue) -> {
                        });
                        dp.setPrefWidth(0);
                        dp.setVisible(false);
                        this.setText(format.format(item));
                        this.setOnMouseClicked(e -> {
                            if (e.getClickCount() == 2)
                                dp.show();
                        });
                        setGraphic(dp);
                    }
                }
            };

            return cell;
        });

        deleteCol.setCellFactory((TableColumn<Expense, String> column) -> {
            TableCell<Expense, String> cell = new TableCell<Expense, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                      final Button deleteBtn = new Button();
                      ImageView graphic = new ImageView("images/delete.png");
                      graphic.setFitHeight(20);
                      graphic.setFitWidth(20);
                      deleteBtn.setGraphic(graphic);
                        deleteBtn.setMaxWidth(Double.MAX_VALUE);
                        deleteBtn.setOnAction(e -> {
                            this.setFocused(true);
                            Expense expense = getTableView().getItems().get(getIndex());
                            displayDeleteExpenseConfirmation(expense);
                        });

                        setGraphic(deleteBtn);
                    }
                }
            };
            return cell;
        });

      editCol.setCellFactory((TableColumn<Expense, String> column) -> {
        TableCell<Expense, String> cell = new TableCell<Expense, String>() {
          @Override
          protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
              setText(null);
            } else {
              final Button editBtn = new Button();
              ImageView graphic = new ImageView("images/edit.png");
              graphic.setFitHeight(20);
              graphic.setFitWidth(20);
              editBtn.setGraphic(graphic);
              editBtn.setMaxWidth(Double.MAX_VALUE);
              editBtn.setOnAction(e -> {
                this.setFocused(true);
                log.info("EDIT PRESSED");
                int id = getTableView().getItems().get(getIndex()).getId();
                Expense expense = EXPENSE_REPOSITORY.findById(id);
                editExpenseDialog(expense);
              });

              setGraphic(editBtn);
            }
          }
        };
        return cell;
      });


        recurrentCol.setCellFactory((TableColumn<Expense, Boolean> column) -> {
            TableCell<Expense, Boolean> cell = new TableCell<Expense, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty)
                        setText(null);
                    else {
                        CheckBox cb = new CheckBox();
                        cb.setSelected(item);
                        cb.setOnAction(e -> {
                            boolean value = cb.isSelected();

                            Expense expense = (Expense) getTableRow().getItem();
                            expense.setRecurrent(value);
                            EXPENSE_REPOSITORY.update(expense);
                        });

                        setGraphic(cb);
                    }
                }
            };
            return cell;
        });


        categoryCol.setCellFactory(ChoiceBoxTableCell.forTableColumn(FXCollections.observableList(new CategoryRepository().fetchAll())));

        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        recurrentCol.setCellValueFactory(new PropertyValueFactory<>("recurrent"));
        createdOnCol.setCellValueFactory(new PropertyValueFactory<>("createdOn"));
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        tableViewMaster.getColumns().addAll(titleCol,
                descriptionCol,
                recurrentCol,
                createdOnCol,
                dueDateCol,
                amountCol,
                categoryCol,
            deleteCol,
            editCol);

        tableViewMaster.setItems(getAllExpenses());
        tableViewMaster.setEditable(true);
      tableViewMaster.autosize();

        return tableViewMaster;
    }

  private TextArea getTextArea(String item) {
    TextArea textArea = new TextArea();
    textArea.setWrapText(true);
    textArea.setEditable(false);
    textArea.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        textArea.setEditable(true);
        textArea.setTooltip(null);
      }
    });
    textArea.setText(item);
    textArea.setTooltip(new Tooltip("Double click to edit"));
    return textArea;
  }

    private TableView<Rate> getRateTable() {
        tableViewDetail = new TableView<>();
        tableViewDetail.setEditable(true);

        // declare columns
        TableColumn<Rate, Double> colRateAmount;
        TableColumn<Rate, Date> colPayedOn;
        TableColumn<Rate, String> colObservation;
        TableColumn<Rate, Button> colDelete;

        // set columns titles
        colRateAmount = new TableColumn<>("Rate amount");
        colPayedOn = new TableColumn<>("Payed on");
        colObservation = new TableColumn<>("Observation");
        colDelete = new TableColumn<>("Delete");

        // set values to display in columns
        colRateAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colPayedOn.setCellValueFactory(new PropertyValueFactory<>("date"));
        colObservation.setCellValueFactory(new PropertyValueFactory<>("observation"));

        colRateAmount.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return String.valueOf(object);
            }

            @Override
            public Double fromString(String string) {
                return Double.parseDouble(string);
            }
        }));

        colRateAmount.setOnEditCommit(event -> {
            Rate rate = event.getRowValue();
            rate.setAmount(event.getNewValue());
            RATE_REPOSITORY.update(rate);
        });

        colObservation.setCellFactory((TableColumn<Rate, String> column) -> {
            TableCell<Rate, String> cell = new TableCell<Rate, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    this.setFocused(true);

                    VBox vBoxObservation = new VBox(5);
                    vBoxObservation.setAlignment(Pos.CENTER_RIGHT);
                    vBoxObservation.setPadding(new Insets(10));

                    TextArea textArea = new TextArea();
                    textArea.setWrapText(true);
                    textArea.setEditable(false);
                    textArea.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) {
                            textArea.setEditable(true);
                            textArea.setTooltip(null);
                        }
                    });
                    textArea.setTooltip(new Tooltip("Double click to edit"));
                    Button btnConfirmEdit = new Button("Confirm");
                    vBoxObservation.getChildren().addAll(textArea, btnConfirmEdit);

                    popOver.setContentNode(vBoxObservation);
                    popOver.setAutoHide(true);
                    popOver.setHeaderAlwaysVisible(true);
                    popOver.setDetachable(true);
                    popOver.setAnimated(true);


                    if (item != null && item.trim().length() > 0) {
                        setText(item.substring(0, item.length() > 15 ? 15 : item.length()));
                        setOnMouseEntered(e -> {
                            textArea.setText(item);
                            popOver.setTitle(((Rate) getTableRow().getItem()).getAmount().toString());

                            btnConfirmEdit.setOnAction(event -> {
                                Rate rate = (Rate) getTableRow().getItem();
                                if (!rate.getObservation().equals(textArea.getText())) {
                                    rate.setObservation(textArea.getText());
                                    RATE_REPOSITORY.update(rate);
                                    tableViewDetail.refresh();
                                    popOver.hide();
                                }
                            });

                            popOver.show((TableCell) e.getTarget());
                        });
                    } else {
                        setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
                                btnConfirmEdit.setOnAction(e -> {
                                    Rate rate = (Rate) getTableRow().getItem();
                                    rate.setObservation(textArea.getText());
                                    RATE_REPOSITORY.update(rate);
                                    tableViewDetail.refresh();
                                    popOver.hide();
                                });
                                btnConfirmEdit.disableProperty().bind(textArea.textProperty().isEmpty());

                                popOver.show((TableCell) event.getTarget());
                                popOver.setTitle(((Rate) getTableRow().getItem()).getAmount().toString());
                            }
                        });
                    }
                }


            };
            return cell;
        });

        // split columns widths to match table
      Binding<Number> preferredWidth = tableViewDetail.widthProperty().divide(4);
        colRateAmount.prefWidthProperty().bind(preferredWidth);
        colPayedOn.prefWidthProperty().bind(preferredWidth);
        colObservation.prefWidthProperty().bind(preferredWidth);
        colDelete.prefWidthProperty().bind(preferredWidth);

        colPayedOn.setCellFactory(column -> {
            TableCell<Rate, Date> cell = new TableCell<Rate, Date>() {
                private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        this.setText(format.format(item));
                    }
                }
            };

            return cell;
        });

        colDelete.setCellFactory(column -> {
            TableCell<Rate, Button> cell = new TableCell<Rate, Button>() {

                @Override
                protected void updateItem(Button item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        final Button DELETE_BTN = new Button("delete");
                        DELETE_BTN.setMaxWidth(Double.MAX_VALUE);
                        DELETE_BTN.setOnAction(e -> {
                            displayDeleteRateConfirmation((Rate) getTableRow().getItem());
                        });

                        setGraphic(DELETE_BTN);
                    }
                }
            };

            return cell;
        });


        // get table data
        ObservableList<Rate> listRates = FXCollections.observableArrayList(RATE_REPOSITORY.fetchAll());
        tableViewDetail.setItems(listRates);

        // add all columns
        tableViewDetail.getColumns().addAll(colRateAmount, colPayedOn, colObservation, colDelete);

        return tableViewDetail;
    }

    private void displayDeleteExpenseConfirmation(Expense e) {
        DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.setCallerPane(rootBorderPane)
                .setHeader("Are you sure you want to delete " + e.getTitle() + " expense ?")
                .show()
                .ifPresent(response -> {
                    if (response == dialogBuilder.getConfirmAction()) {
                        EXPENSE_REPOSITORY.delete(e);
                        tableViewMaster.getItems().remove(e);
                        tableViewMaster.refresh();

                        Notification.create("Deleted expense:\n" + e.getTitle(),
                                "Success",
                                null);
                    }
                });

    }

    private ObservableList<Expense> getAllExpenses() {
        ObservableList<Expense> list = FXCollections.observableArrayList(new ExpenseRepository().fetchAll());
        return list;
    }

    private void displayDeleteRateConfirmation(Rate rate) {
        DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.setCallerPane(rootBorderPane)
                .setHeader("Are you sure you want to delete " + rate.getAmount() + " rate ?")
                .show()
                .ifPresent(response -> {
                    if ((ButtonType) response == dialogBuilder.getConfirmAction()) {
                        RATE_REPOSITORY.delete(rate);
                        tableViewDetail.getItems().remove(rate);
                        tableViewDetail.refresh();

                        Notification.create("Deleted rate:\n" + rate.getAmount(),
                                "Success",
                                null);
                    }
                });
    }

    @FXML
    public void addExpenseDialog() {
        DialogBuilder dialogBuilder = new DialogBuilder();
      ChoiceBox<Category> choiceBoxCat = new ChoiceBox<>(
          FXCollections.observableArrayList(CATEGORY_REPOSITORY.fetchAll()));
      CheckComboBox<Tag> checkComboBoxTag = new CheckComboBox<>(
          FXCollections.observableArrayList(TAG_REPOSITORY.fetchAll()));
        dialogBuilder.setCallerPane(rootBorderPane)
            .setHeader("Add expense")
            .addFormField("Title:", "title", new TextField(), true)
            .addFormField("Description:", "description", new TextArea(), false)
            .addFormField("Recurrent:", "recurrent", new CheckBox(), false)
            .addFormField("Due date:", "dueDate", new DatePicker(), false)
            .addFormField("Amount:", "amount", new TextField(), true)
            .addFormField("Category:", "category", choiceBoxCat, false)
            .addFormField("Tag(s):", "tags", checkComboBoxTag, false)
            .show()
            .ifPresent(result -> {
              if (result == dialogBuilder.getConfirmAction()) {
                String title = ((TextField) dialogBuilder.getControl("title")).getText(),
                    description = ((TextArea) dialogBuilder.getControl("description"))
                        .getText(),
                    dueDate = ((DatePicker) dialogBuilder.getControl("dueDate")).getEditor()
                        .getText(),
                    amount = ((TextField) dialogBuilder.getControl("amount")).getText();
                boolean isRecurrent = ((CheckBox) dialogBuilder.getControl("recurrent"))
                    .isSelected();
                Category category = ((ChoiceBox<Category>) dialogBuilder.getControl("category"))
                    .getValue();
                List<Tag> tags = ((CheckComboBox<Tag>) dialogBuilder.getControl("tags"))
                    .getCheckModel().getCheckedItems();

                Expense expense = new Expense(
                    title,
                    description,
                    isRecurrent,
                    dueDate.trim().length() > 0 ? new Date(dueDate) : null,
                    Double.parseDouble(amount),
                    category
                );

                expense.getTags().addAll(tags);

                EXPENSE_REPOSITORY.save(expense);
                tableViewMaster.getItems().add(expense);
                tableViewMaster.refresh();
                Notification.create("Added Expense:\n" + expense.getTitle(),
                    "Success",
                    null);
              }
            });

    }


  public void editExpenseDialog(Expense ex) {
    DialogBuilder dialogBuilder = new DialogBuilder();
    ChoiceBox<Category> choiceBoxCat = new ChoiceBox<>(
        FXCollections.observableArrayList(CATEGORY_REPOSITORY.fetchAll()));
    Platform.runLater(() -> {
      log.info("CATEGORY: ",
          ex.getCategory().getColor() + " " + ex.getCategory().getDescription());
      choiceBoxCat.getSelectionModel().select(ex.getCategory());
    });
    CheckComboBox<Tag> checkComboBoxTag = new CheckComboBox<>(
        FXCollections.observableArrayList(TAG_REPOSITORY.fetchAll()));
    Object[] intersectingTags = checkComboBoxTag.getItems()
        .stream()
        .filter(ex.getTags()::contains)
        .toArray();
    Platform.runLater(() -> {
      Arrays.stream(intersectingTags)
          .forEach(e -> checkComboBoxTag.getCheckModel().check((Tag) e));
    });
    TextField textFieldTitle = new TextField(ex.getTitle());
    TextArea textAreaDescription = new TextArea(ex.getDescription());
    CheckBox checkBoxRecurrent = new CheckBox();
    checkBoxRecurrent.setSelected(ex.isRecurrent());
    DatePicker datePickerDueDate = new DatePicker();
    if (ex.getDueDate() != null) {
      datePickerDueDate.setValue(LocalDate
          .of(ex.getDueDate().getYear(), ex.getDueDate().getMonth(),
              ex.getDueDate().getDay()));
    }
    TextField textFieldAmount = new TextField(String.valueOf(ex.getAmount()));
    dialogBuilder.setCallerPane(rootBorderPane)
        .setHeader("Edit expense " + ex.getTitle())
        .addFormField("Title:", "title", textFieldTitle, true)
        .addFormField("Description:", "description", textAreaDescription, false)
        .addFormField("Recurrent:", "recurrent", checkBoxRecurrent, false)
        .addFormField("Due date:", "dueDate", datePickerDueDate, false)
        .addFormField("Amount:", "amount", textFieldAmount, true)
        .addFormField("Category:", "category", choiceBoxCat, false)
        .addFormField("Tag(s):", "tags", checkComboBoxTag, false)
        .show()
        .ifPresent(result -> {
          if (result == dialogBuilder.getConfirmAction()) {

            String title = textFieldTitle.getText(),
                description = (textAreaDescription).getText(),
                dueDate = (datePickerDueDate).getEditor().getText(),
                amount = (textFieldAmount).getText();
            boolean isRecurrent = checkBoxRecurrent.isSelected();
            Category category = choiceBoxCat.getValue();
            List<Tag> tags = checkComboBoxTag.getCheckModel().getCheckedItems();

            ex.setTitle(title);
            ex.setDescription(description);
            ex.setRecurrent(isRecurrent);
            ex.setDueDate(dueDate.trim().length() > 0 ? new Date(dueDate) : null);
            ex.setAmount(Double.parseDouble(amount));
            ex.setCategory(category);

            ex.getTags().clear();
            ex.setTags(tags);

            EXPENSE_REPOSITORY.update(ex);
            tableViewMaster.getItems()
                .stream()
                .filter(e -> e.getId() == ex.getId())
                .findFirst()
                .ifPresent(e -> {
                  tableViewMaster.getItems().remove(e);
                  tableViewMaster.getItems().add(ex);
                });

            tableViewMaster.refresh();
            Notification.create("Updated Expense:\n" + ex.getTitle(),
                "Success",
                null);
          }
        });

    }
}
