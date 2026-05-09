import java.io.*;
import java.util.ArrayList;

/**
 * Handles saving and loading data using Java Serialization.
 * Demonstrates FILE HANDLING and EXCEPTION HANDLING.
 */
public class FileManager {

    private static String getUserFile(String username) {
        return username + "_profile.dat";
    }

    private static String getTasksFile(String username) {
        return username + "_tasks.dat";
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    public static void saveUser(User user) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(getUserFile(user.getUsername())))) {
            oos.writeObject(user);
        }
    }

    public static void saveTasks(String username, ArrayList<Task> tasks) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(getTasksFile(username)))) {
            oos.writeObject(tasks);
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    public static User loadUser(String username) throws IOException, ClassNotFoundException {
        File f = new File(getUserFile(username));
        if (!f.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(getUserFile(username)))) {
            return (User) ois.readObject();
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Task> loadTasks(String username) throws IOException, ClassNotFoundException {
        File f = new File(getTasksFile(username));
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(getTasksFile(username)))) {
            return (ArrayList<Task>) ois.readObject();
        }
    }
}