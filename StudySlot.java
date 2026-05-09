import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a single time slot in the schedule.
 * OBJECT COMPOSITION: Planner holds a list of StudySlots.
 */
public class StudySlot implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate date;
    private String startTime;  // e.g. "08:00"
    private String endTime;    // e.g. "10:00"
    private Task task;
    private boolean completed;

    public StudySlot(LocalDate date, String startTime, String endTime, Task task) {
        this.date      = date;
        this.startTime = startTime;
        this.endTime   = endTime;
        this.task      = task;
        this.completed = false;
    }

    public LocalDate getDate()    { return date; }
    public String getStartTime()  { return startTime; }
    public String getEndTime()    { return endTime; }
    public Task getTask()         { return task; }
    public boolean isCompleted()  { return completed; }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) task.setStatus(Task.TaskStatus.COMPLETED);
    }

    @Override
    public String toString() {
        return String.format("%s | %s-%s | %s [%s]",
                date, startTime, endTime,
                task.getSubjectName() + " - " + task.getTopicTitle(),
                completed ? "DONE" : "PENDING");
    }
}