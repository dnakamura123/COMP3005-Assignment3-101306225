package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class Database {

    public static Connection getConnection() throws SQLException {
        // 1) Try environment variables
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");

        if (url != null && user != null && pass != null) {
            return DriverManager.getConnection(url, user, pass);
        }

        // 2) Fallback to application.properties on the classpath
        try (var in = Database.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new SQLException("application.properties not found and DB_* env vars not set.");
            }
            Properties props = new Properties();
            props.load(in);

            String pUrl  = props.getProperty("db.url");
            String pUser = props.getProperty("db.user");
            String pPass = props.getProperty("db.password");

            if (pUrl == null || pUser == null || pPass == null) {
                throw new SQLException("db.url/db.user/db.password missing in application.properties.");
            }
            return DriverManager.getConnection(pUrl, pUser, pPass);
        } catch (Exception e) {
            throw new SQLException("Failed to load DB config: " + e.getMessage(), e);
        }
    }
}
