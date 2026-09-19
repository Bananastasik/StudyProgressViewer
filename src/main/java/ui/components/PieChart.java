package ui.components;

import ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

/**
 * Круговая диаграмма прогресса — «бублик» с процентом внутри.
 */
public class PieChart extends JComponent {

    private double percent;      // 0..100
    private Color color;

    public PieChart(double percent, Color color) {
        this.percent = percent;
        this.color = color;
        setPreferredSize(new Dimension(90, 90));
        setOpaque(false);
    }

    public void setPercent(double percent) {
        this.percent = Math.max(0, Math.min(100, percent));
        repaint();
    }

    public void setColor(Color color) {
        this.color = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight()) - 4;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        // Трек (фон)
        g2.setColor(new Color(38, 44, 52));
        g2.fill(new Ellipse2D.Double(x, y, size, size));

        // Заполненная часть
        double angle = percent / 100.0 * 360.0;
        g2.setColor(color);
        g2.fill(new Arc2D.Double(x, y, size, size, 90, -angle, Arc2D.PIE));

        // Внутренний круг — создаёт «бублик»
        int inner = (int) (size * 0.58);
        int ix = x + (size - inner) / 2;
        int iy = y + (size - inner) / 2;
        g2.setColor(Theme.BG_CARD);
        g2.fill(new Ellipse2D.Double(ix, iy, inner, inner));

        // Текст с процентом
        String text = String.format("%.1f", percent).replace('.', ',') + "%";
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(text)) / 2;
        int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.drawString(text, tx, ty);

        g2.dispose();
    }
}