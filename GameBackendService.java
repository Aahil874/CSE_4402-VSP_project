package application;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


public final class GameBackendService {
    private static final GameBackendService INSTANCE = new GameBackendService();

    private final PlayerDAO playerDAO = new PlayerDAO();
    private final HighScoreDAO highScoreDAO = new HighScoreDAO();
    private final PlayerStatsDAO playerStatsDAO = new PlayerStatsDAO();

    private GameBackendService() {
    }

    public static GameBackendService getInstance() {
        return INSTANCE;
    }

    public void registerPlayer(String playerName) throws SQLException {
        playerDAO.register(playerName);
    }

    public List<String> findRegisteredPlayers() throws SQLException {
        return playerDAO.findAll();
    }

    public int countRegisteredPlayers() throws SQLException {
        return playerDAO.count();
    }

    public Optional<PlayerProfile> findPlayerProfile(String playerName)
            throws SQLException {
        return playerDAO.findProfile(playerName);
    }

    public List<PlayerStats> findChampionshipStandings(int maximumResults)
            throws SQLException {
        return playerStatsDAO.findChampionshipStandings(maximumResults);
    }

    public void saveCompletedMatch(HighScore result) throws SQLException {
        try (Connection connection = DatabaseConnection.open()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                playerDAO.register(connection, result.getPlayerName());
                playerDAO.updatePreferences(connection, result.getPlayerName(),
                        result.getMascot(), result.getDifficulty(),
                        GameSession.getPitchStyle(),
                        GameSession.getPitchLoadout().stream()
                                .map(Enum::name).collect(java.util.stream.Collectors.joining(",")),
                        GameSession.getPlayerLineup().storageValue());
                highScoreDAO.save(connection, result);
                playerStatsDAO.recordMatch(connection, result);
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }
}
