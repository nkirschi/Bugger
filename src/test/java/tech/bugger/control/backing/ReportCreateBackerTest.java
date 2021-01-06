package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;

import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReportCreateBackerTest {

    private ReportCreateBacker reportCreateBacker;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private ReportService reportService;

    @Mock
    private TopicService topicService;

    @Mock
    private UserSession session;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Event<Feedback> feedbackEvent;

    @Mock
    private Part uploadedAttachment;

    private Report testReport;

    private Post testFirstPost;

    private InputStream inputStream;

    private Configuration configuration;

    @BeforeEach
    public void setUp() throws Exception {
        reportCreateBacker = new ReportCreateBacker(applicationSettings, reportService, topicService, session, ectx,
                feedbackEvent/*, ResourceBundleMocker.mock("")*/);

        List<Attachment> attachments = Arrays.asList(new Attachment(), new Attachment(), new Attachment());
        testFirstPost = new Post(100, "Some content", new Lazy<>(mock(Report.class)), mock(Authorship.class), attachments);
        testReport = new Report(100, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", mock(Authorship.class),
                mock(ZonedDateTime.class), null, null, new Lazy<>(mock(Topic.class)));
        reportCreateBacker.setReport(testReport);
        reportCreateBacker.setFirstPost(testFirstPost);

        uploadedAttachment = mock(Part.class);
        inputStream = mock(InputStream.class);
        lenient().doReturn(inputStream).when(uploadedAttachment).getInputStream();
        reportCreateBacker.setUploadedAttachment(uploadedAttachment);

        configuration = mock(Configuration.class);
        lenient().doReturn(configuration).when(applicationSettings).getConfiguration();
        lenient().doReturn(5).when(configuration).getMaxAttachmentsPerPost();
    }

    @Test
    public void testInit() {
        User userMock = mock(User.class);
        doReturn(userMock).when(session).getUser();
        Topic topicMock = mock(Topic.class);
        doReturn(topicMock).when(topicService).getTopicByID(1);
        reportCreateBacker.setTopicID(1);
        reportCreateBacker.init();
        assertEquals(userMock, reportCreateBacker.getReport().getAuthorship().getCreator());
        assertEquals(topicMock, reportCreateBacker.getReport().getTopic().get());
    }

    @Test
    public void testInitWhenNoUser() throws Exception {
        doReturn(null).when(session).getUser();
        reportCreateBacker.setTopicID(1);
        reportCreateBacker.init();
        verify(ectx).redirect(any());
        assertTrue(reportCreateBacker.isBanned());
    }

    @Test
    public void testInitWhenNoTopic() throws Exception {
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(null).when(topicService).getTopicByID(anyInt());
        reportCreateBacker.setTopicID(1);
        reportCreateBacker.init();
        verify(ectx).redirect(any());
        assertTrue(reportCreateBacker.isBanned());
    }

    @Test
    public void testInitWhenBanned() throws Exception {
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(mock(Topic.class)).when(topicService).getTopicByID(anyInt());
        doReturn(true).when(topicService).isBanned(any(), any());
        reportCreateBacker.setTopicID(1);
        reportCreateBacker.init();
        verify(ectx).redirect(any());
        assertTrue(reportCreateBacker.isBanned());
    }

    @Test
    public void testCreateWhenFine() {
        doReturn(true).when(reportService).createReport(any(), any());
        assertTrue(reportCreateBacker.create().endsWith("report.xhtml?r=" + testReport.getId()));
    }

    @Test
    public void testCreateWhenCreationFails() {
        doReturn(false).when(reportService).createReport(any(), any());
        assertNull(reportCreateBacker.create());
    }

    @Test
    public void testSaveAttachmentWhenFine() throws Exception {
        Attachment attachment = new Attachment(0, "test.txt", new Lazy<>(new byte[]{1, 2, 3, 4}),
                "text/plain", new Lazy<>(testFirstPost));
        doReturn(attachment.getContent().get()).when(inputStream).readAllBytes();
        doReturn(attachment.getName()).when(uploadedAttachment).getSubmittedFileName();
        doReturn(attachment.getMimetype()).when(uploadedAttachment).getContentType();

        reportCreateBacker.setAttachments(new ArrayList<>());
        reportCreateBacker.saveAttachment();
        assertTrue(reportCreateBacker.getAttachments().contains(attachment));
    }

    @Test
    public void testSaveAttachmentWhenNoAttachment() {
        reportCreateBacker.setUploadedAttachment(null);
        List<Attachment> attachments = new ArrayList<>();
        reportCreateBacker.setAttachments(attachments);
        reportCreateBacker.saveAttachment();
        assertEquals(attachments, reportCreateBacker.getAttachments());
    }

    @Test
    public void testSaveAttachmentWhenNamesNotUnique() throws Exception {
        doReturn("test.txt").when(uploadedAttachment).getSubmittedFileName();
        List<Attachment> attachments = Arrays.asList(new Attachment(0, "test.txt", null, "", null));
        reportCreateBacker.setAttachments(new ArrayList<>(attachments));
        reportCreateBacker.saveAttachment();
        assertEquals(attachments, reportCreateBacker.getAttachments());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSaveAttachmentWhenTooManyAttachments() throws Exception {
        doReturn(2).when(configuration).getMaxAttachmentsPerPost();
        List<Attachment> attachments = Arrays.asList(
                new Attachment(0, "test1.txt", null, "", null),
                new Attachment(0, "test2.txt", null, "", null)
        );
        reportCreateBacker.setAttachments(new ArrayList<>(attachments));
        reportCreateBacker.saveAttachment();
        assertEquals(attachments, reportCreateBacker.getAttachments());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSaveAttachmentWhenAttachmentInvalid() throws Exception {
        doThrow(IOException.class).when(inputStream).readAllBytes();
        List<Attachment> attachments = new ArrayList<>();
        reportCreateBacker.setAttachments(attachments);
        reportCreateBacker.saveAttachment();
        assertEquals(attachments, reportCreateBacker.getAttachments());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteAllAttachments() {
        reportCreateBacker.setAttachments(new LinkedList<>(Arrays.asList(new Attachment(), new Attachment())));
        reportCreateBacker.deleteAllAttachments();
        assertEquals(0, reportCreateBacker.getAttachments().size());
    }

    @Test
    public void testGetReportTypes() {
        assertArrayEquals(Report.Type.values(), reportCreateBacker.getReportTypes());
    }

    @Test
    public void testGetReportSeverities() {
        assertArrayEquals(Report.Severity.values(), reportCreateBacker.getReportSeverities());
    }

}