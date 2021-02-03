package tech.bugger.control.backing;

import com.sun.faces.context.RequestParameterMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.SearchService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Paginator;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.*;

import javax.faces.context.ExternalContext;
import java.lang.reflect.Field;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(LogExtension.class)
public class SearchBackerTest {

    private SearchBacker searchBacker;

    @Mock
    private TopicService topicService;

    @Mock
    private SearchService searchService;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private Configuration configuration;

    @Mock
    private ExternalContext ectx;

    @Mock
    private RequestParameterMap map;

    @Mock
    private Paginator<Topic> topicResults;

    @Mock
    private Paginator<Report> reportResults;

    @Mock
    private Paginator<User> userResults;

    User user1;
    User user2;
    Topic topic1;
    Topic topic2;
    Report report1;
    Report report2;
    private String query;
    private SearchBacker.Tab tab;
    private static final String KEY1 = "q";
    private static final String KEY2 = "t";
    private static final String USERNAME1 = "user1";
    private static final String USERNAME2 = "user2";
    private static final String TOPICTITLE1 = "topic1";
    private static final String TOPICTITLE2 = "topic2";
    private static final String REPORTTITLE1 = "report1";
    private static final String REPORTTITLE2 = "report2";

    @BeforeEach
    public void setUp() {
        user1 = new User(1, USERNAME1, "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am test user 1.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        user2 = new User(2, USERNAME2, "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am test user 2.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        topic1 = new Topic(1, TOPICTITLE1, "# I am test topic 1.");
        topic2 = new Topic(2, TOPICTITLE2, "# I am test topic 2.");
        report1 = new Report(1, REPORTTITLE1, Report.Type.BUG, Report.Severity.MINOR, "",
                new Authorship(null, null, null, null), null,
                null, null, false, 0, null);
        report2 = new Report(2, REPORTTITLE2, Report.Type.BUG, Report.Severity.MINOR, "",
                new Authorship(null, null, null, null), null,
                null, null, false, 0,
                null);
        searchBacker = new SearchBacker(searchService, topicService, ectx);
        lenient().doReturn(map).when(ectx).getRequestParameterMap();
        lenient().doReturn(configuration).when(applicationSettings).getConfiguration();
    }

    @Test
    public void testInitTabReport() {
        tab = SearchBacker.Tab.REPORT;
        doReturn(true).when(map).containsKey(KEY1);
        doReturn(true).when(map).containsKey(KEY2);
        doReturn(query).when(map).get(KEY1);
        doReturn(tab.toString()).when(map).get(KEY2);
        searchBacker.init();
        assertAll(
                () -> assertEquals(query, searchBacker.getQuery()),
                () -> assertEquals(tab, searchBacker.getTab()),
                () -> assertTrue(searchBacker.isOpenReportShown()),
                () -> assertTrue(searchBacker.isClosedReportShown()),
                () -> assertTrue(searchBacker.isDuplicatesShown()),
                () -> assertTrue(searchBacker.isAdminShown()),
                () -> assertTrue(searchBacker.isNonAdminShown()),
                () -> assertTrue(searchBacker.isShowBug()),
                () -> assertTrue(searchBacker.isShowFeature()),
                () -> assertTrue(searchBacker.isShowHint()),
                () -> assertTrue(searchBacker.isShowMinor()),
                () -> assertTrue(searchBacker.isShowSevere()),
                () -> assertTrue(searchBacker.isShowRelevant())
        );
    }

    @Test
    public void testInitTabUser() {
        tab = SearchBacker.Tab.USER;
        doReturn(true).when(map).containsKey(KEY1);
        doReturn(true).when(map).containsKey(KEY2);
        doReturn(query).when(map).get(KEY1);
        doReturn(tab.toString()).when(map).get(KEY2);
        searchBacker.init();
        assertAll(
                () -> assertEquals(query, searchBacker.getQuery()),
                () -> assertEquals(tab, searchBacker.getTab()),
                () -> assertTrue(searchBacker.isOpenReportShown()),
                () -> assertTrue(searchBacker.isClosedReportShown()),
                () -> assertTrue(searchBacker.isDuplicatesShown()),
                () -> assertTrue(searchBacker.isAdminShown()),
                () -> assertTrue(searchBacker.isNonAdminShown()),
                () -> assertTrue(searchBacker.isShowBug()),
                () -> assertTrue(searchBacker.isShowFeature()),
                () -> assertTrue(searchBacker.isShowHint()),
                () -> assertTrue(searchBacker.isShowMinor()),
                () -> assertTrue(searchBacker.isShowSevere()),
                () -> assertTrue(searchBacker.isShowRelevant())
        );
    }

    @Test
    public void testInitTabTopic() {
        tab = SearchBacker.Tab.TOPIC;
        doReturn(true).when(map).containsKey(KEY1);
        doReturn(true).when(map).containsKey(KEY2);
        doReturn(query).when(map).get(KEY1);
        doReturn(tab.toString()).when(map).get(KEY2);
        searchBacker.init();
        assertAll(
                () -> assertEquals(query, searchBacker.getQuery()),
                () -> assertEquals(tab, searchBacker.getTab()),
                () -> assertTrue(searchBacker.isOpenReportShown()),
                () -> assertTrue(searchBacker.isClosedReportShown()),
                () -> assertTrue(searchBacker.isDuplicatesShown()),
                () -> assertTrue(searchBacker.isAdminShown()),
                () -> assertTrue(searchBacker.isNonAdminShown()),
                () -> assertTrue(searchBacker.isShowBug()),
                () -> assertTrue(searchBacker.isShowFeature()),
                () -> assertTrue(searchBacker.isShowHint()),
                () -> assertTrue(searchBacker.isShowMinor()),
                () -> assertTrue(searchBacker.isShowSevere()),
                () -> assertTrue(searchBacker.isShowRelevant())
        );
    }

    @Test
    public void testInitNumberFormat() {
        query = "NotATab";
        doReturn(false).when(map).containsKey(KEY1);
        doReturn(true).when(map).containsKey(KEY2);
        doReturn(query).when(map).get(KEY2);
        assertThrows(Error404Exception.class,
                () -> searchBacker.init()
        );
    }

    @Test
    public void testSearchTabTopic() {
        query = "1";
        doReturn(topicResults).when(searchService).
        searchBacker.setTab(SearchBacker.Tab.TOPIC);
        searchBacker.setQuery(query);
        searchBacker.search();
        assertAll(
                () -> assertEquals(query, searchBacker.getQuery()),
                () -> assertEquals(tab, searchBacker.getTab()),
                () -> assertTrue(searchBacker.isOpenReportShown()),
                () -> assertTrue(searchBacker.isClosedReportShown()),
                () -> assertTrue(searchBacker.isDuplicatesShown()),
                () -> assertTrue(searchBacker.isAdminShown()),
                () -> assertTrue(searchBacker.isNonAdminShown()),
                () -> assertTrue(searchBacker.isShowBug()),
                () -> assertTrue(searchBacker.isShowFeature()),
                () -> assertTrue(searchBacker.isShowHint()),
                () -> assertTrue(searchBacker.isShowMinor()),
                () -> assertTrue(searchBacker.isShowSevere()),
                () -> assertTrue(searchBacker.isShowRelevant())
        );
    }

}
