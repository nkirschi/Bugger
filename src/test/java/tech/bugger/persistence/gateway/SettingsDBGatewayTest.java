package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
public class SettingsDBGatewayTest {

    private SettingsDBGateway gateway;
    private Connection connection;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new SettingsDBGateway(connection);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    private void removeEntry() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM system_settings;");
        }
    }

    @Test
    public void testGetConfiguration() throws Exception {
        assertNotNull(gateway.getConfiguration());
    }

    @Test
    public void testGetConfigurationWhenEntryDoesNotExist() throws Exception {
        removeEntry();
        assertThrows(NotFoundException.class, () -> gateway.getConfiguration());
    }

    @Test
    public void testGetConfigurationWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new SettingsDBGateway(connectionSpy).getConfiguration());
    }

    @Test
    public void testGetOrganization() throws Exception {
        assertNotNull(gateway.getOrganization());
    }

    @Test
    public void testGetOrganizationWhenEntryDoesNotExist() throws Exception {
        removeEntry();
        assertThrows(NotFoundException.class, () -> gateway.getOrganization());
    }

    @Test
    public void testGetOrganizationWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new SettingsDBGateway(connectionSpy).getOrganization());
    }

    @Test
    public void testSetConfiguration() throws Exception {
        Configuration configuration = new Configuration(true, false, "abc", ".x,.y,.z", 42, "0,1,2");
        gateway.setConfiguration(configuration);
        assertEquals(configuration, gateway.getConfiguration());
    }

    @Test
    public void testSetConfigurationWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        Configuration configurationMock = mock(Configuration.class);
        assertThrows(StoreException.class,
                () -> new SettingsDBGateway(connectionSpy).setConfiguration(configurationMock));
    }

    @Test
    public void testSetOrganization() throws Exception {
        Organization organization = new Organization("orga", new byte[0], "???", "jura", "gaudi");
        gateway.setOrganization(organization);
        assertEquals(organization, gateway.getOrganization());
    }

    @Test
    public void testSetOrganizationWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        Organization organizationMock = mock(Organization.class);
        assertThrows(StoreException.class,
                () -> new SettingsDBGateway(connectionSpy).setOrganization(organizationMock));
    }

}