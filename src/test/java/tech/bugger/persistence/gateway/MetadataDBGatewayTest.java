package tech.bugger.persistence.gateway;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;
import tech.bugger.persistence.exception.StoreException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(LogExtension.class)
public class MetadataDBGatewayTest {
    private static EmbeddedPostgres pg;

    private MetadataDBGateway gateway;
    private Connection connection;

    @BeforeAll
    public static void setUpAll() throws Exception {
        pg = EmbeddedPostgres.builder().setPort(42424).start();
    }

    @AfterAll
    public static void tearDownAll() throws Exception {
        pg.close();
    }

    @BeforeEach
    public void setUp() throws Exception {
        connection = pg.getPostgresDatabase().getConnection();
        gateway = new MetadataDBGateway(connection);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    private void createTable() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE metadata ("
                    + "    id INTEGER NOT NULL PRIMARY KEY DEFAULT 0,"
                    + "    version VARCHAR DEFAULT '1.0',\n"
                    + "    CONSTRAINT metadata_only_one_row CHECK (id = 0)"
                    + ");");
        } catch (SQLException e) {
            fail(e);
        }
    }

    private void dropTable() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS metadata;");
        } catch (SQLException e) {
            fail(e);
        }
    }

    private void insertDefaults() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO metadata DEFAULT VALUES;");
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    public void testRetrieveMetadataWhenTableDoesNotExist() {
        assertNull(gateway.retrieveMetadata());
    }

    @Test
    public void testRetrieveMetadataWhenTableExistsButEntryDoesNot() {
        createTable();
        assertNull(gateway.retrieveMetadata());
        dropTable();
    }

    @Test
    public void testRetrieveMetadataWhenTableAndEntryExist() {
        createTable();
        insertDefaults();
        assertEquals("1.0", gateway.retrieveMetadata().getVersion());
        dropTable();
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