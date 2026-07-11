package application;

import application.model.Team;
import application.model.MatchSimulator;
import application.model.Player;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane; // Added this import
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class MainMenuController {

    // 1. Inject the TilePane from your FXML
    @FXML private TilePane menuTilePane; 

    @FXML private Button playMatchBtn;
    @FXML private Button trainingBtn;
    @FXML private Button seePlayersBtn;
    @FXML private Button transfersBtn;
    @FXML private Button selectStadiumBtn;
    @FXML private Label songLabel;
    private Team myTeam;
    private Team rivalTeam;

    // 2. This runs automatically as soon as the FXML file is loaded by Java
    //its work is also to make sure it creates some dummy players inside the game initially!
    @FXML
    public void initialize() {
        if (menuTilePane != null) {
            menuTilePane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
            menuTilePane.setPrefColumns(3);     // Forces 3 items per row maximum
            menuTilePane.setPrefWidth(750);     // Broadens layout window area
            menuTilePane.setMinWidth(750);      // Prevents VBox from crushing it skinny
            menuTilePane.setAlignment(javafx.geometry.Pos.CENTER); // Perfectly centers the grid
        }
        myTeam = new Team("Home Stars", 50000);
        myTeam.addPlayer(new Player("Alex Rodriguez", "Batter", 85, 90, 75, 0, 0, 80));
        myTeam.addPlayer(new Player("Clayton Kershaw", "Pitcher", 10, 5, 40, 92, 88, 85));

        rivalTeam = new Team("Away Giants", 45000);
        rivalTeam.addPlayer(new Player("Mike Trout", "Batter", 92, 95, 88, 0, 0, 90));

        rivalTeam.addPlayer(new Player("Gerrit Cole", "Pitcher", 15, 10, 30, 95, 91, 88));

        System.out.println("Data successfully loaded inside the existing initialize method!");
    }

    @FXML
    void onPlayMatch(ActionEvent event) {
        try {
            // 1. Create the loader pointing to the new XML screen setup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/MatchScreen.fxml"));
            Parent root = loader.load();
            
            // 2. Fetch the target controller instance right before switching screens
            MatchScreenController matchController = loader.getController();
            
            // 3. Hand off the current team variables straight into the match engine setup!
            matchController.setupMatchData(myTeam, rivalTeam);
            
            // 4. Swap the display stage view gracefully
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Could not load the Match Screen scene! Check file paths.");
            e.printStackTrace();
        }
    }

    @FXML
    void onTraining(ActionEvent event) {
        System.out.println("Navigating to: Training Screen");
    }

    @FXML
    void onSeePlayers(ActionEvent event) {
        System.out.println("\n=== " + myTeam.getTeamName() + " ROSTER ===");
        
        // Loop through every player on the team and print their details
        for (Player p : myTeam.getRoster()) {
            System.out.println("- " + p.getName() + " | Position: " + p.getPosition() 
                               + " | OVR: " + p.getOverallRating());
        }
        System.out.println("===============================\n");
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
