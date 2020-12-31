package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.List;

/**
 * User gateway that gives access to user stored in a database.
 */
public class UserDBGateway implements UserGateway {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(UserDBGateway.class);

    /**
     * The database connection used by this gateway.
     */
    private final Connection conn;

    /**
     * Constructs a new user gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public UserDBGateway(final Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isModerator(final User user, final Topic topic) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAdminEmails() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByID(final int id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByUsername(final String username) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"user\" WHERE username = ?")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string(username)
                    .toStatement().executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"),
                        rs.getString("password_hash"), rs.getString("password_salt"),
                        rs.getString("hashing_algorithm"), rs.getString("email_address"),
                        rs.getString("first_name"), rs.getString("last_name"),
                        new Lazy<>(rs.getBytes("avatar")), rs.getBytes("avatar_thumbnail"),
                        rs.getString("biography"),
                        Language.valueOf(rs.getString("preferred_language").toUpperCase()),
                        User.ProfileVisibility.valueOf(rs.getString("profile_visibility").toUpperCase()),
                        rs.getTimestamp("registered_at").toLocalDateTime().atZone(ZoneId.systemDefault()),
                        rs.getObject("forced_voting_weight", Integer.class),
                        rs.getBoolean("is_admin"));
            } else {
                log.error("No user with username " + username + "could be found in the database");
                throw new NotFoundException("No user with username" + username + "could be found in the database");
            }
        } catch (SQLException e) {
            log.error("Error while searching for user with username " + username, e);
            throw new StoreException("Error while searching for user with username " + username, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getSelectedModerators(final Topic topic, final Selection selection) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getSelectedBannedUsers(final Topic topic, final Selection selection) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createUser(final User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(final User user) {
        // TODO Auto-generated method stub
        // For Lazy attributes: check if they're there first, if not, don't do anything
        // with them
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(final User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getSubscribersOf(final User user) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getSubscribersOf(final Report report) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getSubscribersOf(final Topic topic) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfPosts(final User user) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getVotingWeight(final User user) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBanned(final User user, final Topic topic) {
        // TODO Auto-generated method stub
        return false;
    }

}
