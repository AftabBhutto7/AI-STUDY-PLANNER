/**
 * Interface that every schedulable item must implement.
 * Demonstrates ABSTRACTION via interface.
 */
public interface Schedulable {
    /**
     * Computes a numeric priority score for scheduling.
     * Higher score = scheduled sooner.
     */
    int calculatePriority();
}