package tech.bugger.business.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.commonmark.node.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LogExtension.class)
public class MarkdownTest {

    @Test
    public void testHandlerConstructorAccess() throws NoSuchMethodException {
        Constructor<MarkdownHandler> constructor = MarkdownHandler.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Throwable e = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
    }

    @Test
    public void testNull() {
        assertThrows(IllegalArgumentException.class, () -> MarkdownHandler.toHtml(null));
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
        String url = "https://example.com/";
        String md = "[" + keyword + "](" + url + ")";
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p><a href=\"" + url + "\">" + keyword + "</a></p>\n", html);
    }

    @Test
    public void testCombinedHeaderAndLinkString() {
        String keyword = "I am a Header. Do you want to go out with me?";
        String url = "https://example.com/";
        String md = "## [" + keyword + "](" + url + ")";
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<h2><a href=\"" + url + "\">" + keyword + "</a></h2>\n", html);
    }

    @Test
    public void testCombinedUserAndReportReferenceString() {
        String keyword1 = "hyperspeeed";
        String keyword2 = "42069";
        String md = "Hello @" + keyword1 + " and goodbye #" + keyword2 + "!";
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p>Hello <a href=\"/profile?u=" + keyword1 + "\" title=\"" + keyword1 + "\">@" + keyword1 + "</a> and goodbye <a href=\"/report?r=" + keyword2 + "\" title=\"" + keyword2 + "\">#" + keyword2 + "</a>!</p>\n", html);
    }

    @Test
    public void testCombinedHeaderAndPostReferenceString() {
        String keyword = "42069";
        String md = "## Hello ##" + keyword + " and hello world!";
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<h2>Hello <a href=\"/post/" + keyword + "\" title=\"" + keyword + "\">##" + keyword + "</a> and hello world!</h2>\n", html);
    }

    @Test
    public void testUserString() {
        String keyword = "hyperspeeed";
        String md = "@" + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p><a href=\"/profile?u=" + keyword + "\" title=\"" + keyword + "\">"
                + "@" + keyword + "</a></p>\n", html);
    }

    @Test
    public void testTooShortUserString() {
        String keyword = "+dhas";
        String md = "@" + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p>@" + keyword + "</p>\n", html);
    }

    @Test
    public void testTooShortPostString() {
        String keyword = "+423";
        String md = "##" + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p>##" + keyword + "</p>\n", html);
    }

    @Test
    public void testTooShortReportString() {
        String keyword = "+634";
        String md = "#" + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p>#" + keyword + "</p>\n", html);
    }

    @Test
    public void testReportString() {
        String keyword = "456";
        String md = "#" + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p><a href=\"/report?r=" + keyword + "\" title=\"" + keyword + "\">"
                + "#" + keyword + "</a></p>\n", html);
    }

    @Test
    public void testPostString() {
        String keyword = "456";
        String md = "##" + keyword;
        String html = MarkdownHandler.toHtml(md);
        assertEquals("<p><a href=\"/post/" + keyword + "\" title=\"" + keyword + "\">"
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
        assertEquals("<p><a href=\"/profile?u=" + keyword + "\" title=\"" + keyword + "\">@" + keyword + "</a>"
                + postfix + "</p>\n", html);
    }

    @Test
    public void testStringWithoutReferencesInVisitor() {
        String md = "Hello there! General Kenobi!";
        Text text = new Text(md);
        ReferenceVisitor visitor = new ReferenceVisitor();
        visitor.parseText(text, "@", "/profile?u=");
        assertAll(() -> assertNull(text.getNext()), () -> assertNull(text.getPrevious()));
    }

}
