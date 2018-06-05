package Controllers;

import helpers.CategoryDBHelper;
import helpers.ExpenseDBHelper;
import helpers.RateDBHelper;
import helpers.ui.ControlEffect;
import helpers.ui.Notification;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.Category;
import model.Expense;
import model.Rate;
import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.control.PopOver;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Created by Ovidiu on 18-May-18.
 */
public class AllExpensesController implements Initializable {
    @FXML
    AnchorPane anchorPane;

    BorderPane rootBorderPane;

    @FXML
    MasterDetailPane masterDetailPane;
    private TableView<Rate> tableViewDetail = getRateTable();
    private PopOver popOver = new PopOver();
    private TableView<Expense> tableViewMaster = getExpenseTable();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initMasterDetailPane();
        Platform.runLater(() -> rootBorderPane = (BorderPane) anchorPane.getParent());
    }

    private void initMasterDetailPane() {
        masterDetailPane.setMasterNode(tableViewMaster);
        masterDetailPane.setDetailNode(buildDetailContent());
        masterDetailPane.getDetailNode().minHeight(200);
        masterDetailPane.getDetailNode().prefHeight(500);

        setClickBehaviourMasterDetailsPane();
    }

    private AnchorPane buildDetailContent() {
        AnchorPane anchorPane = new AnchorPane();
        GridPane gridPane = new GridPane();
        Label labelTitle = new Label("Rates");
        Button buttonAdd = new Button("Add");

        gridPane.add(labelTitle, 0, 0);
        gridPane.add(buttonAdd, 1, 0);
        gridPane.setHgrow(labelTitle, Priority.ALWAYS);
        gridPane.setHgrow(buttonAdd, Priority.ALWAYS);
        gridPane.setHalignment(labelTitle, HPos.LEFT);
        gridPane.setHalignment(buttonAdd, HPos.RIGHT);

        anchorPane.getChildren().addAll(gridPane, tableViewDetail);

        labelTitle.setStyle("-fx-font-size: 30");
        buttonAdd.setMinWidth(100);
        buttonAdd.setStyle("-fx-font-weight: bold");

        anchorPane.setTopAnchor(gridPane, 0.0);
        anchorPane.setTopAnchor(tableViewDetail, 50.0);
        anchorPane.setBottomAnchor(tableViewDetail, 0.0);
        anchorPane.setLeftAnchor(tableViewDetail, 0.0);
        anchorPane.setLeftAnchor(gridPane, 0.0);
        anchorPane.setRightAnchor(gridPane, 0.0);
        anchorPane.setRightAnchor(tableViewDetail, 0.0);

        return anchorPane;
    }

    private void setClickBehaviourMasterDetailsPane() {
        masterDetailPane.setAnimated(true);
        tableViewMaster.setRowFactory(e -> {
            TableRow<Expense> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Expense expense = row.getItem();
                    masterDetailPane.setShowDetailNode(true);
                    ObservableList<Rate> ratesList = FXCollections.observableArrayList(new RateDBHelper().fetchAll(expense));
                    updateDetailsPane(ratesList);
                }
            });
            return row;
        });
    }

    private void updateDetailsPane(ObservableList<Rate> rates) {
        tableViewDetail.setItems(rates);
        tableViewDetail.refresh();
    }

    private TableView<Expense> getExpenseTable() {
        tableViewMaster = new TableView<>();

        TableColumn<Expense, String> titleCol,
                descriptionCol,
                deleteCol;

        TableColumn<Expense, Category> categoryCol;

        TableColumn<Expense, Date> createdOnCol,
                dueDateCol;

        TableColumn<Expense, Double> amountCol;
        TableColumn<Expense, Boolean> recurrentCol;

        titleCol = new TableColumn<>("Title");
        descriptionCol = new TableColumn<>("Description");
        recurrentCol = new TableColumn<>("Is recurrent");
        createdOnCol = new TableColumn<>("Created on");
        dueDateCol = new TableColumn<>("Due date");
        amountCol = new TableColumn<>("Amount");
        deleteCol = new TableColumn<>("Delete");
        categoryCol = new TableColumn<>("Category");

        descriptionCol.setCellFactory((TableColumn<Expense, String> column) -> new TableCell<Expense, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && item.trim().length() > 0) {
                    setText(item.substring(0, item.length() > 15 ? 15 : item.length()));
                    setOnMouseEntered(e -> {
                        TextArea textArea = new TextArea();
                        textArea.setWrapText(true);
                        textArea.setEditable(false);
                        textArea.setText(item);
                        popOver.setContentNode(textArea);
                        popOver.show((TableCell) e.getTarget());
                        popOver.setAutoHide(true);
                        popOver.setHeaderAlwaysVisible(true);
                        popOver.setDetachable(true);
                        popOver.setAnimated(true);
                        popOver.setTitle(((Expense) getTableRow().getItem()).getTitle());
                    });
                }
            }
        });

        titleCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        descriptionCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        recurrentCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        createdOnCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        dueDateCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        amountCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        deleteCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));
        categoryCol.prefWidthProperty().bind(tableViewMaster.widthProperty().divide(8));

        createdOnCol.setCellFactory(column -> {
            TableCell<Expense, Date> cell = new TableCell<Expense, Date>() {
                private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        this.setText(format.format(item));
                        this.setTooltip(new Tooltip("Time\n" + item.getHours() + ":" + item.getMinutes()));
                    }
                }
            };

            return cell;
        });

        dueDateCol.setCellFactory(column -> {
            TableCell<Expense, Date> cell = new TableCell<Expense, Date>() {
                private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        this.setText(format.format(item));
                    }
                }
            };

            return cell;
        });

        deleteCol.setCellFactory((TableColumn<Expense, String> column) -> {
            TableCell<Expense, String> cell = new TableCell<Expense, String>() {
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
                            Expense expense = getTableView().getItems().get(getIndex());
                            displayDeleteExpenseConfirmation(expense);
                        });

                        setGraphic(deleteBtn);
                    }
                }
            };
            return cell;
        });


        categoryCol.setCellFactory(ChoiceBoxTableCell.forTableColumn(FXCollections.observableList(new CategoryDBHelper().fetchAll())));

        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        recurrentCol.setCellValueFactory(new PropertyValueFactory<>("recurrent"));
        createdOnCol.setCellValueFactory(new PropertyValueFactory<>("createdOn"));
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        tableViewMaster.getColumns().addAll(titleCol,
                descriptionCol,
                recurrentCol,
                createdOnCol,
                dueDateCol,
                amountCol,
                categoryCol,
                deleteCol);

        tableViewMaster.setItems(getAllExpenses());
        tableViewMaster.setEditable(true);

        return tableViewMaster;
    }

    private TableView<Rate> getRateTable() {
        tableViewDetail = new TableView<>();

        // declare columns
        TableColumn<Rate, Double> colRateAmount;
        TableColumn<Rate, Date> colPayedOn;
        TableColumn<Rate, String> colObservation;
        TableColumn<Rate, Button> colDelete;

        // set columns titles
        colRateAmount = new TableColumn<>("Rate amount");
        colPayedOn = new TableColumn<>("Payed on");
        colObservation = new TableColumn<>("Observation");
        colDelete = new TableColumn<>("Delete");

        // set values to display in columns
        colRateAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colPayedOn.setCellValueFactory(new PropertyValueFactory<>("date"));
        colObservation.setCellValueFactory(new PropertyValueFactory<>("observation"));

        // split columns widths to match table
        Binding preferredWidth = tableViewDetail.widthProperty().divide(4);
        colRateAmount.prefWidthProperty().bind(preferredWidth);
        colPayedOn.prefWidthProperty().bind(preferredWidth);
        colObservation.prefWidthProperty().bind(preferredWidth);
        colDelete.prefWidthProperty().bind(preferredWidth);

        colPayedOn.setCellFactory(column -> {
            TableCell<Rate, Date> cell = new TableCell<Rate, Date>() {
                private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        this.setText(format.format(item));
                    }
                }
            };

            return cell;
        });

        colDelete.setCellFactory(column -> {
            TableCell<Rate, Button> cell = new TableCell<Rate, Button>() {

                @Override
                protected void updateItem(Button item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        final Button DELETE_BTN = new Button("delete");
                        DELETE_BTN.setMaxWidth(Double.MAX_VALUE);
                        DELETE_BTN.setOnAction(e -> {
                            displayDeleteRateConfirmation((Rate) getTableRow().getItem());
                        });

                        setGraphic(DELETE_BTN);
                    }
                }
            };

            return cell;
        });


        // get table data
        ObservableList<Rate> listRates = FXCollections.observableArrayList(new RateDBHelper().fetchAll());
        tableViewDetail.setItems(listRates);

        // add all columns
        tableViewDetail.getColumns().addAll(colRateAmount, colPayedOn, colObservation, colDelete);

        return tableViewDetail;
    }

    private void displayDeleteExpenseConfirmation(Expense e) {
        ButtonType okBtn,
                cancelBtn;

        okBtn = new ButtonType("Confirm");
        cancelBtn = new ButtonType("Cancel");

        Alert alert = new Alert(Alert.AlertType.WARNING, "asdfasd", cancelBtn, okBtn);
        alert.setTitle("Delete");
        alert.setHeaderText("Delete expense");
        alert.setContentText("Are you sure you want to delete " + e.getTitle() + " expense ?");

        alert.setOnShowing(event -> ControlEffect.setBlur(rootBorderPane, true));
        alert.setOnCloseRequest(event -> ControlEffect.setBlur(rootBorderPane, false));

        alert.showAndWait().ifPresent(response -> {
            if (response == okBtn) {
                new ExpenseDBHelper().delete(e);
                tableViewMaster.getItems().remove(e);
                tableViewMaster.refresh();

                Notification.create("Deleted expense:\n" + e.getTitle(),
                        "Success",
                        null);

            }
        });

    }

    private ObservableList<Expense> getAllExpenses() {
        ObservableList<Expense> list = FXCollections.observableArrayList(new ExpenseDBHelper().fetchAll());
        return list;
    }

    private void displayDeleteRateConfirmation(Rate rate) {
        ButtonType okBtn,
                cancelBtn;

        okBtn = new ButtonType("Confirm");
        cancelBtn = new ButtonType("Cancel");

        Alert alert = new Alert(Alert.AlertType.WARNING, "asdfasd", cancelBtn, okBtn);
        alert.setTitle("Delete");
        alert.setHeaderText("Delete rate");
        alert.setContentText("Are you sure you want to delete " + rate.getAmount() + " rate ?");

        alert.setOnShowing(event -> ControlEffect.setBlur(rootBorderPane, true));
        alert.setOnCloseRequest(event -> ControlEffect.setBlur(rootBorderPane, false));

        alert.showAndWait().ifPresent(response -> {
            if (response == okBtn) {

                new RateDBHelper().delete(rate);
                tableViewDetail.getItems().remove(rate);
                tableViewDetail.refresh();

                Notification.create("Deleted rate:\n" + rate.getAmount(),
                        "Success",
                        null);

            }
        });
    }
}
