package helpers.ui;

import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 * Created by Ovidiu on 29-May-18.
 */
public class Notification {
    private Notification() {
    }

    public static void create(String message, String title, String style) {
        Notifications n = Notifications.create()
                .position(Pos.TOP_CENTER)
                .text(message)
                .title(title)
                .hideAfter(Duration.seconds(2));

        if (style != null && style.equalsIgnoreCase("dark"))
            n.darkStyle();

        n.owner(null);
        n.show();
    }
}
