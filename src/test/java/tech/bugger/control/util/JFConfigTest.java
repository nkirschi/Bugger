package tech.bugger.control.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.annotation.FacesConfig;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class JFConfigTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private ExternalContext ectx;

    @BeforeEach
    public void setUp() {
        lenient().doReturn(request).when(ectx).getRequest();
    }

    @Test
    public void testHasAnnotations() {
        Set<Class<? extends Annotation>> annotations = Arrays.stream(JFConfig.class.getAnnotations())
                .map(Annotation::annotationType).collect(Collectors.toSet());
        assertEquals(Set.of(FacesConfig.class, ApplicationScoped.class), annotations);
    }

    @Test
    public void testConstructorAccess() throws NoSuchMethodException {
        Constructor<JFConfig> constructor = JFConfig.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Throwable e = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
    }

    @Test
    public void testGetApplicationPathDefault() {
        FacesContext fctx = mock(FacesContext.class);
        doReturn(ectx).when(fctx).getExternalContext();
        try (MockedStatic<FacesContext> fctxStatic = mockStatic(FacesContext.class)) {
            fctxStatic.when(FacesContext::getCurrentInstance).thenReturn(fctx);
            doReturn("").when(ectx).getApplicationContextPath();
            doReturn(new StringBuffer("https://bugger.tech:8080")).when(request).getRequestURL();
            assertEquals("https://bugger.tech:8080", JFConfig.getApplicationPath());
        }
    }

    @Test
    public void testGetApplicationPath() {
        doReturn("").when(ectx).getApplicationContextPath();
        doReturn(new StringBuffer("https://bugger.tech:8080")).when(request).getRequestURL();
        assertEquals("https://bugger.tech:8080", JFConfig.getApplicationPath(ectx));
    }

    @Test
    public void testGetApplicationPathFail() {
        doReturn(new StringBuffer("thisshouldneverhappen")).when(request).getRequestURL();
        assertThrows(InternalError.class, () -> JFConfig.getApplicationPath(ectx));
    }

    @Test
    public void testGetApplicationPathWithCustomPath() {
        doReturn("/bugger").when(ectx).getApplicationContextPath();
        doReturn(new StringBuffer("https://bugger.tech:8080")).when(request).getRequestURL();
        assertEquals("https://bugger.tech:8080/bugger", JFConfig.getApplicationPath(ectx));
    }

}