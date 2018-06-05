package helpers.ui;

import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;

/**
 * Created by Ovidiu on 05-Jun-18.
 */
public class ControlEffect {

    public static void setBlur(Pane pane, boolean set) {
        BoxBlur bb = new BoxBlur();
        if (set == true) {
            bb.setWidth(5);
            bb.setHeight(5);
            bb.setIterations(3);
            pane.setEffect(bb);
        } else {
            pane.setEffect(null);
        }

    }

}
