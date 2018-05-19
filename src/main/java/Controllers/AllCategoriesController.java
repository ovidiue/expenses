package Controllers;

import helpers.CategoryDBHelper;
import helpers.ExpenseDBHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Category;
import model.Expense;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 19-May-18.
 */
public class AllCategoriesController implements Initializable {
    @FXML
    TableView<Category> table;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTable();
    }

    private void initTable() {
        TableColumn<Category, String> nameCol = new TableColumn<>("Name");
        TableColumn<Category, String> descriptionCol = new TableColumn<>("Description");
        TableColumn<Category, String> colorCol = new TableColumn<>("Color");

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        colorCol.setCellValueFactory(new PropertyValueFactory<>("recurrent"));

        table.getColumns().addAll(nameCol,
                descriptionCol,
                colorCol);

        table.setItems(getAllCategories());
    }

    private ObservableList<Category> getAllCategories() {
        ObservableList<Category> list = FXCollections.observableArrayList(new CategoryDBHelper().fetchAll());
        return list;
    }
}
