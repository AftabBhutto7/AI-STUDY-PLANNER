import javax.swing.*;

/**
 * Entry point of the AI Study Planner application.
 */
public class Main {
    public static void main(String[] args) {
        // Set system look-and-feel so native fonts render well
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Launch on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}