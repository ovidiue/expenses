package Controllers;

import helpers.CategoryDBHelper;
import helpers.ExpenseDBHelper;
import helpers.ui.Notification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Category;
import model.Expense;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 18-May-18.
 */
public class AllExpensesController implements Initializable {
    @FXML
    TableView<Expense> table;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTable();
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
}
