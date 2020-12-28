package tech.bugger.business.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(LogExtension.class)
public class PriorityExecutorTest {

    private PriorityExecutor priorityExecutor;

    private PriorityTask lowPriorityTask;
    private PriorityTask highPriorityTask;

    private BlockingQueue<String> finishedTasks;
    private CountDownLatch latch;

    private static final int TERMINATION_TIMEOUT_MILLIS = 5000;
    private static final int TASK_SLEEP_DURATION_MILLIS = 100;

    @BeforeEach
    public void setUp() {
        priorityExecutor = new PriorityExecutor(10, 1, 1, 60);
        finishedTasks = new ArrayBlockingQueue<>(5);
        latch = new CountDownLatch(1);
        lowPriorityTask = new PriorityTask(PriorityTask.Priority.LOW, () -> {
            awaitStart();
            finishedTasks.add("low");
            sleep(TASK_SLEEP_DURATION_MILLIS);
        });
        highPriorityTask = new PriorityTask(PriorityTask.Priority.HIGH, () -> {
            awaitStart();
            finishedTasks.add("high");
            sleep(TASK_SLEEP_DURATION_MILLIS);
        });
    }

    private void awaitStart() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        priorityExecutor.shutdown(TERMINATION_TIMEOUT_MILLIS);
        finishedTasks.clear();
    }

    @Test
    public void testEnqueuePrioritizes() throws Exception {
        priorityExecutor.enqueue(lowPriorityTask);
        priorityExecutor.enqueue(lowPriorityTask);
        priorityExecutor.enqueue(lowPriorityTask);
        priorityExecutor.enqueue(lowPriorityTask);
        priorityExecutor.enqueue(highPriorityTask);
        latch.countDown();
        priorityExecutor.shutdown(TERMINATION_TIMEOUT_MILLIS);
        assertArrayEquals(new String[]{"low", "high", "low", "low", "low"}, finishedTasks.toArray(),
                "The high priority task should be executed directly after the already running low priority task.");
    }

    @Test
    public void testEnqueueRejectsWhenAlreadyShutDown() throws Exception {
        priorityExecutor.shutdown(TERMINATION_TIMEOUT_MILLIS);
        assertThrows(IllegalStateException.class, () -> priorityExecutor.enqueue(highPriorityTask));
    }

    @Test
    public void testEnqueueRejectsWhenAlreadyKilled() throws Exception {
        priorityExecutor.kill(TERMINATION_TIMEOUT_MILLIS);
        assertThrows(IllegalStateException.class, () -> priorityExecutor.enqueue(highPriorityTask));
    }

    @Test
    public void testShutdownFinishesWaitingTasks() throws Exception {
        priorityExecutor.enqueue(lowPriorityTask);
        priorityExecutor.enqueue(lowPriorityTask);
        priorityExecutor.enqueue(lowPriorityTask);
        priorityExecutor.enqueue(lowPriorityTask);
        priorityExecutor.enqueue(lowPriorityTask);
        latch.countDown();
        priorityExecutor.shutdown(TERMINATION_TIMEOUT_MILLIS);
        assertEquals(5, finishedTasks.size(), "All tasks should finish.");
    }

    @Test
    public void testKillTerminatesWaitingTasks() throws Exception {
        priorityExecutor.enqueue(lowPriorityTask);
        priorityExecutor.enqueue(lowPriorityTask);
        priorityExecutor.enqueue(lowPriorityTask);
        priorityExecutor.enqueue(lowPriorityTask);
        priorityExecutor.enqueue(lowPriorityTask);
        latch.countDown();
        priorityExecutor.kill(TERMINATION_TIMEOUT_MILLIS);
        assertEquals(1, finishedTasks.size(), "Only the first task should finish.");
    }

    @Test
    public void testForeignTaskComparison()throws Exception {
        Field field = priorityExecutor.getClass().getDeclaredField("executorService");
        field.setAccessible(true);
        var executor = (ThreadPoolExecutor) field.get(priorityExecutor);
        var queue = (PriorityBlockingQueue<Runnable>) executor.getQueue();
        field = queue.getClass().getDeclaredField("comparator");
        field.setAccessible(true);
        var comparator = (Comparator<Runnable>) field.get(queue);
        var valid = new PriorityFuture<>(null, null);
        assertAll(
                () -> assertThrows(InternalError.class, () -> comparator.compare(null, null)),
                () -> assertThrows(InternalError.class, () -> comparator.compare(valid, null))
        );
    }
}