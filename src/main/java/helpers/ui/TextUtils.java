package helpers.ui;


import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;

/**
 * Created by Ovidiu on 03-Jun-18.
 */
public class TextUtils {

    public static ChangeListener<String> getDigitListener() {
        return ((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                TextField textField = (TextField) ((StringProperty) observable).getBean();
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

    }

}
