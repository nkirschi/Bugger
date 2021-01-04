package tech.bugger.persistence.util;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Language;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class StatementParametrizerTest {

    private StatementParametrizer parametrizer;

    @Mock
    private PreparedStatement stmt;

    @BeforeEach
    public void setup() {
        parametrizer = new StatementParametrizer(stmt);
    }

    @Test
    public void testInteger1() throws Exception {
        parametrizer.integer(25);
        verify(stmt).setInt(1, 25);
    }

    @Test
    public void testInteger2() throws Exception {
        parametrizer.integer(-5);
        verify(stmt).setInt(1, -5);
    }

    @Test
    public void testInteger3() throws Exception {
        parametrizer.integer(0);
        verify(stmt).setInt(1, 0);
    }

    @Test
    public void testBoolTrue() throws Exception {
        parametrizer.bool(true);
        verify(stmt).setBoolean(1, true);
    }

    @Test
    public void testBoolFalse() throws Exception {
        parametrizer.bool(false);
        verify(stmt).setBoolean(1, false);
    }

    @Test
    public void testBytes1() throws Exception {
        byte[] arr = new byte[0];
        parametrizer.bytes(arr);
        verify(stmt).setBytes(1, arr);
    }

    @Test
    public void testBytes2() throws Exception {
        byte[] arr = new byte[]{-1, 125, 12, 64};
        parametrizer.bytes(arr);
        verify(stmt).setBytes(1, arr);
    }

    @Test
    public void testBytes3() throws Exception {
        byte[] arr = new byte[]{0, 0, 23, -125};
        parametrizer.bytes(arr);
        verify(stmt).setBytes(1, arr);
    }

    @Test
    public void testString1() throws Exception {
        String str = "";
        parametrizer.string(str);
        verify(stmt).setString(1, str);
    }

    @Test
    public void testString2() throws Exception {
        String str = "abc32";
        parametrizer.string(str);
        verify(stmt).setString(1, str);
    }

    @Test
    public void testString3() throws Exception {
        String str = "#ß32i0jfä#";
        parametrizer.string(str);
        verify(stmt).setString(1, str);
    }

    @Test
    public void testObject1() throws Exception {
        String str = "#ß32i0jfä#";
        parametrizer.object(str);
        verify(stmt).setObject(1, str);
    }

    @Test
    public void testObject2() throws Exception {
        Object obj = new Object();
        parametrizer.object(obj);
        verify(stmt).setObject(1, obj);
    }

    @Test
    public void testObject3() throws Exception {
        Integer testInt = null;
        parametrizer.object(testInt);
        verify(stmt).setObject(1, testInt);
    }

    @Test
    public void testObject4() throws Exception {
        Language lang = Language.ENGLISH;
        int type = Types.OTHER;
        parametrizer.object(lang, type);
        verify(stmt).setObject(1, lang, type);
    }

    @Test
    public void testObject5() throws Exception {
        Integer testInt = null;
        int type = Types.INTEGER;
        parametrizer.object(testInt, type);
        verify(stmt).setObject(1, testInt, type);
    }

    @Test
    public void testObject6() throws Exception {
        LocalDate date = LocalDate.of(1999, 10, 3);
        int type = Types.DATE;
        parametrizer.object(date, type);
        verify(stmt).setObject(1, date, type);
    }

    @Test
    public void testMix() throws Exception {
        String str1 = "";
        String str2 = "hello there";
        int type = Types.VARCHAR;
        String str3 = "General Kenobi!";
        int testInt = 15;
        boolean bool = false;

        PreparedStatement resStmt = parametrizer
                .object(str1)
                .object(str2, type)
                .string(str3)
                .integer(testInt)
                .bool(bool)
                .toStatement();

        verify(stmt).setObject(1, str1);
        verify(stmt).setObject(2, str2, type);
        verify(stmt).setString(3, str3);
        verify(stmt).setInt(4, testInt);
        verify(stmt).setBoolean(5, bool);
        assertEquals(stmt, resStmt);
    }

}