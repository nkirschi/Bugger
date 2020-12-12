package tech.bugger.business.util;

import org.commonmark.node.Node;
import org.commonmark.parser.PostProcessor;
import tech.bugger.global.util.Log;

/**
 * {@link PostProcessor} that parses Markdown references.
 */
public class ReferencePostProcessor implements PostProcessor {

    private static final Log log = Log.forClass(ReferencePostProcessor.class);

    /**
     * Processes the given node in the markdown syntax tree.
     */
    @Override
    public Node process(Node node) {
        return null;
    }

}
