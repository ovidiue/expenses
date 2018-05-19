package Controllers;

import helpers.ExpenseDBHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Expense;

import java.net.URL;
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
        TableColumn<Expense, String> titleCol = new TableColumn<>("Title");
        TableColumn<Expense, String> descriptionCol = new TableColumn<>("Description");
        TableColumn<Expense, Boolean> recurrentCol = new TableColumn<>("Is recurrent");
        TableColumn<Expense, Date> createdOnCol = new TableColumn<>("Created on");
        TableColumn<Expense, Date> dueDateCol = new TableColumn<>("Due date");
        TableColumn<Expense, Double> amountCol = new TableColumn<>("Amount");

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
