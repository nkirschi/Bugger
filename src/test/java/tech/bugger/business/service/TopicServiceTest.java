package tech.bugger.business.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.TopicGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    private TopicService topicService;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private TopicGateway topicGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

    private List<Topic> testSelectedTopics;

    private int testNumberOfTopics;

    private Selection testSelection;

    private Topic testTopic1 = new Topic(1, "Hi", "senberg");
    private Topic testTopic2 = new Topic(2, "Hi", "performance");
    private Topic testTopic3 = new Topic(3, "Hi", "de and seek");

    @BeforeEach
    public void setUp() {
        topicService = new TopicService(transactionManager, feedbackEvent, ResourceBundleMocker.mock(""));
        testSelectedTopics = new ArrayList<>();
        testSelectedTopics.add(testTopic1);
        testSelectedTopics.add(testTopic2);
        testSelectedTopics.add(testTopic3);
        testNumberOfTopics = testSelectedTopics.size();
        testSelection = new Selection(3, 1, Selection.PageSize.NORMAL, "", true);
        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(topicGateway).when(tx).newTopicGateway();
    }

    @Test
    public void testSelectTopicsWhenFound() {
        doReturn(testSelectedTopics).when(topicGateway).selectTopics(any());
        assertEquals(testSelectedTopics, topicService.selectTopics(testSelection));
    }

    @Test
    public void testGetSelectedTopicsWhenNotFound() {
        doThrow(NotFoundException.class).when(topicGateway).selectTopics(any());
        assertNull(topicService.selectTopics(testSelection));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSelectTopicsWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(topicService.selectTopics(testSelection));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSelectTopicsWhenSelectionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> topicService.selectTopics(null));
    }

    @Test
    public void testCountTopics() {
        doReturn(testNumberOfTopics).when(topicGateway).countTopics();
        assertEquals(testNumberOfTopics, topicService.countTopics());
    }

    @Test
    public void testCountTopicsWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, topicService.countTopics());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testLastChange() throws Exception {
        ZonedDateTime lastChange = ZonedDateTime.now();
        doReturn(lastChange).when(topicGateway).determineLastActivity(any());
        assertEquals(lastChange, topicService.lastChange(testTopic1));
    }

    @Test
    public void testLastChangeWhenNoChange() throws Exception {
        doReturn(null).when(topicGateway).determineLastActivity(any());
        assertNull(topicService.lastChange(testTopic1));
    }

    @Test
    public void testLastChangeWhenTopicIsNull() {
        assertThrows(IllegalArgumentException.class, () -> topicService.lastChange(null));
    }

    @Test
    public void testLastChangeWhenTopicIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> topicService.lastChange(new Topic()));
    }

    @Test
    public void testLastChangeWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(topicGateway).determineLastActivity(any());
        assertNull(topicService.lastChange(testTopic1));
    }

    @Test
    public void testLastChangeWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(topicService.lastChange(testTopic1));
        verify(feedbackEvent).fire(any());
    }
}