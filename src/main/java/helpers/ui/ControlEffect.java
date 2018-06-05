package helpers.ui;

import javafx.scene.effect.BlurType;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * Created by Ovidiu on 05-Jun-18.
 */
public class ControlEffect {
    private final static BoxBlur boxBlur = new BoxBlur();
    private final static InnerShadow innerShadow = new InnerShadow();

    public static void setBlur(Pane pane, boolean set) {
        if (set == true) {
            pane.setEffect(getEffect());
            pane.setMouseTransparent(true);
        } else {
            pane.setEffect(null);
            pane.setMouseTransparent(false);
        }
    }

    private static Effect getEffect() {
        boxBlur.setWidth(5);
        boxBlur.setHeight(5);
        boxBlur.setIterations(3);
        innerShadow.setBlurType(BlurType.GAUSSIAN);
        innerShadow.setColor(Color.web("#000", 0.8));

        innerShadow.setRadius(50.0);
        innerShadow.setWidth(Double.MAX_VALUE);
        innerShadow.setHeight(Double.MAX_VALUE);

        innerShadow.setRadius(50);
        boxBlur.setInput(innerShadow);

        return boxBlur;
    }

}
