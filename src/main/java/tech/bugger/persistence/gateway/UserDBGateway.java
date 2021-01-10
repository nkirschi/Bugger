package tech.bugger.persistence.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.ZoneId;
import java.util.List;
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
     * Formats the given {@link StatementParametrizer} using the given {@link User} in the order specified in the file
     * {@code setup.sql} (without the ID and registration date).
     *
     * @param parametrizer The {@link StatementParametrizer} to format.
     * @param user         The {@link User} that should be written into the {@code parametrizer}.
     * @return The parametrizer with the given {@code user} inserted.
     * @throws SQLException Some parsing error occurred.
     */
    static StatementParametrizer storeUserInStatement(final StatementParametrizer parametrizer, final User user)
            throws SQLException {
        return parametrizer
                .string(user.getUsername())
                .string(user.getPasswordHash())
                .string(user.getPasswordSalt())
                .string(user.getHashingAlgorithm())
                .string(user.getEmailAddress())
                .string(user.getFirstName())
                .string(user.getLastName())
                .bytes(user.getAvatar().get())
                .bytes(user.getAvatarThumbnail())
                .string(user.getBiography())
                .string(user.getPreferredLanguage().name())
                .object(user.getProfileVisibility(), Types.OTHER)
                .object(user.getForcedVotingWeight(), Types.INTEGER)
                .bool(user.isAdministrator());
    }

    /**
     * Parses the given {@link ResultSet} and returns the corresponding {@link User}.
     *
     * @param rs The {@link ResultSet} to parse.
     * @return The parsed {@link User}.
     * @throws SQLException Some parsing error occurred.
     */
    static User getUserFromResultSet(final ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("username"),
                rs.getString("password_hash"), rs.getString("password_salt"),
                rs.getString("hashing_algorithm"), rs.getString("email_address"),
                rs.getString("first_name"), rs.getString("last_name"),
                new Lazy<>(rs.getBytes("avatar")), rs.getBytes("avatar_thumbnail"),
                rs.getString("biography"), Language.valueOf(rs.getString("preferred_language").toUpperCase()),
                User.ProfileVisibility.valueOf(rs.getString("profile_visibility").toUpperCase()),
                rs.getTimestamp("registered_at").toLocalDateTime().atZone(ZoneId.systemDefault()),
                rs.getObject("forced_voting_weight", Integer.class), rs.getBoolean("is_admin"));
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
        User user;

        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"user\" WHERE id = ?")) {
            ResultSet rs = new StatementParametrizer(stmt).integer(id).toStatement().executeQuery();

            if (rs.next()) {
                user = getUserFromResultSet(rs);
            } else {
                log.error("No user with id " + id + " could be found in the database");
                throw new NotFoundException("No user with id " + id + " could be found in the database.");
            }
        } catch (SQLException e) {
            log.error("Error while searching for user with id " + id, e);
            throw new StoreException("Error while searching for user with id " + id, e);
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByUsername(final String username) throws NotFoundException {
        User user;

        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"user\" WHERE username = ?")) {
            ResultSet rs = new StatementParametrizer(stmt).string(username).toStatement().executeQuery();

            if (rs.next()) {
                user = getUserFromResultSet(rs);
            } else {
                log.error("No user with the given username could be found in the database");
                throw new NotFoundException("No user with the given username could be found in the database.");
            }
        } catch (SQLException e) {
            log.error("Error while searching for user by username.", e);
            throw new StoreException("Error while searching for user by username.", e);
        }

        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByEmail(final String emailAddress) throws NotFoundException {
        User user;

        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"user\" WHERE email_address = ?")) {
            ResultSet rs = new StatementParametrizer(stmt).string(emailAddress).toStatement().executeQuery();

            if (rs.next()) {
                user = getUserFromResultSet(rs);
            } else {
                log.error("No user with the given e-mail address could be found in the database");
                throw new NotFoundException("No user with the given e-mail address could be found in the database.");
            }
        } catch (SQLException e) {
            log.error("Error while searching for user by e-mail address.", e);
            throw new StoreException("Error while searching for user by e-mail address.", e);
        }

        return user;
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
        if (!user.getAvatar().isPresent()) {
            throw new IllegalArgumentException("Avatar must be present!");
        }

        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO \"user\" "
                        + "(username, password_hash, password_salt, hashing_algorithm, email_address, first_name, "
                        + "last_name, avatar, avatar_thumbnail, biography, preferred_language, profile_visibility, "
                        + "forced_voting_weight, is_admin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {

            storeUserInStatement(new StatementParametrizer(stmt), user)
                    .toStatement().executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                user.setId(rs.getInt("id"));
                user.setRegistrationDate(rs.getTimestamp("registered_at").toLocalDateTime()
                        .atZone(ZoneId.systemDefault()));
            } else {
                log.error("Couldn't read new user data.");
                throw new StoreException("Couldn't read new user data.");
            }
        } catch (SQLException e) {
            log.error("Couldn't create the user due to a database error.", e);
            throw new StoreException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(final User user) throws NotFoundException {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID may not be null!");
        } else if (!user.getAvatar().isPresent()) {
            throw new IllegalArgumentException("Avatar must be present!");
        }

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE \"user\" SET "
                        + "username = ?, password_hash = ?, password_salt = ?, hashing_algorithm = ?, "
                        + "email_address = ?, first_name = ?, last_name = ?, avatar = ?, avatar_thumbnail = ?, "
                        + "biography = ?, preferred_language = ?, profile_visibility = ?, "
                        + "forced_voting_weight = ?, is_admin = ? "
                        + "WHERE id = ?",
                Statement.RETURN_GENERATED_KEYS)) {

            StatementParametrizer parametrizer = storeUserInStatement(new StatementParametrizer(stmt), user);
            int changedRows = parametrizer
                    .integer(user.getId())
                    .toStatement().executeUpdate();

            if (changedRows == 0) {
                log.error("No user with id " + user.getId() + " could be found in the database.");
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
    public void deleteUser(final User user) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM \"user\" WHERE id = ?;")) {
            int modified = new StatementParametrizer(stmt).integer(user.getId()).toStatement().executeUpdate();
            if (modified == 0) {
                log.debug("The user with the id " + user.getId() + " has already been deleted.");
                throw new NotFoundException("The user with the id " + user.getId() + " has already been deleted.");
            }
        } catch (SQLException e) {
            log.error("Error while deleting the user with id " + user.getId(), e);
            throw new StoreException("Error while deleting the user with id " + user.getId(), e);
        }
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
    public int getNumberOfPosts(final User user) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT num_posts FROM user_num_posts WHERE "
                + "author = ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .integer(user.getId())
                    .toStatement().executeQuery();
            if (rs.next()) {
                return rs.getInt("num_posts");
            } else {
                log.error("No posts could be found for the user with id " + user.getId());
                throw new NotFoundException("No posts could be found for the user with id " + user.getId());
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
