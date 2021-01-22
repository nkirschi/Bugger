package tech.bugger.persistence.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
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
import tech.bugger.global.util.Log;

/**
 * Basic e-mail sender.
 * <p>
 * This is a facade for any mailing API (currently Jakarta Mail), adapted to our needs.
 */
public final class Mailer {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(Mailer.class);

    /**
     * E-mail session used by this mailer throughout its lifetime.
     */
    private final Session session;

    /**
     * Constructs a new mailer without authentication.
     *
     * @param is Stream of mail configuration settings. The format and valid entries are specified in the
     *           <a href="https://eclipse-ee4j.github.io/mail/docs/api/">Jakarta Mail API Docs</a>.
     * @throws IOException if the configuration could not be read.
     */
    public Mailer(final InputStream is) throws IOException {
        session = Session.getInstance(loadConfiguration(is));
    }

    /**
     * Constructs a new mailer with authentication parameters.
     *
     * @param is       Stream of mail configuration settings. The format and valid entries are specified in the
     *                 <a href="https://eclipse-ee4j.github.io/mail/docs/api/">Jakarta Mail API Docs</a>.
     * @param username The username needed for authentication.
     * @param password The password needed for authentication.
     * @throws IOException if the configuration could not be read.
     */
    public Mailer(final InputStream is, final String username, final String password) throws IOException {
        session = Session.getInstance(loadConfiguration(is), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Properties loadConfiguration(final InputStream is) throws IOException {
        Properties configuration = new Properties();
        try {
            configuration.load(is);
        } catch (IOException e) {
            log.warning("Mailing configuration could not be loaded.");
            throw new IOException("Mailing configuration could not be loaded.", e);
        }
        return configuration;
    }

    /**
     * Sends the given {@link Mail)}.
     *
     * @param mail The e-mail to be sent.
     * @return Whether {@code mail} has been successfully sent.
     */
    public boolean send(final Mail mail) {
        return send(
                addressify(mail.getTo()),
                addressify(mail.getCc()),
                addressify(mail.getBcc()),
                addressify(mail.getReplyto()),
                mail.getSubject(),
                mail.getContent()
        );
    }

    private boolean send(final Address[] to, final Address[] cc, final Address[] bcc, final Address[] replyto,
                         final String subject, final String content) {
        log.debug(String.format("Sending mail to %s with cc %s, bcc %s, replyto %s and subject '%s'.",
                Arrays.toString(to), Arrays.toString(cc), Arrays.toString(bcc), Arrays.toString(replyto), subject));
        try {
            MimeMessage message = new MimeMessage(session);
            message.setRecipients(Message.RecipientType.TO, to);
            message.setRecipients(Message.RecipientType.CC, cc);
            message.setRecipients(Message.RecipientType.BCC, bcc);
            message.setReplyTo(replyto);
            message.setSubject(subject);
            message.setText(content);
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            log.warning("Could not send mail with subject \"" + subject + "\".", e);
            return false;
        }
    }

    private Address[] addressify(final Collection<String> addresses) {
        Collection<Address> validAddresses = new ArrayList<>(addresses.size());
        for (String address : addresses) {
            try {
                validAddresses.add(new InternetAddress(address, true));
            } catch (AddressException e) {
                log.warning("Invalid e-mail address " + address + ".", e);
            }
        }
        return validAddresses.toArray(new Address[]{});
    }

}
