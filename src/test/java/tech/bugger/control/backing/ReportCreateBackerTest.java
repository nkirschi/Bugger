package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.PostService;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Registry;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class ReportCreateBackerTest {

    private ReportCreateBacker reportCreateBacker;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private TopicService topicService;

    @Mock
    private ReportService reportService;

    @Mock
    private PostService postService;

    @Mock
    private UserSession session;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Event<Feedback> feedbackEvent;

    @Mock
    private Registry registry;

    @Mock
    private Part uploadedAttachment;

    private Report testReport;

    private Post testFirstPost;

    private InputStream inputStream;

    private Configuration configuration;

    @BeforeEach
    public void setUp() throws Exception {
        doReturn(ResourceBundleMocker.mock("")).when(registry).getBundle(anyString(), any());
        reportCreateBacker = new ReportCreateBacker(applicationSettings, topicService, reportService, postService,
                session, ectx, feedbackEvent, registry);

        List<Attachment> attachments = Arrays.asList(new Attachment(), new Attachment(), new Attachment());
        testFirstPost = new Post(100, "Some content", new Lazy<>(mock(Report.class)), mock(Authorship.class), attachments);
        testReport = new Report(100, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", mock(Authorship.class),
                mock(ZonedDateTime.class), null, null, 1);
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
        reportCreateBacker.setTopicID(1);
        reportCreateBacker.init();
        assertEquals(1, reportCreateBacker.getReport().getTopic());
    }

    @Test
    public void testInitWhenNoUser() throws Exception {
        reportCreateBacker.setTopicID(1);
        reportCreateBacker.init();
        verify(ectx).redirect(any());
        assertTrue(reportCreateBacker.isBanned());
    }

    @Test
    public void testInitWhenNoTopic() throws Exception {
        reportCreateBacker.setTopicID(1);
        reportCreateBacker.init();
        verify(ectx).redirect(any());
        assertTrue(reportCreateBacker.isBanned());
    }

    @Test
    public void testInitWhenBanned() throws Exception {
        reportCreateBacker.setTopicID(1);
        reportCreateBacker.init();
        verify(ectx).redirect(any());
        assertTrue(reportCreateBacker.isBanned());
    }

    @Test
    public void testCreateWhenFine() {
        doReturn(true).when(reportService).createReport(any(), any());
        assertTrue(reportCreateBacker.create().endsWith("report.xhtml?id=" + testReport.getId()));
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
        doReturn(true).when(postService).isAttachmentListValid(any());

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
    public void testSaveAttachmentWhenAttachmentUnreadable() throws Exception {
        doThrow(IOException.class).when(inputStream).readAllBytes();
        List<Attachment> attachments = new ArrayList<>();
        reportCreateBacker.setAttachments(attachments);
        reportCreateBacker.saveAttachment();
        assertEquals(attachments, reportCreateBacker.getAttachments());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSaveAttachmentWhenListInvalid() throws Exception {
        Attachment attachment = new Attachment(0, "test.txt", new Lazy<>(new byte[]{1, 2, 3, 4}),
                "text/plain", new Lazy<>(testFirstPost));
        doReturn(attachment.getContent().get()).when(inputStream).readAllBytes();
        doReturn(attachment.getName()).when(uploadedAttachment).getSubmittedFileName();
        doReturn(attachment.getMimetype()).when(uploadedAttachment).getContentType();
        doReturn(false).when(postService).isAttachmentListValid(any());

        reportCreateBacker.setAttachments(new ArrayList<>());
        reportCreateBacker.saveAttachment();
        assertFalse(reportCreateBacker.getAttachments().contains(attachment));
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