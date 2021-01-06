package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
class PostDBGatewayTest {

    private PostDBGateway gateway;
    private Connection connection;
    private Report testReport = new Report();
    private Selection testSelection;


    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new PostDBGateway(connection);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    public void validSelection() {
        testSelection = new Selection(42, 0, Selection.PageSize.NORMAL, "", true);
    }

    @Test
    public void testSelectPostsOfReportWhenReportIsNull() {
        validSelection();
        assertThrows(IllegalArgumentException.class, () -> gateway.selectPostsOfReport(null, testSelection));
    }

    @Test
    public void testSelectPostsOfReportWhenSelectionIsNull() {
        testReport.setId(42);
        assertThrows(IllegalArgumentException.class, () -> gateway.selectPostsOfReport(testReport, null));
    }

    @Test
    public void testSelectPostsOfReportWhenReportIDIsNull() {
        validSelection();
        assertThrows(IllegalArgumentException.class, () -> gateway.selectPostsOfReport(new Report(), testSelection));
    }


}