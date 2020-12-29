package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.service.SettingsService;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;

import javax.faces.context.ExternalContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

    @BeforeEach
    public void setUp() {
        this.adminBacker = new AdminBacker(applicationSettings, settingsService, ectx);
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
        assertEquals(oldLogo, adminBacker.getOrganization().getLogo());
    }

    @Test
    public void testUploadLogoWhenStreamError() throws Exception {
        byte[] oldLogo = adminBacker.getOrganization().getLogo();
        doThrow(IOException.class).when(part).getInputStream();
        doReturn(part).when(vce).getNewValue();
        adminBacker.uploadLogo(vce);
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
        doReturn(Arrays.asList("a", "b")).when(settingsService).discoverFiles(any());
        assertEquals(Arrays.asList("a", "b"), adminBacker.getAvailableThemes());
    }

    @Test
    public void testGetAvailableThemesWhenThereAreNone() {
        doReturn(new ArrayList<>()).when(settingsService).discoverFiles(any());
        assertEquals(Arrays.asList(adminBacker.getOrganization().getTheme()), adminBacker.getAvailableThemes());
    }

    @Test
    public void testUpdateOrganizationWhenSuccess() {
        doReturn(true).when(settingsService).updateOrganization(any());
        adminBacker.saveOrganization();
        verify(applicationSettings).setOrganization(adminBacker.getOrganization());
    }

    @Test
    public void testUpdateOrganizationWhenFailure() {
        doReturn(false).when(settingsService).updateOrganization(any());
        adminBacker.saveOrganization();
        verify(applicationSettings, never()).setOrganization(any());
    }

    @Test
    public void testUpdateConfigurationWhenSuccess() {
        doReturn(true).when(settingsService).updateConfiguration(any());

        adminBacker.saveConfiguration();
        verify(applicationSettings).setConfiguration(adminBacker.getConfiguration());
    }

    @Test
    public void testUpdateConfigurationWhenFailure() {
        doReturn(false).when(settingsService).updateConfiguration(any());
        adminBacker.saveConfiguration();
        verify(applicationSettings, never()).setConfiguration(any());
    }
}