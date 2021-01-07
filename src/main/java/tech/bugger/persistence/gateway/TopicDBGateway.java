package tech.bugger.persistence.gateway;

import com.ocpsoft.pretty.faces.util.StringUtils;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Topic gateway that gives access to topics stored in a database.
 */
public class TopicDBGateway implements TopicGateway {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(TopicDBGateway.class);

    /**
     * Database connection used by this gateway.
     */
    private final Connection conn;

    /**
     * Constructs a new topic gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public TopicDBGateway(final Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countReports(final Topic topic, final boolean showOpenReports, final boolean showClosedReports) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countTopics() throws StoreException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM topic;")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new InternalError("Could not count the number of topics.");
            }
        } catch (SQLException e) {
            log.error("Error while retrieving number of topics.", e);
            throw new StoreException("Error while retrieving number of topics.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countModerators(final Topic topic) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countBannedUsers(final Topic topic) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Topic findTopic(final int id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Topic> selectTopics(final Selection selection) {
        if (selection == null) {
            log.error("Error when trying to get topics with selection null.");
            throw new IllegalArgumentException("Selection cannot be null.");
        } else if (StringUtils.isBlank(selection.getSortedBy())) {
            log.error("Error when trying to get topics sorted by nothing.");
            throw new IllegalArgumentException("Cannot sort by nothing.");
        }

        String sql = "SELECT t.*, l.last_activity FROM topic AS t"
                + " LEFT OUTER JOIN topic_last_activity AS l ON t.id = l.topic"
                + " ORDER BY " + selection.getSortedBy() + (selection.isAscending() ? " ASC" : " DESC")
                + " LIMIT " + selection.getPageSize().getSize()
                + " OFFSET " + selection.getCurrentPage() * selection.getPageSize().getSize() + ";";

        List<Topic> selectedTopics = new ArrayList<>(Math.max(0, selection.getTotalSize()));
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ZonedDateTime lastActivity = null;
                if (rs.getTimestamp("last_activity") != null) {
                    lastActivity = rs.getTimestamp("last_activity").toInstant().atZone(ZoneId.systemDefault());
                }
                selectedTopics.add(new Topic(rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                        lastActivity));
            }
        } catch (SQLException e) {
            log.error("Error while retrieving topics with " + selection + ".", e);
            throw new StoreException("Error while retrieving topics with " + selection + ".", e);
        }

        return selectedTopics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTopic(final Topic topic) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTopic(final Topic topic) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTopic(final Topic topic) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void banUser(final Topic topic, final User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbanUser(final Topic topic, final User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void promoteModerator(final Topic topic, final User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void demoteModerator(final Topic topic, final User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime determineLastActivity(final Topic topic) throws NotFoundException {
        if (topic == null) {
            log.error("Error when trying to determine last activity in topic null.");
            throw new IllegalArgumentException("Topic must not be null!");
        } else if (topic.getId() == null) {
            log.error("Error when trying to determine last activity in topic with ID null.");
            throw new IllegalArgumentException("Topic ID must not be null!");
        }

        ZonedDateTime lastActivity = null;
        String sql = "SELECT * FROM topic_last_activity WHERE topic =" + topic.getId();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (rs.getTimestamp("last_activity") != null) {
                    lastActivity = rs.getTimestamp("last_activity").toInstant().atZone(ZoneId.systemDefault());
                }
            } else {
                log.error("Topic " + topic + " could not be found when trying to determine last activity.");
                throw new NotFoundException("Topic " + topic
                        + " could not be found when trying to determine last activity.");
            }
        } catch (SQLException e) {
            log.error("Error while determining last activity in topic " + topic, e);
            throw new StoreException("Error while determining last activity in topic " + topic, e);
        }

        return lastActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countSubscribers(final Topic topic) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countPosts(final Topic topic) {
        // TODO Auto-generated method stub
        return 0;
    }

}
