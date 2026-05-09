import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Displays completion progress per subject.
 */
public class ProgressPanel extends JPanel {

    private Planner planner;
    private JPanel  barContainer;

    private static final Color BG      = new Color(15, 15, 30);
    private static final Color CARD    = new Color(25, 25, 50);
    private static final Color ACCENT  = new Color(99, 179, 237);
    private static final Color GREEN   = new Color(72, 199, 142);
    private static final Color ORANGE  = new Color(255, 179, 71);
    private static final Color RED_C   = new Color(220, 80, 80);
    private static final Color TEXT    = new Color(220, 220, 240);
    private static final Color SUBTEXT = new Color(140, 140, 170);

    public ProgressPanel(Planner planner) {
        this.planner = planner;
        setBackground(BG);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Progress Tracker");
        title.setIcon(IconUtils.get("progress", 24, ACCENT));
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ACCENT);
        add(title, BorderLayout.NORTH);

        barContainer = new JPanel();
        barContainer.setLayout(new BoxLayout(barContainer, BoxLayout.Y_AXIS));
        barContainer.setBackground(BG);

        JScrollPane scroll = new JScrollPane(barContainer);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        barContainer.removeAll();

        List<Task> all = planner.getAllTasks();
        if (all.isEmpty()) {
            JLabel empty = new JLabel("No tasks yet. Add tasks to track progress.", SwingConstants.CENTER);
            empty.setForeground(SUBTEXT);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            barContainer.add(empty);
            barContainer.revalidate();
            barContainer.repaint();
            return;
        }

        // Overall
        barContainer.add(subjectBar("Overall", planner.getCompletionPercentage(), ACCENT));
        barContainer.add(Box.createVerticalStrut(8));

        // Per-subject breakdown
        Map<String, int[]> subjectStats = new LinkedHashMap<>(); // [total, done]
        for (Task t : all) {
            subjectStats.putIfAbsent(t.getSubjectName(), new int[]{0, 0});
            subjectStats.get(t.getSubjectName())[0]++;
            if (t.getStatus() == Task.TaskStatus.COMPLETED)
                subjectStats.get(t.getSubjectName())[1]++;
        }

        for (Map.Entry<String, int[]> e : subjectStats.entrySet()) {
            int total = e.getValue()[0], done = e.getValue()[1];
            double pct = total == 0 ? 0 : (done * 100.0) / total;
            Color barColor = pct >= 75 ? GREEN : pct >= 40 ? ORANGE : RED_C;
            barContainer.add(subjectBar(e.getKey() + "  (" + done + "/" + total + ")", pct, barColor));
            barContainer.add(Box.createVerticalStrut(6));
        }

        // Task type summary
        barContainer.add(Box.createVerticalStrut(16));
        barContainer.add(sectionLabel("Task Breakdown by Type"));

        long exams       = all.stream().filter(t -> t instanceof ExamTask).count();
        long assignments = all.stream().filter(t -> t instanceof AssignmentTask).count();
        long examsDone   = all.stream().filter(t -> t instanceof ExamTask && t.getStatus() == Task.TaskStatus.COMPLETED).count();
        long asgDone     = all.stream().filter(t -> t instanceof AssignmentTask && t.getStatus() == Task.TaskStatus.COMPLETED).count();

        if (exams > 0)
            barContainer.add(subjectBar("Exams (" + examsDone + "/" + exams + ")",
                    exams == 0 ? 0 : examsDone * 100.0 / exams, new Color(154, 117, 235)));
        barContainer.add(Box.createVerticalStrut(6));
        if (assignments > 0)
            barContainer.add(subjectBar("Assignments (" + asgDone + "/" + assignments + ")",
                    assignments == 0 ? 0 : asgDone * 100.0 / assignments, ORANGE));

        barContainer.revalidate();
        barContainer.repaint();
    }

    private JPanel subjectBar(String label, double pct, Color barColor) {
        JPanel p = new JPanel(new BorderLayout(8, 4));
        p.setBackground(CARD);
        p.setBorder(new EmptyBorder(10, 14, 10, 14));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel pctLbl = new JLabel(String.format("%.1f%%", pct), SwingConstants.RIGHT);
        pctLbl.setForeground(barColor);
        pctLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel lblRow = new JPanel(new BorderLayout());
        lblRow.setOpaque(false);
        lblRow.add(lbl, BorderLayout.WEST);
        lblRow.add(pctLbl, BorderLayout.EAST);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) pct);
        bar.setStringPainted(false);
        bar.setForeground(barColor);
        bar.setBackground(new Color(35, 35, 60));
        bar.setBorder(BorderFactory.createEmptyBorder());
        bar.setPreferredSize(new Dimension(0, 10));

        p.add(lblRow, BorderLayout.NORTH);
        p.add(bar,    BorderLayout.CENTER);
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(ACCENT);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        return l;
    }
}