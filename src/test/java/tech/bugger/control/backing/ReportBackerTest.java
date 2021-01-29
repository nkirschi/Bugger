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
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.User;

import javax.faces.context.ExternalContext;
import java.time.OffsetDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

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
    private ApplicationSettings settings;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Configuration configuration;

    private User user;
    private Report report;

    @BeforeEach
    public void setUp() {
        reportBacker = new ReportBacker(settings, topicService, reportService, postService, session, ectx);
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test",
                "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        report = new Report(100, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", mock(Authorship.class),
                mock(OffsetDateTime.class), null, null, false, 1, null);
    }

    @Test
    public void testIsPrivilegedUserNull() {
        reportBacker.setReport(report);
        assertFalse(reportBacker.isPrivileged());
    }

    @Test
    public void testIsBannedUserNull() {
        reportBacker.setReport(report);
        assertFalse(reportBacker.isBanned());
    }

}
