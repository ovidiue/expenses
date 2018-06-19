package Controllers;

import helpers.db.CategoryDBHelper;
import helpers.db.ExpenseDBHelper;
import helpers.db.RateDBHelper;
import helpers.db.TagDBHelper;
import helpers.ui.DialogBuilder;
import helpers.ui.Notification;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import model.Category;
import model.Expense;
import model.Rate;
import model.Tag;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.control.PopOver;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 18-May-18.
 */
public class AllExpensesController implements Initializable {
    @FXML
    AnchorPane anchorPane;

    BorderPane rootBorderPane;


    @FXML
    MasterDetailPane masterDetailPane;
    private PopOver popOver = new PopOver();
    private TableView<Expense> tableViewMaster = getExpenseTable();
    private TableView<Rate> tableViewDetail = getRateTable();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initMasterDetailPane();
        Platform.runLater(() -> rootBorderPane = (BorderPane) anchorPane.getParent());
    }

    private void initMasterDetailPane() {
        masterDetailPane.setMasterNode(tableViewMaster);
        masterDetailPane.setDetailNode(buildDetailContent());
        masterDetailPane.getDetailNode().minHeight(200);
        masterDetailPane.getDetailNode().prefHeight(500);

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
        ExpenseDBHelper expenseDBHelper = new ExpenseDBHelper();
        Expense expense = expenseDBHelper.findById(expenseId);
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
                        expenseDBHelper.update(expense);
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
                    ObservableList<Rate> ratesList = FXCollections.observableArrayList(new RateDBHelper().fetchAll(expense));
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
                deleteCol;

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

        descriptionCol.setCellFactory((TableColumn<Expense, String> column) -> new TableCell<Expense, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                this.setFocused(true);

                if (item != null && item.trim().length() > 0) {
                    setText(item.substring(0, item.length() > 15 ? 15 : item.length()));
                    setOnMouseEntered(e -> {
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

                        popOver.setContentNode(textArea);
                        popOver.show((TableCell) e.getTarget());
                        popOver.setAutoHide(true);
                        popOver.setHeaderAlwaysVisible(true);
                        popOver.setDetachable(true);
                        popOver.setAnimated(true);
                        popOver.setTitle(((Expense) getTableRow().getItem()).getTitle());

                        popOver.setOnHidden(event -> {
                            Expense ex = (Expense) getTableRow().getItem();
                            if (!ex.getDescription().equals(textArea.getText())) {
                                ex.setDescription(textArea.getText());
                                new ExpenseDBHelper().update(ex);
                                tableViewMaster.refresh();
                            }
                        });
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
            new ExpenseDBHelper().update(expense);
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
            new ExpenseDBHelper().update(expense);
            tableViewMaster.refresh();
        });

        titleCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        descriptionCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        recurrentCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        createdOnCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        dueDateCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        amountCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        deleteCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        categoryCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));

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
                            /*Expense expense = (Expense) getTableRow().getItem();
                            expense.setDueDate();
                            new ExpenseDBHelper().update(expense);*/

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
                        final Button deleteBtn = new Button("delete");
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
                            new ExpenseDBHelper().update(expense);
                        });

                        setGraphic(cb);
                    }
                }
            };
            return cell;
        });


        categoryCol.setCellFactory(ChoiceBoxTableCell.forTableColumn(FXCollections.observableList(new CategoryDBHelper().fetchAll())));

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
                deleteCol);

        tableViewMaster.setItems(getAllExpenses());
        tableViewMaster.setEditable(true);

        return tableViewMaster;
    }

    private TableView<Rate> getRateTable() {
        RateDBHelper rateDBHelper = new RateDBHelper();
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
            rateDBHelper.update(rate);
        });

        colObservation.setCellFactory((TableColumn<Rate, String> column) -> {
            TableCell<Rate, String> cell = new TableCell<Rate, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    this.setFocused(true);

                    if (item != null && item.trim().length() > 0) {
                        setText(item.substring(0, item.length() > 15 ? 15 : item.length()));
                        setOnMouseEntered(e -> {
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
                            textArea.setText(item);
                            textArea.setTooltip(new Tooltip("Double click to edit"));

                            Button btnConfirmEdit = new Button("Confirm");
                            btnConfirmEdit.setOnAction(event -> {
                                Rate rate = (Rate) getTableRow().getItem();
                                if (!rate.getObservation().equals(textArea.getText())) {
                                    rate.setObservation(textArea.getText());
                                    rateDBHelper.update(rate);
                                    tableViewDetail.refresh();
                                    popOver.hide();
                                }
                            });

                            vBoxObservation.getChildren().addAll(textArea, btnConfirmEdit);

                            popOver.setContentNode(vBoxObservation);
                            popOver.show((TableCell) e.getTarget());
                            popOver.setAutoHide(true);
                            popOver.setHeaderAlwaysVisible(true);
                            popOver.setDetachable(true);
                            popOver.setAnimated(true);
                            popOver.setTitle(((Rate) getTableRow().getItem()).getAmount().toString());
                        });
                    } else {
                        setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
                                VBox vBoxObservation = new VBox(5);
                                vBoxObservation.setAlignment(Pos.CENTER_RIGHT);
                                vBoxObservation.setPadding(new Insets(10));

                                TextArea textArea = new TextArea();
                                textArea.setWrapText(true);
                                textArea.setEditable(false);
                                textArea.setOnMouseClicked(e -> {
                                    if (e.getClickCount() == 2) {
                                        textArea.setEditable(true);
                                        textArea.setTooltip(null);
                                    }
                                });

                                textArea.setTooltip(new Tooltip("Double click to edit"));

                                Button btnConfirmEdit = new Button("Confirm");
                                btnConfirmEdit.setOnAction(e -> {
                                    Rate rate = (Rate) getTableRow().getItem();
                                    rate.setObservation(textArea.getText());
                                    rateDBHelper.update(rate);
                                    tableViewDetail.refresh();
                                    popOver.hide();
                                });
                                btnConfirmEdit.disableProperty().bind(textArea.textProperty().isEmpty());

                                vBoxObservation.getChildren().addAll(textArea, btnConfirmEdit);

                                popOver.setContentNode(vBoxObservation);
                                popOver.show((TableCell) event.getTarget());
                                popOver.setAutoHide(true);
                                popOver.setHeaderAlwaysVisible(true);
                                popOver.setDetached(true);
                                popOver.setAnimated(true);
                                popOver.setTitle(((Rate) getTableRow().getItem()).getAmount().toString());
                            }
                        });
                    }
                }


            };
            return cell;
        });

        // split columns widths to match table
        Binding preferredWidth = tableViewDetail.widthProperty().divide(4);
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
        ObservableList<Rate> listRates = FXCollections.observableArrayList(new RateDBHelper().fetchAll());
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
                        new ExpenseDBHelper().delete(e);
                        tableViewMaster.getItems().remove(e);
                        tableViewMaster.refresh();

                        Notification.create("Deleted expense:\n" + e.getTitle(),
                                "Success",
                                null);
                    }
                });

    }

    private ObservableList<Expense> getAllExpenses() {
        ObservableList<Expense> list = FXCollections.observableArrayList(new ExpenseDBHelper().fetchAll());
        return list;
    }

    private void displayDeleteRateConfirmation(Rate rate) {
        DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.setCallerPane(rootBorderPane)
                .setHeader("Are you sure you want to delete " + rate.getAmount() + " rate ?")
                .show()
                .ifPresent(response -> {
                    if ((ButtonType) response == dialogBuilder.getConfirmAction()) {
                        new RateDBHelper().delete(rate);
                        tableViewDetail.getItems().remove(rate);
                        tableViewDetail.refresh();

                        Notification.create("Deleted rate:\n" + rate.getAmount(),
                                "Success",
                                null);
                    }
                });
    }

    @FXML
    public void addExpense() {
        DialogBuilder dialogBuilder = new DialogBuilder();
        ChoiceBox<Category> choiceBoxCat = new ChoiceBox<>(FXCollections.observableArrayList(new CategoryDBHelper().fetchAll()));
        CheckComboBox<Tag> checkComboBoxTag = new CheckComboBox<>(FXCollections.observableArrayList(new TagDBHelper().fetchAll()));
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
                                description = ((TextArea) dialogBuilder.getControl("description")).getText(),
                                dueDate = ((DatePicker) dialogBuilder.getControl("dueDate")).getEditor().getText(),
                                amount = ((TextField) dialogBuilder.getControl("amount")).getText();
                        boolean isRecurrent = ((CheckBox) dialogBuilder.getControl("recurrent")).isSelected();
                        Category category = ((ChoiceBox<Category>) dialogBuilder.getControl("category")).getValue();
                        List<Tag> tags = ((CheckComboBox<Tag>) dialogBuilder.getControl("tags")).getCheckModel().getCheckedItems();

                        Expense expense = new Expense(
                                title,
                                description,
                                isRecurrent,
                                new Date(dueDate),
                                Double.parseDouble(amount),
                                category
                        );

                        expense.getTags().addAll(tags);

                        new ExpenseDBHelper().save(expense);
                        tableViewMaster.getItems().add(expense);
                        tableViewMaster.refresh();
                        Notification.create("Added Expense:\n" + expense.getTitle(),
                                "Success",
                                null);
                    }
                });

    }
}
