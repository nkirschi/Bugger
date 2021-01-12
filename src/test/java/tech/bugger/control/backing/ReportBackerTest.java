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
import tech.bugger.business.service.TopicService;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;

import javax.faces.context.FacesContext;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(LogExtension.class)
public class ReportBackerTest {

    private ReportBacker reportBacker;

    @Mock
    private TopicService topicService;

    @Mock
    private ReportService reportService;

    @Mock
    private PostService postService;

    @Mock
    private UserSession session;

    @Mock
    private FacesContext fctx;

    @Mock
    private ApplicationSettings settings;

    private User user;
    private Report report;

    @BeforeEach
    public void setUp() {
        reportBacker = new ReportBacker(settings, reportService, postService, topicService, session, fctx);
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User", new Lazy<>(new byte[]{1, 2, 3, 4}), new byte[]{1}, "# I am a test user.",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        report = new Report(100, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", mock(Authorship.class),
                mock(ZonedDateTime.class), null, null, 1);
    }

    @Test
    public void testIsPrivileged() {
        Authorship authorship = new Authorship(user, ZonedDateTime.now(), null, null);
        report.setAuthorship(authorship);
        reportBacker.setReport(report);
        when(session.getUser()).thenReturn(user);
        assertTrue(reportBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedUserModerator() {
        reportBacker.setReport(report);
        when(session.getUser()).thenReturn(user);
        when(topicService.isModerator(any(), any())).thenReturn(true);
        assertTrue(reportBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedUserAdmin() {
        reportBacker.setReport(report);
        user.setAdministrator(true);
        when(session.getUser()).thenReturn(user);
        assertTrue(reportBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedNot() {
        Authorship authorship = new Authorship(new User(user), ZonedDateTime.now(), null, null);
        user.setId(2);
        report.setAuthorship(authorship);
        reportBacker.setReport(report);
        when(session.getUser()).thenReturn(user);
        assertFalse(reportBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedUserNull() {
        reportBacker.setReport(report);
        assertFalse(reportBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedReportNull() {
        when(session.getUser()).thenReturn(user);
        assertFalse(reportBacker.isPrivileged());
    }

    @Test
    public void testIsPrivilegedUserBanned() {
        reportBacker.setReport(report);
        when(session.getUser()).thenReturn(user);
        when(topicService.isBanned(any(), any())).thenReturn(true);
        assertFalse(reportBacker.isPrivileged());
    }

    @Test
    public void testIsBanned() {
        reportBacker.setReport(report);
        when(session.getUser()).thenReturn(user);
        when(topicService.isBanned(any(), any())).thenReturn(true);
        assertTrue(reportBacker.isBanned());
    }

    @Test
    public void testIsBannedFalse() {
        reportBacker.setReport(report);
        when(session.getUser()).thenReturn(user);
        assertFalse(reportBacker.isBanned());
    }

    @Test
    public void testIsBannedUserNull() {
        reportBacker.setReport(report);
        assertFalse(reportBacker.isBanned());
    }

    @Test
    public void testIsBannedReportNull() {
        when(session.getUser()).thenReturn(user);
        assertFalse(reportBacker.isBanned());
    }

}
