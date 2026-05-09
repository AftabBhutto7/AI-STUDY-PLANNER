import java.time.LocalDate;

/**
 * Represents an exam preparation task.
 * Demonstrates INHERITANCE and POLYMORPHISM.
 * Exams get higher base priority because they are high-stakes.
 */
public class ExamTask extends Task {
    private static final long serialVersionUID = 1L;

    private String examHall;   // optional extra field specific to exams

    public ExamTask(String taskId, String subjectName, String topicTitle,
                    LocalDate deadline, DifficultyLevel difficulty,
                    double estimatedHours, String examHall) {
        super(taskId, subjectName, topicTitle, deadline, difficulty, estimatedHours);
        this.examHall = (examHall == null) ? "TBD" : examHall.trim();
    }

    /**
     * POLYMORPHISM: overrides base priority with exam-specific logic.
     * Exams get a +20 bonus so they always outrank assignments.
     */
    @Override
    public int calculatePriority() {
        int priority = 20; // exam base bonus

        // Deadline proximity (closer = higher)
        long days = daysUntilDeadline();
        if (days <= 1)       priority += 50;
        else if (days <= 3)  priority += 40;
        else if (days <= 7)  priority += 30;
        else if (days <= 14) priority += 15;
        else                 priority += 5;

        // Difficulty factor
        switch (getDifficulty()) {
            case HARD:   priority += 15; break;
            case MEDIUM: priority += 8;  break;
            case EASY:   priority += 3;  break;
        }

        return priority;
    }

    @Override
    public String getTaskType() { return "EXAM"; }

    public String getExamHall() { return examHall; }
    public void setExamHall(String examHall) { this.examHall = examHall; }
}