package Controllers;

import helpers.ExpenseDBHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
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
        TableColumn<Expense, String> titleCol,
                descriptionCol;

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
                    if (empty) {
                        setText(null);
                    } else {
                        this.setText(format.format(item));
                    }
                }
            };

            return cell;
        });


        titleCol.setCellValueFactory(new PropertyValueFactory<Expense, String>("title"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<Expense, String>("description"));
        recurrentCol.setCellValueFactory(new PropertyValueFactory<Expense, Boolean>("recurrent"));
        createdOnCol.setCellValueFactory(new PropertyValueFactory<Expense, Date>("createdOn"));
        dueDateCol.setCellValueFactory(new PropertyValueFactory<Expense, Date>("dueDate"));
        amountCol.setCellValueFactory(new PropertyValueFactory<Expense, Double>("amount"));

        table.getColumns().addAll(titleCol,
                descriptionCol,
                recurrentCol,
                createdOnCol,
                dueDateCol,
                amountCol);

        table.setItems(getAllExpenses());
    }

    private ObservableList<Expense> getAllExpenses() {
        ObservableList<Expense> list = FXCollections.observableArrayList(new ExpenseDBHelper().fetchAll());
        return list;
    }
}
