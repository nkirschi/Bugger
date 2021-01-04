package tech.bugger.persistence.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Builder for conveniently parametrizing {@link PreparedStatement}s.
 *
 * This is done by incrementing a counter for every parameter substitution.
 */
public class StatementParametrizer {

    /**
     * Prepared statement to be successively parametrized.
     */
    private final PreparedStatement stmt;

    /**
     * Counter keeping track of the current parameter in {@code stmt}.
     */
    private int counter;

    /**
     * Construct a new statement parametrizer.
     *
     * @param stmt The prepared statement to be parametrized.
     */
    public StatementParametrizer(final PreparedStatement stmt) {
        this.stmt = stmt;
        this.counter = 1; // JDBC starts at 1 :/
    }

    /**
     * Yields the current parametrized statement.
     *
     * @return The prepared statement with all parameters up to now applied.
     */
    public PreparedStatement toStatement() {
        return stmt;
    }

    /**
     * Substitutes the next parameter in the statement with an integer.
     *
     * @param integer The integer to set as parameter.
     * @return {@code this} builder for further use.
     * @throws SQLException if substituting {@code integer} for the next parameter is not possible.
     * @see PreparedStatement#setInt(int, int)
     */
    public StatementParametrizer integer(final Integer integer) throws SQLException {
        if (integer == null) {
            stmt.setNull(counter++, Types.INTEGER);
        } else {
            stmt.setInt(counter++, integer);
        }
        return this;
    }

    /**
     * Substitutes the next parameter in the statement with a boolean.
     *
     * @param bool The boolean to set as parameter.
     * @return {@code this} builder for further use.
     * @throws SQLException if substituting {@code bool} for the next parameter is not possible.
     * @see PreparedStatement#setBoolean(int, boolean)
     */
    public StatementParametrizer bool(final Boolean bool) throws SQLException {
        if (bool == null) {
            stmt.setNull(counter++, Types.BOOLEAN);
        } else {
            stmt.setBoolean(counter++, bool);
        }
        return this;
    }

    /**
     * Substitutes the next parameter in the statement with a byte array.
     *
     * @param bytes The byte array to set as parameter.
     * @return {@code this} builder for further use.
     * @throws SQLException if substituting {@code bytes} for the next parameter is not possible.
     * @see PreparedStatement#setBytes(int, byte[])
     */
    public StatementParametrizer bytes(final byte[] bytes) throws SQLException {
        stmt.setBytes(counter++, bytes);
        return this;
    }

    /**
     * Substitutes the next parameter in the statement with a string.
     *
     * @param string The string to set as parameter.
     * @return {@code this} builder for further use.
     * @throws SQLException if substituting {@code string} for the next parameter is not possible.
     * @see PreparedStatement#setString(int, String)
     */
    public StatementParametrizer string(final String string) throws SQLException {
        stmt.setString(counter++, string);
        return this;
    }

    /**
     * Substitutes the next parameter in the statement with a general object.
     *
     * @param object The object to set as parameter.
     * @return {@code this} builder for further use.
     * @throws SQLException if substituting {@code object} for the next parameter is not possible.
     * @see PreparedStatement#setObject(int, Object)
     */
    public StatementParametrizer object(final Object object) throws SQLException {
        stmt.setObject(counter++, object);
        return this;
    }

    /**
     * Substitutes the next parameter in the statement with a general object of specified type.
     *
     * @param object The boolean to set as parameter.
     * @param type   The SQL type to interpret {@code object} as.
     * @return {@code this} builder for further use.
     * @throws SQLException if substituting {@code object} for the next parameter is not possible.
     * @see PreparedStatement#setObject(int, Object, int)
     */
    public StatementParametrizer object(final Object object, final int type) throws SQLException {
        stmt.setObject(counter++, object, type);
        return this;
    }

}
