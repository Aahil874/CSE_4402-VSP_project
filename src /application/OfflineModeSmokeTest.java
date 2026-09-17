package application;

import java.sql.SQLException;

public final class OfflineModeSmokeTest {
    private OfflineModeSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        DatabaseConnection.clear();
        DatabaseConnection.enableOfflineMode();
        require(DatabaseConnection.isOfflineMode(), "Offline mode was not enabled.");
        require(!DatabaseConnection.isConfigured(), "Offline mode must not report a live database.");

        GameSession.start("OFFLINE PLAYER");
        require("OFFLINE PLAYER".equals(GameSession.getPlayerName()),
                "Offline player session did not start.");
        require(GameSession.getPlayerLineup() != null,
                "Offline player lineup was not generated.");
        require(GameSession.getRivalLineup() != null,
                "Offline rival lineup was not generated.");

        boolean rejectedDatabaseAccess = false;
        try {
            DatabaseConnection.open();
        } catch (SQLException expected) {
            rejectedDatabaseAccess = true;
        }
        require(rejectedDatabaseAccess,
                "Offline mode must not silently attempt database access.");

        DatabaseConnection.clear();
        System.out.println("OFFLINE_GAME_SESSION=PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
