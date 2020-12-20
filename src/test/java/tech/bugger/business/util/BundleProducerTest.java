package tech.bugger.business.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.bugger.business.internal.UserSession;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BundleProducerTest {
    private BundleProducer bundleProducer;

    @BeforeEach
    public void init() {
        UserSession userSession = mock(UserSession.class);
        when(userSession.getLocale()).thenReturn(Locale.GERMAN);
        bundleProducer = new BundleProducer(userSession);
    }

    @Test
    public void testGetHelpLocale() {
        assertEquals(Locale.GERMAN, bundleProducer.getHelp().getLocale());
    }

    @Test
    public void testGetHelpBundleName() {
        assertEquals("tech.bugger.i18n.help", bundleProducer.getHelp().getBaseBundleName());
    }

    @Test
    public void testGetLabelsLocale() {
        assertEquals(Locale.GERMAN, bundleProducer.getLabels().getLocale());
    }

    @Test
    public void testGetLabelsBundleName() {
        assertEquals("tech.bugger.i18n.labels", bundleProducer.getLabels().getBaseBundleName());
    }

    @Test
    public void testGetMessagesLocale() {
        assertEquals(Locale.GERMAN, bundleProducer.getMessages().getLocale());
    }

    @Test
    public void testGetMessagesBundleName() {
        assertEquals("tech.bugger.i18n.messages", bundleProducer.getMessages().getBaseBundleName());
    }
}