import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Main application window.
 * Hosts the navigation sidebar and content panels.
 */
public class MainFrame extends JFrame {

    private Planner       planner;
    private DashboardPanel dashboardPanel;
    private TaskPanel      taskPanel;
    private SchedulePanel  schedulePanel;
    private ProgressPanel  progressPanel;
    private JPanel         contentArea;
    private CardLayout     cardLayout;

    private static final Color BG_DARK  = new Color(10, 10, 22);
    private static final Color SIDEBAR  = new Color(18, 18, 36);
    private static final Color ACCENT   = new Color(99, 179, 237);
    private static final Color ACCENT2  = new Color(154, 117, 235);
    private static final Color TEXT     = new Color(220, 220, 240);
    private static final Color SUBTEXT  = new Color(100, 100, 140);
    private static final Color SEL      = new Color(35, 55, 100);

    public MainFrame() {
        setTitle("AI Study Planner");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        showLogin();
    }

    private void showLogin() {
        getContentPane().removeAll();
        LoginPanel login = new LoginPanel(this);
        getContentPane().add(login);
        revalidate(); repaint();
    }

    /** Called by LoginPanel when login succeeds. */
    public void loadPlanner(User user) {
        planner = new Planner(user);
        // Load saved tasks
        try {
            ArrayList<Task> saved = FileManager.loadTasks(user.getUsername());
            planner.setTasks(saved);
        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load tasks: " + ex.getMessage(),
                    "Warning", JOptionPane.WARNING_MESSAGE);
        }
        buildMainUI();
    }

    private void buildMainUI() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = buildSidebar();

        // Content area with CardLayout
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(new Color(15, 15, 30));

        dashboardPanel = new DashboardPanel(planner);
        taskPanel      = new TaskPanel(planner, this);
        schedulePanel  = new SchedulePanel(planner);
        progressPanel  = new ProgressPanel(planner);

        contentArea.add(dashboardPanel, "DASHBOARD");
        contentArea.add(taskPanel,      "TASKS");
        contentArea.add(schedulePanel,  "SCHEDULE");
        contentArea.add(progressPanel,  "PROGRESS");

        getContentPane().add(sidebar,     BorderLayout.WEST);
        getContentPane().add(contentArea, BorderLayout.CENTER);
        revalidate(); repaint();
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(SIDEBAR);
        side.setPreferredSize(new Dimension(200, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Logo
        JLabel logo = new JLabel("Study AI");
        logo.setIcon(IconUtils.get("book", 20, ACCENT));
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(ACCENT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(new EmptyBorder(0, 8, 4, 0));

        JLabel version = new JLabel("v1.0 — " + planner.getUser().getFullName());
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(SUBTEXT);
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        version.setBorder(new EmptyBorder(0, 8, 16, 0));

        side.add(logo);
        side.add(version);
        side.add(new JSeparator());
        side.add(Box.createVerticalStrut(12));

        // Nav buttons
        String[][] navItems = {
            {"dashboard", "Dashboard",  "DASHBOARD"},
            {"tasks", "Tasks",      "TASKS"},
            {"schedule", "Schedule",   "SCHEDULE"},
            {"progress", "Progress",   "PROGRESS"}
        };

        ButtonGroup group = new ButtonGroup();
        for (String[] item : navItems) {
            JToggleButton btn = navButton(item[1], item[2], item[0]);
            group.add(btn);
            side.add(btn);
            side.add(Box.createVerticalStrut(4));
            if ("DASHBOARD".equals(item[2])) btn.setSelected(true);
        }

        side.add(Box.createVerticalGlue());

        // Logout button
        JButton logout = new JButton("Logout") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(160,50,50) : new Color(120,30,30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(getText());
                int startX = (getWidth() - textW - 22) / 2;
                IconUtils.get("logout", 14, Color.WHITE).paintIcon(this, g2, startX, (getHeight() - 14) / 2);
                g2.drawString(getText(), startX + 22,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        logout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logout.setOpaque(false); logout.setContentAreaFilled(false); logout.setBorderPainted(false);
        logout.setMaximumSize(new Dimension(180, 34));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> showLogin());
        side.add(logout);

        return side;
    }

    private JToggleButton navButton(String text, String card, String iconType) {
        JToggleButton btn = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(SEL);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(0, 6, 3, getHeight() - 12, 3, 3);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(30, 30, 60));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.setColor(isSelected() ? ACCENT : TEXT);
                IconUtils.get(iconType, 16, isSelected() ? ACCENT : TEXT).paintIcon(this, g2, 14, (getHeight() - 16) / 2);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 40,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", isSelected(btn) ? Font.BOLD : Font.PLAIN, 13));
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(180, 38));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> { cardLayout.show(contentArea, card); refreshAll(); });
        return btn;
    }

    private boolean isSelected(JToggleButton b) { return false; }

    /** Refresh all panels (call after any data change). */
    public void refreshAll() {
        if (dashboardPanel != null) dashboardPanel.refresh();
        if (taskPanel      != null) taskPanel.refresh();
        if (schedulePanel  != null) schedulePanel.refresh();
        if (progressPanel  != null) progressPanel.refresh();
    }
}