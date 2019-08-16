import helpers.repositories.ExpenseRepository;
import helpers.repositories.HibernateHlp;
import helpers.ui.Preloader;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import model.Expense;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;

/**
 * Created by Ovidiu on 15-May-18.
 */
@Slf4j
public class Main extends Application implements Initializable {

  private static final String MAIN_SCREEN = "/fxml_views/main_screen.fxml";
  private static final String EXPENSES_LISTING = "fxml_views/all_expenses.fxml";
  private static final String CATEGORIES_LISTING = "fxml_views/all_categories.fxml";
  private static final String TAGS_LISTING = "fxml_views/all_tags.fxml";
  private static final String REPORTS_PAGE = "fxml_views/reports.fxml";
  private static final String ADD_EXPENSE = "fxml_views/add_expense.fxml";
  private static final String LIGHT_STYLE = "css/style.css";
  private static final String DARK_STYLE = "css/style_dark.css";
  private static final String WHITE_NOTIFICATIONS = "css/custom_notifications_white.css";
  private static final String TRANSLATIONS_FILE = "lang/translations";
  private static final ExpenseRepository EXPENSE_DB_HELPER = new ExpenseRepository();
  @FXML
  BorderPane root;
  @FXML
  RadioMenuItem radioMenuItemLight;
  @FXML
  RadioMenuItem radioMenuItemDark;
  @FXML
  ToggleGroup theme;
  @FXML
  MenuItem radioMenuItemExport;

  FXMLLoader loader;

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {

    primaryStage.setScene(new Scene(new Label("Connecting to DB ..."), 600, 600));
    Preloader preloader = new Preloader();
    Task<Void> taskDB = initializeDBConnection();
    preloader.progressProperty().bind(taskDB.progressProperty());

    taskDB.setOnSucceeded(e -> {
      preloader.hide();

      loader = new FXMLLoader();
      loader.setLocation(getClass().getResource(MAIN_SCREEN));
      loader.setResources(ResourceBundle.getBundle(TRANSLATIONS_FILE));

      try {
        root = loader.load();
      } catch (IOException e1) {
        e1.printStackTrace();
      }

      Scene scene = new Scene(root);
      scene.getStylesheets().add(LIGHT_STYLE);
      scene.getStylesheets().add(WHITE_NOTIFICATIONS);

      primaryStage.setScene(scene);
      primaryStage.setOnCloseRequest(event -> System.exit(1));
      primaryStage.show();
    });

    preloader.show();
    new Thread(taskDB).start();
  }

  private void initMenuActions() {
    initThemeSettings();
    setUpExport();
  }

  private void initThemeSettings() {
    Scene scene = root.getScene();
    radioMenuItemDark.setOnAction(e -> {
      clearStyleSheets();
      scene.getStylesheets().add(DARK_STYLE);
    });

    radioMenuItemLight.setOnAction(e -> {
      clearStyleSheets();
      scene.getStylesheets().add(LIGHT_STYLE);
    });
  }

  private void setUpExport() {
    radioMenuItemExport.setOnAction(e -> {
      List<Expense> expenseList = EXPENSE_DB_HELPER.fetchAll();
      final String[] columns = {"Title", "Description", "Recurrent", "Created On", "Amount"};

      Workbook workbook = new HSSFWorkbook();
      Sheet sheet = workbook.createSheet("expenses");

      Row header = sheet.createRow(0);

      for (int i = 0; i < columns.length; i++) {
        Cell cell = header.createCell(i);
        cell.setCellValue(columns[i]);
      }

      int rowNum = 1;

      for (Expense expense : expenseList) {
        log.info(expense.getTitle());
        log.info(expense.getDescription());
        log.info(expense.getCreatedOn().toString());
        log.info(expense.getAmount().toString());
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(expense.getTitle());
        row.createCell(1).setCellValue(expense.getDescription());
        row.createCell(2).setCellValue(expense.isRecurrent() == true ? "true" : "false");
        row.createCell(3).setCellValue(expense.getCreatedOn().toString());
        row.createCell(4).setCellValue(expense.getAmount());
      }

      for (int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
      }

      try {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XLSX files",
            "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName("expenses");

        File file = fileChooser.showSaveDialog(root.getScene().getWindow());

        if (file != null) {
          FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath());
          log.info("Export Started");
          workbook.write(fileOutputStream);
          fileOutputStream.close();

          Notifications.
              create().
              title("Export").
              text("Exported Succesfully !")
              .action(new Action("Open file", ez -> {
                if (Desktop.isDesktopSupported()) {
                  log.info("OPENING FILE");
                  try {
                    Desktop.getDesktop().open(file);
                    Button target = (Button) ez.getTarget();
                    target.getScene().getWindow().hide();
                  } catch (IOException e1) {
                    e1.printStackTrace();
                  }
                }
              }))
              .owner(null)
              .position(Pos.BOTTOM_RIGHT)
              .hideAfter(Duration.seconds(3))
              .show();
        }

      } catch (IOException e1) {
        e1.printStackTrace();
      }
    });
  }

  private void clearStyleSheets() {
    root.getScene().getStylesheets().clear();
  }

  public void viewAllExpenses() throws IOException {
    AnchorPane table = loader.load(getClass().getResource(EXPENSES_LISTING));
    root.setCenter(table);
  }

  public void addExpense() throws IOException {
    AnchorPane table = loader.load(getClass().getResource(ADD_EXPENSE));
    root.setCenter(table);
  }

  public void viewAllTags() throws IOException {
    AnchorPane table = loader.load(getClass().getResource(TAGS_LISTING));
    table.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    root.setCenter(table);
  }

  public void viewAllCategories() throws IOException {
    AnchorPane table = loader.load(getClass().getResource(CATEGORIES_LISTING));
    root.setCenter(table);
  }

  @FXML
  public void viewReports() throws IOException {
    AnchorPane table = loader.load(getClass().getResource(REPORTS_PAGE));
    root.setCenter(table);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
        /*HibernateHlp.buildSessionFactory();*/
    Platform.runLater(() -> initMenuActions());
  }

  private Task<Void> initializeDBConnection() {
    return new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        HibernateHlp.buildSessionFactory();
        return null;
      }
    };
  }
}
