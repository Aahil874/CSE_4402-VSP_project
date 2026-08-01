package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class PlayerStatsDAO {
    public void recordMatch(Connection connection, HighScore result) throws SQLException {
        String sql = """
                INSERT INTO player_stats(
                    player_name, games_played, wins, losses, ties,
                    total_runs, total_rival_runs, total_hits, home_runs,
                    total_singles, total_doubles, total_triples,
                    total_walks, total_strikeouts,
                    best_score, best_combo, last_mascot, last_difficulty, last_played)
                VALUES (?, 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    games_played = games_played + 1,
                    wins = wins + VALUES(wins),
                    losses = losses + VALUES(losses),
                    ties = ties + VALUES(ties),
                    total_runs = total_runs + VALUES(total_runs),
                    total_rival_runs = total_rival_runs + VALUES(total_rival_runs),
                    total_hits = total_hits + VALUES(total_hits),
                    home_runs = home_runs + VALUES(home_runs),
                    total_singles = total_singles + VALUES(total_singles),
                    total_doubles = total_doubles + VALUES(total_doubles),
                    total_triples = total_triples + VALUES(total_triples),
                    total_walks = total_walks + VALUES(total_walks),
                    total_strikeouts = total_strikeouts + VALUES(total_strikeouts),
                    best_score = GREATEST(best_score, VALUES(best_score)),
                    best_combo = GREATEST(best_combo, VALUES(best_combo)),
                    last_mascot = VALUES(last_mascot),
                    last_difficulty = VALUES(last_difficulty),
                    last_played = VALUES(last_played)
                """;
        int comparison = Integer.compare(result.getScore(), result.getRivalScore());
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, result.getPlayerName());
            statement.setInt(2, comparison > 0 ? 1 : 0);
            statement.setInt(3, comparison < 0 ? 1 : 0);
            statement.setInt(4, comparison == 0 ? 1 : 0);
            statement.setInt(5, result.getScore());
            statement.setInt(6, result.getRivalScore());
            statement.setInt(7, result.getHits());
            statement.setInt(8, result.getHomeRuns());
            statement.setInt(9, result.getSingles());
            statement.setInt(10, result.getDoubles());
            statement.setInt(11, result.getTriples());
            statement.setInt(12, result.getWalks());
            statement.setInt(13, result.getStrikeouts());
            statement.setInt(14, result.getScore());
            statement.setInt(15, result.getMaxCombo());
            statement.setString(16, result.getMascot().name());
            statement.setString(17, result.getDifficulty().name());
            statement.setTimestamp(18, java.sql.Timestamp.valueOf(result.getPlayedAt()));
            statement.executeUpdate();
        }
    }

    public List<PlayerStats> findChampionshipStandings(int maximumResults)
            throws SQLException {
        if (maximumResults < 1) {
            return List.of();
        }
        String sql = """
                SELECT player_name, games_played, wins, losses, ties,
                       total_runs, total_rival_runs, total_hits, home_runs,
                       best_score, best_combo, last_mascot,
                       last_difficulty, last_played
                FROM player_stats
                ORDER BY (wins * 3 + ties) DESC,
                         wins DESC,
                         best_score DESC,
                         total_runs DESC,
                         last_played ASC
                LIMIT ?
                """;
        List<PlayerStats> standings = new ArrayList<>();
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, maximumResults);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    standings.add(read(result));
                }
            }
        }
        return standings;
    }

    private PlayerStats read(ResultSet result) throws SQLException {
        return new PlayerStats(
                result.getString("player_name"),
                result.getInt("games_played"),
                result.getInt("wins"),
                result.getInt("losses"),
                result.getInt("ties"),
                result.getInt("total_runs"),
                result.getInt("total_rival_runs"),
                result.getInt("total_hits"),
                result.getInt("home_runs"),
                result.getInt("best_score"),
                result.getInt("best_combo"),
                parseMascot(result.getString("last_mascot")),
                parseDifficulty(result.getString("last_difficulty")),
                result.getTimestamp("last_played").toLocalDateTime());
    }

    private Mascot parseMascot(String value) {
        try {
            return Mascot.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Mascot.TURBO_TANUKI;
        }
    }

    private GameSettings.Difficulty parseDifficulty(String value) {
        try {
            return GameSettings.Difficulty.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return GameSettings.Difficulty.EASY;
        }
    }
}
