package tech.bugger.business.internal;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.view.ViewDeclarationLanguage;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletResponse;
import tech.bugger.control.exception.Error404Exception;

/**
 * Enables customized handling of exceptions.
 */
public class CustomExceptionHandler extends ExceptionHandlerWrapper {

    /**
     * Constructs a new {@link CustomExceptionHandler} wrapping an {@code ExceptionHandler}.
     *
     * @param wrapped The exceptionHandler being wrapped.
     */
    public CustomExceptionHandler(final ExceptionHandler wrapped) {
        super(wrapped);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle() {
        handleException(FacesContext.getCurrentInstance());
        getWrapped().handle();
    }

    /**
     * Handles exceptions.
     *
     * @param context The {@link FacesContext}.
     */
    protected void handleException(final FacesContext context) {
        Iterator<ExceptionQueuedEvent> unhandledEvents = getUnhandledExceptionQueuedEvents().iterator();

        if (context == null
                || context.getExternalContext().isResponseCommitted()
                || !unhandledEvents.hasNext()) {
            return;
        }

        Throwable exception = unhandledEvents.next().getContext().getException();

        while (exception.getCause() != null
                && (exception instanceof FacesException || exception instanceof ELException)) {
            exception = exception.getCause();
        }

        ExternalContext external = context.getExternalContext();
        String uri = external.getRequestContextPath() + external.getRequestServletPath();
        Map<String, Object> requestScope = external.getRequestMap();
        requestScope.put(RequestDispatcher.ERROR_REQUEST_URI, uri);
        requestScope.put(RequestDispatcher.ERROR_EXCEPTION, exception);

        String viewID;
        if (exception instanceof Error404Exception) {
            viewID = "/WEB-INF/errorpages/404.xhtml";
        } else {
            viewID = "/WEB-INF/errorpages/500.xhtml";
        }
        Application application = context.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot viewRoot = viewHandler.createView(context, viewID);
        context.setViewRoot(viewRoot);

        try {
            external.responseReset();
            if (!context.getPartialViewContext().isAjaxRequest()) {
                external.setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            ViewDeclarationLanguage viewDeclarationLanguage = viewHandler.getViewDeclarationLanguage(context, viewID);
            viewDeclarationLanguage.buildView(context, viewRoot);
            context.getPartialViewContext().setRenderAll(true);
            viewDeclarationLanguage.renderView(context, viewRoot);
            context.responseComplete();
        } catch (IOException e) {
            throw new FacesException(e);
        } finally {
            requestScope.remove(RequestDispatcher.ERROR_EXCEPTION);
        }

        unhandledEvents.remove();

        while (unhandledEvents.hasNext()) {
            unhandledEvents.next();
            unhandledEvents.remove();
        }
    }

    /**
     * Factory producing custom exception handlers.
     */
    public static class Factory extends ExceptionHandlerFactory {

        /**
         * Constructs a new custom exception handler factory wrapping an {@link ExceptionHandlerFactory}.
         *
         * @param wrapped The exceptionHandlerFactory to wrap.
         */
        public Factory(final ExceptionHandlerFactory wrapped) {
            super(wrapped);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ExceptionHandler getExceptionHandler() {
            return new CustomExceptionHandler(getWrapped().getExceptionHandler());
        }
    }

}
