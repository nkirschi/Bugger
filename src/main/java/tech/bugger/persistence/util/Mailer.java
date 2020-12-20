package tech.bugger.persistence.util;

import tech.bugger.global.util.Log;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * Basic e-mail sender singleton.
 *
 * This is a facade for any mailing API (currently Jakarta Mail), adapted to our needs.
 */
public final class Mailer {

    private static final Log log = Log.forClass(Mailer.class);

    private static Mailer instance;

    /**
     * Configuration of the mailer.
     */
    private final Properties configuration;

    /**
     * Authentication information for the mailer.
     */
    private Authenticator authenticator;

    /**
     * Constructs a mailer and initializes an empty configuration.
     */
    private Mailer() {
        configuration = new Properties();
    }

    /**
     * Retrieves the singleton mailer object.
     *
     * @return The one and only instance of the mailer.
     */
    public static Mailer getInstance() {
        if (instance == null) {
            instance = new Mailer();
        }
        return instance;
    }

    /**
     * Configures the fundamental e-mailing parameters. This has to happen before any mail can be sent.
     *
     * @param is       Stream of mail configuration settings. The format and valid entries are specified in the
     *                 <a href="https://eclipse-ee4j.github.io/mail/docs/api/">Jakarta Mail API Docs</a>.
     * @param username The authentication username.
     * @param password The authentication password.
     */
    public void configure(InputStream is, String username, String password) throws IOException {
        configuration.clear();
        try {
            configuration.load(is);
        } catch (IOException e) {
            log.warning("Mailing configuration could not be loaded.");
            throw new IOException("Mailing configuration could not be loaded.", e);
        }
        authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    /**
     * Sends a basic e-mail.
     *
     * @param recipient The recipient e-mail address of the e-mail to be sent.
     * @param subject   The subject of the e-mail to be sent.
     * @param content   The content of the e-mail to be sent.
     * @throws IllegalStateException if called without the mailer being configured beforehand via {@link #configure}.
     */
    public void send(String recipient, String subject, String content) {
        send(recipient, Collections.emptyList(), subject, content);
    }

    /**
     * Sends a basic e-mail with BCC recipients.
     *
     * @param recipient The recipient e-mail address of the e-mail to be sent.
     * @param bcc       The blind carbon copy recipients of the e-mail.
     * @param subject   The subject of the e-mail to be sent.
     * @param content   The content of the e-mail to be sent.
     */
    public void send(String recipient, Collection<String> bcc, String subject, String content) {
        send(Collections.singleton(recipient), Collections.emptyList(), bcc, Collections.emptyList(), subject, content);
    }

    /**
     * Sends a full-featured e-mail.
     *
     * @param to      The primary recipients of the e-mail.
     * @param cc      The carbon copy recipients of the e-mail.
     * @param bcc     The blind carbon copy recipients of the e-mail.
     * @param replyto The reply-to addresses of the e-mail.
     * @param subject The subject of the e-mail to be sent.
     * @param content The content of the e-mail to be sent.
     * @throws IllegalStateException if called without the mailer being configured beforehand via {@link #configure}.
     */
    public void send(Collection<String> to, Collection<String> cc, Collection<String> bcc,
                     Collection<String> replyto, String subject, String content) {
        if (configuration.isEmpty()) {
            throw new IllegalStateException("Mailer has not yet been configured!");
        }
        send(parseAddresses(to), parseAddresses(cc), parseAddresses(bcc), parseAddresses(replyto), subject, content);
    }

    private void send(Address[] to, Address[] cc, Address[] bcc, Address[] replyto, String subject, String content) {
        try {
            Session mailSession = Session.getDefaultInstance(configuration, authenticator);
            MimeMessage message = new MimeMessage(mailSession);
            message.addRecipients(Message.RecipientType.TO, to);
            message.addRecipients(Message.RecipientType.CC, cc);
            message.addRecipients(Message.RecipientType.BCC, bcc);
            message.setReplyTo(replyto);
            message.setSubject(subject);
            message.setText(content);
            Transport.send(message);
        } catch (MessagingException e) {
            log.warning("Could not send mail with subject \"" + subject + "\".");
        }
    }

    private Address[] parseAddresses(Collection<String> addresses) {
        Collection<Address> validAddresses = new ArrayList<>();
        for (String address : addresses) {
            try {
                validAddresses.add(new InternetAddress(address));
            } catch (AddressException e) {
                log.warning("Invalid e-mail address " + address + ".", e);
            }
        }
        return validAddresses.toArray(new Address[]{});
    }

    public static void main(String[] args) throws Exception {
        String config = "mail.smtp.auth = true\n"
                + "mail.smtp.starttls.enable = true\n"
                + "mail.smtp.host = smtp.mail.de\n"
                + "mail.smtp.port = 587\n"
                + "mail.debug = false\n"
                + "mail.from = bugger@mail.de";
        Mailer.getInstance().configure(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)), "bugger",
                "BuggerFahrenMachtSpass42");
        Mailer.getInstance().send("nikolas.kirschstein@gmail.com", "Application", "I apply.");
    }
}
