import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Core scheduling engine — the "AI" of the system.
 * Uses rule-based logic to allocate study slots intelligently.
 */
public class ScheduleEngine {

    /**
     * Generates a list of StudySlots for all pending/in-progress tasks.
     *
     * Algorithm:
     *  1. Filter non-completed tasks.
     *  2. Sort by calculatePriority() descending (POLYMORPHISM in action).
     *  3. Starting from today, fill daily slots up to dailyStudyHours.
     *  4. Hard tasks are placed at the beginning of the day.
     */
    public static List<StudySlot> generateSchedule(List<Task> tasks, User user) {
        List<StudySlot> schedule = new ArrayList<>();

        // Step 1: get only active tasks
        List<Task> active = new ArrayList<>();
        for (Task t : tasks) {
            if (t.getStatus() != Task.TaskStatus.COMPLETED) active.add(t);
        }

        // Step 2: sort by priority descending (POLYMORPHISM — each subclass calculates differently)
        active.sort(Comparator.comparingInt(Task::calculatePriority).reversed());

        // Step 3: allocate slots day by day
        LocalDate currentDate = LocalDate.now();
        double hoursAvailableToday = user.getDailyStudyHours();
        String[] timeSlots = buildTimeSlots(user.getPreferredTime(), user.getDailyStudyHours());

        int slotIndex = 0;

        for (Task task : active) {
            double hoursRemaining = task.getEstimatedHours();

            while (hoursRemaining > 0) {
                // Each block is 2 hours max (or remainder)
                double blockHours = Math.min(2.0, hoursRemaining);

                if (hoursAvailableToday <= 0 || slotIndex >= timeSlots.length - 1) {
                    // Move to next day
                    currentDate = currentDate.plusDays(1);
                    hoursAvailableToday = user.getDailyStudyHours();
                    slotIndex = 0;
                    timeSlots = buildTimeSlots(user.getPreferredTime(), user.getDailyStudyHours());
                }

                // Hard tasks get placed first (slotIndex stays low), easy tasks later
                String start = timeSlots[slotIndex];
                String end   = timeSlots[Math.min(slotIndex + 1, timeSlots.length - 1)];

                schedule.add(new StudySlot(currentDate, start, end, task));

                hoursRemaining      -= blockHours;
                hoursAvailableToday -= blockHours;
                slotIndex++;
            }

            task.setStatus(Task.TaskStatus.IN_PROGRESS);
        }

        return schedule;
    }

    /**
     * Builds an array of time labels based on preferred study time.
     * e.g. MORNING → ["06:00","08:00","10:00","12:00"]
     */
    private static String[] buildTimeSlots(User.StudyTime pref, double totalHours) {
        int startHour;
        switch (pref) {
            case MORNING: startHour = 6;  break;
            case EVENING: startHour = 16; break;
            default:      startHour = 20; break; // NIGHT
        }

        int slots = (int) Math.ceil(totalHours / 2) + 1;
        String[] times = new String[slots];
        for (int i = 0; i < slots; i++) {
            int hour = (startHour + i * 2) % 24;
            times[i] = String.format("%02d:00", hour);
        }
        return times;
    }

    /**
     * Generates plain-text smart recommendations for the user.
     */
    public static List<String> generateRecommendations(List<Task> tasks) {
        List<String> recs = new ArrayList<>();

        for (Task t : tasks) {
            if (t.getStatus() == Task.TaskStatus.COMPLETED) continue;

            long days = t.daysUntilDeadline();

            if (days < 0) {
                recs.add("⚠ OVERDUE: " + t.getSubjectName() + " — " + t.getTopicTitle());
            } else if (days == 0) {
                recs.add("🔴 DUE TODAY: " + t.getSubjectName() + " — study immediately!");
            } else if (days <= 2) {
                recs.add("🟠 URGENT (" + days + " days): Focus on " + t.getSubjectName());
            } else if (days <= 5) {
                recs.add("🟡 Coming soon (" + days + " days): Prioritize " + t.getSubjectName());
            }

            if (t.getDifficulty() == Task.DifficultyLevel.HARD && days <= 7) {
                recs.add("📚 Hard topic — allocate extra revision time for: " + t.getTopicTitle());
            }
        }

        if (recs.isEmpty()) recs.add("✅ You're on track! Keep up the good work.");
        return recs;
    }
}