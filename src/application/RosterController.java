package application;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public final class RosterController {
    @FXML private Label playerLabel, mascotNameLabel, descriptionLabel, statsLabel, lineupLabel;
    @FXML private ImageView portraitImage;
    @FXML private ComboBox<Mascot> mascotComboBox;
    @FXML private ComboBox<String> previewPose;
    private final Image roster = load("mascot-roster-atlas-v11.png");
    private final Image actions = load("mascot-action-atlas-v16.png");

    private static Image load(String name) {
        try (var stream = RosterController.class.getResourceAsStream("/application/" + name)) {
            if (stream == null) throw new IllegalStateException("Missing /application/" + name);
            Image image = new Image(stream);
            if (image.isError()) throw new IllegalStateException("Invalid image " + name, image.getException());
            return image;
        } catch (Exception error) { System.err.println(error.getMessage()); return null; }
    }
    @FXML private void initialize() {
        playerLabel.setText(GameSession.getPlayerName());
        mascotComboBox.getItems().setAll(Mascot.values());
        mascotComboBox.setValue(GameSession.getMascot());
        previewPose.getItems().setAll("Full character", "Idle", "Pitch", "Swing", "Run");
        previewPose.setValue("Full character");
        mascotComboBox.valueProperty().addListener((o,a,b) -> update());
        previewPose.valueProperty().addListener((o,a,b) -> update());
        update();
    }
    private void update() {
        Mascot mascot = mascotComboBox.getValue();
        if (mascot == null) return;
        int pose = previewPose.getSelectionModel().getSelectedIndex();
        Image image = actions;
        portraitImage.setImage(image);
        portraitImage.setPreserveRatio(true);
        if (image != null) {
            int columns = 4;
            int rows = 12;
            double w = image.getWidth() / columns, h = image.getHeight() / rows;
            int column = pose <= 0 ? 0 : pose - 1;
            int row = mascot.ordinal();
            portraitImage.setViewport(new Rectangle2D(column*w, row*h, w, h));
        }
        mascotNameLabel.setText(mascot.getDisplayName() + " • " + mascot.getSpecies());
        descriptionLabel.setText(image == null ? "Preview unavailable: missing mascot image. See application log." : mascot.getDescription());
        statsLabel.setText(String.format("POWER %.2f   CONTACT %.2f%nPITCH %.2f   RUN %.2f%nTHROW %.2f   FIELD / CATCH %.2f", mascot.getPowerMultiplier(), mascot.getContactMultiplier(), mascot.getPitchingMultiplier(), mascot.getSpeedMultiplier(), mascot.getThrowingMultiplier(), mascot.getFieldingMultiplier()));
        lineupLabel.setText("Choose a pose to inspect the complete mascot. Your captain selection is used when building your lineup.");
    }
    @FXML private void handleStartGame() {
        GameSession.selectMascot(mascotComboBox.getValue());
        SceneManager.showPitchCustomization();
    }
    @FXML private void handleBack() { SceneManager.showMainMenu(); }
}

