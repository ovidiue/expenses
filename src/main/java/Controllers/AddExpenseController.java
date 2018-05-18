package Controllers;

import helpers.HibernateHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Category;
import model.Expense;

import javax.xml.soap.Text;
import java.util.Date;

/**
 * Created by Ovidiu on 18-May-18.
 */
public class AddExpenseController {
    @FXML
    TextField title;
    @FXML
    TextArea description;
    @FXML
    TextField amount;
    @FXML
    DatePicker dueDate;
    @FXML
    ChoiceBox<Category> category;
    @FXML
    CheckBox recurrent;

    private Expense getExpense() {
        return new Expense(title.getText(),
                description.getText(),
                recurrent.isSelected(),
                new Date(dueDate.getEditor().getText()),
                Double.parseDouble(amount.getText()));
    }

    public void saveExpense() {
        Expense e = getExpense();
        HibernateHelper.save(e);
        clearFieldSelections();
    }

    private void clearFieldSelections() {
        title.setText("");
        description.setText("");
        amount.setText("");
        dueDate.getEditor().setText("");
        recurrent.setSelected(false);
    }
}
