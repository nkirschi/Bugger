package tech.bugger.global.util;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Wrapper around an arbitrary object for lazy loading.
 *
 * @param <T> The type of the wrapped object.
 */
public class Lazy<T> implements Serializable {
    private static final long serialVersionUID = -7848504462864860011L;

    /**
     * The object being wrapped.
     */
    private volatile T value;

    /**
     * The instructions for acquiring the wrapped object.
     */
    private Supplier<T> retrieval;

    /**
     * Constructs a new lazy wrapper with a specified retrieval method.
     *
     * @param retrieval The instructions for acquiring the wrapped object.
     */
    public Lazy(Supplier<T> retrieval) {
        this.retrieval = retrieval;
    }

    /**
     * Constructs a new lazy wrapper by eagerly initializing the wrapped object.
     *
     * @param value The object to be immediately wrapped.
     */
    public Lazy(T value) {
        this.value = value;
    }

    /**
     * Returns the wrapped object after loading it if necessary.
     *
     * @return The wrapped object itself.
     */
    public synchronized T get() {
        if (!isPresent()) {
            value = retrieval.get();
        }
        return value;
    }

    /**
     * Checks whether the wrapped object has already been loaded.
     *
     * @return Whether the wrapped object is present.
     */
    public boolean isPresent() {
        return value != null;
    }
}
