package tech.bugger.persistence.gateway;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
public class TokenDBGatewayTest {

    private TokenDBGateway gateway;

    private Connection connection;

    private User admin;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new TokenDBGateway(connection);
        admin = new UserDBGateway(connection).getUserByID(1);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testGenerateToken() throws Exception {
        // Get the token's expected length (multiply by 2 because we generate 32 bytes = 64 chars in hex)
        Field f = TokenDBGateway.class.getDeclaredField("TOKEN_LENGTH");
        f.setAccessible(true);
        int tokenLength = (int) f.get(null) * 2;

        ZonedDateTime dt = ZonedDateTime.now();
        Token token = gateway.generateToken(admin, Token.Type.CHANGE_EMAIL);

        assertAll(() -> assertEquals(admin, token.getUser()),
                () -> assertEquals(tokenLength, token.getValue().length()),
                () -> assertTrue(token.getTimestamp().isAfter(dt)));
    }

    @Test
    public void testGenerateTokenWithUserIdNull() {
        User incompleteUser = new User(admin);
        incompleteUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.generateToken(incompleteUser, Token.Type.CHANGE_EMAIL));
    }

    @Test
    public void testGenerateTokenWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(matches("INSERT INTO token.*"), anyInt());
        assertThrows(StoreException.class, () -> new TokenDBGateway(connectionSpy).generateToken(admin, Token.Type.CHANGE_EMAIL));
    }

    @Test
    public void testGenerateTokenUserSearchWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(matches("SELECT \\* FROM \"user\".*"));
        assertThrows(StoreException.class, () -> new TokenDBGateway(connectionSpy).generateToken(admin, Token.Type.CHANGE_EMAIL));
    }

    @Test
    public void testGenerateTokenUserNotExists() {
        User copy = new User(admin);
        copy.setId(45);
        assertThrows(NotFoundException.class, () -> gateway.generateToken(copy, Token.Type.CHANGE_EMAIL));
    }

    @Test
    public void testGenerateTokenInsertedNothing() throws Exception {
        TokenDBGateway gatewaySpy = spy(gateway);
        ResultSet resultSetMock = mock(ResultSet.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(true).when(gatewaySpy).isValid(any());
        doReturn(false).when(resultSetMock).next();
        doReturn(resultSetMock).when(stmtMock).getGeneratedKeys();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any(), anyInt());
        assertNull(new TokenDBGateway(connectionSpy).generateToken(admin, Token.Type.CHANGE_EMAIL));
        reset(gatewaySpy, stmtMock);
    }

    @Test
    public void testDefinitelyInvalidTokenIsValid() {
        boolean isValid = gateway.isValid("i am an invalid token");
        assertFalse(isValid);
    }

    @Test
    public void testCrackedInvalidTokenIsValid() {
        boolean isValid = gateway.isValid("db2b0333a72d2388e42eb772f90f309f7ed5ed5c8b02201392abff9a4509e660");
        assertFalse(isValid);
    }

    @Test
    public void testValidTokenIsValid() throws Exception {
        Token token = gateway.generateToken(admin, Token.Type.CHANGE_EMAIL);
        boolean isValid = gateway.isValid(token.getValue());
        assertTrue(isValid);
    }

    @Test
    public void testIsValidWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new TokenDBGateway(connectionSpy).isValid("doesn't matter"));
    }

    @Test
    public void testGetTokenByValue() throws Exception {
        Token token = gateway.generateToken(admin, Token.Type.CHANGE_EMAIL);

        Token fetched = null;
        try {
            fetched = gateway.getTokenByValue(token.getValue());
        } catch (NotFoundException e) {
            fail();
        }
        assertEquals(token, fetched);
    }

    @Test
    public void testGetTokenByValueNotFound() {
        assertThrows(NotFoundException.class, () -> gateway.getTokenByValue("0123456789abcdef"));
    }

    @Test
    public void testGetTokenByValueWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new TokenDBGateway(connectionSpy).getTokenByValue("0123456789abcdef"));
    }

}