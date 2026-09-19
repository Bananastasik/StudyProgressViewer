import model.Course;
import model.CourseRepository;
import ui.components.CourseCard;
import ui.components.SummaryPanel;
import ui.components.TitleBar;
import ui.dialogs.CourseDialog;
import ui.dialogs.StatsDialog;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;

public class CourseProgressApp extends JFrame {

    private final CourseRepository repo = new CourseRepository();

    private JPanel coursesPanel;
    private SummaryPanel summaryPanel;

    public CourseProgressApp() {
        setTitle("Прогресс курсов");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(1320, 940);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        // ===== Корневая панель с тёмным фоном и закруглением =====
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
        root.add(new TitleBar(this, "Прогресс курсов"), BorderLayout.NORTH);

        // ===== Контент =====
        JPanel content = new JPanel(new BorderLayout(20, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Верх: header + summary
        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
        topBox.setOpaque(false);
        topBox.add(createHeader());
        topBox.add(Box.createVerticalStrut(16));

        summaryPanel = new SummaryPanel(repo);
        topBox.add(summaryPanel);
        content.add(topBox, BorderLayout.NORTH);

        // Центр: карточки курсов
        coursesPanel = new JPanel();
        coursesPanel.setLayout(new BoxLayout(coursesPanel, BoxLayout.X_AXIS));
        coursesPanel.setOpaque(false);
        rebuildCoursesPanel();

        JScrollPane scroll = new JScrollPane(coursesPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getHorizontalScrollBar().setUnitIncrement(20);
        scroll.setBackground(Theme.BG_DARK);
        scroll.getViewport().setBackground(Theme.BG_DARK);
        ui.components.DarkScrollBarUI.apply(scroll.getVerticalScrollBar());
        ui.components.DarkScrollBarUI.apply(scroll.getHorizontalScrollBar());
        content.add(scroll, BorderLayout.CENTER);

        // Низ: статистика
        content.add(createFooter(), BorderLayout.SOUTH);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));

        // Сохраняем при закрытии
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                repo.save();
            }
        });

        setVisible(true);
    }

    // ============ HEADER ============
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Прогресс по курсам");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Визуализация задач · Правый клик по курсу — меню");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        JPanel textBox = new JPanel();
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setOpaque(false);
        textBox.add(title);
        textBox.add(Box.createVerticalStrut(4));
        textBox.add(subtitle);
        header.add(textBox, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        // Кнопка "+ Добавить курс"
        ui.components.StyledButton addBtn =
                new ui.components.StyledButton("+ Добавить курс", Theme.SUCCESS);
        addBtn.addActionListener(e -> showAddCourseDialog());
        right.add(addBtn);

        // Легенда
        JLabel lessLabel = new JLabel("Меньше");
        lessLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lessLabel.setForeground(Theme.TEXT_SECONDARY);
        right.add(lessLabel);

        Color[] levels = { Theme.PINK_DIM, Theme.LEVEL_1, Theme.LEVEL_2, Theme.LEVEL_3, Theme.LEVEL_4 };
        for (Color lvl : levels) {
            JPanel dot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 4, 4));
                    g2.dispose();
                }
            };
            dot.setPreferredSize(new Dimension(12, 12));
            dot.setBackground(lvl);
            dot.setOpaque(false);
            right.add(dot);
        }

        JLabel moreLabel = new JLabel("Больше");
        moreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        moreLabel.setForeground(Theme.TEXT_SECONDARY);
        right.add(moreLabel);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ============ FOOTER ============
    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        ui.components.StyledButton statsBtn =
                new ui.components.StyledButton("Статистика по задачам в день",
                        new Color(33, 38, 45));
        statsBtn.addActionListener(e -> {
            StatsDialog dlg = new StatsDialog(this, repo.getAll());
            dlg.setVisible(true);
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(statsBtn);
        footer.add(left, BorderLayout.WEST);
        return footer;
    }

    // ============ ПЕРЕСБОРКА КАРТОЧЕК ============
    private void rebuildCoursesPanel() {
        coursesPanel.removeAll();

        for (Course c : repo.getAll()) {
            CourseCard card = new CourseCard(
                    c,
                    // onChange — клик по ячейке
                    course -> {
                        repo.save();
                        if (summaryPanel != null) summaryPanel.refresh();
                    },
                    // onContext — правый клик по карточке
                    this::showCourseMenu
            );
            coursesPanel.add(card);
            coursesPanel.add(Box.createHorizontalStrut(16));
        }

        coursesPanel.revalidate();
        coursesPanel.repaint();
    }

    // ============ МЕНЮ КУРСА ============
    private void showCourseMenu(Course course) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Theme.BG_CARD);
        menu.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        menu.setOpaque(true);

        // Отключаем дефолтную отрисовку фона — рисуем сами
        menu.setUI(new javax.swing.plaf.basic.BasicPopupMenuUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRect(0, 0, c.getWidth(), c.getHeight());
                g2.setColor(Theme.BORDER);
                g2.drawRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);
                g2.dispose();
            }
        });

        JMenuItem edit = new JMenuItem("Изменить курс");
        styleMenuItem(edit);
        edit.addActionListener(e -> showEditCourseDialog(course));
        menu.add(edit);

        JMenuItem reset = new JMenuItem("Сбросить прогресс");
        styleMenuItem(reset);
        reset.addActionListener(e -> {
            course.resetProgress();
            repo.save();
            summaryPanel.refresh();
            rebuildCoursesPanel();
        });
        menu.add(reset);

        menu.addSeparator();

        JMenuItem delete = new JMenuItem("Удалить курс");
        styleMenuItem(delete);
        delete.setForeground(Theme.DANGER);
        delete.addActionListener(e -> confirmDelete(course));
        menu.add(delete);

        Point mouse = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mouse, this);
        menu.show(this, mouse.x, mouse.y);
    }

    private void styleMenuItem(JMenuItem item) {
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.setForeground(Theme.TEXT_PRIMARY);
        item.setBackground(Theme.BG_CARD);
        item.setOpaque(true);
        item.setBorder(new EmptyBorder(8, 16, 8, 16));

        // Отключаем дефолтную отрисовку пункта меню (иначе будет серый ховер от системы)
        item.setUI(new javax.swing.plaf.basic.BasicMenuItemUI() {
            @Override
            protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                if (menuItem.isArmed()) {
                    g.setColor(new Color(45, 50, 58));
                    g.fillRect(0, 0, menuItem.getWidth(), menuItem.getHeight());
                } else {
                    g.setColor(Theme.BG_CARD);
                    g.fillRect(0, 0, menuItem.getWidth(), menuItem.getHeight());
                }
            }
            @Override
            protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(menuItem.getFont());
                g2.setColor(menuItem.getForeground());
                g2.drawString(text, textRect.x, textRect.y + g2.getFontMetrics().getAscent());
                g2.dispose();
            }
        });
    }


    // ============ ДИАЛОГИ ============
    private void showAddCourseDialog() {
        CourseDialog dlg = new CourseDialog(this, null, () -> {
            repo.save();
            summaryPanel.refresh();
            rebuildCoursesPanel();
        });
        // Колбэк: что делать при создании нового курса
        dlg.setOnCreate(course -> repo.add(course));
        dlg.setVisible(true);
    }

    private void showEditCourseDialog(Course course) {
        CourseDialog dlg = new CourseDialog(this, course, () -> {
            repo.save();
            summaryPanel.refresh();
            rebuildCoursesPanel();
        });
        // При редактировании onCreate не нужен — курс уже есть
        dlg.setVisible(true);
    }

    private void confirmDelete(Course course) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Удалить курс \"" + course.getName() + "\"?",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (result == JOptionPane.YES_OPTION) {
            repo.remove(course);
            repo.save();
            summaryPanel.refresh();
            rebuildCoursesPanel();
        }
    }

    // ============ MAIN ============
    public static void main(String[] args) {
        UIManager.put("PopupMenu.background", Theme.BG_CARD);
        UIManager.put("PopupMenu.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("MenuItem.background", Theme.BG_CARD);
        UIManager.put("MenuItem.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("MenuItem.selectionBackground", new Color(45, 50, 58));
        UIManager.put("MenuItem.selectionForeground", Theme.TEXT_PRIMARY);
        UIManager.put("Menu.separatorColor", Theme.BORDER);
        UIManager.put("Separator.foreground", Theme.BORDER);
        UIManager.put("Separator.background", Theme.BG_CARD);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("ToolTip.background", Theme.BG_CARD_HOVER);
            UIManager.put("ToolTip.foreground", Theme.TEXT_PRIMARY);
            UIManager.put("ToolTip.border", BorderFactory.createLineBorder(Theme.BORDER));
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(CourseProgressApp::new);
    }
}