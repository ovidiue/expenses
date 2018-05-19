package Controllers;

import helpers.TagDBHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Tag;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 20-May-18.
 */
public class AllTagsController implements Initializable {
    @FXML
    TableView<Tag> table;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTable();
    }

    private void initTable() {
        TableColumn<Tag, String> nameCol = new TableColumn<>("Name");
        TableColumn<Tag, String> colorCol = new TableColumn<>("Color");

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));

        table.getColumns().addAll(nameCol,
                colorCol);

        table.setItems(getAllTags());
    }

    private ObservableList<Tag> getAllTags() {
        ObservableList<Tag> list = FXCollections.observableArrayList(new TagDBHelper().fetchAll());
        return list;
    }
}
