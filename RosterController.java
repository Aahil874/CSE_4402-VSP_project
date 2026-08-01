package application;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.geometry.Rectangle2D;
public final class RosterController {
    @FXML private Label playerLabel;
    @FXML private Label mascotNameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label statsLabel;
    @FXML private Label lineupLabel;
    @FXML private ComboBox<Mascot> mascotComboBox;
    @FXML private ImageView portraitImage;

    @FXML private void initialize() {
        playerLabel.setText(GameSession.getPlayerName().toUpperCase());
        mascotComboBox.getItems().setAll(Mascot.values());
        mascotComboBox.setValue(GameSession.getMascot());
        mascotComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updatePreview());
        updatePreview();
    }

    @FXML private void handleStartGame() {
        GameSession.selectMascot(mascotComboBox.getValue());
        SceneManager.showPitchCustomization();
    }
    @FXML private void handleBack() { SceneManager.showMainMenu(); }

    private void updatePreview() {
        Mascot mascot = mascotComboBox.getValue() == null ? Mascot.TURBO_TANUKI : mascotComboBox.getValue();
        int ordinal = mascot.ordinal();
        portraitImage.setViewport(new Rectangle2D((ordinal % 3) * 341.0, (ordinal / 3) * 384.0, 341, 384));
        mascotNameLabel.setText(mascot.getDisplayName() + "  •  " + mascot.getSpecies().toUpperCase());
        descriptionLabel.setText(String.format(
                "%s%nTEAM: %s  •  HOME: %s%nPITCH: %s  •  HIT: %s%nCELEBRATION: %s",
                mascot.getDescription(), mascot.getTeamName(), mascot.getStadiumName(),
                mascot.getSignaturePitch(), mascot.getSignatureHit(), mascot.getCelebration()));
        statsLabel.setText(String.format("POWER %d  CONTACT %d  PITCH %d  SPEED %d  THROW %d  FIELD %d",
                rating(mascot.getPowerMultiplier()), rating(mascot.getContactMultiplier()),
                rating(mascot.getPitchingMultiplier()), rating(mascot.getSpeedMultiplier()),
                rating(mascot.getThrowingMultiplier()), rating(mascot.getFieldingMultiplier())));
        lineupLabel.setText("A random nine-mascot club will be built around this captain.");
    }
    private int rating(double value) { return (int) Math.round(Math.max(1, Math.min(5, 3 + (value - 1) * 8))); }
}
