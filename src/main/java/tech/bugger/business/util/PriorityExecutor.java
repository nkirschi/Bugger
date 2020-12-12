package tech.bugger.business.util;

import tech.bugger.global.util.Log;

import java.util.concurrent.ExecutorService;

/**
 * Singleton executor for processing prioritized tasks on separate threads.
 */
public class PriorityExecutor {
    private static final Log log = Log.forClass(PriorityExecutor.class);

    private static PriorityExecutor instance;

    private final ExecutorService executorService;

    private PriorityExecutor() {
        executorService = null;
    }

    /**
     * Supplies the singleton task queue object.
     *
     * @return The one and only instance of the task queue.
     */
    public static PriorityExecutor getInstance() {
        return null;
    }

    /**
     * Adds a new task to the prioritized task executor.
     *
     * @param priorityTask The task to be enqueued.
     */
    public void enqueue(PriorityTask priorityTask) {

    }

    /**
     * Shuts down the executor gracefully by not accepting any new tasks and executing the remaining ones.
     */
    public void shutdown() {

    }

    /**
     * Shuts down the executor immediately by aborting any running tasks forcefully.
     */
    public void kill() {

    }
}
