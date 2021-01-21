package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Post;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
        DBExtension.insertMinimalTestData();
        connection = DBExtension.getConnection();
        gateway = new AttachmentDBGateway(connection);

        post = new Post(100, "", 0, null, null);
        byte[] content = "Some random byte string".getBytes();
        attachment = new Attachment(10000, "test.txt", content, "text/plain", post.getId());
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
                    rs.getBytes("content"),
                    rs.getString("mimetype"),
                    0);
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
        assertThrows(StoreException.class, () -> new AttachmentDBGateway(connectionSpy).create(attachment));
    }

    @Test
    public void testCreateWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any(), anyInt());
        assertThrows(StoreException.class, () -> new AttachmentDBGateway(connectionSpy).create(attachment));
    }

    @Test
    public void testUpdate() throws Exception {
        Attachment attachment = find(1);
        attachment.setPost(post.getId());
        attachment.setName("a-different-file-name.txt");
        gateway.update(attachment);
        assertEquals(attachment, find(attachment.getId()));
    }

    @Test
    public void testUpdateWhenNotExists() {
        attachment.setId(42);
        assertThrows(NotFoundException.class, () -> gateway.update(attachment));
    }

    @Test
    public void testUpdateWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        Attachment attachmentMock = mock(Attachment.class);
        assertThrows(StoreException.class, () -> new AttachmentDBGateway(connectionSpy).update(attachmentMock));
    }

    @Test
    public void testDelete() throws Exception {
        Attachment attachment = find(1);
        gateway.delete(attachment);
        assertEquals(null, find(1));
    }

    @Test
    public void testDeleteWhenNotExists() {
        attachment.setId(42);
        assertThrows(NotFoundException.class, () -> gateway.delete(attachment));
    }

    @Test
    public void testDeleteWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        Attachment attachmentMock = mock(Attachment.class);
        assertThrows(StoreException.class, () -> new AttachmentDBGateway(connectionSpy).delete(attachmentMock));
    }

    @Test
    public void testFind() throws Exception {
        Attachment attachment = gateway.find(1);

        // Check if attachment is equal to attachment from test data.
        assertAll(() -> assertEquals(1, attachment.getId()),
                  () -> assertEquals("testattachment.txt", attachment.getName()),
                  () -> assertEquals("text/plain", attachment.getMimetype())
        );
    }

    @Test
    public void testFindWhenNotExists() {
        assertThrows(NotFoundException.class, () -> gateway.find(42));
    }

    @Test
    public void testFindWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new AttachmentDBGateway(connectionSpy).find(1));
    }

    @Test
    public void testFindContent() throws Exception {
        assertArrayEquals("testcontent".getBytes(), gateway.findContent(1));
    }

    @Test
    public void testFindContentWhenNotExists() {
        assertThrows(NotFoundException.class, () -> gateway.findContent(42));
    }

    @Test
    public void testFindContentWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new AttachmentDBGateway(connectionSpy).findContent(1));
    }

    @Test
    public void testGetAttachmentsForPost() {
        Post post = new Post(100, null, 0, null, null);
        List<Attachment> attachments = gateway.getAttachmentsForPost(post);

        // Check if attachments are equal to attachments from test data.
        assertAll(() -> assertEquals(1, attachments.get(0).getId()),
                  () -> assertEquals("testattachment.txt", attachments.get(0).getName()),
                  () -> assertEquals("text/plain", attachments.get(0).getMimetype()),
                  () -> assertEquals(2, attachments.get(1).getId()),
                  () -> assertEquals("another-attachment.png", attachments.get(1).getName()),
                  () -> assertEquals("image/png", attachments.get(1).getMimetype())
        );
    }

    @Test
    public void testGetAttachmentsForPostWhenDatabaseError() throws Exception {
        Post post = new Post(100, null, 0, null, null);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new AttachmentDBGateway(connectionSpy).getAttachmentsForPost(post));
    }

}
