package Controllers;

import helpers.CategoryDBHelper;
import helpers.ui.ControlEffect;
import helpers.ui.Notification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import model.Category;

import java.net.URL;
import java.util.Optional;
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

    private String getColorCellDefaultStyle(String color) {
        return ("-fx-background-color: " + color + ";" +
                "-fx-border-color: gray;" +
                "-fx-border-radius:3px;" +
                "-fx-padding: 1px 1px 1px 1px");
    }

    public void displayAddCategoryDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add new category");
        dialog.setHeaderText("Enter at least title in order to add a new category");

        // Set the button types.
        ButtonType confirmBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField categoryTitle = new TextField();
        categoryTitle.setPromptText("title");
        TextArea catDescription = new TextArea();
        catDescription.setPromptText("description");
        ColorPicker catColor = new ColorPicker();

        catColor.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("Title *:"), 0, 0);
        grid.add(categoryTitle, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(catDescription, 1, 1);
        grid.add(new Label("Color:"), 0, 2);
        grid.add(catColor, 1, 2);

        dialog.setOnShowing(event -> ControlEffect.setBlur(rootBorderPane, true));
        dialog.setOnCloseRequest(event -> ControlEffect.setBlur(rootBorderPane, false));

        // Enable/Disable login button depending on whether a title was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(confirmBtn);
        loginButton.setDisable(true);

        categoryTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the title field by default.
        Platform.runLater(() -> categoryTitle.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmBtn) {
                return new Pair<>(categoryTitle.getText(), catDescription.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
            CategoryDBHelper categoryDBHelper = new CategoryDBHelper();
            Category category = new Category(categoryTitle.getText(),
                    catDescription.getText(),
                    catColor.getValue().toString().replace("0x", "#"));
            categoryDBHelper.save(category);
            table.getItems().add(category);
            table.refresh();

            Notification.create("Added new category:\n" + category.getName(),
                    "Success",
                    null);
        });
    }

    public void displayDeleteCatConfirmation(Category category) {
        ButtonType okBtn,
                cancelBtn;

        okBtn = new ButtonType("Confirm");
        cancelBtn = new ButtonType("Cancel");

        Alert alert = new Alert(Alert.AlertType.WARNING, "asdfasd", cancelBtn, okBtn);
        alert.setTitle("Delete");
        alert.setHeaderText("Delete category");
        alert.setContentText("Are you sure you want to delete " + category.getName() + " category ?");

        alert.setOnShowing(event -> ControlEffect.setBlur(rootBorderPane, true));
        alert.setOnCloseRequest(event -> ControlEffect.setBlur(rootBorderPane, false));

        alert.showAndWait().ifPresent(response -> {
            if (response == okBtn) {
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
