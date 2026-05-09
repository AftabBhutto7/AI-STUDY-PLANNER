import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Displays the generated study schedule.
 */
public class SchedulePanel extends JPanel {

    private Planner planner;
    private JTable  table;
    private DefaultTableModel tableModel;

    private static final Color BG      = new Color(15, 15, 30);
    private static final Color CARD    = new Color(25, 25, 50);
    private static final Color ACCENT  = new Color(99, 179, 237);
    private static final Color ACCENT2 = new Color(154, 117, 235);
    private static final Color GREEN   = new Color(72, 199, 142);
    private static final Color TEXT    = new Color(220, 220, 240);
    private static final Color SUBTEXT = new Color(140, 140, 170);

    private static final String[] COLS = {
        "Date", "Start", "End", "Subject", "Topic", "Type", "Difficulty", "Status"
    };

    public SchedulePanel(Planner planner) {
        this.planner = planner;
        setBackground(BG);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        buildUI();
    }

    private void buildUI() {
        // Header
        JLabel title = new JLabel("Study Schedule");
        title.setIcon(IconUtils.get("schedule", 24, ACCENT));
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ACCENT);

        JLabel sub = new JLabel("AI-generated schedule based on your tasks and priorities.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(SUBTEXT);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(sub,   BorderLayout.CENTER);

        // Generate button
        JButton genBtn = btn("⚙ Generate Schedule", ACCENT2);
        genBtn.addActionListener(e -> {
            planner.generateSchedule();
            refresh();
        });

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(header, BorderLayout.WEST);
        topRow.add(genBtn, BorderLayout.EAST);

        add(topRow, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        // Custom row coloring
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                String status = (String) tbl.getModel().getValueAt(row, 7);
                if (isSelected) {
                    c.setBackground(new Color(60, 80, 140));
                } else if ("DONE".equals(status)) {
                    c.setBackground(new Color(20, 50, 35));
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(20, 20, 45) : new Color(25, 25, 55));
                }
                c.setForeground("DONE".equals(status) ? GREEN : TEXT);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(20, 20, 45));
        scroll.setBorder(new LineBorder(new Color(60, 60, 100)));
        add(scroll, BorderLayout.CENTER);

        // Note
        JLabel note = new JLabel("Tip: Click 'Generate Schedule' after adding or modifying tasks.");
        note.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        note.setForeground(SUBTEXT);
        add(note, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        tableModel.setRowCount(0);
        List<StudySlot> slots = planner.getSchedule();
        for (StudySlot s : slots) {
            tableModel.addRow(new Object[]{
                s.getDate().toString(),
                s.getStartTime(),
                s.getEndTime(),
                s.getTask().getSubjectName(),
                s.getTask().getTopicTitle(),
                s.getTask().getTaskType(),
                s.getTask().getDifficulty(),
                s.isCompleted() ? "DONE" : "PENDING"
            });
        }
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
    }

    private JButton btn(String text, Color color) {
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
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(190, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}