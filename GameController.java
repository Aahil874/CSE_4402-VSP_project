package application;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class GameController {
    private static final double BALL_START_X = 560;
    private static final double BALL_RELEASE_Y = 273;
    private static final double PLATE_X = 560;
    private static final double PLATE_Y = 548;
    private static final double STRIKE_ZONE_TOP = 510;
    private static final double STRIKE_ZONE_BOTTOM = 610;
    private static final double AIM_MIN_Y = STRIKE_ZONE_TOP - 48;
    private static final double AIM_MAX_Y = STRIKE_ZONE_BOTTOM + 48;
    private static final double BASE_CONTACT_WINDOW = 82;
    private static final double BAT_TARGET_MIN_X = PLATE_X - 78;
    private static final double BAT_TARGET_MAX_X = PLATE_X + 78;
    private static final double SWING_ANIMATION_DURATION = 0.16;
    private static final double FIRST_RIVAL_PITCH_DELAY = 0.70;
    private static final double NEXT_RIVAL_PITCH_DELAY = 0.45;
    private static final int TOTAL_INNINGS = 3;
    private static final int SPRITE_COLUMNS = 4;
    private static final int SPRITE_ROWS = 12;
    private static final double SPRITE_CELL_WIDTH = 320;
    private static final double SPRITE_CELL_HEIGHT = 384;
    private static final double SPRITE_BOX_WIDTH = 160;
    private static final double SPRITE_BOX_HEIGHT = 192;

    @FXML
    private Canvas gameCanvas;
    @FXML
    private Label playerLabel;
    @FXML
    private Label mascotLabel;
    @FXML
    private Label difficultyLabel;
    @FXML
    private Label phaseLabel;
    @FXML
    private Label inningLabel;
    @FXML
    private Label playerRunsLabel;
    @FXML
    private Label rivalRunsLabel;
    @FXML
    private Label ballsLabel;
    @FXML
    private Label strikesLabel;
    @FXML
    private Label outsLabel;
    @FXML
    private Label comboLabel;
    @FXML
    private Label baseDiamondLabel;
    @FXML
    private Label pitchMetricsLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label pitchTypeLabel;
    @FXML
    private Label aimLabel;
    @FXML
    private ProgressBar turboMeter;
    @FXML
    private Label turboLabel;
    @FXML
    private Button primaryButton;
    @FXML
    private Button swingButton;
    @FXML
    private Button pauseButton;

    private final Random random = new Random();
    private final List<Particle> particles = new ArrayList<>();
    private final MatchState matchState = new MatchState();
    private final ScoreController scoreController = new ScoreController(matchState);
    private final RunnerController runnerController = new RunnerController();
    private final LiveBallController liveBallController = new LiveBallController();
    private final BattingController battingController = new BattingController();
    private final BatSwingController batSwingController = new BatSwingController();
    private final BaseballPlayStateController playStateController =
            new BaseballPlayStateController();
    private final PitchSequenceController pitchSequenceController =
            new PitchSequenceController();
    private final CameraTransitionController cameraTransitionController =
            new CameraTransitionController();
    private final MascotRenderController mascotRenderController =
            new MascotRenderController();
    private final CompetitiveAIController competitiveAIController = new CompetitiveAIController();
    private final CatchAnimationController catchAnimationController = new CatchAnimationController();
    private final GroundMovementController groundMovementController = new GroundMovementController();
    private final DefensiveAssignmentController defensiveAssignmentController =
            new DefensiveAssignmentController();
    private final CrowdController crowdController = new CrowdController();
    private final MascotAnimationController mascotAnimationController =
            new MascotAnimationController();
    private final Image stadiumBackground =
            loadImage("/application/stadium-competitive-perspective-v14-4k.png");
    private final Image gameplaySprites =
            loadImage("/application/mascot-action-atlas-v16.png");
    private final Image fieldingSprites =
            loadImage("/application/mascot-fielding-atlas-v10.png");
    private final Image turboTanukiSprites =
            loadImage("/application/turbo-tanuki-action-atlas-v15.png");

    private AnimationTimer gameLoop;
    private long previousFrame;
    private Phase phase = Phase.PITCHING;
    private boolean pitchActive;
    private boolean paused;
    private double ballX;
    private double ballY;
    private double pitchSpeed;
    private double pitchTravel;
    private double pitchTargetY = zoneCenterY();
    private double aimY = zoneCenterY();
    private double batOffsetX;
    private double visualAimY = zoneCenterY();
    private double visualBatOffsetX;
    private double pointerX = PLATE_X;
    private double pointerY = zoneCenterY();
    private double automaticPitchDelay;
    private double shakeStrength;
    private double animationClock;
    private double swingAnimation;
    private double pitchAnimation;
    private double contactFlash;
    private double catchPoseLife;
    private double catchPoseX;
    private double catchPoseY;
    private Mascot catchPoseMascot = Mascot.TURBO_TANUKI;
    private boolean fieldingActive;
    private double fieldingTimer;
    private double fieldingTargetX;
    private double fieldingTargetY;
    private double floatingTextLife;
    private String floatingText = "";
    private Color floatingTextColor = Color.WHITE;
    private int selectedPitchIndex;
    private PitchType activePitch = PitchType.FOUR_SEAM;
    private int playerLineupIndex;
    private int rivalLineupIndex;
    private Mascot actionPlayerMascot;
    private Mascot actionRivalMascot;
    private final WorldPoint[] fielderFeet = {
            new WorldPoint(300, 335), new WorldPoint(445, 245),
            new WorldPoint(675, 245), new WorldPoint(820, 335),
            new WorldPoint(270, 145), new WorldPoint(560, 92),
            new WorldPoint(850, 145)
    };

    @FXML
    private void initialize() {
        actionPlayerMascot = playerMascot();
        actionRivalMascot = rivalMascot();
        playerLabel.setText(GameSession.getPlayerName().toUpperCase());
        mascotLabel.setText("AT BAT: " + playerMascot().getDisplayName());
        difficultyLabel.setText(GameSettings.getDifficulty().toString());
        configureKeyboardControls();
        configureMouseControls();
        updateControlsForPhase();
        updateHud();
        drawGame();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (previousFrame == 0) {
                    previousFrame = now;
                }
                double elapsed = Math.min((now - previousFrame) / 1_000_000_000.0, 0.05);
                previousFrame = now;

                if (!paused) {
                    updateGame(elapsed);
                    updateEffects(elapsed);
                }
                drawGame();
            }
        };
        gameLoop.start();
        Platform.runLater(gameCanvas::requestFocus);
    }

    private void configureKeyboardControls() {
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();
            if (key == KeyCode.SPACE) {
                handleSwing();
            } else if (key == KeyCode.P || key == KeyCode.ENTER) {
                handlePrimaryAction();
            } else if (key == KeyCode.UP || key == KeyCode.W) {
                handleAimUp();
            } else if (key == KeyCode.DOWN || key == KeyCode.S) {
                handleAimDown();
            } else if (key == KeyCode.LEFT || key == KeyCode.A) {
                handleLeft();
            } else if (key == KeyCode.RIGHT || key == KeyCode.D) {
                handleRight();
            } else if (key == KeyCode.ESCAPE) {
                handlePause();
            }
            event.consume();
        });
    }

    private void configureMouseControls() {
        gameCanvas.setOnMouseEntered(event -> gameCanvas.requestFocus());
        gameCanvas.setOnMouseMoved(event -> updatePointer(event.getX(), event.getY()));
        gameCanvas.setOnMouseDragged(event -> updatePointer(event.getX(), event.getY()));
        gameCanvas.setOnContextMenuRequested(event -> event.consume());
        gameCanvas.setOnMousePressed(event -> {
            updatePointer(event.getX(), event.getY());
            if (event.getButton() == MouseButton.SECONDARY) {
                visualAimY = aimY;
                visualBatOffsetX = batOffsetX;
                handleSwing();
            } else if (event.getButton() == MouseButton.PRIMARY) {
                handlePrimaryAction();
            }
            event.consume();
        });
        gameCanvas.setOnScroll(event -> {
            if (phase == Phase.PITCHING && !pitchActive && !paused) {
                if (event.getDeltaY() > 0) {
                    handleRight();
                } else if (event.getDeltaY() < 0) {
                    handleLeft();
                }
            }
            event.consume();
        });
    }

    private void updatePointer(double x, double y) {
        if (paused || phase == Phase.FINISHED) {
            return;
        }
        pointerX = clamp(x, 0, gameCanvas.getWidth());
        pointerY = clamp(y, 0, gameCanvas.getHeight());
        aimY = clamp(pointerX, AIM_MIN_Y, AIM_MAX_Y);
        if (phase == Phase.BATTING) {
            batOffsetX = clamp(pointerX, BAT_TARGET_MIN_X, BAT_TARGET_MAX_X) - PLATE_X;
        }
        updateAimLabel();
    }

    private void updateGame(double elapsed) {
        if (phase == Phase.BATTING && !pitchActive && automaticPitchDelay > 0) {
            automaticPitchDelay -= elapsed;
            if (automaticPitchDelay <= 0) {
                launchRivalPitch();
            }
        }

        if (!pitchActive) {
            return;
        }

        pitchTravel = Math.min(1.08, pitchTravel
                + pitchSpeed * elapsed / (PLATE_Y - BALL_RELEASE_Y));
        double progress = pitchProgress(ballX);
        double previousX = ballX;
        double previousY = ballY;
        ballX = trajectoryY(progress, activePitch, pitchTargetY);
        ballY = BALL_RELEASE_Y + (PLATE_Y - BALL_RELEASE_Y) * progress;
        liveBallController.markPitchPosition(ballX, ballY,
                (ballX - previousX) / Math.max(.001, elapsed),
                (ballY - previousY) / Math.max(.001, elapsed));

        if (pitchTravel >= 1.0) {
            if (phase == Phase.PITCHING) {
                resolveRivalAtBat();
            } else {
                resolveTakenPitch();
            }
        }
    }

    private void updateEffects(double elapsed) {
        animationClock += elapsed;
        mascotAnimationController.update(elapsed);
        batSwingController.update(elapsed);
        cameraTransitionController.update(elapsed);
        liveBallController.update(elapsed);
        if (liveBallController.state() == LiveBallController.State.DEAD_BALL
                && (playStateController.is(BaseballPlayStateController.State.BALL_IN_PLAY)
                || playStateController.is(BaseballPlayStateController.State.FIELDING)
                || playStateController.is(BaseballPlayStateController.State.RUNNERS_ADVANCING))) {
            resolvePlayState();
        }
        runnerController.update(elapsed);
        int completedRuns = runnerController.drainScoredRuns();
        if (completedRuns > 0) {
            matchState.addPlayerRuns(completedRuns);
            showFloating("RUNS SCORE +" + completedRuns, Color.web("#ffe066"));
            updateHud();
        }
        matchState.setOccupiedBases(
                runnerController.isOccupied(1),
                runnerController.isOccupied(2),
                runnerController.isOccupied(3));
        if (baseDiamondLabel != null) {
            baseDiamondLabel.setText(scoreController.baseDiamondText());
        }
        shakeStrength = Math.max(0, shakeStrength - elapsed * 30);
        boolean swingJustFinished = swingAnimation > 0 && swingAnimation <= elapsed;
        swingAnimation = Math.max(0, swingAnimation - elapsed);
        pitchAnimation = Math.max(0, pitchAnimation - elapsed);
        contactFlash = Math.max(0, contactFlash - elapsed);
        catchPoseLife = Math.max(0, catchPoseLife - elapsed);
        visualAimY += (aimY - visualAimY) * Math.min(1, elapsed * 11);
        visualBatOffsetX += (batOffsetX - visualBatOffsetX) * Math.min(1, elapsed * 12);

        if (fieldingActive) {
            fieldingTimer = Math.max(0, fieldingTimer - elapsed);
            if (fieldingTimer <= 0) {
                resolveMissedFieldingPlay();
            }
        }
        crowdController.update(elapsed);
        updateFielderFeet(elapsed);
        floatingTextLife = Math.max(0, floatingTextLife - elapsed);
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.x += particle.velocityX * elapsed;
            particle.y += particle.velocityY * elapsed;
            particle.velocityY += 90 * elapsed;
            particle.life -= elapsed;
            if (particle.life <= 0) {
                iterator.remove();
            }
        }
        if (swingJustFinished) {
            updateHud();
        }
    }

    @FXML
    private void handlePrimaryAction() {
        gameCanvas.requestFocus();
        if (paused || phase == Phase.FINISHED) {
            return;
        }
        if (fieldingActive) {
            attemptFieldingCatch();
            return;
        }
        if (pitchActive) {
            statusLabel.setText("THE BALL IS ALREADY IN PLAY!");
            return;
        }

        if (phase == Phase.PITCHING) {
            launchPlayerPitch();
        } else {
            automaticPitchDelay = 0;
            launchRivalPitch();
        }
    }

    private void launchPlayerPitch() {
        actionPlayerMascot = playerMascot();
        activePitch = selectedPitch();
        pitchTargetY = aimY;
        ballX = BALL_START_X;
        ballY = BALL_RELEASE_Y;
        pitchTravel = 0;
        double baseSpeed = GameSettings.getDifficulty().randomPitchSpeed(random.nextDouble());
        pitchSpeed = baseSpeed
                * activePitch.speedMultiplier
                * GameSession.getPitchStyle().speed()
                * playerMascot().getPitchingMultiplier();
        matchState.recordPitch();
        pitchMetricsLabel.setText(String.format(
                "%s  â€¢  %.0f MPH  â€¢  PITCH #%d",
                activePitch.displayName, pitchSpeed * 0.19,
                matchState.pitchCount()));
        pitchAnimation = 0.54 / Math.max(0.8, GameSession.getPitchStyle().speed());
        beginPitchState(actionPlayerMascot, rivalMascot());
        pitchActive = true;
        statusLabel.setText(activePitch.displayName + " LAUNCHED! KEEP IT ON THE EDGE!");
    }

    private void launchRivalPitch() {
        if (pitchActive || phase != Phase.BATTING) {
            return;
        }
        actionRivalMascot = rivalMascot();
        activePitch = competitiveAIController.choosePitch(
                random, matchState.currentInning(), GameSettings.getDifficulty());
        pitchTargetY = competitiveAIController.chooseTarget(
                random, GameSettings.getDifficulty(),
                STRIKE_ZONE_TOP, STRIKE_ZONE_BOTTOM);
        ballX = BALL_START_X;
        ballY = BALL_RELEASE_Y;
        pitchTravel = 0;
        double baseSpeed = GameSettings.getDifficulty().randomPitchSpeed(random.nextDouble());
        double inningPressure = 1 + (matchState.currentInning() - 1) * 0.035;
        pitchSpeed = baseSpeed * activePitch.speedMultiplier * inningPressure;
        matchState.recordPitch();
        pitchMetricsLabel.setText(String.format(
                "%s  â€¢  %.0f MPH  â€¢  PITCH #%d",
                activePitch.displayName, pitchSpeed * 0.19,
                matchState.pitchCount()));
        pitchAnimation = 0.54;
        beginPitchState(actionRivalMascot, playerMascot());
        pitchActive = true;
        pitchTypeLabel.setText("RIVAL " + activePitch.displayName);
        statusLabel.setText("RIVAL PITCH! TRACK IT AND RIGHT-CLICK TO SWING!");
    }

    private void resolveRivalAtBat() {
        pitchActive = false;
        boolean inZone = isInsideStrikeZone(ballX);
        if (!inZone) {
            registerBallForRival();
            return;
        }

        double edgeDistance = Math.abs(ballX - zoneCenterY())
                / ((STRIKE_ZONE_BOTTOM - STRIKE_ZONE_TOP) / 2);
        double pitchSkill = playerMascot().getPitchingMultiplier() - 1;
        double contactChance = GameSettings.getDifficulty().getRivalContactChance()
                + (matchState.currentInning() - 1) * 0.025
                - edgeDistance * 0.16
                - pitchSkill * 0.38
                - activePitch.breakDifficulty;

        if (random.nextDouble() > clamp(contactChance, 0.08, 0.72)) {
            registerStrikeForRival();
            return;
        }

        actionRivalMascot = rivalMascot();
        swingAnimation = SWING_ANIMATION_DURATION;
        playStateController.transition(BaseballPlayStateController.State.BATTER_SWINGING);
        batSwingController.begin();

        beginFieldingChallenge();
    }

    private void beginFieldingChallenge() {
        fieldingActive = true;
        fieldingTimer = GameSettings.getDifficulty().getFieldingReactionSeconds();
        fieldingTargetX = 145 + random.nextDouble() * 830;
        fieldingTargetY = 82 + random.nextDouble() * 340;
        playStateController.transition(BaseballPlayStateController.State.BALL_IN_PLAY);
        cameraTransitionController.transitionTo(CameraTransitionController.View.HIGH_ISOMETRIC);
        liveBallController.launchBattedBall(PLATE_X, PLATE_Y, 0.52, HitResult.SINGLE);
        playStateController.transition(BaseballPlayStateController.State.FIELDING);
        statusLabel.setText("BALL IN PLAY! MOVE TO THE GLOWING CIRCLE AND LEFT-CLICK!");
        showFloating("FIELD IT!", Color.web("#ffe066"));
    }

    private void attemptFieldingCatch() {
        double distance = Math.hypot(pointerX - fieldingTargetX, pointerY - fieldingTargetY);
        double ballSpeed = Math.hypot(liveBallController.ball().velocityX(),
                liveBallController.ball().velocityY());
        double catchRadius = catchAnimationController.catchRadius(playerMascot(),
                GameSettings.getDifficulty(), liveBallController.ball().height(), ballSpeed);
        if (distance <= catchRadius) {
            fieldingActive = false;
            liveBallController.caught(playerMascot().name(),
                    new WorldPoint(fieldingTargetX, fieldingTargetY));
            catchPoseLife = 0.62;
            catchPoseX = fieldingTargetX;
            catchPoseY = fieldingTargetY;
            catchPoseMascot = playerMascot();
            crowdController.react(CrowdController.Event.DIVING_CATCH);
            registerRivalOut(catchAnimationController.pose(
                    GameSession.getPitchStyle(), playerMascot())
                    + "! YOUR DEFENSE MAKES THE PLAY!");
            addTurbo(16);
            burst(fieldingTargetX, fieldingTargetY,
                    Color.web(playerMascot().getAccentColor()), 28);
            resolvePlayState();
            return;
        }
        statusLabel.setText("NOT CLOSE ENOUGHâ€”CHASE THE GLOW AND CLICK AGAIN!");
    }

    private void resolveMissedFieldingPlay() {
        fieldingActive = false;
        liveBallController.deadBall();
        int runs = random.nextDouble() < 0.16 ? 2 : 1;
        matchState.addRivalRuns(runs);
        matchState.resetCombo();
        showFloating("RIVAL +" + runs, Color.web("#ff5b5b"));
        statusLabel.setText(runs == 2 ? "RIVAL DOUBLE!" : "RIVAL BASE HIT!");
        resetCount();
        advanceRivalLineup();
        resolvePlayState();
        updateHud();
    }

    private void resolveTakenPitch() {
        pitchActive = false;
        if (isInsideStrikeZone(ballX)) {
            registerStrikeForPlayer("CALLED STRIKE!");
        } else {
            registerBallForPlayer();
        }
        deadBallAndReady();
        scheduleNextRivalPitch();
    }

    @FXML
    private void handleSwing() {
        gameCanvas.requestFocus();
        if (paused || phase != Phase.BATTING) {
            statusLabel.setText(phase == Phase.PITCHING
                    ? "YOU ARE PITCHING - AIM AND LEFT-CLICK"
                    : "GAME PAUSED");
            return;
        }
        if (!pitchActive) {
            statusLabel.setText("WAIT FOR THE RIVAL PITCH");
            return;
        }

        actionPlayerMascot = playerMascot();
        swingAnimation = SWING_ANIMATION_DURATION;
        playStateController.transition(BaseballPlayStateController.State.BATTER_SWINGING);
        batSwingController.begin();

        double batX = PLATE_X + batOffsetX;
        double horizontalError = Math.abs(ballX - batX);
        double timingDelta = ballY - PLATE_Y;
        double verticalError = Math.abs(timingDelta);
        double contactWindow = BASE_CONTACT_WINDOW
                * playerMascot().getContactMultiplier()
                * GameSettings.getDifficulty().getContactWindowMultiplier();
        boolean turboHit = matchState.turbo() >= 100;
        BattingController.Contact contact = battingController.evaluate(
                horizontalError, verticalError, contactWindow,
                playerMascot().getPowerMultiplier(), turboHit);
        competitiveAIController.recordSwing(timingDelta, contact.region());

        pitchActive = false;
        if (!contact.madeContact()) {
            registerStrikeForPlayer("SWING AND MISS!");
            deadBallAndReady();
        } else if (contact.foul()) {
            registerFoulForPlayer();
            deadBallAndReady();
        } else {
            resolvePlayerContact(contact, turboHit);
        }
        if (phase == Phase.BATTING) {
            scheduleNextRivalPitch();
            drawGame();
        }
    }

    private void resolvePlayerContact(BattingController.Contact contact,
                                      boolean turboHit) {
        Mascot hitter = playerMascot();
        double quality = contact.quality();

        double catchChance = GameSettings.getDifficulty().getDefensiveCatchChance();
        catchChance -= quality * 0.22;
        catchChance -= (hitter.getContactMultiplier() - 1) * 0.18;
        if (turboHit) {
            catchChance -= 0.20;
            matchState.consumeTurbo();
        }

        if (random.nextDouble() < clamp(catchChance, 0.06, 0.86)) {
            matchState.resetCombo();
            registerPlayerOut("RIVAL CATCHER SNAGS IT! OUT!");
            liveBallController.caught(rivalMascot().name(),
                    new WorldPoint(PLATE_X + 80, ballY));
            resolvePlayState();
            showFloating("CAUGHT!", Color.web("#ff6b6b"));
            burst(PLATE_X + 80, ballY, Color.web("#ff6b6b"), 18);
            return;
        }

        double power = contact.power();
        HitResult result = contact.result();
        String message;
        if (result == HitResult.HOME_RUN) {
            message = turboHit
                    ? hitter.getSignatureMove() + "!"
                    : "MASCOT HOME RUN!";
            triggerImpact(15, 45);
        } else if (result == HitResult.TRIPLE) {
            message = "LASER TRIPLE!";
            triggerImpact(10, 34);
        } else if (result == HitResult.DOUBLE) {
            message = "ROCKET DOUBLE!";
            triggerImpact(8, 28);
        } else {
            message = "BASE HIT!";
            triggerImpact(4, 18);
        }

        matchState.incrementCombo();
        int comboBonus = matchState.combo() >= 6 ? 2 : matchState.combo() >= 3 ? 1 : 0;
        matchState.recordHit(result);
        crowdController.react(result == HitResult.HOME_RUN
                ? CrowdController.Event.HOME_RUN : CrowdController.Event.HIT);
        startHitMotion(power, result, hitter);
        advancePlayerLineup();
        addTurbo(18 + quality * 18);
        showFloating(message + (comboBonus > 0 ? "  TURBO COMBO x" + matchState.combo() : ""),
                Color.web("#ffe066"));
        statusLabel.setText(message);
        resetCount();
        updateHud();
    }

    @FXML
    private void handleAimUp() {
        if (!canAdjust()) {
            return;
        }
        aimY = Math.max(AIM_MIN_Y, aimY - 10);
        updateAimLabel();
        gameCanvas.requestFocus();
    }

    @FXML
    private void handleAimDown() {
        if (!canAdjust()) {
            return;
        }
        aimY = Math.min(AIM_MAX_Y, aimY + 10);
        updateAimLabel();
        gameCanvas.requestFocus();
    }

    @FXML
    private void handleLeft() {
        if (!canAdjust()) {
            return;
        }
        if (phase == Phase.PITCHING) {
            selectedPitchIndex = Math.floorMod(
                    selectedPitchIndex - 1, GameSession.getPitchLoadout().size());
        } else {
            batOffsetX = Math.max(BAT_TARGET_MIN_X - PLATE_X, batOffsetX - 9);
        }
        updateControlsForPhase();
        gameCanvas.requestFocus();
    }

    @FXML
    private void handleRight() {
        if (!canAdjust()) {
            return;
        }
        if (phase == Phase.PITCHING) {
            selectedPitchIndex = (selectedPitchIndex + 1) % GameSession.getPitchLoadout().size();
        } else {
            batOffsetX = Math.min(BAT_TARGET_MAX_X - PLATE_X, batOffsetX + 9);
        }
        updateControlsForPhase();
        gameCanvas.requestFocus();
    }

    private boolean canAdjust() {
        if (paused || phase == Phase.FINISHED) {
            return false;
        }
        if (fieldingActive) {
            statusLabel.setText("FIELD THE LIVE BALL WITH THE MOUSE AND LEFT-CLICK!");
            return false;
        }
        if (pitchActive && phase == Phase.PITCHING) {
            statusLabel.setText("WAIT FOR THE PITCH TO FINISH");
            return false;
        }
        return true;
    }

    private void registerStrikeForRival() {
        matchState.addStrike();
        matchState.incrementCombo();
        addTurbo(10);
        showFloating("STRIKE!", Color.web("#56f2e3"));
        statusLabel.setText("RIVAL WHIFFS! KEEP THE COMBO GOING!");
        if (matchState.strikes() >= 3) {
            registerRivalOut("STRIKEOUT! RIVAL BATTER OUT!");
        } else {
            updateHud();
        }
        deadBallAndReady();
    }

    private void registerBallForRival() {
        matchState.addBall();
        matchState.resetCombo();
        statusLabel.setText("BALL - AIM INSIDE THE GLOWING ZONE");
        if (matchState.balls() >= 4) {
            matchState.addRivalRuns(1);
            resetCount();
            showFloating("RIVAL WALK +1", Color.web("#ff8a66"));
            advanceRivalLineup();
        }
        updateHud();
        deadBallAndReady();
    }

    private void registerRivalOut(String message) {
        matchState.addOut();
        resetCount();
        advanceRivalLineup();
        showFloating("OUT " + matchState.outs() + "/3", Color.web("#56f2e3"));
        statusLabel.setText(message);
        updateHud();
        if (matchState.outs() >= 3) {
            beginBattingHalf();
        }
    }

    private void registerStrikeForPlayer(String message) {
        matchState.addStrike();
        matchState.resetCombo();
        statusLabel.setText(message);
        showFloating("STRIKE " + matchState.strikes(), Color.web("#ff8a66"));
        if (matchState.strikes() >= 3) {
            matchState.recordStrikeout();
            registerPlayerOut("STRIKE THREE! BATTER OUT!");
        } else {
            updateHud();
        }
    }

    private void registerFoulForPlayer() {
        if (matchState.strikes() < 2) {
            matchState.addStrike();
        }
        matchState.resetCombo();
        statusLabel.setText("FOUL BALL - PROTECT THE PLATE!");
        showFloating("FOUL", Color.web("#ffe066"));
        updateHud();
    }

    private void registerBallForPlayer() {
        matchState.addBall();
        statusLabel.setText("BALL - GOOD EYE!");
        addTurbo(5);
        if (matchState.balls() >= 4) {
            Mascot walker = playerMascot();
            matchState.recordWalk();
            matchState.incrementCombo();
            resetCount();
            runnerController.advance(HitResult.WALK, walker);
            advancePlayerLineup();
            showFloating("MASCOT WALK", Color.web("#ffe066"));
        }
        updateHud();
    }

    private void registerPlayerOut(String message) {
        matchState.addOut();
        matchState.resetCombo();
        resetCount();
        advancePlayerLineup();
        statusLabel.setText(message);
        showFloating("OUT " + matchState.outs() + "/3", Color.web("#ff8a66"));
        updateHud();
        if (matchState.outs() >= 3) {
            if (matchState.currentInning() >= TOTAL_INNINGS) {
                finishGame();
            } else {
                beginNextInning();
            }
        }
    }

    private void beginNextInning() {
        matchState.nextInning();
        phase = Phase.PITCHING;
        pitchActive = false;
        matchState.resetHalfInning();
        runnerController.clear();
        aimY = zoneCenterY();
        batOffsetX = 0;
        visualAimY = aimY;
        visualBatOffsetX = 0;
        automaticPitchDelay = 0;
        liveBallController.deadBall();
        resolvePlayState();
        statusLabel.setText("INNING " + matchState.currentInning()
                + "! YOUR LINEUP TAKES THE FIELD!");
        showFloating("INNING " + matchState.currentInning(), Color.web("#56f2e3"));
        updateControlsForPhase();
        updateHud();
    }

    private void beginBattingHalf() {
        phase = Phase.BATTING;
        matchState.resetHalfInning();
        runnerController.clear();
        aimY = zoneCenterY();
        batOffsetX = 0;
        visualAimY = aimY;
        visualBatOffsetX = 0;
        automaticPitchDelay = FIRST_RIVAL_PITCH_DELAY;
        liveBallController.deadBall();
        resolvePlayState();
        statusLabel.setText("BOTTOM HALF! MOVE THE MOUSE, THEN RIGHT-CLICK TO SWING!");
        showFloating("YOUR TURN TO BAT!", Color.web("#ffe066"));
        updateControlsForPhase();
        updateHud();
    }

    private void finishGame() {
        phase = Phase.FINISHED;
        pitchActive = false;
        liveBallController.deadBall();
        stopGameLoop();
        GameSession.finish(matchState, rivalMascot().getTeamName());
        SceneManager.showGameOver();
    }

    private void scheduleNextRivalPitch() {
        if (phase == Phase.BATTING) {
            automaticPitchDelay = NEXT_RIVAL_PITCH_DELAY;
        }
    }

    @FXML
    private void handlePause() {
        paused = !paused;
        pauseButton.setText(paused ? "RESUME" : "PAUSE");
        statusLabel.setText(paused ? "RACE PAUSED" : "PLAY BALL!");
        gameCanvas.requestFocus();
    }

    @FXML
    private void handleMainMenu() {
        stopGameLoop();
        SceneManager.showMainMenu();
    }

    private void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    private void resetCount() {
        matchState.resetCount();
    }

    private void addTurbo(double amount) {
        matchState.addTurbo(amount);
        updateHud();
    }

    private void triggerImpact(double shake, int particleCount) {
        if (GameSettings.isScreenShakeEnabled()) {
            shakeStrength = shake;
        }
        burst(visualAimY, PLATE_Y,
                Color.web(playerMascot().getAccentColor()), particleCount);
    }

    private void startHitMotion(double power, HitResult result, Mascot runner) {
        contactFlash = 0.22;
        playStateController.transition(BaseballPlayStateController.State.BALL_IN_PLAY);
        cameraTransitionController.transitionTo(CameraTransitionController.View.HIGH_ISOMETRIC);
        runnerController.advance(result, runner);
        liveBallController.launchBattedBall(
                PLATE_X + visualBatOffsetX, PLATE_Y,
                Math.max(0.15, Math.min(1, power)), result);
        playStateController.transition(BaseballPlayStateController.State.FIELDING);
        pitchMetricsLabel.setText(String.format(
                "%s  â€¢  EXIT %.0f MPH  â€¢  LAUNCH %.0fÂ°",
                result.name().replace('_', ' '),
                liveBallController.exitVelocity(),
                liveBallController.launchAngle()));
    }

    private void beginPitchState(Mascot pitcher, Mascot batter) {
        if (!playStateController.is(BaseballPlayStateController.State.READY_FOR_PITCH)) {
            resolvePlayState();
        }
        pitchSequenceController.configure(pitcher, batter, Mascot.CIRCUIT_BOT);
        playStateController.transition(BaseballPlayStateController.State.PITCH_WINDUP);
        liveBallController.holdByPitcher(pitcher.name(), BALL_START_X, BALL_RELEASE_Y);
        playStateController.transition(BaseballPlayStateController.State.PITCH_IN_FLIGHT);
        liveBallController.releasePitch(BALL_START_X, BALL_RELEASE_Y,
                pitchTargetY, PLATE_Y, pitchSpeed, activePitch.spin(),
                activePitch.curvePixels());
        cameraTransitionController.transitionTo(CameraTransitionController.View.BATTING);
    }

    private void deadBallAndReady() {
        liveBallController.deadBall();
        resolvePlayState();
    }

    private void resolvePlayState() {
        BaseballPlayStateController.State state = playStateController.state();
        if (state == BaseballPlayStateController.State.PITCH_IN_FLIGHT
                || state == BaseballPlayStateController.State.BATTER_SWINGING
                || state == BaseballPlayStateController.State.BALL_IN_PLAY
                || state == BaseballPlayStateController.State.FIELDING
                || state == BaseballPlayStateController.State.DEFENSIVE_THROW
                || state == BaseballPlayStateController.State.RUNNERS_ADVANCING) {
            playStateController.transition(BaseballPlayStateController.State.PLAY_RESOLVED);
        }
        playStateController.forceReadyAfterResolvedPlay();
        cameraTransitionController.transitionTo(CameraTransitionController.View.BATTING);
    }

    private void burst(double x, double y, Color color, int count) {
        for (int index = 0; index < count; index++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 55 + random.nextDouble() * 180;
            particles.add(new Particle(
                    x, y,
                    Math.cos(angle) * speed,
                    Math.sin(angle) * speed - 45,
                    color,
                    0.55 + random.nextDouble() * 0.55));
        }
    }

    private void showFloating(String text, Color color) {
        floatingText = text;
        floatingTextColor = color;
        floatingTextLife = 1.35;
    }

    private Mascot playerMascot() {
        return GameSession.getPlayerLineup().mascotAt(playerLineupIndex);
    }

    private Mascot rivalMascot() {
        return GameSession.getRivalLineup().mascotAt(rivalLineupIndex);
    }

    private Mascot displayedPlayerMascot() {
        boolean actionInProgress = (phase == Phase.BATTING && swingAnimation > 0)
                || (phase == Phase.PITCHING && pitchAnimation > 0);
        return actionInProgress && actionPlayerMascot != null
                ? actionPlayerMascot : playerMascot();
    }

    private Mascot displayedRivalMascot() {
        boolean actionInProgress = (phase == Phase.PITCHING && swingAnimation > 0)
                || (phase == Phase.BATTING && pitchAnimation > 0);
        return actionInProgress && actionRivalMascot != null
                ? actionRivalMascot : rivalMascot();
    }

    private void advancePlayerLineup() {
        playerLineupIndex = (playerLineupIndex + 1) % GameSession.getPlayerLineup().slots().size();
    }

    private void advanceRivalLineup() {
        rivalLineupIndex = (rivalLineupIndex + 1) % GameSession.getRivalLineup().slots().size();
    }

    private void updateHud() {
        Mascot hudMascot = displayedPlayerMascot();
        ScoreboardState score = scoreController.snapshot();
        TeamLineup lineup = phase == Phase.PITCHING
                ? GameSession.getRivalLineup() : GameSession.getPlayerLineup();
        int index = phase == Phase.PITCHING ? rivalLineupIndex : playerLineupIndex;
        mascotLabel.setText((phase == Phase.PITCHING ? "RIVAL BAT: " : "AT BAT: ")
                + hudMascot.getDisplayName() + "  â€¢  ON DECK: "
                + lineup.mascotAt(index + 1).getDisplayName() + "  â€¢  P: "
                + (phase == Phase.PITCHING ? GameSession.getPlayerLineup() : GameSession.getRivalLineup())
                        .pitcher().getDisplayName());
        inningLabel.setText("INNING " + score.inning() + "/" + TOTAL_INNINGS);
        playerRunsLabel.setText(Integer.toString(score.playerRuns()));
        rivalRunsLabel.setText(Integer.toString(score.rivalRuns()));
        ballsLabel.setText(Integer.toString(score.balls()));
        strikesLabel.setText(Integer.toString(score.strikes()));
        outsLabel.setText(Integer.toString(score.outs()));
        comboLabel.setText("x" + score.combo());
        turboMeter.setProgress(matchState.turbo() / 100.0);
        turboLabel.setText(matchState.turbo() >= 100
                ? "TURBO READY!" : (int) matchState.turbo() + "%");
        if (baseDiamondLabel != null) {
            baseDiamondLabel.setText(scoreController.baseDiamondText());
        }
    }

    private void updateControlsForPhase() {
        phaseLabel.setText(phase == Phase.PITCHING ? "TOP: YOU PITCH" : "BOTTOM: YOU BAT");
        if (phase == Phase.PITCHING) {
            pitchTypeLabel.setText(selectedPitch().displayName);
            primaryButton.setText("THROW [LEFT CLICK / P]");
            swingButton.setDisable(true);
        } else {
            pitchTypeLabel.setText("RIVAL AUTO-PITCH");
            primaryButton.setText("CALL PITCH [LEFT CLICK / P]");
            swingButton.setDisable(false);
        }
        updateAimLabel();
    }

    private void updateAimLabel() {
        String vertical;
        if (aimY < STRIKE_ZONE_TOP) {
            vertical = "HIGH";
        } else if (aimY > STRIKE_ZONE_BOTTOM) {
            vertical = "LOW";
        } else {
            vertical = "ZONE " + Math.round((aimY - STRIKE_ZONE_TOP) / 11.0 + 1);
        }
        if (phase == Phase.BATTING) {
            String timing = batOffsetX < -7 ? "EARLY" : batOffsetX > 7 ? "LATE" : "CENTER";
            aimLabel.setText(vertical + " / " + timing);
        } else {
            aimLabel.setText(vertical);
        }
    }

    private void drawGame() {
        GraphicsContext graphics = gameCanvas.getGraphicsContext2D();
        double width = gameCanvas.getWidth();
        double height = gameCanvas.getHeight();

        graphics.save();
        if (shakeStrength > 0) {
            graphics.translate(
                    (random.nextDouble() - 0.5) * shakeStrength,
                    (random.nextDouble() - 0.5) * shakeStrength);
        }
        graphics.setImageSmoothing(true);
        drawBackdrop(graphics, width, height);
        drawFieldGlow(graphics, width, height);
        drawBaseMarkers(graphics);
        drawAutomatedFielders(graphics);
        if (catchPoseLife > 0) {
            drawCatchPose(graphics);
        }
        if (fieldingActive) {
            drawFieldingChallenge(graphics);
        }

        if (phase == Phase.PITCHING) {
            drawMascotPlayer(graphics, 535, 185,
                    displayedPlayerMascot(), false, 0.46);
            drawRivalPlayer(graphics, 515, 456, true);
            drawRoleIndicator(graphics, "PITCHER", 560, 184, Color.web("#56f2e3"));
            drawRoleIndicator(graphics, "BATTER", 538, 454, Color.web("#ffe066"));
        } else {
            drawRivalPlayer(graphics, 535, 185, false);
            drawMascotPlayer(graphics,
                    515 + visualBatOffsetX * 0.28,
                    456,
                    displayedPlayerMascot(), true, 0.46);
            drawRoleIndicator(graphics, "PITCHER", 560, 184, Color.web("#ff8a66"));
            drawRoleIndicator(graphics, "BATTER",
                    538 + visualBatOffsetX * 0.28, 454, Color.web("#ffe066"));
        }
        drawCatcher(graphics, 625, 585);
        drawRoleIndicator(graphics, "CATCHER", 650, 584, Color.web("#d9fffa"));

        if (GameSettings.isStrikeZoneVisible()) {
            drawAimReticle(graphics);
        }

        if (pitchActive) {
            drawBallAndTrail(graphics);
        }
        drawSmoothRunners(graphics);
        if (!pitchActive && liveBallController.ball().active()) {
            drawControlledBall(graphics);
        }
        if (contactFlash > 0) {
            drawContactFlash(graphics);
        }
        drawParticles(graphics);
        drawFloatingText(graphics, width);
        graphics.restore();

        if (paused) {
            graphics.setFill(Color.rgb(5, 8, 24, 0.82));
            graphics.fillRect(0, 0, width, height);
            graphics.setFont(Font.font("Arial Rounded MT Bold", FontWeight.EXTRA_BOLD, 46));
            graphics.setFill(Color.WHITE);
            graphics.fillText("PAUSED", width / 2 - 95, height / 2);
        }
    }

    private void drawBackdrop(GraphicsContext graphics, double width, double height) {
        if (stadiumBackground != null && !stadiumBackground.isError()) {
            drawCoverImage(graphics, stadiumBackground, width, height);
            graphics.setFill(Color.rgb(8, 14, 42, 0.18));
            graphics.fillRect(0, 0, width, height);
            return;
        }

        graphics.setFill(Color.web("#12285b"));
        graphics.fillRect(0, 0, width, 170);
        graphics.setFill(Color.web("#10213b"));
        graphics.fillRect(0, 105, width, 130);
        Color[] crowd = {
                Color.web("#ffe066"), Color.web("#ff5b35"),
                Color.web("#56f2e3"), Color.web("#ff71ce"), Color.WHITE
        };
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 58; column++) {
                graphics.setFill(crowd[(row * 2 + column) % crowd.length]);
                graphics.fillRect(7 + column * 19, 110 + row * 13, 7, 7);
            }
        }
        graphics.setFill(Color.web("#2c9b52"));
        graphics.fillRect(0, 220, width, height - 220);
    }

    private void drawFieldGlow(GraphicsContext graphics, double width, double height) {
        graphics.save();
        graphics.setFill(Color.rgb(6, 10, 28, 0.26));
        graphics.fillRect(0, 0, width, 64);
        graphics.fillRect(0, height - 20, width, 20);
        graphics.restore();
    }

    private void drawAimReticle(GraphicsContext graphics) {
        double x = phase == Phase.BATTING ? PLATE_X + visualBatOffsetX : visualAimY;
        Color color = isInsideStrikeZone(visualAimY)
                ? Color.web("#56f2e3") : Color.web("#ff6b6b");
        double pulse = Math.sin(animationClock * 7) * 1.2;
        graphics.save();
        graphics.setGlobalAlpha(0.55);
        graphics.setStroke(color);
        graphics.setLineWidth(1.5);
        graphics.strokeOval(x - 6 - pulse / 2, PLATE_Y - 6 - pulse / 2,
                12 + pulse, 12 + pulse);
        graphics.restore();
    }

    private void drawRoleIndicator(GraphicsContext graphics, String role,
                                   double centerX, double feetY, Color color) {
        graphics.save();
        graphics.setFont(Font.font("Arial Rounded MT Bold", FontWeight.EXTRA_BOLD, 10));
        double width = role.length() * 7.2 + 16;
        graphics.setFill(Color.rgb(5, 10, 28, 0.76));
        graphics.fillRoundRect(centerX - width / 2, feetY - 15, width, 17, 9, 9);
        graphics.setStroke(color);
        graphics.setLineWidth(1);
        graphics.strokeRoundRect(centerX - width / 2, feetY - 15, width, 17, 9, 9);
        graphics.setFill(color);
        graphics.fillText(role, centerX - role.length() * 3.6, feetY - 3);
        graphics.restore();
    }

    private void drawSwingLane(GraphicsContext graphics) {
        if (phase != Phase.BATTING) {
            return;
        }
        graphics.save();
        graphics.setFill(Color.rgb(86, 242, 227, 0.08));
        graphics.fillRoundRect(
                BAT_TARGET_MIN_X,
                PLATE_Y - 48,
                BAT_TARGET_MAX_X - BAT_TARGET_MIN_X,
                96,
                18, 18);
        graphics.setStroke(Color.rgb(86, 242, 227, 0.42));
        graphics.setLineWidth(2);
        graphics.setLineDashes(8, 7);
        graphics.strokeRoundRect(
                BAT_TARGET_MIN_X,
                PLATE_Y - 48,
                BAT_TARGET_MAX_X - BAT_TARGET_MIN_X,
                96,
                18, 18);
        graphics.setLineDashes();
        graphics.setFill(Color.rgb(255, 255, 255, 0.75));
        graphics.setFont(Font.font("Arial Rounded MT Bold", FontWeight.BOLD, 11));
        graphics.fillText("SWING WINDOW", BAT_TARGET_MIN_X + 8, PLATE_Y - 55);
        graphics.restore();
    }

    private void drawBallAndTrail(GraphicsContext graphics) {
        double progress = pitchProgress(ballX);
        for (int step = 4; step >= 1; step--) {
            double trailProgress = Math.max(0, progress - step * 0.022);
            double trailX = trajectoryY(trailProgress, activePitch, pitchTargetY);
            double trailY = BALL_RELEASE_Y + (PLATE_Y - BALL_RELEASE_Y) * trailProgress;
            graphics.setFill(Color.rgb(255, 240, 170, 0.11 * (5 - step)));
            double size = 6 + (5 - step) * 2;
            graphics.fillOval(trailX - size / 2, trailY - size / 2, size, size);
        }
        graphics.setFill(Color.WHITE);
        graphics.fillOval(ballX - 8, ballY - 8, 16, 16);
        graphics.setStroke(Color.web("#ff5b35"));
        graphics.setLineWidth(2);
        graphics.strokeArc(ballX - 6, ballY - 6, 12, 12, 25, 100,
                javafx.scene.shape.ArcType.OPEN);
        graphics.strokeArc(ballX - 6, ballY - 6, 12, 12, 205, 100,
                javafx.scene.shape.ArcType.OPEN);
    }

    private void drawMascotPlayer(GraphicsContext graphics, double x, double y,
                                  Mascot mascot, boolean batting, double scale) {
        if (mascot == Mascot.TURBO_TANUKI && hasTurboTanukiSpriteSheet()) {
            MascotRenderController.Pose pose;
            if (batting) {
                pose = swingAnimation > 0
                        ? MascotRenderController.Pose.CONTACT
                        : MascotRenderController.Pose.BATTING_STANCE;
            } else {
                pose = pitchAnimation > .27
                        ? MascotRenderController.Pose.PITCH_WINDUP
                        : pitchAnimation > 0
                                ? MascotRenderController.Pose.PITCH_RELEASE
                                : MascotRenderController.Pose.READY;
            }
            drawTurboTanukiFrame(graphics, x, y, pose, scale * 1.18, batting);
        } else if (hasGameplaySpriteSheet() && mascot.ordinal() < SPRITE_ROWS) {
            drawSpriteMascotPlayer(graphics, x, y, mascot, batting, scale);
        } else {
            drawVectorMascotPlayer(graphics, x, y, mascot, batting, scale);
        }
    }

    private void drawSpriteMascotPlayer(GraphicsContext graphics, double x, double y,
                                        Mascot mascot, boolean batting, double scale) {
        double bob = 0;
        double displayWidth = SPRITE_BOX_WIDTH * scale;
        double displayHeight = SPRITE_BOX_HEIGHT * scale;
        SpriteAction action = batting
                ? (swingAnimation > 0 ? SpriteAction.SWING : SpriteAction.IDLE)
                : (pitchAnimation > 0 ? SpriteAction.PITCH : SpriteAction.IDLE);

        graphics.save();
        graphics.setFill(Color.rgb(0, 0, 0, 0.34));
        graphics.fillOval(
                x + displayWidth * 0.12,
                y + bob + displayHeight * 0.82,
                displayWidth * 0.76,
                displayHeight * 0.08);
        graphics.restore();

        boolean facesRight = batting;
        graphics.save();
        graphics.translate(x + (facesRight ? 0 : displayWidth), y + bob);
        graphics.scale(facesRight ? scale : -scale, scale);

        double sourceX = action.column * SPRITE_CELL_WIDTH;
        double sourceY = mascot.ordinal() * SPRITE_CELL_HEIGHT;
        graphics.drawImage(
                gameplaySprites,
                sourceX, sourceY, SPRITE_CELL_WIDTH, SPRITE_CELL_HEIGHT,
                0, 0, SPRITE_BOX_WIDTH, SPRITE_BOX_HEIGHT);

        if (batting) {
            drawSpriteBat(graphics, mascot);
        } else {
            drawSpritePitchMotion(graphics, mascot);
        }
        graphics.restore();
    }

    private void drawSpriteBat(GraphicsContext graphics, Mascot mascot) {
        double swingStrength = swingAnimation > 0
                ? clamp(swingAnimation / SWING_ANIMATION_DURATION, 0, 1)
                : 0;
        double eased = Math.pow(swingStrength, 0.34);
        if (swingAnimation > 0) {
            graphics.save();
            graphics.setGlobalAlpha(0.72 * swingStrength);
            graphics.setStroke(Color.WHITE);
            graphics.setLineWidth(4);
            graphics.strokeArc(18, 21, 112, 112,
                    198 - eased * 72, 52, javafx.scene.shape.ArcType.OPEN);
            graphics.setGlobalAlpha(0.34 * swingStrength);
            graphics.strokeArc(10, 13, 128, 128,
                    203 - eased * 72, 44, javafx.scene.shape.ArcType.OPEN);
            graphics.restore();
        }
    }

    private void drawSpritePitchMotion(GraphicsContext graphics, Mascot mascot) {
        if (pitchAnimation <= 0) {
            return;
        }

        if (pitchAnimation > 0) {
            graphics.save();
            graphics.setGlobalAlpha(0.48);
            graphics.setStroke(Color.WHITE);
            graphics.setLineWidth(3);
            graphics.strokeLine(73, 53, 96, 43);
            graphics.strokeLine(77, 66, 103, 62);
            graphics.strokeLine(76, 79, 100, 82);
            graphics.restore();
        }
    }

    private void drawTurboTanukiFrame(GraphicsContext graphics, double x, double y,
                                      MascotRenderController.Pose pose,
                                      double scale, boolean facesRight) {
        MascotRenderController.Frame frame = mascotRenderController.frame(pose);
        double sourceWidth = turboTanukiSprites.getWidth() / 4.0;
        double sourceHeight = turboTanukiSprites.getHeight() / 4.0;
        double width = 178 * scale;
        double height = 178 * scale;
        graphics.save();
        graphics.setFill(Color.rgb(0, 0, 0, 0.30));
        graphics.fillOval(x + width * .10, y + height * .85,
                width * .72, height * .085);
        graphics.translate(x + (facesRight ? 0 : width), y);
        graphics.scale(facesRight ? 1 : -1, 1);
        graphics.drawImage(turboTanukiSprites,
                frame.column() * sourceWidth, frame.row() * sourceHeight,
                sourceWidth, sourceHeight, 0, 0, width, height);
        graphics.restore();
    }

    private void drawVectorMascotPlayer(GraphicsContext graphics, double x, double y,
                                        Mascot mascot, boolean batting, double scale) {
        graphics.save();
        double bob = 0;
        graphics.translate(x, y + bob);
        graphics.scale(scale, scale);

        Color primary = Color.web(mascot.getPrimaryColor());
        Color accent = Color.web(mascot.getAccentColor());
        graphics.setFill(Color.rgb(0, 0, 0, 0.30));
        graphics.fillOval(-18, 82, 82, 18);

        graphics.setFill(primary);
        graphics.fillRoundRect(0, 25, 48, 55, 18, 18);
        graphics.setFill(accent);
        graphics.fillRect(0, 51, 48, 9);
        double footMotion = Math.sin(animationClock * 9.5) * (batting ? 2.5 : 1.5);
        graphics.setFill(Color.web("#183153"));
        graphics.fillRoundRect(4 - footMotion, 75, 15, 27, 8, 8);
        graphics.fillRoundRect(30 + footMotion, 75, 15, 27, 8, 8);

        drawMascotHead(graphics, mascot, 24, 14, primary, accent);

        if (batting) {
            double swingStrength = swingAnimation > 0
                    ? clamp(swingAnimation / SWING_ANIMATION_DURATION, 0, 1)
                    : 0;
            double eased = Math.pow(swingStrength, 0.34);
            double angle = Math.toRadians(-130 + eased * 70);
            double handX = 37;
            double handY = 40;
            double batEndX = handX + Math.cos(angle) * 78;
            double batEndY = handY + Math.sin(angle) * 78;

            if (swingAnimation > 0) {
                graphics.save();
                graphics.setGlobalAlpha(0.58);
                graphics.setStroke(Color.web("#ffffff"));
                graphics.setLineWidth(4);
                graphics.strokeArc(-38, -39, 128, 128,
                        197 - eased * 185, 55, javafx.scene.shape.ArcType.OPEN);
                graphics.setGlobalAlpha(0.34);
                graphics.strokeArc(-47, -48, 146, 146,
                        203 - eased * 185, 42, javafx.scene.shape.ArcType.OPEN);
                graphics.restore();
            }
            graphics.setStroke(Color.web("#ffe066"));
            graphics.setLineWidth(8);
            graphics.strokeLine(handX, handY, batEndX, batEndY);
            graphics.setFill(accent);
            graphics.fillOval(31, 33, 18, 18);
        } else {
            double progress = pitchAnimation > 0
                    ? 1 - pitchAnimation / 0.54
                    : 0;
            double eased = Math.sin(clamp(progress, 0, 1) * Math.PI / 2);
            double angle = Math.toRadians(-68 + eased * 145);
            double armEndX = 39 + Math.cos(angle) * 47;
            double armEndY = 42 + Math.sin(angle) * 47;

            if (pitchAnimation > 0) {
                graphics.save();
                graphics.setGlobalAlpha(0.45);
                graphics.setStroke(Color.WHITE);
                graphics.setLineWidth(3);
                graphics.strokeLine(63, 2, 83, -8);
                graphics.strokeLine(67, 15, 91, 10);
                graphics.strokeLine(64, 28, 86, 30);
                graphics.restore();
            }
            graphics.setFill(Color.web("#8a3d2f"));
            graphics.fillOval(-13, 39, 24, 22);
            graphics.setStroke(accent);
            graphics.setLineWidth(7);
            graphics.strokeLine(39, 42, armEndX, armEndY);
        }
        graphics.restore();
    }

    private void drawControlledBall(GraphicsContext graphics) {
        WorldPoint shadow = liveBallController.shadowPosition();
        WorldPoint position = liveBallController.screenPosition();
        double height = liveBallController.ball().height();
        double scale = liveBallController.perspectiveScale();
        double size = 13 * scale;

        graphics.save();
        double shadowWidth = Math.max(5, 18 * scale - height * 0.025);
        graphics.setFill(Color.rgb(0, 0, 0, Math.max(0.08, 0.34 - height * 0.0007)));
        graphics.fillOval(shadow.x() - shadowWidth / 2, shadow.y() - 3,
                shadowWidth, 6);

        double velocity = Math.hypot(
                liveBallController.ball().velocityX(),
                liveBallController.ball().velocityY());
        if (velocity > 45) {
            double unitX = liveBallController.ball().velocityX() / velocity;
            double unitY = (liveBallController.ball().velocityY()
                    - liveBallController.ball().velocityHeight()) / velocity;
            graphics.setStroke(Color.rgb(220, 248, 255, 0.50));
            graphics.setLineWidth(Math.max(2, size * 0.30));
            graphics.strokeLine(position.x() - unitX * 26,
                    position.y() - unitY * 20,
                    position.x() - unitX * 6,
                    position.y() - unitY * 5);
        }

        graphics.setFill(Color.rgb(70, 190, 255, 0.18));
        graphics.fillOval(position.x() - size, position.y() - size,
                size * 2, size * 2);
        graphics.setFill(Color.WHITE);
        graphics.fillOval(position.x() - size / 2, position.y() - size / 2,
                size, size);
        graphics.setStroke(Color.web("#e84b4b"));
        graphics.setLineWidth(Math.max(1, scale * 1.5));
        graphics.strokeArc(position.x() - size * 0.38,
                position.y() - size * 0.38,
                size * 0.76, size * 0.76,
                25, 100, javafx.scene.shape.ArcType.OPEN);
        graphics.restore();
    }

    private void drawSmoothRunners(GraphicsContext graphics) {
        for (Runner runner : runnerController.runnersBackToFront()) {
            WorldPoint feet = runner.feet();
            double scale = runnerController.path().perspectiveScale(feet);
            double width = 50 * scale;
            double height = 62 * scale;
            double bob = 0;

            graphics.save();
            graphics.setFill(Color.rgb(0, 0, 0, 0.24));
            graphics.fillOval(feet.x() - width * 0.32, feet.y() - height * 0.035,
                    width * 0.64, height * 0.09);
            drawActionSprite(graphics, runner.mascot(),
                    runner.isRunning() ? SpriteAction.RUN : SpriteAction.IDLE,
                    feet.x() - width / 2, feet.y() - height - bob,
                    width, height, true, 0);
            graphics.restore();
        }
    }

    private void drawBaseMarkers(GraphicsContext graphics) {
        double[][] bases = {{560, 548}, {872, 280}, {560, 191}, {248, 280}};
        graphics.save();
        for (int index = 0; index < bases.length; index++) {
            boolean occupied = index > 0 && runnerController.isOccupied(index);
            if (!occupied) {
                continue;
            }
            double pulse = 0.50 + Math.sin(animationClock * 5 + index) * 0.12;
            graphics.setFill(Color.rgb(86, 242, 227, pulse * 0.30));
            graphics.fillOval(bases[index][0] - 11, bases[index][1] - 7, 22, 14);
            graphics.setStroke(Color.rgb(220, 255, 251, 0.70));
            graphics.setLineWidth(1.2);
            graphics.strokeOval(bases[index][0] - 8, bases[index][1] - 5, 16, 10);
        }
        graphics.restore();
    }

    private void drawAutomatedFielders(GraphicsContext graphics) {
        Mascot[] lineup = Mascot.values();
        WorldPoint ballShadow = liveBallController.shadowPosition();
        double targetX = clamp(ballShadow.x(), 120, 1000);
        double[][] anchors = {
                {300, 335}, {445, 245}, {675, 245}, {820, 335},
                {270, 145}, {560, 92}, {850, 145}
        };

        for (int index = 0; index < fielderFeet.length; index++) {
            double x = fielderFeet[index].x();
            double y = fielderFeet[index].y();
            int defensiveCaptain = phase == Phase.PITCHING
                    ? playerMascot().ordinal() : rivalMascot().ordinal();
            Mascot fielder = lineup[(defensiveCaptain + index + 1) % lineup.length];
            boolean facesRight = targetX >= x;
            double scale = runnerController.path().perspectiveScale(fielderFeet[index]);
            double width = 50 * scale;
            double height = 62 * scale;
            boolean moving = fielderFeet[index].distanceTo(
                    new WorldPoint(anchors[index][0], anchors[index][1])) > 2;
            graphics.setFill(Color.rgb(0, 0, 0, 0.22));
            graphics.fillOval(x - width * 0.34, y - height * 0.03,
                    width * 0.68, Math.max(4, height * 0.08));
            drawActionSprite(graphics, fielder,
                    moving ? SpriteAction.RUN : SpriteAction.IDLE,
                    x - width / 2, y - height, width, height, facesRight, 0);
        }
    }

    private void updateFielderFeet(double elapsed) {
        double[][] anchors = {
                {300, 335}, {445, 245}, {675, 245}, {820, 335},
                {270, 145}, {560, 92}, {850, 145}
        };
        boolean chasing = liveBallController.ball().active() && !pitchActive;
        WorldPoint ball = liveBallController.shadowPosition();
        int nearest = -1;
        int backup = -1;
        if (chasing) {
            DefensiveAssignmentController.Assignment assignment =
                    defensiveAssignmentController.assign(List.of(fielderFeet),
                            liveBallController.ball());
            nearest = assignment.primaryIndex();
            backup = assignment.backupIndex();
        }
        for (int index = 0; index < fielderFeet.length; index++) {
            WorldPoint home = new WorldPoint(anchors[index][0], anchors[index][1]);
            WorldPoint target = home;
            if (chasing && index == nearest) {
                target = new WorldPoint(clamp(ball.x(), 115, 1005),
                        clamp(ball.y(), 75, 455));
            } else if (chasing && index == backup) {
                target = new WorldPoint(clamp(ball.x() + (ball.x() < 560 ? 55 : -55),
                        115, 1005), clamp(ball.y() + 28, 75, 455));
            }
            Mascot fielder = Mascot.values()[(playerMascot().ordinal() + index + 1)
                    % Mascot.values().length];
            fielderFeet[index] = groundMovementController.approach(
                    fielderFeet[index], target,
                    175 * fielder.getSpeedMultiplier(), elapsed);
        }
    }

    private void drawMascotCrowdPulse(GraphicsContext graphics, double width) {
        double energy = crowdController.energy();
        graphics.save();
        for (int index = 0; index < 34; index++) {
            Mascot fan = Mascot.values()[index % Mascot.values().length];
            double x = 26 + index * (width - 52) / 33.0;
            double y = 112 + (index % 3) * 12
                    + Math.sin(animationClock * (3 + energy * 5) + index) * 5 * energy;
            graphics.setFill(Color.web(fan.getPrimaryColor(), 0.40 + energy * 0.28));
            graphics.fillOval(x - 4, y - 4, 8, 8);
            graphics.fillPolygon(new double[] {x - 4, x - 1, x + 1, x + 4},
                    new double[] {y - 2, y - 8, y - 8, y - 2}, 4);
        }
        graphics.restore();
    }

    private void drawCatchPose(GraphicsContext graphics) {
        if (catchPoseMascot.ordinal() >= 4
                || fieldingSprites == null || fieldingSprites.isError()) {
            double scale = runnerController.path().perspectiveScale(
                    new WorldPoint(catchPoseX, catchPoseY));
            graphics.save();
            graphics.setGlobalAlpha(clamp(catchPoseLife * 2.4, 0, 1));
            if (hasGameplaySpriteSheet()) {
                double width = 118 * scale;
                double height = 142 * scale;
                drawActionSprite(graphics, catchPoseMascot, SpriteAction.PITCH,
                        catchPoseX - width / 2, catchPoseY - height,
                        width, height, true, 0);
            } else {
                drawVectorMascotPlayer(graphics, catchPoseX - 25 * scale,
                        catchPoseY - 102 * scale, catchPoseMascot, false, scale);
            }
            graphics.restore();
            return;
        }
        MascotAnimationController.AtlasFrame frame =
                mascotAnimationController.fieldingFrame(
                        catchPoseMascot, MascotAnimationState.CATCHING);
        double scale = runnerController.path().perspectiveScale(
                new WorldPoint(catchPoseX, catchPoseY));
        double size = 142 * scale;
        graphics.save();
        graphics.setGlobalAlpha(clamp(catchPoseLife * 2.4, 0, 1));
        graphics.setFill(Color.rgb(86, 242, 227, 0.22));
        graphics.fillOval(catchPoseX - size * 0.48, catchPoseY - size * 0.14,
                size * 0.96, size * 0.22);
        graphics.drawImage(fieldingSprites,
                frame.x(), frame.y(), frame.width(), frame.height(),
                catchPoseX - size / 2, catchPoseY - size,
                size, size);
        graphics.restore();
    }

    private void drawFieldingChallenge(GraphicsContext graphics) {
        double pulse = 1 + Math.sin(animationClock * 10) * 0.12;
        double radius = 18 * pulse;
        graphics.save();
        graphics.setGlobalAlpha(0.68);
        graphics.setStroke(Color.web("#d9fffa"));
        graphics.setLineDashes(5, 4);
        graphics.setLineWidth(2);
        graphics.strokeOval(fieldingTargetX - radius, fieldingTargetY - radius,
                radius * 2, radius * 2);
        graphics.setLineDashes();
        graphics.setStroke(Color.WHITE);
        graphics.setLineWidth(1.2);
        graphics.strokeLine(pointerX - 6, pointerY, pointerX + 6, pointerY);
        graphics.strokeLine(pointerX, pointerY - 6, pointerX, pointerY + 6);
        graphics.setFont(Font.font("Arial Rounded MT Bold", FontWeight.BOLD, 12));
        graphics.setFill(Color.WHITE);
        graphics.fillText(String.format("CATCH %.1f", fieldingTimer),
                fieldingTargetX - 31, fieldingTargetY - radius - 7);
        graphics.restore();
    }

    private void drawActionSprite(GraphicsContext graphics, Mascot mascot,
                                  SpriteAction action, double x, double y,
                                  double width, double height,
                                  boolean facesRight, double rotation) {
        if (mascot == Mascot.TURBO_TANUKI && hasTurboTanukiSpriteSheet()) {
            MascotRenderController.Pose pose = switch (action) {
                case RUN -> MascotRenderController.Pose.RUNNING;
                case SWING -> MascotRenderController.Pose.CONTACT;
                case PITCH -> MascotRenderController.Pose.PITCH_RELEASE;
                default -> MascotRenderController.Pose.READY;
            };
            MascotRenderController.Frame frame = mascotRenderController.frame(pose);
            double sourceWidth = turboTanukiSprites.getWidth() / 4.0;
            double sourceHeight = turboTanukiSprites.getHeight() / 4.0;
            graphics.save();
            graphics.translate(x + (facesRight ? 0 : width), y);
            graphics.rotate(rotation);
            graphics.scale(facesRight ? 1 : -1, 1);
            graphics.drawImage(turboTanukiSprites,
                    frame.column() * sourceWidth, frame.row() * sourceHeight,
                    sourceWidth, sourceHeight, 0, 0, width, height);
            graphics.restore();
            return;
        }
        if (mascot.ordinal() >= SPRITE_ROWS || !hasGameplaySpriteSheet()) {
            double scale = Math.max(0.35, height / 105.0);
            drawVectorMascotPlayer(graphics, x + width * 0.18, y, mascot,
                    action == SpriteAction.SWING, scale);
            return;
        }
        graphics.save();
        graphics.translate(x + (facesRight ? 0 : width), y);
        graphics.rotate(rotation);
        graphics.scale(facesRight ? 1 : -1, 1);
        graphics.drawImage(
                gameplaySprites,
                action.column * SPRITE_CELL_WIDTH,
                mascot.ordinal() * SPRITE_CELL_HEIGHT,
                SPRITE_CELL_WIDTH, SPRITE_CELL_HEIGHT,
                0, 0, width, height);
        graphics.restore();
    }

    private boolean hasGameplaySpriteSheet() {
        return gameplaySprites != null
                && !gameplaySprites.isError()
                && gameplaySprites.getWidth() >= SPRITE_CELL_WIDTH * SPRITE_COLUMNS
                && gameplaySprites.getHeight() >= SPRITE_CELL_HEIGHT * SPRITE_ROWS;
    }

    private boolean hasTurboTanukiSpriteSheet() {
        return turboTanukiSprites != null && !turboTanukiSprites.isError()
                && turboTanukiSprites.getWidth() >= 1000
                && turboTanukiSprites.getHeight() >= 1000;
    }

    private void drawContactFlash(GraphicsContext graphics) {
        double centerX = PLATE_X + visualBatOffsetX;
        double centerY = PLATE_Y;
        double strength = clamp(contactFlash / 0.22, 0, 1);
        graphics.save();
        graphics.setGlobalAlpha(strength);
        graphics.setStroke(Color.WHITE);
        graphics.setLineWidth(5);
        for (int ray = 0; ray < 12; ray++) {
            double angle = ray * Math.PI / 6;
            graphics.strokeLine(
                    centerX + Math.cos(angle) * 18,
                    centerY + Math.sin(angle) * 18,
                    centerX + Math.cos(angle) * (42 + 18 * strength),
                    centerY + Math.sin(angle) * (42 + 18 * strength));
        }
        graphics.setFill(Color.web("#ffe066"));
        graphics.fillOval(centerX - 14, centerY - 14, 28, 28);
        graphics.restore();
    }

    private void drawMascotHead(GraphicsContext graphics, Mascot mascot,
                                double centerX, double centerY,
                                Color primary, Color accent) {
        graphics.setFill(accent);
        switch (mascot) {
            case TURBO_TANUKI -> {
                graphics.fillOval(centerX - 27, centerY - 23, 18, 18);
                graphics.fillOval(centerX + 9, centerY - 23, 18, 18);
            }
            case BUBBLE_BUNNY -> {
                graphics.fillRoundRect(centerX - 20, centerY - 47, 13, 38, 10, 10);
                graphics.fillRoundRect(centerX + 7, centerY - 47, 13, 38, 10, 10);
            }
            case ROCKET_REX -> {
                graphics.fillPolygon(
                        new double[] {centerX - 22, centerX - 10, centerX, centerX + 10, centerX + 22},
                        new double[] {centerY - 9, centerY - 34, centerY - 13, centerY - 35, centerY - 7},
                        5);
            }
            case NOVA_NEKO -> {
                graphics.fillPolygon(
                        new double[] {centerX - 24, centerX - 17, centerX - 2},
                        new double[] {centerY - 7, centerY - 36, centerY - 19}, 3);
                graphics.fillPolygon(
                        new double[] {centerX + 2, centerX + 17, centerX + 24},
                        new double[] {centerY - 19, centerY - 36, centerY - 7}, 3);
            }
            case BLAZE_FALCON -> {
                graphics.fillPolygon(
                        new double[] {centerX - 8, centerX, centerX + 9},
                        new double[] {centerY - 19, centerY - 43, centerY - 18}, 3);
            }
            case FROST_WOLF -> {
                graphics.fillPolygon(
                        new double[] {centerX - 25, centerX - 18, centerX - 4},
                        new double[] {centerY - 7, centerY - 39, centerY - 17}, 3);
                graphics.fillPolygon(
                        new double[] {centerX + 4, centerX + 18, centerX + 25},
                        new double[] {centerY - 17, centerY - 39, centerY - 7}, 3);
            }
            case REEF_SHARK -> graphics.fillPolygon(
                    new double[] {centerX - 6, centerX + 2, centerX + 11},
                    new double[] {centerY - 17, centerY - 44, centerY - 17}, 3);
            case MAPLE_RED_PANDA -> {
                graphics.fillOval(centerX - 28, centerY - 27, 19, 19);
                graphics.fillOval(centerX + 9, centerY - 27, 19, 19);
            }
            case HARBOR_TURTLE -> {
                graphics.fillOval(centerX - 31, centerY - 13, 15, 20);
                graphics.fillOval(centerX + 16, centerY - 13, 15, 20);
            }
            case EMBER_DRAGON -> {
                graphics.fillPolygon(
                        new double[] {centerX - 21, centerX - 13, centerX - 5},
                        new double[] {centerY - 17, centerY - 45, centerY - 19}, 3);
                graphics.fillPolygon(
                        new double[] {centerX + 5, centerX + 13, centerX + 21},
                        new double[] {centerY - 19, centerY - 45, centerY - 17}, 3);
            }
            case VOLT_TIGER -> {
                graphics.fillRoundRect(centerX - 25, centerY - 34, 17, 24, 7, 7);
                graphics.fillRoundRect(centerX + 8, centerY - 34, 17, 24, 7, 7);
            }
            case CIRCUIT_BOT -> {
                graphics.fillRoundRect(centerX - 4, centerY - 44, 8, 20, 4, 4);
                graphics.fillOval(centerX - 7, centerY - 49, 14, 14);
            }
        }

        graphics.setFill(primary.brighter());
        graphics.fillRoundRect(centerX - 27, centerY - 20, 54, 43, 22, 22);
        graphics.setFill(Color.WHITE);
        graphics.fillOval(centerX - 17, centerY - 7, 13, 16);
        graphics.fillOval(centerX + 4, centerY - 7, 13, 16);
        graphics.setFill(Color.web("#17223b"));
        graphics.fillOval(centerX - 12, centerY - 2, 6, 9);
        graphics.fillOval(centerX + 7, centerY - 2, 6, 9);
        graphics.setFill(accent);
        graphics.fillRoundRect(centerX - 31, centerY - 25, 62, 12, 10, 10);
        graphics.fillRoundRect(centerX - 7, centerY - 31, 35, 9, 8, 8);
    }

    private void drawRivalPlayer(GraphicsContext graphics, double x, double y,
                                 boolean batting) {
        drawMascotPlayer(graphics, x, y, displayedRivalMascot(), batting, 0.46);
    }

    private void drawCatcher(GraphicsContext graphics, double x, double y) {
        graphics.setFill(Color.rgb(0, 0, 0, 0.30));
        graphics.fillOval(x - 16, y + 20, 38, 9);
        graphics.setFill(Color.web("#273c75"));
        graphics.fillRoundRect(x - 9, y - 1, 25, 26, 9, 9);
        graphics.setStroke(Color.web("#ffe066"));
        graphics.setLineWidth(2);
        graphics.strokeRoundRect(x - 5, y - 19, 18, 18, 6, 6);
        graphics.setFill(Color.web("#8a3d2f"));
        graphics.fillOval(x + 14, y + 2, 14, 13);
    }

    private void drawParticles(GraphicsContext graphics) {
        for (Particle particle : particles) {
            graphics.setFill(Color.color(
                    particle.color.getRed(),
                    particle.color.getGreen(),
                    particle.color.getBlue(),
                    clamp(particle.life, 0, 1)));
            graphics.fillRect(particle.x, particle.y, 7, 7);
        }
    }

    private void drawFloatingText(GraphicsContext graphics, double width) {
        if (floatingTextLife <= 0 || floatingText.isBlank()) {
            return;
        }
        double rise = (1.35 - floatingTextLife) * 28;
        graphics.setFont(Font.font("Arial Rounded MT Bold", FontWeight.EXTRA_BOLD, 31));
        graphics.setFill(Color.rgb(6, 10, 28, 0.78));
        graphics.fillRoundRect(width / 2 - 245, 82 - rise, 490, 53, 18, 18);
        graphics.setFill(floatingTextColor);
        graphics.fillText(floatingText, width / 2 - 220, 119 - rise);
    }

    private void drawCoverImage(GraphicsContext graphics, Image image,
                                double width, double height) {
        double imageRatio = image.getWidth() / image.getHeight();
        double canvasRatio = width / height;
        double sourceX = 0;
        double sourceY = 0;
        double sourceWidth = image.getWidth();
        double sourceHeight = image.getHeight();
        if (imageRatio > canvasRatio) {
            sourceWidth = image.getHeight() * canvasRatio;
            sourceX = (image.getWidth() - sourceWidth) / 2;
        } else {
            sourceHeight = image.getWidth() / canvasRatio;
            sourceY = (image.getHeight() - sourceHeight) / 2;
        }
        graphics.drawImage(image, sourceX, sourceY, sourceWidth, sourceHeight,
                0, 0, width, height);
    }

    private double pitchProgress(double currentX) {
        return clamp(pitchTravel, 0, 1);
    }

    private double trajectoryY(double progress, PitchType pitchType, double targetY) {
        double straightLine = BALL_START_X + (targetY - BALL_START_X) * progress;
        double breakMultiplier = phase == Phase.BATTING
                ? GameSettings.getDifficulty().getBreakMultiplier()
                : GameSession.getPitchStyle().breakAmount();
        return straightLine + Math.sin(progress * Math.PI) * pitchType.curvePixels
                * breakMultiplier;
    }

    private boolean isInsideStrikeZone(double y) {
        return y >= STRIKE_ZONE_TOP && y <= STRIKE_ZONE_BOTTOM;
    }

    private static double zoneCenterY() {
        return (STRIKE_ZONE_TOP + STRIKE_ZONE_BOTTOM) / 2;
    }

    private PitchType selectedPitch() {
        return GameSession.getPitchLoadout().get(
                Math.floorMod(selectedPitchIndex, GameSession.getPitchLoadout().size()));
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static Image loadImage(String resource) {
        try (InputStream stream = GameController.class.getResourceAsStream(resource)) {
            return stream == null ? null : new Image(stream);
        } catch (Exception ignored) {
            return null;
        }
    }

    private enum Phase {
        PITCHING,
        BATTING,
        FINISHED
    }

    private enum SpriteAction {
        IDLE(0),
        PITCH(1),
        SWING(2),
        RUN(3);

        private final int column;

        SpriteAction(int column) {
            this.column = column;
        }
    }

    private static final class Particle {
        private double x;
        private double y;
        private double velocityX;
        private double velocityY;
        private final Color color;
        private double life;

        private Particle(double x, double y, double velocityX, double velocityY,
                         Color color, double life) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.color = color;
            this.life = life;
        }
    }
}
