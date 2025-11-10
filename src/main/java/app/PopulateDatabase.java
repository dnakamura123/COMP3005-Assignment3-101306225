package app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PopulateDatabase {
    public static void main(String[] args) {
        System.out.println("=== PopulateDatabase starting ===");
        try (Connection conn = Database.getConnection(); Statement st = conn.createStatement()) {
            String schema = Files.readString(Path.of("sql/schema.sql"));
            String seed   = Files.readString(Path.of("sql/seed.sql"));

            st.execute(schema);
            System.out.println("Applied schema.sql");

            st.execute(seed);
            System.out.println("Applied seed.sql");

            System.out.println("=== PopulateDatabase finished ===");
        } catch (SQLException se) {
            System.err.println("SQL error: " + se.getMessage());
            se.printStackTrace();
        } catch (Exception e) {
            System.err.println("General error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
