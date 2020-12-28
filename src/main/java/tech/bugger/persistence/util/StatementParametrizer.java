package tech.bugger.persistence.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementParametrizer {

    private final PreparedStatement stmt;
    private int counter;

    public StatementParametrizer(final PreparedStatement stmt) {
        this.stmt = stmt;
        this.counter = 1; // JDBC starts at 1 :/
    }

    public PreparedStatement toStatement() {
        return stmt;
    }

    public StatementParametrizer integer(final int integer) throws SQLException {
        stmt.setInt(counter++, integer);
        return this;
    }

    public StatementParametrizer bool(final boolean bool) throws SQLException {
        stmt.setBoolean(counter++, bool);
        return this;
    }

    public StatementParametrizer bytes(final byte[] bytes) throws SQLException {
        stmt.setBytes(counter++, bytes);
        return this;
    }

    public StatementParametrizer string(final String string) throws SQLException {
        stmt.setString(counter++, string);
        return this;
    }

    public StatementParametrizer object(final Object object) throws SQLException {
        stmt.setObject(counter++, object);
        return this;
    }

}
