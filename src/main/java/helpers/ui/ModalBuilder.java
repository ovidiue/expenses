package helpers.ui;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import java.util.Optional;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Ovidiu on 16-Aug-19.
 */
@Slf4j
public class ModalBuilder {

  private JFXAlert dialog;
  private ModalContent modalContent;

  public ModalBuilder(ModalContent content, Stage stage) {
    this.modalContent = content;

    final JFXButton cancelButton = new JFXButton("Cancel");
    final JFXButton saveButton = new JFXButton("Save");

    cancelButton.setOnAction(event -> dialog.close());

    dialog = new JFXAlert(stage);
    JFXDialogLayout layout = content.getLayout();
    layout.setActions(cancelButton, saveButton);
    dialog.setContent(layout);
    saveButton.setOnAction(event -> {
      Object result = content.getResult();
      if (result != null) {
        log.info("ENTERED DATA {}", result);
        dialog.close();
      }
    });
  }

  public Optional show() {
    dialog.showAndWait();
    return Optional.of(modalContent.getResult());
  }

}
