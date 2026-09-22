package ui.theme;

import java.awt.Color;

public class Theme {

    public static Color BG_DARK;
    public static Color BG_TITLEBAR;
    public static Color BG_CARD;
    public static Color BG_CARD_HOVER;
    public static Color BG_INPUT;
    public static Color BORDER;
    public static Color TEXT_PRIMARY;
    public static Color TEXT_SECONDARY;

    public static Color LEVEL_1;
    public static Color LEVEL_2;
    public static Color LEVEL_3;
    public static Color LEVEL_4;

    public static Color PINK_DIM;
    public static Color ACCENT;
    public static Color DANGER;
    public static Color SUCCESS;
    public static Color WARNING;

    public static Color[] PIE_COLORS;

    static { applyDark(); }

    public static void applyDark() {
        BG_DARK = new Color(13, 17, 23);
        BG_TITLEBAR = new Color(0, 0, 0);
        BG_CARD = new Color(22, 27, 34);
        BG_CARD_HOVER = new Color(28, 33, 40);
        BG_INPUT = new Color(33, 38, 45);
        BORDER = new Color(48, 54, 61);
        TEXT_PRIMARY = new Color(240, 246, 252);
        TEXT_SECONDARY = new Color(139, 148, 158);

        LEVEL_1 = new Color(14, 68, 41);
        LEVEL_2 = new Color(0, 109, 50);
        LEVEL_3 = new Color(38, 166, 65);
        LEVEL_4 = new Color(57, 211, 83);

        PINK_DIM = new Color(60, 30, 45);
        ACCENT = new Color(255, 105, 180);
        DANGER = new Color(220, 60, 80);
        SUCCESS = new Color(60, 170, 90);
        WARNING = new Color(255, 200, 87);

        PIE_COLORS = new Color[]{
                new Color(88, 166, 255),
                new Color(255, 123, 114),
                new Color(210, 168, 255),
                new Color(255, 200, 87),
                new Color(121, 192, 255),
                new Color(87, 217, 163),
                new Color(255, 141, 196),
                new Color(247, 183, 49),
                new Color(160, 120, 255),
                new Color(120, 200, 200)
        };
    }

    public static void applyLight() {
        BG_DARK = new Color(240, 245, 250);
        BG_TITLEBAR = new Color(210, 222, 235);
        BG_CARD = new Color(255, 255, 255);
        BG_CARD_HOVER = new Color(235, 242, 248);
        BG_INPUT = new Color(238, 244, 250);
        BORDER = new Color(190, 205, 220);
        TEXT_PRIMARY = new Color(28, 38, 50);
        TEXT_SECONDARY = new Color(100, 118, 138);

        // Сделано — голубая заливка
        LEVEL_1 = new Color(198, 227, 245);
        LEVEL_2 = new Color(133, 189, 227);
        LEVEL_3 = new Color(82, 148, 200);
        LEVEL_4 = new Color(41, 105, 165);

        // Не сделано — очень бледный (почти белый, с лёгкой голубизной)
        PINK_DIM = new Color(250, 252, 255);

        ACCENT = new Color(70, 130, 190);
        DANGER = new Color(207, 70, 80);
        SUCCESS = new Color(70, 130, 190);      // серо-голубые кнопки
        WARNING = new Color(250, 225, 120);     // жёлтый для "Нужно в день"

        PIE_COLORS = new Color[]{
                new Color(70, 130, 190),
                new Color(220, 90, 90),
                new Color(150, 120, 200),
                new Color(210, 160, 60),
                new Color(90, 160, 220),
                new Color(80, 180, 190),
                new Color(210, 120, 170),
                new Color(200, 140, 80),
                new Color(130, 110, 200),
                new Color(100, 160, 170)
        };
    }

    private Theme() {}
}