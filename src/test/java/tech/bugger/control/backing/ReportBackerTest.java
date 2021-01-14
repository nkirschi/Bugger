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
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doReturn;

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

    @Mock
    private ExternalContext ectx;

    @Mock
    private Configuration configuration;

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

    @Test
    public void testMarkDuplicateNoneSelected() {
        reportBacker.setDuplicateOfID(null);
        reportBacker.markDuplicate();
        assertAll(() -> verify(reportService, never()).markDuplicate(any(), anyInt()),
                () -> verify(reportService, never()).close(any()));
    }

    @Test
    public void testMarkDuplicateInvalidIsSelected() {
        reportBacker.setDuplicateOfID(42);
        reportBacker.markDuplicate();
        verify(reportService, never()).close(any());
    }

    @Test
    public void testMarkDuplicateValidIsSelectedAndUnprivileged() {
        reportBacker.setReport(report);
        reportBacker.setDuplicateOfID(100);
        reportBacker.markDuplicate();
        verify(reportService, never()).markDuplicate(any(), anyInt());
        verify(reportService, never()).close(any());
    }

    @Test
    public void testMarkDuplicateValidIsSelectedAndPrivileged() throws Exception {
        reportBacker.setReport(report);
        reportBacker.setDuplicateOfID(100);
        user.setAdministrator(true);
        when(session.getUser()).thenReturn(user);
        doReturn(true).when(reportService).markDuplicate(any(), eq(100));
        Paginator<?> duplicates = mock(Paginator.class);

        Field f = ReportBacker.class.getDeclaredField("duplicates");
        f.setAccessible(true);
        f.set(reportBacker, duplicates);

        reportBacker.markDuplicate();
        verify(reportService).close(any());
        verify(duplicates).update();
    }

    @Test
    public void testUnmarkDuplicateUnprivileged() {
        reportBacker.setDuplicateOfID(100);
        reportBacker.unmarkDuplicate();
        assertAll(() -> assertNotNull(reportBacker.getDuplicateOfID()),
                () -> verify(reportService, never()).unmarkDuplicate(any()));
    }

    @Test
    public void testUnmarkDuplicateError() {
        reportBacker.setDuplicateOfID(100);
        reportBacker.unmarkDuplicate();
        assertNotNull(reportBacker.getDuplicateOfID());
    }

    @Test
    public void testUnmarkDuplicateSuccess() {
        reportBacker.setDuplicateOfID(100);
        reportBacker.setReport(report);
        user.setAdministrator(true);
        when(session.getUser()).thenReturn(user);
        doReturn(true).when(reportService).unmarkDuplicate(any());
        reportBacker.unmarkDuplicate();
        assertNull(reportBacker.getDuplicateOfID());
    }

    @Test
    public void testDuplicatePaginator() {
        Report duplicate1 = new Report();
        Report duplicate2 = new Report();
        List<Report> duplicates = List.of(duplicate1, duplicate2);
        doReturn(duplicates.size()).when(reportService).getNumberOfDuplicates(any());
        doReturn(duplicates).when(reportService).getDuplicatesFor(any(), any());
        doReturn(true).when(configuration).isGuestReading();
        doReturn(ectx).when(fctx).getExternalContext();
        doReturn(Map.of("id", "100")).when(ectx).getRequestParameterMap();
        doReturn(configuration).when(settings).getConfiguration();
        doReturn(report).when(reportService).getReportByID(100);

        reportBacker.init();

        List<Report> paginatedList = StreamSupport.stream(reportBacker.getDuplicates().spliterator(), false)
                .collect(Collectors.toList());
        assertAll(() -> assertEquals(duplicates, paginatedList),
                () -> assertEquals(duplicates.size(), reportBacker.getDuplicates().getSelection().getTotalSize()),
                () -> verify(reportService).getNumberOfDuplicates(any()),
                () -> verify(reportService).getDuplicatesFor(any(), any()));
    }

}
