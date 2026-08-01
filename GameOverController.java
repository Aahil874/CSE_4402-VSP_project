package application;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public final class GameOverController {
    @FXML
    private Label resultLabel;
    @FXML
    private Label playerLabel;
    @FXML
    private Label mascotLabel;
    @FXML
    private Label difficultyLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label rivalScoreLabel;
    @FXML
    private Label hitsLabel;
    @FXML
    private Label homeRunsLabel;
    @FXML
    private Label inningsLabel;
    @FXML
    private Label comboLabel;
    @FXML
    private Label hitBreakdownLabel;
    @FXML
    private Label disciplineLabel;
    @FXML
    private Label opponentLabel;
    @FXML
    private ImageView resultMascotImage;
    @FXML
    private Label saveStatusLabel;
    @FXML
    private Button saveButton;

    private final GameBackendService backend = GameBackendService.getInstance();

    @FXML
    private void initialize() {
        int comparison = Integer.compare(GameSession.getScore(), GameSession.getRivalScore());
        resultLabel.setText(comparison > 0
                ? "VICTORY LAP!"
                : comparison < 0 ? "RIVAL WINS - REMATCH!" : "PHOTO FINISH TIE!");
        Image expressions = new Image(getClass().getResourceAsStream(
                "/application/mascot-expression-atlas-v10.png"));
        resultMascotImage.setImage(expressions);
        int expressionColumn = comparison > 0 ? 5 : comparison < 0 ? 4 : 2;
        resultMascotImage.setViewport(new Rectangle2D(
                expressionColumn * 256,
                GameSession.getMascot().ordinal() * 256,
                256, 256));
        playerLabel.setText(GameSession.getPlayerName().toUpperCase());
        mascotLabel.setText(GameSession.getMascot().getDisplayName());
        difficultyLabel.setText(GameSession.getDifficulty().toString());
        scoreLabel.setText(Integer.toString(GameSession.getScore()));
        rivalScoreLabel.setText(Integer.toString(GameSession.getRivalScore()));
        hitsLabel.setText(Integer.toString(GameSession.getHits()));
        homeRunsLabel.setText(Integer.toString(GameSession.getHomeRuns()));
        inningsLabel.setText(Integer.toString(GameSession.getInnings()));
        comboLabel.setText("x" + GameSession.getMaxCombo());
        hitBreakdownLabel.setText(String.format("%d / %d / %d",
                GameSession.getSingles(), GameSession.getDoubles(),
                GameSession.getTriples()));
        disciplineLabel.setText(String.format("%d / %d",
                GameSession.getWalks(), GameSession.getStrikeouts()));
        opponentLabel.setText(GameSession.getOpponentName() + "  •  "
                + GameSession.getDurationSeconds() + " SEC");
    }

    @FXML
    private void handleSaveScore() {
        HighScore highScore = new HighScore(
                GameSession.getPlayerName(),
                GameSession.getScore(),
                GameSession.getRivalScore(),
                GameSession.getHits(),
                GameSession.getHomeRuns(),
                GameSession.getInnings(),
                GameSession.getMaxCombo(),
                GameSession.getMascot(),
                GameSession.getDifficulty(),
                GameSession.getSingles(),
                GameSession.getDoubles(),
                GameSession.getTriples(),
                GameSession.getWalks(),
                GameSession.getStrikeouts(),
                GameSession.getPitchCount(),
                GameSession.getDurationSeconds(),
                GameSession.getOpponentName());
        saveButton.setDisable(true);
        saveStatusLabel.setText("SAVING MATCH + CAREER STATS...");
        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                backend.saveCompletedMatch(highScore);
                return null;
            }
        };
        saveTask.setOnSucceeded(event -> {
            saveButton.setDisable(true);
            saveButton.setText("SAVED!");
            saveStatusLabel.setText("MATCH + CAREER STATS SAVED ATOMICALLY");
        });
        saveTask.setOnFailed(event -> {
            saveButton.setDisable(false);
            saveStatusLabel.setText("MYSQL UNAVAILABLE - RECONNECT FROM THE MAIN MENU");
        });
        Thread thread = new Thread(saveTask, "mbgp-save-match");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handlePlayAgain() {
        GameSession.start(GameSession.getPlayerName());
        SceneManager.showRoster();
    }

    @FXML
    private void handleLeaderboard() {
        SceneManager.showLeaderboard();
    }

    @FXML
    private void handleMainMenu() {
        SceneManager.showMainMenu();
    }
}
