package util;

import model.Course;
import model.DailyStats;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public class ProgressStorage {

    private static final String SAVE_FILE =
            System.getProperty("user.home") + File.separator + "progress.properties";

    public static void save(List<Course> courses, DailyStats stats) {
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(SAVE_FILE), StandardCharsets.UTF_8))) {

            for (Course c : courses) {
                w.println("[course]");
                w.println("name=" + c.getName());
                w.println("done=" + c.getDone());
                w.println("total=" + c.getTotal());
                w.println("deadline=" + (c.getDeadline() == null ? "" : c.getDeadline().toString()));
                w.println("archived=" + c.isArchived());
                w.println();
            }

            w.println("[history]");
            w.print(stats.serialize());
        } catch (Exception e) {
            System.err.println("Не удалось сохранить: " + e.getMessage());
        }
    }

    public static void loadInto(List<Course> courses, DailyStats stats) {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return;

        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            List<String> allLines = new java.util.ArrayList<>();
            while ((line = r.readLine()) != null) allLines.add(line);

            // Определяем формат
            boolean legacyFormat = true;
            for (String l : allLines) {
                if (l.startsWith("[course]")) { legacyFormat = false; break; }
            }

            if (legacyFormat) {
                for (String l : allLines) {
                    if (l.trim().isEmpty()) continue;
                    int idx = l.indexOf('=');
                    if (idx < 0) continue;

                    String n = l.substring(0, idx).trim();
                    String rest = l.substring(idx + 1).trim();

                    int d, t;
                    if (rest.contains("/")) {
                        String[] parts = rest.split("/", 2);
                        try {
                            d = Integer.parseInt(parts[0].trim());
                            t = Integer.parseInt(parts[1].trim());
                        } catch (NumberFormatException ex) { continue; }
                    } else {
                        try { d = Integer.parseInt(rest); } catch (NumberFormatException ex) { continue; }
                        t = -1;
                        for (Course c : courses) {
                            if (c.getName().equals(n)) { t = c.getTotal(); break; }
                        }
                        if (t < 0) continue;
                    }

                    boolean found = false;
                    for (Course c : courses) {
                        if (c.getName().equals(n)) {
                            c.setTotal(t);
                            c.setDone(d);
                            found = true;
                            break;
                        }
                    }
                    if (!found) courses.add(new Course(n, t, d));
                }
                return;
            }

            // Новый формат
            courses.clear(); // перезаписываем содержимое из файла
            String section = null;
            String name = null;
            int done = 0, total = 0;
            LocalDate deadline = null;
            boolean archived = false;

            for (int i = 0; i <= allLines.size(); i++) {
                String l = (i < allLines.size()) ? allLines.get(i) : null;

                if (l == null || l.startsWith("[course]")) {
                    if (name != null) {
                        Course c = new Course(name, total, done);
                        c.setDeadline(deadline);
                        c.setArchived(archived);
                        courses.add(c);
                        name = null; done = 0; total = 0; deadline = null; archived = false;
                    }
                    if (l != null) section = "course";
                    continue;
                }

                if (l.startsWith("[history]")) { section = "history"; continue; }
                if (l.trim().isEmpty()) continue;

                if ("course".equals(section)) {
                    int idx = l.indexOf('=');
                    if (idx < 0) continue;
                    String key = l.substring(0, idx).trim();
                    String val = l.substring(idx + 1).trim();

                    switch (key) {
                        case "name": name = val; break;
                        case "done": try { done = Integer.parseInt(val); } catch (Exception ignored) {} break;
                        case "total": try { total = Integer.parseInt(val); } catch (Exception ignored) {} break;
                        case "deadline":
                            if (!val.isEmpty()) {
                                try { deadline = LocalDate.parse(val); } catch (Exception ignored) {}
                            }
                            break;
                        case "archived": archived = Boolean.parseBoolean(val); break;
                    }
                } else if ("history".equals(section)) {
                    stats.loadLine(l);
                }
            }
        } catch (Exception e) {
            System.err.println("Не удалось загрузить: " + e.getMessage());
        }
    }
}