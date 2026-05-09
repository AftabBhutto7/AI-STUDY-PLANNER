import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel for managing tasks: add, edit, delete, mark complete.
 */
public class TaskPanel extends JPanel {

    private Planner   planner;
    private JTable    table;
    private DefaultTableModel tableModel;
    private MainFrame mainFrame;

    private static final Color BG      = new Color(15, 15, 30);
    private static final Color CARD    = new Color(25, 25, 50);
    private static final Color ACCENT  = new Color(99, 179, 237);
    private static final Color ACCENT2 = new Color(154, 117, 235);
    private static final Color GREEN   = new Color(72, 199, 142);
    private static final Color RED_C   = new Color(220, 80, 80);
    private static final Color TEXT    = new Color(220, 220, 240);
    private static final Color SUBTEXT = new Color(140, 140, 170);

    private static final String[] COLS = {
        "ID", "Type", "Subject", "Topic", "Deadline", "Difficulty", "Hours", "Status", "Priority"
    };

    public TaskPanel(Planner planner, MainFrame mainFrame) {
        this.planner   = planner;
        this.mainFrame = mainFrame;
        setBackground(BG);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Task Manager");
        title.setIcon(IconUtils.get("tasks", 24, ACCENT));
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ACCENT);
        add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(CARD);
        scroll.getViewport().setBackground(new Color(20, 20, 45));
        scroll.setBorder(new LineBorder(new Color(60, 60, 100)));
        add(scroll, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBar.setOpaque(false);
        btnBar.add(btn("Add Task", "add", ACCENT,  e -> showAddDialog()));
        btnBar.add(btn("Edit", "edit", ACCENT2, e -> showEditDialog()));
        btnBar.add(btn("Mark Done", "done", GREEN,   e -> markDone()));
        btnBar.add(btn("Delete", "delete", RED_C,   e -> deleteSelected()));
        add(btnBar, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        tableModel.setRowCount(0);
        for (Task t : planner.getAllTasks()) {
            tableModel.addRow(new Object[]{
                t.getTaskId(),
                t.getTaskType(),
                t.getSubjectName(),
                t.getTopicTitle(),
                t.getDeadline().toString(),
                t.getDifficulty(),
                t.getEstimatedHours(),
                t.getStatus(),
                t.calculatePriority()
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner helper class that holds all form field references directly.
    // This fixes the ClientProperty bug where fields couldn't be retrieved.
    // ─────────────────────────────────────────────────────────────────────────
    private static class TaskForm {
        JComboBox<String> typeBox;
        JTextField        subjField;
        JTextField        topicField;
        JTextField        dateField;
        JComboBox<String> diffBox;
        JTextField        hoursField;
        JTextField        extraField;
        JLabel            extraLabel;
        JPanel            panel;
    }

    private TaskForm buildTaskForm(Task existing) {
        TaskForm form = new TaskForm();

        form.typeBox    = combo("EXAM", "ASSIGNMENT");
        form.subjField  = field(existing != null ? existing.getSubjectName() : "");
        form.topicField = field(existing != null ? existing.getTopicTitle()  : "");
        form.dateField  = field(existing != null
                ? existing.getDeadline().toString()
                : LocalDate.now().plusDays(7).toString());
        form.diffBox    = combo("EASY", "MEDIUM", "HARD");
        form.hoursField = field(existing != null ? String.valueOf(existing.getEstimatedHours()) : "2.0");

        String extraDefault = "";
        if (existing instanceof ExamTask)           extraDefault = ((ExamTask) existing).getExamHall();
        else if (existing instanceof AssignmentTask) extraDefault = ((AssignmentTask) existing).getSubmissionMode();
        form.extraField = field(extraDefault);

        // Pre-select values when editing
        if (existing != null) {
            form.typeBox.setSelectedItem(existing.getTaskType());
            form.diffBox.setSelectedItem(existing.getDifficulty().name());
        }

        // Dynamic label for extra field
        boolean isExam = "EXAM".equals(form.typeBox.getSelectedItem());
        form.extraLabel = makeLabel(isExam ? "Exam Hall:" : "Submission Mode:");

        form.typeBox.addActionListener(e -> {
            boolean exam = "EXAM".equals(form.typeBox.getSelectedItem());
            form.extraLabel.setText(exam ? "Exam Hall:" : "Submission Mode:");
        });

        // Build panel
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD);
        p.setBorder(new EmptyBorder(16, 20, 8, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 4, 7, 4);
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.gridy  = 0;

        addFormRow(p, g, makeLabel("Task Type:"),            form.typeBox);
        addFormRow(p, g, makeLabel("Subject:"),              form.subjField);
        addFormRow(p, g, makeLabel("Topic:"),                form.topicField);
        addFormRow(p, g, makeLabel("Deadline (YYYY-MM-DD):"),form.dateField);
        addFormRow(p, g, makeLabel("Difficulty:"),           form.diffBox);
        addFormRow(p, g, makeLabel("Est. Hours:"),           form.hoursField);
        addFormRow(p, g, form.extraLabel,                    form.extraField);

        form.panel = p;
        return form;
    }

    private void showAddDialog() {
        JDialog dlg = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), "Add Task", true);
        dlg.setSize(440, 460);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(CARD);

        TaskForm form = buildTaskForm(null);

        JButton save = btn("Save Task", null, ACCENT, e -> {
            if (commitForm(form, null)) {
                dlg.dispose();
                refresh();
                mainFrame.refreshAll();
                persist();
            }
        });

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bot.setBackground(CARD);
        bot.add(save);

        dlg.setLayout(new BorderLayout());
        dlg.add(form.panel, BorderLayout.CENTER);
        dlg.add(bot,        BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { warn("Select a task to edit."); return; }

        String id   = (String) tableModel.getValueAt(row, 0);
        Task   task = planner.findTask(id);
        if (task == null) { warn("Task not found."); return; }

        JDialog dlg = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), "Edit Task", true);
        dlg.setSize(440, 460);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(CARD);

        TaskForm form = buildTaskForm(task);

        JButton save = btn("Update Task", null, ACCENT2, e -> {
            if (commitForm(form, task)) {
                dlg.dispose();
                refresh();
                mainFrame.refreshAll();
                persist();
            }
        });

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bot.setBackground(CARD);
        bot.add(save);

        dlg.setLayout(new BorderLayout());
        dlg.add(form.panel, BorderLayout.CENTER);
        dlg.add(bot,        BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    /**
     * Reads values directly from the TaskForm fields and either creates or
     * updates a task. Returns true on success, false if validation fails.
     */
    private boolean commitForm(TaskForm form, Task existing) {
        // Read all values directly — no ClientProperty lookup needed
        String type    = (String) form.typeBox.getSelectedItem();
        String subj    = form.subjField.getText().trim();
        String topic   = form.topicField.getText().trim();
        String dateStr = form.dateField.getText().trim();
        String diffStr = (String) form.diffBox.getSelectedItem();
        String hoursStr = form.hoursField.getText().trim();
        String extra   = form.extraField.getText().trim();

        // Validate required fields
        if (subj.isEmpty()) {
            warn("Subject field is required."); return false;
        }
        if (topic.isEmpty()) {
            warn("Topic field is required."); return false;
        }
        if (dateStr.isEmpty()) {
            warn("Deadline field is required."); return false;
        }
        if (hoursStr.isEmpty()) {
            warn("Estimated hours field is required."); return false;
        }

        // Parse deadline
        LocalDate deadline;
        try {
            deadline = LocalDate.parse(dateStr);
        } catch (DateTimeParseException ex) {
            warn("Invalid date format. Please use YYYY-MM-DD (e.g. 2026-05-20).");
            return false;
        }

        // Parse hours
        double hrs;
        try {
            hrs = Double.parseDouble(hoursStr);
        } catch (NumberFormatException ex) {
            warn("Estimated hours must be a number (e.g. 2.5).");
            return false;
        }
        if (hrs <= 0) {
            warn("Estimated hours must be greater than 0.");
            return false;
        }

        try {
            Task.DifficultyLevel diff = Task.DifficultyLevel.valueOf(diffStr);

            if (existing != null) {
                // ── Update mode ──────────────────────────────────────────────
                existing.setSubjectName(subj);
                existing.setTopicTitle(topic);
                existing.setDeadline(deadline);
                existing.setDifficulty(diff);
                existing.setEstimatedHours(hrs);
                // Update type-specific fields
                if (existing instanceof ExamTask) {
                    ((ExamTask) existing).setExamHall(extra.isEmpty() ? "TBD" : extra);
                } else if (existing instanceof AssignmentTask) {
                    ((AssignmentTask) existing).setSubmissionMode(extra.isEmpty() ? "Online" : extra);
                }
            } else {
                // ── Create mode ──────────────────────────────────────────────
                String id = Planner.generateId();
                Task newTask;
                if ("EXAM".equals(type)) {
                    newTask = new ExamTask(id, subj, topic, deadline, diff, hrs,
                            extra.isEmpty() ? "TBD" : extra);
                } else {
                    newTask = new AssignmentTask(id, subj, topic, deadline, diff, hrs,
                            extra.isEmpty() ? "Online" : extra);
                }
                planner.addTask(newTask);
            }
            return true;

        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
            return false;
        }
    }

    private void markDone() {
        int row = table.getSelectedRow();
        if (row == -1) { warn("Select a task to mark as completed."); return; }
        String id   = (String) tableModel.getValueAt(row, 0);
        Task   task = planner.findTask(id);
        if (task != null) {
            task.setStatus(Task.TaskStatus.COMPLETED);
            refresh();
            mainFrame.refreshAll();
            persist();
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) { warn("Select a task to delete."); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this task?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            planner.removeTask(id);
            refresh();
            mainFrame.refreshAll();
            persist();
        }
    }

    private void persist() {
        try {
            FileManager.saveTasks(planner.getUser().getUsername(), new java.util.ArrayList<>(planner.getAllTasks()));
        } catch (IOException ex) {
            warn("Could not save data: " + ex.getMessage());
        }
    }

    // ── Style helpers ─────────────────────────────────────────────────────────

    private void addFormRow(JPanel p, GridBagConstraints g, JLabel label, JComponent field) {
        g.gridx = 0; g.weightx = 0.38; g.gridwidth = 1;
        p.add(label, g);
        g.gridx = 1; g.weightx = 0.62;
        p.add(field, g);
        g.gridy++;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(SUBTEXT);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    private JTextField field(String val) {
        JTextField f = new JTextField(val, 16);
        f.setBackground(new Color(35, 35, 65));
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(70, 70, 110), 1),
                new EmptyBorder(5, 9, 5, 9)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return f;
    }

    private JComboBox<String> combo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setBackground(new Color(35, 35, 65));
        c.setForeground(TEXT);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return c;
    }

    private void styleTable() {
        table.setBackground(new Color(20, 20, 45));
        table.setForeground(TEXT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.setGridColor(new Color(50, 50, 80));
        table.setSelectionBackground(new Color(60, 80, 140));
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setBackground(CARD);
        table.getTableHeader().setForeground(ACCENT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private JButton btn(String text, String iconType, Color color, ActionListener al) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? color.darker()   :
                            getModel().isRollover() ? color.brighter() : color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(getText());
                int startX = (getWidth() - textW - 20) / 2;
                if (iconType != null) {
                    IconUtils.get(iconType, 14, Color.WHITE).paintIcon(this, g2, startX, (getHeight() - 14) / 2);
                }
                g2.drawString(getText(), startX + (iconType != null ? 20 : 10),
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(130, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        return b;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}