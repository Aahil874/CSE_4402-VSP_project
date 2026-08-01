package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
public final class BackendRoundTripSmokeTest {
    private BackendRoundTripSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        DatabaseConnection.configureAndVerify(
                "localhost", "3306", "mascot_baseball_grand_prix");
        DatabaseInitializer.initialize();
        String player = "QA_" + Long.toString(System.nanoTime()).substring(5, 15);
        try (Connection connection = DatabaseConnection.open()) {
            connection.setAutoCommit(false);
            try {
                PlayerDAO playerDAO = new PlayerDAO();
                HighScoreDAO historyDAO = new HighScoreDAO();
                PlayerStatsDAO statsDAO = new PlayerStatsDAO();
                HighScore result = new HighScore(
                        player, 7, 3, 5, 1, 3, 4,
                        Mascot.NOVA_NEKO, GameSettings.Difficulty.HARD,
                        2, 1, 1, 2, 3, 41, 185,
                        "ROCKET ROAR");
                playerDAO.register(connection, player);
                playerDAO.updatePreferences(connection, player,
                        result.getMascot(), result.getDifficulty(),
                        PitchStyle.SIDEARM,
                        "FOUR_SEAM,SLIDER,CUTTER,CHANGEUP",
                        TeamGenerationController.generate(result.getMascot()).storageValue());
                historyDAO.save(connection, result);
                statsDAO.recordMatch(connection, result);
                require(count(connection, "players", player) == 1,
                        "Player registration failed.");
                require(count(connection, "high_scores", player) == 1,
                        "Match history insert failed.");
                require(count(connection, "player_stats", player) == 1,
                        "Career statistics update failed.");
            } finally {
                connection.rollback();
            }
        } finally {
            DatabaseConnection.clear();
        }
        System.out.println("BACKEND_TRANSACTION_ROUND_TRIP=PASS");
    }

    private static int count(Connection connection, String table, String player)
            throws Exception {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE player_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
