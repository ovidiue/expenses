package Controllers;

import helpers.CategoryDBHelper;
import helpers.ExpenseDBHelper;
import helpers.RateDBHelper;
import helpers.ui.Notification;
import javafx.beans.binding.Binding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Category;
import model.Expense;
import model.Rate;
import org.controlsfx.control.MasterDetailPane;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 18-May-18.
 */
public class AllExpensesController implements Initializable {
    @FXML
    TableView<Expense> table;

    @FXML
    MasterDetailPane masterDetailPane;

    private TableView<Expense> tableViewMaster = getExpenseTable();
    private TableView<Rate> tableViewDetail = getRateTable();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initMasterDetailPane();
        // initTable();
    }

    private void initMasterDetailPane() {
        masterDetailPane.setMasterNode(tableViewMaster);
        masterDetailPane.setDetailNode(tableViewDetail);

        masterDetailPane.getDetailNode().prefHeight(masterDetailPane.getHeight());
        setClickBehaviourMasterDetailsPane();
    }

    private void setClickBehaviourMasterDetailsPane() {
        masterDetailPane.setAnimated(true);
        tableViewMaster.setRowFactory(e -> {
            TableRow<Expense> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Expense expense = row.getItem();
                    if (expense.getPayedRates().size() > 0) {
                        System.out.println("RATES: " + expense.getPayedRates());
                        masterDetailPane.setShowDetailNode(true);
                        updateDetailsPane(FXCollections.observableArrayList(expense.getPayedRates()));
                    } else {
                        masterDetailPane.setShowDetailNode(false);
                    }
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
                        this.setText(format.format(item));
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
        tableViewDetail = new TableView<>();

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

    private void initTable() {
        /*TODO - set display category
        * TODO - show payed rates per expense
        * TODO - show tags*/
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

        titleCol.prefWidthProperty().bind(table.widthProperty().divide(8));
        descriptionCol.prefWidthProperty().bind(table.widthProperty().divide(8));
        recurrentCol.prefWidthProperty().bind(table.widthProperty().divide(8));
        createdOnCol.prefWidthProperty().bind(table.widthProperty().divide(8));
        dueDateCol.prefWidthProperty().bind(table.widthProperty().divide(8));
        amountCol.prefWidthProperty().bind(table.widthProperty().divide(8));
        deleteCol.prefWidthProperty().bind(table.widthProperty().divide(8));
        categoryCol.prefWidthProperty().bind(table.widthProperty().divide(8));

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
                        this.setText(format.format(item));
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

/*
        categoryCol.setCellFactory((TableColumn<Expense, Category> column) -> {
            TableCell<Expense, Category> cell = new TableCell<Expense, Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        ChoiceBox<Category> choiceBoxCategory = new ChoiceBox<>();
                        choiceBoxCategory.setItems(FXCollections.observableList(new CategoryDBHelper().fetchAll()));
                        Expense expense = getTableView().getItems().get(getIndex());

                        choiceBoxCategory.setMaxWidth(Double.MAX_VALUE);


                        Platform.runLater(() -> {
                            Category c = expense.getCategory();
                            choiceBoxCategory.getSelectionModel().select(c);
                            // choiceBoxCategory.setValue(c);
                        });

                        setGraphic(choiceBoxCategory);
                    }
                }
            };
            return cell;
        });
*/

        categoryCol.setCellFactory(ChoiceBoxTableCell.forTableColumn(FXCollections.observableList(new CategoryDBHelper().fetchAll())));

        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        recurrentCol.setCellValueFactory(new PropertyValueFactory<>("recurrent"));
        createdOnCol.setCellValueFactory(new PropertyValueFactory<>("createdOn"));
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        table.getColumns().addAll(titleCol,
                descriptionCol,
                recurrentCol,
                createdOnCol,
                dueDateCol,
                amountCol,
                categoryCol,
                deleteCol);

        table.setItems(getAllExpenses());
        table.setEditable(true);
    }

    private void displayDeleteExpenseConfirmation(Expense e) {
        ButtonType okBtn,
                cancelBtn;

        okBtn = new ButtonType("Confirm");
        cancelBtn = new ButtonType("Cancel");

        Alert alert = new Alert(Alert.AlertType.WARNING, "asdfasd", cancelBtn, okBtn);
        alert.setTitle("Delete");
        alert.setHeaderText("Delete expense");
        alert.setContentText("Are you sure you want to delete " + e.getTitle() + " expense ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == okBtn) {
                new ExpenseDBHelper().delete(e);
                table.getItems().remove(e);
                table.refresh();

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
        ButtonType okBtn,
                cancelBtn;

        okBtn = new ButtonType("Confirm");
        cancelBtn = new ButtonType("Cancel");

        Alert alert = new Alert(Alert.AlertType.WARNING, "asdfasd", cancelBtn, okBtn);
        alert.setTitle("Delete");
        alert.setHeaderText("Delete category");
        alert.setContentText("Are you sure you want to delete " + rate.getAmount() + " rate ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == okBtn) {
                Optional<Expense> optional =  tableViewMaster.getItems().stream().filter(e -> e.getPayedRates().contains(rate)).findFirst();
                Expense expense = optional.get();
                expense.getPayedRates().remove(rate);
                new ExpenseDBHelper().update(expense);

                new RateDBHelper().delete(rate);
                tableViewDetail.getItems().remove(rate);
                tableViewDetail.refresh();

                Notification.create("Deleted category:\n" + rate.getAmount(),
                        "Success",
                        null);

            }
        });
    }
}
