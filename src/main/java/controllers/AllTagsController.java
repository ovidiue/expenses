package controllers;

import helpers.db.TagDBHelper;
import helpers.ui.DialogBuilder;
import helpers.ui.Notification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 20-May-18.
 */
public class AllTagsController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(AllTagsController.class);
    private static final TagDBHelper TAG_DB_HELPER = new TagDBHelper();
    @FXML
    TableView<Tag> table;
    @FXML
    AnchorPane anchorPane;
    private BorderPane rootBorderPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTable();
        Platform.runLater(() -> rootBorderPane = (BorderPane) anchorPane.getParent());
    }

    private void initTable() {
        TableColumn<Tag, String> nameCol,
                colorCol,
                deleteCol;
        TableColumn<Tag, Integer> idCol;

        nameCol = new TableColumn<>("Name");
        colorCol = new TableColumn<>("Color");
        idCol = new TableColumn<>("id");
        deleteCol = new TableColumn<>("Delete");

        idCol.setVisible(false);

        table.setEditable(true);
        table.getSelectionModel().cellSelectionEnabledProperty().set(true);

        table.setMaxHeight(Double.MAX_VALUE);

        nameCol.prefWidthProperty().bind(table.widthProperty().divide(2.4));
        colorCol.prefWidthProperty().bind(table.widthProperty().divide(2.4));
        deleteCol.prefWidthProperty().bind(table.widthProperty().divide(6));

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
                            logger.info(t.toString());
                            t.setColor(newColor);
                            TAG_DB_HELPER.update(t);

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

        deleteCol.setCellFactory((TableColumn<Tag, String> column) -> {
            TableCell<Tag, String> cell = new TableCell<Tag, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        final Button deleteBtn = new Button("delete");
                        deleteBtn.setMaxWidth(Double.MAX_VALUE);
                        deleteBtn.setOnAction(e -> {
                            this.setFocused(true);
                            Tag t = getTableView().getItems().get(getIndex());
                            displayDeleteTagConfirmation(t);
                        });

                        setGraphic(deleteBtn);
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
            logger.info(t.toString());
            t.setName(value);
            TAG_DB_HELPER.update(t);
            table.refresh();
        });

        table.getColumns().addAll(nameCol,
                colorCol,
                idCol,
                deleteCol);

        table.setItems(getAllTags());
    }

    private ObservableList<Tag> getAllTags() {
        ObservableList<Tag> list = FXCollections.observableArrayList(TAG_DB_HELPER.fetchAll());
        return list;
    }


    public void displayAddTagDialog() {
        DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.setTitle("Add new TAG")
                .setHeader("Enter at least title in order to add a new tag")
                .addFormField("Name:", "name", new TextField(), true)
                .addFormField("Color: ", "color", new ColorPicker(), false)
                .setCallerPane(rootBorderPane);

        dialogBuilder.show().ifPresent(response -> {
            ButtonType confirm = dialogBuilder.getConfirmAction();

            if (response == confirm) {

                String name = ((TextField) dialogBuilder.getControl("name")).getText();
                String color = ((ColorPicker) dialogBuilder.getControl("color")).getValue().toString().replace("0x", "#");

                Tag tag = new Tag(name, color);
                TAG_DB_HELPER.save(tag);
                table.getItems().add(tag);
                table.refresh();

                Notification.create("Added new tag:\n" + tag.getName(),
                        "Success",
                        null);
            }
        });
    }


    public void displayDeleteTagConfirmation(Tag tag) {

        DialogBuilder dialogBuilder = new DialogBuilder()
                .setHeader("Are you sure you want to delete " + tag.getName() + " tag ?")
                .setCallerPane(rootBorderPane);

        dialogBuilder.show().ifPresent(response -> {
            if (response == dialogBuilder.getConfirmAction()) {
                TAG_DB_HELPER.delete(tag);
                table.getItems().remove(tag);
                table.refresh();

                Notification.create("Deleted tag:\n" + tag.getName(),
                        "Success",
                        null);
            }
        });
    }


}
