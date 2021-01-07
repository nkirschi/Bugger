package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;

import java.beans.Transient;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
class PostDBGatewayTest {

    private PostDBGateway gateway;
    private Connection connection;
    private Report testReport = new Report();
    private Selection testSelection;
    private int numberOfPosts;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new PostDBGateway(connection);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
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
        return new Post(postID, "testpost" + postID, new Lazy<>(testReport), null, null);
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
        assertThrows(IllegalArgumentException.class, () -> gateway.selectPostsOfReport(new Report(), testSelection));
    }

    @Test
    public void testSelectPostsOfReportWhenThereAreSome() throws Exception {
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
        validSelection();
        testReport.setId(100);
        insertReport();
        assertTrue(gateway.selectPostsOfReport(testReport, testSelection).isEmpty());
    }

    @Test
    public void testSelectPostsOfReportWhenReportDoesNotExist() {
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
        for (int i = 120; i < 140; i++) {
            expected.add(makeTestPost(i));
        }
        assertEquals(expected, gateway.selectPostsOfReport(testReport, testSelection));
    }

    @Test
    public void testSelectPostsOfReportWhenSortedByCreatedAtDescending() throws Exception {
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
    public void testDeletePost() throws Exception {
        insertReport();
        numberOfPosts = 1;
        insertPosts(100);
        gateway.deletePost(makeTestPost(100));
        assertTrue(isGone(100));
    }

    @Test
    public void testDeletePostTwice() throws Exception {
        insertReport();
        numberOfPosts = 1;
        insertPosts(100);
        gateway.deletePost(makeTestPost(100));
        assertThrows(NotFoundException.class, () -> gateway.deletePost(makeTestPost(100)));
    }

    @Test
    public void testDeletePostWhenPostDoesNotExist() {
        assertThrows(NotFoundException.class, () -> gateway.deletePost(makeTestPost(4)));
    }

    @Test
    public void testDeletePostWhenPostIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.deletePost(null));
    }
}