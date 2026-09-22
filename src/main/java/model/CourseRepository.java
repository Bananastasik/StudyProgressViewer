package model;

import util.ProgressStorage;

import java.util.ArrayList;
import java.util.List;

public class CourseRepository {

    private final List<Course> courses = new ArrayList<>();
    private final DailyStats stats = new DailyStats();

    public CourseRepository() {
        courses.add(new Course("Java25",      1625, 906));
        courses.add(new Course("SQL",         1525, 125));
        courses.add(new Course("Claude",       350,  15));
        courses.add(new Course("Java старый", 4470, 255));
        courses.add(new Course("Spring 1",    2000,   4));
        courses.add(new Course("Spring 2",    2200,   0));
        courses.add(new Course("Spring 3",    1950,   0));
        courses.add(new Course("Docker",       600,   0));

        ProgressStorage.loadInto(courses, stats);
    }

    public List<Course> getAll() { return courses; }

    public List<Course> getActive() {
        List<Course> out = new ArrayList<>();
        for (Course c : courses) if (!c.isArchived()) out.add(c);
        return out;
    }

    public List<Course> getArchived() {
        List<Course> out = new ArrayList<>();
        for (Course c : courses) if (c.isArchived()) out.add(c);
        return out;
    }

    public void add(Course c) { courses.add(c); }
    public void remove(Course c) { courses.remove(c); }

    public void save() {
        ProgressStorage.save(courses, stats);
    }

    public void move(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= courses.size()) return;
        if (toIndex < 0) toIndex = 0;
        if (toIndex >= courses.size()) toIndex = courses.size() - 1;
        if (fromIndex == toIndex) return;

        Course c = courses.remove(fromIndex);
        courses.add(toIndex, c);
    }

    public DailyStats getStats() { return stats; }

    public int getTotalDone() {
        int sum = 0;
        for (Course c : courses) if (!c.isArchived()) sum += c.getDone();
        return sum;
    }

    public int getTotalAll() {
        int sum = 0;
        for (Course c : courses) if (!c.isArchived()) sum += c.getTotal();
        return sum;
    }

    public int getTotalLeft() {
        return getTotalAll() - getTotalDone();
    }

    public List<Course> snapshot() {
        List<Course> copy = new ArrayList<>();
        for (Course c : courses) {
            Course nc = new Course(c.getName(), c.getTotal(), c.getDone());
            nc.setDeadline(c.getDeadline());
            nc.setArchived(c.isArchived());
            copy.add(nc);
        }
        return copy;
    }

    public void restore(List<Course> snapshot) {
        courses.clear();
        for (Course c : snapshot) {
            Course nc = new Course(c.getName(), c.getTotal(), c.getDone());
            nc.setDeadline(c.getDeadline());
            nc.setArchived(c.isArchived());
            courses.add(nc);
        }
    }
}