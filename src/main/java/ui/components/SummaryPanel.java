package ui.components;

import model.Course;
import model.CourseRepository;
import model.DailyStats;
import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SummaryPanel extends JPanel {

    private static final int SUMMARY_ROWS = 10;
    private static final int SUMMARY_CELLS = 80 * 24;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final CourseRepository repo;

    private JLabel subLabel;
    private JLabel percentLabel;
    private JPanel gridPanel;
    private JPanel piesContainer;
    private JPanel piesPanel;

    private JLabel forecastValue;

    public SummaryPanel(CourseRepository repo) {
        this.repo = repo;

        setLayout(new BorderLayout(20, 8));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        add(createTitleBox(), BorderLayout.WEST);

        gridPanel = createSummaryGrid();
        add(gridPanel, BorderLayout.CENTER);

        add(createRightColumn(), BorderLayout.EAST);

        piesContainer = new JPanel(new BorderLayout());
        piesContainer.setOpaque(false);
        piesPanel = createPiesPanel();
        piesContainer.add(piesPanel, BorderLayout.CENTER);
        add(piesContainer, BorderLayout.SOUTH);
    }

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

    private JPanel createTitleBox() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(200, 140));

        JLabel title = new JLabel("Общий прогресс");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        int totalDone = repo.getTotalDone();
        int totalAll = repo.getTotalAll();

        subLabel = new JLabel(totalDone + " из " + totalAll + " задач");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(Theme.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        percentLabel = new JLabel(formatPercent(totalDone, totalAll));
        percentLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        percentLabel.setForeground(Theme.LEVEL_4);
        percentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.add(title);
        box.add(Box.createVerticalStrut(4));
        box.add(subLabel);
        box.add(Box.createVerticalStrut(8));
        box.add(percentLabel);
        return box;
    }

    private String formatPercent(int done, int all) {
        if (all == 0) return "0%";
        return String.format("%.2f", done * 100.0 / all).replace('.', ',') + "%";
    }

    private JPanel createRightColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        col.setPreferredSize(new Dimension(230, 140));
        col.setBorder(new EmptyBorder(0, 16, 0, 0));

        forecastValue = new JLabel();
        col.add(metricRow("Прогноз финиша", forecastValue, Theme.TEXT_PRIMARY));

        return col;
    }

    private JPanel metricRow(String label, JLabel valueLabel, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(Theme.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valueLabel.setForeground(valueColor);

        row.add(l, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel createSummaryGrid() {
        int cols = (int) Math.ceil(SUMMARY_CELLS / (double) SUMMARY_ROWS);
        JPanel grid = new JPanel(new GridLayout(SUMMARY_ROWS, cols, 3, 3));
        grid.setOpaque(false);

        int totalDone = repo.getTotalDone();
        int totalAll = repo.getTotalAll();
        double ratio = totalAll == 0 ? 0 : totalDone / (double) totalAll;
        int doneCells = (int) Math.round(ratio * SUMMARY_CELLS);

        for (int i = 0; i < SUMMARY_CELLS; i++) {
            boolean isDone = i < doneCells;
            Color color = computeSummaryColor(i, doneCells, isDone);
            grid.add(createSummaryCell(color));
        }
        return grid;
    }

    private Color computeSummaryColor(int i, int doneCells, boolean isDone) {
        if (!isDone) return Theme.PINK_DIM;
        double pos = i / (double) Math.max(1, doneCells);
        if (pos < 0.25) return Theme.LEVEL_1;
        if (pos < 0.5) return Theme.LEVEL_2;
        if (pos < 0.75) return Theme.LEVEL_3;
        return Theme.LEVEL_4;
    }

    private JPanel createSummaryCell(Color initial) {
        JPanel cell = new JPanel() {
            Color color = initial;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 3, 3));
                g2.dispose();
            }
            @Override public void setBackground(Color bg) {
                super.setBackground(bg);
                this.color = bg;
                repaint();
            }
        };
        cell.setOpaque(false);
        cell.setBackground(initial);
        return cell;
    }

    private JPanel createPiesPanel() {
        List<Course> courses = repo.getActive();
        int total = Math.max(1, courses.size());
        int cols = Math.min(total, 8);
        int rows = (int) Math.ceil(total / (double) cols);

        JPanel panel = new JPanel(new GridLayout(rows, cols, 14, 14));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 140 * rows));
        panel.setBorder(new EmptyBorder(14, 0, 4, 0));

        for (int i = 0; i < total; i++) {
            Course c = courses.get(i);
            Color color = Theme.PIE_COLORS[i % Theme.PIE_COLORS.length];
            panel.add(createPieCard(c, color));
        }

        for (int i = total; i < rows * cols; i++) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            panel.add(empty);
        }
        return panel;
    }

    private JPanel createPieCard(Course c, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD_HOVER);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));

        PieChart pie = new PieChart(c.getPercent(), color);
        card.add(pie, BorderLayout.CENTER);

        JLabel name = new JLabel(c.getName(), SwingConstants.CENTER);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        name.setForeground(Theme.TEXT_SECONDARY);
        card.add(name, BorderLayout.SOUTH);

        return card;
    }

    public void rebuildPies() {
        if (piesContainer == null) return;
        if (piesPanel != null) piesContainer.remove(piesPanel);

        piesPanel = createPiesPanel();
        piesContainer.add(piesPanel, BorderLayout.CENTER);
        piesContainer.revalidate();
        piesContainer.repaint();
        revalidate();
        repaint();
    }

    public void refresh() {
        int totalDone = repo.getTotalDone();
        int totalAll = repo.getTotalAll();

        subLabel.setText(totalDone + " из " + totalAll + " задач");
        percentLabel.setText(formatPercent(totalDone, totalAll));

        int totalCells = gridPanel.getComponentCount();
        double ratio = totalAll == 0 ? 0 : totalDone / (double) totalAll;
        int doneCells = (int) Math.round(ratio * totalCells);

        for (int i = 0; i < totalCells; i++) {
            Component comp = gridPanel.getComponent(i);
            if (comp instanceof JPanel) {
                ((JPanel) comp).setBackground(
                        computeSummaryColor(i, doneCells, i < doneCells));
            }
        }

        List<Course> courses = repo.getActive();
        if (piesPanel != null) {
            Component[] pieCards = piesPanel.getComponents();
            for (int i = 0; i < Math.min(courses.size(), pieCards.length); i++) {
                if (pieCards[i] instanceof JPanel) {
                    JPanel card = (JPanel) pieCards[i];
                    for (Component inner : card.getComponents()) {
                        if (inner instanceof PieChart) {
                            ((PieChart) inner).setPercent(courses.get(i).getPercent());
                        }
                    }
                }
            }
        }

        DailyStats stats = repo.getStats();
        int left = repo.getTotalLeft();
        LocalDate forecast = stats.predictFinishDate(left);
        forecastValue.setText(forecast == null ? "—" : forecast.format(FMT));
    }
}