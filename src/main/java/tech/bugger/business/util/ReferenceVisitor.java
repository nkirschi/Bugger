package tech.bugger.business.util;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Link;
import org.commonmark.node.Text;
import tech.bugger.global.util.Log;

/**
 * Renderer for custom Markdown reference syntax.
 * <ul>
 *     <li><code>#reportID</code> turns into a link to the corresponding report</li>
 *     <li><code>##postID</code> turns into a link to the corresponding post</li>
 *     <li><code>@username</code> turns into a link to the correspoding user's profile page</li>
 * </ul>
 */
public class ReferenceVisitor extends AbstractVisitor {

    private static final Log log = Log.forClass(ReferenceVisitor.class);

    /**
     * Searches for references in {@link Text} nodes, extracts them and renders them as {@link Link} nodes.
     *
     * @param text The {@link Text} node to parse.
     */
    @Override
    public void visit(Text text) {

    }
}
