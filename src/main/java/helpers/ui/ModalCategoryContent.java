package helpers.ui;

import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import model.Category;

/**
 * Created by Ovidiu on 16-Aug-19.
 */
@Slf4j
public class ModalCategoryContent implements ModalContent {

  JFXTextField title = new JFXTextField();
  JFXTextArea description = new JFXTextArea();
  JFXColorPicker color = new JFXColorPicker();
  RequiredFieldValidator validator = new RequiredFieldValidator();

  public ModalCategoryContent() {
    title.setPromptText("Title");
    description.setPromptText("Description");
    color.setPromptText("Color");

    title.setLabelFloat(true);
    description.setLabelFloat(true);
  }

  @Override
  public Category getResult() {
    log.info("GET CONTENT CATEGORY CALLED");
    if (!title.validate()) {
      return null;
    }
    log.info("title {}", title.getText());
    log.info("description {}", description.getText());
    log.info("color {}", color.getValue());
    String colorValue = this.color.getValue().toString().replace("0x", "#");
    return new Category(title.getText().trim(), description.getText().trim(), colorValue);
  }

  @Override
  public JFXDialogLayout getLayout() {
    JFXDialogLayout layout = new JFXDialogLayout();

    title.getValidators().add(validator);
    validator.setMessage("Title is mandatory!");

    VBox bodyContent = new VBox(20);
    bodyContent.getChildren().addAll(title, description, color);

    layout.setHeading(new Label("Add Category"));
    layout.setBody(bodyContent);

    return layout;
  }

}
