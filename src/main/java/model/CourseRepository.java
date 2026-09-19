package model;

import util.ProgressStorage;

import java.util.ArrayList;
import java.util.List;

public class CourseRepository {
    private final List<Course> courses = new ArrayList<>();

    public CourseRepository() {
        // Стартовые данные
        courses.add(new Course("Java25",      1625, 906));
        courses.add(new Course("SQL",         1525, 125));
        courses.add(new Course("Claude",       350,  15));
        courses.add(new Course("Java старый", 4470, 255));
        courses.add(new Course("Spring 1",    2000,   4));
        courses.add(new Course("Spring 2",    2200,   0));
        courses.add(new Course("Spring 3",    1950,   0));
        courses.add(new Course("Docker",       600,   0));

        ProgressStorage.loadInto(courses);
    }

    public List<Course> getAll() { return courses; }

    public void add(Course c) { courses.add(c); }

    public void remove(Course c) { courses.remove(c); }

    public void save() {
        ProgressStorage.save(courses);
    }

    public int getTotalDone() {
        int sum = 0;
        for (Course c : courses) sum += c.getDone();
        return sum;
    }

    public int getTotalAll() {
        int sum = 0;
        for (Course c : courses) sum += c.getTotal();
        return sum;
    }
}