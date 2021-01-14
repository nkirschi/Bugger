package tech.bugger.control.backing;

import java.time.ZonedDateTime;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    private Registry registry;

    private Topic testTopic;

    private Report testReport;

    @BeforeEach
    public void setUp() throws Exception {
        doReturn(ResourceBundleMocker.mock("")).when(registry).getBundle(anyString(), any());
        reportEditBacker = new ReportEditBacker(topicService, reportService, session, fctx, registry);

        testReport = new Report(100, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", mock(Authorship.class),
                mock(ZonedDateTime.class), null, null, false, 1);
        reportEditBacker.setReport(testReport);
        reportEditBacker.setReportID(testReport.getId());
        testTopic = new Topic(1, "Some title", "Some description");
        reportEditBacker.setCurrentTopic(testTopic);
        lenient().doReturn(testTopic).when(topicService).getTopicByID(1);
        lenient().doReturn(mock(ExternalContext.class)).when(fctx).getExternalContext();
    }

    @Test
    public void testSaveChangesWithConfirm() {
        reportEditBacker.setDestinationID(testReport.getTopic());
        doReturn(true).when(reportService).updateReport(any());
        assertNotNull(reportEditBacker.saveChangesWithConfirm());
        assertFalse(reportEditBacker.isDisplayConfirmDialog());
    }

    @Test
    public void testSaveChangesWithConfirmWhenCanMove() {
        reportEditBacker.setDestinationID(42);
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(testTopic).when(topicService).getTopicByID(anyInt());
        doReturn(false).when(topicService).isBanned(any(), any());
        assertNull(reportEditBacker.saveChangesWithConfirm());
        assertTrue(reportEditBacker.isDisplayConfirmDialog());
    }

    @Test
    public void testSaveChangesWithConfirmWhenCannotMove() {
        reportEditBacker.setDestinationID(42);
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(null).when(topicService).getTopicByID(42);
        assertNull(reportEditBacker.saveChangesWithConfirm());
        assertFalse(reportEditBacker.isDisplayConfirmDialog());
        verify(fctx).addMessage(any(), any());
    }

    @Test
    public void testSaveChangesWithConfirmWhenNotLoggedIn() {
        reportEditBacker.setDestinationID(42);
        doReturn(testTopic).when(topicService).getTopicByID(anyInt());
        doReturn(null).when(session).getUser();
        assertNull(reportEditBacker.saveChangesWithConfirm());
        assertFalse(reportEditBacker.isDisplayConfirmDialog());
        verify(fctx).addMessage(any(), any());
    }

    @Test
    public void testSaveChangesWithConfirmWhenBanned() {
        reportEditBacker.setDestinationID(42);
        doReturn(mock(User.class)).when(session).getUser();
        doReturn(testTopic).when(topicService).getTopicByID(anyInt());
        doReturn(true).when(topicService).isBanned(any(), any());
        assertNull(reportEditBacker.saveChangesWithConfirm());
        assertFalse(reportEditBacker.isDisplayConfirmDialog());
    }

    @Test
    public void testSaveChanges() {
        doReturn(true).when(reportService).updateReport(any());
        assertNotNull(reportEditBacker.saveChanges());
    }

    @Test
    public void testSaveChangesWhenError() {
        doReturn(false).when(reportService).updateReport(any());
        assertNull(reportEditBacker.saveChanges());
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

}