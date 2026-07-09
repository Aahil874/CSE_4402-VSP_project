package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MainMenuController {

    @FXML private Button playMatchBtn;
    @FXML private Button trainingBtn;
    @FXML private Button seePlayersBtn;
    @FXML private Button transfersBtn;
    @FXML private Button selectStadiumBtn;

    @FXML
    void onPlayMatch(ActionEvent event) {
        System.out.println("Navigating to: Play New Match Screen");
        // We will add scene switching code here later!
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
}