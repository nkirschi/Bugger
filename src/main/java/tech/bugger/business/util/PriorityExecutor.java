package tech.bugger.business.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Executor for processing prioritized tasks on separate threads.
 */
public final class PriorityExecutor {

    /**
     * Underlying executor service handling most of the work.
     */
    private final ExecutorService executorService;

    /**
     * Constructs a new priority task executor.
     *
     * @param initialCap  Initial capacity of the underlying task queue.
     * @param coreThreads Target number of threads to use for task execution.
     * @param maxThreads  Maximum number of threads to use for task execution.
     * @param timeoutSecs Idle time after which a thread is terminated.
     */
    public PriorityExecutor(final int initialCap, final int coreThreads, final int maxThreads, final int timeoutSecs) {
        BlockingQueue<Runnable> queue = new PriorityBlockingQueue<>(initialCap, (r1, r2) -> {
            if (!(r1 instanceof PriorityFuture<?>)) {
                return 1;
            } else if (!(r2 instanceof PriorityFuture<?>)) {
                return -1;
            }

            PriorityFuture<?> t1 = (PriorityFuture<?>) r1;
            PriorityFuture<?> t2 = (PriorityFuture<?>) r2;
            return t1.getTask().getPriority().compareTo(t2.getTask().getPriority());
        });
        executorService = new ThreadPoolExecutor(coreThreads, maxThreads, timeoutSecs, TimeUnit.SECONDS, queue) {
            @Override
            protected <V> RunnableFuture<V> newTaskFor(final Runnable r, final V v) {
                return new PriorityFuture<>(super.newTaskFor(r, v), (PriorityTask) r);
            }

            @Override
            public Future<?> submit(final Runnable task) {
                return super.submit(task);
            }
        };
    }

    /**
     * Adds a new task to the prioritized task executor.
     *
     * @param priorityTask The task to be enqueued.
     */
    public void enqueue(final PriorityTask priorityTask) {
        executorService.submit(priorityTask);
    }

    /**
     * Shuts down the executor gracefully by not accepting any new tasks and finishing the remaining ones.
     */
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * Shuts down the executor immediately by aborting any running tasks forcefully.
     */
    public void kill() {
        executorService.shutdownNow();
    }
}
