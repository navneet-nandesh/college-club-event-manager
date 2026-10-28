package clubevent;

import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 1. Traverse and display everything from MySQL
        DBManager.traverseAll();

        // 2. Launch GUI if graphical environment is available
        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        } else {
            System.out.println("Console mode: Traversal complete.");
        }
    }
}
