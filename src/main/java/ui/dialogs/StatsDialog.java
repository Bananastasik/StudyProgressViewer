package ui.dialogs;

import model.CourseRepository;
import ui.components.StyledButton;
import ui.theme.Theme;
import ui.theme.ThemeManager;
import util.StatsConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class StatsDialog extends JDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static Color btnBg() {
        return ThemeManager.isDark() ? new Color(50, 55, 62) : new Color(215, 228, 240);
    }

    private final CourseRepository repo;
    private final StatsConfig config;

    private JLabel startValueLabel;
    private JLabel deadlineValueLabel;
    private JLabel daysPassedValueLabel;
    private JLabel daysLeftValueLabel;
    private JLabel avgDoneValueLabel;
    private JLabel needPerDayValueLabel;
    private JLabel subLabel;

    public StatsDialog(JFrame parent, CourseRepository repo) {
        super(parent, "Статистика", true);
        this.repo = repo;
        this.config = new StatsConfig();

        setUndecorated(true);
        setSize(620, 860);
        setLocationRelativeTo(parent);
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_DARK);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));

        root.add(createTitleBar(), BorderLayout.NORTH);
        root.add(createContent(), BorderLayout.CENTER);
        root.add(createBottomBar(), BorderLayout.SOUTH);

        setContentPane(root);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));

        refreshValues();
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Theme.BG_TITLEBAR);
        titleBar.setPreferredSize(new Dimension(0, 36));
        titleBar.setBorder(new EmptyBorder(0, 16, 0, 8));

        final Point[] d = {null};
        titleBar.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { d[0] = e.getPoint(); }
        });
        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (d[0] != null) {
                    Point p = e.getLocationOnScreen();
                    setLocation(p.x - d[0].x, p.y - d[0].y);
                }
            }
        });

        JLabel t = new JLabel("Статистика");
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(Theme.TEXT_SECONDARY);
        titleBar.add(t, BorderLayout.WEST);

        JButton c = new JButton() {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hover) { g2.setColor(new Color(200, 40, 60)); g2.fillRect(0, 0, getWidth(), getHeight()); }
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2, r = 5;
                g2.drawLine(cx - r, cy - r, cx + r, cy + r);
                g2.drawLine(cx - r, cy + r, cx + r, cy - r);
                g2.dispose();
            }
        };
        c.setPreferredSize(new Dimension(46, 36));
        c.setFocusPainted(false);
        c.setBorderPainted(false);
        c.setContentAreaFilled(false);
        c.setOpaque(false);
        c.setCursor(new Cursor(Cursor.HAND_CURSOR));
        c.addActionListener(e -> dispose());
        titleBar.add(c, BorderLayout.EAST);
        return titleBar;
    }

    private JScrollPane createContent() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG_DARK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(24, 28, 20, 28));

        JLabel header = new JLabel("Статистика по задачам в день");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Theme.TEXT_PRIMARY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(Box.createVerticalStrut(8));

        subLabel = new JLabel();
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(Theme.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(subLabel);
        panel.add(Box.createVerticalStrut(20));

        int totalDone = repo.getTotalDone();
        int totalLeft = repo.getTotalLeft();

        panel.add(cardRow("Всего сделано задач", String.valueOf(totalDone), Theme.LEVEL_4));
        panel.add(Box.createVerticalStrut(10));
        panel.add(cardRow("Осталось задач", String.valueOf(totalLeft), Theme.ACCENT));
        panel.add(Box.createVerticalStrut(20));

        startValueLabel = new JLabel();
        panel.add(dateRow("Дата старта", startValueLabel));
        panel.add(Box.createVerticalStrut(8));

        deadlineValueLabel = new JLabel();
        panel.add(dateRow("Дедлайн", deadlineValueLabel));
        panel.add(Box.createVerticalStrut(20));

        daysPassedValueLabel = new JLabel();
        panel.add(valueRow("Дней прошло", daysPassedValueLabel));
        panel.add(Box.createVerticalStrut(8));

        daysLeftValueLabel = new JLabel();
        panel.add(valueRow("Дней осталось", daysLeftValueLabel));
        panel.add(Box.createVerticalStrut(24));

        avgDoneValueLabel = new JLabel();
        panel.add(highlightRow("Средне в день (сделано)", avgDoneValueLabel, Theme.LEVEL_4));
        panel.add(Box.createVerticalStrut(12));

        needPerDayValueLabel = new JLabel();
        panel.add(highlightRow("Нужно в день (чтобы успеть)", needPerDayValueLabel, Theme.WARNING));

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBackground(Theme.BG_DARK);
        scroll.getViewport().setBackground(Theme.BG_DARK);
        ui.components.DarkScrollBarUI.apply(scroll.getVerticalScrollBar());
        return scroll;
    }

    private JPanel createBottomBar() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 20, 12, 20));

        StyledButton editBtn = new StyledButton("Изменить период", btnBg());
        editBtn.addActionListener(e -> showEditDatesDialog());
        bottom.add(editBtn);

        StyledButton close = new StyledButton("Закрыть", btnBg());
        close.addActionListener(e -> dispose());
        bottom.add(close);

        return bottom;
    }

    private void showEditDatesDialog() {
        JDialog dlg = new JDialog(this, "Период", true);
        dlg.setUndecorated(true);
        dlg.setSize(420, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_DARK);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));

        JPanel tbar = new JPanel(new BorderLayout());
        tbar.setBackground(Theme.BG_TITLEBAR);
        tbar.setPreferredSize(new Dimension(0, 36));
        tbar.setBorder(new EmptyBorder(0, 16, 0, 8));

        final Point[] d = {null};
        tbar.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { d[0] = e.getPoint(); }
        });
        tbar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (d[0] != null) {
                    Point p = e.getLocationOnScreen();
                    dlg.setLocation(p.x - d[0].x, p.y - d[0].y);
                }
            }
        });

        JLabel tl = new JLabel("Изменить период");
        tl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tl.setForeground(Theme.TEXT_SECONDARY);
        tbar.add(tl, BorderLayout.WEST);

        JButton c = new JButton() {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hover) { g2.setColor(new Color(200, 40, 60)); g2.fillRect(0, 0, getWidth(), getHeight()); }
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2, r = 5;
                g2.drawLine(cx - r, cy - r, cx + r, cy + r);
                g2.drawLine(cx - r, cy + r, cx + r, cy - r);
                g2.dispose();
            }
        };
        c.setPreferredSize(new Dimension(46, 36));
        c.setFocusPainted(false);
        c.setBorderPainted(false);
        c.setContentAreaFilled(false);
        c.setOpaque(false);
        c.setCursor(new Cursor(Cursor.HAND_CURSOR));
        c.addActionListener(e -> dlg.dispose());
        tbar.add(c, BorderLayout.EAST);
        root.add(tbar, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel header = new JLabel("Период обучения");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(Theme.TEXT_PRIMARY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(header);
        form.add(Box.createVerticalStrut(20));

        form.add(makeLabel("Дата старта"));
        form.add(Box.createVerticalStrut(6));
        JTextField startField = makeTextField(config.getStart().format(FMT));
        form.add(startField);
        form.add(Box.createVerticalStrut(16));

        form.add(makeLabel("Дедлайн"));
        form.add(Box.createVerticalStrut(6));
        JTextField deadlineField = makeTextField(config.getDeadline().format(FMT));
        form.add(deadlineField);
        form.add(Box.createVerticalStrut(8));

        JLabel hint = new JLabel("Формат: дд.мм.гггг, например 01.01.2026");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(Theme.TEXT_SECONDARY);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(hint);

        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(0, 20, 12, 20));

        StyledButton cancel = new StyledButton("Отмена", btnBg());
        cancel.addActionListener(e -> dlg.dispose());
        buttons.add(cancel);

        StyledButton ok = new StyledButton("Сохранить", Theme.SUCCESS);
        ok.addActionListener(e -> {
            LocalDate newStart, newDeadline;
            try { newStart = LocalDate.parse(startField.getText().trim(), FMT); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Неверная дата старта", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try { newDeadline = LocalDate.parse(deadlineField.getText().trim(), FMT); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Неверная дата дедлайна", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newStart.isBefore(newDeadline)) {
                JOptionPane.showMessageDialog(dlg, "Старт должен быть раньше дедлайна", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            config.setStart(newStart);
            config.setDeadline(newDeadline);
            config.save();
            refreshValues();
            dlg.dispose();
        });
        buttons.add(ok);

        root.add(buttons, BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setShape(new RoundRectangle2D.Double(0, 0, dlg.getWidth(), dlg.getHeight(), 14, 14));
        dlg.setVisible(true);
    }

    private void refreshValues() {
        LocalDate start = config.getStart();
        LocalDate today = LocalDate.now();
        LocalDate deadline = config.getDeadline();

        long daysPassed = Math.max(1, ChronoUnit.DAYS.between(start, today) + 1);
        long daysLeft = Math.max(1, ChronoUnit.DAYS.between(today, deadline));

        int totalDone = repo.getTotalDone();
        int totalLeft = repo.getTotalLeft();

        double avgDone = totalDone / (double) daysPassed;
        double needPerDay = totalLeft / (double) daysLeft;

        startValueLabel.setText(start.format(FMT));
        deadlineValueLabel.setText(deadline.format(FMT));
        daysPassedValueLabel.setText(String.valueOf(daysPassed));
        daysLeftValueLabel.setText(String.valueOf(daysLeft));

        avgDoneValueLabel.setText(String.format("%.2f", avgDone).replace('.', ','));
        needPerDayValueLabel.setText(String.format("%.2f", needPerDay).replace('.', ','));

        subLabel.setText("Период: " + start.format(FMT) + " → " + deadline.format(FMT));
    }

    private JPanel cardRow(String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Theme.BORDER);
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(Theme.TEXT_SECONDARY);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        v.setForeground(valueColor);

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private JPanel dateRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Theme.BORDER);
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setBorder(new EmptyBorder(8, 16, 8, 16));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.setToolTipText("Клик — изменить период");

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(Theme.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valueLabel.setForeground(Theme.TEXT_PRIMARY);

        row.add(l, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showEditDatesDialog(); }
        });
        return row;
    }

    private JPanel valueRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(Theme.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valueLabel.setForeground(Theme.TEXT_PRIMARY);

        row.add(l, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel highlightRow(String label, JLabel valueLabel, Color accent) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 90));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(Theme.TEXT_PRIMARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(Theme.TEXT_PRIMARY);

        row.add(l, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField makeTextField(String initial) {
        JTextField field = new JTextField(initial);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Theme.TEXT_PRIMARY);
        field.setBackground(Theme.BG_INPUT);
        field.setCaretColor(Theme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }
}