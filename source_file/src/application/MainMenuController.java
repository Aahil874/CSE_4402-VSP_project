package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane; // Added this import

public class MainMenuController {

    // 1. Inject the TilePane from your FXML
    @FXML private TilePane menuTilePane; 

    @FXML private Button playMatchBtn;
    @FXML private Button trainingBtn;
    @FXML private Button seePlayersBtn;
    @FXML private Button transfersBtn;
    @FXML private Button selectStadiumBtn;
    @FXML private Label songLabel;

    // 2. This runs automatically as soon as the FXML file is loaded by Java
    @FXML
    public void initialize() {
        if (menuTilePane != null) {
            menuTilePane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
            menuTilePane.setPrefColumns(3);     // Forces 3 items per row maximum
            menuTilePane.setPrefWidth(750);     // Broadens layout window area
            menuTilePane.setMinWidth(750);      // Prevents VBox from crushing it skinny
            menuTilePane.setAlignment(javafx.geometry.Pos.CENTER); // Perfectly centers the grid
        }
    }

    @FXML
    void onPlayMatch(ActionEvent event) {
        System.out.println("Navigating to: Play New Match Screen");
    }

    @FXML
    void onTraining(ActionEvent event) {
        System.out.println("Navigating to: Training Screen");
    }

    @FXML
    void onSeePlayers(ActionEvent event) {
        System.out.println("Navigating to: See Your Players Screen");
    }

    @FXML
    void onTransfers(ActionEvent event) {
        System.out.println("Navigating to: Player Transfers Screen");
    }

    @FXML
    void onSelectStadium(ActionEvent event) {
        System.out.println("Navigating to: Select Stadium Screen");
    }
    public void updateSongLabel(String songName) {
        javafx.application.Platform.runLater(() -> {
            if (songLabel != null) {
                songLabel.setText("🎵 Now Playing: " + songName);
            }
        });
    }
}
