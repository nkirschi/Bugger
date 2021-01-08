package tech.bugger.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.util.Feedback;
import tech.bugger.persistence.gateway.PostGateway;
import tech.bugger.persistence.gateway.ReportGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private ReportService reportService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PostService postService;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private Event<Feedback> feedbackEvent;

    @Mock
    private ReportGateway reportGateway;

    @Mock
    private PostGateway postGateway;

    @BeforeEach
    public void setUp() {
        reportService = new ReportService(notificationService, postService, transactionManager, feedbackEvent,
                ResourceBundleMocker.mock(""));
        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(reportGateway).when(tx).newReportGateway();
        lenient().doReturn(postGateway).when(tx).newPostGateway();
    }

    @Test
    public void testClose() {
    }

    @Test
    void open() {
    }

    @Test
    void deleteReport() {
    }

    @Test
    void getNumberOfPosts() {
    }

    @Test
    void getPostsFor() {
    }
}