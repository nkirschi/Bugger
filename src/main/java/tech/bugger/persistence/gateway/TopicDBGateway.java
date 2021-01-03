package tech.bugger.persistence.gateway;

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
    private Connection conn;

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
    public int getNumberOfReports(Topic topic, boolean showOpenReports, boolean showClosedReports) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfTopics() throws StoreException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(id) FROM topic")) {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            log.error("Error while retrieving number of topics.", e);
            throw new StoreException("Error while retrieving number of topics.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfModerators(Topic topic) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfBannedUsers(Topic topic) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Topic getTopicByID(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Topic> getSelectedTopics(final Selection selection) throws NotFoundException {
        if (selection == null) {
            log.error("Error when trying to get topics with selection null.");
            throw new IllegalArgumentException("Selection cannot be null.");
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM topic");
        if (selection.getSortedBy() != null && !selection.getSortedBy().equals("")) {
            sql.append(" ORDER BY ").append(selection.getSortedBy());
            if (selection.isAscending()) {
                sql.append(" ASC");
            } else {
                sql.append(" DESC");
            }
        }
        sql.append(" LIMIT ").append(selection.getPageSize().getSize());
        sql.append(" OFFSET ").append(selection.getCurrentPage() * selection.getPageSize().getSize()).append(";");

        List<Topic> selectedTopics = new ArrayList<>(Math.max(0, selection.getTotalSize()));
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Topic topic = new Topic(rs.getInt("id"), rs.getString("title"), rs.getString("description"));
                selectedTopics.add(topic);
            }
        } catch (SQLException e) {
            log.error("Error while retrieving topics with " + selection + ".", e);
            throw new StoreException("Error while retrieving topics with " + selection + ".", e);
        }
        if (selectedTopics.isEmpty()) {
            log.error("Topics with " + selection + " not found.");
            throw new NotFoundException("Topics with " + selection + " not found.");
        } else {
            return selectedTopics;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTopic(Topic topic) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTopic(Topic topic) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTopic(Topic topic) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void banUser(Topic topic, User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbanUser(Topic topic, User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeModerator(Topic topic, User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeModerator(Topic topic, User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime getLastChangeTimestamp(Topic topic) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfSubscribers(Topic topic) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfPosts(Topic topic) {
        // TODO Auto-generated method stub
        return 0;
    }

}
