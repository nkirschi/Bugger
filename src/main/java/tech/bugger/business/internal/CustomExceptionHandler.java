package tech.bugger.business.internal;

import java.io.IOException;
import java.util.HashMap;
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
import org.jboss.weld.exceptions.WeldException;
import tech.bugger.control.exception.Error404Exception;

/**
 * Enables customized handling of exceptions.
 */
public class CustomExceptionHandler extends ExceptionHandlerWrapper {

    /**
     * Constructs a new {@link CustomExceptionHandler} wrapping an {@link ExceptionHandler}.
     *
     * @param wrapped The {@link ExceptionHandler} being wrapped.
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
     * Handles all exceptions in the given {@link FacesContext}.
     *
     * @param fctx The current {@link FacesContext}.
     */
    protected void handleException(final FacesContext fctx) {
        Iterator<ExceptionQueuedEvent> unhandledEvents = getUnhandledExceptionQueuedEvents().iterator();

        if (fctx == null
                || fctx.getExternalContext().isResponseCommitted()
                || !unhandledEvents.hasNext()) {
            return;
        }

        Throwable exception = unhandledEvents.next().getContext().getException();

        while (exception.getCause() != null
                && (exception instanceof FacesException || exception instanceof ELException
                || exception instanceof WeldException)) {
            exception = exception.getCause();
        }

        ExternalContext ectx = fctx.getExternalContext();
        String uri = ectx.getRequestContextPath() + ectx.getRequestServletPath();
        Map<String, Object> requestScope = ectx.getRequestMap();
        requestScope.put(RequestDispatcher.ERROR_REQUEST_URI, uri);
        requestScope.put(RequestDispatcher.ERROR_EXCEPTION, exception);
        if (exception instanceof Error404Exception
                || (exception.getCause() != null && exception.getCause() instanceof Error404Exception)) {
            requestScope.put("Show404", "yes");
        } else {
            requestScope.put("Show404", "no");
        }
        String viewID = "/WEB-INF/errorpages/error.xhtml";
        Application application = fctx.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot viewRoot = viewHandler.createView(fctx, viewID);
        fctx.setViewRoot(viewRoot);

        try {
            // Backup headers
            HttpServletResponse response = (HttpServletResponse) ectx.getResponse();
            Map<String, String> headers = new HashMap<>();
            for (String header : response.getHeaderNames()) {
                headers.put(header, response.getHeader(header));
            }

            // Reset the response
            ectx.responseReset();

            // Add back old headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                ectx.addResponseHeader(header.getKey(), header.getValue());
            }

            // Overwrite rest of data
            if (!fctx.getPartialViewContext().isAjaxRequest()) {
                ectx.setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            ViewDeclarationLanguage viewDeclarationLanguage = viewHandler.getViewDeclarationLanguage(fctx, viewID);
            viewDeclarationLanguage.buildView(fctx, viewRoot);
            fctx.getPartialViewContext().setRenderAll(true);
            viewDeclarationLanguage.renderView(fctx, viewRoot);
            fctx.responseComplete();
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
         * Constructs a new {@link CustomExceptionHandler} factory wrapping an {@link ExceptionHandlerFactory}.
         *
         * @param wrapped The {@link ExceptionHandlerFactory} to wrap.
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
