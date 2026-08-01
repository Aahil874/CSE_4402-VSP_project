package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class HighScoreDAO {
    public void save(HighScore highScore) throws SQLException {
        try (Connection connection = DatabaseConnection.open()) {
            save(connection, highScore);
        }
    }

    void save(Connection connection, HighScore highScore) throws SQLException {
        String sql = """
                INSERT INTO high_scores(
                    player_name, score, rival_score, hits, home_runs, innings,
                    max_combo, mascot_key, difficulty, singles, doubles_hit,
                    triples_hit, walks, strikeouts, pitch_count, opponent_name,
                    duration_seconds, played_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, highScore.getPlayerName());
            statement.setInt(2, highScore.getScore());
            statement.setInt(3, highScore.getRivalScore());
            statement.setInt(4, highScore.getHits());
            statement.setInt(5, highScore.getHomeRuns());
            statement.setInt(6, highScore.getInnings());
            statement.setInt(7, highScore.getMaxCombo());
            statement.setString(8, highScore.getMascot().name());
            statement.setString(9, highScore.getDifficulty().name());
            statement.setInt(10, highScore.getSingles());
            statement.setInt(11, highScore.getDoubles());
            statement.setInt(12, highScore.getTriples());
            statement.setInt(13, highScore.getWalks());
            statement.setInt(14, highScore.getStrikeouts());
            statement.setInt(15, highScore.getPitchCount());
            statement.setString(16, highScore.getOpponentName());
            statement.setInt(17, highScore.getDurationSeconds());
            statement.setTimestamp(18, Timestamp.valueOf(highScore.getPlayedAt()));
            statement.executeUpdate();
        }
    }

    public List<HighScore> findBestPerPlayer(int maximumResults) throws SQLException {
        if (maximumResults < 1) {
            return List.of();
        }

        String sql = """
                SELECT current_score.id,
                       current_score.player_name,
                       current_score.score,
                       current_score.rival_score,
                       current_score.hits,
                       current_score.home_runs,
                       current_score.innings,
                       current_score.max_combo,
                       current_score.mascot_key,
                       current_score.difficulty,
                       current_score.singles,
                       current_score.doubles_hit,
                       current_score.triples_hit,
                       current_score.walks,
                       current_score.strikeouts,
                       current_score.pitch_count,
                       current_score.opponent_name,
                       current_score.duration_seconds,
                       current_score.played_at
                FROM high_scores current_score
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM high_scores better_score
                    WHERE better_score.player_name = current_score.player_name
                      AND (
                          better_score.score > current_score.score
                          OR (better_score.score = current_score.score
                              AND better_score.hits > current_score.hits)
                          OR (better_score.score = current_score.score
                              AND better_score.hits = current_score.hits
                              AND better_score.max_combo > current_score.max_combo)
                          OR (better_score.score = current_score.score
                              AND better_score.hits = current_score.hits
                              AND better_score.max_combo = current_score.max_combo
                              AND better_score.played_at < current_score.played_at)
                          OR (better_score.score = current_score.score
                              AND better_score.hits = current_score.hits
                              AND better_score.max_combo = current_score.max_combo
                              AND better_score.played_at = current_score.played_at
                              AND better_score.id < current_score.id)
                      )
                )
                ORDER BY current_score.score DESC,
                         current_score.hits DESC,
                         current_score.max_combo DESC,
                         current_score.played_at ASC
                LIMIT ?
                """;
        List<HighScore> scores = new ArrayList<>();

        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, maximumResults);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    scores.add(readHighScore(result));
                }
            }
        }
        return scores;
    }

    private HighScore readHighScore(ResultSet result) throws SQLException {
        return new HighScore(
                result.getInt("id"),
                result.getString("player_name"),
                result.getInt("score"),
                result.getInt("rival_score"),
                result.getInt("hits"),
                result.getInt("home_runs"),
                result.getInt("innings"),
                result.getInt("max_combo"),
                parseMascot(result.getString("mascot_key")),
                parseDifficulty(result.getString("difficulty")),
                result.getTimestamp("played_at").toLocalDateTime(),
                result.getInt("singles"),
                result.getInt("doubles_hit"),
                result.getInt("triples_hit"),
                result.getInt("walks"),
                result.getInt("strikeouts"),
                result.getInt("pitch_count"),
                result.getInt("duration_seconds"),
                result.getString("opponent_name"));
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
