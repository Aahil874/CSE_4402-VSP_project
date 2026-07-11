package application;

import application.model.Player;
import application.model.Team;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.util.Random;

public class MatchScreenController {

    @FXML private Label homeTeamLabel;
    @FXML private Label homeScoreLabel;
    @FXML private Label awayTeamLabel;
    @FXML private Label awayScoreLabel;
    @FXML private Label inningLabel;
    @FXML private TextArea commentaryBox;
    @FXML private Button nextPitchBtn;
    @FXML private Button leaveMatchBtn;

    private Team homeTeam;
    private Team awayTeam;
    private Random random = new Random();

    private int homeScore = 0;
    private int awayScore = 0;
    private int inning = 1;
    private boolean isTopInning = true;
    private int outs = 0;

    // This custom method allows the MainMenu to pass data directly to this screen!
    public void setupMatchData(Team home, Team away) {
        this.homeTeam = home;
        this.awayTeam = away;
        
        homeTeamLabel.setText(home.getTeamName());
        awayTeamLabel.setText(away.getTeamName());
        
        commentaryBox.setText("Welcome to the ballpark! Press 'Simulate Next Pitch' to begin the match.\n");
        updateInningDisplay();
    }

    @FXML
    void onNextPitch(ActionEvent event) {
        Team battingTeam = isTopInning ? awayTeam : homeTeam;
        Team pitchingTeam = isTopInning ? homeTeam : awayTeam;
        
        Player batter = battingTeam.getRoster().get(0);
        Player pitcher = pitchingTeam.getRoster().stream()
                .filter(p -> "Pitcher".equalsIgnoreCase(p.getPosition()))
                .findFirst().orElse(pitchingTeam.getRoster().get(0));

        int roll = random.nextInt(100) + 1;
        int calculation = roll + (batter.getContact() - pitcher.getPitchingControl());

        if (calculation > 75) {
            if (isTopInning) awayScore++; else homeScore++;
            commentaryBox.appendText("💥 BOOM! " + batter.getName() + " hits a deep line drive! A run scores!\n");
        } else if (calculation < 45) {
            outs++;
            commentaryBox.appendText("❌ STRIKE THREE! " + pitcher.getName() + " freezes the batter.\n");
        } else {
            commentaryBox.appendText("⚾ Foul ball out of play. Count resets.\n");
        }

        // Sync Scoreboard UI
        homeScoreLabel.setText(String.valueOf(homeScore));
        awayScoreLabel.setText(String.valueOf(awayScore));

        // Inning progression management
        if (outs >= 3) {
            outs = 0;
            commentaryBox.appendText("\n--- 3 Outs! Teams switch sides ---\n\n");
            if (isTopInning) {
                isTopInning = false;
            } else {
                isTopInning = true;
                inning++;
            }
            
            if (inning > 9) {
                endGame();
                return;
            }
        }
        updateInningDisplay();
    }

    private void updateInningDisplay() {
        String half = isTopInning ? "Top" : "Bottom";
        inningLabel.setText("Inning: " + half + " of the " + inning + " | Outs: " + outs);
    }

    private void endGame() {
        nextPitchBtn.setDisable(true);
        leaveMatchBtn.setDisable(false);
        inningLabel.setText("Match Over!");
        
        commentaryBox.appendText("🏁 The game has concluded!\n");
        if (homeScore > awayScore) {
            commentaryBox.appendText("🎉 " + homeTeam.getTeamName() + " wins the game!");
        } else if (awayScore > homeScore) {
            commentaryBox.appendText("🎉 " + awayTeam.getTeamName() + " wins the game!");
        } else {
            commentaryBox.appendText("🤝 It's a dead draw!");
        }
    }

    @FXML
    void onLeaveMatch(ActionEvent event) {
        try {
            // Standard JavaFX screen switcher back to main menu
            Parent root = FXMLLoader.load(getClass().getResource("/application/MainMenu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}