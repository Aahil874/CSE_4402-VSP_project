package application;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneManager {
    /**
     * Full HD. The park is authored against a fixed 1180x650 space and then
     * fitted to the viewport, so the window is what decides how many real
     * pixels each mascot gets. At the old 1280x900 the scene was presented at
     * roughly 1234x680 -- smaller than the art it is drawn from, which is what
     * made the mascots look small and soft however good the source art was.
     */
    private static final double WIDTH = 1920;
    private static final double HEIGHT = 1080;
    /** Still usable on a smaller display; the window stays freely resizable. */
    private static final double MIN_WIDTH = 1180;
    private static final double MIN_HEIGHT = 760;
    private static Stage stage;

    private SceneManager() {
    }

    public static void initialize(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Mascot Baseball Grand Prix - Competitive Field Edition");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
    }

    /** Full HD, or as much of it as this screen can actually show. */
    private static double[] preferredSize() {
        try {
            javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
            return new double[]{Math.max(MIN_WIDTH, Math.min(WIDTH, screen.getWidth())),
                                Math.max(MIN_HEIGHT, Math.min(HEIGHT, screen.getHeight()))};
        } catch (RuntimeException noToolkit) {
            return new double[]{WIDTH, HEIGHT};
        }
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
                    SceneManager.class.getResource("/application/" + resourceName));
            Parent root = loader.load();
            Scene current = stage.getScene();
            if (current == null) {
                double[] size = preferredSize();
                stage.setScene(new Scene(root, size[0], size[1]));
            } else {
                // Reuse the live scene so moving between screens keeps whatever
                // size the player resized or maximised the window to, instead of
                // snapping back to the default on every navigation.
                current.setRoot(root);
            }
            stage.show();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load " + resourceName, exception);
        }
    }
}
