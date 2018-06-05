package Controllers;

import helpers.TagDBHelper;
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
import model.Tag;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 20-May-18.
 */
public class AllTagsController implements Initializable {
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
            System.out.println(t.toString());
            t.setName(value);
            new TagDBHelper().update(t);
            table.refresh();
        });

        table.getColumns().addAll(nameCol,
                colorCol,
                idCol,
                deleteCol);

        table.setItems(getAllTags());
    }

    private ObservableList<Tag> getAllTags() {
        ObservableList<Tag> list = FXCollections.observableArrayList(new TagDBHelper().fetchAll());
        return list;
    }

    public void displayAddTagDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add new tag");
        dialog.setHeaderText("Enter at least title in order to add a new tag");

        dialog.setOnShowing(event -> ControlEffect.setBlur(rootBorderPane, true));
        dialog.setOnCloseRequest(event -> ControlEffect.setBlur(rootBorderPane, false));

        ButtonType confirmBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.setMaxWidth(Double.MAX_VALUE);

        TextField tagName = new TextField();
        tagName.setPromptText("name");
        tagName.setMaxWidth(Double.MAX_VALUE);
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setMaxWidth(Double.MAX_VALUE);
        colorPicker.setPromptText("choose color");

        grid.add(new Label("Name *:"), 0, 0);
        grid.add(tagName, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorPicker, 1, 1);

        Node saveBtn = dialog.getDialogPane().lookupButton(confirmBtn);
        saveBtn.setDisable(true);

        tagName.textProperty().addListener((observable, oldValue, newValue) -> {
            saveBtn.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> tagName.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmBtn) {
                return new Pair<String, String>(tagName.getText(), colorPicker.getValue().toString());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            String color = extractRGB(colorPicker.getValue());
            System.out.println(color);

            Tag tag = new Tag(tagName.getText(), color);
            new TagDBHelper().save(tag);
            table.getItems().add(tag);
            table.refresh();

            Notification.create("Added new tag:\n" + tag.getName(),
                    "Success",
                    null);

        });

    }

    private String extractRGB(Color color) {
        int red,
                green,
                blue;

        double opacity;

        String result;

        red = (int) (color.getRed() * 255);
        blue = (int) (color.getBlue() * 255);
        green = (int) (color.getGreen() * 255);
        //opacity = color.getOpacity();

        //return "rgb("+red+","+green+","+blue+","+opacity+")";
        return "rgb(" + red + "," + green + "," + blue + ")";
    }

    public void displayDeleteTagConfirmation(Tag tag) {
        ButtonType okBtn,
                cancelBtn;

        okBtn = new ButtonType("Confirm");
        cancelBtn = new ButtonType("Cancel");

        Alert alert = new Alert(Alert.AlertType.WARNING, "asdfasd", cancelBtn, okBtn);
        alert.setTitle("Delete");
        alert.setHeaderText("Delete tag");
        alert.setContentText("Are you sure you want to delete " + tag.getName() + " tag ?");

        alert.setOnShowing(event -> ControlEffect.setBlur(rootBorderPane, true));
        alert.setOnCloseRequest(event -> ControlEffect.setBlur(rootBorderPane, false));

        alert.showAndWait().ifPresent(response -> {
            if (response == okBtn) {
                new TagDBHelper().delete(tag);
                table.getItems().remove(tag);
                table.refresh();

                Notification.create("Deleted tag:\n" + tag.getName(),
                        "Success",
                        null);

            }
        });
    }


}
