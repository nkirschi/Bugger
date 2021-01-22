package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.MarkdownHandler;
import tech.bugger.global.transfer.Organization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class PrivacyBackerTest {

    private PrivacyBacker backer;

    @Mock
    private ApplicationSettings settings;

    @Mock
    private Organization organization;

    @BeforeEach
    public void setUp() {
        doReturn(organization).when(settings).getOrganization();
        backer = new PrivacyBacker(settings);
    }

    @Test
    public void testGetPrivacyPolicyNull() {
        doReturn(null).when(organization).getPrivacyPolicy();
        assertEquals("", backer.getPrivacyPolicy());
    }

    @Test
    public void testGetPrivacyPolicySuccess() {
        String key = "# Privacy";
        doReturn(key).when(organization).getPrivacyPolicy();
        try (MockedStatic<MarkdownHandler> handlerMock = mockStatic(MarkdownHandler.class)) {
            String parsed = "Parsed string";
            handlerMock.when(() -> MarkdownHandler.toHtml(key)).thenReturn(parsed);
            assertEquals(parsed, backer.getPrivacyPolicy());
            handlerMock.verify(() -> MarkdownHandler.toHtml(key));
        }
    }

}
