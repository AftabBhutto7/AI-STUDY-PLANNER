import java.time.LocalDate;

/**
 * Represents an assignment / homework task.
 * Demonstrates INHERITANCE and POLYMORPHISM.
 * Assignments use flexible scheduling — lower base priority than exams.
 */
public class AssignmentTask extends Task {
    private static final long serialVersionUID = 1L;

    private String submissionMode; // e.g. "Online", "Printed"

    public AssignmentTask(String taskId, String subjectName, String topicTitle,
                          LocalDate deadline, DifficultyLevel difficulty,
                          double estimatedHours, String submissionMode) {
        super(taskId, subjectName, topicTitle, deadline, difficulty, estimatedHours);
        this.submissionMode = (submissionMode == null) ? "Online" : submissionMode.trim();
    }

    /**
     * POLYMORPHISM: flexible priority — no base bonus, purely deadline-driven.
     */
    @Override
    public int calculatePriority() {
        int priority = 0;

        long days = daysUntilDeadline();
        if (days <= 1)       priority += 45;
        else if (days <= 3)  priority += 35;
        else if (days <= 7)  priority += 22;
        else if (days <= 14) priority += 10;
        else                 priority += 2;

        switch (getDifficulty()) {
            case HARD:   priority += 10; break;
            case MEDIUM: priority += 5;  break;
            case EASY:   priority += 1;  break;
        }

        return priority;
    }

    @Override
    public String getTaskType() { return "ASSIGNMENT"; }

    public String getSubmissionMode() { return submissionMode; }
    public void setSubmissionMode(String submissionMode) { this.submissionMode = submissionMode; }
}