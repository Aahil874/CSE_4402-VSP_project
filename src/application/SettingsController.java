package application;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

public final class SettingsController {
    @FXML
    private ComboBox<GameSettings.Difficulty> difficultyComboBox;
    @FXML
    private CheckBox strikeZoneCheckBox;
    @FXML
    private CheckBox screenShakeCheckBox;

    @FXML
    private void initialize() {
        difficultyComboBox.getItems().setAll(GameSettings.Difficulty.values());
        difficultyComboBox.setValue(GameSettings.getDifficulty());
        strikeZoneCheckBox.setSelected(GameSettings.isStrikeZoneVisible());
        screenShakeCheckBox.setSelected(GameSettings.isScreenShakeEnabled());
    }

    @FXML
    private void handleSave() {
        GameSettings.setDifficulty(difficultyComboBox.getValue());
        GameSettings.setStrikeZoneVisible(strikeZoneCheckBox.isSelected());
        GameSettings.setScreenShakeEnabled(screenShakeCheckBox.isSelected());
        SceneManager.showMainMenu();
    }

    @FXML
    private void handleCancel() {
        SceneManager.showMainMenu();
    }
}
