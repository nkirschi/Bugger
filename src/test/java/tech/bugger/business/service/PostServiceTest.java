package tech.bugger.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.AttachmentGateway;
import tech.bugger.persistence.gateway.PostGateway;
import tech.bugger.persistence.gateway.ReportGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    private PostService service;

    private PostService postService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationSettings applicationSettings;

    private Configuration configuration;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private PostGateway postGateway;

    @Mock
    private AttachmentGateway attachmentGateway;

    @Mock
    private ReportGateway reportGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

    private Post testPost1;

    private User testUser = new User();
    private Report testReport = new Report(100, "Hi", Report.Type.BUG, Report.Severity.MINOR, "1", null, null, null, null, false, null);
    private Post testPost = new Post(300, "Hi", new Lazy<>(testReport), null, null);


    @BeforeEach
    public void setUp() {
        service = new PostService(notificationService, applicationSettings, transactionManager, feedbackEvent,
                ResourceBundleMocker.mock(""));
        postService = new PostService(notificationService, applicationSettings, transactionManager, feedbackEvent,
                ResourceBundleMocker.mock(""));
        List<Attachment> attachments = Arrays.asList(
                new Attachment(0, "test1.txt", null, "", null),
                new Attachment(0, "test2.txt", null, "", null),
                new Attachment(0, "test3.txt", null, "", null)
        );
        testPost1 = new Post(100, "Some content", new Lazy<>(mock(Report.class)), mock(Authorship.class), attachments);

        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(postGateway).when(tx).newPostGateway();
        lenient().doReturn(reportGateway).when(tx).newReportGateway();
        lenient().doReturn(attachmentGateway).when(tx).newAttachmentGateway();
        configuration = new Configuration(false, false, "", ".txt,.mp3", 5, "");
        lenient().doReturn(configuration).when(applicationSettings).getConfiguration();
    }

    @Test
    public void testIsAttachmentListValidWhenFine() {
        assertTrue(service.isAttachmentListValid(testPost1.getAttachments()));
    }

    @Test
    public void testIsAttachmentListValidWhenTooManyAttachments() {
        configuration.setMaxAttachmentsPerPost(2);
        assertFalse(service.isAttachmentListValid(testPost1.getAttachments()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsAttachmentListValidWhenNamesNotUnique() {
        testPost1.getAttachments().get(2).setName("test1.txt");
        assertFalse(service.isAttachmentListValid(testPost1.getAttachments()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsAttachmentListValidWhenNoExtension() {
        testPost1.getAttachments().get(2).setName("test");
        assertFalse(service.isAttachmentListValid(testPost1.getAttachments()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsAttachmentListValidWhenWrongExtension() {
        testPost1.getAttachments().get(2).setName("test.gif");
        assertFalse(service.isAttachmentListValid(testPost1.getAttachments()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testCreatePostWithTransactionWhenFine() throws Exception {
        PostService serviceSpy = spy(service);
        lenient().doReturn(true).when(serviceSpy).isAttachmentListValid(any());
        assertTrue(serviceSpy.createPostWithTransaction(testPost1, tx));
        verify(postGateway).create(any());
        verify(attachmentGateway, times(3)).create(any());
    }

    @Test
    public void testCreatePostWithTransactionWhenInvalid() throws Exception {
        PostService serviceSpy = spy(service);
        lenient().doReturn(false).when(serviceSpy).isAttachmentListValid(any());
        assertFalse(serviceSpy.createPostWithTransaction(testPost1, tx));
        verify(postGateway, times(0)).create(any());
    }

    @Test
    public void testCreatePostWhenFine() throws Exception {
        PostService serviceSpy = spy(service);
        lenient().doReturn(true).when(serviceSpy).createPostWithTransaction(any(), any());
        assertTrue(serviceSpy.createPost(testPost1));
        verify(tx).commit();
    }

    @Test
    public void testCreatePostWhenNoSuccess() throws Exception {
        PostService serviceSpy = spy(service);
        lenient().doReturn(false).when(serviceSpy).createPostWithTransaction(any(), any());
        assertFalse(serviceSpy.createPost(testPost1));
    }

    @Test
    public void testCreatePostWhenCommitFails() throws Exception {
        PostService serviceSpy = spy(service);
        lenient().doReturn(true).when(serviceSpy).createPostWithTransaction(any(), any());
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(serviceSpy.createPost(testPost1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void deletePostWhenPostIsNull() {
        assertThrows(IllegalArgumentException.class, () -> postService.deletePost(null));
    }

    @Test
    public void deletePostWhenPostIsFirstPost() throws Exception {
        doReturn(testPost).when(postGateway).getFirstPost(any());
        assertDoesNotThrow(() -> postService.deletePost(testPost));
        verify(reportGateway).delete(any());
    }

    @Test
    public void deletePostWhenPostIsNotFirstPost() throws Exception {
        doReturn(null).when(postGateway).getFirstPost(any());
        assertDoesNotThrow(() -> postService.deletePost(testPost));
        verify(postGateway).delete(any());
    }

    @Test
    public void deletePostWhenCommitFails() throws Exception{
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> postService.deletePost(testPost));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void deletePostWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(postGateway).delete(any());
        assertDoesNotThrow(() -> postService.deletePost(testPost));
        verify(feedbackEvent).fire(any());
    }
    public void testIsPrivilegedWhenUserIsAnon() {
        assertFalse(postService.canModify(null, testPost));
    }

    @Test
    public void testIsPrivilegedWhenUserIsAdmin() {
        testUser.setAdministrator(true);
        assertTrue(postService.canModify(testUser, testPost));
    }

    @Test
    public void testIsPrivilegedWhenUserIsAuthor() {
        testUser.setAdministrator(false);
        Authorship authorship = new Authorship(testUser, null, null, null);
        testPost.setAuthorship(authorship);
        assertTrue(postService.canModify(testUser, testPost));
    }

    @Test
    public void testIsPrivilegedWhenUserIsNotAuthor() {
        testUser.setAdministrator(false);
        testPost.setAuthorship(new Authorship(null, null, null, null));
        assertFalse(postService.canModify(testUser, testPost));
    }

}