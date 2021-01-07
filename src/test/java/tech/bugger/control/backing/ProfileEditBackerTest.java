package tech.bugger.control.backing;

import com.sun.faces.context.RequestParameterMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.service.ProfileService;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;

import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doReturn;

@ExtendWith(LogExtension.class)
public class ProfileEditBackerTest {

    @InjectMocks
    private ProfileEditBacker profileEditBacker;

    @Mock
    private UserSession session;

    @Mock
    private FacesContext fctx;

    @Mock
    private ExternalContext ext;

    @Mock
    private ProfileService profileService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private RequestParameterMap map;

    @Mock
    private HttpServletRequest request;

    @Mock
    private NavigationHandler navHandler;

    @Mock
    private Application application;

    private User user;
    private static final String TOKEN = "token";
    private static final String CREATE = "c";
    private static final String EDIT = "e";
    private static final String ERROR = "error.xhtml";
    private static final String HOME = "home.xhtml";
    private static final String PROFILE = "profile.xhtml";
    private static final String EMAIL = "test@test.de";
    private Token emailToken;
    private Field createUser;

    @BeforeEach
    public void setup() throws NoSuchFieldException {
        user = new User(12345, "Helgi", "v3ry_s3cur3", "salt", "algorithm", "helga@web.de", "Helga", "Brötchen", new Lazy<>(new byte[1]),
                new byte[]{1}, "Hallo, ich bin die Helgi | Perfect | He/They/Her | vergeben | Abo =|= endorsement",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, ZonedDateTime.now(), null, false);
        emailToken = new Token(TOKEN, Token.Type.CHANGE_EMAIL, ZonedDateTime.now(), user);
        MockitoAnnotations.openMocks(this);
        createUser = profileEditBacker.getClass().getDeclaredField("create");
        createUser.setAccessible(true);
        when(fctx.getExternalContext()).thenReturn(ext);
        when(fctx.getApplication()).thenReturn(application);
        when(application.getNavigationHandler()).thenReturn(navHandler);
        when(ext.getRequestParameterMap()).thenReturn(map);
        when(ext.getRequest()).thenReturn(request);
    }

    @Test
    public void testInit() {
        when(session.getUser()).thenReturn(user);
        when(profileService.getUser(user.getId())).thenReturn(user);
        profileEditBacker.init();
        assertAll(
                () -> assertEquals(user, profileEditBacker.getUser()),
                () -> assertEquals(user.getEmailAddress(), profileEditBacker.getEmailNew()),
                () -> assertEquals(user.getUsername(), profileEditBacker.getUsernameNew()),
                () -> assertEquals(ProfileEditBacker.DialogType.NONE, profileEditBacker.getDialog())
        );
    }

    @Test
    public void testInitWithToken() {
        when(map.containsKey(TOKEN)).thenReturn(true);
        when(map.get(TOKEN)).thenReturn(TOKEN);
        when(authenticationService.findToken(TOKEN)).thenReturn(emailToken);
        when(session.getUser()).thenReturn(user);
        when(profileService.getUser(user.getId())).thenReturn(user);
        profileEditBacker.init();
        assertAll(
                () -> assertEquals(user, profileEditBacker.getUser()),
                () -> assertEquals(user.getEmailAddress(), profileEditBacker.getEmailNew()),
                () -> assertEquals(user.getUsername(), profileEditBacker.getUsernameNew()),
                () -> assertEquals(ProfileEditBacker.DialogType.NONE, profileEditBacker.getDialog())
        );
    }

    @Test
    public void testInitWithTokenNull() {
        when(map.containsKey(TOKEN)).thenReturn(true);
        when(map.get(TOKEN)).thenReturn(TOKEN);
        when(session.getUser()).thenReturn(user);
        profileEditBacker.init();
        //Since navHandler is mocked, method execution continues after the first redirect in updateUserEmail().
        verify(navHandler, times(2)).handleNavigation(any(), any(), any());
    }

    @Test
    public void testInitWithTokenWrongType() {
        emailToken.setType(Token.Type.REGISTER);
        when(map.containsKey(TOKEN)).thenReturn(true);
        when(map.get(TOKEN)).thenReturn(TOKEN);
        when(authenticationService.findToken(TOKEN)).thenReturn(emailToken);
        when(session.getUser()).thenReturn(user);
        profileEditBacker.init();
        //Since navHandler is mocked, method execution continues after the first redirect in updateUserEmail().
        verify(navHandler, times(2)).handleNavigation(any(), any(), any());
    }

    @Test
    public void testInitWithCreate() {
        user.setAdministrator(true);
        when(session.getUser()).thenReturn(user);
        when(map.containsKey(CREATE)).thenReturn(true);
        User createUser = new User();
        profileEditBacker.init();
        assertAll(
                () -> assertEquals(createUser, profileEditBacker.getUser()),
                () -> assertEquals(createUser.getEmailAddress(), profileEditBacker.getEmailNew()),
                () -> assertEquals(createUser.getUsername(), profileEditBacker.getUsernameNew()),
                () -> assertEquals(ProfileEditBacker.DialogType.NONE, profileEditBacker.getDialog()),
                () -> assertTrue(profileEditBacker.isCreate())
        );
    }

    @Test
    public void testInitWithCreateNoAdmin() {
        when(session.getUser()).thenReturn(user);
        when(map.containsKey(CREATE)).thenReturn(true);
        when(profileService.getUser(user.getId())).thenReturn(user);
        profileEditBacker.init();
        assertAll(
                () -> assertEquals(user, profileEditBacker.getUser()),
                () -> assertEquals(user.getEmailAddress(), profileEditBacker.getEmailNew()),
                () -> assertEquals(user.getUsername(), profileEditBacker.getUsernameNew()),
                () -> assertEquals(ProfileEditBacker.DialogType.NONE, profileEditBacker.getDialog())
        );
    }

    @Test
    public void testInitWithEdit() {
        when(session.getUser()).thenReturn(user);
        when(map.containsKey(EDIT)).thenReturn(true);
        when(map.get(EDIT)).thenReturn(user.getId().toString());
        when(profileService.getUser(user.getId())).thenReturn(user);
        profileEditBacker.init();
        assertAll(
                () -> assertEquals(user, profileEditBacker.getUser()),
                () -> assertEquals(user.getEmailAddress(), profileEditBacker.getEmailNew()),
                () -> assertEquals(user.getUsername(), profileEditBacker.getUsernameNew()),
                () -> assertEquals(ProfileEditBacker.DialogType.NONE, profileEditBacker.getDialog())
        );
    }

    @Test
    public void testInitWithEditInvalidId() {
        when(session.getUser()).thenReturn(user);
        when(map.containsKey(EDIT)).thenReturn(true);
        when(map.get(EDIT)).thenReturn("abc");
        profileEditBacker.init();
        assertAll(
                () -> assertNull(profileEditBacker.getUser()),
                () -> assertEquals(ProfileEditBacker.DialogType.NONE, profileEditBacker.getDialog())
        );
        verify(navHandler, times(1)).handleNavigation(any(), any(), any());
    }

    @Test
    public void testInitSessionUserNull() {
        //Necessary since method execution continues after first call to handleNavigation().
        when(session.getUser()).thenReturn(null).thenReturn(user);
        profileEditBacker.init();
        verify(navHandler, times(2)).handleNavigation(any(), any(), any());
    }

    @Test
    public void testInitGetUserNull() {
        when(session.getUser()).thenReturn(user);
        profileEditBacker.init();
        verify(navHandler, times(1)).handleNavigation(any(), any(), any());
    }

    @Test
    public void testSaveChangesCreate() throws IllegalAccessException {
        createUser.setBoolean(profileEditBacker, true);
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        when(profileService.createUser(any())).thenReturn(true);
        profileEditBacker.saveChanges();
        verify(profileService, times(1)).matchingPassword(any(), any());
        verify(profileService, times(1)).createUser(any());
        verify(navHandler, times(1)).handleNavigation(any(), any(), any());
    }

    @Test
    public void testSaveChangesCreateUnsuccessful() throws IllegalAccessException {
        createUser.setBoolean(profileEditBacker, true);
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        profileEditBacker.saveChanges();
        verify(profileService, times(1)).matchingPassword(any(), any());
        verify(profileService, times(1)).createUser(any());
    }

    @Test
    public void testSaveChangesPasswordNotMatching() throws IllegalAccessException {
        createUser.setBoolean(profileEditBacker, true);
        when(profileService.matchingPassword(any(), any())).thenReturn(false);
        profileEditBacker.saveChanges();
        verify(profileService, times(1)).matchingPassword(any(), any());
        verify(profileService, times(0)).createUser(any());
    }

    @Test
    public void testSaveChanges() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        when(profileService.updateUser(user)).thenReturn(true);
        profileEditBacker.setUser(user);
        profileEditBacker.setEmailNew(user.getEmailAddress());
        profileEditBacker.saveChanges();
        verify(profileService, times(1)).matchingPassword(any(), any());
        verify(navHandler, times(1)).handleNavigation(any(), any(), any());
    }

    @Test
    public void testSaveChangesUnsuccessful() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        profileEditBacker.setUser(user);
        profileEditBacker.setEmailNew(user.getEmailAddress());
        profileEditBacker.saveChanges();
        verify(profileService, times(1)).matchingPassword(any(), any());
    }

    @Test
    public void testSaveChangesNewEmail() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        StringBuffer buffer = new StringBuffer("http://test.de/hello_there.xhtml?someparam=69420");
        doReturn(buffer).when(request).getRequestURL();
        when(authenticationService.updateEmail(any(), any())).thenReturn(true);
        when(profileService.updateUser(any())).thenReturn(true);
        profileEditBacker.setUser(user);
        profileEditBacker.setEmailNew(EMAIL);
        profileEditBacker.saveChanges();
        verify(profileService, times(1)).matchingPassword(any(), any());
        verify(authenticationService, times(1)).updateEmail(any(), any());
        verify(navHandler, times(1)).handleNavigation(any(), any(), any());
    }

    @Test
    public void testSaveChangesNewEmailUnsuccessful() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        StringBuffer buffer = new StringBuffer("http://test.de/hello_there.xhtml?someparam=69420");
        doReturn(buffer).when(request).getRequestURL();
        profileEditBacker.setUser(user);
        profileEditBacker.setEmailNew(EMAIL);
        profileEditBacker.saveChanges();
        verify(profileService, times(1)).matchingPassword(any(), any());
        verify(authenticationService, times(1)).updateEmail(any(), any());
    }

    @Test
    public void testSaveChangesNewEmailInvalidURL() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        StringBuffer buffer = new StringBuffer("not a link");
        doReturn(buffer).when(request).getRequestURL();
        profileEditBacker.setUser(user);
        profileEditBacker.setEmailNew(EMAIL);
        assertThrows(InternalError.class,
                () -> profileEditBacker.saveChanges()
        );
    }

    @Test
    public void testDelete() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        when(profileService.deleteUser(user)).thenReturn(true);
        profileEditBacker.setUser(user);
        profileEditBacker.delete();
        verify(profileService, times(1)).matchingPassword(any(), any());
        verify(profileService, times(1)).deleteUser(user);
        verify(navHandler, times(1)).handleNavigation(any(), any(), any());
    }

    @Test
    public void testDeleteFails() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        profileEditBacker.setUser(user);
        profileEditBacker.delete();
        verify(profileService, times(1)).matchingPassword(any(), any());
        verify(profileService, times(1)).deleteUser(user);
    }

    @Test
    public void testDeleteWrongPassword() {
        profileEditBacker.setUser(user);
        profileEditBacker.delete();
        verify(profileService, times(1)).matchingPassword(any(), any());
    }

    @Test
    public void testDeleteEqualSessionUser() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        when(profileService.deleteUser(user)).thenReturn(true);
        when(session.getUser()).thenReturn(user);
        profileEditBacker.setUser(user);
        profileEditBacker.delete();
        verify(profileService, times(1)).matchingPassword(any(), any());
        verify(profileService, times(1)).deleteUser(user);
        verify(session, times(1)).invalidateSession();
        verify(navHandler, times(1)).handleNavigation(any(), any(), any());
    }

    @Test
    public void testDeleteAvatar() {
        profileEditBacker.setUser(user);
        profileEditBacker.deleteAvatar();
        assertAll(
                () -> assertEquals(0, user.getAvatar().get().length),
                () -> assertEquals(0, user.getAvatarThumbnail().length)
        );
    }

    @Test
    public void testUploadAvatar() {
        Lazy<byte[]> avatar = new Lazy<>(new byte[]{1, 2, 3, 4});
        byte[] thumbnail = new byte[]{1, 2, 3, 4};
        when(profileService.uploadAvatar(any())).thenReturn(avatar);
        when(profileService.generateThumbnail(any())).thenReturn(thumbnail);
        profileEditBacker.setUser(user);
        profileEditBacker.uploadAvatar();
        assertAll(
                () -> assertEquals(avatar.get(), user.getAvatar().get()),
                () -> assertEquals(thumbnail, user.getAvatarThumbnail())
        );
        verify(profileService, times(1)).uploadAvatar(any());
        verify(profileService, times(1)).generateThumbnail(any());
    }

    @Test
    public void testUploadAvatarGenerateThumbnailFails() {
        Lazy<byte[]> avatar = new Lazy<>(new byte[]{1, 2, 3, 4});
        when(profileService.uploadAvatar(any())).thenReturn(avatar);
        profileEditBacker.setUser(user);
        profileEditBacker.uploadAvatar();
        assertAll(
                () -> assertEquals(avatar.get(), user.getAvatar().get()),
                () -> assertEquals(0, user.getAvatarThumbnail().length)
        );
        verify(profileService, times(1)).uploadAvatar(any());
        verify(profileService, times(1)).generateThumbnail(any());
    }

    @Test
    public void testUploadAvatarFails() {
        profileEditBacker.setUser(user);
        profileEditBacker.uploadAvatar();
        assertAll(
                () -> assertEquals(0, user.getAvatar().get().length),
                () -> assertEquals(0, user.getAvatarThumbnail().length)
        );
        verify(profileService, times(1)).uploadAvatar(any());
    }

    @Test
    public void testOpenDeleteDialog() {
        profileEditBacker.setDialog(ProfileEditBacker.DialogType.NONE);
        profileEditBacker.openDeleteDialog();
        assertEquals(ProfileEditBacker.DialogType.DELETE, profileEditBacker.getDialog());
    }

    @Test
    public void testCloseDeleteDialog() {
        profileEditBacker.setDialog(ProfileEditBacker.DialogType.DELETE);
        profileEditBacker.closeDeleteDialog();
        assertEquals(ProfileEditBacker.DialogType.NONE, profileEditBacker.getDialog());
    }

    @Test
    public void testOpenChangeDialog() {
        profileEditBacker.setDialog(ProfileEditBacker.DialogType.NONE);
        profileEditBacker.openChangeDialog();
        assertEquals(ProfileEditBacker.DialogType.UPDATE, profileEditBacker.getDialog());
    }

    @Test
    public void testCloseChangeDialog() {
        profileEditBacker.setDialog(ProfileEditBacker.DialogType.UPDATE);
        profileEditBacker.closeChangeDialog();
        assertEquals(ProfileEditBacker.DialogType.NONE, profileEditBacker.getDialog());
    }
}
