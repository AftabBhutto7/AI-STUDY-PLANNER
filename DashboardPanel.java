import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Dashboard — overview stats + recommendations.
 */
public class DashboardPanel extends JPanel {

    private Planner planner;
    private JLabel  welcomeLabel, totalLabel, pendingLabel, completedLabel, progressLabel;
    private JTextArea recommendationsArea;
    private JProgressBar progressBar;

    private static final Color BG      = new Color(15, 15, 30);
    private static final Color CARD    = new Color(25, 25, 50);
    private static final Color ACCENT  = new Color(99, 179, 237);
    private static final Color GREEN   = new Color(72, 199, 142);
    private static final Color ORANGE  = new Color(255, 179, 71);
    private static final Color TEXT    = new Color(220, 220, 240);
    private static final Color SUBTEXT = new Color(140, 140, 170);

    public DashboardPanel(Planner planner) {
        this.planner = planner;
        setBackground(BG);
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        // Header
        welcomeLabel = new JLabel("Welcome, " + planner.getUser().getFullName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setForeground(ACCENT);

        JLabel sub = new JLabel("Here's your study overview for today.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(SUBTEXT);

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.add(welcomeLabel);
        header.add(sub);
        add(header, BorderLayout.NORTH);

        // Stats cards row
        totalLabel     = statLabel("0", "Total Tasks",     ACCENT);
        pendingLabel   = statLabel("0", "Pending",         ORANGE);
        completedLabel = statLabel("0", "Completed",       GREEN);
        progressLabel  = statLabel("0%","Progress",        new Color(154, 117, 235));

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statsRow.setOpaque(false);
        statsRow.add(statCard(totalLabel,     "Total Tasks",     ACCENT));
        statsRow.add(statCard(pendingLabel,   "Pending",         ORANGE));
        statsRow.add(statCard(completedLabel, "Completed",       GREEN));
        statsRow.add(statCard(progressLabel,  "Progress",        new Color(154, 117, 235)));

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(GREEN);
        progressBar.setBackground(new Color(35, 35, 60));
        progressBar.setBorder(new EmptyBorder(0, 0, 0, 0));
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel barPanel = card("Overall Completion");
        barPanel.add(progressBar, BorderLayout.CENTER);

        // Recommendations
        recommendationsArea = new JTextArea(8, 40);
        recommendationsArea.setEditable(false);
        recommendationsArea.setBackground(new Color(20, 20, 40));
        recommendationsArea.setForeground(TEXT);
        recommendationsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        recommendationsArea.setLineWrap(true);
        recommendationsArea.setWrapStyleWord(true);
        recommendationsArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane recScroll = new JScrollPane(recommendationsArea);
        recScroll.setBorder(BorderFactory.createEmptyBorder());
        recScroll.getViewport().setBackground(new Color(20, 20, 40));

        JPanel recCard = card("💡 Smart Recommendations");
        recCard.add(recScroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(statsRow, BorderLayout.NORTH);
        center.add(barPanel, BorderLayout.CENTER);
        center.add(recCard,  BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
        refresh();
    }

    /** Re-reads planner data and updates all labels. */
    public void refresh() {
        List<Task> all       = planner.getAllTasks();
        long done            = all.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count();
        long pending         = all.size() - done;
        double pct           = planner.getCompletionPercentage();

        totalLabel.setText(String.valueOf(all.size()));
        pendingLabel.setText(String.valueOf(pending));
        completedLabel.setText(String.valueOf(done));
        progressLabel.setText(String.format("%.0f%%", pct));
        progressBar.setValue((int) pct);
        progressBar.setString(String.format("%.1f%%", pct));

        List<String> recs = planner.getRecommendations();
        StringBuilder sb = new StringBuilder();
        for (String r : recs) sb.append(r).append("\n\n");
        recommendationsArea.setText(sb.toString().trim());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JPanel statCard(JLabel numLabel, String title, Color accent) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 4));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(accent.darker(), 1, true),
                new EmptyBorder(14, 18, 14, 18)));
        p.add(numLabel);

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(SUBTEXT);
        p.add(lbl);
        return p;
    }

    private JLabel statLabel(String value, String title, Color color) {
        JLabel l = new JLabel(value, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        l.setForeground(color);
        return l;
    }

    private JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 100), 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(ACCENT);
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }
}