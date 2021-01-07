package tech.bugger.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.AttachmentGateway;
import tech.bugger.persistence.gateway.PostGateway;
import tech.bugger.persistence.gateway.ReportGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    private ReportService service;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PostService postService;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private ReportGateway reportGateway;

    @Mock
    private PostGateway postGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

    private Report testReport;

    @BeforeEach
    public void setUp() {
        service = new ReportService(notificationService, postService, transactionManager, feedbackEvent,
                ResourceBundleMocker.mock(""));
        List<Attachment> attachments = Arrays.asList(new Attachment(), new Attachment(), new Attachment());
        testReport = new Report(200, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", mock(Authorship.class),
                mock(ZonedDateTime.class), null, null, 0);

        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(reportGateway).when(tx).newReportGateway();
        lenient().doReturn(postGateway).when(tx).newPostGateway();
    }

    @Test
    public void testGetReportByIDWhenExists() throws Exception{
        testReport.setId(100);
        doReturn(testReport).when(reportGateway).find(anyInt());
        assertEquals(testReport, service.getReportByID(100));
    }

    @Test
    public void testGetReportByIDWhenNotExists() throws Exception {
        doThrow(NotFoundException.class).when(reportGateway).find(anyInt());
        assertNull(service.getReportByID(100));
    }

    @Test
    public void testGetReportByIDWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(service.getReportByID(100));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpdateReportWhenFine() throws Exception {
        assertTrue(service.updateReport(testReport));
        verify(reportGateway).update(testReport);
    }

    @Test
    public void testUpdateReportWhenNotExists() throws Exception {
        doThrow(NotFoundException.class).when(reportGateway).update(any());
        assertFalse(service.updateReport(testReport));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpdateReportWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.updateReport(testReport));
        verify(feedbackEvent).fire(any());
    }

}