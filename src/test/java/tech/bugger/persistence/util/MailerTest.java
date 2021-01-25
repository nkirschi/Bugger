package tech.bugger.persistence.util;

import com.dumbster.smtp.SimpleSmtpServer;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LogExtension.class)
public class MailerTest {

    private SimpleSmtpServer smtpServer;

    private static final int SMTP_PORT = 42424;

    private static final String RECIPIENT = "admin@bugger.tech";
    private static final String CC = "thomas@hackl.root";
    private static final String CC2 = "president@whitehouse.gov";
    private static final String BCC = "chuck@norr.is";
    private static final String REPLYTO = "watzlaf@wttlbrm.ft";
    private static final String SUBJECT = "Reversi Problem";
    private static final String MESSAGE = "The bot is cheating. -Watzlaf";

    private Mailer mailer;

    @BeforeEach
    public void setUp() throws Exception {
        while (isPortBlocked()) ;
        smtpServer = SimpleSmtpServer.start(SMTP_PORT);
        mailer = new Mailer(ClassLoader.getSystemResourceAsStream("mailing.properties"));
    }

    @AfterEach
    public void tearDown() {
        smtpServer.stop();
    }

    private static boolean isPortBlocked() {
        try (Socket ignored = new Socket("localhost", MailerTest.SMTP_PORT)) {
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    @Test
    public void testSend() {
        Mail mail = new Mail.Builder()
                .to(RECIPIENT)
                .cc(CC).cc(CC2)
                .bcc(BCC)
                .replyto(REPLYTO)
                .subject(SUBJECT)
                .content(MESSAGE)
                .envelop();
        boolean sent = mailer.send(mail);
        var message = smtpServer.getReceivedEmails().get(0);
        assertAll(
                () -> assertTrue(sent),
                () -> assertTrue(message.getHeaderValue("To").contains(RECIPIENT)),
                () -> assertTrue(message.getHeaderValue("Cc").contains(CC)),
                () -> assertTrue(message.getHeaderValue("Cc").contains(CC2)),
                () -> assertTrue(message.getHeaderValue("Reply-To").contains(REPLYTO)),
                () -> assertTrue(message.getHeaderValue("Subject").contains(SUBJECT)),
                () -> assertTrue(message.getBody().contains(MESSAGE))
        );
    }

    @Test
    public void testSendWhenNoRecipients() {
        boolean sent = mailer.send(new Mail.Builder().envelop());
        assertAll(
                () -> assertFalse(sent),
                () -> assertTrue(smtpServer.getReceivedEmails().isEmpty())
        );
    }

    @Test
    public void testSendWhenAddressInvalid() {
        Mail mail = new Mail.Builder().to("?nväliD").to(RECIPIENT).envelop();
        boolean sent = mailer.send(mail);
        assertAll(
                () -> assertTrue(sent),
                () -> assertEquals(1, smtpServer.getReceivedEmails().size())
        );
    }

    @Test
    public void testSimpleConstructorWhenConfigurationInvalid() throws Exception {
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
        is.close();
        assertThrows(IOException.class, () -> new Mailer(is));
    }

    @Test
    public void testFullConstructorWhenConfigurationInvalid() throws Exception {
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
        is.close();
        assertThrows(IOException.class, () -> new Mailer(is, "username", "password"));
    }

    @Test
    public void testFullConstructorWhenAllFine() {
        assertDoesNotThrow(() -> new Mailer(ClassLoader.getSystemResourceAsStream("mailing.properties"), null, null));
    }

    @Test
    public void testGetPasswordAuthenticatorJustForBranchCoverage() throws Exception {
        Mailer mailer = new Mailer(ClassLoader.getSystemResourceAsStream("mailing.properties"), "username", "password");
        Field field = mailer.getClass().getDeclaredField("session");
        field.setAccessible(true);
        Session session = (Session) field.get(mailer);
        PasswordAuthentication auth = session.requestPasswordAuthentication(InetAddress.getLocalHost(), SMTP_PORT,
                "SMTP", null, null);
        assertAll(
                () -> assertEquals("username", auth.getUserName()),
                () -> assertEquals("password", auth.getPassword())
        );
    }
}