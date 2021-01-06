package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import javax.mail.Store;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Topic gateway that gives access to topics stored in a database.
 */
public class TopicDBGateway implements TopicGateway {

    private static final Log log = Log.forClass(TopicDBGateway.class);

    private Connection conn;

    /**
     * Constructs a new topic gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public TopicDBGateway(Connection conn) {
        this.conn = conn;
    }

    /**
     * Parses the given {@link ResultSet} and returns the corresponding {@link Topic}.
     *
     * @param rs The {@link ResultSet} to parse.
     * @return The parsed {@link Topic}.
     * @throws SQLException Some parsing error occurred.
     */
    static Topic getTopicFromResultSet(final ResultSet rs) throws SQLException {
        return new Topic(rs.getInt("id"), rs.getString("title"),
                rs.getString("description"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfReports(Topic topic, boolean showOpenReports, boolean showClosedReports) {
        int numberOfReports = 0;
        if (showOpenReports) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT * AS num_reports FROM \"reports\" WHERE topic = ?, closed_at IS NULL")) {
                ResultSet resultSet = new StatementParametrizer(stmt)
                        .integer(topic.getId()).toStatement().executeQuery();
                int numReports = 0;
                if (resultSet.next()) {
                    numReports = resultSet.getInt("num_reports");
                    numberOfReports =+ numReports;
                }
            } catch (SQLException e) {
                log.error("Error while searching for report by topic.", e);
                throw new StoreException("Error while searching for report by topic.", e);
            }
        }
        if (showClosedReports) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT * AS num_reports FROM \"reports\" WHERE topic = ?, IS NOT NULL")) {
                ResultSet resultSet = new StatementParametrizer(stmt)
                        .integer(topic.getId()).toStatement().executeQuery();
                int numReports = 0;
                if (resultSet.next()) {
                    numReports = resultSet.getInt("num_reports");
                    numberOfReports =+ numReports;
                }
            } catch (SQLException e) {
                log.error("Error while searching for report by topic.", e);
                throw new StoreException("Error while searching for report by topic.", e);
            }
        }
        return numberOfReports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfTopics() {
        // TODO Auto-generated method stub
        return 0;
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
    public Topic getTopicByID(int id) throws NotFoundException {
        Topic topic;

        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"topic\" WHERE id = ?")) {
            ResultSet rs = new StatementParametrizer(stmt).integer(id).toStatement().executeQuery();

            if (rs.next()) {
                topic = getTopicFromResultSet(rs);
            } else {
                log.error("No user with id " + id + " could be found in the database");
                throw new NotFoundException("No user with id " + id + " could be found in the database.");
            }
        } catch (SQLException e) {
            log.error("Error while searching for user with id " + id, e);
            throw new StoreException("Error while searching for user with id " + id, e);
        }
        return topic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Topic> getSelectedTopics(Selection selection) {
        // TODO Auto-generated method stub
        return null;
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
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM \"topic\" WHERE id = ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .integer(topic.getId()).toStatement().executeQuery();
        } catch (SQLException e) {
            log.error("Error while deleting the topic with id " + topic.getId(), e);
            throw new StoreException("Error while deleting the topic with id " + topic.getId(), e);
        }

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
