package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
public class AttachmentDBGatewayTest {

    private AttachmentDBGateway gateway;
    private Connection connection;
    private Post post;
    private Attachment attachment;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new AttachmentDBGateway(connection);

        post = new Post(100, "", null, null, null);
        byte[] content = "Some random byte string".getBytes();
        attachment = new Attachment(2, "test.txt", new Lazy<>(content), "text/plain", new Lazy<>(post));
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    private Attachment find(int id) throws Exception {
        // necessary until AttachmentDBGateway#find is implemented
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM attachment WHERE id = ?;");
        ResultSet rs = new StatementParametrizer(stmt).integer(id).toStatement().executeQuery();

        if (rs.next()) {
            return new Attachment(
                    rs.getInt("id"),
                    rs.getString("name"),
                    new Lazy<>(rs.getBytes("content")),
                    rs.getString("mimetype"),
                    null);
        } else {
            return null;
        }
    }

    @Test
    public void testCreate() throws Exception {
        gateway.create(attachment);
        assertEquals(attachment, find(attachment.getId()));
    }

    @Test
    public void testCreateNoKeysGenerated() throws Exception {
        Connection connectionSpy = spy(connection);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        ResultSet rsMock = mock(ResultSet.class);
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any(), anyInt());
        when(stmtMock.getGeneratedKeys()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(false);
        assertThrows(StoreException.class,
                () -> new AttachmentDBGateway(connectionSpy).create(attachment));
    }

    @Test
    public void testCreateWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any(), anyInt());
        Attachment attachmentMock = mock(Attachment.class);
        assertThrows(StoreException.class,
                () -> new AttachmentDBGateway(connectionSpy).create(attachmentMock));
    }

    @Test
    public void testUpdate() throws Exception {
        Attachment attachment = find(1);
        attachment.setPost(new Lazy<>(post));
        attachment.setName("a-different-file-name.txt");
        gateway.update(attachment);
        assertEquals(attachment, find(attachment.getId()));
    }

    @Test
    public void testUpdateWhenNotExists() throws Exception {
        attachment.setId(42);
        assertThrows(NotFoundException.class, () -> gateway.update(attachment));
    }

    @Test
    public void testUpdateWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        Attachment attachmentMock = mock(Attachment.class);
        assertThrows(StoreException.class,
                () -> new AttachmentDBGateway(connectionSpy).update(attachmentMock));
    }

    @Test
    public void testDelete() throws Exception {
        Attachment attachment = find(1);
        gateway.delete(attachment);
        assertEquals(null, find(1));
    }

    @Test
    public void testDeleteWhenNotExists() throws Exception {
        attachment.setId(42);
        assertThrows(NotFoundException.class, () -> gateway.update(attachment));
    }

    @Test
    public void testDeleteWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        Attachment attachmentMock = mock(Attachment.class);
        assertThrows(StoreException.class,
                () -> new AttachmentDBGateway(connectionSpy).delete(attachmentMock));
    }

}
