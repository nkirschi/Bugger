package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.PostService;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReportCreateBackerTest {

    private ReportCreateBacker reportCreateBacker;

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
    private Part uploadedAttachment;

    @Mock
    private Map<String, String> requestParameterMap;

    private Report testReport;

    private Post testFirstPost;

    @BeforeEach
    public void setUp() throws Exception {
        reportCreateBacker = new ReportCreateBacker(topicService, reportService, postService, session, ectx);

        List<Attachment> attachments = List.of(new Attachment(), new Attachment(), new Attachment());
        testFirstPost = new Post(100, "Some content", 42, mock(Authorship.class), attachments);
        testReport = new Report(100, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "",
                                mock(Authorship.class),
                                mock(OffsetDateTime.class), null, null, false, 1);
        reportCreateBacker.setReport(testReport);
        reportCreateBacker.setFirstPost(testFirstPost);
        lenient().doReturn(requestParameterMap).when(ectx).getRequestParameterMap();

        uploadedAttachment = mock(Part.class);
        reportCreateBacker.setUploadedAttachment(uploadedAttachment);
    }

    @Test
    public void testInit() {
        doReturn("1234").when(requestParameterMap).get("id");
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(mock(Topic.class)).when(topicService).getTopicByID(anyInt());
        doReturn(true).when(topicService).canCreateReportIn(any(), any());
        reportCreateBacker.init();
        assertEquals(1234, reportCreateBacker.getReport().getTopicID());
        assertEquals(reportCreateBacker.getReport().getId(), reportCreateBacker.getFirstPost().getReport());
    }

    @Test
    public void testInitWhenNoParam() throws Exception {
        doReturn(null).when(requestParameterMap).get("id");
        assertThrows(Error404Exception.class, () -> reportCreateBacker.init());
        assertTrue(reportCreateBacker.isBanned());
    }

    @Test
    public void testInitWhenNoUser() throws Exception {
        doReturn("1").when(requestParameterMap).get("id");
        doReturn(null).when(session).getUser();
        assertThrows(Error404Exception.class, () -> reportCreateBacker.init());
        assertTrue(reportCreateBacker.isBanned());
    }

    @Test
    public void testInitWhenNoTopic() throws Exception {
        doReturn("1").when(requestParameterMap).get("id");
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(null).when(topicService).getTopicByID(anyInt());
        assertThrows(Error404Exception.class, () -> reportCreateBacker.init());
        assertTrue(reportCreateBacker.isBanned());
    }

    @Test
    public void testInitWhenNotAllowed() throws Exception {
        doReturn("1").when(requestParameterMap).get("id");
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(mock(Topic.class)).when(topicService).getTopicByID(anyInt());
        doReturn(false).when(topicService).canCreateReportIn(any(), any());
        assertThrows(Error404Exception.class, () -> reportCreateBacker.init());
        assertTrue(reportCreateBacker.isBanned());
    }

    @Test
    public void testCreateWhenFine() throws Exception {
        doReturn(true).when(reportService).createReport(any(), any());
        reportCreateBacker.create();
        verify(ectx).redirect(anyString());
    }

    @Test
    public void testCreateWhenCreationFails() throws Exception {
        doReturn(false).when(reportService).createReport(any(), any());
        reportCreateBacker.create();
        verify(ectx, times(0)).redirect(any());
    }

    @Test
    public void testSaveAttachments() {
        reportCreateBacker.setUploadedAttachment(uploadedAttachment);
        reportCreateBacker.saveAttachment();
        verify(postService).addAttachment(testFirstPost, uploadedAttachment);
    }

    @Test
    public void testSaveAttachmentsWhenUploadNull() {
        reportCreateBacker.setUploadedAttachment(null);
        reportCreateBacker.saveAttachment();
        verify(postService, times(0)).addAttachment(testFirstPost, uploadedAttachment);
    }

    @Test
    public void testDeleteAllAttachments() {
        reportCreateBacker.setAttachments(new LinkedList<>(List.of(new Attachment(), new Attachment())));
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
