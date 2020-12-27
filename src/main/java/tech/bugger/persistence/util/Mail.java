package tech.bugger.persistence.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Representation of an e-mail.
 */
public class Mail {

    /**
     * Direct recipients of the e-mail.
     */
    private final Collection<String> to;

    /**
     * Carbon copy recipients of the e-mail.
     */
    private final Collection<String> cc;

    /**
     * Blind carbon copy recipients of the e-mail.
     */
    private final Collection<String> bcc;

    /**
     * Reply recipients of the e-mail.
     */
    private final Collection<String> replyto;

    /**
     * Subject of the e-mail.
     */
    private String subject;

    /**
     * Content of the e-mail.
     */
    private String content;

    /**
     * Constructs a new e-mail with no recipients and empty content.
     */
    public Mail() {
        to = new ArrayList<>();
        cc = new ArrayList<>();
        bcc = new ArrayList<>();
        replyto = new ArrayList<>();
        subject = "";
        content = "";
    }

    /**
     * Returns the direct recipients of this e-mail.
     *
     * @return The direct recipients.
     */
    public Collection<String> getTo() {
        return Collections.unmodifiableCollection(to);
    }

    /**
     * Adds the given recipient to this e-mail's direct recipients.
     *
     * @param recipient E-mail address to add to the direct recipients.
     */
    public void addTo(final String recipient) {
        to.add(recipient);
    }

    /**
     * Returns the carbon copy recipients of this e-mail.
     *
     * @return The CC recipients.
     */
    public Collection<String> getCc() {
        return Collections.unmodifiableCollection(cc);
    }

    /**
     * Adds the given recipient to this e-mail's carbon copy recipients.
     *
     * @param recipient E-mail address to add to the CC recipients.
     */
    public void addCc(final String recipient) {
        cc.add(recipient);
    }

    /**
     * Returns the blind carbon copy recipients of this e-mail.
     *
     * @return The BCC recipients.
     */
    public Collection<String> getBcc() {
        return Collections.unmodifiableCollection(bcc);
    }

    /**
     * Adds the given recipient to this e-mail's blind carbon copy recipients.
     *
     * @param recipient E-mail address to add to the BCC recipients.
     */
    public void addBcc(final String recipient) {
        bcc.add(recipient);
    }

    /**
     * Returns the reply recipients of this e-mail.
     *
     * @return The Reply-To recipients.
     */
    public Collection<String> getReplyto() {
        return Collections.unmodifiableCollection(replyto);
    }

    /**
     * Adds the given recipient to this e-mail's reply recipients.
     *
     * @param recipient E-mail address to add to the Reply-To recipients.
     */
    public void addReplyto(final String recipient) {
        replyto.add(recipient);
    }

    /**
     * Returns the subject of this e-mail.
     *
     * @return The e-mail subject.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject of this e-mail.
     *
     * @param subject The subject to be set.
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    /**
     * Returns the content of this e-mail.
     *
     * @return The e-mail content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of this e-mail.
     *
     * @param content The e-mail content.
     */
    public void setContent(final String content) {
        this.content = content;
    }
}
