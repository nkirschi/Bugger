package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Report;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
class NotificationDBGatewayTest {

    private NotificationDBGateway gateway;

    private Connection connection;

    private Notification notification;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new NotificationDBGateway(connection);
        notification = new Notification();
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testCreate() {
        DBExtension.insertMinimalTestData();
        notification.setSent(false);
        notification.setRead(false);
        notification.setType(Notification.Type.NEW_POST);
        notification.setRecipientID(1);
        notification.setActuatorID(2);
        notification.setTopic(1);
        notification.setReportID(100);
        notification.setPostID(100);
        assertDoesNotThrow(() -> gateway.create(notification));
    }
}