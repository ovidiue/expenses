package Controllers;

import helpers.CategoryDBHelper;
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
import model.Category;

import java.net.URL;
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
        TableColumn<Category, String>
                nameCol,
                descriptionCol,
                colorCol;

        TableColumn<Category, Integer> idCol;

        nameCol = new TableColumn<>("Name");
        descriptionCol = new TableColumn<>("Description");
        colorCol = new TableColumn<>("Color");
        idCol = new TableColumn<>("id");

        idCol.setVisible(false);

        table.setEditable(true);
        table.getSelectionModel().cellSelectionEnabledProperty().set(true);

        nameCol.prefWidthProperty().bind(table.widthProperty().divide(3));
        descriptionCol.prefWidthProperty().bind(table.widthProperty().divide(3));
        colorCol.prefWidthProperty().bind(table.widthProperty().divide(3));

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));

        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionCol.setCellFactory(TextFieldTableCell.forTableColumn());

        table.getColumns().addAll(nameCol,
                descriptionCol,
                colorCol,
                idCol);

        colorCol.setCellFactory((TableColumn<Category, String> column) -> {
            TableCell<Category, String> cell = new TableCell<Category, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty)
                        setText(null);
                    else {
                        item = item != null ? item : Color.WHITE.toString();
                        ColorPicker cp = new ColorPicker(Color.web(item));
                        cp.setOnAction(e -> {
                            String newColor = cp.getValue()
                                    .toString()
                                    .replace("0x", "#");
                            this.setStyle("-fx-background-color: " + newColor + ";" +
                                    "-fx-border-color:grey;" +
                                    "-fx-border-radius:3px;" +
                                    "-fx-padding: 1px 1px 1px 1px");

                            Category t = table.getSelectionModel().getSelectedItem();
                            System.out.println(t.toString());
                            t.setColor(newColor);
                            new CategoryDBHelper().update(t);

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
            Category t = table.getSelectionModel().getSelectedItem();
            System.out.println(t.toString());
            t.setName(value);
            new CategoryDBHelper().update(t);
            table.refresh();
        });

        descriptionCol.setOnEditCommit(event -> {
            final String value = event.getNewValue() != null ? event.getNewValue() :
                    event.getOldValue();
            (event.getTableView().getItems()
                    .get(event.getTablePosition().getRow()))
                    .setName(value);
            Category t = table.getSelectionModel().getSelectedItem();
            System.out.println(t.toString());
            t.setDescription(value);
            new CategoryDBHelper().update(t);
            table.refresh();
        });

        table.setItems(getAllCategories());
    }

    private ObservableList<Category> getAllCategories() {
        ObservableList<Category> list = FXCollections.observableArrayList(new CategoryDBHelper().fetchAll());
        return list;
    }
}
