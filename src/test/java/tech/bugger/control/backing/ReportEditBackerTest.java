package tech.bugger.control.backing;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.List;
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
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Registry;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportEditBackerTest {

    private ReportEditBacker reportEditBacker;

    @Mock
    private ApplicationSettings applicationSettings;

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

    @Mock
    private Configuration configuration;

    private Topic testTopic;
    private Topic testTopic2;
    private Report testReport;
    private User user;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().doReturn(ResourceBundleMocker.mock("")).when(registry).getBundle(anyString(), any());
        reportEditBacker = new ReportEditBacker(applicationSettings, topicService, reportService, session,
                registry, fctx, ectx);

        testReport = new Report(100, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "",
                new Authorship(null, null, null, null), mock(OffsetDateTime.class),
                null, null, false, 1, "Some title");
        reportEditBacker.setReport(testReport);
        reportEditBacker.setReportID(testReport.getId());
        testTopic = new Topic(1, "Some title", "Some description");
        testTopic2 = new Topic(2, "Some title2", "Some description 2");
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test",
                "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        reportEditBacker.setCurrentTopic(testTopic);
        lenient().doReturn(testTopic).when(topicService).getTopicByID(1);

        lenient().doReturn(ectx).when(fctx).getExternalContext();
        lenient().doReturn(requestParameterMap).when(ectx).getRequestParameterMap();
        lenient().doReturn(configuration).when(applicationSettings).getConfiguration();
    }

    private void setTopicsInBacker(List<Topic> topics) throws Exception {
        Field field = ReportEditBacker.class.getDeclaredField("topics");
        field.setAccessible(true);
        field.set(reportEditBacker, topics);
    }

    @Test
    public void testInit() throws Exception {
        doReturn("1234").when(requestParameterMap).get("id");
        doReturn(testReport).when(reportService).getReportByID(1234);
        doReturn(user).when(session).getUser();
        testReport.getAuthorship().setCreator(user);
        testReport.setClosingDate(null);
        doReturn(false).when(topicService).isBanned(any(), any());
        reportEditBacker.init();
        assertEquals(testReport, reportEditBacker.getReport());
        assertEquals(testTopic, reportEditBacker.getCurrentTopic());
        assertEquals(testTopic.getTitle(), reportEditBacker.getDestination());
        assertEquals(user, reportEditBacker.getReport().getAuthorship().getModifier());
    }

    @Test
    public void testInitWhenNoParam() {
        doReturn(null).when(requestParameterMap).get("id");
        assertThrows(Error404Exception.class, () -> reportEditBacker.init());
    }

    @Test
    public void testInitWhenNoReport() throws Exception {
        doReturn("1234").when(requestParameterMap).get("id");
        doReturn(null).when(reportService).getReportByID(1234);
        assertThrows(Error404Exception.class, () -> reportEditBacker.init());
    }

    @Test
    public void testInitWhenNotPrivileged() throws Exception {
        doReturn("1234").when(requestParameterMap).get("id");
        doReturn(testReport).when(reportService).getReportByID(1234);
        doReturn(user).when(session).getUser();
        testReport.getAuthorship().setCreator(mock(User.class));
        assertThrows(Error404Exception.class, () -> reportEditBacker.init());
    }

    @Test
    public void testInitWhenReportClosed() throws Exception {
        doReturn("1234").when(requestParameterMap).get("id");
        doReturn(testReport).when(reportService).getReportByID(1234);
        doReturn(user).when(session).getUser();
        testReport.getAuthorship().setCreator(user);
        doReturn(false).when(configuration).isClosedReportPosting();
        testReport.setClosingDate(mock(OffsetDateTime.class));
        assertThrows(Error404Exception.class, () -> reportEditBacker.init());
    }

    @Test
    public void testSaveChangesWithConfirm() {
        reportEditBacker.setDestination(testReport.getTopic());
        doReturn(true).when(reportService).updateReport(any());
        reportEditBacker.saveChangesWithConfirm();
        assertFalse(reportEditBacker.isDisplayConfirmDialog());
    }

    @Test
    public void testSaveChangesWithConfirmWhenCanMove() throws Exception {
        setTopicsInBacker(List.of(testTopic, testTopic2));
        reportEditBacker.setDestination("Some title2");
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(false).when(topicService).isBanned(any(), any());
        reportEditBacker.saveChangesWithConfirm();
        assertTrue(reportEditBacker.isDisplayConfirmDialog());
    }

    @Test
    public void testSaveChangesWithConfirmWhenCannotMove() throws Exception {
        setTopicsInBacker(List.of(testTopic, testTopic2));
        reportEditBacker.setDestination("Does not exist for some reason");
        doReturn(mock(User.class)).when(session).getUser();
        assertFalse(reportEditBacker.isDisplayConfirmDialog());
        reportEditBacker.saveChangesWithConfirm();
        verify(fctx).addMessage(any(), any());
    }

    @Test
    public void testSaveChangesWithConfirmWhenBanned() throws Exception {
        setTopicsInBacker(List.of(testTopic, testTopic2));
        reportEditBacker.setDestination("Some title2");
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(true).when(topicService).isBanned(any(), any());
        reportEditBacker.saveChangesWithConfirm();
        assertFalse(reportEditBacker.isDisplayConfirmDialog());
    }

    @Test
    public void testSaveChanges() throws Exception {
        doReturn(true).when(reportService).updateReport(any());
        reportEditBacker.setDestination(reportEditBacker.getReport().getTopic());
        reportEditBacker.saveChanges();
        verify(ectx).redirect(any());
    }

    @Test
    public void testSaveChangesMove() throws Exception {
        setTopicsInBacker(List.of(testTopic, testTopic2));
        doReturn(true).when(reportService).move(any());
        reportEditBacker.setDestination(reportEditBacker.getReport().getTopic() + "2");
        reportEditBacker.saveChanges();
        verify(ectx).redirect(any());
        verify(reportService, never()).updateReport(any());
    }

    @Test
    public void testSaveChangesWhenError() throws Exception {
        doReturn(false).when(reportService).updateReport(any());
        reportEditBacker.setDestination(reportEditBacker.getReport().getTopic());
        reportEditBacker.saveChanges();
        verify(ectx, times(0)).redirect(any());
    }

    @Test
    public void testSaveChangesWhenMoveError() throws Exception {
        setTopicsInBacker(List.of(testTopic, testTopic2));
        doReturn(false).when(reportService).move(any());
        reportEditBacker.setDestination(reportEditBacker.getReport().getTopic() + "2");
        reportEditBacker.saveChanges();
        verify(ectx, times(0)).redirect(any());
    }

    @Test
    public void testIsDisplayNoModerationWarningWhenNotChanged() {
        reportEditBacker.setCurrentTopic(testTopic);
        reportEditBacker.setDestination(testTopic.getTitle());
        assertFalse(reportEditBacker.isDisplayNoModerationWarning());
    }

    @Test
    public void testIsDisplayNoModerationWarningWhenNotLoggedIn() throws Exception {
        setTopicsInBacker(List.of(testTopic, testTopic2));
        doReturn(null).when(session).getUser();
        assertFalse(reportEditBacker.isDisplayNoModerationWarning());
    }

    @Test
    public void testIsDisplayNoModerationWarningWhenInvalidDestination() throws Exception {
        setTopicsInBacker(List.of(testTopic, testTopic2));
        reportEditBacker.setDestination("Some title2");
        doReturn(mock(User.class)).when(session).getUser();
        assertFalse(reportEditBacker.isDisplayNoModerationWarning());
    }

    @Test
    public void testIsDisplayNoModerationWarningWhenCurrentTopicNotMod() throws Exception {
        setTopicsInBacker(List.of(testTopic, testTopic2));
        reportEditBacker.setDestination("Some title2");
        doReturn(false).when(topicService).isModerator(any(), eq(testTopic));
        doReturn(mock(User.class)).when(session).getUser();
        assertFalse(reportEditBacker.isDisplayNoModerationWarning());
    }

    @Test
    public void testIsDisplayNoModerationWarningWhenNewTopicMod() throws Exception {
        setTopicsInBacker(List.of(testTopic, testTopic2));
        reportEditBacker.setDestination("Some title2");
        doReturn(true).when(topicService).isModerator(any(), eq(testTopic));
        doReturn(true).when(topicService).isModerator(any(), eq(testTopic2));
        doReturn(mock(User.class)).when(session).getUser();
        assertFalse(reportEditBacker.isDisplayNoModerationWarning());
    }

    @Test
    public void testIsDisplayNoModerationWarningWhenTrue() throws Exception {
        setTopicsInBacker(List.of(testTopic, testTopic2));
        reportEditBacker.setDestination("Some title2");
        doReturn(true).when(topicService).isModerator(any(), eq(testTopic));
        doReturn(false).when(topicService).isModerator(any(), eq(testTopic2));
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
        Authorship authorship = new Authorship(user, OffsetDateTime.now(), null, null);
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
        Authorship authorship = new Authorship(new User(user), OffsetDateTime.now(), null, null);
        user.setId(5);
        testReport.setAuthorship(authorship);
        reportEditBacker.setReport(testReport);
        reportEditBacker.setCurrentTopic(testTopic);
        when(session.getUser()).thenReturn(user);
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