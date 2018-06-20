package Controllers;

import helpers.db.ExpenseDBHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import model.Expense;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 21-Jun-18.
 */
public class ReportsController implements Initializable {
    @FXML
    PieChart pieExpenses;

    private ExpenseDBHelper expenseDBHelper = new ExpenseDBHelper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpPieChartExpenses();
    }

    private void setUpPieChartExpenses() {
        ObservableList<Expense> expensesList = FXCollections.observableArrayList(expenseDBHelper.fetchAll());
        ObservableList<PieChart.Data> pieChartExpensesData = FXCollections.observableArrayList();
        for (Expense e : expensesList) {
            pieChartExpensesData.add(new PieChart.Data(e.getTitle(), e.getAmount()));
        }
        pieExpenses.setData(pieChartExpensesData);
    }
}
