package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.persistence.exception.StoreException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
public class MetadataDBGatewayTest {

    private MetadataDBGateway gateway;
    private Connection connection;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new MetadataDBGateway(connection);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testRetrieveMetadataWhenTableDoesNotExist() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS metadata;");
        }
        assertNull(gateway.retrieveMetadata());
    }

    @Test
    public void testRetrieveMetadataWhenTableExistsButEntryDoesNot() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM metadata;");
        }
        assertNull(gateway.retrieveMetadata());
    }

    @Test
    public void testRetrieveMetadataWhenTableAndEntryExist() {
        assertNotNull(gateway.retrieveMetadata().getVersion());
    }

    @Test
    public void testRetrieveMetadataWhenDatabaseErrorOnCreate() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new MetadataDBGateway(connectionSpy).retrieveMetadata());
    }

    @Test
    public void testRetrieveMetadataWhenDatabaseErrorOnClose() throws Exception {
        Connection connectionSpy = spy(connection);
        PreparedStatement preparedStatementMock = mock(PreparedStatement.class);
        doThrow(SQLException.class).when(preparedStatementMock).close();
        ResultSet rsMock = mock(ResultSet.class);
        doReturn(rsMock).when(preparedStatementMock).executeQuery();
        doReturn(preparedStatementMock).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new MetadataDBGateway(connectionSpy).retrieveMetadata());
    }

    @Test
    public void testRetrieveMetadataWhenDatabaseErrorOnExecute() throws Exception {
        Connection connectionSpy = spy(connection);
        PreparedStatement preparedStatementMock = mock(PreparedStatement.class);
        doThrow(SQLException.class).when(preparedStatementMock).executeQuery();
        doReturn(preparedStatementMock).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new MetadataDBGateway(connectionSpy).retrieveMetadata());
    }

    @Test
    public void testInitializeSchemaWhenFine() throws Exception {
        Connection connectionSpy = spy(connection);
        InputStream is = new ByteArrayInputStream(new byte[0]);
        new MetadataDBGateway(connectionSpy).initializeSchema(is);
        verify(connectionSpy).createStatement();
    }

    @Test
    public void testInitializeSchemaNonemptyStream() throws Exception {
        Connection connectionSpy = spy(connection);
        InputStream is = new ByteArrayInputStream("--".getBytes(StandardCharsets.UTF_8));
        new MetadataDBGateway(connectionSpy).initializeSchema(is);
        verify(connectionSpy).createStatement();
    }

    @Test
    public void testInitializeSchemaWhenError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).createStatement();
        InputStream is = new ByteArrayInputStream(new byte[0]);
        assertThrows(StoreException.class, () -> new MetadataDBGateway(connectionSpy).initializeSchema(is));
    }
}