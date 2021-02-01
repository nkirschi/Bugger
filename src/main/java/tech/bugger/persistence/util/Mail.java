package tech.bugger.persistence.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Representation of an e-mail.
 */
public final class Mail {

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
    private final String subject;

    /**
     * Content of the e-mail.
     */
    private final String content;

    /**
     * Builder for {@link Mail}s.
     */
    public static class Builder {

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
         * Constructs a new e-mail builder.
         */
        public Builder() {
            to = new ArrayList<>();
            cc = new ArrayList<>();
            bcc = new ArrayList<>();
            replyto = new ArrayList<>();
            subject = "";
            content = "";
        }

        /**
         * Returns the e-mail being built.
         *
         * @return The current {@link Mail} instance.
         */
        public Mail envelop() {
            return new Mail(to, cc, bcc, replyto, subject, content);
        }

        /**
         * Adds a direct recipient to the e-mail.
         *
         * @param recipient The direct recipient to add.
         * @return {@code this} builder for further use.
         */
        public Builder to(final String recipient) {
            to.add(recipient);
            return this;
        }

        /**
         * Adds a carbon copy recipient to the e-mail.
         *
         * @param recipient The CC recipient to add.
         * @return {@code this} builder for further use.
         */
        public Builder cc(final String recipient) {
            cc.add(recipient);
            return this;
        }

        /**
         * Adds a blind carbon copy recipient to the e-mail.
         *
         * @param recipient The BCC recipient to add.
         * @return {@code this} builder for further use.
         */
        public Builder bcc(final String recipient) {
            bcc.add(recipient);
            return this;
        }


        /**
         * Adds a reply recipient to the e-mail.
         *
         * @param recipient The Reply-To recipient to add.
         * @return {@code this} builder for further use.
         */
        public Builder replyto(final String recipient) {
            replyto.add(recipient);
            return this;
        }

        /**
         * Sets a subject on the e-mail, overwriting the old value.
         *
         * @param subject The subject to set.
         * @return {@code this} builder for further use.
         */
        public Builder subject(final String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * Sets a content on the e-mail, overwriting the old value.
         *
         * @param content The content to set.
         * @return {@code this} builder for further use.
         */
        public Builder content(final String content) {
            this.content = content;
            return this;
        }

    }

    /**
     * Constructs a new e-mail from the given parameters.
     *
     * @param to      The direct recipients of the e-mail.
     * @param cc      The carbon copy recipients of the e-mail.
     * @param bcc     The blind carbon copy recipients of the e-mail.
     * @param replyto The reply recipients of the e-mail.
     * @param subject The subject of the e-mail.
     * @param content The content fo the e-mail.
     */
    private Mail(final Collection<String> to, final Collection<String> cc, final Collection<String> bcc,
                 final Collection<String> replyto, final String subject, final String content) {
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.replyto = replyto;
        this.subject = subject;
        this.content = content;
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
     * Returns the carbon copy recipients of this e-mail.
     *
     * @return The CC recipients.
     */
    public Collection<String> getCc() {
        return Collections.unmodifiableCollection(cc);
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
     * Returns the reply recipients of this e-mail.
     *
     * @return The Reply-To recipients.
     */
    public Collection<String> getReplyto() {
        return Collections.unmodifiableCollection(replyto);
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
     * Returns the content of this e-mail.
     *
     * @return The e-mail content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Converts this mail into a human-readable string representation.
     *
     * @return A human-readable string representation of this mail.
     */
    @Override
    public String toString() {
        return "Mail{"
                + "to=" + to
                + ", cc=" + cc
                + ", bcc=" + bcc
                + ", replyto=" + replyto
                + ", subject='" + subject + '\''
                + ", content.length='" + content.length() + '\''
                + '}';
    }

}
