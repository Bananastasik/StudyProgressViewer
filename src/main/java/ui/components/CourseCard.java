package ui.components;

import model.Course;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public class CourseCard extends JPanel {

    private static final int CELLS_PER_ROW = 5;

    private final Course course;
    private final Consumer<Course> onChange;    // вызывается при клике на ячейку
    private final Consumer<Course> onContext;   // вызывается при правом клике

    private JLabel percentLabel;
    private JLabel doneLabel;
    private JLabel leftLabel;
    private java.util.List<JPanel> cellPanels = new java.util.ArrayList<>();

    public CourseCard(Course course,
                      Consumer<Course> onChange,
                      Consumer<Course> onContext) {
        this.course = course;
        this.onChange = onChange;
        this.onContext = onContext;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(16, 14, 16, 14));
        setPreferredSize(new Dimension(240, 620));
        setMaximumSize(new Dimension(240, Integer.MAX_VALUE));
        setMinimumSize(new Dimension(240, 400));

        // Правый клик по всей карточке — вызвать меню
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { maybeShow(e); }
            @Override public void mouseReleased(MouseEvent e) { maybeShow(e); }
            private void maybeShow(MouseEvent e) {
                if (e.isPopupTrigger() && onContext != null) {
                    onContext.accept(course);
                }
            }
        });

        add(createHead(), BorderLayout.NORTH);
        add(createGridWrapper(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    // ============ ФОН КАРТОЧКИ ============
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.BG_CARD);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
        g2.setColor(Theme.BORDER);
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 16, 16));
        g2.dispose();
        super.paintComponent(g);
    }

    // ============ ВЕРХ: имя + процент ============
    private JPanel createHead() {
        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setOpaque(false);

        JLabel nameLabel = new JLabel(course.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nameLabel.setToolTipText("Правый клик — управление курсом");
        nameLabel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { maybeShow(e); }
            @Override public void mouseReleased(MouseEvent e) { maybeShow(e); }
            private void maybeShow(MouseEvent e) {
                if (e.isPopupTrigger() && onContext != null) onContext.accept(course);
            }
        });
        head.add(nameLabel);
        head.add(Box.createVerticalStrut(4));

        percentLabel = new JLabel(formatPercent(course.getPercent()) + " сделано");
        percentLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        percentLabel.setForeground(progressColor(course.getPercent()));
        percentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(percentLabel);

        return head;
    }

    private String formatPercent(double p) {
        return String.format("%.1f", p).replace('.', ',') + "%";
    }


    // Правильная версия — оборачиваем
    private JPanel createGridWrapper() {
        JPanel grid = createCellsGrid();

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBackground(Theme.BG_CARD);
        scroll.getViewport().setBackground(Theme.BG_CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        DarkScrollBarUI.apply(scroll.getVerticalScrollBar());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(12, 0, 12, 0));
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ============ СЕТКА ЯЧЕЕК ============
    private JPanel createCellsGrid() {
        cellPanels.clear();

        int rows = (int) Math.ceil(course.getTotal() / (double) CELLS_PER_ROW);

        JPanel grid = new JPanel();
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
        grid.setOpaque(false);

        for (int row = 0; row < rows; row++) {
            JPanel rowPanel = new JPanel(new BorderLayout(8, 0));
            rowPanel.setOpaque(false);
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
            rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            int idx = row + 1;
            int major = idx / 5 + 1;
            int minor = idx % 5;
            String rowLabelText = major + "." + minor;

            JLabel rowLabel = new JLabel(rowLabelText, SwingConstants.RIGHT);
            rowLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
            rowLabel.setForeground(Theme.TEXT_SECONDARY);
            rowLabel.setPreferredSize(new Dimension(32, 20));
            rowPanel.add(rowLabel, BorderLayout.WEST);

            JPanel cellsRow = new JPanel(new GridLayout(1, CELLS_PER_ROW, 3, 3));
            cellsRow.setOpaque(false);

            for (int col = 0; col < CELLS_PER_ROW; col++) {
                int taskIndex = row * CELLS_PER_ROW + col;
                boolean isActive = taskIndex < course.getTotal();

                JPanel cell;
                if (isActive) {
                    cell = createClickableCell(taskIndex);
                } else {
                    cell = new JPanel();
                    cell.setOpaque(false);
                }
                cellsRow.add(cell);
                cellPanels.add(cell);
            }

            rowPanel.add(cellsRow, BorderLayout.CENTER);
            grid.add(rowPanel);
            grid.add(Box.createVerticalStrut(3));
        }

        return grid;
    }

    // ============ КЛИКАБЕЛЬНАЯ ЯЧЕЙКА ============
    private JPanel createClickableCell(int taskIndex) {
        JPanel cell = new JPanel() {
            private boolean hover = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        course.toggle(taskIndex);

                        // Обновляем свою карточку — цвета и текст
                        refreshLocal();

                        // Сообщаем наверх — обновить сводку и сохранить
                        if (onChange != null) onChange.accept(course);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                boolean isDone = course.getCells().get(taskIndex);
                Color color;
                if (!isDone) {
                    color = Theme.PINK_DIM;
                } else {
                    double pos = taskIndex / (double) Math.max(1, course.getDone());
                    if (pos < 0.25) color = Theme.LEVEL_1;
                    else if (pos < 0.5) color = Theme.LEVEL_2;
                    else if (pos < 0.75) color = Theme.LEVEL_3;
                    else color = Theme.LEVEL_4;
                }
                if (hover) color = brighter(color, 0.45f);

                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 3, 3));
                if (hover) {
                    g2.setColor(Theme.TEXT_PRIMARY);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 3, 3));
                }
                g2.dispose();
            }
        };
        cell.setOpaque(false);
        cell.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cell.setToolTipText(" ");
        return cell;
    }

    // ============ НИЗ: статистика ============
    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);

        footer.add(makeStatLine("Всего", String.valueOf(course.getTotal()), Theme.TEXT_SECONDARY));

        doneLabel = new JLabel(String.valueOf(course.getDone()));
        footer.add(Box.createVerticalStrut(3));
        footer.add(makeStatLineWithLabel("Сделано", doneLabel, Theme.LEVEL_4));

        leftLabel = new JLabel(String.valueOf(course.getLeft()));
        footer.add(Box.createVerticalStrut(3));
        footer.add(makeStatLineWithLabel("Осталось", leftLabel, Theme.ACCENT));

        return footer;
    }

    private JPanel makeStatLine(String label, String value, Color valueColor) {
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(valueColor);
        return makeStatLineWithLabel(label, v, valueColor);
    }

    private JPanel makeStatLineWithLabel(String label, JLabel valueLabel, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(Theme.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valueLabel.setForeground(valueColor);

        row.add(l, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    // ============ ОБНОВЛЕНИЕ ============
    /**
     * Обновляет тексты и цвета ячеек в этой карточке — без пересоздания.
     */
    private void refreshLocal() {
        if (percentLabel != null) {
            percentLabel.setText(formatPercent(course.getPercent()) + " сделано");
            percentLabel.setForeground(progressColor(course.getPercent()));
        }
        if (doneLabel != null) doneLabel.setText(String.valueOf(course.getDone()));
        if (leftLabel != null) leftLabel.setText(String.valueOf(course.getLeft()));
        for (JPanel cell : cellPanels) cell.repaint();
    }

    // ============ ВСПОМОГАТЕЛЬНЫЕ ============
    private Color progressColor(double percent) {
        if (percent < 25) return new Color(248, 81, 73);
        if (percent < 50) return new Color(210, 153, 34);
        if (percent < 75) return new Color(88, 166, 255);
        return new Color(63, 185, 80);
    }

    private Color brighter(Color c, float factor) {
        int r = Math.min(255, (int) (c.getRed() + 255 * factor));
        int g = Math.min(255, (int) (c.getGreen() + 255 * factor));
        int b = Math.min(255, (int) (c.getBlue() + 255 * factor));
        return new Color(r, g, b);
    }
}