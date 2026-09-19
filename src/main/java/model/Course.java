package model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private String name;
    private int total;
    private int done;
    private final List<Boolean> cells = new ArrayList<>();

    public Course(String name, int total, int done) {
        setName(name);
        setTotal(total);
        setDone(done);
        rebuildCells();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotal() { return total; }
    public void setTotal(int total) {
        this.total = Math.max(0, total);
        rebuildCells();
    }

    public int getDone() { return done; }
    public void setDone(int done) {
        this.done = Math.max(0, Math.min(done, total));
        rebuildCells();
    }

    public int getLeft() { return total - done; }

    public double getPercent() {
        return total == 0 ? 0 : done * 100.0 / total;
    }

    public List<Boolean> getCells() { return cells; }

    public void toggle(int index) {
        if (index < 0 || index >= cells.size()) return;
        boolean now = !cells.get(index);
        cells.set(index, now);
        done += now ? 1 : -1;
        if (done < 0) done = 0;
        if (done > total) done = total;
    }

    public void resetProgress() {
        done = 0;
        rebuildCells();
    }

    private void rebuildCells() {
        cells.clear();
        for (int i = 0; i < total; i++) cells.add(i < done);
    }
}