package tech.bugger.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.SelfReferenceException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.PostGateway;
import tech.bugger.persistence.gateway.ReportGateway;
import tech.bugger.persistence.gateway.SubscriptionGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    private ReportService service;

    @Mock
    private TopicService topicService;

    @Mock
    private ProfileService profileService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PostService postService;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private ReportGateway reportGateway;

    @Mock
    private PostGateway postGateway;

    @Mock
    private SubscriptionGateway subscriptionGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

    private Report testReport;

    private Post testFirstPost;

    @BeforeEach
    public void setUp() {
        service = new ReportService(notificationService, topicService, postService, profileService, transactionManager,
                feedbackEvent, ResourceBundleMocker.mock(""));
        List<Attachment> attachments = List.of(new Attachment(), new Attachment(), new Attachment());
        testFirstPost = new Post(100, "Some content", new Lazy<>(mock(Report.class)), mock(Authorship.class), attachments);
        User testUser = new User();
        testUser.setId(1);
        Authorship authorship = new Authorship(testUser, OffsetDateTime.now(), testUser, OffsetDateTime.now());
        testReport = new Report(200, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", authorship,
                mock(OffsetDateTime.class), null, null, false, 1);

        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(reportGateway).when(tx).newReportGateway();
        lenient().doReturn(postGateway).when(tx).newPostGateway();
        lenient().doReturn(subscriptionGateway).when(tx).newSubscriptionGateway();
    }

    @Test
    public void testGetReportByIDWhenExists() throws Exception {
        testReport.setId(100);
        doReturn(testReport).when(reportGateway).find(anyInt());
        assertEquals(testReport, service.getReportByID(100));
    }

    @Test
    public void testGetReportByIDWhenNotExists() throws Exception {
        doThrow(NotFoundException.class).when(reportGateway).find(anyInt());
        assertNull(service.getReportByID(100));
    }

    @Test
    public void testGetReportByIDWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(service.getReportByID(100));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMoveReportWhenFine() throws Exception {
        assertTrue(service.move(testReport));
        verify(reportGateway).update(testReport);
    }

    @Test
    public void testMoveReportWhenNotExists() throws Exception {
        doThrow(NotFoundException.class).when(reportGateway).update(any());
        assertFalse(service.move(testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMoveReportWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.move(testReport));
    }

    @Test
    public void testUpdateReportWhenFine() throws Exception {
        assertTrue(service.updateReport(testReport));
        verify(reportGateway).update(testReport);
    }

    @Test
    public void testUpdateReportWhenNotExists() throws Exception {
        doThrow(NotFoundException.class).when(reportGateway).update(any());
        assertFalse(service.updateReport(testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpdateReportWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.updateReport(testReport));
    }

    @Test
    public void testCreateReportWhenFine() throws Exception {
        doReturn(true).when(postService).createPostWithTransaction(any(), any());
        assertTrue(service.createReport(testReport, testFirstPost));
        verify(reportGateway).create(any());
        verify(tx, times(2)).commit();
    }

    @Test
    public void testCreateReportWhenPostCreationFails() {
        assertFalse(service.createReport(testReport, testFirstPost));
        verify(tx).abort();
    }

    @Test
    public void testCreateReportWhenCommitFails() throws Exception {
        doReturn(true).when(postService).createPostWithTransaction(any(), any());
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.createReport(testReport, testFirstPost));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMarkDuplicateWhenSelfReference() {
        assertFalse(service.markDuplicate(testReport, testReport.getId()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMarkDuplicateWhenOriginalNotFound() {
        ReportService service = spy(this.service);
        doReturn(null).when(service).getReportByID(anyInt());
        assertFalse(service.markDuplicate(testReport, testReport.getId() - 1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMarkDuplicateWhenOriginalOfItself() {
        Report original = new Report();
        original.setDuplicateOf(testReport.getId());
        ReportService service = spy(this.service);
        doReturn(original).when(service).getReportByID(anyInt());
        assertFalse(service.markDuplicate(testReport, testReport.getId() - 1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMarkDuplicateWhenGatewaySelfReference() throws Exception {
        Report original = new Report();
        ReportService service = spy(this.service);
        doReturn(original).when(service).getReportByID(anyInt());
        doThrow(SelfReferenceException.class).when(reportGateway).markDuplicate(any(), anyInt());
        assertFalse(service.markDuplicate(testReport, testReport.getId() - 1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMarkDuplicateWhenGatewayCouldNotFindOriginalReport() throws Exception {
        Report original = new Report();
        ReportService service = spy(this.service);
        doReturn(original).when(service).getReportByID(anyInt());
        doThrow(NotFoundException.class).when(reportGateway).markDuplicate(any(), anyInt());
        assertFalse(service.markDuplicate(testReport, testReport.getId() - 1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMarkDuplicateWhenTransactionError() throws Exception {
        Report original = new Report();
        ReportService service = spy(this.service);
        doReturn(original).when(service).getReportByID(anyInt());
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.markDuplicate(testReport, testReport.getId() - 1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMarkDuplicateWhenSuccess() {
        Report original = new Report();
        ReportService service = spy(this.service);
        doReturn(original).when(service).getReportByID(anyInt());
        assertTrue(service.markDuplicate(testReport, testReport.getId() - 1));
    }

    @Test
    public void testMarkDuplicateWhenOriginalIDPropagate() throws Exception {
        Report original = new Report();
        original.setDuplicateOf(testReport.getId() - 1);
        ReportService service = spy(this.service);
        doReturn(original).when(service).getReportByID(anyInt());
        assertTrue(service.markDuplicate(testReport, testReport.getId() - 1));
        verify(reportGateway).markDuplicate(any(), eq(original.getDuplicateOf()));
    }

    @Test
    public void testUnmarkDuplicateSuccess() {
        assertTrue(service.unmarkDuplicate(testReport));
    }

    @Test
    public void testUnmarkDuplicateWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(reportGateway).unmarkDuplicate(testReport);
        assertFalse(service.unmarkDuplicate(testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUnmarkDuplicateWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.unmarkDuplicate(testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSelectDuplicatesSuccess() {
        List<Report> reports = List.of(new Report(), new Report());
        doReturn(reports).when(reportGateway).selectDuplicates(any(), any());
        assertEquals(reports, service.getDuplicatesFor(testReport, mock(Selection.class)));
    }

    @Test
    public void testGetDuplicatesForWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(List.of(), service.getDuplicatesFor(testReport, mock(Selection.class)));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfDuplicatesSuccess() throws Exception {
        doReturn(2).when(reportGateway).countDuplicates(testReport);
        assertEquals(2, service.getNumberOfDuplicates(testReport));
    }

    @Test
    public void testGetNumberOfDuplicatesWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(reportGateway).countDuplicates(testReport);
        assertEquals(0, service.getNumberOfDuplicates(testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfDuplicatesWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, service.getNumberOfDuplicates(testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testCanPostInReportWhenUserNull() {
        assertFalse(service.canPostInReport(null, testReport));
    }

    @Test
    public void testCanPostInReportWhenReportNull() {
        assertFalse(service.canPostInReport(new User(), null));
    }

    @Test
    public void testCanPostInReportWhenAdministrator() {
        User user = new User();
        user.setAdministrator(true);
        assertTrue(service.canPostInReport(user, testReport));
    }

    @Test
    public void testCanPostInReportWhenTopicNull() {
        testReport.setTopicID(1234);
        doReturn(null).when(topicService).getTopicByID(1234);
        assertFalse(service.canPostInReport(new User(), testReport));
    }

    @Test
    public void testCanPostInReportWhenNotBanned() {
        Topic topic = new Topic(1234, "title", "description");
        testReport.setTopicID(1234);
        doReturn(topic).when(topicService).getTopicByID(1234);
        User user = new User();
        doReturn(false).when(topicService).isBanned(user, topic);
        assertTrue(service.canPostInReport(new User(), testReport));
    }

}