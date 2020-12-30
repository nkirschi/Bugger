package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ProfileService;
import tech.bugger.global.transfer.User;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(LogExtension.class)
public class ProfileBackerTest {

    @InjectMocks
    private ProfileBacker profileBacker;

    @Mock
    private ProfileService profileService;

    @Mock
    private UserSession session;

    private User user;
    private int theAnswer = 42;

    @BeforeEach
    public void setup()
    {
        user = new User();
        user.setId(12345);
        user.setUsername("Helgi");
        user.setPasswordHash("v3ry_s3cur3");
        user.setEmailAddress("helga@web.de");
        user.setFirstName("Helga");
        user.setLastName("Brötchen");
        user.setBiography("Hallo, ich bin die Helgi | Perfect | He/They/Her | vergeben | Abo =|= endorsement)");
        user.setProfileVisibility(User.ProfileVisibility.MINIMAL);
        user.setRegistrationDate(ZonedDateTime.now());
        user.setAdministrator(false);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testOpenPromoteDemoteAdminDialog() {
        assertAll("Should return null and set the boolean to true!",
                () -> assertNull(profileBacker.openPromoteDemoteAdminDialog()),
                () -> assertTrue(profileBacker.isDisplayPromoteDemoteAdminDialog())
        );
    }

    @Test
    public void testOpenPromoteDemoteAdminDialogClosesOtherDialogs() {
        profileBacker.openPromoteDemoteAdminDialog();
        assertAll("All other dialogs should be closed!",
                () -> assertFalse(profileBacker.isDisplayDeleteAllTopicSubscriptionsDialog()),
                () -> assertFalse(profileBacker.isDisplayDeleteAllReportSubscriptionsDialog()),
                () -> assertFalse(profileBacker.isDisplayDeleteAllUserSubscriptionsDialog())
        );
    }

    @Test
    public void testClosePromoteDemoteAdminDialog() {
        assertAll("Should return null and set the boolean to false",
                () -> assertNull(profileBacker.closePromoteDemoteAdminDialog()),
                () -> assertFalse(profileBacker.isDisplayPromoteDemoteAdminDialog())
        );
    }

    @Test
    public void testGetVotingWeight() {
        profileBacker.setUser(user);
        when(profileService.getVotingWeightForUser(user)).thenReturn(theAnswer);
        int votingWeight = profileBacker.getVotingWeight();
        assertEquals(theAnswer, votingWeight);
        verify(profileService, times(1)).getVotingWeightForUser(user);
    }

    @Test
    public void testGetNumberOfPosts() {
        profileBacker.setUser(user);
        when(profileService.getNumberOfPostsForUser(user)).thenReturn(theAnswer);
        int posts = profileBacker.getNumberOfPosts();
        assertEquals(theAnswer, posts);
        verify(profileService, times(1)).getNumberOfPostsForUser(user);
    }

    @Test
    public void testIsPrivilegedEqualUser() {
        profileBacker.setUser(user);
        when(session.getUser()).thenReturn(profileBacker.getUser());
        assertTrue(profileBacker.isPrivileged());
        verify(session, times(2)).getUser();
    }

    @Test
    public void testIsPrivilegedAdmin() {
        user.setAdministrator(true);
        when(session.getUser()).thenReturn(user);
        assertTrue(profileBacker.isPrivileged());
        verify(session, times(1)).getUser();
    }

    @Test
    public void testIsPrivilegedFalse() {
        profileBacker.setUser(user);
        User owner = new User();
        owner.setId(45678);
        owner.setUsername(user.getUsername());
        owner.setPasswordHash(user.getPasswordHash());
        owner.setEmailAddress(user.getEmailAddress());
        owner.setFirstName(user.getFirstName());
        owner.setLastName(user.getLastName());
        owner.setBiography(user.getBiography());
        owner.setProfileVisibility(user.getProfileVisibility());
        owner.setRegistrationDate(user.getRegistrationDate());
        owner.setAdministrator(user.isAdministrator());
        when(session.getUser()).thenReturn(owner);
        assertFalse(profileBacker.isPrivileged());
        verify(session, times(2)).getUser();
    }

    @Test
    public void testToggleAdmin() {
        profileBacker.setUser(user);
        profileBacker.toggleAdmin();
        verify(profileService, times(1)).toggleAdmin(user);
    }
}
