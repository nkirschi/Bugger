package tech.bugger.business.service;

import org.checkerframework.checker.units.qual.A;
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
import tech.bugger.global.transfer.Topic;
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
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @Mock
    private NotificationService notificationService;

    @Mock
    private TopicService topicService;

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

    private User testUser = new User();
    private Report testReport = new Report(100, "Hi", Report.Type.BUG, Report.Severity.MINOR, "1", null, null, null, null, false, 123);
    private Post testPost = new Post(300, "Hi", new Lazy<>(testReport), new Authorship(null, null, null, null), null);


    @BeforeEach
    public void setUp() {
        service = new PostService(notificationService, topicService, applicationSettings, transactionManager,
                feedbackEvent, ResourceBundleMocker.mock(""));
        List<Attachment> attachments = Arrays.asList(
                new Attachment(1, "test1.txt", new Lazy<>(new byte[0]), "", new Lazy<>(testPost)),
                new Attachment(2, "test2.txt", new Lazy<>(new byte[0]), "", new Lazy<>(testPost)),
                new Attachment(3, "test3.txt", new Lazy<>(new byte[0]), "", new Lazy<>(testPost))
        );
        testPost.setAttachments(new ArrayList<>(attachments));

        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(postGateway).when(tx).newPostGateway();
        lenient().doReturn(reportGateway).when(tx).newReportGateway();
        lenient().doReturn(attachmentGateway).when(tx).newAttachmentGateway();
        configuration = new Configuration(false, false, "", ".txt,.mp3", 5, "");
        lenient().doReturn(configuration).when(applicationSettings).getConfiguration();
    }

    @Test
    public void testUpdatePost() throws Exception {
        List<Attachment> oldAttachments = new ArrayList<>(testPost.getAttachments());
        Attachment attachmentToAdd = new Attachment(4, "a-unique-filename.txt", new Lazy<>(new byte[0]), "",
                new Lazy<>(mock(Post.class)));
        Attachment attachmentToDelete = testPost.getAttachments().get(1);
        testPost.getAttachments().add(attachmentToAdd);
        testPost.getAttachments().remove(attachmentToDelete);
        doReturn(oldAttachments).when(attachmentGateway).getAttachmentsForPost(testPost);
        assertTrue(service.updatePost(testPost));
        verify(attachmentGateway).delete(attachmentToDelete);
        verify(attachmentGateway).create(attachmentToAdd);
    }

    @Test
    public void testUpdatePostWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(postGateway).update(testPost);
        assertFalse(service.updatePost(testPost));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpdatePostWhenCommitFailed() throws Exception {
        doReturn(testPost.getAttachments()).when(attachmentGateway).getAttachmentsForPost(testPost);
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.updatePost(testPost));
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
    public void testCreatePostWithTransactionWhenFine() throws Exception {
        PostService serviceSpy = spy(service);
        lenient().doReturn(true).when(serviceSpy).isAttachmentListValid(any());
        assertTrue(serviceSpy.createPostWithTransaction(testPost, tx));
        verify(postGateway).create(any());
        verify(attachmentGateway, times(3)).create(any());
    }

    @Test
    public void testCreatePostWithTransactionWhenInvalid() throws Exception {
        PostService serviceSpy = spy(service);
        lenient().doReturn(false).when(serviceSpy).isAttachmentListValid(any());
        assertFalse(serviceSpy.createPostWithTransaction(testPost, tx));
        verify(postGateway, times(0)).create(any());
    }

    @Test
    public void testCreatePostWhenFine() throws Exception {
        PostService serviceSpy = spy(service);
        lenient().doReturn(true).when(serviceSpy).createPostWithTransaction(any(), any());
        assertTrue(serviceSpy.createPost(testPost));
        verify(tx).commit();
    }

    @Test
    public void testCreatePostWhenNoSuccess() throws Exception {
        PostService serviceSpy = spy(service);
        lenient().doReturn(false).when(serviceSpy).createPostWithTransaction(any(), any());
        assertFalse(serviceSpy.createPost(testPost));
    }

    @Test
    public void testCreatePostWhenCommitFails() throws Exception {
        PostService serviceSpy = spy(service);
        lenient().doReturn(true).when(serviceSpy).createPostWithTransaction(any(), any());
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(serviceSpy.createPost(testPost));
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
        assertArrayEquals(new byte[]{1, 2, 3, 4}, attachment.getContent().get());
        assertEquals(testPost, attachment.getPost().get());
    }

    @Test
    public void deletePostWhenPostIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.deletePost(null));
    }

    @Test
    public void deletePostWhenPostIsFirstPost() throws Exception {
        doReturn(testPost).when(postGateway).getFirstPost(any());
        assertDoesNotThrow(() -> service.deletePost(testPost));
        verify(reportGateway).delete(any());
    }

    @Test
    public void deletePostWhenPostIsNotFirstPost() throws Exception {
        doReturn(null).when(postGateway).getFirstPost(any());
        assertDoesNotThrow(() -> service.deletePost(testPost));
        verify(postGateway).delete(any());
    }

    @Test
    public void deletePostWhenCommitFails() throws Exception{
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> service.deletePost(testPost));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void deletePostWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(postGateway).delete(any());
        assertDoesNotThrow(() -> service.deletePost(testPost));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetAttachmentByID() throws Exception {
        Attachment attachment = new Attachment(1234, "test1.txt", new Lazy<>(new byte[0]), "", new Lazy<>(testPost));
        doReturn(attachment).when(attachmentGateway).find(1234);
        assertEquals(attachment, service.getAttachmentByID(1234));
    }

    @Test
    public void testGetAttachmentByIDWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(attachmentGateway).find(1234);
        assertDoesNotThrow(() -> service.getAttachmentByID(1234));
        verify(feedbackEvent, times(0)).fire(any());
    }

    @Test
    public void testGetAttachmentByIDWhenCommitFailed() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> service.getAttachmentByID(1234));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsPrivilegedWhenUserIsAnon() {
        assertFalse(service.isPrivileged(null, testPost));
    }

    @Test
    public void testIsPrivilegedWhenUserIsAdmin() {
        testUser.setAdministrator(true);
        assertTrue(service.isPrivileged(testUser, testPost));
    }

    @Test
    public void testIsPrivilegedWhenReportNull() {
        testUser.setAdministrator(false);
        testPost.setReport(null);
        assertFalse(service.isPrivileged(testUser, testPost));
    }

    @Test
    public void testIsPrivilegedWhenTopicIDNull() {
        testUser.setAdministrator(false);
        testReport.setTopicID(null);
        assertFalse(service.isPrivileged(testUser, testPost));
    }

    @Test
    public void testIsPrivilegedWhenTopicNull() {
        testUser.setAdministrator(false);
        doReturn(null).when(topicService).getTopicByID(testReport.getTopicID());
        assertFalse(service.isPrivileged(testUser, testPost));
    }

    @Test
    public void testIsPrivilegedWhenUserIsModerator() {
        testUser.setAdministrator(false);
        Topic topic = new Topic(testReport.getTopicID(), "testtopic", "testdescription");
        doReturn(topic).when(topicService).getTopicByID(testReport.getTopicID());
        doReturn(true).when(topicService).isModerator(testUser, topic);
        assertTrue(service.isPrivileged(testUser, testPost));
    }

    @Test
    public void testIsPrivilegedWhenUserIsAuthor() {
        testUser.setAdministrator(false);
        Topic topic = new Topic(testReport.getTopicID(), "testtopic", "testdescription");
        doReturn(topic).when(topicService).getTopicByID(testReport.getTopicID());
        doReturn(false).when(topicService).isModerator(testUser, topic);
        doReturn(false).when(topicService).isBanned(testUser, topic);
        testPost.setAuthorship(new Authorship(testUser, null, testUser, null));
        assertTrue(service.isPrivileged(testUser, testPost));
    }

    @Test
    public void testIsPrivilegedWhenUserIsBanned() {
        testUser.setAdministrator(false);
        Topic topic = new Topic(testReport.getTopicID(), "testtopic", "testdescription");
        doReturn(topic).when(topicService).getTopicByID(testReport.getTopicID());
        doReturn(false).when(topicService).isModerator(testUser, topic);
        doReturn(true).when(topicService).isBanned(testUser, topic);
        testPost.setAuthorship(new Authorship(testUser, null, testUser, null));
        assertFalse(service.isPrivileged(testUser, testPost));
    }

}
