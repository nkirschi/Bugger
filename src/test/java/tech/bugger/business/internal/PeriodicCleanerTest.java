package tech.bugger.business.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.TokenGateway;
import tech.bugger.persistence.gateway.UserGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class PeriodicCleanerTest {

    @InjectMocks
    private PeriodicCleaner periodicCleaner;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private TokenGateway tokenGateway;

    @Mock
    private UserGateway userGateway;

    @BeforeEach
    public void setUp() {
        doReturn(tokenGateway).when(tx).newTokenGateway();
        doReturn(userGateway).when(tx).newUserGateway();
        doReturn(tx).when(transactionManager).begin();
    }

    @Test
    public void testRunWhenSuccess() {
        periodicCleaner.run();
        verify(tokenGateway).cleanExpiredTokens(any());
        verify(userGateway).cleanExpiredRegistrations();
    }

    @Test
    public void testRunWhenError() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> periodicCleaner.run());
    }

}