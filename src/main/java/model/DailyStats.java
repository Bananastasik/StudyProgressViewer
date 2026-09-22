package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class DailyStats {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Map<LocalDate, Integer> perDay = new TreeMap<>();
    private int dailyGoal = 5;

    public DailyStats() {}

    public Map<LocalDate, Integer> getPerDay() { return perDay; }

    public int getDailyGoal() { return dailyGoal; }
    public void setDailyGoal(int dailyGoal) {
        this.dailyGoal = Math.max(1, dailyGoal);
    }

    public void addForToday(int delta) {
        LocalDate today = LocalDate.now();
        int cur = perDay.getOrDefault(today, 0);
        int next = Math.max(0, cur + delta);
        if (next == 0) perDay.remove(today);
        else perDay.put(today, next);
    }

    public int getTodayCount() {
        return perDay.getOrDefault(LocalDate.now(), 0);
    }

    public boolean isTodayGoalReached() {
        return getTodayCount() >= dailyGoal;
    }

    public int getStreak() {
        LocalDate day = LocalDate.now();
        if (perDay.getOrDefault(day, 0) < dailyGoal) {
            day = day.minusDays(1);
        }
        int streak = 0;
        while (perDay.getOrDefault(day, 0) >= dailyGoal) {
            streak++;
            day = day.minusDays(1);
        }
        return streak;
    }

    public int getDoneThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);
        int sum = 0;
        for (Map.Entry<LocalDate, Integer> e : perDay.entrySet()) {
            if (!e.getKey().isBefore(weekAgo) && !e.getKey().isAfter(today)) {
                sum += e.getValue();
            }
        }
        return sum;
    }

    public int getDoneLastWeek() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(13);
        LocalDate end = today.minusDays(7);
        int sum = 0;
        for (Map.Entry<LocalDate, Integer> e : perDay.entrySet()) {
            if (!e.getKey().isBefore(start) && !e.getKey().isAfter(end)) {
                sum += e.getValue();
            }
        }
        return sum;
    }

    public double getWeekDiffPercent() {
        int thisW = getDoneThisWeek();
        int lastW = getDoneLastWeek();
        if (lastW == 0) return thisW > 0 ? 100 : 0;
        return (thisW - lastW) * 100.0 / lastW;
    }

    public double getAvgPerDay(int windowDays) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(windowDays - 1);
        int sum = 0;
        int activeDays = 0;
        for (Map.Entry<LocalDate, Integer> e : perDay.entrySet()) {
            if (!e.getKey().isBefore(start) && !e.getKey().isAfter(today)) {
                sum += e.getValue();
                if (e.getValue() > 0) activeDays++;
            }
        }
        if (activeDays == 0) return 0;
        return sum / (double) activeDays;
    }

    public LocalDate predictFinishDate(int tasksLeft) {
        if (tasksLeft <= 0) return LocalDate.now();
        double avg = getAvgPerDay(14);
        if (avg <= 0) return null;
        long daysNeeded = (long) Math.ceil(tasksLeft / avg);
        return LocalDate.now().plusDays(daysNeeded);
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append("goal=").append(dailyGoal).append("\n");
        for (Map.Entry<LocalDate, Integer> e : perDay.entrySet()) {
            sb.append(e.getKey().format(FMT)).append("=").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }

    public void loadLine(String line) {
        if (line == null || line.trim().isEmpty()) return;
        int idx = line.indexOf('=');
        if (idx < 0) return;

        String key = line.substring(0, idx).trim();
        String val = line.substring(idx + 1).trim();

        if ("goal".equals(key)) {
            try { dailyGoal = Math.max(1, Integer.parseInt(val)); } catch (Exception ignored) {}
            return;
        }

        try {
            LocalDate date = LocalDate.parse(key, FMT);
            int count = Integer.parseInt(val);
            if (count > 0) perDay.put(date, count);
        } catch (Exception ignored) {}
    }
}