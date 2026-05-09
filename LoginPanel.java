import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * Login / Registration screen.
 */
public class LoginPanel extends JPanel {

    private MainFrame mainFrame;

    // Login fields
    private JTextField  loginUserField;
    private JPasswordField loginPassField;

    // Register fields
    private JTextField  regNameField, regUserField, regHoursField;
    private JPasswordField regPassField;
    private JComboBox<String> regTimeCombo;

    private static final Color BG       = new Color(15, 15, 30);
    private static final Color CARD     = new Color(25, 25, 50);
    private static final Color ACCENT   = new Color(99, 179, 237);
    private static final Color ACCENT2  = new Color(154, 117, 235);
    private static final Color TEXT     = new Color(220, 220, 240);
    private static final Color SUBTEXT  = new Color(140, 140, 170);
    private static final Font  TITLE_F  = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font  LABEL_F  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  BTN_F    = new Font("Segoe UI", Font.BOLD, 13);

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(BG);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(CARD);
        tabs.setForeground(TEXT);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setPreferredSize(new Dimension(420, 380));

        tabs.addTab("  Login  ",  buildLoginTab());
        tabs.addTab("  Register  ", buildRegisterTab());

        // Wrapper card
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 100), 1, true),
                new EmptyBorder(24, 32, 24, 32)));
        card.setPreferredSize(new Dimension(460, 440));

        JLabel title = new JLabel("AI Study Planner", SwingConstants.CENTER);
        title.setIcon(IconUtils.get("book", 28, ACCENT));
        title.setFont(TITLE_F);
        title.setForeground(ACCENT);

        JLabel sub = new JLabel("Plan smarter, study better.", SwingConstants.CENTER);
        sub.setFont(LABEL_F);
        sub.setForeground(SUBTEXT);

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.add(title);
        header.add(sub);

        card.add(header, BorderLayout.NORTH);
        card.add(tabs,   BorderLayout.CENTER);

        add(card);
    }

    private JPanel buildLoginTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.gridx  = 0; gbc.gridy = 0; gbc.gridwidth = 2;

        loginUserField = styledField("Username");
        loginPassField = new JPasswordField();
        stylePassField(loginPassField, "Password");

        addRow(p, gbc, "Username", loginUserField);
        addRow(p, gbc, "Password", loginPassField);

        JButton btn = styledButton("Login", ACCENT);
        btn.addActionListener(e -> doLogin());

        gbc.gridy++; gbc.gridwidth = 2;
        p.add(btn, gbc);
        return p;
    }

    private JPanel buildRegisterTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.gridx  = 0; gbc.gridy = 0; gbc.gridwidth = 2;

        regNameField  = styledField("Full name");
        regUserField  = styledField("Username");
        regPassField  = new JPasswordField();
        stylePassField(regPassField, "Password");
        regHoursField = styledField("e.g. 4");

        regTimeCombo = new JComboBox<>(new String[]{"MORNING","EVENING","NIGHT"});
        styleCombo(regTimeCombo);

        addRow(p, gbc, "Full Name",   regNameField);
        addRow(p, gbc, "Username",    regUserField);
        addRow(p, gbc, "Password",    regPassField);
        addRow(p, gbc, "Study Hrs/Day", regHoursField);
        addRow(p, gbc, "Preferred Time", regTimeCombo);

        JButton btn = styledButton("Create Account", ACCENT2);
        btn.addActionListener(e -> doRegister());
        gbc.gridy++; gbc.gridwidth = 2;
        p.add(btn, gbc);
        return p;
    }

    private void doLogin() {
        String username = loginUserField.getText().trim();
        String password = new String(loginPassField.getPassword());
        try {
            User saved = FileManager.loadUser(username);
            if (saved == null) {
                showError("No account found for this username. Please register.");
                return;
            }
            if (!saved.authenticate(password)) {
                showError("Invalid username or password.");
                return;
            }
            mainFrame.loadPlanner(saved);
        } catch (IOException | ClassNotFoundException ex) {
            showError("Error loading profile: " + ex.getMessage());
        }
    }

    private void doRegister() {
        try {
            String fullName  = regNameField.getText().trim();
            String username  = regUserField.getText().trim();
            String password  = new String(regPassField.getPassword());
            String hoursStr  = regHoursField.getText().trim();
            String timePref  = (String) regTimeCombo.getSelectedItem();

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || hoursStr.isEmpty()) {
                showError("All fields are required."); return;
            }

            double hours;
            try { hours = Double.parseDouble(hoursStr); }
            catch (NumberFormatException ex) { showError("Study hours must be a number."); return; }

            User.StudyTime st = User.StudyTime.valueOf(timePref);
            User user = new User(username, password, fullName, hours, st);
            FileManager.saveUser(user);
            JOptionPane.showMessageDialog(this, "Account created! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (IOException ex) {
            showError("Could not save profile: " + ex.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addRow(JPanel p, GridBagConstraints gbc, String label, JComponent field) {
        gbc.gridwidth = 1; gbc.gridx = 0; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label + ":");
        lbl.setForeground(SUBTEXT); lbl.setFont(LABEL_F);
        p.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        p.add(field, gbc);
        gbc.gridy++;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField(14);
        f.setBackground(new Color(35, 35, 65));
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(70, 70, 110), 1),
                new EmptyBorder(4, 8, 4, 8)));
        f.setFont(LABEL_F);
        return f;
    }

    private void stylePassField(JPasswordField f, String ph) {
        f.setBackground(new Color(35, 35, 65));
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(70, 70, 110), 1),
                new EmptyBorder(4, 8, 4, 8)));
        f.setFont(LABEL_F);
        f.setColumns(14);
    }

    private void styleCombo(JComboBox<String> c) {
        c.setBackground(new Color(35, 35, 65));
        c.setForeground(TEXT);
        c.setFont(LABEL_F);
    }

    private JButton styledButton(String text, Color color) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? color.darker() :
                            getModel().isRollover() ? color.brighter() : color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(BTN_F);
        b.setForeground(Color.WHITE);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(160, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}