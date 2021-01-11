package tech.bugger.business.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
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
     * Constructs a new priority task executor with the given technical parameters.
     *
     * @param initialCap  Initial capacity of the underlying task queue.
     * @param coreThreads Target number of threads to use for task execution.
     * @param maxThreads  Maximum number of threads to use for task execution.
     * @param timeoutSecs Idle time after which a thread is terminated.
     */
    public PriorityExecutor(final int initialCap, final int coreThreads, final int maxThreads, final int timeoutSecs) {
        BlockingQueue<Runnable> queue = new PriorityBlockingQueue<>(initialCap, (r1, r2) -> {
            if (!(r1 instanceof PriorityFuture<?> && r2 instanceof PriorityFuture<?>)) {
                throw new InternalError("Foreign tasks in priority queue. This should never happen!");
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
        };
    }

    /**
     * Adds a new task to the prioritized task executor.
     *
     * @param priorityTask The task to be enqueued.
     */
    public void enqueue(final PriorityTask priorityTask) {
        if (executorService.isTerminated()) {
            throw new IllegalStateException("Priority executor has already been shut down.");
        }
        executorService.submit(priorityTask);
    }

    /**
     * Shuts down the executor gracefully by not accepting any new tasks and finishing the remaining ones.
     *
     * Calling this method blocks until either all tasks have been terminated or the given timeout has been reached.
     *
     * @param timeoutMillis The maximum time in milliseconds to wait for remaining task execution completion.
     * @return {@code true} iff all remaining tasks have been completed without timeout.
     * @throws InterruptedException if interrupted whilst awaiting termination.
     */
    public boolean shutdown(final int timeoutMillis) throws InterruptedException {
        executorService.shutdown();
        return executorService.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Shuts down the executor immediately by discarding any waiting tasks and only finishing the running ones.
     *
     * Calling this method blocks until either all tasks have been terminated or the given timeout has been reached.
     *
     * @param timeoutMillis The maximum time in milliseconds to wait for remaining task execution completion.
     * @return {@code true} iff all remaining tasks have been completed without timeout.
     * @throws InterruptedException if interrupted whilst awaiting termination.
     */
    public boolean kill(final int timeoutMillis) throws InterruptedException {
        executorService.shutdownNow();
        return executorService.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
    }

}
