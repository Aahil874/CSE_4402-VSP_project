package application;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    private static final String CREATE_PLAYERS = """
            CREATE TABLE IF NOT EXISTS players (
                id            INT         UNSIGNED NOT NULL AUTO_INCREMENT,
                player_name   VARCHAR(16)          NOT NULL,
                selected_mascot VARCHAR(32)         NOT NULL DEFAULT 'TURBO_TANUKI',
                preferred_difficulty VARCHAR(10)    NOT NULL DEFAULT 'EASY',
                pitch_style    VARCHAR(32)           NOT NULL DEFAULT 'CLASSIC_OVERHAND',
                pitch_loadout  VARCHAR(255)          NOT NULL DEFAULT 'FOUR_SEAM,CURVEBALL,SLIDER,CHANGEUP',
                team_lineup    VARCHAR(1024)         NOT NULL DEFAULT '',
                registered_at DATETIME(6)           NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                last_played   DATETIME(6)           NULL,
                PRIMARY KEY (id),
                UNIQUE KEY uk_players_name (player_name)
            ) ENGINE=InnoDB
            """;

    private static final String CREATE_HIGH_SCORES = """
            CREATE TABLE IF NOT EXISTS high_scores (
                id             INT         UNSIGNED NOT NULL AUTO_INCREMENT,
                player_name    VARCHAR(16)          NOT NULL,
                score          INT         UNSIGNED NOT NULL,
                rival_score    INT         UNSIGNED NOT NULL DEFAULT 0,
                hits           INT         UNSIGNED NOT NULL DEFAULT 0,
                home_runs      INT         UNSIGNED NOT NULL DEFAULT 0,
                innings        SMALLINT    UNSIGNED NOT NULL DEFAULT 1,
                max_combo      INT         UNSIGNED NOT NULL DEFAULT 0,
                mascot_key     VARCHAR(32)           NOT NULL DEFAULT 'TURBO_TANUKI',
                difficulty     VARCHAR(10)           NOT NULL DEFAULT 'EASY',
                singles        INT         UNSIGNED NOT NULL DEFAULT 0,
                doubles_hit    INT         UNSIGNED NOT NULL DEFAULT 0,
                triples_hit    INT         UNSIGNED NOT NULL DEFAULT 0,
                walks          INT         UNSIGNED NOT NULL DEFAULT 0,
                strikeouts     INT         UNSIGNED NOT NULL DEFAULT 0,
                pitch_count    INT         UNSIGNED NOT NULL DEFAULT 0,
                opponent_name  VARCHAR(48)           NOT NULL DEFAULT 'GRAND PRIX RIVALS',
                duration_seconds INT       UNSIGNED NOT NULL DEFAULT 0,
                played_at      DATETIME(6)           NOT NULL,
                PRIMARY KEY (id),
                INDEX idx_high_scores_ranking (score DESC, hits DESC),
                INDEX idx_high_scores_player (player_name)
            ) ENGINE=InnoDB
            """;

    private static final String CREATE_PLAYER_STATS = """
            CREATE TABLE IF NOT EXISTS player_stats (
                player_name       VARCHAR(16)          NOT NULL,
                games_played      INT         UNSIGNED NOT NULL DEFAULT 0,
                wins              INT         UNSIGNED NOT NULL DEFAULT 0,
                losses            INT         UNSIGNED NOT NULL DEFAULT 0,
                ties              INT         UNSIGNED NOT NULL DEFAULT 0,
                total_runs        INT         UNSIGNED NOT NULL DEFAULT 0,
                total_rival_runs  INT         UNSIGNED NOT NULL DEFAULT 0,
                total_hits        INT         UNSIGNED NOT NULL DEFAULT 0,
                home_runs         INT         UNSIGNED NOT NULL DEFAULT 0,
                total_singles     INT         UNSIGNED NOT NULL DEFAULT 0,
                total_doubles     INT         UNSIGNED NOT NULL DEFAULT 0,
                total_triples     INT         UNSIGNED NOT NULL DEFAULT 0,
                total_walks       INT         UNSIGNED NOT NULL DEFAULT 0,
                total_strikeouts  INT         UNSIGNED NOT NULL DEFAULT 0,
                best_score        INT         UNSIGNED NOT NULL DEFAULT 0,
                best_combo        INT         UNSIGNED NOT NULL DEFAULT 0,
                last_mascot       VARCHAR(32)           NOT NULL DEFAULT 'TURBO_TANUKI',
                last_difficulty   VARCHAR(10)           NOT NULL DEFAULT 'EASY',
                last_played       DATETIME(6)           NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                PRIMARY KEY (player_name),
                INDEX idx_player_stats_championship (wins DESC, best_score DESC)
            ) ENGINE=InnoDB
            """;

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection connection = DatabaseConnection.open();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_PLAYERS);
            statement.executeUpdate(CREATE_HIGH_SCORES);
            statement.executeUpdate(CREATE_PLAYER_STATS);

            ensureColumn(connection, statement, "high_scores", "rival_score",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "high_scores", "home_runs",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "high_scores", "max_combo",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "high_scores", "mascot_key",
                    "VARCHAR(32) NOT NULL DEFAULT 'TURBO_TANUKI'");
            ensureColumn(connection, statement, "high_scores", "difficulty",
                    "VARCHAR(10) NOT NULL DEFAULT 'EASY'");
            ensureColumn(connection, statement, "players", "selected_mascot",
                    "VARCHAR(32) NOT NULL DEFAULT 'TURBO_TANUKI'");
            ensureColumn(connection, statement, "players", "preferred_difficulty",
                    "VARCHAR(10) NOT NULL DEFAULT 'EASY'");
            ensureColumn(connection, statement, "players", "last_played",
                    "DATETIME(6) NULL");
            ensureColumn(connection, statement, "players", "pitch_style",
                    "VARCHAR(32) NOT NULL DEFAULT 'CLASSIC_OVERHAND'");
            ensureColumn(connection, statement, "players", "pitch_loadout",
                    "VARCHAR(255) NOT NULL DEFAULT 'FOUR_SEAM,CURVEBALL,SLIDER,CHANGEUP'");
            ensureColumn(connection, statement, "players", "team_lineup",
                    "VARCHAR(1024) NOT NULL DEFAULT ''");
            ensureColumn(connection, statement, "high_scores", "singles",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "high_scores", "doubles_hit",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "high_scores", "triples_hit",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "high_scores", "walks",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "high_scores", "strikeouts",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "high_scores", "pitch_count",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "high_scores", "opponent_name",
                    "VARCHAR(48) NOT NULL DEFAULT 'GRAND PRIX RIVALS'");
            ensureColumn(connection, statement, "high_scores", "duration_seconds",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "player_stats", "total_singles",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "player_stats", "total_doubles",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "player_stats", "total_triples",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "player_stats", "total_walks",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
            ensureColumn(connection, statement, "player_stats", "total_strikeouts",
                    "INT UNSIGNED NOT NULL DEFAULT 0");
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not initialize the MySQL database.", exception);
        }
    }

    private static void ensureColumn(Connection connection, Statement statement,
                                     String tableName, String columnName,
                                     String columnDefinition) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet columns = metadata.getColumns(
                connection.getCatalog(), null, tableName, columnName)) {
            if (columns.next()) {
                return;
            }
        }
        statement.executeUpdate("ALTER TABLE " + tableName
                + " ADD COLUMN " + columnName + " " + columnDefinition);
    }
}
