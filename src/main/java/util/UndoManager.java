package util;

import model.Course;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class UndoManager {

    private static final int MAX_STACK = 20;

    private final Deque<List<Course>> stack = new ArrayDeque<>();

    public void push(List<Course> state) {
        List<Course> snapshot = new ArrayList<>();
        for (Course c : state) {
            Course nc = new Course(c.getName(), c.getTotal(), c.getDone());
            nc.setDeadline(c.getDeadline());
            nc.setArchived(c.isArchived());
            snapshot.add(nc);
        }
        stack.push(snapshot);
        while (stack.size() > MAX_STACK) stack.removeLast();
    }

    public List<Course> pop() {
        return stack.isEmpty() ? null : stack.pop();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}