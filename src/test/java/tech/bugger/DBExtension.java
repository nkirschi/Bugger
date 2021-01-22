package tech.bugger;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DBExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static EmbeddedPostgres pg;
    private static String setupSQL;
    private static String eraseSQL;
    private static String minimalSQL;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        pg = EmbeddedPostgres.builder().start();
        setupSQL = Files.readString(Paths.get("src/main/webapp/WEB-INF/setup.sql"));
        eraseSQL = Files.readString(Paths.get("src/main/webapp/WEB-INF/erase.sql"));
        minimalSQL = Files.readString(Paths.get("testdata/minimal.sql"));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        applyScript(setupSQL);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        applyScript(eraseSQL);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        pg.close();
    }

    public static Connection getConnection() throws Exception {
        return pg.getPostgresDatabase().getConnection();
    }

    public static void insertMinimalTestData() {
        applyScript(minimalSQL);
    }

    public static void emptyDatabase() {
        applyScript(eraseSQL);
        applyScript(setupSQL);
    }

    private static void applyScript(String sql) {
        try (Connection conn = pg.getPostgresDatabase().getConnection();
             Statement stmt = conn.createStatement()) {

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
