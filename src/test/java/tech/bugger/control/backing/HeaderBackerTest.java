package tech.bugger.control.backing;

import com.ocpsoft.pretty.PrettyContext;
import com.ocpsoft.pretty.faces.config.mapping.UrlMapping;
import com.sun.faces.context.RequestParameterMap;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.enterprise.event.Event;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.SearchService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class HeaderBackerTest {

    private HeaderBacker headerBacker;

    @Mock
    private ApplicationSettings settings;

    @Mock
    private SearchService searchService;

    @Mock
    private UserSession session;

    @Mock
    private FacesContext fctx;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Event<Feedback> feedback;

    @Mock
    private ResourceBundle messages;

    @Mock
    private RequestParameterMap map;

    @Mock
    private PrettyContext prettyContext;

    @Mock
    private HttpServletRequest request;

    @Mock
    private UrlMapping mapping;

    private Field field;
    private User user;
    private static final String KEY = "url";
    private static MockedStatic<PrettyContext> config;
    private final List<FacesMessage> facesMessages = new ArrayList<>();
    private final FacesMessage facesMessage = new FacesMessage("Danger!");

    @BeforeEach
    public void setup() throws NoSuchFieldException {
        user = new User();
        MockitoAnnotations.openMocks(this);
        lenient().doReturn(user).when(session).getUser();
        lenient().doReturn(ectx).when(fctx).getExternalContext();
        lenient().doReturn(map).when(ectx).getRequestParameterMap();
        lenient().doReturn(request).when(ectx).getRequest();
        config = mockStatic(PrettyContext.class);
        config.when(PrettyContext::getCurrentInstance).thenReturn(prettyContext);
        headerBacker = new HeaderBacker(searchService, session, fctx, ectx, feedback, messages);
    }

    @AfterEach
    public void tearDown() {
        config.close();
    }

    @Test
    public void testInit() throws IllegalAccessException, NoSuchFieldException {
        headerBacker.init();
        assertAll(
                () -> assertEquals(user, headerBacker.getUser())
        );
    }

    @Test
    public void testDetermineAlertClassError() {
        facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
        facesMessages.add(facesMessage);
        doReturn(facesMessages).when(fctx).getMessageList(null);
        doReturn(facesMessages).when(fctx).getMessageList();
        assertEquals(" alert-danger", headerBacker.determineAlertClass());
    }

    @Test
    public void testDetermineAlertClassWarn() {
        facesMessage.setSeverity(FacesMessage.SEVERITY_WARN);
        facesMessages.add(facesMessage);
        doReturn(facesMessages).when(fctx).getMessageList(null);
        doReturn(facesMessages).when(fctx).getMessageList();
        assertEquals(" alert-warning", headerBacker.determineAlertClass());
    }

    @Test
    public void testDetermineAlertClassInfo() {
        facesMessage.setSeverity(FacesMessage.SEVERITY_INFO);
        facesMessages.add(facesMessage);
        doReturn(facesMessages).when(fctx).getMessageList(null);
        doReturn(facesMessages).when(fctx).getMessageList();
        assertEquals(" alert-success", headerBacker.determineAlertClass());
    }

    @Test
    public void testDetermineAlertClassPrimary() {
        facesMessage.setSeverity(FacesMessage.SEVERITY_FATAL);
        facesMessages.add(facesMessage);
        doReturn(facesMessages).when(fctx).getMessageList(null);
        doReturn(facesMessages).when(fctx).getMessageList();
        assertEquals(" alert-primary", headerBacker.determineAlertClass());
    }

    @Test
    public void testDetermineAlertClassEmpty() {
        assertTrue(headerBacker.determineAlertClass().isBlank());
    }

    @Test
    public void testGetRedirectUrl() {
        doReturn(true).when(map).containsKey(KEY);
        doReturn(KEY).when(map).get(KEY);
        assertEquals(KEY, headerBacker.getRedirectUrl());
    }

    @Test
    public void testGetRedirectUrlNoKey() {
        doReturn(mapping).when(prettyContext).getCurrentMapping();
        assertNotNull(headerBacker.getRedirectUrl());
    }

    @Test
    public void testGetRedirectUrlMappingNull() {
        doReturn("").when(request).getRequestURI();
        doReturn("").when(request).getQueryString();
        assertNotNull(headerBacker.getRedirectUrl());
    }

    @Test
    public void testUpdateSuggestions() {
        headerBacker.setSearch(KEY);
        headerBacker.updateSuggestions();
        assertAll(
                () -> assertEquals(LocalDate.now().getYear(), headerBacker.getCurrentYear()),
                () -> assertEquals(KEY, headerBacker.getSearch()),
                () -> assertNotNull(headerBacker.getReportSearchSuggestion()),
                () -> assertNotNull(headerBacker.getTopicSearchSuggestion()),
                () -> assertNotNull(headerBacker.getUserSearchSuggestion())
        );
    }

    @Test
    public void testUpdateSuggestionsSearchNull() {
        headerBacker.updateSuggestions();
        verify(searchService, never()).getUserSuggestions(anyString());
    }

    @Test
    public void testUpdateSuggestionsSearchBlank() {
        headerBacker.setSearch("");
        headerBacker.updateSuggestions();
        verify(searchService, never()).getUserSuggestions(anyString());
    }

    @Test
    public void testValidateForm() {
        doReturn(true).when(fctx).isValidationFailed();
        headerBacker.validateForm();
        verify(feedback).fire(any());
    }

    @Test
    public void testValidateFormListNotEmpty() {
        facesMessage.setSeverity(FacesMessage.SEVERITY_FATAL);
        facesMessages.add(facesMessage);
        doReturn(facesMessages).when(fctx).getMessageList(null);
        doReturn(true).when(fctx).isValidationFailed();
        headerBacker.validateForm();
        verify(feedback, never()).fire(any());
    }

    @Test
    public void testValidateFormTrue() {
        headerBacker.validateForm();
        verify(feedback, never()).fire(any());
    }

    @Test
    public void testLogout() {
        assertEquals("pretty:home", headerBacker.logout());
        verify(ectx).invalidateSession();
    }

    @Test
    public void testExecuteSearch() throws IOException {
        headerBacker.executeSearch();
        verify(ectx).redirect(any());
    }

    @Test
    public void testSettersForCoverage() {
        headerBacker.setUser(user);
        headerBacker.setUserSearchSuggestion(new ArrayList<>());
        headerBacker.setReportSearchSuggestion(new ArrayList<>());
        headerBacker.setTopicSearchSuggestion(new ArrayList<>());
        assertAll(
                () -> assertEquals(user, headerBacker.getUser()),
                () -> assertTrue(headerBacker.getUserSearchSuggestion().isEmpty()),
                () -> assertTrue(headerBacker.getReportSearchSuggestion().isEmpty()),
                () -> assertTrue(headerBacker.getTopicSearchSuggestion().isEmpty())
        );
    }

}
