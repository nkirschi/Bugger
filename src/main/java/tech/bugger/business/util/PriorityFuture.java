package tech.bugger.business.util;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Decorator empowering Java Concurrent API tasks with prioritized execution.
 *
 * @param <T> The type of result this task can yield.
 */
public class PriorityFuture<T> implements RunnableFuture<T> {

    /**
     * Action to be decorated with a priority.
     */
    private final RunnableFuture<T> action;

    /**
     * Priority associated with {@code action}.
     */
    private final PriorityTask task;

    /**
     * Construct a new priority task decorator with the specified original API action and prioritized task.
     *
     * @param action The original API action to be wrapped.
     * @param task   The prioritized task to be executed.
     */
    public PriorityFuture(final RunnableFuture<T> action, final PriorityTask task) {
        this.task = task;
        this.action = action;
    }

    /**
     * When an object implementing interface {@code Runnable} is used to create a thread, starting the thread causes the
     * object's {@code run} method to be called in that separately executing thread.
     * <p>
     * The general contract of the method {@code run} is that it may take any action whatsoever.
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        action.run();
    }

    /**
     * Attempts to cancel execution of this task.  This attempt will fail if the task has already completed, has already
     * been cancelled, or could not be cancelled for some other reason. If successful, and this task has not started
     * when {@code cancel} is called, this task should never run.  If the task has already started, then the {@code
     * mayInterruptIfRunning} parameter determines whether the thread executing this task should be interrupted in an
     * attempt to stop the task.
     *
     * <p>After this method returns, subsequent calls to {@link #isDone} will
     * always return {@code true}.  Subsequent calls to {@link #isCancelled} will always return {@code true} if this
     * method returned {@code true}.
     *
     * @param mayInterruptIfRunning {@code true} if the thread executing this task should be interrupted; otherwise,
     *                              in-progress tasks are allowed to complete
     * @return {@code false} if the task could not be cancelled, typically because it has already completed normally;
     *         {@code true} otherwise
     */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return action.cancel(mayInterruptIfRunning);
    }

    /**
     * Returns {@code true} if this task was cancelled before it completed normally.
     *
     * @return {@code true} if this task was cancelled before it completed
     */
    @Override
    public boolean isCancelled() {
        return action.isCancelled();
    }

    /**
     * Returns {@code true} if this task completed.
     *
     * Completion may be due to normal termination, an exception, or cancellation -- in all of these cases, this method
     * will return {@code true}.
     *
     * @return {@code true} if this task completed
     */
    @Override
    public boolean isDone() {
        return action.isDone();
    }

    /**
     * Waits if necessary for the computation to complete, and then retrieves its result.
     *
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException    if the computation threw an exception
     * @throws InterruptedException  if the current thread was interrupted while waiting
     */
    @Override
    public T get() throws InterruptedException, ExecutionException, CancellationException {
        return action.get();
    }

    /**
     * Waits if necessary for at most the given time for the computation to complete, and then retrieves its result, if
     * available.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException    if the computation threw an exception
     * @throws InterruptedException  if the current thread was interrupted while waiting
     * @throws TimeoutException      if the wait timed out
     */
    @Override
    public T get(final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException, CancellationException {
        return action.get(timeout, unit);
    }

    /**
     * Returns the prioritized task to be executed.
     *
     * @return The prioritized task.
     */
    public PriorityTask getTask() {
        return task;
    }
}
