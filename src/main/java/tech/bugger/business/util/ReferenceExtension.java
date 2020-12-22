package tech.bugger.business.util;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import tech.bugger.global.util.Log;

/**
 * Registrable extension for parsing custom Markdown reference syntax.
 */
public final class ReferenceExtension implements Parser.ParserExtension {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ReferenceExtension.class);

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
