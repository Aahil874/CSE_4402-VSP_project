package application;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public final class PitchCustomizationController {
    @FXML private ComboBox<PitchStyle> styleBox;
    @FXML private ComboBox<PitchType> pitchOne;
    @FXML private ComboBox<PitchType> pitchTwo;
    @FXML private ComboBox<PitchType> pitchThree;
    @FXML private ComboBox<PitchType> pitchFour;
    @FXML private Label detailsLabel;
    @FXML private Label lineupLabel;

    @FXML private void initialize() {
        styleBox.getItems().setAll(PitchStyle.values());
        styleBox.setValue(GameSession.getPitchStyle());
        List<ComboBox<PitchType>> boxes = boxes();
        for (ComboBox<PitchType> box : boxes) {
            box.getItems().setAll(PitchType.values());
            box.valueProperty().addListener((o, a, b) -> updateDetails());
        }
        List<PitchType> saved = GameSession.getPitchLoadout();
        for (int i = 0; i < boxes.size(); i++) boxes.get(i).setValue(saved.get(i));
        styleBox.valueProperty().addListener((o, a, b) -> updateDetails());
        lineupLabel.setText("YOUR NINE: " + GameSession.getPlayerLineup().summary()
                + "\nRIVAL NINE: " + GameSession.getRivalLineup().summary());
        updateDetails();
    }
    @FXML private void handlePlay() {
        List<PitchType> loadout = boxes().stream().map(ComboBox::getValue).toList();
        if (loadout.stream().anyMatch(java.util.Objects::isNull) || loadout.stream().distinct().count() != 4) {
            detailsLabel.setText("Choose four different pitches before entering the stadium.");
            return;
        }
        GameSession.configurePitching(styleBox.getValue(), loadout);
        SceneManager.showGame();
    }
    @FXML private void handleBack() { SceneManager.showRoster(); }
    private void updateDetails() {
        PitchStyle style = styleBox.getValue();
        PitchType pitch = pitchOne.getValue();
        if (style == null || pitch == null) return;
        detailsLabel.setText(String.format(
                "%s  •  SPEED x%.2f  CONTROL x%.2f  BREAK x%.2f  •  %s%n%s: SPEED %d  SPIN %d  ACCURACY %d  STAMINA %d  TURBO %d  •  %s",
                style, style.speed(), style.control(), style.breakAmount(), style.catchAnimation(),
                pitch, pitch.speed(), pitch.spin(), pitch.accuracy(), pitch.staminaCost(),
                pitch.turboCost(), pitch.effect()));
    }
    private List<ComboBox<PitchType>> boxes() {
        List<ComboBox<PitchType>> result = new ArrayList<>();
        result.add(pitchOne); result.add(pitchTwo); result.add(pitchThree); result.add(pitchFour);
        return result;
    }
}
