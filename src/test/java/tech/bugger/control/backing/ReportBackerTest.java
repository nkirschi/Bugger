package tech.bugger.control.backing;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReportBackerTest {

    private ReportBacker reportBacker;

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
    private FacesContext fctx;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Configuration configuration;

    @Mock
    private User user;

    private Report testReport;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().doReturn(user).when(session).getUser();
        lenient().doReturn(configuration).when(applicationSettings).getConfiguration();
        lenient().doReturn(ectx).when(fctx).getExternalContext();
        reportBacker = new ReportBacker(applicationSettings, reportService, postService,
                session, fctx);

        testReport = new Report(100, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", mock(Authorship.class),
                mock(ZonedDateTime.class), null, null, false, 1);
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
        doReturn(true).when(reportService).isPrivileged(any(), any());
        doReturn(false).when(reportService).markDuplicate(any(), eq(42));
        reportBacker.markDuplicate();
        verify(reportService, never()).close(any());
    }

    @Test
    public void testMarkDuplicateValidIsSelectedAndUnprivileged() {
        reportBacker.setReport(testReport);
        reportBacker.setDuplicateOfID(100);
        doReturn(false).when(reportService).isPrivileged(any(), any());
        reportBacker.markDuplicate();
        verify(reportService, never()).markDuplicate(any(), anyInt());
        verify(reportService, never()).close(any());
    }

    @Test
    public void testMarkDuplicateValidIsSelectedAndPrivileged() throws Exception {
        reportBacker.setReport(testReport);
        reportBacker.setDuplicateOfID(100);
        doReturn(true).when(reportService).isPrivileged(any(), any());
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
        doReturn(false).when(reportService).isPrivileged(any(), any());
        reportBacker.unmarkDuplicate();
        assertAll(() -> assertNotNull(reportBacker.getDuplicateOfID()),
                () -> verify(reportService, never()).unmarkDuplicate(any()));
    }

    @Test
    public void testUnmarkDuplicateError() {
        reportBacker.setDuplicateOfID(100);
        doReturn(true).when(reportService).isPrivileged(any(), any());
        doReturn(false).when(reportService).unmarkDuplicate(any());
        reportBacker.unmarkDuplicate();
        assertNotNull(reportBacker.getDuplicateOfID());
    }

    @Test
    public void testUnmarkDuplicateSuccess() {
        reportBacker.setDuplicateOfID(100);
        doReturn(true).when(reportService).isPrivileged(any(), any());
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
        doReturn(Map.of("id", "100")).when(ectx).getRequestParameterMap();
        doReturn(testReport).when(reportService).getReportByID(100);

        reportBacker.init();

        List<Report> paginatedList = StreamSupport.stream(reportBacker.getDuplicates().spliterator(), false)
                .collect(Collectors.toList());
        assertAll(() -> assertEquals(duplicates, paginatedList),
                () -> assertEquals(duplicates.size(), reportBacker.getDuplicates().getSelection().getTotalSize()),
                () -> verify(reportService).getNumberOfDuplicates(any()),
                () -> verify(reportService).getDuplicatesFor(any(), any()));
    }

}
