package clubevent;

import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new LoginFrame().setVisible(true);
                }
            });
        }
    }
}
