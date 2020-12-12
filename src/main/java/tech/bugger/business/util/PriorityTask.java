package tech.bugger.business.util;

/**
 * Task augmented with an associated priority.
 */
public class PriorityTask implements Runnable {
    private Priority priority;
    private Runnable action;

    /**
     * Available task priority levels.
     */
    public enum Priority { // the order defines the importance!
        /**
         * High priotiy.
         */
        HIGH,

        /**
         * Low priority.
         */
        LOW
    }

    /**
     * Constructs a new task with the speicified priority and action.
     *
     * @param priority The task priority level.
     * @param action   The action to invoke upon task execution.
     */
    public PriorityTask(Priority priority, Runnable action) {
        this.priority = priority;
        this.action = action;
    }

    /**
     * Execute the assigned action.
     *
     * {@inheritDoc}
     */
    @Override
    public void run() {
        action.run();
    }

    /**
     * Returns the priority of this task.
     *
     * @return The task's priority.
     */
    public Priority getPriority() {
        return priority;
    }
}
