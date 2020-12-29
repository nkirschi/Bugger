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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * User gateway that gives access to user stored in a database.
 */
public class UserDBGateway implements UserGateway {

    private static final Log log = Log.forClass(UserDBGateway.class);

    private Connection conn;

    /**
     * Constructs a new user gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public UserDBGateway(Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isModerator(User user, Topic topic) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfAdmins() throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(u.id) AS num_admins FROM \"user\" AS u "
                + "WHERE u.is_administrator = true")) {
            ResultSet resultSet = stmt.executeQuery();
            int numAdmins = resultSet.getInt("num_admins");
            if (numAdmins == 0) {
                log.error("No administrators could be found in the database");
                throw new NotFoundException("No administrators could be found in the database.");
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
    public User getUserByID(int id) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"user\" AS u WHERE u.id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String prefLanguage = rs.getString("preferred_language");
                String visibility = rs.getString("profile_visibility");

                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setPasswordSalt(rs.getString("password_salt"));
                user.setHashingAlgorithm(rs.getString("hashing_algorithm"));
                user.setEmailAddress(rs.getString("email_address"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setAvatar(new Lazy<>(rs.getBytes("avatar")));
                user.setAvatarThumbnail(rs.getBytes("avatar_thumbnail"));
                user.setBiography(rs.getString("biography"));
                user.setRegistrationDate((ZonedDateTime) rs.getObject("registered_at"));
                user.setForcedVotingWeight(rs.getInt("forced_voting_weight"));
                user.setAdministrator(rs.getBoolean("is_admin"));

                if (prefLanguage.equalsIgnoreCase("german")) {
                    user.setPreferredLanguage(Language.GERMAN);
                } else {
                    user.setPreferredLanguage(Language.ENGLISH);
                }

                if (visibility.equalsIgnoreCase("full")) {
                    user.setProfileVisibility(User.ProfileVisibility.FULL);
                } else {
                    user.setProfileVisibility(User.ProfileVisibility.MINIMAL);
                }

                return user;
            } else {
                log.error("No user could be found in the database");
                throw new NotFoundException("No user could be found in the database.");
            }
        } catch (SQLException e) {
            log.error("Error while searching for user", e);
            throw new StoreException("Error while searching for user", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByUsername(String username) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getSelectedModerators(Topic topic, Selection selection) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getSelectedBannedUsers(Topic topic, Selection selection) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createUser(User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(User user) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE \"user\" SET id = ?, username = ?, "
                + "password_hash = ?, password_salt = ?, hashing_algorithm = ?, email_address = ?, first_name = ?, "
                + "last_name = ?, avatar = ?, avatar_thumbnail = ?, biography = ?, preferred_language = ?, "
                + "profile_visibility = ?, registered_at = ?, forced_voting_weight = ?, is_admin = ?;")) {
            stmt.setInt(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getPasswordSalt());
            stmt.setString(5, user.getHashingAlgorithm());
            stmt.setString(6, user.getFirstName());
            stmt.setString(7, user.getEmailAddress());
            stmt.setString(8, user.getLastName());
            stmt.setBytes(9, user.getAvatar().get());
            stmt.setBytes(10, user.getAvatarThumbnail());
            stmt.setString(11, user.getBiography());
            stmt.setString(12, user.getPreferredLanguage().name());
            stmt.setString(13, user.getProfileVisibility().name());
            stmt.setObject(14, user.getRegistrationDate());
            stmt.setInt(15, user.getForcedVotingWeight());
            stmt.setBoolean(16, user.isAdministrator());
            int modified = stmt.executeUpdate();
            if (modified == 0) {
                log.error("No user could be found in the database.");
                throw new NotFoundException("No user could be found in the database.");
            }
        } catch (SQLException e) {
            log.error("Error while updating user.", e);
            throw new StoreException("Error while updating user.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getSubscribersOf(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getSubscribersOf(Report report) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getSubscribersOf(Topic topic) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfPosts(User user) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT num_posts FROM user_num_posts WHERE "
                + "author = ?;")) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("num_posts");
            } else {
                getUserByID(user.getId());
                return 0;
            }
        } catch (SQLException e) {
            log.error("Error while searching for number of posts.", e);
            throw new StoreException("Error while searching for number of posts.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getVotingWeight(User user) throws NotFoundException {
        int numPosts = getNumberOfPosts(user);
        try (PreparedStatement stmt = conn.prepareStatement("SELECT voting_weight_definition FROM "
                + "system_settings;")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String[] votingWeight = rs.getString("voting_weight_definition").split(",");
                for (int i = 0; i < votingWeight.length; i++) {
                    try {
                        int boundary = Integer.parseInt(votingWeight[i]);
                        if (numPosts <= boundary) {
                            return i;
                        }
                    } catch (NumberFormatException e) {
                        log.error("The voting weight definition could not be parsed to a number");
                        throw new InternalError("The voting weight definition could not be parsed to a number");
                    }
                }
                return votingWeight.length;
            } else {
                log.error("The voting weight definition could not be found in the database.");
                throw new NotFoundException("The voting weight definition could not be found in the database.");
            }
        } catch (SQLException e) {
            log.error("Error while loading the voting weight definition.", e);
            throw new StoreException("Error while loading the voting weight definition.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBanned(User user, Topic topic) {
        // TODO Auto-generated method stub
        return false;
    }

}
