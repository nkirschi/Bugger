package tech.bugger.persistence.gateway;

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

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
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
    public void testCreateToken() throws Exception {
        ZonedDateTime dt = ZonedDateTime.now();
        String value = "0123456789abcdef";
        User user = new User();
        user.setId(admin.getId());
        Token toInsert = new Token(value, Token.Type.CHANGE_EMAIL, null, user);
        Token token = gateway.createToken(toInsert);

        assertAll(() -> assertEquals(admin, token.getUser()),
                () -> assertEquals(value, token.getValue()),
                () -> assertTrue(token.getTimestamp().isAfter(dt)));
    }

    @Test
    public void testCreateTokenWithTokenValueNull() {
        Token token = new Token(null, Token.Type.CHANGE_EMAIL, null, admin);
        assertThrows(IllegalArgumentException.class, () -> gateway.createToken(token));
    }

    @Test
    public void testCreateTokenWithTokenTypeNull() {
        Token token = new Token("0123456789abcdef", null, null, admin);
        assertThrows(IllegalArgumentException.class, () -> gateway.createToken(token));
    }

    @Test
    public void testCreateTokenWithNullUser() {
        Token token = new Token("0123456789abcdef", Token.Type.CHANGE_EMAIL, null, null);
        assertThrows(IllegalArgumentException.class, () -> gateway.createToken(token));
    }

    @Test
    public void testCreateTokenWithUserIdNull() {
        User incompleteUser = new User(admin);
        incompleteUser.setId(null);
        Token token = new Token("0123456789abcdef", Token.Type.CHANGE_EMAIL, null, incompleteUser);
        assertThrows(IllegalArgumentException.class, () -> gateway.createToken(token));
    }

    @Test
    public void testCreateTokenWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(matches("INSERT INTO token.*"), anyInt());
        Token token = new Token("0123456789abcdef", Token.Type.CHANGE_EMAIL, null, admin);
        assertThrows(StoreException.class, () -> new TokenDBGateway(connectionSpy).createToken(token));
    }

    @Test
    public void testCreateTokenUserNotExists() {
        admin.setId(45);
        Token token = new Token("0123456789abcdef", Token.Type.CHANGE_EMAIL, null, admin);
        assertThrows(NotFoundException.class, () -> gateway.createToken(token));
    }

    @Test
    public void testCreateTokenInsertedNothing() throws Exception {
        ResultSet resultSetMock = mock(ResultSet.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(false).when(resultSetMock).next();
        doReturn(resultSetMock).when(stmtMock).getGeneratedKeys();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any(), anyInt());
        Token token = new Token("0123456789abcdef", Token.Type.CHANGE_EMAIL, null, admin);
        assertThrows(StoreException.class, () -> new TokenDBGateway(connectionSpy).createToken(token));
        reset(connectionSpy, stmtMock);
    }

    @Test
    public void testFindToken() throws Exception {
        Token toInsert = new Token("0123456789abcdef", Token.Type.CHANGE_EMAIL, null, admin);
        Token token = gateway.createToken(toInsert);

        Token fetched = null;
        try {
            fetched = gateway.findToken(token.getValue());
        } catch (NotFoundException e) {
            fail();
        }
        assertEquals(token, fetched);
    }

    @Test
    public void testFindTokenNotFound() {
        assertThrows(NotFoundException.class, () -> gateway.findToken("0123456789abcdef"));
    }

    @Test
    public void testFindTokenWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new TokenDBGateway(connectionSpy).findToken("0123456789abcdef"));
    }

}