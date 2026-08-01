package application;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

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
    private TextField playerNameField;
    @FXML
    private Label feedbackLabel;

    private final GameBackendService backend = GameBackendService.getInstance();

    @FXML
    private void initialize() {
        playerNameField.setText(GameSession.getPlayerName());
        loadRegisteredPlayers(GameSession.getPlayerName());
    }

    @FXML
    private void handleRegister() {
        String name = cleanName(playerNameField.getText());
        if (!isValidName(name)) {
            return;
        }

        runDatabaseTask(() -> {
            backend.registerPlayer(name);
            return backend.findRegisteredPlayers();
        }, players -> {
            updateRegisteredPlayers(players, name);
            feedbackLabel.setText("PLAYER REGISTERED: " + name.toUpperCase());
        });
    }

    @FXML
    private void handleSelectPlayer() {
        String selectedPlayer = registeredPlayersComboBox.getValue();
        if (selectedPlayer != null && !selectedPlayer.isBlank()) {
            playerNameField.setText(selectedPlayer);
            feedbackLabel.setText("READY: " + selectedPlayer.toUpperCase());
        }
    }

    @FXML
    private void handlePlay() {
        String name = cleanName(playerNameField.getText());
        if (!isValidName(name)) {
            return;
        }

        runDatabaseTask(() -> {
            backend.registerPlayer(name);
            return backend.findPlayerProfile(name).orElse(null);
        }, profile -> {
            GameSession.start(name);
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
        } else if (!players.isEmpty()) {
            registeredPlayersComboBox.setValue(players.get(0));
        }
    }

    private boolean isValidName(String name) {
        if (name.isEmpty()) {
            feedbackLabel.setText("ENTER A PLAYER NAME");
            return false;
        }
        if (name.length() > 16) {
            feedbackLabel.setText("USE 16 CHARACTERS OR FEWER");
            return false;
        }
        if (name.chars().anyMatch(Character::isISOControl)) {
            feedbackLabel.setText("REMOVE CONTROL CHARACTERS FROM THE NAME");
            return false;
        }
        return true;
    }

    private String cleanName(String name) {
        return name == null ? "" : name.trim();
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
                    : "MYSQL UNAVAILABLE - OPEN DATABASE CONNECTION");
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
}
