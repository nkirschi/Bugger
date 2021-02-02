package tech.bugger.system;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BeaTest {

    public static String getRegistrationURL(String username) {
        try {
            URL url = new URL("http://restmail.net/mail/" + username);
            String mails = new String(url.openStream().readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = Pattern.compile(".*\"text\":.*?(https?://.*?)\\\\n.*?\",", Pattern.DOTALL).matcher(mails);
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new AssertionError("No registration URL in e-mails " + mails);
            }
        } catch (IOException e) {
            throw new AssertionError("Error when retrieving e-mails of user " + username);
        }
    }

    public static boolean clearEmails(String username) {
        try {
            URL url = new URL("http://restmail.net/mail/" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            throw new AssertionError("Error when clearing e-mails of user " + username);
        }
    }

    public static void main(String[] args) {
        System.out.println(clearEmails("beatest"));
    }

}
