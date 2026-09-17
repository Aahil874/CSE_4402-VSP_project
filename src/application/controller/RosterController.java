package application.controller;

import application.SceneManager;
import application.model.GameSession;
import application.model.Mascot;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

public final class RosterController {

    @FXML
    private Label playerLabel;
    @FXML
    private ImageView portraitImage;
    @FXML
    private ComboBox<Mascot> mascotComboBox;
    @FXML
    private Label mascotNameLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label statsLabel;
    @FXML
    private Label lineupLabel;

    @FXML
    private void initialize() {
        // 1. Bind active player name directly from GameSession
        String activePlayer = GameSession.getPlayerName();
        if (playerLabel != null) {
            playerLabel.setText((activePlayer != null && !activePlayer.isBlank()) 
                    ? activePlayer.toUpperCase() 
                    : "PLAYER 1");
        }

        // 2. Populate ComboBox with Mascot enum values
        if (mascotComboBox != null) {
            mascotComboBox.getItems().setAll(Mascot.values());
            mascotComboBox.setValue(GameSession.getMascot());

            mascotComboBox.setOnAction(event -> {
                Mascot selected = mascotComboBox.getValue();
                if (selected != null) {
                    updateMascotDetails(selected);
                }
            });
        }

        // 3. Initialize visual details for the starting mascot
        updateMascotDetails(GameSession.getMascot());
    }

    private void updateMascotDetails(Mascot mascot) {
        if (mascot == null) return;

        // --- 1. Load Sprite Atlas & Set Viewport ---
        if (portraitImage != null) {
            var inputStream = getClass().getResourceAsStream("/application/assets/mascot-roster-atlas-v11.png");
            
            if (inputStream != null) {
                Image rosterAtlas = new Image(inputStream);
                portraitImage.setImage(rosterAtlas);

                // Calculate X coordinate based on mascot index in the Enum
                // Assuming 180x180 px tiles lined up horizontally in the atlas:
                double tileWidth = 180.0;
                double tileHeight = 180.0;
                double xOffset = mascot.ordinal() * tileWidth; 
                double yOffset = 0.0;

                portraitImage.setViewport(new Rectangle2D(xOffset, yOffset, tileWidth, tileHeight));
            } else {
                System.err.println("Could not load atlas image from /application/assets/mascot-roster-atlas-v11.png");
            }
        }

        // --- 2. Update UI Labels ---
        if (mascotNameLabel != null) {
            mascotNameLabel.setText(mascot.name().replace("_", " ") + " \u2022 CAPTAIN");
        }
        if (descriptionLabel != null) {
            descriptionLabel.setText("Fearless and upbeat mascot captain leading your club into matchday.");
        }
        if (statsLabel != null) {
            statsLabel.setText("POWER 4 | CONTACT 3 | PITCH 3 | SPEED 4 | THROW 3 | FIELD 3");
        }
        if (lineupLabel != null) {
            lineupLabel.setText("A balanced 9-player roster will be automatically generated around this captain.");
        }
    }

    @FXML
    private void handleStartGame() {
        Mascot selected = mascotComboBox != null ? mascotComboBox.getValue() : Mascot.TURBO_TANUKI;
        GameSession.selectMascot(selected);
        SceneManager.showPitchCustomization();
    }

    @FXML
    private void handleBack() {
        SceneManager.showMainMenu();
    }
}