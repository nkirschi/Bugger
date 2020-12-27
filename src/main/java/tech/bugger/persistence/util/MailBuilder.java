package tech.bugger.persistence.util;

/**
 * Builder for {@link Mail}s.
 */
public class MailBuilder {

    /**
     * E-mail being successively built.
     */
    private final Mail mail;

    /**
     * Constructs a new e-mail builder.
     */
    public MailBuilder() {
        mail = new Mail();
    }

    /**
     * Returns the e-mail being built.
     *
     * @return The current {@link Mail} instance.
     */
    public Mail envelop() {
        return mail;
    }

    /**
     * Adds a direct recipient to the e-mail.
     *
     * @param recipient The direct recipient to add.
     * @return {@code this} builder for further use.
     */
    public MailBuilder to(final String recipient) {
        mail.addTo(recipient);
        return this;
    }

    /**
     * Adds a carbon copy recipient to the e-mail.
     *
     * @param recipient The CC recipient to add.
     * @return {@code this} builder for further use.
     */
    public MailBuilder cc(final String recipient) {
        mail.addCc(recipient);
        return this;
    }

    /**
     * Adds a blind carbon copy recipient to the e-mail.
     *
     * @param recipient The BCC recipient to add.
     * @return {@code this} builder for further use.
     */
    public MailBuilder bcc(final String recipient) {
        mail.addBcc(recipient);
        return this;
    }

    /**
     * Adds a reply recipient to the e-mail.
     *
     * @param recipient The Reply-To recipient to add.
     * @return {@code this} builder for further use.
     */
    public MailBuilder replyto(final String recipient) {
        mail.addReplyto(recipient);
        return this;
    }

    /**
     * Sets a subject on the e-mail, overwriting the old value.
     *
     * @param subject The subject to set.
     * @return {@code this} builder for further use.
     */
    public MailBuilder subject(final String subject) {
        mail.setSubject(subject);
        return this;
    }

    /**
     * Sets a content on the e-mail, overwriting the old value.
     *
     * @param content The content to set.
     * @return {@code this} builder for further use.
     */
    public MailBuilder content(final String content) {
        mail.setContent(content);
        return this;
    }

}
