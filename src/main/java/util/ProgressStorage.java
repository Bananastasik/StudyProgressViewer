package util;

import model.Course;

import java.io.*;
import java.util.List;

public class ProgressStorage {

    private static final String SAVE_FILE =
            System.getProperty("user.home") + File.separator + "progress.properties";

    public static void save(List<Course> courses) {
        try (PrintWriter w = new PrintWriter(SAVE_FILE, "UTF-8")) {
            for (Course c : courses) {
                w.println(c.getName() + "=" + c.getDone() + "/" + c.getTotal());
            }
        } catch (Exception e) {
            System.err.println("Не удалось сохранить: " + e.getMessage());
        }
    }

    public static void loadInto(List<Course> courses) {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return;

        try (BufferedReader r = new BufferedReader(new FileReader(f, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                int idx = line.indexOf('=');
                if (idx < 0) continue;

                String name = line.substring(0, idx).trim();
                String rest = line.substring(idx + 1).trim();
                int done = 0, total = 0;

                if (rest.contains("/")) {
                    String[] parts = rest.split("/", 2);
                    done = Integer.parseInt(parts[0].trim());
                    total = Integer.parseInt(parts[1].trim());
                } else {
                    done = Integer.parseInt(rest);
                    for (Course c : courses) {
                        if (c.getName().equals(name)) { total = c.getTotal(); break; }
                    }
                }

                // Обновляем существующий или добавляем новый
                boolean found = false;
                for (Course c : courses) {
                    if (c.getName().equals(name)) {
                        c.setTotal(total);
                        c.setDone(done);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    courses.add(new Course(name, total, done));
                }
            }
        } catch (Exception e) {
            System.err.println("Не удалось загрузить: " + e.getMessage());
        }
    }
}