package selenium;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import tech.bugger.persistence.util.ConnectionPool;
import tech.bugger.persistence.util.PropertiesReader;

@Order(1)
public class TestDBCleaner {

    @Test
    public void run() {
        // retrieve DB scripts
        String clearSQL;
        try {
            clearSQL = Files.readString(Paths.get("src/main/webapp/WEB-INF/clear.sql"));
        } catch (IOException e) {
            throw new AssertionError("Error when reading SQL scripts from disk.", e);
        }

        // initialize connection pool
        ConnectionPool connectionPool;
        try {
            PropertiesReader config = new PropertiesReader(ClassLoader.getSystemResourceAsStream("selenium.properties"));
            Properties props = new Properties();
            props.load(ClassLoader.getSystemResourceAsStream("selenium-jdbc.properties"));


            connectionPool = new ConnectionPool(config.getString("DB_DRIVER"),
                    config.getString("DB_URL"),
                    props, 1, 1, 2000);
        } catch (IOException e) {
            throw new AssertionError("Error when reading JDBC properties from disk.", e);
        }

        // reset DB
        Connection conn = connectionPool.getConnection();
        applyScript(conn, clearSQL);
        connectionPool.releaseConnection(conn);
        connectionPool.shutdown();
    }

    private static void applyScript(Connection conn, String sql) {
        try {
            Statement stmt = conn.createStatement();
            Scanner scanner = new Scanner(sql);
            scanner.useDelimiter(";(?=(?:[^$]*\\$\\$[^$]*\\$\\$)*[^$]*\\Z)");
            while (scanner.hasNext()) {
                stmt.addBatch(scanner.next());
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new InternalError("Applying SQL script failed.", e);
        }
    }

}