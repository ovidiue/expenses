package Controllers;

import helpers.db.CategoryDBHelper;
import helpers.ui.DialogBuilder;
import helpers.ui.Notification;
import helpers.ui.View;
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
import model.Category;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 19-May-18.
 */
public class AllCategoriesController implements Initializable {
    @FXML
    TableView<Category> table;
    @FXML
    AnchorPane anchorPane;

    private BorderPane rootBorderPane;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTable();
        Platform.runLater(() -> rootBorderPane = (BorderPane) anchorPane.getParent());
    }

    private void initTable() {
        TableColumn<Category, String>
                nameCol,
                descriptionCol,
                colorCol;

        TableColumn<Category, Integer> idCol;
        TableColumn<Category, String> deleteCol;

        nameCol = new TableColumn<>("Name");
        descriptionCol = new TableColumn<>("Description");
        colorCol = new TableColumn<>("Color");
        idCol = new TableColumn<>("id");
        deleteCol = new TableColumn<>("Delete");

        idCol.setVisible(false);

        table.setEditable(true);
        table.getSelectionModel().cellSelectionEnabledProperty().set(true);
        table.setMaxHeight(Double.MAX_VALUE);

        nameCol.prefWidthProperty().bind(table.widthProperty().divide(3.4));
        descriptionCol.prefWidthProperty().bind(table.widthProperty().divide(3.4));
        colorCol.prefWidthProperty().bind(table.widthProperty().divide(3.4));
        deleteCol.prefWidthProperty().bind(table.widthProperty().divide(8));

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));

        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionCol.setCellFactory(TextFieldTableCell.forTableColumn());

        table.getColumns().addAll(nameCol,
                descriptionCol,
                colorCol,
                idCol,
                deleteCol);

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
                            this.setStyle(getColorCellDefaultStyle(newColor));

                            Category t = table.getSelectionModel().getSelectedItem();
                            System.out.println(t.toString());
                            t.setColor(newColor);
                            new CategoryDBHelper().update(t);

                        });
                        cp.setVisible(false);
                        this.setStyle(getColorCellDefaultStyle(item));
                        this.setOnMouseClicked(e -> {
                            cp.show();
                        });
                        setGraphic(cp);

                    }

                }
            };
            return cell;
        });

        deleteCol.setCellFactory((TableColumn<Category, String> column) -> {
            TableCell<Category, String> cell = new TableCell<Category, String>() {
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
                            Category t = getTableView().getItems().get(getIndex());
                            displayDeleteCatConfirmation(t);
                        });

                        setGraphic(deleteBtn);
                    }
                }
            };
            return cell;
        });


        nameCol.setOnEditCommit(event -> {
            final String value = event.getNewValue() != null ?
                    event.getNewValue() :
                    event.getOldValue();

            Category t = table.getSelectionModel().getSelectedItem();
            System.out.println(t.toString());
            t.setName(value);
            new CategoryDBHelper().update(t);
            table.refresh();
        });

        descriptionCol.setOnEditCommit(event -> {
            final String value = event.getNewValue() != null ?
                    event.getNewValue() :
                    event.getOldValue();

            Category t = table.getSelectionModel().getSelectedItem();
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

    private String getColorCellDefaultStyle(String color) {
        return ("-fx-background-color: " + color + ";" +
                "-fx-border-color: gray;" +
                "-fx-border-radius:3px;" +
                "-fx-padding: 1px 1px 1px 1px");
    }

    public void displayAddCategoryDialog() throws IOException {

        final String ADD_CATEGORY_SCREEN = "/fxml_views/add_category.fxml";
        View.loadScreen(ADD_CATEGORY_SCREEN);

      /*  DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.setTitle("Add new category")
                .setHeader("Enter at least title in order to add a new category")
                .addFormField("Title *:", "title", new TextField(), true)
                .addFormField("Description :", "description", new TextArea(), false)
                .addFormField("Color :", "color", new ColorPicker(), false)
                .setCallerPane(rootBorderPane);

        dialogBuilder.show().ifPresent(response -> {
            ButtonType confirm = dialogBuilder.getConfirmAction();

            if (response == confirm) {
                String name = ((TextField) dialogBuilder.getControl("title")).getText().trim();
                String description = ((TextArea) dialogBuilder.getControl("description")).getText().trim();
                String color = ((ColorPicker) dialogBuilder.getControl("color")).getValue().toString().replace("0x", "#");

                if (categoryExists(name)) {
                    System.out.println("ALREADY EXISTS");
                    return;
                }

                Category category = new Category(name, description, color);
                new CategoryDBHelper().save(category);
                table.getItems().add(category);
                table.refresh();

                Notification.create("Added new category:\n" + category.getName(),
                        "Success",
                        null);
            }
        });*/
    }

    private boolean categoryExists(String catName) {
        return new CategoryDBHelper().nameExists(catName);
    }

    public void displayDeleteCatConfirmation(Category category) {

        DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.setTitle("Delete")
                .setHeader("Are you sure you want to delete " + category.getName() + " category ?")
                .setCallerPane(rootBorderPane);

        dialogBuilder.show().ifPresent(response -> {
            if (response == dialogBuilder.getConfirmAction()) {
                new CategoryDBHelper().delete(category);
                table.getItems().remove(category);
                table.refresh();

                Notification.create("Deleted category:\n" + category.getName(),
                        "Success",
                        null);
            }
        });

    }
}
