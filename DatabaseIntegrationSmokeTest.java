package application;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;


public final class DatabaseIntegrationSmokeTest {
    private DatabaseIntegrationSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        DatabaseConnection.configureDefaultAndVerify();
        DatabaseInitializer.initialize();
        try (Connection connection = DatabaseConnection.open();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT 1")) {
            require(result.next() && result.getInt(1) == 1,
                    "MySQL SELECT 1 failed.");
            DatabaseMetaData metadata = connection.getMetaData();
            require(tableExists(metadata, connection.getCatalog(), "players"),
                    "players table missing.");
            require(tableExists(metadata, connection.getCatalog(), "high_scores"),
                    "high_scores table missing.");
            require(tableExists(metadata, connection.getCatalog(), "player_stats"),
                    "player_stats table missing.");
            require(columnExists(metadata, connection.getCatalog(),
                    "high_scores", "duration_seconds"),
                    "high_scores migration missing.");
            require(columnExists(metadata, connection.getCatalog(),
                    "players", "selected_mascot"),
                    "players profile migration missing.");
        } finally {
            DatabaseConnection.clear();
        }
        System.out.println("DATABASE_INTEGRATION_TEST=PASS");
    }

    private static boolean tableExists(DatabaseMetaData metadata, String catalog,
                                       String table) throws Exception {
        try (ResultSet result = metadata.getTables(catalog, null, table,
                new String[] {"TABLE"})) {
            return result.next();
        }
    }

    private static boolean columnExists(DatabaseMetaData metadata, String catalog,
                                        String table, String column) throws Exception {
        try (ResultSet result = metadata.getColumns(catalog, null, table, column)) {
            return result.next();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
