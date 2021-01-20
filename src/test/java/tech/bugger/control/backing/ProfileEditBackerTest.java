package tech.bugger.control.backing;

import com.sun.faces.context.RequestParameterMap;
import java.util.Locale;
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
import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;

import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

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
    private static final String EMAIL = "test@test.de";
    private static final String PASSWORD = "password";
    private Token emailToken;
    private Field createUser;

    @BeforeEach
    public void setup() throws NoSuchFieldException {
        user = new User(12345, "Helgi", "v3ry_s3cur3", "salt", "algorithm", "helga@web.de", "Helga", "Brötchen",
                new byte[1], new byte[]{1}, "Hallo, ich bin die Helgi | Perfect | He/They/Her | vergeben | Abo =|= endorsement",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, OffsetDateTime.now(), null, false);
        emailToken = new Token(TOKEN, Token.Type.CHANGE_EMAIL, OffsetDateTime.now(), EMAIL, user);
        MockitoAnnotations.openMocks(this);
        profileEditBacker = new ProfileEditBacker(authenticationService, profileService, session, fctx);
        createUser = profileEditBacker.getClass().getDeclaredField("create");
        createUser.setAccessible(true);
        profileEditBacker.setPasswordNew("");
        profileEditBacker.setPasswordNewConfirm("");
        profileEditBacker.setUsernameNew(user.getUsername());
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
                () -> assertEquals(ProfileEditBacker.ProfileEditDialog.NONE, profileEditBacker.getDialog()),
                () -> assertFalse(profileEditBacker.isDeleteAvatar()),
                () -> assertTrue(profileEditBacker.getPasswordNew().isBlank()),
                () -> assertTrue(profileEditBacker.getPasswordNewConfirm().isBlank()),
                () -> assertNull(profileEditBacker.getPassword())
        );
    }

    @Test
    public void testInitWithToken() {
        when(map.containsKey(TOKEN)).thenReturn(true);
        when(map.get(TOKEN)).thenReturn(TOKEN);
        when(authenticationService.findToken(TOKEN)).thenReturn(emailToken);
        when(session.getUser()).thenReturn(user);
        when(profileService.getUser(emailToken.getUser().getId())).thenReturn(user);
        profileEditBacker.init();
        assertAll(
                () -> assertEquals(user, profileEditBacker.getUser()),
                () -> assertEquals(user.getEmailAddress(), profileEditBacker.getEmailNew()),
                () -> assertEquals(user.getUsername(), profileEditBacker.getUsernameNew()),
                () -> assertEquals(ProfileEditBacker.ProfileEditDialog.NONE, profileEditBacker.getDialog())
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
                () -> assertEquals(ProfileEditBacker.ProfileEditDialog.NONE, profileEditBacker.getDialog()),
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
                () -> assertEquals(ProfileEditBacker.ProfileEditDialog.NONE, profileEditBacker.getDialog())
        );
    }

    @Test
    public void testInitWithEdit() {
        user.setAdministrator(true);
        when(session.getUser()).thenReturn(user);
        when(map.containsKey(EDIT)).thenReturn(true);
        when(map.get(EDIT)).thenReturn(user.getUsername());
        when(profileService.getUserByUsername(user.getUsername())).thenReturn(user);
        profileEditBacker.init();
        assertAll(
                () -> assertEquals(user, profileEditBacker.getUser()),
                () -> assertEquals(user.getEmailAddress(), profileEditBacker.getEmailNew()),
                () -> assertEquals(user.getUsername(), profileEditBacker.getUsernameNew()),
                () -> assertEquals(ProfileEditBacker.ProfileEditDialog.NONE, profileEditBacker.getDialog())
        );
        verify(profileService).getAvatarForUser(anyInt());
    }

    @Test
    public void testInitWithEditNoAdmin() {
        when(session.getUser()).thenReturn(user);
        when(map.containsKey(EDIT)).thenReturn(true);
        profileEditBacker.init();
        verify(navHandler).handleNavigation(any(), any(), any());
    }

    @Test
    public void testInitWithEditInvalidId() {
        when(session.getUser()).thenReturn(user);
        when(map.containsKey(EDIT)).thenReturn(true);
        when(map.get(EDIT)).thenReturn("abc");
        profileEditBacker.init();
        assertAll(
                () -> assertNull(profileEditBacker.getUser()),
                () -> assertEquals(ProfileEditBacker.ProfileEditDialog.NONE, profileEditBacker.getDialog())
        );
        verify(navHandler).handleNavigation(any(), any(), any());
    }

    @Test
    public void testInitSessionUserNull() {
        profileEditBacker.init();
        verify(navHandler).handleNavigation(any(), any(), any());
    }

    @Test
    public void testInitGetUserNull() {
        when(session.getUser()).thenReturn(user);
        profileEditBacker.init();
        verify(navHandler).handleNavigation(any(), any(), any());
    }

    @Test
    public void testSaveChangesCreate() throws IllegalAccessException {
        createUser.setBoolean(profileEditBacker, true);
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        when(profileService.createUser(any())).thenReturn(true);
        profileEditBacker.setUser(user);
        profileEditBacker.setEmailNew(user.getEmailAddress());
        profileEditBacker.saveChanges();
        verify(profileService).matchingPassword(any(), any());
        verify(profileService).createUser(any());
    }

    @Test
    public void testSaveChangesCreateUnsuccessful() throws IllegalAccessException {
        createUser.setBoolean(profileEditBacker, true);
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        profileEditBacker.setUser(user);
        profileEditBacker.setEmailNew(user.getEmailAddress());
        profileEditBacker.saveChanges();
        verify(profileService).matchingPassword(any(), any());
        verify(profileService).createUser(any());
    }

    @Test
    public void testSaveChangesPasswordNotMatching() throws IllegalAccessException {
        createUser.setBoolean(profileEditBacker, true);
        when(profileService.matchingPassword(any(), any())).thenReturn(false);
        profileEditBacker.saveChanges();
        verify(profileService).matchingPassword(any(), any());
        verify(profileService, never()).createUser(any());
    }

    @Test
    public void testSaveChanges() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        when(profileService.updateUser(user)).thenReturn(true);
        profileEditBacker.setUser(user);
        profileEditBacker.setEmailNew(user.getEmailAddress());
        profileEditBacker.saveChanges();
        verify(profileService).matchingPassword(any(), any());
    }

    @Test
    public void testSaveChangesUnsuccessful() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        profileEditBacker.setUser(user);
        profileEditBacker.setEmailNew(user.getEmailAddress());
        profileEditBacker.saveChanges();
        verify(profileService).matchingPassword(any(), any());
    }

    @Test
    public void testSaveChangesNewEmail() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        StringBuffer buffer = new StringBuffer("http://test.de/hello_there.xhtml?someparam=69420");
        doReturn(buffer).when(request).getRequestURL();
        when(authenticationService.updateEmail(any(), any(), any())).thenReturn(true);
        when(profileService.updateUser(any())).thenReturn(true);
        profileEditBacker.setUser(user);
        profileEditBacker.setEmailNew(EMAIL);
        profileEditBacker.saveChanges();
        verify(profileService).matchingPassword(any(), any());
        verify(authenticationService).updateEmail(any(), any(), any());
        verify(profileService).updateUser(user);
    }

    @Test
    public void testSaveChangesNewEmailUnsuccessful() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        StringBuffer buffer = new StringBuffer("http://test.de/hello_there.xhtml?someparam=69420");
        doReturn(buffer).when(request).getRequestURL();
        profileEditBacker.setUser(user);
        profileEditBacker.setEmailNew(EMAIL);
        profileEditBacker.saveChanges();
        verify(profileService).matchingPassword(any(), any());
        verify(authenticationService).updateEmail(any(), any(), any());
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
    public void testSaveChangesNewPassword() {
        profileEditBacker.setPasswordNew(PASSWORD);
        profileEditBacker.setPasswordNewConfirm(PASSWORD);
        profileEditBacker.setUser(user);
        profileEditBacker.saveChanges();
        verify(authenticationService).hashPassword(user, PASSWORD);
    }

    @Test
    public void testSaveChangesNewPasswordNotMatching() {
        profileEditBacker.setPasswordNew(PASSWORD);
        profileEditBacker.setUser(user);
        profileEditBacker.saveChanges();
        verify(authenticationService, never()).hashPassword(user, PASSWORD);
        verify(profileService, never()).matchingPassword(any(), any());
    }

    @Test
    public void testSaveChangesIOException() throws IOException {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        when(profileService.updateUser(user)).thenReturn(true);
        doThrow(IOException.class).when(ext).redirect(any());
        profileEditBacker.setUser(user);
        profileEditBacker.setEmailNew(user.getEmailAddress());
        assertNull(profileEditBacker.saveChanges());
    }

    @Test
    public void testDelete() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        when(profileService.deleteUser(user)).thenReturn(true);
        profileEditBacker.setUser(user);
        assertEquals("pretty:home", profileEditBacker.delete());
        verify(profileService).matchingPassword(any(), any());
        verify(profileService).deleteUser(user);
    }

    @Test
    public void testDeleteFails() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        profileEditBacker.setUser(user);
        profileEditBacker.delete();
        verify(profileService).matchingPassword(any(), any());
        verify(profileService).deleteUser(user);
    }

    @Test
    public void testDeleteWrongPassword() {
        profileEditBacker.setUser(user);
        profileEditBacker.delete();
        verify(profileService).matchingPassword(any(), any());
    }

    @Test
    public void testDeleteEqualSessionUser() {
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        when(profileService.deleteUser(user)).thenReturn(true);

        when(session.getUser()).thenReturn(user);
        profileEditBacker.setUser(user);
        assertEquals("pretty:home", profileEditBacker.delete());
        verify(profileService).matchingPassword(any(), any());
        verify(profileService).deleteUser(user);
        verify(session).invalidateSession();
    }

    @Test
    public void testUploadAvatar() {
        byte[] avatar = new byte[]{1, 2, 3, 4};
        byte[] thumbnail = new byte[]{1, 2, 3, 4};
        when(profileService.uploadAvatar(any())).thenReturn(avatar);
        when(profileService.generateThumbnail(any())).thenReturn(thumbnail);
        profileEditBacker.setUser(user);
        assertAll(
                () -> assertTrue(profileEditBacker.uploadAvatar()),
                () -> assertEquals(avatar, user.getAvatar()),
                () -> assertEquals(thumbnail, user.getAvatarThumbnail())
        );
        verify(profileService).uploadAvatar(any());
        verify(profileService).generateThumbnail(any());
    }

    @Test
    public void testUploadAvatarWhenDelete() {
        profileEditBacker.setDeleteAvatar(true);
        profileEditBacker.setUser(user);
        assertAll(
                () -> assertTrue(profileEditBacker.uploadAvatar()),
                () -> assertArrayEquals(new byte[0], user.getAvatar()),
                () -> assertArrayEquals(new byte[0], user.getAvatarThumbnail())
        );
        verify(profileService, never()).generateThumbnail(any());
    }

    @Test
    public void testUploadAvatarGenerateThumbnailFails() {
        byte[] oldAvatar = user.getAvatar();
        byte[] oldThumbnail = user.getAvatarThumbnail();
        byte[] avatar = new byte[]{1, 2, 3, 4};
        when(profileService.uploadAvatar(any())).thenReturn(avatar);
        profileEditBacker.setUser(user);
        assertAll(
                () -> assertFalse(profileEditBacker.uploadAvatar()),
                () -> assertEquals(oldAvatar, user.getAvatar()),
                () -> assertEquals(oldThumbnail, user.getAvatarThumbnail())
        );
        verify(profileService).uploadAvatar(any());
        verify(profileService).generateThumbnail(any());
    }

    @Test
    public void testUploadAvatarFails() {
        byte[] oldAvatar = user.getAvatar();
        byte[] oldThumbnail = user.getAvatarThumbnail();
        profileEditBacker.setUser(user);
        assertAll(
                () -> assertFalse(profileEditBacker.uploadAvatar()),
                () -> assertEquals(oldAvatar, user.getAvatar()),
                () -> assertEquals(oldThumbnail, user.getAvatarThumbnail())
        );
        verify(profileService).uploadAvatar(any());
    }

    @Test
    public void testOpenDeleteDialog() {
        profileEditBacker.setDialog(ProfileEditBacker.ProfileEditDialog.NONE);
        profileEditBacker.openDeleteDialog();
        assertEquals(ProfileEditBacker.ProfileEditDialog.DELETE, profileEditBacker.getDialog());
    }

    @Test
    public void testOpenChangeDialog() {
        profileEditBacker.setUploadedAvatar(null);
        profileEditBacker.setDeleteAvatar(false);
        profileEditBacker.setDialog(ProfileEditBacker.ProfileEditDialog.NONE);
        profileEditBacker.openChangeDialog();
        assertEquals(ProfileEditBacker.ProfileEditDialog.UPDATE, profileEditBacker.getDialog());
    }

    @Test
    public void testOpenChangeDialogUpload() {
        ProfileEditBacker spyBacker = spy(profileEditBacker);
        spyBacker.setUploadedAvatar(mock(Part.class));
        spyBacker.setDeleteAvatar(true);
        doReturn(true).when(spyBacker).uploadAvatar();
        spyBacker.setDialog(ProfileEditBacker.ProfileEditDialog.NONE);
        spyBacker.openChangeDialog();
        assertEquals(ProfileEditBacker.ProfileEditDialog.UPDATE, spyBacker.getDialog());
    }

    @Test
    public void testOpenChangeDialogWhenUploadFailed() {
        ProfileEditBacker spyBacker = spy(profileEditBacker);
        spyBacker.setUploadedAvatar(null);
        spyBacker.setDeleteAvatar(true);
        doReturn(false).when(spyBacker).uploadAvatar();
        spyBacker.setDialog(ProfileEditBacker.ProfileEditDialog.NONE);
        spyBacker.openChangeDialog();
        assertEquals(ProfileEditBacker.ProfileEditDialog.NONE, spyBacker.getDialog());
    }

    @Test
    public void testOpenChangeDialogWhenUploadSuccessful() {
        ProfileEditBacker spyBacker = spy(profileEditBacker);
        spyBacker.setUploadedAvatar(null);
        spyBacker.setDeleteAvatar(true);
        doReturn(true).when(spyBacker).uploadAvatar();
        spyBacker.setDialog(ProfileEditBacker.ProfileEditDialog.NONE);
        spyBacker.openChangeDialog();
        assertEquals(ProfileEditBacker.ProfileEditDialog.UPDATE, spyBacker.getDialog());
    }

    @Test
    public void testOpenPreviewDialog() {
        profileEditBacker.setUser(user);
        profileEditBacker.openPreviewDialog();
        assertAll(
                () -> assertEquals(ProfileEditBacker.ProfileEditDialog.PREVIEW, profileEditBacker.getDialog()),
                () -> assertNotNull(profileEditBacker.getSanitizedBio())
        );
    }

    @Test
    public void testOpenPreviewDialogBioNull() {
        user.setBiography(null);
        profileEditBacker.setUser(user);
        profileEditBacker.openPreviewDialog();
        assertAll(
                () -> assertEquals(ProfileEditBacker.ProfileEditDialog.PREVIEW, profileEditBacker.getDialog()),
                () -> assertNull(profileEditBacker.getSanitizedBio())
        );
    }

    @Test
    public void testOpenPreviewDialogUpload() {
        ProfileEditBacker spyBacker = spy(profileEditBacker);
        spyBacker.setUser(user);
        spyBacker.setUploadedAvatar(mock(Part.class));
        doReturn(true).when(spyBacker).uploadAvatar();
        spyBacker.openPreviewDialog();
        assertAll(
                () -> assertEquals(ProfileEditBacker.ProfileEditDialog.PREVIEW, spyBacker.getDialog()),
                () -> assertNotNull(spyBacker.getSanitizedBio()),
                () -> assertNotNull(spyBacker.getUploadedAvatar())
        );
    }

    @Test
    public void testOpenPreviewDialogDeleteAvatar() {
        ProfileEditBacker spyBacker = spy(profileEditBacker);
        spyBacker.setUser(user);
        spyBacker.setUploadedAvatar(null);
        spyBacker.setDeleteAvatar(true);
        doReturn(true).when(spyBacker).uploadAvatar();
        spyBacker.openPreviewDialog();
        assertEquals(ProfileEditBacker.ProfileEditDialog.PREVIEW, spyBacker.getDialog());
    }

    @Test
    public void testOpenPreviewDialogWhenUploadFailed() {
        ProfileEditBacker spyBacker = spy(profileEditBacker);
        spyBacker.setUploadedAvatar(null);
        spyBacker.setDeleteAvatar(true);
        doReturn(false).when(spyBacker).uploadAvatar();
        spyBacker.setDialog(ProfileEditBacker.ProfileEditDialog.NONE);
        spyBacker.openPreviewDialog();
        assertEquals(ProfileEditBacker.ProfileEditDialog.NONE, spyBacker.getDialog());
    }

    @Test
    public void testCloseDialog() {
        profileEditBacker.setDialog(ProfileEditBacker.ProfileEditDialog.UPDATE);
        profileEditBacker.closeDialog();
        assertEquals(ProfileEditBacker.ProfileEditDialog.NONE, profileEditBacker.getDialog());
    }

    @Test
    public void testSettersForCoverage() {
        profileEditBacker.setPassword(user.getPasswordHash());
        profileEditBacker.setSanitizedBio(user.getBiography());
        profileEditBacker.setCreate(true);
        assertAll(
                () -> assertEquals(user.getPasswordHash(), profileEditBacker.getPassword()),
                () -> assertEquals(user.getBiography(), profileEditBacker.getSanitizedBio()),
                () -> assertTrue(profileEditBacker.isCreate())
        );
    }

}
