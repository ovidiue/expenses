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
import javafx.scene.control.cell.TextFieldTableCell;
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
        TableColumn<Tag, String> nameCol,
                colorCol;
        TableColumn<Tag, Integer> idCol;

        nameCol = new TableColumn<>("Name");
        colorCol = new TableColumn<>("Color");
        idCol = new TableColumn<>("id");

        idCol.setVisible(false);

        table.setEditable(true);
        table.getSelectionModel().cellSelectionEnabledProperty().set(true);

        table.setMaxHeight(Double.MAX_VALUE);

        nameCol.prefWidthProperty().bind(table.widthProperty().divide(2));
        colorCol.prefWidthProperty().bind(table.widthProperty().divide(2));

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));

        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());

        colorCol.setCellFactory((TableColumn<Tag, String> column) -> {
            TableCell<Tag, String> cell = new TableCell<Tag, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty)
                        setText(null);
                    else {
                        ColorPicker cp = new ColorPicker(Color.web(item));
                        cp.setOnAction(e -> {
                            String newColor = cp.getValue()
                                    .toString()
                                    .replace("0x", "#");
                            this.setStyle("-fx-background-color: " + newColor + ";" +
                                    "-fx-border-color:grey;" +
                                    "-fx-border-radius:3px;" +
                                    "-fx-padding: 1px 1px 1px 1px");

                            Tag t = table.getSelectionModel().getSelectedItem();
                            System.out.println(t.toString());
                            t.setColor(newColor);
                            new TagDBHelper().update(t);

                        });
                        cp.setVisible(false);
                        this.setStyle("-fx-background-color: " + item + ";" +
                                "-fx-border-color: gray;" +
                                "-fx-border-radius:3px;" +
                                "-fx-padding: 1px 1px 1px 1px");
                        this.setOnMouseClicked(e -> {
                            cp.show();
                        });
                        setGraphic(cp);
                    }

                }
            };
            return cell;
        });

        nameCol.setOnEditCommit(event -> {
            final String value = event.getNewValue() != null ? event.getNewValue() :
                    event.getOldValue();
            (event.getTableView().getItems()
                    .get(event.getTablePosition().getRow()))
                    .setName(value);
            Tag t = table.getSelectionModel().getSelectedItem();
            System.out.println(t.toString());
            t.setName(value);
            new TagDBHelper().update(t);
            table.refresh();
        });

        table.getColumns().addAll(nameCol,
                colorCol,
                idCol);

        table.setItems(getAllTags());
    }

    private ObservableList<Tag> getAllTags() {
        ObservableList<Tag> list = FXCollections.observableArrayList(new TagDBHelper().fetchAll());
        return list;
    }

}
