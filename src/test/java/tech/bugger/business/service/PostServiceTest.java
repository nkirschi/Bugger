package tech.bugger.business.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.PostDBGateway;
import tech.bugger.persistence.gateway.PostGateway;
import tech.bugger.persistence.gateway.ReportGateway;
import tech.bugger.persistence.gateway.TopicGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    private PostService postService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private PostGateway postGateway;

    @Mock
    private ReportGateway reportGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

    private User testUser = new User();
    private Report testReport = new Report(100, "Hi", Report.Type.BUG, Report.Severity.MINOR, "1", null, null, null, null, null);
    private Post testPost = new Post(300, "Hi", new Lazy<>(testReport), null, null);

    @BeforeEach
    public void setUp() {
        postService = new PostService(notificationService, applicationSettings, transactionManager, feedbackEvent,
                ResourceBundleMocker.mock(""));
        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(postGateway).when(tx).newPostGateway();
        lenient().doReturn(reportGateway).when(tx).newReportGateway();
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void deletePostWhenPostIsNull() {
        assertThrows(IllegalArgumentException.class, () -> postService.deletePost(null));
    }

    @Test
    public void deletePostWhenPostIsFirstPost() throws Exception {
        doReturn(testPost).when(postGateway).getFirstPost(any());
        assertDoesNotThrow(() -> postService.deletePost(testPost));
        verify(reportGateway).deleteReport(any());
    }

    @Test
    public void deletePostWhenPostIsNotFirstPost() throws Exception {
        doReturn(null).when(postGateway).getFirstPost(any());
        assertDoesNotThrow(() -> postService.deletePost(testPost));
        verify(postGateway).deletePost(any());
    }

    @Test
    public void deletePostWhenCommitFails() throws Exception{
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> postService.deletePost(testPost));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void deletePostWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(postGateway).deletePost(any());
        assertDoesNotThrow(() -> postService.deletePost(testPost));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsPrivilegedWhenUserIsAnon() {
        assertFalse(postService.isPrivileged(null, testPost));
    }

    @Test
    public void testIsPrivilegedWhenUserIsAdmin() {
        testUser.setAdministrator(true);
        assertTrue(postService.isPrivileged(testUser, testPost));
    }

    @Test
    public void testIsPrivilegedWhenUserIsAuthor() {
        testUser.setAdministrator(false);
        Authorship authorship = new Authorship(testUser, null, null, null);
        testPost.setAuthorship(authorship);
        assertTrue(postService.isPrivileged(testUser, testPost));
    }

    @Test
    public void testIsPrivilegedWhenUserIsNotAuthor() {
        testUser.setAdministrator(false);
        testPost.setAuthorship(new Authorship(null, null, null, null));
        assertFalse(postService.isPrivileged(testUser, testPost));
    }
}