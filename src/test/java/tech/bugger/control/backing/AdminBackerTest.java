package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.service.SettingsService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;

import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class AdminBackerTest {

    private AdminBacker adminBacker;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private SettingsService settingsService;

    @Mock
    private ExternalContext ectx;

    @Mock
    private ValueChangeEvent vce;

    @Mock
    private Part part;

    @Mock
    private Event<Feedback> feedbackEvent;

    @BeforeEach
    public void setUp() {
        this.adminBacker = new AdminBacker(applicationSettings, settingsService, ectx, feedbackEvent,
                                           ResourceBundleMocker.mock(""));
        adminBacker.setOrganization(new Organization("", new byte[1], "theme.css", "", "", ""));
        adminBacker.setConfiguration(new Configuration(true, false, "abc", ".x,.y,.z", 42, "0,1,2"));
    }

    @Test
    public void testInit() {
        Organization organization = adminBacker.getOrganization();
        Configuration configuration = adminBacker.getConfiguration();
        doReturn(organization).when(applicationSettings).getOrganization();
        doReturn(configuration).when(applicationSettings).getConfiguration();
        adminBacker.init();
        assertAll(
                () -> assertEquals(organization, adminBacker.getOrganization()),
                () -> assertNotSame(organization, adminBacker.getOrganization()),
                () -> assertEquals(configuration, adminBacker.getConfiguration()),
                () -> assertNotSame(configuration, adminBacker.getConfiguration())
        );
    }

    @Test
    public void testUploadLogoWhenFine() {
        byte[] newLogo = new byte[2];
        doReturn(part).when(vce).getNewValue();
        doReturn(newLogo).when(settingsService).readFile(any());
        adminBacker.uploadLogo(vce);
        assertEquals(newLogo, adminBacker.getOrganization().getLogo());
    }

    @Test
    public void testUploadLogoWhenReadError() {
        byte[] oldLogo = adminBacker.getOrganization().getLogo();
        doReturn(part).when(vce).getNewValue();
        doReturn(null).when(settingsService).readFile(any());
        adminBacker.uploadLogo(vce);
        verify(feedbackEvent).fire(new Feedback(any(), Feedback.Type.ERROR));
        assertEquals(oldLogo, adminBacker.getOrganization().getLogo());
    }

    @Test
    public void testUploadLogoWhenStreamError() throws Exception {
        byte[] oldLogo = adminBacker.getOrganization().getLogo();
        doThrow(IOException.class).when(part).getInputStream();
        doReturn(part).when(vce).getNewValue();
        adminBacker.uploadLogo(vce);
        verify(feedbackEvent).fire(new Feedback(any(), Feedback.Type.ERROR));
        assertEquals(oldLogo, adminBacker.getOrganization().getLogo());
    }


    @Test
    public void testRemoveLogoWhenTrue() {
        doReturn(true).when(vce).getNewValue();
        adminBacker.removeLogo(vce);
        assertArrayEquals(new byte[0], adminBacker.getOrganization().getLogo());
    }

    @Test
    public void testRemoveLogoWhenFalse() {
        doReturn(false).when(vce).getNewValue();
        byte[] logo = adminBacker.getOrganization().getLogo();
        adminBacker.removeLogo(vce);
        assertArrayEquals(logo, adminBacker.getOrganization().getLogo());
    }

    @Test
    public void testGetAvailableThemesWhenThereAreSome() {
        doReturn(List.of("a", "b.css")).when(settingsService).discoverFiles(any());
        assertEquals(List.of("b.css"), adminBacker.getAvailableThemes());
    }

    @Test
    public void testGetAvailableThemesWhenThereAreNone() {
        doReturn(Collections.emptyList()).when(settingsService).discoverFiles(any());
        assertEquals(List.of(adminBacker.getOrganization().getTheme()), adminBacker.getAvailableThemes());
    }

    @Test
    public void testGetAvailableThemesWhenError() {
        doReturn(null).when(settingsService).discoverFiles(any());
        assertEquals(List.of(adminBacker.getOrganization().getTheme()), adminBacker.getAvailableThemes());
        verify(feedbackEvent).fire(new Feedback(any(), Feedback.Type.ERROR));
    }

    @Test
    public void testUpdateOrganizationWhenSuccess() {
        doReturn(true).when(settingsService).updateOrganization(any());
        adminBacker.saveOrganization();
        verify(applicationSettings).setOrganization(adminBacker.getOrganization());
        verify(feedbackEvent).fire(new Feedback(any(), Feedback.Type.INFO));
    }

    @Test
    public void testUpdateOrganizationWhenFailure() {
        doReturn(false).when(settingsService).updateOrganization(any());
        adminBacker.saveOrganization();
        verify(applicationSettings, never()).setOrganization(any());
        verify(feedbackEvent).fire(new Feedback(any(), Feedback.Type.ERROR));
    }

    @Test
    public void testUpdateConfigurationWhenSuccess() {
        doReturn(true).when(settingsService).updateConfiguration(any());
        adminBacker.saveConfiguration();
        verify(applicationSettings).setConfiguration(adminBacker.getConfiguration());
        verify(feedbackEvent).fire(new Feedback(any(), Feedback.Type.INFO));
    }

    @Test
    public void testUpdateConfigurationWhenFailure() {
        doReturn(false).when(settingsService).updateConfiguration(any());
        adminBacker.saveConfiguration();
        verify(applicationSettings, never()).setConfiguration(any());
        verify(feedbackEvent).fire(new Feedback(any(), Feedback.Type.ERROR));
    }
}