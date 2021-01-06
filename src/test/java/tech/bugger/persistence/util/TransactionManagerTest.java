package tech.bugger.persistence.util;

import java.sql.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.business.util.Registry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionManagerTest {

    private TransactionManager manager;

    @Mock
    private Registry registry;

    @Mock
    private ConnectionPool pool;

    @Mock
    private Connection conn;

    @BeforeEach
    public void setup() {
        doReturn(conn).when(pool).getConnection();
        doReturn(pool).when(registry).getConnectionPool("db");
        manager = new TransactionManager(registry);
    }

    @Test
    public void testBegin() throws Exception {
        Transaction tx = manager.begin();
        assertNotNull(tx);
        verify(conn).setAutoCommit(false);
    }

}