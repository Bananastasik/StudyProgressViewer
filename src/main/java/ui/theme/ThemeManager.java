package ui.theme;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ThemeManager {

    private static final String CONFIG_FILE =
            System.getProperty("user.home") + File.separator + "study-theme.properties";

    public enum Mode { DARK, LIGHT }

    private static Mode current = Mode.DARK;
    private static Runnable onThemeChange;

    static {
        load();
        apply();
    }

    public static Mode getMode() { return current; }
    public static boolean isDark() { return current == Mode.DARK; }

    public static void setOnThemeChange(Runnable r) { onThemeChange = r; }

    public static void toggle() {
        current = (current == Mode.DARK) ? Mode.LIGHT : Mode.DARK;
        apply();
        save();
        if (onThemeChange != null) onThemeChange.run();
    }

    private static void apply() {
        if (current == Mode.DARK) Theme.applyDark();
        else Theme.applyLight();
    }

    private static void save() {
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8))) {
            w.println("mode=" + current.name().toLowerCase());
        } catch (Exception e) {
            System.err.println("Не удалось сохранить тему: " + e.getMessage());
        }
    }

    private static void load() {
        File f = new File(CONFIG_FILE);
        if (!f.exists()) return;

        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith("mode=")) {
                    String mode = line.substring(5).trim();
                    if ("light".equalsIgnoreCase(mode)) current = Mode.LIGHT;
                    else if ("dark".equalsIgnoreCase(mode)) current = Mode.DARK;
                }
            }
        } catch (Exception e) {
            System.err.println("Не удалось загрузить тему: " + e.getMessage());
        }
    }

    private ThemeManager() {}
}