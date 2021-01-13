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
import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void testCreateBulk() {
        DBExtension.insertMinimalTestData();
        List<Notification> notifications = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            notifications.add(new Notification(null, 2, 1, Notification.Type.NEW_POST, null, false, false, 1, 100, 100));
        }
        assertDoesNotThrow(() -> gateway.createNotificationBulk(notifications));
        assertDoesNotThrow(() -> gateway.find(1));
        assertDoesNotThrow(() -> gateway.find(20));
    }
}