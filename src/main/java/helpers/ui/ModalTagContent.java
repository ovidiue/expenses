package helpers.ui;

import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import model.Tag;

/**
 * Created by Ovidiu on 16-Aug-19.
 */
@Slf4j
public class ModalTagContent implements ModalContent {

  JFXTextField title = new JFXTextField();
  JFXColorPicker color = new JFXColorPicker();
  RequiredFieldValidator validator = new RequiredFieldValidator();

  public ModalTagContent() {
    title.setPromptText("Title");
    color.setPromptText("Color");

    title.setLabelFloat(true);
  }

  @Override
  public Tag getResult() {
    if (!title.validate()) {
      return null;
    }
    String colorValue = this.color.getValue().toString().replace("0x", "#");
    return new Tag(title.getText().trim(), colorValue);
  }

  @Override
  public JFXDialogLayout getLayout() {
    JFXDialogLayout layout = new JFXDialogLayout();

    title.getValidators().add(validator);
    validator.setMessage("Title is mandatory!");

    VBox bodyContent = new VBox(20);
    bodyContent.getChildren().addAll(title, color);

    layout.setHeading(new Label("Add Category"));
    layout.setBody(bodyContent);

    return layout;
  }

}
