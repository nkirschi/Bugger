package tech.bugger.business.util;

import org.commonmark.node.Text;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MarkdownTest {

    @Test
    public void testNull() {
        String html = MarkdownHandler.toHtml(null);
        assertEquals("", html);
    }

    @Test
    public void testEmptyString() {
        String html = MarkdownHandler.toHtml("");
        assertEquals("", html);
    }

    @Test
    public void testNormalString() {
        String md = "I am a normal String. What do you want to do today?";
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p>" + md + "</p>\n", html);
    }

    @Test
    public void testHeaderString() {
        String keyword = "I am a Header. Do you want to go out with me?";
        String md = "# " + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<h1>" + keyword + "</h1>\n", html);
    }

    @Test
    public void testSubHeaderString() {
        String keyword = "I am a Header. Do you want to go out with me?";
        String md = "## " + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<h2>" + keyword + "</h2>\n", html);
    }

    @Test
    public void testSubSubHeaderString() {
        String keyword = "I am a Header. Do you want to go out with me?";
        String md = "### " + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<h3>" + keyword + "</h3>\n", html);
    }

    @Test
    public void testLinkString() {
        String keyword = "I am a Header. Do you want to go out with me?";
        String linkTo = "https://example.com/";
        String md = "[" + keyword + "](" + linkTo + ")";
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p><a href=\"" + linkTo + "\">" + keyword + "</a></p>\n", html);
    }

    @Test
    public void testCombinedString() {
        String keyword = "I am a Header. Do you want to go out with me?";
        String linkTo = "https://example.com/";
        String md = "## [" + keyword + "](" + linkTo + ")";
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<h2><a href=\"" + linkTo + "\">" + keyword + "</a></h2>\n", html);
    }

    @Test
    public void testUserString() {
        String keyword = "hyperspeeed";
        String md = "@" + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p><a href=\"/user/" + keyword + "\" title=\"User " + keyword + "\">"
                + "@" + keyword + "</a></p>\n", html);
    }

    @Test
    public void testReportString() {
        String keyword = "456";
        String md = "#" + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p><a href=\"/report/" + keyword + "\" title=\"Report " + keyword + "\">"
                + "#" + keyword + "</a></p>\n", html);
    }

    @Test
    public void testPostString() {
        String keyword = "456";
        String md = "##" + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p><a href=\"/post/" + keyword + "\" title=\"Post " + keyword + "\">"
                + "##" + keyword + "</a></p>\n", html);
    }

    @Test
    public void testNotValidPostString() {
        String keyword = "test";
        String md = "#" + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p>#test</p>\n", html);
    }

    @Test
    public void testStringAfterUserString() {
        String keyword = "test";
        String postfix = ", hhh";
        String md = "@" + keyword + postfix;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p><a href=\"/user/" + keyword + "\" title=\"User " + keyword + "\">@" + keyword + "</a>"
                + postfix + "</p>\n", html);
    }

    @Test
    public void testStringWithoutReferencesInVisitor() {
        String md = "Hello there! General Kenobi!";
        Text text = new Text(md);
        ReferenceVisitor visitor = new ReferenceVisitor();
        visitor.parseText(text, "@", "/user/", "User ");
        assertAll(() -> assertNull(text.getNext()), () -> assertNull(text.getPrevious()));
    }

}
