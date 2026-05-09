import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core planner that owns the task list and user.
 * Demonstrates OBJECT COMPOSITION: contains ArrayList<Task> and a User object.
 */
public class Planner {

    // COMPOSITION: Planner HAS a User and HAS a collection of Tasks
    private User user;
    private ArrayList<Task> tasks;       // <-- object composition
    private List<StudySlot> schedule;

    public Planner(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null.");
        this.user     = user;
        this.tasks    = new ArrayList<>();
        this.schedule = new ArrayList<>();
    }

    // ── Task CRUD ─────────────────────────────────────────────────────────────

    public void addTask(Task task) {
        if (task == null) throw new IllegalArgumentException("Task cannot be null.");
        tasks.add(task);
    }

    public boolean removeTask(String taskId) {
        return tasks.removeIf(t -> t.getTaskId().equals(taskId));
    }

    public Task findTask(String taskId) {
        for (Task t : tasks) {
            if (t.getTaskId().equals(taskId)) return t;
        }
        return null;
    }

    public List<Task> getAllTasks()      { return new ArrayList<>(tasks); }
    public List<Task> getPendingTasks() {
        List<Task> out = new ArrayList<>();
        for (Task t : tasks)
            if (t.getStatus() != Task.TaskStatus.COMPLETED) out.add(t);
        return out;
    }

    // ── Scheduling ────────────────────────────────────────────────────────────

    public void generateSchedule() {
        schedule = ScheduleEngine.generateSchedule(tasks, user);
    }

    public List<StudySlot> getSchedule()  { return schedule; }

    // ── Progress ──────────────────────────────────────────────────────────────

    public double getCompletionPercentage() {
        if (tasks.isEmpty()) return 0;
        long done = tasks.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED)
                .count();
        return (done * 100.0) / tasks.size();
    }

    public List<String> getRecommendations() {
        return ScheduleEngine.generateRecommendations(tasks);
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    public static String generateId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public User getUser()           { return user; }
    public void setUser(User user)  { this.user = user; }

    /** Replace entire task list (used after loading from file). */
    public void setTasks(ArrayList<Task> tasks) { this.tasks = tasks; }
}