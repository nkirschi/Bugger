package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.PostService;
import tech.bugger.business.service.ReportService;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.User;

import javax.faces.context.ExternalContext;
import javax.servlet.http.Part;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class PostEditBackerTest {

    private PostEditBacker postEditBacker;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private ReportService reportService;

    @Mock
    private PostService postService;

    @Mock
    private UserSession session;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Map<String, String> requestParameterMap;

    @Mock
    private Configuration configuration;

    private Post post;

    private Report report;

    private User user;

    @BeforeEach
    public void setUp() throws Exception {
        postEditBacker = new PostEditBacker(applicationSettings, reportService, postService, session, ectx);

        List<Attachment> attachments = List.of(new Attachment(), new Attachment(), new Attachment());
        report = new Report(1234, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "",
                new Authorship(null, null, null, null), mock(OffsetDateTime.class),
                null, null, false, 1, null);
        post = new Post(5678, "Some content", report.getId(), new Authorship(null, null, null, null), attachments);
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);

        lenient().doReturn(requestParameterMap).when(ectx).getRequestParameterMap();
        lenient().doReturn(configuration).when(applicationSettings).getConfiguration();
    }

    @Test
    public void testInitCreate() {
        doReturn("1234").when(requestParameterMap).get("r");
        doReturn(true).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(report).when(reportService).getReportByID(1234);
        doReturn(true).when(reportService).canPostInReport(user, report);
        doReturn(true).when(configuration).isClosedReportPosting();
        postEditBacker.init();
        assertTrue(postEditBacker.isCreate());
        assertEquals(report.getId(), postEditBacker.getPost().getReport());
        assertEquals(user, postEditBacker.getPost().getAuthorship().getCreator());
    }

    @Test
    public void testInitCreateNoParam() {
        doReturn(null).when(requestParameterMap).get("r");
        doReturn(true).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        assertThrows(Error404Exception.class, () -> postEditBacker.init());
    }

    @Test
    public void testInitCreateReportNull() {
        doReturn("1234").when(requestParameterMap).get("r");
        doReturn(true).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(null).when(reportService).getReportByID(1234);
        assertThrows(Error404Exception.class, () -> postEditBacker.init());
    }

    @Test
    public void testInitCreateBanned() {
        doReturn("1234").when(requestParameterMap).get("r");
        doReturn(true).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(report).when(reportService).getReportByID(1234);
        doReturn(false).when(reportService).canPostInReport(user, report);
        assertThrows(Error404Exception.class, () -> postEditBacker.init());
    }

    @Test
    public void testInitEdit() {
        doReturn("5678").when(requestParameterMap).get("p");
        doReturn(false).when(requestParameterMap).containsKey("c");
        doReturn(report).when(reportService).getReportByID(1234);
        doReturn(user).when(session).getUser();
        doReturn(post).when(postService).getPostByID(5678);
        doReturn(true).when(postService).isPrivileged(user, post, report);
        doReturn(true).when(configuration).isClosedReportPosting();
        postEditBacker.init();
        assertFalse(postEditBacker.isCreate());
        assertEquals(post.getAttachments(), postEditBacker.getAttachments());
        assertEquals(user, postEditBacker.getPost().getAuthorship().getModifier());
    }

    @Test
    public void testInitEditNoParam() {
        doReturn(null).when(requestParameterMap).get("p");
        doReturn(false).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        assertThrows(Error404Exception.class, () -> postEditBacker.init());
    }

    @Test
    public void testInitEditPostNull() {
        doReturn("5678").when(requestParameterMap).get("p");
        doReturn(false).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(null).when(postService).getPostByID(5678);
        assertThrows(Error404Exception.class, () -> postEditBacker.init());
    }

    @Test
    public void testInitEditBanned() {
        doReturn("5678").when(requestParameterMap).get("p");
        doReturn(false).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(post).when(postService).getPostByID(5678);
        lenient().doReturn(false).when(postService).isPrivileged(user, post, report);
        assertThrows(Error404Exception.class, () -> postEditBacker.init());
    }

    @Test
    public void testInitEditReportNull() {
        doReturn("5678").when(requestParameterMap).get("p");
        doReturn(false).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(post).when(postService).getPostByID(5678);
        post.setReport(4321);
        doReturn(null).when(reportService).getReportByID(4321);
        assertThrows(Error404Exception.class, () -> postEditBacker.init());
    }

    @Test
    public void testInitEditNotPrivileged() {
        doReturn("5678").when(requestParameterMap).get("p");
        doReturn(false).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(post).when(postService).getPostByID(5678);
        post.setReport(4321);
        doReturn(report).when(reportService).getReportByID(4321);
        doReturn(false).when(postService).isPrivileged(user, post, report);
        assertThrows(Error404Exception.class, () -> postEditBacker.init());
    }

    @Test
    public void testInitReportClosed() {
        doReturn("1234").when(requestParameterMap).get("r");
        doReturn(true).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(report).when(reportService).getReportByID(1234);
        doReturn(true).when(reportService).canPostInReport(user, report);
        doReturn(false).when(configuration).isClosedReportPosting();
        report.setClosingDate(mock(OffsetDateTime.class));
        assertThrows(Error404Exception.class, () -> postEditBacker.init());
    }

    @Test
    public void testSaveChangesCreate() throws Exception {
        postEditBacker.setPost(post);
        postEditBacker.setReport(report);
        postEditBacker.setCreate(true);
        doReturn(true).when(postService).createPost(post, report);
        postEditBacker.saveChanges();
        verify(postService).createPost(post, report);
        verify(ectx).redirect(anyString());
    }

    @Test
    public void testSaveChangesEdit() throws Exception {
        postEditBacker.setPost(post);
        postEditBacker.setReport(report);
        postEditBacker.setCreate(false);
        doReturn(true).when(postService).updatePost(post, report);
        postEditBacker.saveChanges();
        verify(postService).updatePost(post, report);
        verify(ectx).redirect(anyString());
    }

    @Test
    public void testSaveChangesNoSuccess() throws Exception {
        postEditBacker.setPost(post);
        postEditBacker.setReport(report);
        postEditBacker.setCreate(false);
        doReturn(false).when(postService).updatePost(post, report);
        postEditBacker.saveChanges();
        verify(ectx, times(0)).redirect(any());
    }

    @Test
    public void testSaveChangesRedirectFails() throws Exception {
        postEditBacker.setPost(post);
        postEditBacker.setReport(report);
        postEditBacker.setCreate(false);
        doReturn(true).when(postService).updatePost(post, report);
        doThrow(IOException.class).when(ectx).redirect(anyString());
        assertThrows(InternalError.class, () -> postEditBacker.saveChanges());
        verify(postService).updatePost(post, report);
    }

    @Test
    public void testUploadAttachments() {
        Part uploadedAttachment = mock(Part.class);
        postEditBacker.setLastAttachmentUploaded(uploadedAttachment);
        postEditBacker.setPost(post);
        postEditBacker.uploadAttachment();
        verify(postService).addAttachment(post, uploadedAttachment);
    }

    @Test
    public void testUploadAttachmentsWhenUploadNull() {
        Part uploadedAttachment = mock(Part.class);
        postEditBacker.setLastAttachmentUploaded(null);
        postEditBacker.setPost(post);
        postEditBacker.uploadAttachment();
        verify(postService, times(0)).addAttachment(post, uploadedAttachment);
    }

    @Test
    public void testDeleteAllAttachments() {
        postEditBacker.setAttachments(new LinkedList<>(List.of(new Attachment(), new Attachment())));
        postEditBacker.deleteAllAttachments();
        assertEquals(0, postEditBacker.getAttachments().size());
    }

}
