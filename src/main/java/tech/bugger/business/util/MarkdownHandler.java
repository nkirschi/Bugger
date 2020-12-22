package tech.bugger.business.util;

import java.util.Collections;
import java.util.List;
import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import tech.bugger.global.util.Log;

/**
 * Utility class for converting Markdown to HTML.
 */
public final class MarkdownHandler {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(MarkdownHandler.class);

    /**
     * All {@link Extension}s to load for the commonmark-java library.
     */
    private static final List<Extension> EXTENSIONS = Collections.singletonList(ReferenceExtension.create());

    /**
     * The {@link Parser} used to parse Markdown via the commonmark-java library.
     */
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

    /**
     * The {@link HtmlRenderer} used to render HTML from Markdown via the commonmark-java library.
     */
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().escapeHtml(true).extensions(EXTENSIONS).build();

    private MarkdownHandler() {
    }

    /**
     * Parses and renders the given Markdown String to HTML.
     *
     * @param md The input formatted as Markdown.
     * @return The parsed and rendered equivalent HTML Output.
     */
    public static String toHtml(final String md) {
        if (md == null) {
            return "";
        }
        return RENDERER.render(PARSER.parse(md));
    }

}
