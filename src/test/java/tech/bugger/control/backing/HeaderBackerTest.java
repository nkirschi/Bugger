package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.global.transfer.User;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class HeaderBackerTest {

    @InjectMocks
    private HeaderBacker headerBacker;

    @Mock
    private UserSession session;

    @Mock
    private FacesContext fctx;

    @Mock
    private ExternalContext ectx;

    private User user;

    @BeforeEach
    public void setup() throws NoSuchFieldException {
        user = new User();
        lenient().doReturn(user).when(session).getUser();
        lenient().doReturn(ectx).when(fctx).getExternalContext();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInit() throws IllegalAccessException, NoSuchFieldException {
        headerBacker.init();
        assertAll(
                () -> assertEquals(user, headerBacker.getUser())
        );
    }

}
