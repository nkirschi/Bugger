package tech.bugger.persistence.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
@ExtendWith(MockitoExtension.class)
public class PostDBGatewayTest {

    private PostDBGateway gateway;

    @Mock
    private UserGateway userGateway;

    @Mock
    private AttachmentGateway attachmentGateway;

    private Connection connection;

    private Report report;

    private Post post;

    private final Report testReport = new Report();
    private Selection testSelection;
    private int numberOfPosts;

    @BeforeEach
    public void setUp() throws Exception {
        DBExtension.insertMinimalTestData();
        connection = DBExtension.getConnection();
        gateway = new PostDBGateway(connection, userGateway, attachmentGateway);

        report = new Report(100, "title", Report.Type.BUG, Report.Severity.MINOR, "", null, null, null, null, false, 0, null);
        Authorship authorship = new Authorship(new User(), OffsetDateTime.now(), new User(), OffsetDateTime.now());
        authorship.getCreator().setId(1);
        authorship.getModifier().setId(1);
        post = new Post(10000, "test.txt", report.getId(), authorship, new ArrayList<>());
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testFind() throws Exception {
        Post post = gateway.find(100);

        // Check if post is equal to post from test data.
        assertAll(() -> assertEquals(100, post.getId()),
                () -> assertEquals("testpost", post.getContent())
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
        assertThrows(StoreException.class,
                () -> new PostDBGateway(connectionSpy, mock(UserGateway.class), mock(AttachmentGateway.class))
                        .find(100));
    }

    @Test
    public void testCreate() throws Exception {
        gateway.create(post);
        assertEquals(post, gateway.find(post.getId()));
    }

    @Test
    public void testCreateNoCreatorAndModifier() throws NotFoundException {
        Authorship authorship = new Authorship(null, OffsetDateTime.now(), null, OffsetDateTime.now());
        post.setAuthorship(authorship);
        List<Attachment> attachments = new ArrayList<>();
        Attachment attachment = new Attachment(102, "text.txt", new byte[]{1}, "text/plain", 0);
        attachments.add(attachment);
        post.setAttachments(attachments);
        gateway.create(post);
        Post findPost = gateway.find(post.getId());
        assertAll(
                () -> assertEquals(post.getId(), findPost.getId()),
                () -> assertEquals(post.getContent(), findPost.getContent()),
                () -> assertEquals(attachment.getPost(), post.getId())
        );
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
                () -> new PostDBGateway(connectionSpy, userGateway, attachmentGateway).create(post));
    }

    @Test
    public void testCreateWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any(), anyInt());
        assertThrows(StoreException.class,
                () -> new PostDBGateway(connectionSpy, userGateway, attachmentGateway).create(post));
    }

    @Test
    public void testUpdate() throws NotFoundException {
        post.setId(100);
        gateway.update(post);
        Post findPost = gateway.find(post.getId());
        assertAll(
                () -> assertEquals(post.getContent(), findPost.getContent()),
                () -> assertEquals(post.getReport(), findPost.getReport())
        );
    }

    @Test
    public void testUpdateNoModifier() {
        Authorship authorship = new Authorship(new User(), OffsetDateTime.now(), null, OffsetDateTime.now());
        post.setAuthorship(authorship);
        post.setId(100);
        assertDoesNotThrow(() -> gateway.update(post));
    }

    @Test
    public void testUpdateNotFound() {
        assertThrows(NotFoundException.class,
                () -> gateway.update(post)
        );
    }

    @Test
    public void testUpdateSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new PostDBGateway(connectionSpy, userGateway, attachmentGateway).update(post)
        );
    }

    public void validSelection() {
        testSelection = new Selection(42, 0, Selection.PageSize.NORMAL, "id", true);
    }

    public void insertReport() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO topic (title, description) VALUES ('topic1', 'description1');");
            stmt.execute("INSERT INTO report (title, type, severity, topic) VALUES ('HI', 'BUG', 'MINOR', 1);");
        }
    }

    public void insertPosts(int reportID) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DO\n" +
                    "$$\n" +
                    "BEGIN\n" +
                    "FOR i IN 1.." + numberOfPosts + " LOOP\n" +
                    "    INSERT INTO post (content, report) VALUES\n" +
                    "        (CONCAT('testpost', CURRVAL('post_id_seq'))," + reportID + ");\n" +
                    "END LOOP;\n" +
                    "END;\n" +
                    "$$\n" +
                    ";\n");
        }
    }

    public List<Post> expectedPosts() {
        List<Post> expected = new ArrayList<>(numberOfPosts);
        for (int i = 0; i < numberOfPosts; i++) {
            expected.add(makeTestPost(100 + i));
        }
        return expected;
    }

    public Post makeTestPost(int postID) {
        return new Post(postID, "testpost" + postID, testReport.getId(), null, null);
    }

    public boolean isGone(int postID) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM post WHERE id = " + postID);
            return (!rs.next());
        }
    }

    @Test
    public void testSelectPostsOfReportWhenReportIsNull() {
        validSelection();
        assertThrows(IllegalArgumentException.class, () -> gateway.selectPostsOfReport(null, testSelection));
    }

    @Test
    public void testSelectPostsOfReportWhenSelectionIsNull() {
        testReport.setId(42);
        assertThrows(IllegalArgumentException.class, () -> gateway.selectPostsOfReport(testReport, null));
    }

    @Test
    public void testSelectPostsOfReportWhenReportIDIsNull() {
        validSelection();
        Report report = new Report();
        report.setId(null);
        assertThrows(IllegalArgumentException.class, () -> gateway.selectPostsOfReport(report, testSelection));
    }

    @Test
    public void testSelectPostsOfReportWhenThereAreSome() throws Exception {
        DBExtension.emptyDatabase();
        validSelection();
        testSelection.setSortedBy("id");
        testReport.setId(100);
        insertReport();
        numberOfPosts = 5;
        insertPosts(100);
        assertEquals(expectedPosts(), gateway.selectPostsOfReport(testReport, testSelection));
    }

    @Test
    public void testSelectPostsOfReportWhenThereAreNone() throws Exception {
        DBExtension.emptyDatabase();
        validSelection();
        testReport.setId(100);
        insertReport();
        assertTrue(gateway.selectPostsOfReport(testReport, testSelection).isEmpty());
    }

    @Test
    public void testSelectPostsOfReportWhenReportDoesNotExist() throws Exception {
        validSelection();
        testReport.setId(12);
        assertTrue(gateway.selectPostsOfReport(testReport, testSelection).isEmpty());
    }

    @Test
    public void testSelectPostsOfReportSecondPage() throws Exception {
        validSelection();
        testReport.setId(100);
        insertReport();
        numberOfPosts = 50;
        insertPosts(100);
        testSelection.setCurrentPage(1);
        List<Post> expected = new ArrayList<>(20);
        for (int i = 121; i <= 140; i++) {
            expected.add(makeTestPost(i));
        }
        assertEquals(expected, gateway.selectPostsOfReport(testReport, testSelection));
    }

    @Test
    public void testSelectPostsOfReportWhenSortedByCreatedAtDescending() throws Exception {
        DBExtension.emptyDatabase();
        insertReport();
        numberOfPosts = 1;
        for (int i = 0; i < 10; i++) {
            insertPosts(100);
        }
        testReport.setId(100);
        validSelection();
        testSelection.setSortedBy("created_at");
        testSelection.setAscending(false);
        numberOfPosts = 10;
        List<Post> expected = expectedPosts();
        Collections.reverse(expected);
        assertEquals(expected, gateway.selectPostsOfReport(testReport, testSelection));
    }

    @Test
    public void testSelectPostsCreatorAndModifier() throws NotFoundException {
        Selection selection = new Selection(1, 0, Selection.PageSize.SMALL, "id", true);
        gateway.create(post);
        List<Post> posts = gateway.selectPostsOfReport(report, selection);
        assertEquals(2, posts.size());
    }

    @Test
    public void testSelectPostsStoreException() throws SQLException {
        Selection selection = new Selection(1, 0, Selection.PageSize.SMALL, "id", true);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new PostDBGateway(connectionSpy, userGateway, attachmentGateway).selectPostsOfReport(report,
                        selection)
        );
    }

    @Test
    public void testDeletePost() throws Exception {
        insertReport();
        numberOfPosts = 1;
        insertPosts(100);
        gateway.delete(makeTestPost(100));
        assertTrue(isGone(100));
    }

    @Test
    public void testDeletePostTwice() throws Exception {
        insertReport();
        numberOfPosts = 1;
        insertPosts(100);
        gateway.delete(makeTestPost(100));
        assertThrows(NotFoundException.class, () -> gateway.delete(makeTestPost(100)));
    }

    @Test
    public void testDeletePostInternalError() throws SQLException {
        post.setId(100);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        doReturn(true).when(rs).next();
        doReturn(1000).when(rs).getInt("id");
        assertThrows(InternalError.class,
                () -> new PostDBGateway(connectionSpy, userGateway, attachmentGateway).delete(post)
        );
    }

    @Test
    public void testDeletePostStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new PostDBGateway(connectionSpy, userGateway, attachmentGateway).delete(post)
        );
    }

    @Test
    public void testDeletePostWhenPostDoesNotExist() {
        assertThrows(NotFoundException.class, () -> gateway.delete(makeTestPost(4)));
    }

    @Test
    public void testDeletePostWhenPostIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.delete(null));
    }

    @Test
    public void testGetFirstPost() throws NotFoundException {
        assertEquals("testpost", gateway.getFirstPost(report).getContent());
    }

    @Test
    public void testGetFirstPostNotFound() {
        report.setId(1000);
        assertThrows(NotFoundException.class,
                () -> gateway.getFirstPost(report)
        );
    }

    @Test
    public void testGetFirstPostStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new PostDBGateway(connectionSpy, userGateway, attachmentGateway).getFirstPost(report)
        );
    }

    @Test
    public void testGetFirstPostReportNull() {
        assertThrows(IllegalArgumentException.class,
                () -> gateway.getFirstPost(null)
        );
    }

    @Test
    public void testGetFirstPostReportIdNull() {
        report.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.getFirstPost(report)
        );
    }

}