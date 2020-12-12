package tech.bugger.business.internal;

import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.ExceptionHandlerWrapper;

/**
 * Factory producing custom exception handlers.
 */
public class CustomExceptionHandlerFactory extends ExceptionHandlerFactory {

    /**
     * Enables customized handling of exceptions.
     */
    private class CustomExceptionHandler extends ExceptionHandlerWrapper {

        private ExceptionHandler exceptionHandler;

        /**
         * Constructs a new {@code CustomExceptionHandler} wrapping an {@code ExceptionHandler}.
         *
         * @param exceptionHandler The exceptionHandler being wrapped.
         */
        public CustomExceptionHandler(ExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
        }

        /**
         * Gets the wrapped {@code ExceptionHandler}.
         *
         * @return The wrapped exceptionHandler.
         */
        @Override
        public ExceptionHandler getWrapped() {
            return exceptionHandler;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handle() throws FacesException {

        }
    }

    private ExceptionHandlerFactory exceptionHandlerFactory;

    /**
     * Constructs a new {@code CustomExceptionHandlerFactory}.
     */
    public CustomExceptionHandlerFactory() {
    }

    /**
     * Constructs a new {@code CustomExceptionHandlerFactory} wrapping an {@code ExceptionHandlerFactory}.
     *
     * @param exceptionHandlerFactory The exceptionHandlerFactory to wrap.
     */
    public CustomExceptionHandlerFactory(ExceptionHandlerFactory exceptionHandlerFactory) {
        this.exceptionHandlerFactory = exceptionHandlerFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExceptionHandler getExceptionHandler() {
        return new CustomExceptionHandler(exceptionHandlerFactory.getExceptionHandler());
    }
}
