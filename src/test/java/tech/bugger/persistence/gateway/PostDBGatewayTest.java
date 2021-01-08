package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;

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
public class PostDBGatewayTest {

    private PostDBGateway gateway;

    private Connection connection;

    private Report report;

    private Post post;

    @BeforeEach
    public void setUp() throws Exception {
        DBExtension.insertMinimalTestData();
        connection = DBExtension.getConnection();
        gateway = new PostDBGateway(connection);

        report = new Report(100, "title", Report.Type.BUG, Report.Severity.MINOR, "", null, null, null, null, 0);
        Authorship authorship = new Authorship(new User(), ZonedDateTime.now(), new User(), ZonedDateTime.now());
        authorship.getCreator().setId(1);
        authorship.getModifier().setId(1);
        post = new Post(10000, "test.txt", new Lazy<>(report), authorship, null);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    private Post find(int id) throws Exception {
        // necessary until PostDBGateway#find is implemented
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM post WHERE id = ?;");
        ResultSet rs = new StatementParametrizer(stmt).integer(id).toStatement().executeQuery();

        if (rs.next()) {
            return new Post(
                    rs.getInt("id"), rs.getString("content"),
                    null, null, null
            );
        } else {
            return null;
        }
    }

    @Test
    public void testCreate() throws Exception {
        gateway.create(post);
        assertEquals(post, find(post.getId()));
    }

    @Test
    public void testCreateNoKeysGenerated() throws Exception {
        Connection connectionSpy = spy(connection);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        ResultSet rsMock = mock(ResultSet.class);
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any(), anyInt());
        when(stmtMock.getGeneratedKeys()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(false);
        assertThrows(StoreException.class, () -> new PostDBGateway(connectionSpy).create(post));
    }

    @Test
    public void testCreateWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any(), anyInt());
        assertThrows(StoreException.class, () -> new PostDBGateway(connectionSpy).create(post));
    }

}
