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

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
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
        testSelection = new Selection(42, 0, Selection.PageSize.NORMAL, "", true);
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

}