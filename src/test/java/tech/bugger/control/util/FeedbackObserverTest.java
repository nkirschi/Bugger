package tech.bugger.control.util;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.business.util.Feedback;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedbackObserverTest {

    private FeedbackObserver feedbackObserver;

    @Mock
    private FacesContext fctx;

    @BeforeEach
    public void setUp() {
        feedbackObserver = new FeedbackObserver(fctx);
    }

    @Test
    public void testDisplayFeedbackError() {
        Feedback feedback = new Feedback("message", Feedback.Type.ERROR);
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        feedbackObserver.displayFeedback(feedback);
        verify(fctx).addMessage(eq(null), captor.capture());
        assertAll(
                () -> assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity()),
                () -> assertEquals("message", captor.getValue().getSummary())
        );
    }

    @Test
    public void testDisplayFeedbackWarning() {
        Feedback feedback = new Feedback("message", Feedback.Type.WARNING);
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        feedbackObserver.displayFeedback(feedback);
        verify(fctx).addMessage(eq(null), captor.capture());
        assertAll(
                () -> assertEquals(FacesMessage.SEVERITY_WARN, captor.getValue().getSeverity()),
                () -> assertEquals("message", captor.getValue().getSummary())
        );
    }

    @Test
    public void testDisplayFeedbackInfo() {
        Feedback feedback = new Feedback("message", Feedback.Type.INFO);
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        feedbackObserver.displayFeedback(feedback);
        verify(fctx).addMessage(eq(null), captor.capture());
        assertAll(
                () -> assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity()),
                () -> assertEquals("message", captor.getValue().getSummary())
        );
    }

    /* throws java.lang.IncompatibleClassChangeError :(
    @Test
    public void testDisplayFeedbackUnknown() {
        Feedback.Type unknownValue = mock(Feedback.Type.class);
        when(unknownValue.ordinal()).thenReturn(Feedback.Type.values().length);

        try (MockedStatic<Feedback.Type> typeMockedStatic = mockStatic(Feedback.Type.class)) {
            typeMockedStatic.when(Feedback.Type::values).thenReturn(new Feedback.Type[]{Feedback.Type.INFO,
                    Feedback.Type.WARNING, Feedback.Type.ERROR, unknownValue});
        }

        Feedback feedback = new Feedback("message", unknownValue);
        assertThrows(IllegalArgumentException.class, () -> feedbackObserver.displayFeedback(feedback));
    }
    */
}