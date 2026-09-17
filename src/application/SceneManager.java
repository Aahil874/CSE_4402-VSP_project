package application;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneManager {
    // Increased target height to ensure bottom control panel stays fully visible
    private static final double WIDTH = 1280;
    private static final double HEIGHT = 960; 
    private static Stage stage;

    private SceneManager() {
    }

    public static void initialize(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Mascot Baseball Grand Prix - Competitive Field Edition");
        stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT);
        stage.setResizable(true);
    }

    public static void showMainMenu() {
        show("main-menu.fxml");
    }

    public static void showDatabaseLogin() {
        show("database-login.fxml");
    }

    public static void showGame() {
        show("game.fxml");
    }

    public static void showRoster() {
        show("roster.fxml");
    }

    public static void showPitchCustomization() {
        show("pitch-customization.fxml");
    }

    public static void showSettings() {
        show("settings.fxml");
    }

    public static void showGameOver() {
        show("game-over.fxml");
    }

    public static void showLeaderboard() {
        show("leaderboard.fxml");
    }

    private static void show(String resourceName) {
        if (stage == null) {
            throw new IllegalStateException("SceneManager must be initialized first.");
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/application/views/" + resourceName));
            Parent root = loader.load();

            // Reuse existing scene if available to retain window sizing adjustments
            if (stage.getScene() == null) {
                Scene scene = new Scene(root, WIDTH, HEIGHT);
                stage.setScene(scene);
            } else {
                stage.getScene().setRoot(root);
            }

            stage.sizeToScene(); // Forces stage to recalculate layout based on child preferred heights
            stage.show();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load " + resourceName, exception);
        }
    }
}