package tech.bugger.business.internal;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.*;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.view.ViewDeclarationLanguage;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class ExceptHand extends ExceptionHandlerWrapper {

    public ExceptHand(ExceptionHandler wrapped) {
        super(wrapped);
    }

    @Override
    public void handle() {
        handleException(FacesContext.getCurrentInstance());
        getWrapped().handle();
    }

    protected void handleException(FacesContext context) {
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

        String viewID = "/WEB-INF/errorpages/500.xhtml"; // TODO replace with proper String
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

    public static class Factory extends ExceptionHandlerFactory {

        public Factory(ExceptionHandlerFactory wrapped) {
            super(wrapped);
        }

        @Override
        public ExceptionHandler getExceptionHandler() {
            return new ExceptHand(getWrapped().getExceptionHandler());
        }
    }

}
