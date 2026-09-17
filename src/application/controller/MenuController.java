package application.controller;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import application.SceneManager;
import application.model.GameSession;
import application.model.GameSettings;
import application.model.PlayerProfile;
import application.service.GameBackendService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public final class MenuController {

    @FXML
    private ComboBox<String> registeredPlayersComboBox;
    @FXML
    private Label feedbackLabel;

    private final GameBackendService backend = GameBackendService.getInstance();

    @FXML
    private void initialize() {
        String current = GameSession.getPlayerName();
        loadRegisteredPlayers(current != null && !current.isBlank() ? current : "BABE RUTH");
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
            feedbackLabel.setText("SELECT A PLAYER TO START");
            return;
        }

        runDatabaseTask(() -> backend.findPlayerProfile(selectedPlayer).orElse(null), profile -> {
            GameSession.start(selectedPlayer);
            if (profile != null) {
                applyProfile(profile);
            }
            SceneManager.showRoster();
        });
    }

    private void loadRegisteredPlayers(String preferredPlayer) {
        runDatabaseTask(backend::findRegisteredPlayers,
                players -> updateRegisteredPlayers(players, preferredPlayer));
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

    private <T> void runDatabaseTask(Callable<T> operation, Consumer<T> onSuccess) {
        feedbackLabel.setText("SYNCING PLAYER DATABASE...");
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return operation.call();
            }
        };
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            feedbackLabel.setText(error instanceof IllegalArgumentException
                    ? error.getMessage()
                    : "DATABASE UNAVAILABLE - CHECK SERVER");
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
    private void handleExit() {
        Platform.exit();
    }
}