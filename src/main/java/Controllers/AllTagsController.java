package Controllers;

import helpers.TagDBHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
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

        colorCol.setCellFactory((TableColumn<Tag, String> column) -> {
            TableCell<Tag, String> cell = new TableCell<Tag, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty)
                        setText(null);
                    else {
                       /* this.setStyle("-fx-background-color: " + item + ";" +
                                "-fx-border-color: gray;" +
                                "-fx-border-radius:3px;" +
                                "-fx-padding: 1px 1px 1px 1px");*/
                        ColorPicker cp = new ColorPicker(Color.web(item));
                        cp.getStyleClass().add("button");
                        setGraphic(cp);
                    }

                }
            };
            return cell;
        });

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

    private String getColor(String c) {
        String values[] = c.split(",");
        // Color s = new Color(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]) ,1);
        String col = "rgb(" + String.valueOf(Math.ceil(Double.parseDouble(values[0]) * 100)) +
                "," + String.valueOf(Math.ceil(Double.parseDouble(values[1]) * 100)) +
                "," + String.valueOf(Math.ceil(Double.parseDouble(values[2]) * 100)) + ")";
        System.out.println("COLOR: " + col);
        return col;
    }
}
