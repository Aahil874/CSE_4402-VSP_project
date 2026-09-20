package application;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Verifies that packaged FXML and sprite resources are available offline. */
public final class ResourceLoadingTest {
    private ResourceLoadingTest() { }

    public static void main(String[] args) throws Exception {
        String[] screens = {"main-menu.fxml", "roster.fxml", "pitch-customization.fxml",
                "game.fxml", "settings.fxml", "leaderboard.fxml", "database-login.fxml", "game-over.fxml"};
        for (String screen : screens) {
            try (InputStream stream = ResourceLoadingTest.class.getResourceAsStream("/application/" + screen)) {
                require(stream != null, "missing FXML: " + screen);
                String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                require(text.contains("fx:controller=\"application."), "invalid controller reference: " + screen);
            }
        }
        try (InputStream atlas = ResourceLoadingTest.class.getResourceAsStream("/application/mascot-action-atlas-v16.png")) {
            require(atlas != null, "missing mascot atlas");
        }
        try (InputStream stadium = ResourceLoadingTest.class.getResourceAsStream("/application/stadium-mascot-crowd-v12-4k.png")) {
            require(stadium != null, "missing mascot-only stadium background");
        }
        System.out.println("RESOURCE_LOADING=PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
