package tech.bugger.control.backing;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Registry;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class ReportEditBackerTest {

    private ReportEditBacker reportEditBacker;

    @Mock
    private TopicService topicService;

    @Mock
    private ReportService reportService;

    @Mock
    private UserSession session;

    @Mock
    private FacesContext fctx;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Map<String, String> requestParameterMap;

    @Mock
    private Registry registry;

    private Topic testTopic;
    private Report testReport;
    private User user;

    @BeforeEach
    public void setUp() throws Exception {
        doReturn(ResourceBundleMocker.mock("")).when(registry).getBundle(anyString(), any());
        reportEditBacker = new ReportEditBacker(topicService, reportService, session, fctx, registry);

        testReport = new Report(100, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "",
                new Authorship(null, null, null, null), mock(ZonedDateTime.class),
                null, null, false, 1);
        reportEditBacker.setReport(testReport);
        reportEditBacker.setReportID(testReport.getId());
        testTopic = new Topic(1, "Some title", "Some description");
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        reportEditBacker.setCurrentTopic(testTopic);
        lenient().doReturn(testTopic).when(topicService).getTopicByID(1);
        lenient().doReturn(ectx).when(fctx).getExternalContext();
        lenient().doReturn(requestParameterMap).when(ectx).getRequestParameterMap();
    }

    @Test
    public void testInit() {
        doReturn("1234").when(requestParameterMap).get("id");
        doReturn(testReport).when(reportService).getReportByID(1234);
        doReturn(user).when(session).getUser();
        testReport.getAuthorship().setCreator(user);
        doReturn(false).when(topicService).isBanned(any(), any());
        reportEditBacker.init();
        assertEquals(testReport, reportEditBacker.getReport());
        assertEquals(testTopic, reportEditBacker.getCurrentTopic());
        assertEquals(testTopic.getId(), reportEditBacker.getDestinationID());
        assertEquals(user, reportEditBacker.getReport().getAuthorship().getModifier());
    }

    @Test
    public void testInitWhenNoParam() throws Exception {
        doReturn(null).when(requestParameterMap).get("id");
        reportEditBacker.init();
        verify(ectx).redirect(any());
    }

    @Test
    public void testInitWhenNoReport() throws Exception {
        doReturn("1234").when(requestParameterMap).get("id");
        doReturn(null).when(reportService).getReportByID(1234);
        reportEditBacker.init();
        verify(ectx).redirect(any());
    }

    @Test
    public void testInitWhenNoUser() throws Exception {
        doReturn("1234").when(requestParameterMap).get("id");
        doReturn(testReport).when(reportService).getReportByID(1234);
        doReturn(user).when(session).getUser();
        reportEditBacker.init();
        verify(ectx).redirect(any());
    }

    @Test
    public void testInitWhenNotPrivileged() throws Exception {
        doReturn("1234").when(requestParameterMap).get("id");
        doReturn(testReport).when(reportService).getReportByID(1234);
        doReturn(user).when(session).getUser();
        testReport.getAuthorship().setCreator(mock(User.class));
        reportEditBacker.init();
        verify(ectx).redirect(any());
    }

    @Test
    public void testSaveChangesWithConfirm() {
        reportEditBacker.setDestinationID(testReport.getTopicID());
        doReturn(true).when(reportService).updateReport(any());
        reportEditBacker.saveChangesWithConfirm();
        assertFalse(reportEditBacker.isDisplayConfirmDialog());
    }

    @Test
    public void testSaveChangesWithConfirmWhenCanMove() {
        reportEditBacker.setDestinationID(42);
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(testTopic).when(topicService).getTopicByID(anyInt());
        doReturn(false).when(topicService).isBanned(any(), any());
        reportEditBacker.saveChangesWithConfirm();
        assertTrue(reportEditBacker.isDisplayConfirmDialog());
    }

    @Test
    public void testSaveChangesWithConfirmWhenCannotMove() {
        reportEditBacker.setDestinationID(42);
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(null).when(topicService).getTopicByID(42);
        assertFalse(reportEditBacker.isDisplayConfirmDialog());
        reportEditBacker.saveChangesWithConfirm();
        verify(fctx).addMessage(any(), any());
    }

    @Test
    public void testSaveChangesWithConfirmWhenNotLoggedIn() {
        reportEditBacker.setDestinationID(42);
        doReturn(testTopic).when(topicService).getTopicByID(anyInt());
        doReturn(null).when(session).getUser();
        assertFalse(reportEditBacker.isDisplayConfirmDialog());
        reportEditBacker.saveChangesWithConfirm();
        verify(fctx).addMessage(any(), any());
    }

    @Test
    public void testSaveChangesWithConfirmWhenBanned() {
        reportEditBacker.setDestinationID(42);
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(testTopic).when(topicService).getTopicByID(anyInt());
        doReturn(true).when(topicService).isBanned(any(), any());
        reportEditBacker.saveChangesWithConfirm();
        assertFalse(reportEditBacker.isDisplayConfirmDialog());
    }

    @Test
    public void testSaveChanges() throws Exception {
        doReturn(true).when(reportService).updateReport(any());
        reportEditBacker.setDestinationID(reportEditBacker.getReport().getTopicID());
        reportEditBacker.saveChanges();
        verify(ectx).redirect(any());
    }

    @Test
    public void testSaveChangesMove() throws Exception {
        doReturn(true).when(reportService).move(any());
        doReturn(true).when(reportService).updateReport(any());
        reportEditBacker.setDestinationID(reportEditBacker.getReport().getTopicID() + 1);
        reportEditBacker.saveChanges();
        verify(ectx).redirect(any());
    }

    @Test
    public void testSaveChangesWhenError() throws Exception {
        doReturn(false).when(reportService).updateReport(any());
        reportEditBacker.setDestinationID(reportEditBacker.getReport().getTopicID());
        reportEditBacker.saveChanges();
        verify(ectx, times(0)).redirect(any());
    }

    @Test
    public void testSaveChangesWhenMoveError() throws Exception {
        doReturn(false).when(reportService).move(any());
        reportEditBacker.setDestinationID(reportEditBacker.getReport().getTopicID() + 1);
        reportEditBacker.saveChanges();
        verify(ectx, times(0)).redirect(any());
    }

    @Test
    public void testIsDisplayNoModerationWarningWhenNotChanged() {
        reportEditBacker.setCurrentTopic(testTopic);
        reportEditBacker.setDestinationID(testTopic.getId());
        assertFalse(reportEditBacker.isDisplayNoModerationWarning());
    }

    @Test
    public void testIsDisplayNoModerationWarningWhenNotLoggedIn() {
        doReturn(null).when(session).getUser();
        assertFalse(reportEditBacker.isDisplayNoModerationWarning());
    }

    @Test
    public void testIsDisplayNoModerationWarningWhenInvalidDestination() {
        reportEditBacker.setDestinationID(42);
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(null).when(topicService).getTopicByID(42);
        assertFalse(reportEditBacker.isDisplayNoModerationWarning());
    }

    @Test
    public void testIsDisplayNoModerationWarningWhenCurrentTopicNotMod() {
        reportEditBacker.setDestinationID(42);
        Topic destination = mock(Topic.class);
        doReturn(destination).when(topicService).getTopicByID(42);
        doReturn(false).when(topicService).isModerator(any(), eq(testTopic));
        doReturn(mock(User.class)).when(session).getUser();
        assertFalse(reportEditBacker.isDisplayNoModerationWarning());
    }

    @Test
    public void testIsDisplayNoModerationWarningWhenNewTopicMod() {
        reportEditBacker.setDestinationID(42);
        Topic destination = mock(Topic.class);
        doReturn(destination).when(topicService).getTopicByID(42);
        doReturn(true).when(topicService).isModerator(any(), eq(testTopic));
        doReturn(true).when(topicService).isModerator(any(), eq(destination));
        doReturn(mock(User.class)).when(session).getUser();
        assertFalse(reportEditBacker.isDisplayNoModerationWarning());
    }

    @Test
    public void testIsDisplayNoModerationWarningWhenTrue() {
        reportEditBacker.setDestinationID(42);
        Topic destination = mock(Topic.class);
        doReturn(destination).when(topicService).getTopicByID(42);
        doReturn(true).when(topicService).isModerator(any(), eq(testTopic));
        doReturn(false).when(topicService).isModerator(any(), eq(destination));
        doReturn(mock(User.class)).when(session).getUser();
        assertTrue(reportEditBacker.isDisplayNoModerationWarning());
    }

    @Test
    public void testGetReportTypes() {
        assertArrayEquals(Report.Type.values(), reportEditBacker.getReportTypes());
    }

    @Test
    public void testGetReportSeverities() {
        assertArrayEquals(Report.Severity.values(), reportEditBacker.getReportSeverities());
    }

    @Test
    public void testIsPrivileged() {
        Authorship authorship = new Authorship(user, ZonedDateTime.now(), null, null);
        testReport.setAuthorship(authorship);
        reportEditBacker.setReport(testReport);
        reportEditBacker.setCurrentTopic(testTopic);
        when(session.getUser()).thenReturn(user);
        assertTrue(reportEditBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedUserModerator() {
        reportEditBacker.setReport(testReport);
        reportEditBacker.setCurrentTopic(testTopic);
        when(session.getUser()).thenReturn(user);
        when(topicService.isModerator(user, testTopic)).thenReturn(true);
        assertTrue(reportEditBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedUserAdmin() {
        reportEditBacker.setReport(testReport);
        reportEditBacker.setCurrentTopic(testTopic);
        user.setAdministrator(true);
        when(session.getUser()).thenReturn(user);
        assertTrue(reportEditBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedFalse() {
        Authorship authorship = new Authorship(new User(user), ZonedDateTime.now(), null, null);
        user.setId(5);
        testReport.setAuthorship(authorship);
        reportEditBacker.setReport(testReport);
        reportEditBacker.setCurrentTopic(testTopic);
        when(session.getUser()).thenReturn(user);
        assertFalse(reportEditBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedUserNull() {
        reportEditBacker.setCurrentTopic(testTopic);
        assertFalse(reportEditBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedTopicNull() {
        when(session.getUser()).thenReturn(user);
        assertFalse(reportEditBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedAdmin() {
        reportEditBacker.setCurrentTopic(testTopic);
        when(session.getUser()).thenReturn(user);
        user.setAdministrator(true);
        assertTrue(reportEditBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedModerator() {
        reportEditBacker.setCurrentTopic(testTopic);
        when(session.getUser()).thenReturn(user);
        user.setAdministrator(false);
        when(topicService.isModerator(user, testTopic)).thenReturn(true);
        assertTrue(reportEditBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedCreator() {
        reportEditBacker.setCurrentTopic(testTopic);
        when(session.getUser()).thenReturn(user);
        user.setAdministrator(false);
        when(topicService.isModerator(user, testTopic)).thenReturn(false);
        testReport.getAuthorship().setCreator(user);
        when(topicService.isBanned(user, testTopic)).thenReturn(false);
        assertTrue(reportEditBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedBanned() {
        reportEditBacker.setCurrentTopic(testTopic);
        when(session.getUser()).thenReturn(user);
        user.setAdministrator(false);
        when(topicService.isModerator(user, testTopic)).thenReturn(false);
        testReport.getAuthorship().setCreator(user);
        when(topicService.isBanned(user, testTopic)).thenReturn(true);
        assertFalse(reportEditBacker.isPrivileged());
    }

}