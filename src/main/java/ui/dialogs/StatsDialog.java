package ui.dialogs;

import model.Course;
import ui.components.StyledButton;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class StatsDialog extends JDialog {

    public StatsDialog(JFrame parent, List<Course> courses) {
        super(parent, "Статистика", true);

        // ===== Расчёты =====
        int totalDone = 0, totalLeft = 0;
        for (Course c : courses) {
            totalDone += c.getDone();
            totalLeft += c.getLeft();
        }

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate today = LocalDate.now();
        LocalDate deadline = LocalDate.of(2027, 8, 1);

        long daysPassed = Math.max(1, ChronoUnit.DAYS.between(start, today) + 1);
        long daysLeft = Math.max(1, ChronoUnit.DAYS.between(today, deadline));

        double avgDone = totalDone / (double) daysPassed;
        double needPerDay = totalLeft / (double) daysLeft;

        // ===== Окно =====
        setUndecorated(true);
        setSize(620, 800);
        setLocationRelativeTo(parent);
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout(0, 0)) {
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

        // ===== Шапка =====
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Theme.BG_TITLEBAR);
        titleBar.setPreferredSize(new Dimension(0, 36));
        titleBar.setBorder(new EmptyBorder(0, 16, 0, 8));

        final Point[] dlgDrag = {null};
        titleBar.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dlgDrag[0] = e.getPoint(); }
        });
        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dlgDrag[0] != null) {
                    Point p = e.getLocationOnScreen();
                    setLocation(p.x - dlgDrag[0].x, p.y - dlgDrag[0].y);
                }
            }
        });

        JLabel tLabel = new JLabel("Статистика");
        tLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tLabel.setForeground(Theme.TEXT_SECONDARY);
        titleBar.add(tLabel, BorderLayout.WEST);

        JButton closeBtn = new JButton() {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
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
        closeBtn.setPreferredSize(new Dimension(46, 36));
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setOpaque(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        titleBar.add(closeBtn, BorderLayout.EAST);
        root.add(titleBar, BorderLayout.NORTH);

        // ===== Контент =====
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG_DARK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(24, 28, 28, 28));

        JLabel header = new JLabel("Статистика по задачам в день");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Theme.TEXT_PRIMARY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(Box.createVerticalStrut(8));

        JLabel sub = new JLabel("Период: 01.01.2026 → 01.08.2027");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sub);
        panel.add(Box.createVerticalStrut(24));

        panel.add(cardRow("Всего сделано задач", String.valueOf(totalDone), Theme.LEVEL_4));
        panel.add(Box.createVerticalStrut(10));
        panel.add(cardRow("Осталось задач", String.valueOf(totalLeft), Theme.ACCENT));
        panel.add(Box.createVerticalStrut(24));

        panel.add(infoRow("Дата старта", start.toString()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(infoRow("Сегодня", today.toString()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(infoRow("Дедлайн", deadline.toString()));
        panel.add(Box.createVerticalStrut(24));

        panel.add(infoRow("Дней прошло", String.valueOf(daysPassed)));
        panel.add(Box.createVerticalStrut(8));
        panel.add(infoRow("Дней осталось", String.valueOf(daysLeft)));
        panel.add(Box.createVerticalStrut(28));

        panel.add(highlightRow("Средне в день (сделано)",
                String.format("%.2f", avgDone).replace('.', ','), Theme.LEVEL_4));
        panel.add(Box.createVerticalStrut(14));
        panel.add(highlightRow("Нужно в день (чтобы успеть)",
                String.format("%.2f", needPerDay).replace('.', ','), Theme.ACCENT));

        root.add(panel, BorderLayout.CENTER);

        // ===== Кнопка Закрыть =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 12));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 20, 12, 20));

        StyledButton close = new StyledButton("Закрыть", new Color(33, 38, 45));
        close.addActionListener(e -> dispose());
        bottom.add(close);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
    }

    // ===== Хелперы =====

    private JPanel cardRow(String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
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

    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(Theme.TEXT_SECONDARY);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        v.setForeground(Theme.TEXT_PRIMARY);

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private JPanel highlightRow(String label, String value, Color accent) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120));
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

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        v.setForeground(accent);

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }
}