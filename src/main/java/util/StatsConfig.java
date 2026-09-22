package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class StatsConfig {

    private static final String CONFIG_FILE =
            System.getProperty("user.home") + File.separator + "study-dates.properties";

    private LocalDate start = LocalDate.of(2026, 1, 1);
    private LocalDate deadline = LocalDate.of(2027, 8, 1);

    public StatsConfig() {
        load();
    }

    public LocalDate getStart() { return start; }
    public LocalDate getDeadline() { return deadline; }

    public void setStart(LocalDate start) { this.start = start; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public void save() {
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8))) {
            w.println("start=" + start);
            w.println("deadline=" + deadline);
        } catch (Exception e) {
            System.err.println("Не удалось сохранить даты: " + e.getMessage());
        }
    }

    private void load() {
        File f = new File(CONFIG_FILE);
        if (!f.exists()) return;

        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith("start=")) {
                    try { start = LocalDate.parse(line.substring(6).trim()); } catch (Exception ignored) {}
                } else if (line.startsWith("deadline=")) {
                    try { deadline = LocalDate.parse(line.substring(9).trim()); } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            System.err.println("Не удалось загрузить даты: " + e.getMessage());
        }
    }
}