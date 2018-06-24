package Controllers;

import helpers.db.ExpenseDBHelper;
import helpers.db.RateDBHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import model.Expense;
import model.Rate;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 21-Jun-18.
 */
public class ReportsController implements Initializable {
    @FXML
    PieChart pieExpenses;
    @FXML
    BarChart<String, Number> barChartRates;
    @FXML
    StackedBarChart<String, Number> stackedBarChart;


    private ExpenseDBHelper expenseDBHelper = new ExpenseDBHelper();
    private RateDBHelper rateDBHelper = new RateDBHelper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpPieChartExpenses();
        setUpBarChartRates();
        setUpStackedBarChart();
    }

    private void setUpPieChartExpenses() {
        ObservableList<Expense> expensesList = FXCollections.observableArrayList(expenseDBHelper.fetchAll());
        ObservableList<PieChart.Data> pieChartExpensesData = FXCollections.observableArrayList();
        for (Expense e : expensesList) {
            pieChartExpensesData.add(new PieChart.Data(e.getTitle(), e.getAmount()));
        }
        pieExpenses.setData(pieChartExpensesData);
        pieExpenses.setAnimated(true);
        pieExpenses.setLabelsVisible(true);
    }

    private void setUpBarChartRates() {
        ObservableList<Expense> expensesList = FXCollections.observableArrayList(expenseDBHelper.fetchAllWithRates());

        for (Expense expense : expensesList) {
            XYChart.Series series = new XYChart.Series();
            series.setName(expense.getTitle());
            if (expense.getPayedRates().size() > 0) {
                for (Rate rate : expense.getPayedRates()) {
                    series.getData().add(new XYChart.Data<>(rate.getAmount().toString(), rate.getAmount()));
                }
                barChartRates.getData().add(series);
            }
        }
        barChartRates.setBarGap(5);

    }

    private void setUpStackedBarChart() {
        ObservableList<Expense> expensesList = FXCollections.observableArrayList(expenseDBHelper.fetchAllWithRates());

        for (Expense expense : expensesList) {
            XYChart.Series series = new XYChart.Series();
            series.setName(expense.getTitle());
            if (expense.getPayedRates().size() > 0) {
                for (Rate rate : expense.getPayedRates()) {
                    XYChart.Data<String, Double> e = new XYChart.Data<>(expense.getTitle(), rate.getAmount());
                    series.getData().add(e);
                }
                stackedBarChart.getData().add(series);
            }
        }
    }
}
