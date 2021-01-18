package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.StoreException;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
class SubscriptionDBGatewayTest {

    private SubscriptionDBGateway gateway;

    private Connection connection;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new SubscriptionDBGateway(connection);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testSubscribeTwice() {
        DBExtension.insertMinimalTestData();
        Topic topic = new Topic();
        topic.setId(1);
        User user = new User();
        user.setId(1);
        assertDoesNotThrow(() -> gateway.subscribe(topic, user));
        assertThrows(DuplicateException.class, () -> gateway.subscribe(topic, user));
    }
}