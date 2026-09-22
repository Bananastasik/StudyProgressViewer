import model.Course;
import model.CourseRepository;
import ui.components.CourseCard;
import ui.components.DarkScrollBarUI;
import ui.components.StyledButton;
import ui.components.SummaryPanel;
import ui.components.TitleBar;
import ui.dialogs.CourseDialog;
import ui.dialogs.StatsDialog;
import ui.theme.Theme;
import ui.theme.ThemeManager;
import util.UndoManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CourseProgressApp extends JFrame {

    private final CourseRepository repo = new CourseRepository();
    private final UndoManager undoManager = new UndoManager();

    private JPanel coursesPanel;
    private SummaryPanel summaryPanel;
    private JTextField searchField;
    private JLabel sortLabel;
    private JLabel archiveLabel;
    private boolean showArchive = false;
    private String currentSort = "Порядок";

    private CourseCard draggingCard;
    private Course draggingCourse;
    private JWindow dragGhost;
    private Timer dragTimer;

    private Timer autoSaveTimer;

    public CourseProgressApp() {
        setTitle("Прогресс курсов");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(1320, 940);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        ThemeManager.setOnThemeChange(this::rebuildAll);

        buildUI();
        installHotkeys();

        autoSaveTimer = new Timer(3 * 60 * 1000, e -> repo.save());
        autoSaveTimer.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                repo.save();
            }
        });

        setVisible(true);
    }

    private void buildUI() {
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

        root.add(new TitleBar(this, "Прогресс курсов", this::rebuildAll), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(20, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
        topBox.setOpaque(false);
        topBox.add(createHeader());
        topBox.add(Box.createVerticalStrut(12));
        topBox.add(createToolbar());
        topBox.add(Box.createVerticalStrut(12));

        summaryPanel = new SummaryPanel(repo);
        topBox.add(summaryPanel);
        content.add(topBox, BorderLayout.NORTH);

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
        DarkScrollBarUI.apply(scroll.getVerticalScrollBar());
        DarkScrollBarUI.apply(scroll.getHorizontalScrollBar());
        content.add(scroll, BorderLayout.CENTER);

        content.add(createFooter(), BorderLayout.SOUTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
    }

    private void rebuildAll() {
        getContentPane().removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Прогресс по курсам");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Theme.TEXT_PRIMARY);


        JPanel textBox = new JPanel();
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setOpaque(false);
        textBox.add(title);
        header.add(textBox, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        StyledButton addBtn = new StyledButton("+ Добавить курс", Theme.SUCCESS);
        addBtn.addActionListener(e -> showAddCourseDialog());
        right.add(addBtn);

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

    private JPanel createToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel searchLabel = new JLabel("Поиск:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchLabel.setForeground(Theme.TEXT_SECONDARY);
        left.add(searchLabel);

        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setForeground(Theme.TEXT_PRIMARY);
        searchField.setBackground(Theme.BG_INPUT);
        searchField.setCaretColor(Theme.TEXT_PRIMARY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        searchField.setToolTipText("Поиск по названию курса");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { rebuildCoursesPanel(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { rebuildCoursesPanel(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { rebuildCoursesPanel(); }
        });
        left.add(searchField);

        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JLabel sortTitle = new JLabel("Сортировка:");
        sortTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sortTitle.setForeground(Theme.TEXT_SECONDARY);
        right.add(sortTitle);

        sortLabel = new JLabel(currentSort);
        sortLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sortLabel.setForeground(Theme.TEXT_PRIMARY);

        JButton sortBtn = createDropdownButton(sortLabel);
        sortBtn.addActionListener(e -> showSortMenu(sortBtn));
        right.add(sortBtn);

        archiveLabel = new JLabel("Архив");
        archiveLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        archiveLabel.setForeground(Theme.TEXT_PRIMARY);

        JButton archiveBtn = createDropdownButton(archiveLabel);
        archiveBtn.addActionListener(e -> {
            showArchive = !showArchive;
            updateArchiveButton(archiveLabel);
            archiveBtn.repaint();
            rebuildCoursesPanel();
        });
        right.add(archiveBtn);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JButton createDropdownButton(JLabel innerLabel) {
        JButton btn = new JButton() {
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
                g2.setColor(hover ? Theme.BG_CARD_HOVER : Theme.BG_INPUT);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 6));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.add(innerLabel);
        return btn;
    }

    private void updateArchiveButton(JLabel label) {
        if (showArchive) {
            label.setForeground(Theme.ACCENT);
            label.setText("Архив: вкл");
        } else {
            label.setForeground(Theme.TEXT_PRIMARY);
            label.setText("Архив");
        }
    }

    private void showSortMenu(JButton anchor) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Theme.BG_CARD);
        menu.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));

        menu.setUI(new javax.swing.plaf.basic.BasicPopupMenuUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_CARD);
                g2.fillRect(0, 0, c.getWidth(), c.getHeight());
                g2.setColor(Theme.BORDER);
                g2.drawRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);
                g2.dispose();
            }
        });

        String[] options = {
                "Порядок", "По имени", "По проценту (убыв.)", "По проценту (возр.)",
                "По остатку (убыв.)", "По остатку (возр.)"
        };

        for (String opt : options) {
            JMenuItem item = new JMenuItem(opt + (opt.equals(currentSort) ? "  ●" : ""));
            styleMenuItem(item);
            item.addActionListener(e -> {
                currentSort = opt;
                sortLabel.setText(opt);
                sortLabel.repaint();
                anchor.repaint();
                rebuildCoursesPanel();
            });
            menu.add(item);
        }

        menu.show(anchor, 0, anchor.getHeight());
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        Color footerBtnBg = ui.theme.ThemeManager.isDark()
                ? new Color(33, 38, 45)
                : new Color(215, 228, 240);
        StyledButton statsBtn = new StyledButton("Статистика по задачам в день", footerBtnBg);
        statsBtn.addActionListener(e -> {
            StatsDialog dlg = new StatsDialog(this, repo);
            dlg.setVisible(true);
            summaryPanel.refresh();
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(statsBtn);

        footer.add(left, BorderLayout.WEST);
        return footer;
    }

    private void rebuildCoursesPanel() {
        coursesPanel.removeAll();

        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";

        List<Course> toShow = new ArrayList<>();
        List<Course> source = showArchive ? repo.getArchived() : repo.getActive();
        for (Course c : source) {
            if (query.isEmpty() || c.getName().toLowerCase().contains(query)) {
                toShow.add(c);
            }
        }

        if (currentSort != null) {
            switch (currentSort) {
                case "По имени":
                    toShow.sort(Comparator.comparing(Course::getName, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "По проценту (убыв.)":
                    toShow.sort((a, b) -> Double.compare(b.getPercent(), a.getPercent()));
                    break;
                case "По проценту (возр.)":
                    toShow.sort(Comparator.comparingDouble(Course::getPercent));
                    break;
                case "По остатку (убыв.)":
                    toShow.sort((a, b) -> Integer.compare(b.getLeft(), a.getLeft()));
                    break;
                case "По остатку (возр.)":
                    toShow.sort(Comparator.comparingInt(Course::getLeft));
                    break;
            }
        }

        for (Course c : toShow) {
            CourseCard card = new CourseCard(
                    c,
                    repo,
                    course -> {
                        repo.save();
                        if (summaryPanel != null) summaryPanel.refresh();
                    },
                    this::showCourseMenu
            );

            card.setOnDragStart(() -> startDrag(card));
            card.setOnDragEnd(() -> endDrag());

            coursesPanel.add(card);
            coursesPanel.add(Box.createHorizontalStrut(16));
        }

        coursesPanel.revalidate();
        coursesPanel.repaint();
    }

    private void startDrag(CourseCard card) {
        undoManager.push(repo.getAll());
        this.draggingCard = card;
        this.draggingCourse = card.getCourse();
        createDragGhost();
    }

    private void endDrag() {
        if (draggingCourse == null) { cleanupDrag(); return; }

        Point mouse = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mouse, coursesPanel);

        int targetIndex = findCardIndexAt(mouse.x);

        if (targetIndex >= 0) {
            int fromIndex = repo.getAll().indexOf(draggingCourse);
            if (fromIndex != targetIndex && fromIndex >= 0) {
                repo.move(fromIndex, targetIndex);
                repo.save();
                rebuildAll();
            }
        }
        cleanupDrag();
    }

    private int findCardIndexAt(int x) {
        for (int i = 0; i < coursesPanel.getComponentCount(); i++) {
            Component comp = coursesPanel.getComponent(i);
            if (comp instanceof CourseCard) {
                Rectangle bounds = comp.getBounds();
                if (x >= bounds.x - 8 && x <= bounds.x + bounds.width + 8) {
                    return repo.getAll().indexOf(((CourseCard) comp).getCourse());
                }
            }
        }
        return -1;
    }

    private void createDragGhost() {
        dragGhost = new JWindow(this);
        JPanel ghost = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(Theme.ACCENT);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 16, 16));
                g2.dispose();
            }
        };
        ghost.setOpaque(false);
        ghost.setLayout(new BorderLayout());
        ghost.setPreferredSize(new Dimension(draggingCard.getWidth(), 80));

        JLabel label = new JLabel(draggingCourse.getName(), SwingConstants.CENTER);
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ghost.add(label, BorderLayout.CENTER);

        dragGhost.setContentPane(ghost);
        dragGhost.setSize(draggingCard.getWidth(), 80);
        dragGhost.setBackground(new Color(0, 0, 0, 0));
        dragGhost.setAlwaysOnTop(true);

        dragTimer = new Timer(16, e -> {
            Point p = MouseInfo.getPointerInfo().getLocation();
            dragGhost.setLocation(p.x - dragGhost.getWidth() / 2, p.y - 40);
        });
        dragTimer.start();

        dragGhost.setVisible(true);
        try { dragGhost.setOpacity(0.85f); } catch (Exception ignored) {}
    }

    private void cleanupDrag() {
        if (dragTimer != null) { dragTimer.stop(); dragTimer = null; }
        if (dragGhost != null) { dragGhost.dispose(); dragGhost = null; }
        draggingCard = null;
        draggingCourse = null;
    }

    private void showCourseMenu(Course course) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Theme.BG_CARD);
        menu.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        menu.setOpaque(true);

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
            undoManager.push(repo.getAll());
            course.resetProgress();
            repo.save();
            rebuildAll();
        });
        menu.add(reset);

        JMenuItem arch = new JMenuItem(course.isArchived() ? "Вернуть из архива" : "В архив");
        styleMenuItem(arch);
        arch.addActionListener(e -> {
            undoManager.push(repo.getAll());
            course.setArchived(!course.isArchived());
            repo.save();
            rebuildAll();
        });
        menu.add(arch);

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

        item.setUI(new javax.swing.plaf.basic.BasicMenuItemUI() {
            @Override
            protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                if (menuItem.isArmed()) {
                    g.setColor(Theme.BG_CARD_HOVER);
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

    private void showAddCourseDialog() {
        undoManager.push(repo.getAll());
        CourseDialog dlg = new CourseDialog(this, null, () -> {
            repo.save();
            rebuildAll();
        });
        dlg.setOnCreate(course -> repo.add(course));
        dlg.setVisible(true);
    }

    private void showEditCourseDialog(Course course) {
        undoManager.push(repo.getAll());
        CourseDialog dlg = new CourseDialog(this, course, () -> {
            repo.save();
            rebuildAll();
        });
        dlg.setVisible(true);
    }

    private void confirmDelete(Course course) {
        JDialog dlg = new JDialog(this, "Подтверждение", true);
        dlg.setUndecorated(true);
        dlg.setSize(400, 220);
        dlg.setLocationRelativeTo(this);
        dlg.setBackground(new Color(0, 0, 0, 0));

        JPanel r = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_DARK);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        r.setOpaque(false);
        r.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));

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

        JLabel tl = new JLabel("Подтверждение");
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
                int cx = getWidth() / 2, cy = getHeight() / 2, rr = 5;
                g2.drawLine(cx - rr, cy - rr, cx + rr, cy + rr);
                g2.drawLine(cx - rr, cy + rr, cx + rr, cy - rr);
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
        r.add(tbar, BorderLayout.NORTH);

        JPanel msg = new JPanel();
        msg.setLayout(new BoxLayout(msg, BoxLayout.Y_AXIS));
        msg.setOpaque(false);
        msg.setBorder(new EmptyBorder(24, 24, 16, 24));

        JLabel header = new JLabel("Удалить курс?");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(Theme.TEXT_PRIMARY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        msg.add(header);
        msg.add(Box.createVerticalStrut(12));

        JLabel text = new JLabel("<html>Курс <b>\"" + course.getName() + "\"</b> будет удалён.<br>Это действие нельзя отменить.</html>");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setForeground(Theme.TEXT_SECONDARY);
        text.setAlignmentX(Component.LEFT_ALIGNMENT);
        msg.add(text);
        r.add(msg, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(0, 20, 12, 20));

        Color delDlgBg = ui.theme.ThemeManager.isDark()
                ? new Color(50, 55, 62)
                : new Color(215, 228, 240);
        StyledButton cancel = new StyledButton("Отмена", delDlgBg);
        cancel.addActionListener(e -> dlg.dispose());
        buttons.add(cancel);

        StyledButton ok = new StyledButton("Удалить", Theme.DANGER);
        ok.addActionListener(e -> {
            undoManager.push(repo.getAll());
            repo.remove(course);
            repo.save();
            rebuildAll();
            dlg.dispose();
        });
        buttons.add(ok);

        r.add(buttons, BorderLayout.SOUTH);
        dlg.setContentPane(r);
        dlg.setShape(new RoundRectangle2D.Double(0, 0, dlg.getWidth(), dlg.getHeight(), 14, 14));
        dlg.setVisible(true);
    }

    private void installHotkeys() {
        JRootPane rp = getRootPane();
        InputMap im = rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rp.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "undo");
        am.put("undo", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                List<Course> snap = undoManager.pop();
                if (snap == null) return;
                repo.restore(snap);
                repo.save();
                rebuildAll();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "newCourse");
        am.put("newCourse", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { showAddCourseDialog(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "save");
        am.put("save", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { repo.save(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "search");
        am.put("search", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (searchField != null) searchField.requestFocusInWindow();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        am.put("clearSearch", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (searchField != null) searchField.setText("");
            }
        });
    }

    public static void main(String[] args) {
        UIManager.put("PopupMenu.background", Theme.BG_CARD);
        UIManager.put("PopupMenu.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("MenuItem.background", Theme.BG_CARD);
        UIManager.put("MenuItem.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("MenuItem.selectionBackground", Theme.BG_CARD_HOVER);
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