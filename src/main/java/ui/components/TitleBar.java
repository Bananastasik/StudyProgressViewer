package ui.components;

import ui.theme.Theme;
import ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;

public class TitleBar extends JPanel {

    public TitleBar(JFrame parent, String titleText, Runnable onThemeToggle) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_TITLEBAR);
        setPreferredSize(new Dimension(0, 36));
        setBorder(new EmptyBorder(0, 16, 0, 8));

        final Point[] dragOffset = {null};
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragOffset[0] = e.getPoint(); }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset[0] != null) {
                    Point p = e.getLocationOnScreen();
                    parent.setLocation(p.x - dragOffset[0].x, p.y - dragOffset[0].y);
                }
            }
        });

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(Theme.TEXT_SECONDARY);
        add(title, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttons.setBackground(Theme.BG_TITLEBAR);

        // ===== Свитч темы =====
        ThemeSwitch themeSwitch = new ThemeSwitch();
        themeSwitch.addActionListener(e -> {
            ThemeManager.toggle();
            if (onThemeToggle != null) onThemeToggle.run();
        });
        buttons.add(themeSwitch);

        IconButton minimize = new IconButton(false);
        minimize.addActionListener(e -> parent.setState(Frame.ICONIFIED));
        buttons.add(minimize);

        IconButton close = new IconButton(true);
        close.addActionListener(e -> System.exit(0));
        buttons.add(close);

        add(buttons, BorderLayout.EAST);
    }

    // ===== Свитч тёмная/светлая =====
    private static class ThemeSwitch extends JButton {
        private boolean hover = false;
        private float anim = 0f;      // 0 = тёмная, 1 = светлая
        private Timer animTimer;
        private boolean lastState;

        ThemeSwitch() {
            setPreferredSize(new Dimension(60, 36));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Переключить тему");

            lastState = ThemeManager.isDark();
            anim = lastState ? 0f : 1f;

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });

            animTimer = new Timer(16, e -> {
                float target = ThemeManager.isDark() ? 0f : 1f;
                float diff = target - anim;
                if (Math.abs(diff) < 0.02f) {
                    anim = target;
                    animTimer.stop();
                } else {
                    anim += diff * 0.2f;
                }
                repaint();
            });

            // Дёргаем анимацию при клике
            addActionListener(e -> {
                if (!animTimer.isRunning()) animTimer.start();
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int sw = 40;               // ширина свитча
            int sh = 20;               // высота
            int sx = (w - sw) / 2;
            int sy = (h - sh) / 2;

            // Трек
            Color trackDark = new Color(50, 55, 65);
            Color trackLight = new Color(210, 225, 240);
            Color track = blend(trackDark, trackLight, anim);

            if (hover) track = track.brighter();
            g2.setColor(track);
            g2.fill(new RoundRectangle2D.Double(sx, sy, sw, sh, sh, sh));

            // Кружок
            int circleD = sh - 4;
            int circleX = (int) (sx + 2 + anim * (sw - circleD - 4));
            int circleY = sy + 2;

            // Солнце / луна — значок внутри кружка
            g2.setColor(new Color(255, 255, 255));
            g2.fillOval(circleX, circleY, circleD, circleD);

            // Внутри кружка: точка или ореол
            g2.setColor(new Color(240, 200, 80));  // жёлтое солнце
            int dotD = (int) (circleD * 0.5);
            g2.fillOval(circleX + (circleD - dotD) / 2, circleY + (circleD - dotD) / 2, dotD, dotD);

            g2.dispose();
        }

        private Color blend(Color a, Color b, float t) {
            int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
            int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
            int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
            return new Color(r, g, bl);
        }
    }

    private static class IconButton extends JButton {
        private final boolean isClose;
        private boolean hover = false;

        IconButton(boolean isClose) {
            this.isClose = isClose;
            setPreferredSize(new Dimension(46, 36));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (hover) {
                g2.setColor(isClose ? new Color(200, 40, 60) : new Color(70, 80, 95));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2, cy = getHeight() / 2;
            if (isClose) {
                int r = 5;
                g2.drawLine(cx - r, cy - r, cx + r, cy + r);
                g2.drawLine(cx - r, cy + r, cx + r, cy - r);
            } else {
                g2.drawLine(cx - 5, cy, cx + 5, cy);
            }
            g2.dispose();
        }
    }
}