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
public class ImprintBackerTest {

    private ImprintBacker backer;

    @Mock
    private ApplicationSettings settings;

    @Mock
    private Organization organization;

    @BeforeEach
    public void setUp() {
        doReturn(organization).when(settings).getOrganization();
        backer = new ImprintBacker(settings);
    }

    @Test
    public void testGetImprintNull() {
        doReturn(null).when(organization).getImprint();
        assertEquals("", backer.getImprint());
    }

    @Test
    public void testGetImprintSuccess() {
        String key = "# Imprint";
        doReturn(key).when(organization).getImprint();
        try (MockedStatic<MarkdownHandler> handlerMock = mockStatic(MarkdownHandler.class)) {
            String parsed = "Parsed string";
            handlerMock.when(() -> MarkdownHandler.toHtml(key)).thenReturn(parsed);
            assertEquals(parsed, backer.getImprint());
            handlerMock.verify(() -> MarkdownHandler.toHtml(key));
        }
    }

}
