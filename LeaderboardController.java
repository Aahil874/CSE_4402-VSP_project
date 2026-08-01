package application;

import java.util.List;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class LeaderboardController {
    @FXML
    private TableView<PlayerStats> scoreTable;
    @FXML
    private TableColumn<PlayerStats, Number> rankColumn;
    @FXML
    private TableColumn<PlayerStats, String> playerColumn;
    @FXML
    private TableColumn<PlayerStats, String> mascotColumn;
    @FXML
    private TableColumn<PlayerStats, Number> gamesColumn;
    @FXML
    private TableColumn<PlayerStats, String> recordColumn;
    @FXML
    private TableColumn<PlayerStats, Number> pointsColumn;
    @FXML
    private TableColumn<PlayerStats, Number> runsColumn;
    @FXML
    private TableColumn<PlayerStats, Number> homersColumn;
    @FXML
    private TableColumn<PlayerStats, Number> bestColumn;
    @FXML
    private Label statusLabel;

    private final GameBackendService backend = GameBackendService.getInstance();

    @FXML
    private void initialize() {
        scoreTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        rankColumn.setCellValueFactory(cell ->
                new ReadOnlyIntegerWrapper(scoreTable.getItems().indexOf(cell.getValue()) + 1));
        playerColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getPlayerName()));
        mascotColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getLastMascot().getDisplayName()));
        gamesColumn.setCellValueFactory(cell ->
                new ReadOnlyIntegerWrapper(cell.getValue().getGamesPlayed()));
        recordColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getWins() + "-" + cell.getValue().getLosses()
                        + "-" + cell.getValue().getTies()));
        pointsColumn.setCellValueFactory(cell ->
                new ReadOnlyIntegerWrapper(cell.getValue().getChampionshipPoints()));
        runsColumn.setCellValueFactory(cell ->
                new ReadOnlyIntegerWrapper(cell.getValue().getTotalRuns()));
        homersColumn.setCellValueFactory(cell ->
                new ReadOnlyIntegerWrapper(cell.getValue().getHomeRuns()));
        bestColumn.setCellValueFactory(cell ->
                new ReadOnlyIntegerWrapper(cell.getValue().getBestScore()));
        loadStandings();
    }

    private void loadStandings() {
        statusLabel.setText("LOADING CHAMPIONSHIP STANDINGS...");
        Task<StandingsPayload> task = new Task<>() {
            @Override
            protected StandingsPayload call() throws Exception {
                return new StandingsPayload(
                        backend.findChampionshipStandings(50),
                        backend.countRegisteredPlayers());
            }
        };
        task.setOnSucceeded(event -> {
            StandingsPayload payload = task.getValue();
            scoreTable.getItems().setAll(payload.standings());
            int registeredPlayers = payload.registeredPlayers();
            statusLabel.setText(scoreTable.getItems().isEmpty()
                    ? registeredPlayers + " PLAYERS REGISTERED - COMPLETE A MATCH"
                    : registeredPlayers + " PLAYERS - 3 POINTS PER WIN, 1 PER TIE");
        });
        task.setOnFailed(event ->
            statusLabel.setText("MYSQL UNAVAILABLE - RECONNECT FROM THE MAIN MENU")
        );
        Thread thread = new Thread(task, "mbgp-load-leaderboard");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleBack() {
        SceneManager.showMainMenu();
    }

    private record StandingsPayload(
            List<PlayerStats> standings, int registeredPlayers) {
    }
}
