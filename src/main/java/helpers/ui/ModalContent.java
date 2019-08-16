package helpers.ui;

import com.jfoenix.controls.JFXDialogLayout;

/**
 * Created by Ovidiu on 16-Aug-19.
 */
public interface ModalContent<T> {

  T getResult();

  JFXDialogLayout getLayout();
}
