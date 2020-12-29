package tech.bugger.business.util;

import org.commonmark.node.Node;
import org.commonmark.parser.PostProcessor;
import tech.bugger.global.util.Log;

/**
 * {@link PostProcessor} that parses Markdown references.
 */
public class ReferencePostProcessor implements PostProcessor {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ReferencePostProcessor.class);

    /**
     * The {@link ReferenceVisitor} to use when parsing.
     */
    private final ReferenceVisitor visitor = new ReferenceVisitor();

    /**
     * Processes the given node in the markdown syntax tree.
     */
    @Override
    public Node process(final Node node) {
        log.debug("Parsing node " + node + " with reference support.");
        node.accept(visitor);
        return node;
    }

}
