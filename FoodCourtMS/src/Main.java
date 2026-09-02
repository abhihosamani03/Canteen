import javax.swing.*;

/**
 * Main is the entry point of the Food Court Management System application.
 */
public class Main {
    public static void main(String[] args) {
        // Set Nimbus Look & Feel for a modern aesthetic and proper button color contrast
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Nimbus Look and Feel not found. Defaulting to Swing Look and Feel.");
        }

        // Launch the login frame in the Event Dispatch Thread (EDT) for thread-safe UI rendering
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}