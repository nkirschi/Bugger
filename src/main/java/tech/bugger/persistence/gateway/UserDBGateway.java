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
import java.sql.Types;
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
    public int getNumberOfAdmins() {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(u.id) AS num_admins FROM \"user\" AS u "
                + "WHERE u.is_admin = true;")) {
            ResultSet resultSet = stmt.executeQuery();
            int numAdmins = 0;
            if (resultSet.next()) {
                numAdmins = resultSet.getInt("num_admins");
            }
            return numAdmins;
        } catch (SQLException e) {
            log.error("Error while counting the number of administrators", e);
            throw new StoreException("Error while counting the number of administrators", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByID(final int id) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"user\" WHERE id = ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .integer(id)
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
                        rs.getObject("forced_voting_weight", Integer.class), rs.getBoolean("is_admin"));
            } else {
                log.error("No user with id " + id + " could be found in the database");
                throw new NotFoundException("No user with id " + id + " could be found in the database.");
            }
        } catch (SQLException e) {
            log.error("Error while searching for user with id " + id, e);
            throw new StoreException("Error while searching for user with id " + id, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByUsername(final String username) {
        // TODO Auto-generated method stub
        return null;
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
    public void updateUser(final User user) throws NotFoundException {
        Lazy<byte[]> avatar = user.getAvatar();

        if (avatar == null) {
            throw new InternalError("The user's avatar should not be null!");
        }

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE \"user\" SET "
                + "username = ?, password_hash = ?, password_salt = ?, hashing_algorithm = ?, "
                + "email_address = ?, first_name = ?, last_name = ?, avatar_thumbnail = ?, "
                + "biography = ?, preferred_language = ?, profile_visibility = ?, "
                + "forced_voting_weight = ?, is_admin = ?, avatar = ? WHERE id = ?;")) {
            int modified = new StatementParametrizer(stmt)
                    .string(user.getUsername())
                    .string(user.getPasswordHash())
                    .string(user.getPasswordSalt())
                    .string(user.getHashingAlgorithm())
                    .string(user.getEmailAddress())
                    .string(user.getFirstName())
                    .string(user.getLastName())
                    .bytes(user.getAvatarThumbnail())
                    .string(user.getBiography())
                    .string(user.getPreferredLanguage().name())
                    .object(user.getProfileVisibility(), Types.OTHER)
                    .object(user.getForcedVotingWeight(), Types.INTEGER)
                    .bool(user.isAdministrator())
                    .bytes(avatar.get())
                    .integer(user.getId())
                    .toStatement().executeUpdate();

            if (modified == 0) {
                log.error("No user with id " + user.getId() + " could be found in the database");
                throw new NotFoundException("No user with id " + user.getId() + " could be found in the database.");
            }
        } catch (SQLException e) {
            log.error("Error while updating the user with id " + user.getId(), e);
            throw new StoreException("Error while updating user with id " + user.getId(), e);
        }
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
        try (PreparedStatement stmt = conn.prepareStatement("SELECT num_posts FROM user_num_posts WHERE "
                + "author = ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .integer(user.getId())
                    .toStatement().executeQuery();
            if (rs.next()) {
                return rs.getInt("num_posts");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            log.error("Error while searching for number of posts of the user with id " + user.getId(), e);
            throw new StoreException("Error while searching for number of posts of the user with id "
                    + user.getId(), e);
        }
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
