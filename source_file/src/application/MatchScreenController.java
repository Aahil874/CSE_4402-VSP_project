package application;

import application.model.Player;
import application.model.Team;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class MatchScreenController {

    @FXML private Pane gameContainer;
    @FXML private ImageView pitcherSprite;
    @FXML private ImageView batterSprite;
    @FXML private ImageView runnerSprite;
    @FXML private Circle baseball;
    
    @FXML private Label homeScoreLabel;
    @FXML private Label awayScoreLabel;
    @FXML private Label feedbackLabel;
    @FXML private Label countLabel;
    @FXML private TextArea commentaryBox;

    private Team homeTeam;
    private Team awayTeam;
    
    // Active player stats loaded from DB/Model
    private Player currentBatter;
    private Player currentPitcher;

    // Real-time Physics Engine Variables
    private double ballX, ballY;
    private double ballVelX, ballVelY;
    private boolean isPitchInFlight = false;
    private boolean isSwinging = false;
    private boolean isBallHit = false;
    
    // Base Runner Logic
    private boolean isRunnerActive = false;
    private double runnerX, runnerY;
    private int runnerTargetBase = 0; // 1: 1st, 2: 2nd, 3: 3rd, 4: Home

    private AnimationTimer gameLoop;

    public void setupMatchData(Team homeTeam, Team awayTeam) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;

        // Fetch active players from team roster
        if (homeTeam != null && !homeTeam.getRoster().isEmpty()) {
            this.currentBatter = homeTeam.getRoster().get(0);
            loadSpriteTexture(batterSprite, currentBatter.getSpritePath());
        }
        if (awayTeam != null && !awayTeam.getRoster().isEmpty()) {
            this.currentPitcher = awayTeam.getRoster().get(0);
            loadSpriteTexture(pitcherSprite, currentPitcher.getSpritePath());
        }

        startGameLoop();
    }

    private void startGameLoop() {
        if (gameLoop != null) gameLoop.stop();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGamePhysics();
            }
        };
        gameLoop.start();
    }

    /**
     * Main 60 FPS Engine
     */
    private void updateGamePhysics() {
        // 1. Move Ball
        if (isPitchInFlight) {
            ballX += ballVelX;
            ballY += ballVelY;

            baseball.setLayoutX(ballX);
            baseball.setLayoutY(ballY);

            // Ball reaches catcher without swing
            if (!isBallHit && ballY >= 355) {
                isPitchInFlight = false;
                baseball.setVisible(false);
                feedbackLabel.setText("STRIKE!");
                logCommentary("Pitcher threw a strike inside the zone.");
            }

            // Ball flying after hit
            if (isBallHit) {
                checkHitLanding();
            }
        }

        // 2. Base Runner Physics
        if (isRunnerActive) {
            updateRunnerMovement();
        }
    }

    /**
     * Keyboard Input Handler
     */
    @FXML
    void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            handleBatterSwing();
        } else if (event.getCode() == KeyCode.A) {
            // Shift batter left
            batterSprite.setLayoutX(Math.max(320, batterSprite.getLayoutX() - 5));
        } else if (event.getCode() == KeyCode.D) {
            // Shift batter right
            batterSprite.setLayoutX(Math.min(375, batterSprite.getLayoutX() + 5));
        } else if (event.getCode() == KeyCode.R) {
            // Trigger manual base running
            triggerBaseRunning();
        }
    }

    /**
     * Calculates Hit Outcome based on Timing + DB Attributes
     */
    private void handleBatterSwing() {
        if (!isPitchInFlight || isBallHit || isSwinging) return;
        isSwinging = true;

        // Perfect home plate timing line is Y = 340
        double timingDelta = Math.abs(ballY - 340);

        // Stats modifier from Database (Default scale 1-100)
        double batterContact = currentBatter != null ? currentBatter.getContact() : 70;
        double batterPower = currentBatter != null ? currentBatter.getPower() : 70;

        // Expanded window based on batter contact stat
        double perfectWindow = 12 + (batterContact * 0.1); 
        double goodWindow = 30 + (batterContact * 0.2);

        if (timingDelta <= perfectWindow) {
            // PERFECT TIMING -> HOME RUN or BIG HIT
            isBallHit = true;
            feedbackLabel.setText("IT'S OUT OF THE PARK! HOME RUN!");
            logCommentary(currentBatter.getName() + " crushed the ball for a HOME RUN!");

            double exitVelocity = 7.0 + (batterPower * 0.05);
            ballVelY = -exitVelocity; 
            ballVelX = (Math.random() - 0.5) * 6.0;

            triggerBaseRunning();
        } else if (timingDelta <= goodWindow) {
            // GOOD TIMING -> BASE HIT
            isBallHit = true;
            feedbackLabel.setText("GREAT CONTACT! BASE HIT!");
            logCommentary(currentBatter.getName() + " hits a solid line drive into the field.");

            ballVelY = -(3.0 + (batterPower * 0.03));
            ballVelX = (Math.random() - 0.5) * 8.0;

            triggerBaseRunning();
        } else if (timingDelta <= goodWindow + 25) {
            // FOUL BALL
            isBallHit = true;
            feedbackLabel.setText("FOUL BALL!");
            ballVelY = -2.0;
            ballVelX = (ballX < 400) ? -6.0 : 6.0;
        } else {
            // MISSED SWING
            feedbackLabel.setText("SWING AND A MISS!");
        }
    }

    private void checkHitLanding() {
        double boundsHeight = gameContainer.getHeight();
        double boundsWidth = gameContainer.getWidth();

        if (ballY < -20 || ballX < 0 || ballX > boundsWidth || ballY > boundsHeight) {
            isPitchInFlight = false;
            baseball.setVisible(false);
        }
    }

    private void triggerBaseRunning() {
        isRunnerActive = true;
        runnerSprite.setVisible(true);
        runnerX = 392;
        runnerY = 352;
        runnerTargetBase = 1;
    }

    private void updateRunnerMovement() {
        double runnerSpeed = currentBatter != null ? currentBatter.getSpeed() * 0.04 : 2.5;

        // Base coordinates: 1st (513, 203) -> 2nd (393, 73) -> 3rd (273, 203) -> Home (392, 352)
        double targetX = 513, targetY = 203;
        if (runnerTargetBase == 2) { targetX = 393; targetY = 73; }
        else if (runnerTargetBase == 3) { targetX = 273; targetY = 203; }
        else if (runnerTargetBase == 4) { targetX = 392; targetY = 352; }

        double dx = targetX - runnerX;
        double dy = targetY - runnerY;
        double distance = Math.hypot(dx, dy);

        if (distance < runnerSpeed) {
            runnerX = targetX;
            runnerY = targetY;
            if (runnerTargetBase < 4) {
                runnerTargetBase++; // Move to next base
            } else {
                isRunnerActive = false;
                runnerSprite.setVisible(false);
                logCommentary("RUNNER SCORED A RUN!");
            }
        } else {
            runnerX += (dx / distance) * runnerSpeed;
            runnerY += (dy / distance) * runnerSpeed;
        }

        runnerSprite.setLayoutX(runnerX);
        runnerSprite.setLayoutY(runnerY);
    }

    @FXML
    void onAIStartPitch(ActionEvent event) {
        // Request key focus on the game container for controls
        gameContainer.requestFocus();

        if (isPitchInFlight) return;

        // Pitcher speed based on pitcher DB attribute
        double pitchSpeedStat = currentPitcher != null ? currentPitcher.getPitchSpeed() : 70;
        double speedY = 4.0 + (pitchSpeedStat * 0.04);

        ballX = 400;
        ballY = 188;
        baseball.setLayoutX(ballX);
        baseball.setLayoutY(ballY);

        ballVelX = (Math.random() - 0.5) * 1.5; // Curve/breaking pitch variation
        ballVelY = speedY;

        isSwinging = false;
        isBallHit = false;
        isPitchInFlight = true;
        baseball.setVisible(true);
        feedbackLabel.setText("");
    }

    private void loadSpriteTexture(ImageView view, String path) {
        if (path == null || path.isEmpty()) return;
        try {
            Image img = new Image(getClass().getResourceAsStream(path));
            view.setImage(img);
        } catch (Exception e) {
            System.err.println("Could not load player texture: " + path);
        }
    }

    private void logCommentary(String text) {
        if (commentaryBox != null) {
            commentaryBox.appendText(text + "\n");
        }
    }

    @FXML
    void onLeaveMatch(ActionEvent event) {
        if (gameLoop != null) gameLoop.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/MainMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}