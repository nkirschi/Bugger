package tech.bugger.business.util;

/**
 * Task augmented with an associated priority.
 */
public class PriorityTask implements Runnable {

    /**
     * {@link Priority} level of this task.
     */
    private final Priority priority;

    /**
     * Action to invoke when this task is executed.
     */
    private final Runnable action;

    /**
     * Available task priority levels.
     */
    public enum Priority { // The order defines the importance. Do not change!
        /**
         * High priority.
         */
        HIGH,

        /**
         * Low priority.
         */
        LOW
    }

    /**
     * Constructs a new task with the specified priority and action.
     *
     * @param priority The task priority level.
     * @param action   The action to invoke upon task execution.
     */
    public PriorityTask(final Priority priority, final Runnable action) {
        this.priority = priority;
        this.action = action;
    }

    /**
     * Execute the assigned action.
     * <p>
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
