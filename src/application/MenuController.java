package application;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import application.SceneManager;
import application.GameSession;
import application.GameSettings;
import application.PlayerProfile;
import application.GameBackendService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public final class MenuController {

    @FXML
    private ComboBox<String> registeredPlayersComboBox;
    @FXML
    private Label feedbackLabel;
    @FXML
    private TextField playerNameField;

    private final GameBackendService backend = GameBackendService.getInstance();

    @FXML
    private void initialize() {
        String current = GameSession.getPlayerName();
        String preferred = current != null && !current.isBlank() ? current : "PLAYER 1";
        if (DatabaseConnection.isOfflineMode() || !DatabaseConnection.isConfigured()) {
            activateOfflineMode(preferred);
        } else {
            loadRegisteredPlayers(preferred);
        }
    }

    @FXML
    private void handleSelectPlayer() {
        String selectedPlayer = registeredPlayersComboBox.getValue();
        if (selectedPlayer != null && !selectedPlayer.isBlank()) {
            feedbackLabel.setText("READY: " + selectedPlayer.toUpperCase());
        }
    }

    @FXML
    private void handlePlay() {
        String selectedPlayer = registeredPlayersComboBox.getValue();
        if (selectedPlayer == null || selectedPlayer.isBlank()) {
            selectedPlayer = normalizedPlayerName(playerNameField.getText());
        }
        if (selectedPlayer == null) {
            selectedPlayer = "PLAYER 1";
        }

        String player = selectedPlayer;
        if (DatabaseConnection.isOfflineMode() || !DatabaseConnection.isConfigured()) {
            startGame(player, null);
            return;
        }

        runDatabaseTask(() -> backend.findPlayerProfile(player).orElse(null), profile -> {
            startGame(player, profile);
        }, () -> startGame(player, null));
    }


    @FXML
    private void handleRegister() {
        String player = normalizedPlayerName(playerNameField.getText());
        if (player == null) {
            feedbackLabel.setText("ENTER A PLAYER NAME (1-16 CHARACTERS)");
            return;
        }

        if (DatabaseConnection.isOfflineMode() || !DatabaseConnection.isConfigured()) {
            registerOfflinePlayer(player);
            return;
        }

        runDatabaseTask(() -> {
            backend.registerPlayer(player);
            return backend.findRegisteredPlayers();
        }, players -> {
            updateRegisteredPlayers(players, player);
            playerNameField.clear();
            feedbackLabel.setText("REGISTERED: " + player.toUpperCase());
        }, () -> registerOfflinePlayer(player));
    }

    private void startGame(String player, PlayerProfile profile) {
            GameSession.start(player);
            if (profile != null) {
                applyProfile(profile);
            }
            SceneManager.showRoster();
    }

    private void loadRegisteredPlayers(String preferredPlayer) {
        runDatabaseTask(backend::findRegisteredPlayers,
                players -> updateRegisteredPlayers(players, preferredPlayer),
                () -> activateOfflineMode(preferredPlayer));
    }

    private void updateRegisteredPlayers(List<String> players, String preferredPlayer) {
        registeredPlayersComboBox.getItems().setAll(players);
        if (preferredPlayer != null && players.contains(preferredPlayer)) {
            registeredPlayersComboBox.setValue(preferredPlayer);
            feedbackLabel.setText("READY: " + preferredPlayer.toUpperCase());
        } else if (!players.isEmpty()) {
            String first = players.get(0);
            registeredPlayersComboBox.setValue(first);
            feedbackLabel.setText("READY: " + first.toUpperCase());
        } else {
            feedbackLabel.setText("NO REGISTERED PLAYERS FOUND");
        }
    }

    private void applyProfile(PlayerProfile profile) {
        GameSession.selectMascot(profile.selectedMascot());
        GameSettings.setDifficulty(profile.preferredDifficulty());
    }

    private <T> void runDatabaseTask(
            Callable<T> operation, Consumer<T> onSuccess, Runnable onFailure) {
        feedbackLabel.setText("SYNCING PLAYER DATABASE...");
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return operation.call();
            }
        };
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> {
            DatabaseConnection.enableOfflineMode();
            feedbackLabel.setText("MYSQL UNAVAILABLE - OFFLINE MODE ACTIVE");
            onFailure.run();
        });
        Thread thread = new Thread(task, "mbgp-player-database");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleLeaderboard() {
        SceneManager.showLeaderboard();
    }

    @FXML
    private void handleSettings() {
        SceneManager.showSettings();
    }

    @FXML
    private void handleDatabase() {
        SceneManager.showDatabaseLogin();
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    private void activateOfflineMode(String preferredPlayer) {
        DatabaseConnection.enableOfflineMode();
        String player = normalizedPlayerName(preferredPlayer);
        if (player == null) {
            player = "PLAYER 1";
        }
        registeredPlayersComboBox.getItems().setAll(player);
        registeredPlayersComboBox.setValue(player);
        feedbackLabel.setText("OFFLINE MODE - GAMEPLAY AVAILABLE");
    }

    private void registerOfflinePlayer(String player) {
        if (!registeredPlayersComboBox.getItems().contains(player)) {
            registeredPlayersComboBox.getItems().add(player);
        }
        registeredPlayersComboBox.setValue(player);
        playerNameField.clear();
        feedbackLabel.setText("OFFLINE PLAYER READY: " + player.toUpperCase());
    }

    private String normalizedPlayerName(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() || normalized.length() > 16 ? null : normalized;
    }
}
