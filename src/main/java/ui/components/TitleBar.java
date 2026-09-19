package ui.components;

import ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class TitleBar extends JPanel {

    public TitleBar(JFrame parent, String titleText) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_TITLEBAR);
        setPreferredSize(new Dimension(0, 36));
        setBorder(new EmptyBorder(0, 16, 0, 8));

        final Point[] dragOffset = {null};
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragOffset[0] = e.getPoint(); }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
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

        buttons.add(createMinimizeButton(parent));
        buttons.add(createCloseButton(parent));

        add(buttons, BorderLayout.EAST);
    }

    private JButton createMinimizeButton(JFrame parent) {
        JButton btn = new IconButton(false);
        btn.addActionListener(e -> parent.setState(Frame.ICONIFIED));
        return btn;
    }

    private JButton createCloseButton(JFrame parent) {
        JButton btn = new IconButton(true);
        btn.addActionListener(e -> {
            // Сохранение перед выходом делает вызывающий код
            System.exit(0);
        });
        return btn;
    }

    // Кнопка с иконкой «минус» или «крестик»
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
                g2.setColor(isClose ? new Color(200, 40, 60) : new Color(45, 50, 58));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.setColor(Color.WHITE);
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