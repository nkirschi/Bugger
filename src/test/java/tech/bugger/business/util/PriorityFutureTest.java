package tech.bugger.business.util;

import java.util.concurrent.RunnableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
public class PriorityFutureTest {
    private PriorityFuture<Runnable> priorityFuture;

    private PriorityTask taskMock;
    private RunnableFuture<Runnable> futureMock;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        taskMock = mock(PriorityTask.class);
        futureMock = (RunnableFuture<Runnable>) mock(RunnableFuture.class);
        priorityFuture = new PriorityFuture<>(futureMock, taskMock);
    }

    @Test
    public void testRun() {
        priorityFuture.run();
        verify(futureMock).run();
    }

    @Test
    public void testCancel() {
        priorityFuture.cancel(anyBoolean());
        verify(futureMock).cancel(anyBoolean());
    }

    @Test
    public void testIsCancelled() {
        priorityFuture.isCancelled();
        verify(futureMock).isCancelled();
    }

    @Test
    public void testIsDone() {
        priorityFuture.isDone();
        verify(futureMock).isDone();
    }

    @Test
    public void testGet() throws Exception {
        priorityFuture.get();
        verify(futureMock).get();
    }

    @Test
    public void testGetWithParams() throws Exception {
        priorityFuture.get(anyLong(), any());
        verify(futureMock).get(anyLong(), any());
    }

    @Test
    public void testGetTask() {
        PriorityTask task = priorityFuture.getTask();
        assertSame(taskMock, task);
    }
}