package tech.bugger.control.backing;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.PostService;
import tech.bugger.business.service.ReportService;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;

import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    private FacesContext fctx;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Map<String, String> requestParameterMap;

    @Mock
    private Application app;

    @Mock
    private NavigationHandler navigationHandler;

    private Post post;

    private Report report;

    private User user;

    @BeforeEach
    public void setUp() throws Exception {
        postEditBacker = new PostEditBacker(applicationSettings, reportService, postService, session, fctx);

        List<Attachment> attachments = Arrays.asList(new Attachment(), new Attachment(), new Attachment());
        report = new Report(1234, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "",
                new Authorship(null, null, null, null), mock(ZonedDateTime.class),
                null, null, false, 1);
        post = new Post(5678, "Some content", new Lazy<>(report), new Authorship(null, null, null, null), attachments);
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);

        lenient().doReturn(ectx).when(fctx).getExternalContext();
        lenient().doReturn(requestParameterMap).when(ectx).getRequestParameterMap();
        lenient().doReturn(app).when(fctx).getApplication();
        lenient().doReturn(navigationHandler).when(app).getNavigationHandler();
    }

    private void verify404Redirect() {
        verify(navigationHandler).handleNavigation(any(), any(), eq("pretty:error"));
    }

    @Test
    public void testInitNoUser()  {
        doReturn(null).when(session).getUser();
        postEditBacker.init();
        verify404Redirect();
    }

    @Test
    public void testInitCreate() {
        doReturn("1234").when(requestParameterMap).get("r");
        doReturn(true).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(report).when(reportService).getReportByID(1234);
        doReturn(true).when(reportService).canPostInReport(user, report);
        postEditBacker.init();
        assertTrue(postEditBacker.isCreate());
        assertEquals(report, postEditBacker.getPost().getReport().get());
        assertEquals(user, postEditBacker.getPost().getAuthorship().getCreator());
    }

    @Test
    public void testInitCreateNoParam() {
        doReturn(null).when(requestParameterMap).get("r");
        doReturn(true).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        postEditBacker.init();
        verify404Redirect();
    }

    @Test
    public void testInitCreateReportNull() {
        doReturn("1234").when(requestParameterMap).get("r");
        doReturn(true).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(null).when(reportService).getReportByID(1234);
        postEditBacker.init();
        verify404Redirect();
    }

    @Test
    public void testInitCreateBanned() {
        doReturn("1234").when(requestParameterMap).get("r");
        doReturn(true).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(report).when(reportService).getReportByID(1234);
        doReturn(false).when(reportService).canPostInReport(user, report);
        postEditBacker.init();
        verify404Redirect();
    }

    @Test
    public void testInitEdit() {
        doReturn("5678").when(requestParameterMap).get("p");
        doReturn(false).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(post).when(postService).getPostByID(5678);
        doReturn(true).when(postService).isPrivileged(user, post);
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
        postEditBacker.init();
        verify404Redirect();
    }

    @Test
    public void testInitEditPostNull() {
        doReturn("5678").when(requestParameterMap).get("p");
        doReturn(false).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(null).when(postService).getPostByID(5678);
        postEditBacker.init();
        verify404Redirect();
    }

    @Test
    public void testInitEditBanned() {
        doReturn("5678").when(requestParameterMap).get("p");
        doReturn(false).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(post).when(postService).getPostByID(5678);
        doReturn(false).when(postService).isPrivileged(user, post);
        postEditBacker.init();
        verify404Redirect();
    }

    @Test
    public void testInitEditReportNull() {
        doReturn("5678").when(requestParameterMap).get("p");
        doReturn(false).when(requestParameterMap).containsKey("c");
        doReturn(user).when(session).getUser();
        doReturn(post).when(postService).getPostByID(5678);
        doReturn(true).when(postService).isPrivileged(user, post);
        post.setReport(null);
        postEditBacker.init();
        verify404Redirect();
    }

    @Test
    public void testSaveChangesCreate() throws Exception {
        postEditBacker.setPost(post);
        postEditBacker.setReport(report);
        postEditBacker.setCreate(true);
        doReturn(true).when(postService).createPost(post);
        postEditBacker.saveChanges();
        verify(postService).createPost(post);
        verify(ectx).redirect(anyString());
    }

    @Test
    public void testSaveChangesEdit() throws Exception {
        postEditBacker.setPost(post);
        postEditBacker.setReport(report);
        postEditBacker.setCreate(false);
        doReturn(true).when(postService).updatePost(post);
        postEditBacker.saveChanges();
        verify(postService).updatePost(post);
        verify(ectx).redirect(anyString());
    }

    @Test
    public void testSaveChangesNoSuccess() {
        postEditBacker.setPost(post);
        postEditBacker.setReport(report);
        postEditBacker.setCreate(false);
        doReturn(false).when(postService).updatePost(post);
        postEditBacker.saveChanges();
        verify(fctx, times(0)).getApplication();
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
        postEditBacker.setAttachments(new LinkedList<>(Arrays.asList(new Attachment(), new Attachment())));
        postEditBacker.deleteAllAttachments();
        assertEquals(0, postEditBacker.getAttachments().size());
    }

}
