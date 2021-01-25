package tech.bugger.business.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.event.Event;
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
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.SelfReferenceException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.PostGateway;
import tech.bugger.persistence.gateway.ReportGateway;
import tech.bugger.persistence.gateway.SubscriptionGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

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

    private User testUser;

    private Report testReport;

    private Post testFirstPost;

    private final Post testPost = new Post(101, "my content", 0, null, null);

    private final Selection selection = new Selection(2, 0, Selection.PageSize.SMALL, "id", true);

    private static final int VOTING_WEIGHT = 10;

    @BeforeEach
    public void setUp() {
        service = new ReportService(notificationService, topicService, postService, profileService, transactionManager,
                feedbackEvent, ResourceBundleMocker.mock(""));
        List<Attachment> attachments = List.of(new Attachment(), new Attachment(), new Attachment());
        testFirstPost = new Post(100, "Some content", 42, mock(Authorship.class), attachments);
        testUser = new User();
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
    public void testSubscribeToReport() throws NotFoundException, DuplicateException {
        service.subscribeToReport(testUser, testReport);
        verify(subscriptionGateway).subscribe(testReport, testUser);
    }

    @Test
    public void testSubscribeToReportNotFound() throws NotFoundException, DuplicateException {
        doThrow(NotFoundException.class).when(subscriptionGateway).subscribe(testReport, testUser);
        service.subscribeToReport(testUser, testReport);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSubscribeToReportDuplicate() throws NotFoundException, DuplicateException {
        doThrow(DuplicateException.class).when(subscriptionGateway).subscribe(testReport, testUser);
        service.subscribeToReport(testUser, testReport);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSubscribeToReportTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.subscribeToReport(testUser, testReport);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSubscribeToReportUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.subscribeToReport(null, testReport)
        );
    }

    @Test
    public void testSubscribeToReportUserIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.subscribeToReport(testUser, testReport)
        );
    }

    @Test
    public void testSubscribeToReportReportNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.subscribeToReport(testUser, null)
        );
    }

    @Test
    public void testSubscribeToReportReportIdNull() {
        testReport.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.subscribeToReport(testUser, testReport)
        );
    }

    @Test
    public void testUnsubscribeFromReport() throws NotFoundException {
        service.unsubscribeFromReport(testUser, testReport);
        verify(subscriptionGateway).unsubscribe(testReport, testUser);
    }

    @Test
    public void testUnsubscribeFromReportNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(subscriptionGateway).unsubscribe(testReport, testUser);
        service.unsubscribeFromReport(testUser, testReport);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUnsubscribeFromReportTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.unsubscribeFromReport(testUser, testReport);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUnsubscribeFromReportUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.unsubscribeFromReport(null, testReport)
        );
    }

    @Test
    public void testUnsubscribeFromReportUserIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.unsubscribeFromReport(testUser, testReport)
        );
    }

    @Test
    public void testUnsubscribeFromReportReportNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.unsubscribeFromReport(testUser, null)
        );
    }

    @Test
    public void testUnsubscribeFromReportReportIdNull() {
        testReport.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.unsubscribeFromReport(testUser, testReport)
        );
    }

    @Test
    public void testIsSubscribed() throws NotFoundException {
        doReturn(true).when(subscriptionGateway).isSubscribed(testUser, testReport);
        assertTrue(service.isSubscribed(testUser, testReport));
        verify(subscriptionGateway).isSubscribed(testUser, testReport);
    }

    @Test
    public void testIsSubscribedFalse() throws NotFoundException {
        assertFalse(service.isSubscribed(testUser, testReport));
        verify(subscriptionGateway).isSubscribed(testUser, testReport);
    }

    @Test
    public void testIsSubscribedNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(subscriptionGateway).isSubscribed(testUser, testReport);
        assertFalse(service.isSubscribed(testUser, testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsSubscribedTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.isSubscribed(testUser, testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetReportByIDWhenExists() throws Exception {
        testReport.setId(100);
        doReturn(testReport).when(reportGateway).find(anyInt());
        assertEquals(testReport, service.getReportByID(100));
    }

    @Test
    public void testIsSubscribedUserNull() {
        assertFalse(service.isSubscribed(null, testReport));
    }

    @Test
    public void testIsSubscribedUserIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.isSubscribed(testUser, testReport)
        );
    }

    @Test
    public void testIsSubscribedReportNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.isSubscribed(testUser, null)
        );
    }

    @Test
    public void testIsSubscribedReportIdNull() {
        testReport.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.isSubscribed(testUser, testReport)
        );
    }

    @Test
    public void testClose() throws NotFoundException {
        service.close(testReport);
        assertNotNull(testReport.getClosingDate());
        verify(reportGateway).update(testReport);
    }

    @Test
    public void testCloseNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(reportGateway).update(testReport);
        service.close(testReport);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testCloseTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.close(testReport);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testOpen() throws NotFoundException {
        service.close(testReport);
        service.open(testReport);
        assertNull(testReport.getClosingDate());
        verify(reportGateway, times(2)).update(testReport);
    }

    @Test
    public void testOpenNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(reportGateway).update(testReport);
        service.open(testReport);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testOpenTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.open(testReport);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpvote() throws DuplicateException, NotFoundException {
        doReturn(VOTING_WEIGHT).when(profileService).getVotingWeightForUser(testUser);
        service.upvote(testReport, testUser);
        verify(reportGateway).addVote(testReport, testUser, VOTING_WEIGHT);
    }

    @Test
    public void testUpvoteNotFound() throws DuplicateException, NotFoundException {
        doReturn(VOTING_WEIGHT).when(profileService).getVotingWeightForUser(testUser);
        doThrow(NotFoundException.class).when(reportGateway).addVote(testReport, testUser, VOTING_WEIGHT);
        service.upvote(testReport, testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpvoteDuplicate() throws DuplicateException, NotFoundException {
        doReturn(VOTING_WEIGHT).when(profileService).getVotingWeightForUser(testUser);
        doThrow(DuplicateException.class).when(reportGateway).addVote(testReport, testUser, VOTING_WEIGHT);
        service.upvote(testReport, testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpvoteTransaction() throws TransactionException {
        doReturn(VOTING_WEIGHT).when(profileService).getVotingWeightForUser(testUser);
        doNothing().doThrow(TransactionException.class).when(tx).commit();
        service.upvote(testReport, testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpvoteNoVotingWeight() throws DuplicateException, NotFoundException {
        doReturn(0).when(profileService).getVotingWeightForUser(testUser);
        service.upvote(testReport, testUser);
        verify(reportGateway, never()).addVote(any(), any(), anyInt());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDownvote() throws DuplicateException, NotFoundException {
        doReturn(VOTING_WEIGHT).when(profileService).getVotingWeightForUser(testUser);
        service.downvote(testReport, testUser);
        verify(reportGateway).addVote(testReport, testUser, -VOTING_WEIGHT);
    }

    @Test
    public void testDownvoteNotFound() throws DuplicateException, NotFoundException {
        doReturn(VOTING_WEIGHT).when(profileService).getVotingWeightForUser(testUser);
        doThrow(NotFoundException.class).when(reportGateway).addVote(testReport, testUser, -VOTING_WEIGHT);
        service.downvote(testReport, testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDownvoteDuplicate() throws DuplicateException, NotFoundException {
        doReturn(VOTING_WEIGHT).when(profileService).getVotingWeightForUser(testUser);
        doThrow(DuplicateException.class).when(reportGateway).addVote(testReport, testUser, -VOTING_WEIGHT);
        service.downvote(testReport, testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDownvoteTransaction() throws TransactionException {
        doReturn(VOTING_WEIGHT).when(profileService).getVotingWeightForUser(testUser);
        doNothing().doThrow(TransactionException.class).when(tx).commit();
        service.downvote(testReport, testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDownvoteNoVotingWeight() throws DuplicateException, NotFoundException {
        doReturn(0).when(profileService).getVotingWeightForUser(testUser);
        service.downvote(testReport, testUser);
        verify(reportGateway, never()).addVote(any(), any(), anyInt());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testRemoveVote() throws NotFoundException {
        service.removeVote(testReport, testUser);
        verify(reportGateway).removeVote(testReport, testUser);
    }

    @Test
    public void testRemoveVoteNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(reportGateway).removeVote(testReport, testUser);
        service.removeVote(testReport, testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testRemoveVoteTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.removeVote(testReport, testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testHasUpvoted() {
        doReturn(VOTING_WEIGHT).when(reportGateway).getVote(testUser, testReport);
        assertTrue(service.hasUpvoted(testReport, testUser));
    }

    @Test
    public void testHasUpvotedVotingWeightZero() {
        assertFalse(service.hasUpvoted(testReport, testUser));
    }

    @Test
    public void testHasUpvotedVotingWeightNull() {
        doReturn(null).when(reportGateway).getVote(testUser, testReport);
        assertFalse(service.hasUpvoted(testReport, testUser));
    }

    @Test
    public void testHasUpvotedTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.hasUpvoted(testReport, testUser));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testHasDownvoted() {
        doReturn(-VOTING_WEIGHT).when(reportGateway).getVote(testUser, testReport);
        assertTrue(service.hasDownvoted(testReport, testUser));
    }

    @Test
    public void testHasDownvotedVotingWeightZero() {
        assertFalse(service.hasDownvoted(testReport, testUser));
    }

    @Test
    public void testHasDownvotedVotingWeightNull() {
        doReturn(null).when(reportGateway).getVote(testUser, testReport);
        assertFalse(service.hasDownvoted(testReport, testUser));
    }

    @Test
    public void testHasDownvotedTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.hasDownvoted(testReport, testUser));
        verify(feedbackEvent).fire(any());
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
    public void testDeleteReport() throws NotFoundException {
        service.deleteReport(testReport);
        verify(reportGateway).delete(testReport);
    }

    @Test
    public void testDeleteReportNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(reportGateway).delete(testReport);
        service.deleteReport(testReport);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteReportTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.deleteReport(testReport);
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
    public void testOverwriteRelevance() throws NotFoundException {
        service.overwriteRelevance(testReport, VOTING_WEIGHT);
        verify(reportGateway).overwriteRelevance(testReport, VOTING_WEIGHT);
    }

    @Test
    public void testOverwriteRelevanceNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(reportGateway).overwriteRelevance(testReport, VOTING_WEIGHT);
        service.overwriteRelevance(testReport, VOTING_WEIGHT);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testOverwriteRelevanceTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.overwriteRelevance(testReport, null);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfPosts() throws NotFoundException {
        doReturn(VOTING_WEIGHT).when(reportGateway).countPosts(testReport);
        assertEquals(VOTING_WEIGHT, service.getNumberOfPosts(testReport));
    }

    @Test
    public void testGetNumberOfPostsNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(reportGateway).countPosts(testReport);
        assertEquals(0, service.getNumberOfPosts(testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfPostsTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, service.getNumberOfPosts(testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetPostsFor() throws NotFoundException {
        testFirstPost.setReport(testReport.getId());
        testPost.setId(testReport.getId());
        List<Post> posts = new ArrayList<>();
        posts.add(testPost);
        posts.add(testFirstPost);
        doReturn(posts).when(postGateway).selectPostsOfReport(testReport, selection);
        List<Post> findPosts = service.getPostsFor(testReport, selection);
        assertAll(
                () -> assertEquals(2, findPosts.size()),
                () -> assertTrue(findPosts.contains(testPost)),
                () -> assertTrue(findPosts.contains(testFirstPost))
        );
    }

    @Test
    public void testGetPostsForNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(postGateway).selectPostsOfReport(testReport, selection);
        assertNull(service.getPostsFor(testReport, selection));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetPostsForTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(service.getPostsFor(testReport, selection).isEmpty());
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
        assertFalse(service.canPostInReport(testUser, null));
    }

    @Test
    public void testCanPostInReportWhenAdministrator() {
        testUser.setAdministrator(true);
        assertTrue(service.canPostInReport(testUser, testReport));
    }

    @Test
    public void testCanPostInReportWhenTopicNull() {
        testReport.setTopicID(1234);
        doReturn(null).when(topicService).getTopicByID(1234);
        assertFalse(service.canPostInReport(testUser, testReport));
    }

    @Test
    public void testCanPostInReportWhenNotBanned() {
        Topic topic = new Topic(1234, "title", "description");
        testReport.setTopicID(1234);
        doReturn(topic).when(topicService).getTopicByID(1234);
        assertTrue(service.canPostInReport(testUser, testReport));
    }

    @Test
    public void testCanPostInReportWhenBanned() {
        Topic topic = new Topic(1234, "title", "description");
        testReport.setTopicID(1234);
        doReturn(topic).when(topicService).getTopicByID(1234);
        doReturn(true).when(topicService).isBanned(testUser, topic);
        assertFalse(service.canPostInReport(testUser, testReport));
    }

    @Test
    public void testFindReportOfPost() throws NotFoundException {
        doReturn(testReport.getId()).when(reportGateway).findReportOfPost(testPost.getId());
        assertEquals(testReport.getId(), service.findReportOfPost(testPost.getId()));
        verify(reportGateway).findReportOfPost(testPost.getId());
    }

    @Test
    public void testFindReportOfPostNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(reportGateway).findReportOfPost(testPost.getId());
        assertEquals(0, service.findReportOfPost(testPost.getId()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testFindReportOfPostsTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, service.findReportOfPost(testPost.getId()));
        verify(feedbackEvent).fire(any());
    }

}
