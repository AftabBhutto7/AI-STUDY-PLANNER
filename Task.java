import java.io.Serializable;
import java.time.LocalDate;

/**
 * Abstract base class representing a generic study task.
 * Demonstrates ABSTRACTION and ENCAPSULATION.
 */
public abstract class Task implements Serializable, Schedulable {
    private static final long serialVersionUID = 1L;

    // ENCAPSULATION: private fields with getters/setters
    private String taskId;
    private String subjectName;
    private String topicTitle;
    private LocalDate deadline;
    private DifficultyLevel difficulty;
    private double estimatedHours;
    private TaskStatus status;

    public enum DifficultyLevel { EASY, MEDIUM, HARD }
    public enum TaskStatus { PENDING, IN_PROGRESS, COMPLETED }

    public Task(String taskId, String subjectName, String topicTitle,
                LocalDate deadline, DifficultyLevel difficulty, double estimatedHours) {
        if (subjectName == null || subjectName.trim().isEmpty())
            throw new IllegalArgumentException("Subject name cannot be empty.");
        if (estimatedHours <= 0)
            throw new IllegalArgumentException("Estimated hours must be positive.");
        if (deadline == null)
            throw new IllegalArgumentException("Deadline cannot be null.");

        this.taskId       = taskId;
        this.subjectName  = subjectName.trim();
        this.topicTitle   = topicTitle.trim();
        this.deadline     = deadline;
        this.difficulty   = difficulty;
        this.estimatedHours = estimatedHours;
        this.status       = TaskStatus.PENDING;
    }

    // ABSTRACTION: subclasses must implement their own priority logic
    @Override
    public abstract int calculatePriority();

    // ABSTRACTION: subclasses describe their type
    public abstract String getTaskType();

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getTaskId()          { return taskId; }
    public String getSubjectName()     { return subjectName; }
    public String getTopicTitle()      { return topicTitle; }
    public LocalDate getDeadline()     { return deadline; }
    public DifficultyLevel getDifficulty() { return difficulty; }
    public double getEstimatedHours()  { return estimatedHours; }
    public TaskStatus getStatus()      { return status; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setSubjectName(String subjectName) {
        if (subjectName == null || subjectName.trim().isEmpty())
            throw new IllegalArgumentException("Subject name cannot be empty.");
        this.subjectName = subjectName.trim();
    }
    public void setTopicTitle(String topicTitle)   { this.topicTitle = topicTitle.trim(); }
    public void setDeadline(LocalDate deadline)    {
        if (deadline == null) throw new IllegalArgumentException("Deadline cannot be null.");
        this.deadline = deadline;
    }
    public void setDifficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; }
    public void setEstimatedHours(double estimatedHours) {
        if (estimatedHours <= 0) throw new IllegalArgumentException("Hours must be positive.");
        this.estimatedHours = estimatedHours;
    }
    public void setStatus(TaskStatus status) { this.status = status; }

    /** Days remaining until deadline (negative = overdue). */
    public long daysUntilDeadline() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s | Due: %s | Difficulty: %s | Hours: %.1f | Status: %s",
                getTaskType(), subjectName, topicTitle, deadline, difficulty, estimatedHours, status);
    }
}