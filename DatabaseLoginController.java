package application;

import javafx.fxml.FXML;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
public final class DatabaseLoginController {
    @FXML
    private Button connectButton;
    @FXML
    private Button backButton;
    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        backButton.setDisable(!DatabaseConnection.isConfigured());
        handleConnect();
    }

    @FXML
    private void handleConnect() {
        connectButton.setDisable(true);
        backButton.setDisable(true);
        statusLabel.setText("CONNECTING TO MYSQL...");

        Task<Void> connectionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                DatabaseConnection.configureDefaultAndVerify();
                DatabaseInitializer.initialize();
                return null;
            }
        };
        connectionTask.setOnSucceeded(event -> {
            SceneManager.showMainMenu();
        });
        connectionTask.setOnFailed(event -> {
            Throwable exception = connectionTask.getException();
            statusLabel.setText(findRootCauseMessage(exception).toUpperCase());
            connectButton.setDisable(false);
            backButton.setDisable(!DatabaseConnection.isConfigured());
        });

        Thread connectionThread = new Thread(
                connectionTask, "retro-baseball-mysql-connect");
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    @FXML
    private void handleBack() {
        if (DatabaseConnection.isConfigured()) {
            SceneManager.showMainMenu();
        }
    }

    private String findRootCauseMessage(Throwable exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        String message = rootCause.getMessage();
        return message == null || message.isBlank()
                ? rootCause.getClass().getSimpleName()
                : message;
    }
}
