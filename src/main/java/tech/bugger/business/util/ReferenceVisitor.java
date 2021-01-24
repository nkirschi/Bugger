package tech.bugger.business.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import tech.bugger.control.util.JFConfig;
import tech.bugger.global.util.Log;

/**
 * Renderer for custom Markdown reference syntax.
 * <ul>
 *     <li><code>#reportID</code> turns into a link to the corresponding report</li>
 *     <li><code>##postID</code> turns into a link to the corresponding post</li>
 *     <li><code>@username</code> turns into a link to the corresponding user's profile page</li>
 * </ul>
 */
public class ReferenceVisitor extends AbstractVisitor {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ReferenceVisitor.class);

    /**
     * Allowed characters in references.
     */
    private static final String CHARACTERS_IN_REFERENCES = "a-zA-Z0-9_äöüÄÖÜßẞ";

    /**
     * Pattern to match characters, that are not present in any reference.
     */
    private static final Pattern PATTERN_NON_REFERENCE = Pattern.compile("[^" + CHARACTERS_IN_REFERENCES + "]");

    /**
     * Pattern to match characters, that are not present in any reference.
     */
    private static final Pattern PATTERN_USER = Pattern.compile("^[" + CHARACTERS_IN_REFERENCES + "].*");

    /**
     * The intro sequence for a user reference.
     */
    private static final String USER_REFERENCE = "@";

    /**
     * The intro sequence for a topic reference.
     */
    private static final String TOPIC_REFERENCE = "!";

    /**
     * The intro sequence for a post reference.
     */
    private static final String POST_REFERENCE = "##";

    /**
     * The intro sequence for a report reference.
     */
    private static final String REPORT_REFERENCE = "#";

    /**
     * The endpoint URL for a user reference.
     */
    private static final String USER_ENDPOINT = "/profile?u=%1$s";

    /**
     * The endpoint URL for a topic reference.
     */
    private static final String TOPIC_ENDPOINT = "/topic?id=%1$s";

    /**
     * The endpoint URL for a post reference.
     */
    private static final String POST_ENDPOINT = "/report?p=%1$s#post-%1$s";

    /**
     * The endpoint URL for a report reference.
     */
    private static final String REPORT_ENDPOINT = "/report?id=%1$s";

    /**
     * Searches for references in {@link Text} nodes, extracts them and renders them as {@link Link} nodes.
     *
     * @param text The {@link Text} node to parse.
     */
    @Override
    public void visit(final Text text) {
        String str = text.getLiteral();
        if (str.contains(USER_REFERENCE)) {
            parseText(text, USER_REFERENCE, USER_ENDPOINT);
        } else if (str.contains(TOPIC_REFERENCE)) {
            parseText(text, TOPIC_REFERENCE, TOPIC_ENDPOINT);
        } else if (str.contains(POST_REFERENCE)) {
            parseText(text, POST_REFERENCE, POST_ENDPOINT);
        } else if (str.contains(REPORT_REFERENCE)) {
            parseText(text, REPORT_REFERENCE, REPORT_ENDPOINT);
        }
    }

    /**
     * Parses the given text with the given parameters.
     *
     * @param text     The text node to parse.
     * @param sequence The reference sequence to search for.
     * @param dest     The URL endpoint to refer to.
     */
    public void parseText(final Text text, final String sequence, final String dest) {
        String str = text.getLiteral();
        log.debug("Parsing reference in \"" + str + "\" to a node.");

        // Split at reference start
        String[] split = str.split(sequence, 2);

        // Parsing already done
        if (split.length < 2) {
            return;
        }
        String refAndRest = split[1];

        // Reference end index
        int indexEnd = indexOf(refAndRest, PATTERN_NON_REFERENCE);

        // Parse Link if needed
        Node refNode = parseLink(sequence, refAndRest.substring(0, indexEnd), dest);

        // Append rest
        String rest = refAndRest.substring(indexEnd);
        Text beforeNode = new Text(split[0]);
        Text afterNode = new Text(rest);

        // Parse recursively until all references are parsed
        parseInOrder(text, beforeNode, refNode, afterNode);
    }

    /**
     * Parses the given input to a link if it represents one, otherwise this just parses it to a text node.
     *
     * @param sequence The reference sequence to search for.
     * @param refId    The reference's id.
     * @param dest     The URL endpoint to refer to.
     * @return A link node if the input is a valid reference, otherwise just a text node.
     */
    private Node parseLink(final String sequence, final String refId, final String dest) {
        Optional<Integer> parsedInt = getInteger(refId);
        // Users can also contain other characters
        if (sequence.equals(USER_REFERENCE) && PATTERN_USER.matcher(refId).matches()) {
            Link node = new Link(JFConfig.getApplicationPath() + String.format(dest, refId), refId);
            node.appendChild(new Text(sequence + refId));
            return node;
        } else if (parsedInt.isPresent()) {
            String ref = parsedInt.get() + "";
            Link node = new Link(JFConfig.getApplicationPath() + String.format(dest, ref), ref);
            node.appendChild(new Text(sequence + ref));
            return node;
        } else {
            return new Text(sequence + refId);
        }
    }

    /**
     * Parses the given nodes in order and appends them to the syntax tree.
     *
     * @param original   The original node, from which the other nodes were split.
     * @param firstNode  The first node.
     * @param middleNode The middle node.
     * @param lastNode   The last node.
     */
    private void parseInOrder(final Node original, final Text firstNode, final Node middleNode, final Text lastNode) {
        original.insertAfter(lastNode);
        original.insertAfter(middleNode);
        original.insertAfter(firstNode);
        visit(firstNode);
        visit(lastNode);
        original.unlink();
    }

    /**
     * Tries to parse the given string into an integer and returns whether it was successful or not.
     *
     * @param str The string to check.
     * @return {@code true} iff the given string represents an integer, {@code false} otherwise.
     */
    private Optional<Integer> getInteger(final String str) {
        try {
            return Optional.of(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the occurrence index of the given regular expression.
     *
     * @param str     The string to search in.
     * @param pattern The {@link Pattern} to search for.
     * @return The index of the given regular expression in the string or the string's size if it couldn't be found.
     */
    private int indexOf(final String str, final Pattern pattern) {
        Matcher matcher = pattern.matcher(str);
        return matcher.find() ? matcher.start() : str.length();
    }

}
