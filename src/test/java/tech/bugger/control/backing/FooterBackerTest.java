package tech.bugger.control.backing;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.util.MarkdownHandler;
import tech.bugger.business.util.Registry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class FooterBackerTest {

    private FooterBacker backer;

    @Mock
    private UserSession session;

    @Mock
    private Registry registry;

    @BeforeEach
    public void setUp() {
        doReturn(ResourceBundleMocker.mock("help")).when(registry)
                .getBundle(anyString(), any());
        doReturn(Locale.GERMAN).when(session).getLocale();
        backer = new FooterBacker(session, registry);
    }

    @Test
    public void testToggleHelpToFalse() {
        backer.setHelpDisplayed(true);
        String site = backer.toggleHelp();
        assertAll(() -> assertNull(site),
                () -> assertFalse(backer.isHelpDisplayed()));
    }

    @Test
    public void testToggleHelpToTrue() {
        backer.setHelpDisplayed(false);
        String site = backer.toggleHelp();
        assertAll(() -> assertNull(site),
                () -> assertTrue(backer.isHelpDisplayed()));
    }

    @Test
    public void testGetHelp() {
        try (MockedStatic<MarkdownHandler> handlerMock = mockStatic(MarkdownHandler.class)) {
            String html = "nananananana batman";
            handlerMock.when(() -> MarkdownHandler.toHtml(any())).thenReturn(html);
            assertEquals(html, backer.getHelp("helpKey"));
            handlerMock.verify(() -> MarkdownHandler.toHtml(eq("help\n\nhelp")));
        }
    }

}
