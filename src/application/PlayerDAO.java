package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public final class PlayerDAO {
    public void register(String playerName) throws SQLException {
        try (Connection connection = DatabaseConnection.open()) {
            register(connection, playerName);
        }
    }

    void register(Connection connection, String playerName) throws SQLException {
        String cleanedName = validateName(playerName);
        String sql = "INSERT IGNORE INTO players(player_name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cleanedName);
            statement.executeUpdate();
        }
    }

    void updatePreferences(Connection connection, String playerName,
                           Mascot mascot, GameSettings.Difficulty difficulty,
                           PitchStyle pitchStyle, String pitchLoadout, String teamLineup)
            throws SQLException {
        String sql = """
                UPDATE players
                SET selected_mascot = ?,
                    preferred_difficulty = ?,
                    pitch_style = ?,
                    pitch_loadout = ?,
                    team_lineup = ?,
                    last_played = CURRENT_TIMESTAMP(6)
                WHERE player_name = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, mascot.name());
            statement.setString(2, difficulty.name());
            statement.setString(3, pitchStyle.name());
            statement.setString(4, pitchLoadout);
            statement.setString(5, teamLineup);
            statement.setString(6, validateName(playerName));
            statement.executeUpdate();
        }
    }

    public List<String> findAll() throws SQLException {
        String sql = """
                SELECT player_name
                FROM players
                ORDER BY player_name ASC
                """;
        List<String> players = new ArrayList<>();

        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                players.add(result.getString("player_name"));
            }
        }
        return players;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM players";
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    public Optional<PlayerProfile> findProfile(String playerName) throws SQLException {
        String sql = """
                SELECT id, player_name, selected_mascot, preferred_difficulty,
                       pitch_style, pitch_loadout, team_lineup,
                       registered_at, last_played
                FROM players
                WHERE player_name = ?
                """;
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, validateName(playerName));
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                java.sql.Timestamp lastPlayed = result.getTimestamp("last_played");
                return Optional.of(new PlayerProfile(
                        result.getLong("id"),
                        result.getString("player_name"),
                        parseMascot(result.getString("selected_mascot")),
                        parseDifficulty(result.getString("preferred_difficulty")),
                        parsePitchStyle(result.getString("pitch_style")),
                        result.getString("pitch_loadout"),
                        result.getString("team_lineup"),
                        result.getTimestamp("registered_at").toLocalDateTime(),
                        lastPlayed == null ? null : lastPlayed.toLocalDateTime()));
            }
        }
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

    private PitchStyle parsePitchStyle(String value) {
        try {
            return PitchStyle.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return PitchStyle.CLASSIC_OVERHAND;
        }
    }

    private String validateName(String playerName) {
        String cleanedName = playerName == null ? "" : playerName.trim();
        if (cleanedName.isEmpty()) {
            throw new IllegalArgumentException("ENTER A PLAYER NAME");
        }
        if (cleanedName.length() > 16) {
            throw new IllegalArgumentException("USE 16 CHARACTERS OR FEWER");
        }
        if (cleanedName.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("REMOVE CONTROL CHARACTERS FROM THE NAME");
        }
        return cleanedName;
    }
}
