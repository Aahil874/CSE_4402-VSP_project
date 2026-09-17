package application;

import javafx.fxml.FXML;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
public final class DatabaseLoginController {
    @FXML
    private Button connectButton;
    @FXML
    private Button backButton;
    @FXML
    private Label statusLabel;
    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private TextField databaseField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void initialize() {
        hostField.setText(DatabaseConnection.defaultHost());
        portField.setText(Integer.toString(DatabaseConnection.defaultPort()));
        databaseField.setText(DatabaseConnection.defaultDatabase());
        usernameField.setText(DatabaseConnection.defaultUsername());
        passwordField.setText(DatabaseConnection.defaultPassword());
        backButton.setDisable(!DatabaseConnection.isConfigured());
        handleConnect();
    }

    @FXML
    private void handleConnect() {
        final int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (RuntimeException exception) {
            statusLabel.setText("MYSQL PORT MUST BE A NUMBER.");
            return;
        }

        String host = hostField.getText();
        String database = databaseField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        connectButton.setDisable(true);
        backButton.setDisable(true);
        statusLabel.setText("CONNECTING TO MYSQL...");

        Task<Void> connectionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                DatabaseConnection.configureAndVerify(
                        host, port, database, username, password);
                DatabaseInitializer.initialize();
                return null;
            }
        };
        connectionTask.setOnSucceeded(event -> {
            SceneManager.showMainMenu();
        });
        connectionTask.setOnFailed(event -> {
            Throwable exception = connectionTask.getException();
            DatabaseConnection.enableOfflineMode();
            statusLabel.setText("MYSQL UNAVAILABLE: "
                    + findRootCauseMessage(exception).toUpperCase()
                    + " - CONTINUING OFFLINE");
            SceneManager.showMainMenu();
        });

        Thread connectionThread = new Thread(
                connectionTask, "retro-baseball-mysql-connect");
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    @FXML
    private void handleBack() {
        if (DatabaseConnection.isConfigured() || DatabaseConnection.isOfflineMode()) {
            SceneManager.showMainMenu();
        }
    }

    @FXML
    private void handlePlayOffline() {
        DatabaseConnection.enableOfflineMode();
        SceneManager.showMainMenu();
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
