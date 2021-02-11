package tech.bugger.business.util;

import org.commonmark.node.Node;
import org.commonmark.parser.PostProcessor;

/**
 * {@link PostProcessor} that parses Markdown references.
 */
public class ReferencePostProcessor implements PostProcessor {

    /**
     * The {@link ReferenceVisitor} to use when parsing.
     */
    private final ReferenceVisitor visitor = new ReferenceVisitor();

    /**
     * Processes the given node in the markdown syntax tree.
     */
    @Override
    public Node process(final Node node) {
        node.accept(visitor);
        return node;
    }

}
