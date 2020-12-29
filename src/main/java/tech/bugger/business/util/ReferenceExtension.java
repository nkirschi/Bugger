package tech.bugger.business.util;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;

/**
 * Registrable extension for parsing custom Markdown reference syntax.
 */
public final class ReferenceExtension implements Parser.ParserExtension {

    /**
     * Only the builder may legitimately create an instance of this class.
     */
    private ReferenceExtension() {
    }

    /**
     * Returns a registrable instance of this extension.
     *
     * @return A registrable instance of this extension.
     */
    public static Extension create() {
        return new ReferenceExtension();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void extend(final Parser.Builder parserBuilder) {
        parserBuilder.postProcessor(new ReferencePostProcessor());
    }

}
