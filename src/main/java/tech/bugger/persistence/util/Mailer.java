package tech.bugger.persistence.util;

import tech.bugger.global.util.Log;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Basic e-mail sender singleton.
 *
 * This is a facade for any mailing API, currently Jakarta Mail.
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
     * @param host     The SMTP server host name.
     * @param port     The SMTP server TCP port.
     * @param username The SMTP server login username.
     * @param password The SMTP server login password.
     * @param starttls Whether to use a secure TLS connection.
     * @param sender   The sender e-mail address to include in each mail.
     */
    public void configure(String host, int port, String username, String password, boolean starttls, String sender) {
        configuration.clear();
        configuration.put("mail.smtp.auth", Boolean.toString(starttls));
        configuration.put("mail.smtp.starttls.enable", Boolean.toString(starttls));
        configuration.put("mail.smtp.host", host);
        configuration.put("mail.smtp.port", port);
        configuration.put("sender", sender);
        authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    /**
     * Sends an e-mail with the specified properties.
     *
     * @param recipient The recipient e-mail address of the e-mail to be sent.
     * @param subject   The subject of the e-mail to be sent.
     * @param msg       The content of the e-mail to be sent.
     * @throws IllegalStateException if called without the mailer being configured via {@link #configure}.
     */
    public void sendMail(String recipient, String subject, String msg) {
        if (configuration.isEmpty()) {
            throw new IllegalStateException("Mailer has not yet been configured!");
        }
        Session mailSession = Session.getDefaultInstance(configuration, authenticator);
        try {
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(configuration.getProperty("sender")));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject);
            message.setText(msg);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
