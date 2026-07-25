package application;

import application.model.Player;
import application.model.Team;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

public class MainMenuController {

    // FXML UI Elements
    @FXML private TilePane menuTilePane; 
    @FXML private Button playMatchBtn;
    @FXML private Button trainingBtn;
    @FXML private Button seePlayersBtn;
    @FXML private Button transfersBtn;
    @FXML private Button selectStadiumBtn;
    @FXML private Label songLabel;

    // Team Data Models
    private Team myTeam;
    private Team rivalTeam;

    @FXML
    public void initialize() {
        // 1. Format menu grid
        if (menuTilePane != null) {
            menuTilePane.setOrientation(Orientation.HORIZONTAL);
            menuTilePane.setPrefColumns(3);     // 3 items per row
            menuTilePane.setPrefWidth(750);     
            menuTilePane.setMinWidth(750);      
            menuTilePane.setAlignment(Pos.CENTER);
        }

        myTeam = new Team("Home Stars", 50000);
     // Parameters: Name, Position, SpritePath, Contact, Power, Speed, PitchSpeed, Control
     myTeam.addPlayer(new Player("Alex Rodriguez", "Batter", "/pictures/player.png", 85, 90, 75, 0, 0));
     myTeam.addPlayer(new Player("Clayton Kershaw", "Pitcher", "/pictures/npc.png", 10, 5, 40, 92, 88));

     // 3. Initialize Rival Team and Roster
     rivalTeam = new Team("Away Giants", 45000);
     rivalTeam.addPlayer(new Player("Mike Trout", "Batter", "/pictures/player.png", 92, 95, 88, 0, 0));
     rivalTeam.addPlayer(new Player("Gerrit Cole", "Pitcher", "/pictures/npc.png", 15, 10, 30, 95, 91));

     System.out.println("Data successfully loaded inside initialize()!");
    }

    @FXML
    void onPlayMatch(ActionEvent event) {
        try {
            // Load MatchScreen.fxml from resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/MatchScreen.fxml"));
            Parent root = loader.load();
            
            // Get MatchScreenController and pass both Team objects
            MatchScreenController matchController = loader.getController();
            matchController.setupMatchData(myTeam, rivalTeam);
            
            // Switch Scene on the current Stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Could not load MatchScreen.fxml! Check your file path.");
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
        Platform.runLater(() -> {
            if (songLabel != null) {
                songLabel.setText("🎵 Now Playing: " + songName);
            }
        });
    }
}