package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.exception.DataAccessException;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.PostService;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Paginator;
import tech.bugger.business.util.Registry;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(LogExtension.class)
public class ReportBackerTest {

    private ReportBacker reportBacker;

    @Mock
    private TopicService topicService;

    @Mock
    private ReportService reportService;

    @Mock
    private PostService postService;

    @Mock
    private UserSession session;

    @Mock
    private ApplicationSettings settings;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Configuration configuration;

    @Mock
    private Map<String, String> requestParameterMap;

    @Mock
    private Event<Feedback> feedbackEvent;

    @Mock
    private Registry registry;

    private User user;
    private Report report;

    @BeforeEach
    public void setUp() {
        lenient().doReturn(requestParameterMap).when(ectx).getRequestParameterMap();
        lenient().doReturn(ResourceBundleMocker.mock("")).when(registry).getBundle(eq("messages"), any());
        reportBacker = new ReportBacker(settings, topicService, reportService, postService, session,
                feedbackEvent, registry, ectx);
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test",
                "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        report = new Report(100, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", mock(Authorship.class),
                mock(OffsetDateTime.class), null, null, false, 1, null);
    }

    @Test
    public void testIsPrivilegedUserNull() {
        reportBacker.setReport(report);
        assertFalse(reportBacker.isPrivileged());
    }

    @Test
    public void testIsBannedUserNull() {
        reportBacker.setReport(report);
        assertFalse(reportBacker.isBanned());
    }

    @Test
    public void testDisplayDialog() {
        assertAll(
                () -> assertNull(reportBacker.displayDialog(ReportBacker.Dialog.DELETE_POST)),
                () -> assertEquals(ReportBacker.Dialog.DELETE_POST, reportBacker.getCurrentDialog())
        );
    }

    @Test
    public void testDeletePostDialog() {
        Post post = new Post(42, "a", 100, null, null);
        assertAll(
                () -> assertNull(reportBacker.deletePostDialog(post)),
                () -> assertEquals(ReportBacker.Dialog.DELETE_POST, reportBacker.getCurrentDialog()),
                () -> assertEquals(post, reportBacker.getPostToBeDeleted())
        );
    }

    @Test
    public void testToggleReportSubscriptionWhenUserIsNull() {
        assertNull(reportBacker.toggleReportSubscription());
        verify(reportService, never()).isSubscribed(any(), any());
    }

    @Test
    public void testToggleReportSubscriptionWhenUserIsSubscribed() {
        doReturn(user).when(session).getUser();
        doReturn(true).when(reportService).isSubscribed(any(), any());
        assertNull(reportBacker.toggleReportSubscription());
        verify(reportService).isSubscribed(eq(user), any());
        verify(reportService).unsubscribeFromReport(eq(user), any());
        verify(reportService, never()).subscribeToReport(any(), any());
    }

    @Test
    public void testToggleReportSubscriptionWhenUserIsNotSubscribed() {
        doReturn(user).when(session).getUser();
        assertNull(reportBacker.toggleReportSubscription());
        assertTrue(reportBacker.isSubscribed());
        verify(reportService).isSubscribed(eq(user), any());
        verify(reportService, never()).unsubscribeFromReport(any(), any());
        verify(reportService).subscribeToReport(eq(user), any());
    }

    @Test
    public void testUpvoteWhenUserIsNull() throws Exception {
        reportBacker.setReport(report);
        doReturn(report).when(reportService).getReportByID(anyInt());
        assertNull(reportBacker.upvote());
        verify(reportService, never()).upvote(any(), any());
    }

    @Test
    public void testUpvote() throws Exception {
        reportBacker.setReport(report);
        doReturn(report).when(reportService).getReportByID(anyInt());
        doReturn(user).when(session).getUser();
        doReturn(true).when(reportService).hasUpvoted(report, user);
        assertNull(reportBacker.upvote());
        assertTrue(reportBacker.isUpvoted());
        assertFalse(reportBacker.isDownvoted());
        verify(reportService).upvote(report, user);
    }

    @Test
    public void testUpvoteWhenReportNotFound() {
        reportBacker.setReport(report);
        doReturn(user).when(session).getUser();
        assertThrows(Error404Exception.class, () -> reportBacker.upvote());
    }

    @Test
    public void testUpvoteWhenRelevanceOverwritten() throws Exception {
        report.setRelevanceOverwritten(true);
        report.setRelevance(42);
        reportBacker.setReport(report);
        doReturn(report).when(reportService).getReportByID(anyInt());
        doReturn(user).when(session).getUser();
        assertNull(reportBacker.upvote());
        assertEquals(42, reportBacker.getOverwriteRelevanceValue());
    }

    @Test
    public void testDownvote() throws Exception {
        reportBacker.setReport(report);
        doReturn(report).when(reportService).getReportByID(anyInt());
        doReturn(user).when(session).getUser();
        doReturn(true).when(reportService).hasDownvoted(report, user);
        assertNull(reportBacker.downvote());
        assertTrue(reportBacker.isDownvoted());
        assertFalse(reportBacker.isUpvoted());
        verify(reportService).downvote(report, user);
    }

    @Test
    public void testDownvoteWhenUserIsNull() throws Exception {
        reportBacker.setReport(report);
        doReturn(report).when(reportService).getReportByID(anyInt());
        assertNull(reportBacker.downvote());
        verify(reportService, never()).downvote(any(), any());
    }

    @Test
    public void testRemoveVote() throws Exception {
        reportBacker.setReport(report);
        doReturn(report).when(reportService).getReportByID(anyInt());
        doReturn(user).when(session).getUser();
        assertNull(reportBacker.removeVote());
        verify(reportService).removeVote(eq(report), eq(user));
    }

    @Test
    public void testRemoveVoteWhenUserIsNull() throws Exception {
        reportBacker.setReport(report);
        doReturn(report).when(reportService).getReportByID(anyInt());
        assertNull(reportBacker.removeVote());
        verify(reportService, never()).removeVote(any(), any());
    }

    @Test
    public void testToggleOpenClosedWhenReportIsOpen() {
        report.setClosingDate(null);
        reportBacker.setReport(report);
        assertDoesNotThrow(() -> reportBacker.toggleOpenClosed());
        verify(reportService).close(report);
        verify(reportService, never()).open(any());
        assertNull(reportBacker.getCurrentDialog());
    }

    @Test
    public void testToggleOpenClosedWhenReportIsClosed() {
        report.setClosingDate(OffsetDateTime.now());
        reportBacker.setReport(report);
        assertDoesNotThrow(() -> reportBacker.toggleOpenClosed());
        verify(reportService).open(report);
        verify(reportService, never()).close(any());
        assertNull(reportBacker.getCurrentDialog());
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        doReturn(true).when(reportService).deleteReport(report);
        reportBacker.setReport(report);
        assertDoesNotThrow(() -> reportBacker.delete());
        verify(reportService).deleteReport(report);
        verify(ectx).redirect(any());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteNoSuccess() {
        reportBacker.setReport(report);
        assertThrows(Error404Exception.class, () -> reportBacker.delete());
    }

    @Test
    public void testDeleteDataAccessFailed() throws Exception {
        reportBacker.setReport(report);
        doThrow(DataAccessException.class).when(reportService).deleteReport(report);
        assertDoesNotThrow(() -> reportBacker.delete());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteRedirectFailed() throws Exception {
        doReturn(true).when(reportService).deleteReport(report);
        doThrow(IOException.class).when(ectx).redirect(any());
        reportBacker.setReport(report);
        assertThrows(Error404Exception.class, () -> reportBacker.delete());
    }

    @Test
    public void testMarkDuplicate() throws Exception {
        report.setDuplicateOf(42);
        reportBacker.setReport(report);
        Field f = ReportBacker.class.getDeclaredField("privileged");
        f.setAccessible(true);
        f.set(reportBacker, true);
        f = ReportBacker.class.getDeclaredField("duplicates");
        f.setAccessible(true);
        f.set(reportBacker, mock(Paginator.class));
        doReturn(true).when(reportService).markDuplicate(report, 42);
        assertDoesNotThrow(() -> reportBacker.markDuplicate());
        verify(reportService).markDuplicate(report, 42);
        verify(reportService).close(report);
        assertNull(reportBacker.getCurrentDialog());
    }

    @Test
    public void testMarkDuplicateVerifyUpdate() throws Exception {
        report.setDuplicateOf(42);
        doReturn(false).when(requestParameterMap).containsKey("p");
        doReturn(true).when(requestParameterMap).containsKey("id");
        doReturn("100").when(requestParameterMap).get("id");
        doReturn(report).when(reportService).getReportByID(anyInt());
        Topic topic = new Topic();
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        doReturn(configuration).when(settings).getConfiguration();
        user.setAdministrator(true);
        doReturn(user).when(session).getUser();
        doReturn(true).when(reportService).markDuplicate(report, 42);
        doReturn(12).when(reportService).getNumberOfDuplicates(report);
        assertDoesNotThrow(() -> reportBacker.init());
        assertDoesNotThrow(() -> reportBacker.markDuplicate());
        verify(reportService, times(2)).getDuplicatesFor(eq(report), any());
    }

    @Test
    public void testMarkDuplicateWhenUserIsNotPrivileged() {
        assertDoesNotThrow(() -> reportBacker.markDuplicate());
        verify(reportService, never()).close(any());
        verify(reportService, never()).markDuplicate(any(), anyInt());
    }

    @Test
    public void testMarkDuplicateWhenMarkingFails() throws Exception {
        report.setDuplicateOf(42);
        reportBacker.setReport(report);
        Field f = ReportBacker.class.getDeclaredField("privileged");
        f.setAccessible(true);
        f.set(reportBacker, true);
        assertDoesNotThrow(() -> reportBacker.markDuplicate());
        verify(reportService).markDuplicate(report, 42);
        verify(reportService, never()).close(any());
    }

    @Test
    public void testUnmarkDuplicateWhenUserIsNotPrivileged() {
        assertDoesNotThrow(() -> reportBacker.unmarkDuplicate());
        verify(reportService, never()).unmarkDuplicate(any());
    }

    @Test
    public void testUnmarkDuplicate() throws Exception {
        reportBacker.setReport(report);
        Field f = ReportBacker.class.getDeclaredField("privileged");
        f.setAccessible(true);
        f.set(reportBacker, true);
        assertDoesNotThrow(() -> reportBacker.unmarkDuplicate());
        verify(reportService).unmarkDuplicate(report);
    }

    @Test
    public void testApplyOverwriteRelevanceWhenUserIsAnon() {
        assertThrows(Error404Exception.class, () -> reportBacker.applyOverwriteRelevance());
    }

    @Test
    public void testApplyOverwriteRelevanceWhenUserIsNoAdmin() {
        doReturn(user).when(session).getUser();
        assertThrows(Error404Exception.class, () -> reportBacker.applyOverwriteRelevance());
    }

    @Test
    public void testApplyOverwriteRelevance() throws Exception {
        user.setAdministrator(true);
        doReturn(user).when(session).getUser();
        reportBacker.setReport(report);
        reportBacker.setOverwriteRelevanceValue(42);
        doReturn(report).when(reportService).getReportByID(anyInt());
        assertNull(reportBacker.applyOverwriteRelevance());
        verify(reportService).overwriteRelevance(report, 42);
    }

    @Test
    public void testDeletePostReportStillThere() throws Exception {
        Post post = new Post(42, "a", 100, null, null);
        reportBacker.setReport(report);
        reportBacker.setPostToBeDeleted(post);
        reportBacker.setPosts(mock(Paginator.class));
        doReturn(report).when(reportService).getReportByID(report.getId());
        assertDoesNotThrow(() -> reportBacker.deletePost());
        verify(postService).deletePost(post, report);
        verify(feedbackEvent).fire(any());
        assertNull(reportBacker.getCurrentDialog());
    }

    @Test
    public void testDeletePostWhenReportDeletedAsWell() throws Exception {
        Post post = new Post(42, "a", 100, null, null);
        reportBacker.setReport(report);
        reportBacker.setPostToBeDeleted(post);
        assertDoesNotThrow(() -> reportBacker.deletePost());
        verify(ectx).redirect(any());
    }

    @Test
    public void testDeletePostWhenReportDeletedAndRedirectFails() throws Exception {
        Post post = new Post(42, "a", 100, null, null);
        reportBacker.setReport(report);
        reportBacker.setPostToBeDeleted(post);
        doThrow(IOException.class).when(ectx).redirect(any());
        assertThrows(Error404Exception.class, () -> reportBacker.deletePost());
    }

    @Test
    public void testIsAllowedToPostWhenUserIsAnon() {
        assertFalse(reportBacker.isAllowedToPost());
    }

    @Test
    public void testIsAllowedToPostWhenUserIsBanned() throws Exception {
        doReturn(user).when(session).getUser();
        Field f = ReportBacker.class.getDeclaredField("banned");
        f.setAccessible(true);
        f.set(reportBacker, true);
        assertFalse(reportBacker.isAllowedToPost());
    }

    @Test
    public void testIsAllowedToPostWhenReportIsOpen() {
        doReturn(user).when(session).getUser();
        report.setClosingDate(null);
        reportBacker.setReport(report);
        assertTrue(reportBacker.isAllowedToPost());
    }

    @Test
    public void testIsAllowedToPostWhenClosedReportPostingEnabled() {
        doReturn(user).when(session).getUser();
        report.setClosingDate(OffsetDateTime.now());
        reportBacker.setReport(report);
        doReturn(configuration).when(settings).getConfiguration();
        doReturn(true).when(configuration).isClosedReportPosting();
        assertTrue(reportBacker.isAllowedToPost());
    }

    @Test
    public void testIsAllowedToPostWhenClosedReportPostingDisabled() {
        doReturn(user).when(session).getUser();
        report.setClosingDate(OffsetDateTime.now());
        reportBacker.setReport(report);
        doReturn(configuration).when(settings).getConfiguration();
        assertFalse(reportBacker.isAllowedToPost());
    }

    @Test
    public void testPrivilegedForPostWhenUserIsAnon() {
        Post post = new Post(42, "a", 100, null, null);
        assertFalse(reportBacker.privilegedForPost(post));
    }

    @Test
    public void testPrivilegedForPostWhenClosedReportPostingDisabled() {
        Post post = new Post(42, "a", 100, null, null);
        doReturn(user).when(session).getUser();
        report.setClosingDate(OffsetDateTime.now());
        reportBacker.setReport(report);
        doReturn(configuration).when(settings).getConfiguration();
        assertFalse(reportBacker.privilegedForPost(post));
    }

    @Test
    public void testPrivilegedForPostWhenUserIsAdmin() {
        Post post = new Post(42, "a", 100, null, null);
        user.setAdministrator(true);
        doReturn(user).when(session).getUser();
        report.setClosingDate(null);
        reportBacker.setReport(report);
        assertTrue(reportBacker.privilegedForPost(post));
    }

    @Test
    public void testPrivilegedForPostWhenUserIsMod() throws Exception {
        Post post = new Post(42, "a", 100, null, null);
        doReturn(user).when(session).getUser();
        report.setClosingDate(null);
        reportBacker.setReport(report);
        Field f = ReportBacker.class.getDeclaredField("moderator");
        f.setAccessible(true);
        f.set(reportBacker, true);
        assertTrue(reportBacker.privilegedForPost(post));
    }

    @Test
    public void testPrivilegedForPostWhenUserIsAuthor() {
        Post post = new Post(42, "a", 100, null, null);
        Authorship authorship = new Authorship(user, null, null, null);
        post.setAuthorship(authorship);
        doReturn(user).when(session).getUser();
        report.setClosingDate(null);
        reportBacker.setReport(report);
        assertTrue(reportBacker.privilegedForPost(post));
    }

    @Test
    public void testPrivilegedForPostWhenNope() {
        Post post = new Post(42, "a", 100, null, null);
        Authorship authorship = new Authorship(null, null, null, null);
        post.setAuthorship(authorship);
        doReturn(user).when(session).getUser();
        report.setClosingDate(null);
        reportBacker.setReport(report);
        assertFalse(reportBacker.privilegedForPost(post));
    }

    @Test
    public void testInitWhenPostIDInvalid() {
        doReturn(true).when(requestParameterMap).containsKey("p");
        doReturn("invalid").when(requestParameterMap).get("p");
        assertThrows(Error404Exception.class, () -> reportBacker.init());
    }

    @Test
    public void testInitWhenNoIDsPresent() {
        assertThrows(Error404Exception.class, () -> reportBacker.init());
    }

    @Test
    public void testInitWhenReportIDInvalid() {
        // explicitly needed here
        doReturn(false).when(requestParameterMap).containsKey("p");
        doReturn(true).when(requestParameterMap).containsKey("id");
        doReturn("invalid").when(requestParameterMap).get("id");
        assertThrows(Error404Exception.class, () -> reportBacker.init());
    }

    @Test
    public void testInitWhenReportNotFound() {
        doReturn(false).when(requestParameterMap).containsKey("p");
        doReturn(true).when(requestParameterMap).containsKey("id");
        doReturn("100").when(requestParameterMap).get("id");
        assertThrows(Error404Exception.class, () -> reportBacker.init());
    }

    @Test
    public void testInitWhenTopicNotFound() throws Exception {
        doReturn(false).when(requestParameterMap).containsKey("p");
        doReturn(true).when(requestParameterMap).containsKey("id");
        doReturn("100").when(requestParameterMap).get("id");
        doReturn(report).when(reportService).getReportByID(anyInt());
        assertThrows(InternalError.class, () -> reportBacker.init());
    }

    @Test
    public void testInit() throws Exception {
        doReturn(false).when(requestParameterMap).containsKey("p");
        doReturn(true).when(requestParameterMap).containsKey("id");
        doReturn("100").when(requestParameterMap).get("id");
        doReturn(report).when(reportService).getReportByID(anyInt());
        Topic topic = new Topic();
        topic.setId(12);
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        doReturn(configuration).when(settings).getConfiguration();
        assertDoesNotThrow(() -> reportBacker.init());
        assertEquals(topic, reportBacker.getTopic());
        assertEquals(report, reportBacker.getReport());
    }

    @Test
    public void testInitWhenPostIDGiven() throws Exception {
        doReturn(true).when(requestParameterMap).containsKey("p");
        doReturn("42").when(requestParameterMap).get("p");
        doReturn(100).when(reportService).findReportOfPost(anyInt());
        doReturn(report).when(reportService).getReportByID(anyInt());
        Topic topic = new Topic();
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        doReturn(configuration).when(settings).getConfiguration();
        ArrayList<Post> posts = new ArrayList<>();
        Post post = new Post(42, "a", 100, null, null);
        posts.add(post);
        doReturn(posts).when(reportService).getPostsFor(eq(report), any());
        doReturn(1).when(reportService).getNumberOfPosts(report);
        assertDoesNotThrow(() -> reportBacker.init());
    }

    @Test
    public void testInitWhenPostIDGivenNotOnFirstPage() throws Exception {
        doReturn(true).when(requestParameterMap).containsKey("p");
        doReturn("42").when(requestParameterMap).get("p");
        doReturn(100).when(reportService).findReportOfPost(anyInt());
        doReturn(report).when(reportService).getReportByID(anyInt());
        Topic topic = new Topic();
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        doReturn(configuration).when(settings).getConfiguration();
        ArrayList<Post> firstPage = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            firstPage.add(new Post(i+1, "a", 100, null, null));
        }
        ArrayList<Post> posts = new ArrayList<>();
        Post post = new Post(42, "a", 100, null, null);
        posts.add(post);
        doReturn(firstPage, posts).when(reportService).getPostsFor(eq(report), any());
        doReturn(21).when(reportService).getNumberOfPosts(report);
        assertDoesNotThrow(() -> reportBacker.init());
        assertEquals(posts, reportBacker.getPosts().getWrappedData());
    }

    @Test
    public void testInitWhenPostIDGivenButNotFound() throws Exception {
        doReturn(true).when(requestParameterMap).containsKey("p");
        doReturn("42").when(requestParameterMap).get("p");
        doReturn(100).when(reportService).findReportOfPost(anyInt());
        doReturn(report).when(reportService).getReportByID(anyInt());
        Topic topic = new Topic();
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        doReturn(configuration).when(settings).getConfiguration();
        ArrayList<Post> posts = new ArrayList<>();
        Post post = new Post(5, "a", 100, null, null);
        posts.add(post);
        doReturn(posts).when(reportService).getPostsFor(eq(report), any());
        doReturn(1).when(reportService).getNumberOfPosts(report);
        assertThrows(Error404Exception.class, () -> reportBacker.init());
    }

    @Test
    public void testInitWhenUserIsBannedAndGuestReadingDisabled() throws Exception {
        doReturn(false).when(requestParameterMap).containsKey("p");
        doReturn(true).when(requestParameterMap).containsKey("id");
        doReturn("100").when(requestParameterMap).get("id");
        doReturn(report).when(reportService).getReportByID(anyInt());
        Topic topic = new Topic();
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        doReturn(configuration).when(settings).getConfiguration();
        doReturn(user).when(session).getUser();
        doReturn(true).when(topicService).isBanned(user, topic);
        assertThrows(Error404Exception.class, () -> reportBacker.init());
    }

    @Test
    public void testInitWhenUserIsMod() throws Exception {
        doReturn(false).when(requestParameterMap).containsKey("p");
        doReturn(true).when(requestParameterMap).containsKey("id");
        doReturn("100").when(requestParameterMap).get("id");
        doReturn(report).when(reportService).getReportByID(anyInt());
        Topic topic = new Topic();
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        doReturn(configuration).when(settings).getConfiguration();
        doReturn(user).when(session).getUser();
        doReturn(true).when(topicService).isModerator(user, topic);
        assertDoesNotThrow(() -> reportBacker.init());
        assertNull(reportBacker.getCurrentDialog());
        assertFalse(reportBacker.isBanned());
        Field f = ReportBacker.class.getDeclaredField("moderator");
        f.setAccessible(true);
        assertTrue((boolean) f.get(reportBacker));
        assertTrue(reportBacker.isPrivileged());
    }

    @Test
    public void testInitWhenUserIsAuthor() throws Exception {
        Authorship authorship = new Authorship(user, null, null, null);
        report.setAuthorship(authorship);
        doReturn(false).when(requestParameterMap).containsKey("p");
        doReturn(true).when(requestParameterMap).containsKey("id");
        doReturn("100").when(requestParameterMap).get("id");
        doReturn(report).when(reportService).getReportByID(anyInt());
        Topic topic = new Topic();
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        doReturn(configuration).when(settings).getConfiguration();
        doReturn(user).when(session).getUser();
        assertDoesNotThrow(() -> reportBacker.init());
        assertTrue(reportBacker.isPrivileged());
    }

    @Test
    public void testInitWhenReportHasDuplicates() throws Exception {
        doReturn(false).when(requestParameterMap).containsKey("p");
        doReturn(true).when(requestParameterMap).containsKey("id");
        doReturn("100").when(requestParameterMap).get("id");
        doReturn(report).when(reportService).getReportByID(anyInt());
        Topic topic = new Topic();
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        doReturn(configuration).when(settings).getConfiguration();
        ArrayList<Report> duplicates = new ArrayList<>();
        Report report1 = new Report(report);
        report1.setId(420);
        duplicates.add(report1);
        doReturn(duplicates).when(reportService).getDuplicatesFor(eq(report), any());
        doReturn(1).when(reportService).getNumberOfDuplicates(report);
        assertDoesNotThrow(() -> reportBacker.init());
        reportBacker.getDuplicates().update();
        assertEquals(duplicates, reportBacker.getDuplicates().getWrappedData());
    }

    @Test
    public void testInitWhenUserIsSubscribed() throws Exception {
        doReturn(false).when(requestParameterMap).containsKey("p");
        doReturn(true).when(requestParameterMap).containsKey("id");
        doReturn("100").when(requestParameterMap).get("id");
        doReturn(report).when(reportService).getReportByID(anyInt());
        Topic topic = new Topic();
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        doReturn(configuration).when(settings).getConfiguration();
        doReturn(user).when(session).getUser();
        doReturn(true).when(reportService).isSubscribed(user, report);
        assertDoesNotThrow(() -> reportBacker.init());
        assertTrue(reportBacker.isSubscribed());
    }

    @Test
    public void testInitWhenUserIsAdmin() throws Exception {
        doReturn(false).when(requestParameterMap).containsKey("p");
        doReturn(true).when(requestParameterMap).containsKey("id");
        doReturn("100").when(requestParameterMap).get("id");
        doReturn(report).when(reportService).getReportByID(anyInt());
        Topic topic = new Topic();
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        doReturn(configuration).when(settings).getConfiguration();
        user.setAdministrator(true);
        doReturn(user).when(session).getUser();
        assertDoesNotThrow(() -> reportBacker.init());
        assertTrue(reportBacker.isPrivileged());
        assertFalse(reportBacker.isSubscribed());
        verify(reportService).hasUpvoted(report, user);
    }
}
