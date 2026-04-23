package db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection — loads database credentials from db.properties (never hardcoded).
 *
 * Every DAO uses try-with-resources on getConnection(), so each call gets a
 * fresh Connection that is closed automatically. This avoids the shared-singleton
 * race condition and stale-connection issues of the previous design.
 */
public class DBConnection {

    private static final String url;
    private static final String user;
    private static final String password;

    static {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new RuntimeException(
                    "db.properties not found on classpath. " +
                    "Make sure src/main/resources/db.properties exists.");
            }
            props.load(in);
            String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            Class.forName(driver);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to initialise DBConnection: " + e.getMessage(), e);
        }
        Properties loaded = new Properties();
        try (InputStream in2 = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {
            loaded.load(in2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        url      = loaded.getProperty("db.url");
        user     = loaded.getProperty("db.user");
        password = loaded.getProperty("db.password");
    }

    private DBConnection() {}

    /**
     * Returns a fresh Connection per call.
     * Always use in try-with-resources:
     *   try (Connection conn = DBConnection.getConnection(); ...) { }
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
