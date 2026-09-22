package ui.dialogs;

import model.Course;
import ui.components.StyledButton;
import ui.theme.Theme;
import ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class CourseDialog extends JDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static Color btnBg() {
        return ThemeManager.isDark() ? new Color(50, 55, 62) : new Color(215, 228, 240);
    }

    public CourseDialog(JFrame parent, Course existing, Runnable onSave) {
        super(parent, true);
        boolean isEdit = existing != null;
        String titleText = isEdit ? "Изменить курс" : "Новый курс";

        setUndecorated(true);
        setSize(430, 520);
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
                    setLocation(p.x - d[0].x, p.y - d[0].y);
                }
            }
        });

        JLabel tl = new JLabel(titleText);
        tl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tl.setForeground(Theme.TEXT_SECONDARY);
        tbar.add(tl, BorderLayout.WEST);

        JButton cbtn = makeCloseBtn();
        cbtn.addActionListener(e -> dispose());
        tbar.add(cbtn, BorderLayout.EAST);
        root.add(tbar, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel headerLabel = new JLabel(isEdit ? "Редактирование курса" : "Создание нового курса");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(Theme.TEXT_PRIMARY);
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(headerLabel);
        form.add(Box.createVerticalStrut(16));

        form.add(makeLabel("Название"));
        form.add(Box.createVerticalStrut(6));
        JTextField nameField = makeTextField(isEdit ? existing.getName() : "");
        form.add(nameField);
        form.add(Box.createVerticalStrut(14));

        form.add(makeLabel("Всего задач"));
        form.add(Box.createVerticalStrut(6));
        JTextField totalField = makeTextField(isEdit ? String.valueOf(existing.getTotal()) : "0");
        form.add(totalField);
        form.add(Box.createVerticalStrut(14));

        form.add(makeLabel("Сделано задач"));
        form.add(Box.createVerticalStrut(6));
        JTextField doneField = makeTextField(isEdit ? String.valueOf(existing.getDone()) : "0");
        form.add(doneField);
        form.add(Box.createVerticalStrut(14));

        form.add(makeLabel("Дедлайн (необязательно)"));
        form.add(Box.createVerticalStrut(6));

        JTextField deadlineField = makeTextField(
                (isEdit && existing.getDeadline() != null)
                        ? existing.getDeadline().format(FMT) : "");
        deadlineField.setToolTipText("Формат: дд.мм.гггг. Оставь пустым — дедлайна не будет");
        form.add(deadlineField);
        form.add(Box.createVerticalStrut(6));

        JLabel hint = new JLabel("Формат: 31.12.2026");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(Theme.TEXT_SECONDARY);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(hint);

        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(0, 20, 12, 20));

        StyledButton cancelBtn = new StyledButton("Отмена", btnBg());
        cancelBtn.addActionListener(e -> dispose());
        buttons.add(cancelBtn);

        StyledButton okBtn = new StyledButton(isEdit ? "Сохранить" : "Создать", Theme.SUCCESS);
        okBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите название курса", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int total, done;
            try {
                total = Integer.parseInt(totalField.getText().trim());
                done = Integer.parseInt(doneField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Всего и Сделано должны быть числами", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (total < 0 || done < 0) {
                JOptionPane.showMessageDialog(this, "Числа не могут быть отрицательными", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (done > total) done = total;

            LocalDate deadline = null;
            String dTxt = deadlineField.getText().trim();
            if (!dTxt.isEmpty()) {
                try {
                    deadline = LocalDate.parse(dTxt, FMT);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Неверная дата. Формат: дд.мм.гггг", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (isEdit) {
                existing.setName(name);
                existing.setTotal(total);
                existing.setDone(done);
                existing.setDeadline(deadline);
            } else {
                Course c = new Course(name, total, done);
                c.setDeadline(deadline);
                if (onCreate != null) onCreate.accept(c);
            }

            if (onSave != null) onSave.run();
            dispose();
        });
        buttons.add(okBtn);

        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));

        SwingUtilities.invokeLater(nameField::requestFocusInWindow);
    }

    private Consumer<Course> onCreate;
    public void setOnCreate(Consumer<Course> onCreate) { this.onCreate = onCreate; }

    private JButton makeCloseBtn() {
        JButton btn = new JButton() {
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
        btn.setPreferredSize(new Dimension(46, 36));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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