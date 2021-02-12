package tech.bugger.global.transfer;

import org.junit.jupiter.api.Test;
import tech.bugger.DTOVerifier;

public class DTOTest {

    @Test
    public void testAttachment() {
        DTOVerifier.forClass(Attachment.class).verify();
    }

    @Test
    public void testAuthorship() {
        DTOVerifier.forClass(Authorship.class)
                .setTarget(new Authorship(null, null, null, null))
                .verify();
    }

    @Test
    public void testConfiguration() {
        DTOVerifier.forClass(Configuration.class)
                .setTarget(new Configuration(false, false, null, null, 0, null))
                .verify();
    }

    @Test
    public void testMetadata() {
        DTOVerifier.forClass(Metadata.class)
                .setTarget(new Metadata(null))
                .verify();
    }

    @Test
    public void testNotification() {
        DTOVerifier.forClass(Notification.class).verify();
    }

    @Test
    public void testOrganization() {
        DTOVerifier.forClass(Organization.class)
                .setTarget(new Organization(null, null, null, null, null, null))
                .verify();
    }

    @Test
    public void testPost() {
        DTOVerifier.forClass(Post.class)
                .setTarget(new Post(0, null, 0, null, null))
                .verify();
    }

    @Test
    public void testReport() {
        DTOVerifier.forClass(Report.class).verify();
    }

    @Test
    public void testReportCriteria() {
        DTOVerifier.forClass(ReportCriteria.class)
                .setTarget(new ReportCriteria(null, null, null))
                .verify();
    }

    @Test
    public void testSelection() {
        DTOVerifier.forClass(Selection.class)
                .setTarget(new Selection(0, 0, null, null, false))
                .verify();
    }

    @Test
    public void testToken() {
        DTOVerifier.forClass(Token.class)
                .setTarget(new Token(null, null, null, null, null))
                .verify();
    }

    @Test
    public void testTopic() {
        DTOVerifier.forClass(Topic.class).verify();
    }

    @Test
    public void testTopReport() {
        DTOVerifier.forClass(TopReport.class).verify();
    }

    @Test
    public void testTopUser() {
        DTOVerifier.forClass(TopUser.class).verify();
    }

    @Test
    public void testUser() {
        DTOVerifier.forClass(User.class).verify();
    }

}
