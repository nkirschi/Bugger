package tech.bugger.business.util;

import java.util.Locale;
import java.util.ResourceBundle;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.persistence.util.ConnectionPool;
import tech.bugger.persistence.util.Mailer;
import tech.bugger.persistence.util.PropertiesReader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
public class RegistryTest {

    private Registry registry;
    private InjectionPoint injectionPoint;
    private UserSession userSession;
    private RegistryKey registryKey;

    @BeforeEach
    public void init() {
        registry = new Registry();

        injectionPoint = mock(InjectionPoint.class);
        registryKey = mock(RegistryKey.class);
        Annotated annotated = mock(Annotated.class);
        when(annotated.getAnnotation(RegistryKey.class)).thenReturn(registryKey);
        when(injectionPoint.getAnnotated()).thenReturn(annotated);

        userSession = mock(UserSession.class);
        when(userSession.getLocale()).thenReturn(Locale.GERMAN);
    }

    @Test
    public void testGetResourceBundleWhenPresent() {
        when(registryKey.value()).thenReturn("labels");
        ResourceBundle resourceBundle = registry.getBundle(injectionPoint, userSession);
        assertAll(
                () -> assertEquals(Locale.GERMAN, resourceBundle.getLocale()),
                () -> assertEquals("tech.bugger.i18n.labels", resourceBundle.getBaseBundleName())
        );

    }

    @Test
    public void testGetResourceBundleWhenNotPresent() {
        when(registryKey.value()).thenReturn("invalid");
        assertThrows(InternalError.class, () -> registry.getBundle(injectionPoint, userSession));
    }

    @Test
    public void testGetConnectionPoolWhenPresent() {
        ConnectionPool connectionPoolMock = mock(ConnectionPool.class);
        registry.registerConnectionPool("key", connectionPoolMock);
        when(registryKey.value()).thenReturn("key");
        assertSame(connectionPoolMock, registry.getConnectionPool(injectionPoint));
    }

    @Test
    public void testGetConnectionPoolWhenNotPresent() {
        assertThrows(InternalError.class, () -> registry.getConnectionPool("invalid"));
    }

    @Test
    public void testGetMailerWhenPresent() {
        Mailer mailer = mock(Mailer.class);
        registry.registerMailer("key", mailer);
        when(registryKey.value()).thenReturn("key");
        assertSame(mailer, registry.getMailer(injectionPoint));
    }

    @Test
    public void testGetMailerWhenNotPresent() {
        assertThrows(InternalError.class, () -> registry.getMailer("invalid"));
    }

    @Test
    public void testGetPriorityExecutorWhenPresent() {
        PriorityExecutor priorityExecutor = mock(PriorityExecutor.class);
        registry.registerPriorityExecutor("key", priorityExecutor);
        when(registryKey.value()).thenReturn("key");
        assertSame(priorityExecutor, registry.getPriorityExecutor(injectionPoint));
    }

    @Test
    public void testGetPriorityExecutorWhenNotPresent() {
        assertThrows(InternalError.class, () -> registry.getPriorityExecutor("invalid"));
    }

    @Test
    public void testGetPropertiesReaderWhenPresent() {
        PropertiesReader propertiesReader = mock(PropertiesReader.class);
        registry.registerPropertiesReader("key", propertiesReader);
        when(registryKey.value()).thenReturn("key");
        assertSame(propertiesReader, registry.getPropertiesReader(injectionPoint));
    }

    @Test
    public void testGetPropertiesReaderWhenNotPresent() {
        assertThrows(InternalError.class, () -> registry.getPropertiesReader("invalid"));
    }

}
