package tech.bugger.persistence.util;

public interface ExceptionlessAutoCloseable extends AutoCloseable {
    @Override
    public void close();
}
