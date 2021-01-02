package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
class TopicDBGatewayTest {

    private TopicDBGateway gateway;
    private Connection connection;
    private int numberOfTopics;
    private Selection selection;
    private List<Topic> topics;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new TopicDBGateway(connection);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    private void validSelection() {
        selection = new Selection(3, 0, Selection.PageSize.NORMAL, "id", true);
    }

    private void addTopics() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DO\n" +
                    "$$\n" +
                    "BEGIN\n" +
                    "FOR i IN 1.." + numberOfTopics + " LOOP\n" +
                    "    INSERT INTO topic (title, description) VALUES\n" +
                    "        (CONCAT('testtopic', CURRVAL('topic_id_seq')), CONCAT('Description for testtopic', CURRVAL('topic_id_seq')));\n" +
                    "END LOOP;\n" +
                    "END;\n" +
                    "$$\n" +
                    ";\n");
        }
    }

    private void expectedTopics(int number) {
        topics = new ArrayList<>(number);
        for (int i = 1; i <= number; i++) {
            Topic topic = new Topic(i, "testtopic" + i, "Description for testtopic" + i);
            topics.add(topic);
        }
    }

    private Topic makeTestTopic(int id) {
        return new Topic(id, "testtopic" + id, "Description for testtopic" + id);
    }

    @Test
    public void testCountTopicsWhenThereAreSome() throws Exception {
        numberOfTopics = 5;
        addTopics();
        assertEquals(numberOfTopics, gateway.countTopics());
    }

    @Test
    public void testCountTopicsWhenThereAreNone() {
        assertEquals(0, gateway.countTopics());
    }

    @Test
    public void testCountTopicsWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new TopicDBGateway(connectionSpy).countTopics());
    }

    @Test
    public void testCountTopicsWhenSNAFU() throws Exception {
        ResultSet resultSetMock = mock(ResultSet.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(false).when(resultSetMock).next();
        doReturn(resultSetMock).when(stmtMock).executeQuery();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any());
        assertThrows(InternalError.class, () -> new TopicDBGateway(connectionSpy).countTopics());
    }

    @Test
    public void testSelectTopicsWhenSelectionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.selectTopics(null));
    }

    @Test
    public void testSelectTopicsWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        validSelection();
        assertThrows(StoreException.class, () -> new TopicDBGateway(connectionSpy).selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenSelectionIsBlank() {
        validSelection();
        selection.setSortedBy("");
        assertThrows(IllegalArgumentException.class, () -> gateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenThereAreSome() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        expectedTopics(5);
        assertEquals(topics, gateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenSortedByIDDescending() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setAscending(false);
        selection.setSortedBy("id");
        expectedTopics(5);
        Collections.reverse(topics);
        assertEquals(topics, gateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenThereAreNone() {
        validSelection();
        assertTrue(gateway.selectTopics(selection).isEmpty());
    }

    @Test
    public void testSelectTopicsWhenCurrentPageIsInvalid() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setCurrentPage(-1);
        assertThrows(StoreException.class, () -> gateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenSortedByIsInvalid() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setSortedBy("hüttenkäse");
        assertThrows(StoreException.class, () -> gateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenTotalSizeIsTooBig() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setTotalSize(300);
        expectedTopics(5);
        assertEquals(topics, gateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenTotalSizeIsNegative() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setTotalSize(-11);
        expectedTopics(5);
        assertEquals(topics, gateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenTotalSizeIsTooSmall() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setTotalSize(3);
        expectedTopics(5);
        assertEquals(topics, gateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenThereAreTooMany() throws Exception {
        numberOfTopics = 50;
        addTopics();
        validSelection();
        expectedTopics(20);
        assertEquals(topics, gateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenThereAreTooManySortedByTitleDescending() throws Exception {
        numberOfTopics = 50;
        addTopics();
        validSelection();
        selection.setSortedBy("title");
        selection.setAscending(false);
        topics = new ArrayList<>(20);
        for (int i = 9; i >= 6; i--) {
            topics.add(makeTestTopic(i));
        }
        topics.add(makeTestTopic(50));
        topics.add(makeTestTopic(5));
        for (int i = 49; i >= 40; i--) {
            topics.add(makeTestTopic(i));
        }
        topics.add(makeTestTopic(4));
        for (int i = 39; i >= 37; i--) {
            topics.add(makeTestTopic(i));
        }
        assertEquals(topics, gateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenSortedByLastActivityWithNoActivity() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setSortedBy("last_activity");
        expectedTopics(5);
        assertEquals(topics, gateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenSortedByLastActivityWithSomeActivity() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setSortedBy("last_activity");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO report (title, type, severity, topic) VALUES ('Hello', 'BUG', 'MINOR', 5)");
        }
        expectedTopics(4);
        topics.add(0, makeTestTopic(5));
        assertEquals(topics, gateway.selectTopics(selection));
    }

    @Test
    public void testDetermineLastActivityWhenTopicIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.determineLastActivity(null));
    }

    @Test
    public void testDetermineLastActivityWhenTopicIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.determineLastActivity(new Topic()));
    }

    @Test
    public void testDetermineLastActivityWhenNoActivity() throws Exception {
        numberOfTopics = 1;
        addTopics();
        Topic topic = makeTestTopic(1);
        assertNull(gateway.determineLastActivity(topic));
    }

    @Test
    public void testDetermineLastActivityWhenThereWasSomeActivity() throws Exception {
        numberOfTopics = 1;
        addTopics();
        Topic topic = makeTestTopic(1);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO report (title, type, severity, topic) VALUES ('Hello', 'BUG', 'MINOR', 1)");
        }
        assertNotNull(gateway.determineLastActivity(topic));
    }

    @Test
    public void testDetermineLastActivityWhenTopicNotFound() {
        Topic topic = makeTestTopic(42);
        assertThrows(NotFoundException.class, () -> gateway.determineLastActivity(topic));
    }

    @Test
    public void testGetNumberOfTopicsWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new TopicDBGateway(connectionSpy).getNumberOfTopics());
    }

    @Test
    public void testGetSelectedTopicsWhenSelectionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.getSelectedTopics(null));
    }

    @Test
    public void testGetSelectedTopicsWhenThereAreSome() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        expectedTopics(5);
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenSortedByIDDescending() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setAscending(false);
        selection.setSortedBy("id");
        expectedTopics(5);
        Collections.reverse(topics);
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenThereAreNone() {
        validSelection();
        assertThrows(NotFoundException.class, () -> gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenCurrentPageIsInvalid() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setCurrentPage(-1);
        assertThrows(StoreException.class, () -> gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenSortedByIsInvalid() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setSortedBy("hüttenkäse");
        assertThrows(StoreException.class, () -> gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenTotalSizeIsTooBig() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setTotalSize(300);
        expectedTopics(5);
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenTotalSizeIsNegative() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setTotalSize(-11);
        expectedTopics(5);
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenTotalSizeIsTooSmall() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setTotalSize(3);
        expectedTopics(5);
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenThereAreTooMany() throws Exception {
        numberOfTopics = 50;
        addTopics();
        validSelection();
        expectedTopics(20);
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenThereAreTooManySortedByTitleDescending() throws Exception {
        numberOfTopics = 50;
        addTopics();
        validSelection();
        selection.setSortedBy("title");
        selection.setAscending(false);
        topics = new ArrayList<>(20);
        for (int i = 9; i >= 6; i--) {
            topics.add(makeTestTopic(i));
        }
        topics.add(makeTestTopic(50));
        topics.add(makeTestTopic(5));
        for (int i = 49; i >= 40; i--) {
            topics.add(makeTestTopic(i));
        }
        topics.add(makeTestTopic(4));
        for (int i = 39; i >= 37; i--) {
            topics.add(makeTestTopic(i));
        }
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetNumberOfTopicsWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new TopicDBGateway(connectionSpy).getNumberOfTopics());
    }

    @Test
    public void testGetSelectedTopicsWhenSelectionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.getSelectedTopics(null));
    }

    @Test
    public void testGetSelectedTopicsWhenThereAreSome() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        expectedTopics(5);
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenSortedByIDDescending() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setAscending(false);
        selection.setSortedBy("id");
        expectedTopics(5);
        Collections.reverse(topics);
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenThereAreNone() {
        validSelection();
        assertThrows(NotFoundException.class, () -> gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenCurrentPageIsInvalid() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setCurrentPage(-1);
        assertThrows(StoreException.class, () -> gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenSortedByIsInvalid() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setSortedBy("hüttenkäse");
        assertThrows(StoreException.class, () -> gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenTotalSizeIsTooBig() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setTotalSize(300);
        expectedTopics(5);
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenTotalSizeIsNegative() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setTotalSize(-11);
        expectedTopics(5);
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenTotalSizeIsTooSmall() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setTotalSize(3);
        expectedTopics(5);
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenThereAreTooMany() throws Exception {
        numberOfTopics = 50;
        addTopics();
        validSelection();
        expectedTopics(20);
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }

    @Test
    public void testGetSelectedTopicsWhenThereAreTooManySortedByTitleDescending() throws Exception {
        numberOfTopics = 50;
        addTopics();
        validSelection();
        selection.setSortedBy("title");
        selection.setAscending(false);
        topics = new ArrayList<>(20);
        for (int i = 9; i >= 6; i--) {
            topics.add(makeTestTopic(i));
        }
        topics.add(makeTestTopic(50));
        topics.add(makeTestTopic(5));
        for (int i = 49; i >= 40; i--) {
            topics.add(makeTestTopic(i));
        }
        topics.add(makeTestTopic(4));
        for (int i = 39; i >= 37; i--) {
            topics.add(makeTestTopic(i));
        }
        assertEquals(topics, gateway.getSelectedTopics(selection));
    }
}