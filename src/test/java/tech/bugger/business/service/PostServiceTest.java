package tech.bugger.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.event.Event;
import javax.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.AttachmentGateway;
import tech.bugger.persistence.gateway.PostGateway;
import tech.bugger.persistence.gateway.ReportGateway;
import tech.bugger.persistence.gateway.TopicGateway;
import tech.bugger.persistence.gateway.UserGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    private PostService service;

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
    private TopicGateway topicGateway;

    @Mock
    private UserGateway userGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

    private final User testUser = new User();
    private final Report testReport = new Report(100, "Hi", Report.Type.BUG, Report.Severity.MINOR, "1", null, null, null,
            null, false, 100);
    private final Post testPost = new Post(300, "Hi", testReport.getId(), null, null);
    private final Topic testTopic = new Topic(100, "Hi", "I am a topic");

    @BeforeEach
    public void setUp() {
        service = new PostService(notificationService, applicationSettings, transactionManager,
                feedbackEvent, ResourceBundleMocker.mock(""));
        List<Attachment> attachments = List.of(
                new Attachment(1, "test1.txt", new byte[0], "", testPost.getId()),
                new Attachment(2, "test2.txt", new byte[0], "", testPost.getId()),
                new Attachment(3, "test3.txt", new byte[0], "", testPost.getId())
        );
        testPost.setAttachments(new ArrayList<>(attachments));
        testUser.setId(1);
        Authorship authorship = new Authorship(testUser, OffsetDateTime.now(), testUser, OffsetDateTime.now());
        testPost.setAuthorship(authorship);
        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(postGateway).when(tx).newPostGateway();
        lenient().doReturn(reportGateway).when(tx).newReportGateway();
        lenient().doReturn(attachmentGateway).when(tx).newAttachmentGateway();
        lenient().doReturn(topicGateway).when(tx).newTopicGateway();
        lenient().doReturn(userGateway).when(tx).newUserGateway();
        configuration = new Configuration(false, false, "", ".txt,.mp3", 5, "");
        lenient().doReturn(configuration).when(applicationSettings).getConfiguration();
    }

    @Test
    public void testUpdatePost() throws Exception {
        List<Attachment> oldAttachments = new ArrayList<>(testPost.getAttachments());
        Attachment attachmentToAdd = new Attachment(4, "a-unique-filename.txt", new byte[0], "", 42);
        Attachment attachmentToDelete = testPost.getAttachments().get(1);
        testPost.getAttachments().add(attachmentToAdd);
        testPost.getAttachments().remove(attachmentToDelete);
        doReturn(oldAttachments).when(attachmentGateway).getAttachmentsForPost(testPost);
        assertTrue(service.updatePost(testPost, testReport));
        verify(attachmentGateway).delete(attachmentToDelete);
        verify(attachmentGateway).create(attachmentToAdd);
    }

    @Test
    public void testUpdatePostWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(postGateway).update(testPost);
        assertFalse(service.updatePost(testPost, testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpdatePostWhenCommitFailed() throws Exception {
        doReturn(testPost.getAttachments()).when(attachmentGateway).getAttachmentsForPost(testPost);
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.updatePost(testPost, testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsAttachmentListValidWhenFine() {
        assertTrue(service.isAttachmentListValid(testPost.getAttachments()));
    }

    @Test
    public void testIsAttachmentListValidWhenTooManyAttachments() {
        configuration.setMaxAttachmentsPerPost(2);
        assertFalse(service.isAttachmentListValid(testPost.getAttachments()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsAttachmentListValidWhenNamesNotUnique() {
        testPost.getAttachments().get(2).setName("test1.txt");
        assertFalse(service.isAttachmentListValid(testPost.getAttachments()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsAttachmentListValidWhenNoExtension() {
        testPost.getAttachments().get(2).setName("test");
        assertFalse(service.isAttachmentListValid(testPost.getAttachments()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsAttachmentListValidWhenWrongExtension() {
        testPost.getAttachments().get(2).setName("test.gif");
        assertFalse(service.isAttachmentListValid(testPost.getAttachments()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testCreatePostWithTransactionWhenFine() {
        PostService serviceSpy = spy(service);
        lenient().doReturn(true).when(serviceSpy).isAttachmentListValid(any());
        assertTrue(serviceSpy.createPostWithTransaction(testPost, tx));
        verify(postGateway).create(any());
        verify(attachmentGateway, times(3)).create(any());
    }

    @Test
    public void testCreatePostWithTransactionWhenInvalid() {
        PostService serviceSpy = spy(service);
        lenient().doReturn(false).when(serviceSpy).isAttachmentListValid(any());
        assertFalse(serviceSpy.createPostWithTransaction(testPost, tx));
        verify(postGateway, times(0)).create(any());
    }

    @Test
    public void testCreatePostWhenFine() throws Exception {
        PostService serviceSpy = spy(service);
        lenient().doReturn(true).when(serviceSpy).createPostWithTransaction(any(), any());
        assertTrue(serviceSpy.createPost(testPost, testReport));
        verify(tx).commit();
    }

    @Test
    public void testCreatePostWhenNoSuccess() {
        PostService serviceSpy = spy(service);
        lenient().doReturn(false).when(serviceSpy).createPostWithTransaction(any(), any());
        assertFalse(serviceSpy.createPost(testPost, testReport));
    }

    @Test
    public void testCreatePostWhenCommitFails() throws Exception {
        PostService serviceSpy = spy(service);
        lenient().doReturn(true).when(serviceSpy).createPostWithTransaction(any(), any());
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(serviceSpy.createPost(testPost, testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testAddAttachmentWhenPostIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.addAttachment(null, null));
    }

    @Test
    public void testAddAttachmentWhenPartIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.addAttachment(testPost, null));
    }

    @Test
    public void testAddAttachmentWhenPartIsUnreadable() throws Exception {
        List<Attachment> attachments = new ArrayList<>(testPost.getAttachments());
        Part part = mock(Part.class);
        doThrow(IOException.class).when(part).getInputStream();
        service.addAttachment(testPost, part);
        verify(feedbackEvent).fire(any());
        assertEquals(attachments, testPost.getAttachments());
    }

    @Test
    public void testAddAttachmentWhenNotAllowed() throws Exception {
        List<Attachment> attachments = new ArrayList<>(testPost.getAttachments());
        Part part = mock(Part.class);
        doReturn(testPost.getAttachments().get(1).getName()).when(part).getSubmittedFileName(); // Duplicate filename.
        InputStream is = mock(InputStream.class);
        doReturn(is).when(part).getInputStream();
        doReturn(new byte[]{1, 2, 3, 4}).when(is).readAllBytes();
        service.addAttachment(testPost, part);
        assertEquals(attachments, testPost.getAttachments());
    }

    @Test
    public void testAddAttachmentWhenAllowed() throws Exception {
        Part part = mock(Part.class);
        doReturn("a-unique-filename.txt").when(part).getSubmittedFileName();
        doReturn("text/plain").when(part).getContentType();
        InputStream is = mock(InputStream.class);
        doReturn(is).when(part).getInputStream();
        doReturn(new byte[]{1, 2, 3, 4}).when(is).readAllBytes();
        service.addAttachment(testPost, part);
        Attachment attachment = testPost.getAttachments().get(testPost.getAttachments().size() - 1);
        assertEquals("a-unique-filename.txt", attachment.getName());
        assertEquals("text/plain", attachment.getMimetype());
        assertArrayEquals(new byte[]{1, 2, 3, 4}, attachment.getContent());
        assertEquals(testPost.getId(), attachment.getPost());
    }

    @Test
    public void deletePostWhenPostIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.deletePost(null, testReport));
    }

    @Test
    public void deletePostWhenPostIsFirstPost() throws Exception {
        doReturn(testPost).when(postGateway).getFirstPost(any());
        assertDoesNotThrow(() -> service.deletePost(testPost, testReport));
        verify(reportGateway).delete(any());
    }

    @Test
    public void deletePostWhenPostIsNotFirstPost() throws Exception {
        doReturn(null).when(postGateway).getFirstPost(any());
        assertDoesNotThrow(() -> service.deletePost(testPost, testReport));
        verify(postGateway).delete(any());
    }

    @Test
    public void deletePostWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> service.deletePost(testPost, testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void deletePostWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(postGateway).delete(any());
        assertDoesNotThrow(() -> service.deletePost(testPost, testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetPostByID() throws Exception {
        testPost.setId(1234);
        doReturn(testPost).when(postGateway).find(1234);
        assertEquals(testPost, service.getPostByID(1234));
    }

    @Test
    public void testGetPostByIDWhenNotFound() throws Exception {
        testPost.setId(1234);
        doThrow(NotFoundException.class).when(postGateway).find(1234);
        assertDoesNotThrow(() -> service.getPostByID(1234));
        verify(feedbackEvent, never()).fire(any());
    }

    @Test
    public void testGetPostByIDWhenCommitFailed() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> service.getPostByID(1234));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetAttachmentByID() throws Exception {
        Attachment attachment = new Attachment(1234, "test1.txt", new byte[0], "", testPost.getId());
        doReturn(attachment).when(attachmentGateway).find(1234);
        assertEquals(attachment, service.getAttachmentByID(1234));
    }

    @Test
    public void testGetAttachmentByIDWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(attachmentGateway).find(1234);
        assertDoesNotThrow(() -> service.getAttachmentByID(1234));
        verify(feedbackEvent, never()).fire(any());
    }

    @Test
    public void testGetAttachmentByIDWhenCommitFailed() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> service.getAttachmentByID(1234));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetAttachmentContent() throws Exception {
        byte[] content = new byte[]{1, 2, 3, 4};
        doReturn(content).when(attachmentGateway).findContent(1234);
        assertEquals(content, service.getAttachmentContent(1234));
    }

    @Test
    public void testGetAttachmentContentWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(attachmentGateway).findContent(1234);
        assertDoesNotThrow(() -> service.getAttachmentContent(1234));
        verify(feedbackEvent, never()).fire(any());
    }

    @Test
    public void testGetAttachmentContentWhenCommitFailed() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> service.getAttachmentContent(1234));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsPrivilegedWhenUserIsAnon() {
        assertFalse(service.isPrivileged(null, testPost, testReport));
    }

    @Test
    public void testIsPrivilegedPostNull() {
        assertFalse(service.isPrivileged(testUser, null, testReport));
    }

    @Test
    public void testIsPrivilegedWhenUserIsAdmin() {
        testUser.setAdministrator(true);
        assertTrue(service.isPrivileged(testUser, testPost, testReport));
    }

    @Test
    public void testIsPrivilegedWhenUserIsBanned() throws NotFoundException {
        testUser.setAdministrator(false);
        doReturn(testTopic).when(topicGateway).findTopic(anyInt());
        doReturn(true).when(userGateway).isBanned(any(), any());
        assertFalse(service.isPrivileged(testUser, testPost, testReport));
    }

    @Test
    public void testIsPrivilegedWhenUserIsMod() throws NotFoundException {
        testUser.setAdministrator(false);
        doReturn(testTopic).when(topicGateway).findTopic(anyInt());
        doReturn(true).when(userGateway).isModerator(testUser, testTopic);
        assertTrue(service.isPrivileged(testUser, testPost, testReport));
    }

    @Test
    public void testIsPrivilegedWhenUserIsAuthor() throws NotFoundException {
        testUser.setAdministrator(false);
        Authorship authorship = new Authorship(testUser, null, null, null);
        testPost.setAuthorship(authorship);
        doReturn(testTopic).when(topicGateway).findTopic(anyInt());
        assertTrue(service.isPrivileged(testUser, testPost, testReport));
    }

    @Test
    public void testIsPrivilegedNotPrivileged() throws NotFoundException {
        doReturn(testTopic).when(topicGateway).findTopic(anyInt());
        Authorship authorship = new Authorship(null, null, null, null);
        testPost.setAuthorship(authorship);
        assertFalse(service.isPrivileged(testUser, testPost, testReport));
    }

    @Test
    public void testIsPrivilegedNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(topicGateway).findTopic(anyInt());
        assertFalse(service.isPrivileged(testUser, testPost, testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsPrivilegedTransactionException() throws TransactionException, NotFoundException {
        doReturn(testTopic).when(topicGateway).findTopic(anyInt());
        doThrow(TransactionException.class).when(tx).commit();
        Authorship authorship = new Authorship(null, null, null, null);
        testPost.setAuthorship(authorship);
        assertFalse(service.isPrivileged(testUser, testPost, testReport));
        verify(feedbackEvent).fire(any());
    }

}
