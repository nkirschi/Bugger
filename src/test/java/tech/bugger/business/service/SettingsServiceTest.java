package tech.bugger.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.SettingsGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SettingsServiceTest {

    private SettingsService service;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private SettingsGateway settingsGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

    private Configuration testConfiguration;

    private Organization testOrganization;

    @BeforeEach
    public void setUp() {
        service = new SettingsService(transactionManager, feedbackEvent, ResourceBundleMocker.mock(""));
        testConfiguration = new Configuration(true, false, "abc", ".x,.y,.z", 42, "0,1,2");
        testOrganization = new Organization("orga", new byte[0], "???", "jura", "gaudi");
        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(settingsGateway).when(tx).newSettingsGateway();
    }

    @Test
    public void testLoadConfigurationWhenFound() throws Exception {
        doReturn(testConfiguration).when(settingsGateway).getConfiguration();
        assertEquals(testConfiguration, service.loadConfiguration());
    }

    @Test
    public void testLoadConfigurationWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(settingsGateway).getConfiguration();
        assertNull(service.loadConfiguration());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testLoadConfigurationWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(service.loadConfiguration());
        verify(feedbackEvent).fire(any());
    }


    @Test
    public void testLoadOrganizationWhenFound() throws Exception {
        doReturn(testOrganization).when(settingsGateway).getOrganization();
        assertEquals(testOrganization, service.loadOrganization());
    }

    @Test
    public void testLoadOrganizationWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(settingsGateway).getOrganization();
        assertNull(service.loadOrganization());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testLoadOrganizationWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(service.loadOrganization());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpdateConfigurationWhenFine() {
        assertTrue(service.updateConfiguration(testConfiguration));
        verify(settingsGateway).setConfiguration(any());
    }

    @Test
    public void testUpdateConfigurationWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.updateConfiguration(testConfiguration));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpdateOrganizationWhenFine() {
        assertTrue(service.updateOrganization(testOrganization));
        verify(settingsGateway).setOrganization(any());
    }

    @Test
    public void testUpdateOrganizationWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.updateOrganization(testOrganization));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testReadFileOnValidInputStream() {
        assertArrayEquals(new byte[0], service.readFile(new ByteArrayInputStream(new byte[0])));
    }

    @Test
    public void testReadFileOnInvalidInputStream() throws Exception {
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
        is.close();
        service.readFile(is);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDiscoverFilesOnExistingDirectory() {
        assertArrayEquals(new String[]{"file"}, service.discoverFiles("src/test/resources/dir").toArray());
    }

    @Test
    public void testDiscoverFilesOnNonexistingDirectory() {
        assertTrue(service.discoverFiles("src/test/resources/nodir").isEmpty());
        verify(feedbackEvent).fire(any());
    }
}