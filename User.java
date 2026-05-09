import java.io.Serializable;

/**
 * Stores user profile information.
 * Demonstrates ENCAPSULATION.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum StudyTime { MORNING, EVENING, NIGHT }

    private String username;
    private String password;         // simple auth
    private String fullName;
    private double dailyStudyHours;  // how many hours/day the student can study
    private StudyTime preferredTime;

    public User(String username, String password, String fullName,
                double dailyStudyHours, StudyTime preferredTime) {
        validateUsername(username);
        validateHours(dailyStudyHours);
        this.username        = username.trim();
        this.password        = password;
        this.fullName        = fullName.trim();
        this.dailyStudyHours = dailyStudyHours;
        this.preferredTime   = preferredTime;
    }

    private void validateUsername(String u) {
        if (u == null || u.trim().isEmpty())
            throw new IllegalArgumentException("Username cannot be empty.");
    }
    private void validateHours(double h) {
        if (h <= 0 || h > 24)
            throw new IllegalArgumentException("Daily study hours must be between 1 and 24.");
    }

    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    // Getters
    public String getUsername()          { return username; }
    public String getFullName()          { return fullName; }
    public double getDailyStudyHours()   { return dailyStudyHours; }
    public StudyTime getPreferredTime()  { return preferredTime; }

    // Setters
    public void setFullName(String fullName)            { this.fullName = fullName.trim(); }
    public void setPassword(String password)            { this.password = password; }
    public void setDailyStudyHours(double hours)        { validateHours(hours); this.dailyStudyHours = hours; }
    public void setPreferredTime(StudyTime preferredTime) { this.preferredTime = preferredTime; }

    @Override
    public String toString() {
        return String.format("User{name='%s', hours/day=%.1f, preferred='%s'}",
                fullName, dailyStudyHours, preferredTime);
    }
}