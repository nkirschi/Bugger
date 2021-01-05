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
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.AttachmentGateway;
import tech.bugger.persistence.gateway.PostGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    private Event<Feedback> feedbackEvent;

    private Post testPost;

    @BeforeEach
    public void setUp() {
        service = new PostService(notificationService, applicationSettings, transactionManager, feedbackEvent,
                ResourceBundleMocker.mock(""));
        List<Attachment> attachments = Arrays.asList(new Attachment(), new Attachment(), new Attachment());
        testPost = new Post(100, "Some content", new Lazy<>(mock(Report.class)), mock(Authorship.class), attachments);

        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(postGateway).when(tx).newPostGateway();
        lenient().doReturn(attachmentGateway).when(tx).newAttachmentGateway();
        configuration = new Configuration(false, false, "", "", 5, "");
        doReturn(configuration).when(applicationSettings).getConfiguration();
    }

    @Test
    public void testCreatePostWhenFine() {
        assertTrue(service.createPost(testPost));
        verify(postGateway).create(any());
        verify(attachmentGateway, times(3)).create(any());
    }

    @Test
    public void testCreatePostWhenTooManyAttachments() throws Exception {
        configuration.setMaxAttachmentsPerPost(2);
        assertFalse(service.createPost(testPost));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testCreatePostWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.createPost(testPost));
        verify(feedbackEvent).fire(any());
    }

}