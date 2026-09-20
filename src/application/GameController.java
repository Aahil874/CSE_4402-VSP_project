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
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class GameController {
    private static final double SPRITE_BOX_WIDTH = 160.0;
    private static final double SPRITE_BOX_HEIGHT = 192.0;
    private static final double BALL_DIAMETER = 16.0;
    private static final double ON_FIELD_MASCOT_SCALE = 0.78;
    private static final double FIELD_MIN_X = 70.0;
    private static final double FIELD_MAX_X = 1050.0;
    private static final double ON_FIELD_MASCOT_WIDTH = SPRITE_BOX_WIDTH * ON_FIELD_MASCOT_SCALE;
    private static final double ON_FIELD_MASCOT_HEIGHT = SPRITE_BOX_HEIGHT * ON_FIELD_MASCOT_SCALE;
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
    private final List<Particle> particles = new ArrayList<Particle>();
    private final MatchState matchState = new MatchState();
    private final ScoreController scoreController = new ScoreController(this.matchState);
    private final RunnerController runnerController = new RunnerController();
    private final LiveBallController liveBallController = new LiveBallController();
    private final BattingController battingController = new BattingController();
    private final BatSwingController batSwingController = new BatSwingController();
    private final BaseballPlayStateController playStateController = new BaseballPlayStateController();
    private final PitchSequenceController pitchSequenceController = new PitchSequenceController();
    private final CameraTransitionController cameraTransitionController = new CameraTransitionController();
    private final MascotRenderController mascotRenderController = new MascotRenderController();
    private final CompetitiveAIController competitiveAIController = new CompetitiveAIController();
    private final CatchAnimationController catchAnimationController = new CatchAnimationController();
    private final GroundMovementController groundMovementController = new GroundMovementController();
    private final DefensiveAssignmentController defensiveAssignmentController = new DefensiveAssignmentController();
    private final CrowdController crowdController = new CrowdController();
    private final MascotAnimationController mascotAnimationController = new MascotAnimationController();
    private final ParkCamera parkCamera = new ParkCamera();
    private final ParkSceneRenderer parkRenderer = new ParkSceneRenderer();
    private double pitchAimX = 560.0;
    private double pitchAimHeight = 10.0;
    @FXML private javafx.scene.layout.StackPane viewportPane;
    @FXML private javafx.scene.layout.HBox pitchControls;
    @FXML private javafx.scene.layout.HBox aimControls;
    @FXML private Label inputHelpLabel;
    @FXML private Button pitchPreviousButton;
    @FXML private Button pitchNextButton;
    private double windupRemaining, pitchFlightSeconds;
    private double pitchStartX, pitchStartY, pitchStartHeight, pitchEndX, pitchEndHeight;
    private double pitchHeight = 18;
    private boolean swingMissed, playOutcomeRecorded, contactIsFoul;
    private double playResultHold;
    /** Seven defensive positions (3B, SS, 2B, 1B, LF, CF, RF). */
    private final double[] fielderRunClock = new double[7];
    private double pitcherRunClock;
    private int groundHolder = -1, baseReceiver = -1;
    private double groundHoldSeconds;
    private boolean defensiveThrowActive;
    // Broadcast-style overhead field keeps home plate, mound, diamond and outfield readable.
    // Mascot-only crowd keeps the fantasy roster consistent with the field;
    // no photographic people or baked human players are visible behind the
    // controllable characters.
    // Use the verified PNG plate for both renderer paths; the previous 4K file
    // was truncated and could silently fall back to a sparse vector crowd.
    private final Image stadiumBackground = GameController.loadImage("/application/stadium-mascot-crowd-v12-source.png");
    private final Image gameplaySprites = GameController.loadImage("/application/mascot-action-atlas-v16.png");
    private final CatcherMovementSimulation catcherMovement = new CatcherMovementSimulation(560,CatcherMovementSimulation.RECEIVE_Y,0x5EEDL);
    private final Image turboTanukiSprites = GameController.loadImage("/application/turbo-tanuki-action-atlas-v15.png");
    private AnimationTimer gameLoop;
    private long previousFrame;
    private double simulationRemainder;
    private double catcherHoldTime;
    private double catcherReceiveTime;
    private double catcherReceiveX;
    private final Image catcherSprites = GameController.loadImage("/application/circuit-catcher-v17.png");
    private Phase phase = Phase.PITCHING;
    private boolean pitchActive;
    private boolean paused;
    private double ballX;
    private double ballY;
    private double pitchSpeed;
    private double pitchTravel;
    private double pitchTargetY = GameController.zoneCenterY();
    private double aimY = GameController.zoneCenterY();
    private double batOffsetX;
    private double visualAimY = GameController.zoneCenterY();
    private double visualBatOffsetX;
    private double pointerX = 560.0;
    private double pointerY = GameController.zoneCenterY();
    private double automaticPitchDelay;
    private double shakeStrength;
    private double animationClock;
    private double swingAnimation;
    private static final double SWING_SECONDS=.24;
    private double pitchAnimation;
    private double contactFlash;
    private boolean fieldingActive;
    private boolean catchRequested;
    private double catchRequestLife;
    private double reactionRemaining;
    private final boolean[] fielderFacing = new boolean[7];
    private final boolean[] fielderMoving = new boolean[7];
    private final double[] fielderReach = new double[7];
    private boolean gameLoopStopped;
    private int heldFielder = -1;
    private double heldSeconds;
    private double previousBallX, previousBallY, previousBallHeight;
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
    private final WorldPoint[] fielderFeet = new WorldPoint[]{
        new WorldPoint(345,365), // third base
        new WorldPoint(430,300), // shortstop
        new WorldPoint(690,300), // second base
        new WorldPoint(775,365), // first base
        new WorldPoint(120,-220), // left field
        new WorldPoint(560,-410), // center field
        new WorldPoint(1000,-220) // right field
    };
    // World coordinates use the mascot's feet as the anchor.  The mound in
    // the batter-eye stadium art sits at this depth, so the pitcher remains
    // planted on the dirt instead of appearing above the field.
    private double pitcherX = 560.0;
    private double pitcherY = 368.0;
    private double pitcherTargetX = 560.0;
    private double pitcherTargetY = 368.0;
    private int fieldingFielderIndex = -1;
    private PitcherFieldingStage pitcherFieldingStage = PitcherFieldingStage.NONE;
    private double pitcherFieldingTimer;
    private HitResult pitcherFieldingResult = HitResult.SINGLE;
    private Mascot pitcherFieldingBatter = Mascot.TURBO_TANUKI;
    private final DefensiveThrowController defensiveThrowController = new DefensiveThrowController();

    private enum PitcherFieldingStage { NONE, REACTION, CHASE, PICKUP, HOLD, THROW }

    @FXML
    private void initialize() {
        if (GameSettings.getGameMode() == GameMode.BATTING_ONLY) {
            this.phase = Phase.BATTING;
            this.automaticPitchDelay = 0.7;
        } else {
            this.phase = Phase.PITCHING;
        }
        this.actionPlayerMascot = this.playerMascot();
        this.actionRivalMascot = this.rivalMascot();
        this.playerLabel.setText(GameSession.getPlayerName().toUpperCase());
        this.mascotLabel.setText("AT BAT: " + this.playerMascot().getDisplayName());
        this.difficultyLabel.setText(GameSettings.getDifficulty().toString());
        this.pitchSequenceController.configure(this.pitcherMascot(), this.phase == Phase.PITCHING ? this.rivalMascot() : this.playerMascot(), Mascot.CIRCUIT_BOT);
        this.configureViewport();
        this.configureKeyboardControls();
        this.configureMouseControls();
        this.updateControlsForPhase();
        this.updateHud();
        this.parkCamera.snap(this.phase == Phase.BATTING ? ParkCamera.Mode.BATTING : ParkCamera.Mode.PITCHING);
        this.statusLabel.setText(this.phase == Phase.BATTING ? "Track the pitch into the box. Space to swing." : "Aim inside the box. Click to pitch.");
        this.drawGame();
        this.gameLoop = new AnimationTimer(){

            public void handle(long now) {
                if (GameController.this.previousFrame == 0L) {
                    GameController.this.previousFrame = now;
                }
                double elapsed = Math.min((double)(now - GameController.this.previousFrame) / 1.0E9, 0.05);
                GameController.this.previousFrame = now;
                if (!GameController.this.paused) {
                    GameController.this.simulationRemainder += elapsed;
                    while (GameController.this.simulationRemainder >= 1.0 / 120.0) {
                        GameController.this.updateGame(1.0 / 120.0);
                        GameController.this.updateEffects(1.0 / 120.0);
                        GameController.this.simulationRemainder -= 1.0 / 120.0;
                    }
                }
                GameController.this.drawGame();
            }
        };
        this.gameLoop.start();
        this.gameCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null && newScene == null) this.stopGameLoop();
            if (newScene != null) newScene.windowProperty().addListener((o, oldWindow, newWindow) -> {
                if (oldWindow != null && newWindow == null) this.stopGameLoop();
                if (newWindow != null) newWindow.showingProperty().addListener((v, was, showing) -> {
                    if (!showing) this.stopGameLoop();
                });
            });
        });
        Platform.runLater(() -> ((Canvas)this.gameCanvas).requestFocus());
    }

    private void configureKeyboardControls() {
        this.gameCanvas.setFocusTraversable(true);
        this.gameCanvas.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();
            if (key == KeyCode.SPACE) {
                this.handleSwing();
            } else if (key == KeyCode.P || key == KeyCode.ENTER) {
                this.handlePrimaryAction();
            } else if (key == KeyCode.UP || key == KeyCode.W) {
                this.handleAimUp();
            } else if (key == KeyCode.DOWN || key == KeyCode.S) {
                this.handleAimDown();
            } else if (key == KeyCode.LEFT || key == KeyCode.A) {
                this.handleLeft();
            } else if (key == KeyCode.RIGHT || key == KeyCode.D) {
                this.handleRight();
            } else if (key == KeyCode.Q) {
                this.toggleStar();
            } else if (key == KeyCode.E) {
                this.commandRunners(true);
            } else if (key == KeyCode.R) {
                this.commandRunners(false);
            } else if (key == KeyCode.SHIFT || key == KeyCode.F) {
                this.dashRunners();
            } else if (key == KeyCode.ESCAPE) {
                this.handlePause();
            }
            event.consume();
        });
    }

    private void configureMouseControls() {
        this.gameCanvas.setOnMouseEntered(event -> this.gameCanvas.requestFocus());
        this.gameCanvas.setOnMouseMoved(event -> this.updatePointer(this.pointerX(event), this.pointerY(event)));
        this.gameCanvas.setOnMouseDragged(event -> this.updatePointer(this.pointerX(event), this.pointerY(event)));
        this.gameCanvas.setOnContextMenuRequested(event -> event.consume());
        this.gameCanvas.setOnMousePressed(event -> {
            this.updatePointer(this.pointerX(event), this.pointerY(event));
            if (event.getButton() == MouseButton.SECONDARY) {
                this.visualAimY = this.aimY;
                this.visualBatOffsetX = this.batOffsetX;
                this.handleSwing();
            } else if (event.getButton() == MouseButton.PRIMARY) {
                if (this.fieldingActive) {
                    if (this.parkCatchPromptContains(this.pointerX(event),this.pointerY(event))) this.attemptFieldingCatch();
                } else this.handlePrimaryAction();
            }
            event.consume();
        });
        this.gameCanvas.setOnScroll(event -> {
            if (this.phase == Phase.PITCHING && !this.pitchActive && !this.paused) {
                if (event.getDeltaY() > 0.0) {
                    this.handleRight();
                } else if (event.getDeltaY() < 0.0) {
                    this.handleLeft();
                }
            }
            event.consume();
        });
    }

    private void updatePointer(double x, double y) {
        if(paused||phase==Phase.FINISHED||fieldingActive||heldSeconds>0||defensiveThrowActive)return;
        if(phase==Phase.PITCHING&&pitchActive)return;
        pointerX=x;pointerY=y;
        WorldPoint aim=parkCamera.unprojectAtGroundY(x,y,610);
        pitchAimX=clamp(aim.x(),553,567);pitchAimHeight=clamp(aim.y(),3,18);
        aimY=658-(pitchAimHeight-3)/15*196;
        if(phase==Phase.BATTING)batOffsetX=pitchAimX-560;
        updateAimLabel();
    }

    private boolean parkCatchPromptContains(double x,double y) {
        if(!this.catchEligible())return false;
        GlovePose glove=this.glovePose(this.fieldingFielderIndex);
        WorldPoint p=this.parkCamera.project(glove.ground(),glove.height());
        return x>=p.x()-44&&x<=p.x()+44&&y>=p.y()-36&&y<=p.y()-10;
    }

    private void updateGame(double elapsed) {
        if(playResultHold>0){playResultHold=Math.max(0,playResultHold-elapsed);return;}
        if(catcherReceiveTime>0) {
            catcherReceiveTime=Math.max(0,catcherReceiveTime-elapsed);
            double t=1-catcherReceiveTime/.18,oldX=ballX,oldY=ballY,oldHeight=pitchHeight;
            ballX=pitchEndX;ballY=610+(CatcherMovementSimulation.RECEIVE_Y-610)*t;
            pitchHeight=pitchEndHeight+(CatcherMovementSimulation.GLOVE_HEIGHT-pitchEndHeight)*t;
            liveBallController.markPitchPosition(ballX,ballY,pitchHeight,(ballX-oldX)/elapsed,(ballY-oldY)/elapsed,(pitchHeight-oldHeight)/elapsed);
            if(catcherReceiveTime==0)finishTakenPitch();
            return;
        }
        if(phase==Phase.BATTING&&!pitchActive&&automaticPitchDelay>0&&!playInProgress()&&!runnerController.hasMovingRunners()) {
            automaticPitchDelay-=elapsed;
            if(automaticPitchDelay<=0)launchRivalPitch();
        }
        if(!pitchActive)return;
        if(windupRemaining>0) {
            windupRemaining=Math.max(0,windupRemaining-elapsed);
            if(windupRemaining==0) {
                playStateController.transition(BaseballPlayStateController.State.PITCH_IN_FLIGHT);
                liveBallController.releasePitch(pitchStartX,pitchStartY,pitchEndX,610,pitchSpeed,activePitch.spin(),activePitch.curvePixels());
                liveBallController.markPitchPosition(pitchStartX,pitchStartY,pitchStartHeight,0,0,0);
            } return;
        }
        pitchTravel=Math.min(1,pitchTravel+elapsed/pitchFlightSeconds);
        double t=pitchTravel,oldX=ballX,oldY=ballY,oldHeight=pitchHeight;
        ballX=pitchStartX+(pitchEndX-pitchStartX)*t+activePitch.curvePixels()*.14*Math.sin(Math.PI*t);
        ballY=pitchStartY+(610-pitchStartY)*t;
        pitchHeight=pitchStartHeight+(pitchEndHeight-pitchStartHeight)*t
                +.5*128.696*pitchFlightSeconds*pitchFlightSeconds*t*(1-t);
        liveBallController.markPitchPosition(ballX,ballY,pitchHeight,(ballX-oldX)/elapsed,(ballY-oldY)/elapsed,(pitchHeight-oldHeight)/elapsed);
        if(t>=1){if(phase==Phase.PITCHING&&!swingMissed)resolveRivalAtBat();else resolveTakenPitch();}
    }

    private void updateEffects(double elapsed) {
        animationClock+=elapsed;
        // Keep the role camera locked for the entire play.  The old live-ball
        // branch switched to ParkCamera.Mode.FIELDING as soon as contact was
        // made, which produced a second overhead view and made the scene jump
        // away from the batter/pitcher perspective.  Fielders and runners still
        // animate in the same camera; only the gameplay state changes.
        parkCamera.update(phase==Phase.BATTING?ParkCamera.Mode.BATTING:ParkCamera.Mode.PITCHING,elapsed);
        parkRenderer.updateEffects(elapsed);
        // Dust under anyone actually moving, on a timer so the rate is
        // independent of frame rate.
        dustClock+=elapsed;
        if(dustClock>=.055){
            dustClock=0;
            for(Runner runner:runnerController.runnersBackToFront()){
                if(!runner.isRunning())continue;
                parkRenderer.puff(runner.feet().x(),runner.feet().y());
                // A dash kicks up a heavier trail with a spark in it, so the
                // burst reads on screen and not just on the clock.
                if(runner.isDashing()){
                    parkRenderer.puff(runner.feet().x(),runner.feet().y());
                    parkRenderer.sparkle(runner.feet().x(),runner.feet().y(),5,Color.web("#ffe9a8"),9);
                }
            }
            for(int i=0;i<fielderFeet.length;i++)
                if(fielderMoving[i])parkRenderer.puff(fielderFeet[i].x(),fielderFeet[i].y());
        }
        reactionRemaining=Math.max(0,reactionRemaining-elapsed);
        catchRequestLife=Math.max(0,catchRequestLife-elapsed);if(catchRequestLife==0)catchRequested=false;
        catcherHoldTime=Math.max(0,catcherHoldTime-elapsed);
        catcherMovement.update(elapsed,pitchActive||catcherReceiveTime>0,pitchEndX,634);
        mascotAnimationController.update(elapsed);batSwingController.update(elapsed);
        previousBallX=liveBallController.ball().x();previousBallY=liveBallController.ball().y();previousBallHeight=liveBallController.ball().height();
        liveBallController.update(elapsed);
        runnerController.update(elapsed);
        updateBoundaryResult();
        updateFielderFeet(elapsed);separateFielderPositions();updatePitcherPosition(elapsed);
        updatePitcherFieldingSequence(elapsed);
        if(phase==Phase.BATTING&&physicalCatchEligible()){catchRequested=true;catchRequestLife=.45;}
        tryAutomaticFieldingCatch();
        updateGroundDefense(elapsed);
        if(heldSeconds>0){
            heldSeconds=Math.max(0,heldSeconds-elapsed);
            if(heldSeconds==0){
                heldFielder=-1;
                // Without this the fielder chosen to cover first on an earlier
                // grounder keeps first base as his target and never goes back to
                // his own position for the next batter.
                baseReceiver=-1;
                if(phase==Phase.PITCHING)registerRivalOut("Fly ball caught · batter out");else registerPlayerOut("Fly ball caught · batter out");
                scheduleNextRivalPitch();resolvePlayState();liveBallController.deadBall();playResultHold=.6;
            }
        }
        if(fieldingActive&&liveBallController.state()==LiveBallController.State.DEAD_BALL&&!playOutcomeRecorded)resolveMissedFieldingPlay();
        int completedRuns=runnerController.drainScoredRuns();
        if(completedRuns>0){
            if(phase==Phase.PITCHING)matchState.addRivalRuns(completedRuns);else matchState.addPlayerRuns(completedRuns);
            showFloating((phase==Phase.PITCHING?"RIVAL":"YOU")+" SCORES "+completedRuns,Color.web("#e9d79b"));updateHud();
        }
        matchState.setOccupiedBases(runnerController.isOccupied(1),runnerController.isOccupied(2),runnerController.isOccupied(3));
        String bases=scoreController.baseDiamondText();if(!bases.equals(baseDiamondLabel.getText()))baseDiamondLabel.setText(bases);
        swingAnimation=Math.max(0,swingAnimation-elapsed);pitchAnimation=Math.max(0,pitchAnimation-elapsed);
        contactFlash=Math.max(0,contactFlash-elapsed);
        visualBatOffsetX+=(batOffsetX-visualBatOffsetX)*Math.min(1,elapsed*12);
        visualAimY+=(aimY-visualAimY)*Math.min(1,elapsed*11);
        floatingTextLife=Math.max(0,floatingTextLife-elapsed);
        updateControlsForPhase();
    }

    @FXML
    private void handlePrimaryAction() {
        gameCanvas.requestFocus();
        if(paused||phase!=Phase.PITCHING||playResultHold>0)return;
        if(fieldingActive){attemptFieldingCatch();return;}
        if(pitchActive||catcherReceiveTime>0||playInProgress()||runnerController.hasMovingRunners())return;
        launchPlayerPitch();
    }

    private void launchPlayerPitch() {
        if(playInProgress()||runnerController.hasMovingRunners())return;
        actionPlayerMascot=playerMascot();activePitch=selectedPitch();
        pitchEndX=pitchAimX;pitchEndHeight=pitchAimHeight;
        double baseSpeed=GameSettings.getDifficulty().randomPitchSpeed(random.nextDouble());
        pitchSpeed=baseSpeed*activePitch.speedMultiplier*GameSession.getPitchStyle().speed()*playerMascot().getPitchingMultiplier()*1.114666667;
        boolean star=consumeStar();
        if(star){
            // The mascot's signature delivery: noticeably quicker and harder to
            // read, announced by name the way a star pitch is.
            pitchSpeed*=1.22;
            starPitchActive=true;
            showFloating(playerMascot().getSignaturePitch(),Color.web("#ffe066"));
        }
        beginPitchState(actionPlayerMascot,rivalMascot());
        statusLabel.setText(star?"STAR PITCH · "+playerMascot().getSignaturePitch()
                                :"Wind-up · "+activePitch.displayName);
    }

    /**
     * Star moves, in the spirit of the character baseball games: the meter fills
     * through good contact and clean defence, the player chooses the moment to
     * spend it rather than it firing on its own, and it comes out as that
     * mascot's named signature swing or pitch.
     */
    private boolean starArmed,starPitchActive;
    public boolean starReady(){return matchState.turbo()>=100;}
    @FXML
    private void toggleStar() {
        gameCanvas.requestFocus();
        if(!starReady()){
            if(!starArmed)showFloating("STAR NOT READY",Color.web("#c9d3b4"));
            starArmed=false;updateHud();return;
        }
        starArmed=!starArmed;
        Mascot user=playerMascot();
        showFloating(starArmed?"STAR ARMED · "+(phase==Phase.PITCHING?user.getSignaturePitch():user.getSignatureHit())
                              :"STAR HELD",Color.web("#ffe066"));
        updateHud();
    }
    /** Spends the star if one is armed and charged; false leaves the meter alone. */
    private boolean consumeStar() {
        if(!starArmed||!starReady())return false;
        starArmed=false;matchState.consumeTurbo();updateHud();return true;
    }

    /**
     * Manual base running, Superstar-Baseball style: the player can wave his
     * own runners on or hold them up instead of letting the fielding heuristic
     * in updateGroundDefense decide. It only ever touches the player's own
     * runners, so it is gated on the batting half of the inning -- during
     * PITCHING the runners on the paths belong to the rival.
     */
    private boolean canSteerRunners() {
        return !paused&&phase==Phase.BATTING&&runnerController.isLivePlay();
    }
    private void commandRunners(boolean advance) {
        gameCanvas.requestFocus();
        if(!canSteerRunners())return;
        runnerController.commandRunning(advance);
        showFloating(advance?"RUN!":"HOLD!",Color.web(advance?"#cde1a2":"#ead6a1"));
        statusLabel.setText(advance?"Runners sent · you are steering the bases"
                                   :"Runners holding · back to the bag");
        for(Runner runner:runnerController.runnersBackToFront())
            if(runner.isRunning())parkRenderer.puff(runner.feet().x(),runner.feet().y());
        updateHud();
    }
    private void dashRunners() {
        gameCanvas.requestFocus();
        if(paused||phase!=Phase.BATTING||!runnerController.hasMovingRunners())return;
        if(!runnerController.dashRunners())return;
        showFloating("DASH!",Color.web("#ffe066"));
        for(Runner runner:runnerController.runnersBackToFront())
            if(runner.isDashing())
                parkRenderer.sparkle(runner.feet().x(),runner.feet().y(),7,Color.web("#fff0b8"),16);
    }

    private Mascot previousPlayerBatter;
    private double dustClock;

    /**
     * Contact feedback: the swing is named and graded the moment the bat meets
     * the ball, with a star burst scaled to how well it was struck, so a perfect
     * hit feels different from a scrape rather than both just putting the ball
     * in play.
     */
    private void celebrateContact(BattingController.Contact contact,boolean starHit) {
        boolean homer=contact.result()==HitResult.HOME_RUN;
        String call=starHit?playerMascot().getSignatureHit()
                :homer?"OUT OF THE PARK!"
                :switch(contact.region()){
                    case PERFECT->"PERFECT!";
                    case SOLID->"NICE HIT!";
                    case GENERAL->contactIsFoul?"FOUL":"CONTACT";
                    default->"CONTACT";
                };
        showFloating(call,starHit||homer?Color.web("#ffe066"):Color.web("#e9f4c4"));
        int burst=starHit?26:homer?22:switch(contact.region()){
            case PERFECT->16;case SOLID->9;default->4;};
        Color tint=starHit?Color.rgb(255,226,120,.95)
                :contact.region()==BattingController.ContactRegion.PERFECT?Color.rgb(255,240,170,.9)
                :Color.rgb(240,248,210,.8);
        for(int i=0;i<burst;i++)parkRenderer.sparkle(560+visualBatOffsetX,610,13,tint,starHit||homer?26:16);
        if(starHit||homer)shakeStrength=Math.max(shakeStrength,homer?7:5);
    }
    /**
     * Team chemistry. Mascots who share a temperament play off each other, and
     * ones who pull against each other do not, so who bats behind whom changes
     * how the innings run rather than every mascot being interchangeable.
     * Returns a multiplier around 1.
     */
    private double lineupChemistry(Mascot batter,Mascot ahead) {
        if(batter==null||ahead==null||batter==ahead)return 1;
        String a=batter.getPersonality().toLowerCase(),b=ahead.getPersonality().toLowerCase();
        boolean warm=bothHave(a,b,"upbeat","cheerful","loud","fiery","electric");
        boolean cool=bothHave(a,b,"calm","cool","patient","precise","quick");
        if(warm||cool)return 1.12;
        // opposite temperaments: one winds the other up
        boolean clash=(hasAny(a,"loud","fiery","electric")&&hasAny(b,"calm","cool","patient","precise"))
                    ||(hasAny(b,"loud","fiery","electric")&&hasAny(a,"calm","cool","patient","precise"));
        return clash?.93:1;
    }
    /** Both temperaments mention the same trait. */
    private static boolean bothHave(String a,String b,String... traits) {
        for(String t:traits)if(a.contains(t)&&b.contains(t))return true;
        return false;
    }
    private static boolean hasAny(String a,String... traits) {
        for(String t:traits)if(a.contains(t))return true;
        return false;
    }

    private void launchRivalPitch() {
        if(pitchActive||phase!=Phase.BATTING||playInProgress()||runnerController.hasMovingRunners())return;
        actionRivalMascot=rivalMascot();activePitch=competitiveAIController.choosePitch(random,matchState.currentInning(),GameSettings.getDifficulty());
        pitchEndX=560+(random.nextDouble()-.5)*8;pitchEndHeight=6+random.nextDouble()*8;
        pitchSpeed=GameSettings.getDifficulty().randomPitchSpeed(random.nextDouble())*activePitch.speedMultiplier*1.114666667;
        beginPitchState(actionRivalMascot,playerMascot());statusLabel.setText("Watch the ball into the box · Space to swing");
    }

    private void resolveRivalAtBat() {
        boolean inZone=isInsideStrikeZone(ballX);
        double chance=GameSettings.getDifficulty().getRivalContactChance()-Math.abs(ballX-560)/3*.07-activePitch.breakDifficulty;
        // a spent star pitch is meaningfully tougher to square up
        if(starPitchActive){chance-=.22;starPitchActive=false;}
        if(!inZone||random.nextDouble()>clamp(chance,.12,.75)){swingMissed=inZone;resolveTakenPitch();return;}
        pitchActive=false;actionRivalMascot=rivalMascot();swingAnimation=SWING_SECONDS;
        playStateController.transition(BaseballPlayStateController.State.BATTER_SWINGING);
        batSwingController.begin();beginFieldingChallenge();
    }

    private void beginFieldingChallenge() {
        fieldingActive=true;catchRequested=false;reactionRemaining=.22;heldFielder=-1;playOutcomeRecorded=false;
        playStateController.transition(BaseballPlayStateController.State.BALL_IN_PLAY);
        double spray=(random.nextDouble()-.5)*1.42,quality=.35+random.nextDouble()*.6;
        runnerController.startLivePlay(rivalMascot());
        liveBallController.launchContact(ballX,ballY,pitchHeight,58+quality*62,16+quality*30,Math.sin(spray),-Math.cos(spray),(quality-.5)*34);
        assignLiveFielder();playStateController.transition(BaseballPlayStateController.State.FIELDING);
        statusLabel.setText("Ball in play · follow the flight");updateControlsForPhase();
    }

    private HitResult rivalHitResult() {
        double roll = this.random.nextDouble();
        if (roll < 0.58) return HitResult.SINGLE;
        if (roll < 0.84) return HitResult.DOUBLE;
        return HitResult.TRIPLE;
    }

    private WorldPoint fielderGlove() {
        return this.glovePose(Math.max(0,this.fieldingFielderIndex)).ground();
    }
    /**
     * Every actor on the field is this tall, in world units. It drives both the
     * drawn sprite and the physical glove position, so growing the mascots keeps
     * the ball meeting the glove instead of passing under it.
     */
    private static final double ACTOR_HEIGHT=32;
    /** Perspective compression shared by every actor and its glove. */
    private double readableHeight(WorldPoint feet,double height){
        return ParkSceneRenderer.readableHeight(parkCamera,feet,height);
    }
    private GlovePose glovePose(int index) {
        // The same height the sprite is drawn at, so a fielder's glove is always
        // where his hands are even out at the fence where the body is enlarged.
        return GlovePose.at(fielderFeet[index],readableHeight(fielderFeet[index],ACTOR_HEIGHT),
                fielderFacing[index],fielderReach[index]);
    }
    private boolean catchEligible() {
        return this.phase == Phase.PITCHING && this.physicalCatchEligible();
    }
    private boolean physicalCatchEligible() {
        return fieldingActive&&fieldingFielderIndex>=0
            &&liveBallController.state()==LiveBallController.State.AIRBORNE&&!liveBallController.bounced()
            &&reactionRemaining==0&&liveBallController.owner().isEmpty()
            &&CatchGeometry.eligible(liveBallController.ball(),fielderGlove(),glovePose(fieldingFielderIndex).height(),liveCatchRadius());
    }
    private double liveCatchRadius() {
        // A real glove is wider than a single pixel.  Use the ball speed to
        // give hard line drives a short, readable catch window while keeping
        // the test tied to the live trajectory and glove position.
        double speed=Math.hypot(liveBallController.ball().velocityX(),liveBallController.ball().velocityY());
        return speed>145?7.0:6.0;
    }
    private void attemptFieldingCatch() {
        if (this.catchEligible()) { this.catchRequested = true; this.catchRequestLife=.45; }
    }
    private void resolveMissedFieldingPlay() {
        finishDefensivePlay(false);
    }

    private void resolveTakenPitch() {
        pitchActive=false;catcherReceiveX=ballX;catcherReceiveTime=.18;updateControlsForPhase();
    }

    private void finishTakenPitch() {
        boolean secured=Math.abs(ballX-catcherMovement.x())<6&&Math.abs(ballY-catcherMovement.y())<7;
        catcherHoldTime=secured?.6:0;
        boolean inZone=isInsideStrikeZone(ballX);
        if(phase==Phase.PITCHING){if(inZone||swingMissed)registerStrikeForRival();else registerBallForRival();}
        else {
            if(swingMissed)registerStrikeForPlayer("Swing and miss");
            else if(inZone)registerStrikeForPlayer("Called strike");else registerBallForPlayer();
            deadBallAndReady();scheduleNextRivalPitch();
        }
        if(secured)liveBallController.caught(pitchSequenceController.catcher().name(),new WorldPoint(catcherMovement.x(),catcherMovement.y()),CatcherMovementSimulation.GLOVE_HEIGHT);
        playResultHold=.65;swingMissed=false;updateControlsForPhase();
    }

    @FXML
    private void handleSwing() {
        gameCanvas.requestFocus();
        if(paused||phase!=Phase.BATTING||!pitchActive||windupRemaining>0||swingMissed)return;
        actionPlayerMascot=playerMascot();swingAnimation=SWING_SECONDS;
        playStateController.transition(BaseballPlayStateController.State.BATTER_SWINGING);batSwingController.begin();
        double horizontalError=Math.abs(ballX-(560+batOffsetX))*8,timingDelta=(pitchTravel-.98)*310;
        // Chemistry: a batter following a team-mate he reads well gets a slightly
        // kinder window, which is where the lineup order starts to matter.
        double contactWindow=45*playerMascot().getContactMultiplier()
                *GameSettings.getDifficulty().getContactWindowMultiplier()
                *lineupChemistry(playerMascot(),previousPlayerBatter);
        // A star swing is both harder to miss and harder to defend, so spending
        // it at the right moment is the decision rather than a passive bonus.
        boolean turbo=consumeStar();
        if(turbo)contactWindow*=1.45;
        BattingController.Contact contact=battingController.evaluate(horizontalError,Math.hypot(timingDelta,Math.abs(pitchHeight-pitchAimHeight)*4),contactWindow,playerMascot().getPowerMultiplier(),turbo);
        competitiveAIController.recordSwing(timingDelta,contact.region());
        if(!contact.madeContact()){swingMissed=true;statusLabel.setText("Swing missed · catcher receiving");}
        else {pitchActive=false;contactIsFoul=contact.foul();resolvePlayerContact(contact,turbo);}
        updateControlsForPhase();
    }

    private void resolvePlayerContact(BattingController.Contact contact,boolean turboHit) {
        playOutcomeRecorded=false;
        celebrateContact(contact,turboHit);
        previousPlayerBatter=playerMascot();
        if(!contactIsFoul&&isPitcherGrounder()){startPitcherFieldingSequence(contact.power(),HitResult.SINGLE,playerMascot());showFloating("GROUND BALL",Color.web("#e9d79b"));}
        else {startHitMotion(contact.power(),contact.result(),playerMascot(),turboHit);fieldingActive=true;catchRequested=false;reactionRemaining=.22;assignLiveFielder();showFloating("BALL IN PLAY",Color.web("#e9d79b"));}
        matchState.incrementCombo();addTurbo(12+contact.quality()*12);
        statusLabel.setText("Ball in play · fielders are tracking the landing point");resetCount();updateHud();updateControlsForPhase();
    }

    @FXML
    private void handleAimUp() {
        if(!canAdjust())return;pitchAimHeight=clamp(pitchAimHeight+.6,3,18);aimY=658-(pitchAimHeight-3)/15*196;updateAimLabel();gameCanvas.requestFocus();
    }

    @FXML
    private void handleAimDown() {
        if(!canAdjust())return;pitchAimHeight=clamp(pitchAimHeight-.6,3,18);aimY=658-(pitchAimHeight-3)/15*196;updateAimLabel();gameCanvas.requestFocus();
    }

    @FXML
    private void handleLeft() {
        if (!this.canAdjust()) {
            return;
        }
        if (this.phase == Phase.PITCHING) {
            this.selectedPitchIndex = Math.floorMod(this.selectedPitchIndex - 1, GameSession.getPitchLoadout().size());
        } else {
            this.batOffsetX = Math.max(-7.0, this.batOffsetX - .5);
        }
        this.updateControlsForPhase();
        this.gameCanvas.requestFocus();
    }

    @FXML
    private void handleRight() {
        if (!this.canAdjust()) {
            return;
        }
        if (this.phase == Phase.PITCHING) {
            this.selectedPitchIndex = (this.selectedPitchIndex + 1) % GameSession.getPitchLoadout().size();
        } else {
            this.batOffsetX = Math.min(7.0, this.batOffsetX + .5);
        }
        this.updateControlsForPhase();
        this.gameCanvas.requestFocus();
    }

    private boolean canAdjust() {
        if (this.paused || this.phase == Phase.FINISHED || this.heldSeconds > 0) {
            return false;
        }
        if (this.fieldingActive) {
            this.statusLabel.setText("FIELD THE LIVE BALL WITH THE MOUSE AND LEFT-CLICK!");
            return false;
        }
        if (this.pitchActive && this.phase == Phase.PITCHING) {
            this.statusLabel.setText("WAIT FOR THE PITCH TO FINISH");
            return false;
        }
        return true;
    }

    private void registerStrikeForRival() {
        this.matchState.addStrike();
        this.matchState.incrementCombo();
        this.addTurbo(10.0);
        this.showFloating("STRIKE!", Color.web((String)"#56f2e3"));
        this.statusLabel.setText("RIVAL WHIFFS! KEEP THE COMBO GOING!");
        if (this.matchState.strikes() >= 3) {
            this.registerRivalOut("STRIKEOUT! RIVAL BATTER OUT!");
        } else {
            this.updateHud();
        }
        this.deadBallAndReady();
    }

    private void registerBallForRival() {
        this.matchState.addBall();
        this.matchState.resetCombo();
        this.statusLabel.setText("BALL - AIM INSIDE THE GLOWING ZONE");
        if (this.matchState.balls() >= 4) {
            this.runnerController.advance(HitResult.WALK,this.rivalMascot());
            this.matchState.recordWalk();
            this.resetCount();
            this.showFloating("WALK", Color.web("#e9d79b"));
            this.advanceRivalLineup();
        }
        this.updateHud();
        this.deadBallAndReady();
    }

    private void registerRivalOut(String message) {
        this.matchState.addOut();
        this.resetCount();
        this.advanceRivalLineup();
        this.showFloating("OUT " + this.matchState.outs() + "/3", Color.web((String)"#56f2e3"));
        this.statusLabel.setText(message);
        this.updateHud();
        if (this.matchState.outs() >= 3) {
            if (GameSettings.getGameMode() == GameMode.BOWLING_ONLY) {
                this.matchState.resetHalfInning();
                this.runnerController.clear();
                this.statusLabel.setText("BOWLING ONLY: NEXT BATTER UP!");
                this.updateHud();
            } else {
                this.beginBattingHalf();
            }
        }
    }

    private void registerStrikeForPlayer(String message) {
        this.matchState.addStrike();
        this.matchState.resetCombo();
        this.statusLabel.setText(message);
        this.showFloating("STRIKE " + this.matchState.strikes(), Color.web((String)"#ff8a66"));
        if (this.matchState.strikes() >= 3) {
            this.matchState.recordStrikeout();
            this.registerPlayerOut("STRIKE THREE! BATTER OUT!");
        } else {
            this.updateHud();
        }
    }

    private void registerFoulForPlayer() {
        if (this.matchState.strikes() < 2) {
            this.matchState.addStrike();
        }
        this.matchState.resetCombo();
        this.statusLabel.setText("FOUL BALL - PROTECT THE PLATE!");
        this.showFloating("FOUL", Color.web((String)"#ffe066"));
        this.updateHud();
    }

    private void registerBallForPlayer() {
        this.matchState.addBall();
        this.statusLabel.setText("BALL - GOOD EYE!");
        this.addTurbo(5.0);
        if (this.matchState.balls() >= 4) {
            Mascot walker = this.playerMascot();
            this.matchState.recordWalk();
            this.matchState.incrementCombo();
            this.resetCount();
            this.runnerController.advance(HitResult.WALK, walker);
            this.advancePlayerLineup();
            this.showFloating("MASCOT WALK", Color.web((String)"#ffe066"));
        }
        this.updateHud();
    }

    private void registerPlayerOut(String message) {
        this.matchState.addOut();
        this.matchState.resetCombo();
        this.resetCount();
        this.advancePlayerLineup();
        this.statusLabel.setText(message);
        this.showFloating("OUT " + this.matchState.outs() + "/3", Color.web((String)"#ff8a66"));
        this.updateHud();
        if (this.matchState.outs() >= 3) {
            if (GameSettings.getGameMode() == GameMode.BATTING_ONLY) {
                this.matchState.resetHalfInning();
                this.runnerController.clear();
                this.automaticPitchDelay = 0.7;
                this.statusLabel.setText("BATTING ONLY: NEXT PITCH COMING!");
                this.updateHud();
            } else if (this.matchState.currentInning() >= 3) {
                this.finishGame();
            } else {
                this.beginNextInning();
            }
        }
    }

    private void beginNextInning() {
        this.matchState.nextInning();
        this.phase = Phase.PITCHING;
        this.pitchActive = false;
        this.matchState.resetHalfInning();
        this.runnerController.clear();
        this.aimY = GameController.zoneCenterY();
        this.batOffsetX = 0.0;
        this.visualAimY = this.aimY;
        this.visualBatOffsetX = 0.0;
        this.automaticPitchDelay = 0.0;
        this.liveBallController.deadBall();
        this.resolvePlayState();
        this.statusLabel.setText("INNING " + this.matchState.currentInning() + "! YOUR LINEUP TAKES THE FIELD!");
        this.showFloating("INNING " + this.matchState.currentInning(), Color.web((String)"#56f2e3"));
        this.updateControlsForPhase();
        this.updateHud();
    }

    private void beginBattingHalf() {
        this.phase = Phase.BATTING;
        this.matchState.resetHalfInning();
        this.runnerController.clear();
        this.aimY = GameController.zoneCenterY();
        this.batOffsetX = 0.0;
        this.visualAimY = this.aimY;
        this.visualBatOffsetX = 0.0;
        this.automaticPitchDelay = 0.7;
        this.liveBallController.deadBall();
        this.resolvePlayState();
        this.statusLabel.setText("BOTTOM HALF! MOVE THE MOUSE, THEN RIGHT-CLICK TO SWING!");
        this.showFloating("YOUR TURN TO BAT!", Color.web((String)"#ffe066"));
        this.updateControlsForPhase();
        this.updateHud();
    }

    private void finishGame() {
        this.phase = Phase.FINISHED;
        this.pitchActive = false;
        this.liveBallController.deadBall();
        this.stopGameLoop();
        GameSession.finish(this.matchState, this.rivalMascot().getTeamName());
        SceneManager.showGameOver();
    }

    private void scheduleNextRivalPitch() {
        if (this.phase == Phase.BATTING) {
            this.automaticPitchDelay = 0.45;
        }
    }

    @FXML
    private void handlePause() {
        this.paused = !this.paused;
        this.pauseButton.setText(this.paused ? "RESUME" : "PAUSE");
        this.statusLabel.setText(this.paused ? "RACE PAUSED" : "PLAY BALL!");
        this.gameCanvas.requestFocus();
    }

    @FXML
    private void handleMainMenu() {
        this.stopGameLoop();
        SceneManager.showMainMenu();
    }

    private void stopGameLoop() {
        if (this.gameLoop != null) {
            this.gameLoop.stop();
            this.gameLoopStopped = true;
        }
    }

    private void resetCount() {
        this.matchState.resetCount();
    }

    private void addTurbo(double amount) {
        this.matchState.addTurbo(amount);
        this.updateHud();
    }

    private void triggerImpact(double shake, int particleCount) {
        if (GameSettings.isScreenShakeEnabled()) {
            this.shakeStrength = shake;
        }
        this.burst(this.visualAimY, 548.0, Color.web((String)this.playerMascot().getAccentColor()), particleCount);
    }

    private void startHitMotion(double power,HitResult result,Mascot runner,boolean turboHit) {
        contactFlash=.22;playStateController.transition(BaseballPlayStateController.State.BALL_IN_PLAY);
        runnerController.startLivePlay(runner);
        double spray=clamp(visualBatOffsetX/7*.6+(pitchTravel-.97)*4,-1.05,1.05);
        if(contactIsFoul)spray=spray<0?-1.15:1.15;
        liveBallController.launchContact(ballX,ballY,pitchHeight,58+clamp(power,.15,1)*62,
            contactIsFoul?8:16+clamp(power,.15,1)*30,Math.sin(spray),-Math.cos(spray),(pitchTravel-.97)*80);
        contactIsFoul=false;parkTrail.clear();
        playStateController.transition(BaseballPlayStateController.State.FIELDING);
        pitchMetricsLabel.setText(String.format("Exit %.0f mph · launch %.0f°",liveBallController.exitVelocity(),liveBallController.launchAngle()));
    }

    private void separateFielderPositions() {
        final double minimum=14;
        for(int i=0;i<fielderFeet.length;i++)for(int j=i+1;j<fielderFeet.length;j++){
            WorldPoint a=fielderFeet[i],b=fielderFeet[j];double dx=b.x()-a.x(),dy=b.y()-a.y(),d=Math.hypot(dx,dy);
            if(d>=minimum)continue;
            double nx=d<.001?1:dx/d,ny=d<.001?0:dy/d;
            double push=Math.min(.6,(minimum-d)/2);
            if(i!=fieldingFielderIndex&&i!=groundHolder&&i!=baseReceiver)fielderFeet[i]=new WorldPoint(a.x()-nx*push,a.y()-ny*push);
            if(j!=fieldingFielderIndex&&j!=groundHolder&&j!=baseReceiver)fielderFeet[j]=new WorldPoint(b.x()+nx*push,b.y()+ny*push);
        }
    }

    private CameraTransitionController.View trajectoryView(HitResult result,
                                                            double horizontalDirection,
                                                            double verticalDirection) {
        if (result == HitResult.HOME_RUN) return CameraTransitionController.View.DEEP_HOME_RUN;
        if (verticalDirection > 0.35) return CameraTransitionController.View.GROUND_BALL;
        if (verticalDirection < -0.55) return CameraTransitionController.View.HIGH_FLY_BALL;
        if (horizontalDirection < -0.25) return CameraTransitionController.View.LEFT_FIELD;
        if (horizontalDirection > 0.25) return CameraTransitionController.View.RIGHT_FIELD;
        return CameraTransitionController.View.LOW_LINE_DRIVE;
    }

    private void beginPitchState(Mascot pitcher,Mascot batter) {
        resolvePlayState();pitchSequenceController.configure(pitcher,batter,Mascot.CIRCUIT_BOT);
        playStateController.transition(BaseballPlayStateController.State.PITCH_WINDUP);
        pitchStartX=pitcherX+4;pitchStartY=pitcherY;pitchStartHeight=20+FieldGeometry.surfaceHeight(new WorldPoint(pitcherX,pitcherY));
        ballX=pitchStartX;ballY=pitchStartY;pitchHeight=pitchStartHeight;pitchTravel=0;
        windupRemaining=.48;pitchAnimation=.75;
        pitchFlightSeconds=Math.max(.48,242/Math.max(250,pitchSpeed));
        liveBallController.holdByPitcher(pitcher.name(),pitchStartX,pitchStartY);
        liveBallController.ball().setMotion(ballX,ballY,pitchHeight,0,0,0);
        pitchActive=true;swingMissed=false;playOutcomeRecorded=false;parkTrail.clear();
        matchState.recordPitch();pitchMetricsLabel.setText(String.format("%s · %.0f mph",activePitch.displayName,pitchSpeed/5.866666667));
        updateControlsForPhase();
    }

    private void deadBallAndReady() {
        this.liveBallController.deadBall();
        this.resolvePlayState();
    }

    private void resolvePlayState() {
        BaseballPlayStateController.State state = this.playStateController.state();
        if (state == BaseballPlayStateController.State.PITCH_IN_FLIGHT || state == BaseballPlayStateController.State.BATTER_SWINGING || state == BaseballPlayStateController.State.BALL_IN_PLAY || state == BaseballPlayStateController.State.FIELDING || state == BaseballPlayStateController.State.DEFENSIVE_THROW || state == BaseballPlayStateController.State.RUNNERS_ADVANCING) {
            this.playStateController.transition(BaseballPlayStateController.State.PLAY_RESOLVED);
        }
        this.playStateController.forceReadyAfterResolvedPlay();
        this.cameraTransitionController.transitionTo(CameraTransitionController.View.BATTING);
    }

    private void burst(double x, double y, Color color, int count) {
        int index = 0;
        while (index < count) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double speed = 55.0 + this.random.nextDouble() * 180.0;
            this.particles.add(new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed - 45.0, color, 0.55 + this.random.nextDouble() * 0.55));
            ++index;
        }
    }

    private void showFloating(String text, Color color) {
        this.floatingText = text;
        this.floatingTextColor = color;
        this.floatingTextLife = 1.35;
    }

    private Mascot playerMascot() {
        return this.phase == Phase.PITCHING ? GameSession.getPlayerLineup().pitcher() : GameSession.getPlayerLineup().mascotAt(this.playerLineupIndex);
    }

    private Mascot rivalMascot() {
        return this.phase == Phase.BATTING ? GameSession.getRivalLineup().pitcher() : GameSession.getRivalLineup().mascotAt(this.rivalLineupIndex);
    }

    private Mascot displayedPlayerMascot() {
        boolean actionInProgress = this.phase == Phase.BATTING && this.swingAnimation > 0.0 || this.phase == Phase.PITCHING && this.pitchAnimation > 0.0;
        return actionInProgress && this.actionPlayerMascot != null ? this.actionPlayerMascot : this.playerMascot();
    }

    private Mascot displayedRivalMascot() {
        boolean actionInProgress = this.phase == Phase.PITCHING && this.swingAnimation > 0.0 || this.phase == Phase.BATTING && this.pitchAnimation > 0.0;
        return actionInProgress && this.actionRivalMascot != null ? this.actionRivalMascot : this.rivalMascot();
    }

    private void advancePlayerLineup() {
        this.playerLineupIndex = (this.playerLineupIndex + 1) % GameSession.getPlayerLineup().slots().size();
    }

    private void advanceRivalLineup() {
        this.rivalLineupIndex = (this.rivalLineupIndex + 1) % GameSession.getRivalLineup().slots().size();
    }

    private void updateHud() {
        ScoreboardState score=scoreController.snapshot();
        Mascot batter=phase==Phase.PITCHING?displayedRivalMascot():displayedPlayerMascot();
        mascotLabel.setText("BATTER  "+batter.getDisplayName()+"     PITCHER  "+pitcherMascot().getDisplayName());
        inningLabel.setText("INNING "+score.inning()+" / 3");
        playerRunsLabel.setText(Integer.toString(score.playerRuns()));rivalRunsLabel.setText(Integer.toString(score.rivalRuns()));
        ballsLabel.setText(Integer.toString(score.balls()));strikesLabel.setText(Integer.toString(score.strikes()));
        outsLabel.setText(Integer.toString(score.outs()));comboLabel.setText("x"+score.combo());
        turboMeter.setProgress(matchState.turbo()/100);
        turboLabel.setText(starArmed?"★ STAR ARMED":matchState.turbo()>=100?"STAR READY · Q":(int)matchState.turbo()+"%");
        baseDiamondLabel.setText(scoreController.baseDiamondText());updateControlsForPhase();
    }

    private void updateControlsForPhase() {
        boolean pitching=phase==Phase.PITCHING,batting=phase==Phase.BATTING,live=playInProgress();
        boolean available=!paused&&playResultHold<=0&&catcherReceiveTime<=0;
        phaseLabel.setText(pitching?"TOP · YOU PITCH":batting?"BOTTOM · YOU BAT":"FINAL");
        boolean canPitch=pitching&&!live;
        setControlVisible(pitchControls,canPitch);setControlVisible(aimControls,!live&&phase!=Phase.FINISHED);
        setControlVisible(swingButton,batting&&!live);
        swingButton.setDisable(!available||!pitchActive||windupRemaining>0||swingMissed);swingButton.setText("SWING  ·  SPACE");
        setControlVisible(primaryButton,canPitch||(pitching&&catchEligible()));
        primaryButton.setText(catchEligible()?"CATCH  ·  P":"PITCH  ·  CLICK / P");
        primaryButton.setDisable(!available||pitchActive||runnerController.hasMovingRunners()&&!catchEligible());
        pitchTypeLabel.setText(pitching?selectedPitch().displayName:activePitch.displayName);
        pitchPreviousButton.setDisable(!available||pitchActive);pitchNextButton.setDisable(!available||pitchActive);
        if(inputHelpLabel!=null)inputHelpLabel.setText(live?
            (pitching&&catchEligible()?"P / click glove prompt · catch":
             canSteerRunners()?"E · send runners     R · hold up     Shift · dash"
                    +(runnerController.isManualRunning()?"     YOU ARE STEERING":""):
             "Follow the ball · runners and fielders move automatically"):
            pitching?"Mouse · aim     A / D or wheel · pitch type     P or click · pitch"
                    +(starReady()||starArmed?"     Q · star pitch":""):
            "Mouse or arrow keys · aim     Space or right click · swing"
                    +(starReady()||starArmed?"     Q · star swing":"     Computer pitches automatically"));
        updateAimLabel();
    }

    private void updateAimLabel() {
        String vertical=pitchAimHeight>12?"HIGH":pitchAimHeight<8?"LOW":"MIDDLE";
        double x=phase==Phase.BATTING?batOffsetX:pitchAimX-560;
        aimLabel.setText(vertical+" · "+(x<-1.2?"LEFT":x>1.2?"RIGHT":"CENTER"));
    }


    /** Mouse events arrive in backing-canvas pixels; aiming works in logical units. */
    private double pointerX(javafx.scene.input.MouseEvent event){return event.getX()/ParkSceneRenderer.RENDER_SCALE;}
    private double pointerY(javafx.scene.input.MouseEvent event){return event.getY()/ParkSceneRenderer.RENDER_SCALE;}

    private void configureViewport() {
        if(viewportPane==null)return;
        javafx.scene.Group surface=new javafx.scene.Group(gameCanvas);
        viewportPane.getChildren().setAll(surface);
        // Give the canvas RENDER_SCALE times as many real pixels as the logical
        // 1180x650 scene, then shrink the node by the same factor. The window
        // shows the same framing as before, but the display is handed a
        // high-resolution image instead of a stretched one.
        gameCanvas.setWidth(ParkSceneRenderer.VIEW_WIDTH*ParkSceneRenderer.RENDER_SCALE);
        gameCanvas.setHeight(ParkSceneRenderer.VIEW_HEIGHT*ParkSceneRenderer.RENDER_SCALE);
        Runnable fit=()->{
            double scale=Math.min(viewportPane.getWidth()/ParkSceneRenderer.VIEW_WIDTH,
                    viewportPane.getHeight()/ParkSceneRenderer.VIEW_HEIGHT);
            if(scale>0){
                double node=scale/ParkSceneRenderer.RENDER_SCALE;
                gameCanvas.setScaleX(node);gameCanvas.setScaleY(node);
            }
        };
        viewportPane.widthProperty().addListener((o,a,b)->fit.run());
        viewportPane.heightProperty().addListener((o,a,b)->fit.run());Platform.runLater(fit);
    }
    private static void setControlVisible(javafx.scene.Node node,boolean visible) {
        if(node!=null){node.setVisible(visible);node.setManaged(visible);}
    }
    private boolean playInProgress() {
        return fieldingActive||heldSeconds>0||groundHolder>=0||defensiveThrowActive
            ||pitcherFieldingStage!=PitcherFieldingStage.NONE
            ||(!pitchActive&&catcherReceiveTime<=0&&liveBallController.isLive());
    }
    private void assignLiveFielder() {
        DefensiveAssignmentController.Assignment a=defensiveAssignmentController.assign(List.of(fielderFeet),liveBallController.ball());
        fieldingFielderIndex=a.primaryIndex();fieldingTargetX=a.predictedLanding().x();fieldingTargetY=a.predictedLanding().y();
    }

    private WorldPoint boundField(WorldPoint p) {
        // Whole physical park, with foul territory and room behind home.
        double dx=p.x()-560,dy=p.y()-610;
        double radius=Math.hypot(dx,dy),limit=FieldGeometry.isFair(p)?FieldGeometry.fenceRadius(p)-8:1450;
        if(radius>limit){dx*=limit/radius;dy*=limit/radius;}
        return new WorldPoint(clamp(560+dx,-650,1770),clamp(610+dy,-990,690));
    }
    private void chooseBaseReceiver(int except) {
        baseReceiver=-1;double best=Double.POSITIVE_INFINITY;
        for(int i=0;i<fielderFeet.length;i++){
            if(i==except)continue;double d=fielderFeet[i].distanceTo(FieldGeometry.FIRST);
            if(d<best){best=d;baseReceiver=i;}
        }
    }
    private void updateBoundaryResult() {
        if(playOutcomeRecorded||!runnerController.isLivePlay())return;
        LiveBallController.BoundaryOutcome result=liveBallController.boundaryOutcome();
        if(result==LiveBallController.BoundaryOutcome.NONE)return;
        fieldingActive=false;groundHolder=-1;defensiveThrowActive=false;pitcherFieldingStage=PitcherFieldingStage.NONE;
        playOutcomeRecorded=true;
        if(result==LiveBallController.BoundaryOutcome.HOME_RUN){
            runnerController.awardHomeRun();matchState.recordHit(HitResult.HOME_RUN);showFloating("HOME RUN",Color.web("#e9d79b"));statusLabel.setText("Home run · fair ball cleared the fence");
        }else if(result==LiveBallController.BoundaryOutcome.GROUND_RULE_DOUBLE){
            runnerController.advanceLiveRunners(2);runnerController.completeLivePlay();matchState.recordHit(HitResult.DOUBLE);
            showFloating("GROUND-RULE DOUBLE",Color.web("#e9d79b"));statusLabel.setText("Ball bounced over the fence · two bases");
        }else{
            runnerController.flyOut();if(phase==Phase.BATTING)registerFoulForPlayer();
            else {if(matchState.strikes()<2)matchState.addStrike();statusLabel.setText("Foul ball");}
        }
        if(result!=LiveBallController.BoundaryOutcome.FOUL){if(phase==Phase.PITCHING)advanceRivalLineup();else advancePlayerLineup();resetCount();}
        playResultHold=1.2;resolvePlayState();scheduleNextRivalPitch();updateHud();
    }
    private void updateGroundDefense(double elapsed) {
        if(pitcherFieldingStage!=PitcherFieldingStage.NONE||playOutcomeRecorded)return;
        Baseball b=liveBallController.ball();
        if(fieldingActive&&liveBallController.bounced()&&fieldingFielderIndex>=0&&b.height()<5){
            int index=fieldingFielderIndex;
            if(fielderFeet[index].distanceTo(new WorldPoint(b.x(),b.y()))<7){
                groundHolder=index;groundHoldSeconds=1.05;fieldingActive=false;chooseBaseReceiver(index);
                liveBallController.field(fieldingMascot().name(),new WorldPoint(b.x(),b.y()),b.height());
                statusLabel.setText(fieldingMascot().getDisplayName()+" picks up the ground ball");
            }
        }
        if(groundHolder>=0&&!defensiveThrowActive){
            groundHoldSeconds=Math.max(0,groundHoldSeconds-elapsed);
            GlovePose hand=glovePose(groundHolder);
            liveBallController.moveHeldBall(hand.ground(),hand.height()*clamp((1.05-groundHoldSeconds)/.45,0,1));
            if(groundHoldSeconds==0&&baseReceiver>=0&&fielderFeet[baseReceiver].distanceTo(FieldGeometry.FIRST)<5){
                GlovePose receiver=glovePose(baseReceiver);
                liveBallController.throwFromFielder(hand.ground(),hand.height(),receiver.ground(),receiver.height(),300*fieldingMascot().getThrowingMultiplier());
                defensiveThrowActive=true;statusLabel.setText("Throw to first · watch the runner");
            }
        }
        if(defensiveThrowActive&&liveBallController.state()==LiveBallController.State.CAUGHT){
            boolean out=!runnerController.batterReachedFirst()&&baseReceiver>=0&&fielderFeet[baseReceiver].distanceTo(FieldGeometry.FIRST)<5;
            finishDefensivePlay(out);
        }
        // Runners take another base only after reaching the previous base with time to spare.
        if(fieldingActive&&runnerController.liveBatter()!=null&&!runnerController.liveBatter().isRunning()
                &&runnerController.batterBasesReached()<3){
            int next=runnerController.batterBasesReached()+1;
            double fielderSeconds=fielderFeet[Math.max(0,fieldingFielderIndex)].distanceTo(new WorldPoint(b.x(),b.y()))/95;
            // Only while the player has not taken the bases over himself.
            if(fielderSeconds>2.8)runnerController.autoAdvanceLiveRunners(next);
        }
    }
    private void finishDefensivePlay(boolean out) {
        if(playOutcomeRecorded)return;
        playOutcomeRecorded=true;fieldingActive=false;groundHolder=-1;defensiveThrowActive=false;
        if(baseReceiver>=0){
            GlovePose glove=glovePose(baseReceiver);
            int captain=phase==Phase.PITCHING?playerMascot().ordinal():rivalMascot().ordinal();
            liveBallController.caught(Mascot.values()[(captain+baseReceiver+1)%Mascot.values().length].name(),glove.ground(),glove.height());
        }else liveBallController.deadBall();
        if(out){
            runnerController.forceOutBatter();
            if(matchState.outs()<2)runnerController.completeLivePlay();else runnerController.clear();
            if(phase==Phase.PITCHING)registerRivalOut("Throw reached first before the runner · OUT");
            else registerPlayerOut("Throw reached first before the runner · OUT");
            showFloating("OUT AT FIRST",Color.web("#ead6a1"));
        }else{
            int reached=Math.max(1,runnerController.batterBasesReached());
            HitResult result=reached>=3?HitResult.TRIPLE:reached==2?HitResult.DOUBLE:HitResult.SINGLE;
            matchState.recordHit(result);runnerController.completeLivePlay();
            if(phase==Phase.PITCHING)advanceRivalLineup();else advancePlayerLineup();
            resetCount();statusLabel.setText("Safe · "+result.name().toLowerCase().replace('_',' '));showFloating("SAFE",Color.web("#cde1a2"));
        }
        baseReceiver=-1;playResultHold=.85;resolvePlayState();scheduleNextRivalPitch();updateHud();
    }

    private void drawGame() {
        GraphicsContext g=gameCanvas.getGraphicsContext2D();
        // Everything below is authored in the logical 1180x650 space; the
        // transform maps that onto the higher-resolution backing canvas.
        double rs=ParkSceneRenderer.RENDER_SCALE;
        g.save();g.setTransform(rs,0,0,rs,0,0);g.clearRect(0,0,1180,650);g.setImageSmoothing(true);
        parkRenderer.field(g,parkCamera,runnerController.path());
        parkRenderer.wind(g,parkCamera,animationClock);
        parkRenderer.grass(g,parkCamera,animationClock);
        boolean batting=phase==Phase.BATTING,inPlay=playInProgress()||runnerController.hasMovingRunners();
        record Actor(Mascot mascot,WorldPoint feet,ParkSceneRenderer.Pose pose,boolean facing,double height,double clock,String label,boolean playerTeam){}
        List<Actor> actors=new ArrayList<>();
        // The post-contact broadcast view keeps the complete diamond visible.
        // Increase only the on-field sprite draw height in that view so the
        // active fielder, runner and ball remain readable without cropping the
        // foul lines or fence.
        double fieldActorScale=parkCamera.currentMode()==ParkCamera.Mode.FIELDING?1.62:1.0;
        for(int i=0;i<fielderFeet.length;i++){
            int captain=batting?rivalMascot().ordinal():playerMascot().ordinal();
            Mascot fielder=Mascot.values()[(captain+i+1)%Mascot.values().length];
            ParkSceneRenderer.Pose pose=fielderMoving[i]?ParkSceneRenderer.Pose.RUN:ParkSceneRenderer.Pose.READY;
            if(i==heldFielder)pose=ParkSceneRenderer.Pose.HOLD;
            if(i==groundHolder)pose=groundHoldSeconds>.65?ParkSceneRenderer.Pose.PICKUP:ParkSceneRenderer.Pose.HOLD;
            if(defensiveThrowActive&&i==groundHolder)pose=ParkSceneRenderer.Pose.THROW;
            actors.add(new Actor(fielder,fielderFeet[i],pose,fielderFacing[i],
                readableHeight(fielderFeet[i],ACTOR_HEIGHT*fieldActorScale),fielderRunClock[i],null,phase==Phase.PITCHING));
        }
        WorldPoint pitcherFeet=new WorldPoint(pitcherX,pitcherY);
        // Windup covers the lead-in (pitchAnimation above the .27 release
        // threshold already used elsewhere for the ball's flight timing); the
        // release frame only shows once the ball actually leaves the hand.
        ParkSceneRenderer.Pose pp=switch(pitcherFieldingStage){
            case CHASE->ParkSceneRenderer.Pose.RUN;case PICKUP->ParkSceneRenderer.Pose.PICKUP;
            case HOLD->ParkSceneRenderer.Pose.HOLD;case THROW->ParkSceneRenderer.Pose.THROW;
            default->pitchAnimation>0.27?ParkSceneRenderer.Pose.WINDUP:pitchAnimation>0?ParkSceneRenderer.Pose.PITCH:ParkSceneRenderer.Pose.READY;};
        actors.add(new Actor(pitcherMascot(),pitcherFeet,pp,true,readableHeight(pitcherFeet,ACTOR_HEIGHT),
            pp==ParkSceneRenderer.Pose.RUN?pitcherRunClock:animationClock,batting||inPlay?null:"YOU · PITCHER",phase==Phase.PITCHING));
        // The batting eye naturally excludes its catcher; pitching shows the receiver behind home.
        if(parkCamera.currentMode()!=ParkCamera.Mode.BATTING)
            actors.add(new Actor(pitchSequenceController.catcher(),new WorldPoint(catcherMovement.x(),catcherMovement.y()),
                ParkSceneRenderer.Pose.CATCH,false,
                CatcherMovementSimulation.CROUCH_HEIGHT*(ACTOR_HEIGHT/24)*fieldActorScale,animationClock,null,phase==Phase.PITCHING));
        if(!inPlay||swingAnimation>0)actors.add(new Actor(batting?displayedPlayerMascot():displayedRivalMascot(),new WorldPoint(538,610),
            swingAnimation>0?ParkSceneRenderer.Pose.SWING:ParkSceneRenderer.Pose.BATTING,true,
            // The batter used to be forced to 42 in the pitching view while every
            // other actor stayed at 24, so he towered over the catcher beside him
            // with a head to match. Everyone on the field is one height now; the
            // pitching camera's longer lens already makes the batter read.
            // 1.2729 rather than 1.08: the rebuilt batting cell is 1.32 body
            // heights tall where the old content box was 1.12, so this keeps the
            // batter's body exactly the size it was before while the cell's bat
            // overhang lives in the padding.
            ACTOR_HEIGHT*1.2729*fieldActorScale,
            // Pose.SWING reads the clock as swing progress, 0 at the load and 1
            // at the finish, so the renderer can sweep the bat through.
            swingAnimation>0?1-swingAnimation/SWING_SECONDS:animationClock,
            batting?"YOU · BATTER":"BATTER",batting));
        // Base runners are batters, not fielders, so they run empty-handed.
        for(Runner runner:runnerController.runnersBackToFront()){
            // A runner sitting on home plate has not left the box yet, so he is
            // the batter and is already drawn there. Adding him again stacked a
            // second mascot on the plate, showing as a stray head and glove
            // poking out from behind the batter for the whole at-bat.
            if(!runner.isRunning()&&runner.lapPosition()<.0001)continue;
            // Continuous stride phase rather than the quantised frame index, so
            // the renderer can add sub-frame bounce between the four drawn frames.
            actors.add(new Actor(runner.mascot(),runner.feet(),
                runner.isRunning()?ParkSceneRenderer.Pose.BASERUN:ParkSceneRenderer.Pose.READY,runner.facingRight(),
                readableHeight(runner.feet(),ACTOR_HEIGHT*fieldActorScale),
                runner.runPhase()*.09+.001,null,phase==Phase.BATTING));
        }
        actors.sort(java.util.Comparator.comparingDouble((Actor a)->parkCamera.depthAt(a.feet())).reversed());
        for(Actor a:actors)if(parkCamera.isVisible(a.feet()))
            parkRenderer.mascot(g,parkCamera,a.mascot(),a.feet(),a.pose(),a.facing(),a.height(),a.clock(),a.label(),
                a.playerTeam()?Color.web("#2f67db"):Color.web("#d63f4d"));
        // Team markers go on last, over every sprite: drawn inside each actor's
        // own pass they were painted over by whoever stood in front.
        parkRenderer.drawTeamPointers(g);
        parkRenderer.drawEffects(g,parkCamera);
        if(!inPlay)parkRenderer.zone(g,parkCamera,batting?560+visualBatOffsetX:pitchAimX,pitchAimHeight,contactFlash>0);
        Baseball b=liveBallController.ball();
        if(pitchActive||catcherReceiveTime>0||b.active()||heldFielder>=0||groundHolder>=0||catcherHoldTime>0||playResultHold>0&&playOutcomeRecorded){
            WorldPoint ground=new WorldPoint(b.x(),b.y());
            if(parkCamera.isVisible(ground)){
                WorldPoint p=parkCamera.project(ground,b.height());
                WorldPoint prev=null;int n=0;
                for(LiveBallController.TrailPoint q:liveBallController.worldTrail()){
                    WorldPoint gp=new WorldPoint(q.x(),q.y());
                    if(!parkCamera.isVisible(gp))continue;
                    WorldPoint t=parkCamera.project(gp,q.height());
                    if(prev!=null){g.setStroke(Color.rgb(245,239,187,.30));g.setLineWidth(1.8);g.strokeLine(prev.x(),prev.y(),t.x(),t.y());}
                    prev=t;if(++n>12)break;
                }
                // A real baseball is barely a unit across, so at outfield depth it
                // collapsed to a two or three pixel speck and was easy to lose.
                // Drawn deliberately oversized, with a floor that keeps it
                // readable no matter how far away the play is.
                double ballDiameter=(heldFielder>=0||groundHolder>=0||catcherHoldTime>0)
                    ?Math.max(10,parkCamera.scaleAt(ground)*2.9)
                    :Math.max(8,parkCamera.scaleAt(ground)*1.9);
                parkRenderer.ball(g,p,parkCamera.project(ground,0),ballDiameter);
                if(p.x()<18||p.x()>1162||p.y()<30||p.y()>632)
                    parkRenderer.tag(g,clamp(p.x(),100,1080),clamp(p.y(),70,620),"BALL "+(p.x()<0?"←":p.x()>1180?"→":"↑"));
            }
            if(pitchActive&&windupRemaining<=0)drawTimingMeter(g,pitchTravel);
        }
        if(catchEligible()){
            GlovePose glove=glovePose(fieldingFielderIndex);WorldPoint p=parkCamera.project(glove.ground(),glove.height());
            parkRenderer.tag(g,p.x(),p.y()-22,catchRequested?"READY":"CATCH [P]");
        }
        if(inPlay)drawParkRadar(g);
        if(floatingTextLife>0&&!floatingText.isBlank()){
            g.setFont(Font.font("Segoe UI",FontWeight.BOLD,16));g.setFill(Color.rgb(23,33,26,.85));
            g.fillRoundRect(390,22,400,34,6,6);g.setFill(Color.web("#eff4dc"));
            g.fillText(floatingText.length()>40?floatingText.substring(0,40):floatingText,405,45);
        }
        if(paused){g.setFill(Color.rgb(15,25,20,.65));g.fillRect(0,0,1180,650);parkRenderer.tag(g,590,320,"PAUSED · ESC TO RESUME");}
        g.restore();
    }
    private final java.util.ArrayDeque<WorldPoint> parkTrail=new java.util.ArrayDeque<>();
    private void drawParkBallTrail(GraphicsContext g,WorldPoint p){
        parkTrail.addLast(p);while(parkTrail.size()>12)parkTrail.removeFirst();
        WorldPoint previous=null;int i=0;
        for(WorldPoint q:parkTrail){if(previous!=null){g.setStroke(Color.rgb(239,246,198,(double)i/parkTrail.size()*.7));g.setLineWidth(2);g.strokeLine(previous.x(),previous.y(),q.x(),q.y());}previous=q;i++;}
    }
    private void drawTimingMeter(GraphicsContext g,double progress){
        g.setFill(Color.rgb(28,39,29,.7));g.fillRoundRect(482,620,216,10,5,5);
        g.setFill(Color.web("#cadf85"));g.fillRect(482+216*.84,620,216*.13,10);
        g.setFill(Color.WHITE);g.fillOval(478+216*progress,617,8,16);
    }
    private void drawParkRadar(GraphicsContext g) {
        double x=1080,y=577,scale=.069;
        g.setFill(Color.rgb(20,34,31,.76));g.fillRoundRect(988,446,176,181,8,8);
        g.setStroke(Color.web("#bbc997"));g.setLineWidth(1);
        WorldPoint[] bases={FieldGeometry.HOME,FieldGeometry.FIRST,FieldGeometry.SECOND,FieldGeometry.THIRD};
        double[] xx=new double[4],yy=new double[4];for(int i=0;i<4;i++){xx[i]=x+(bases[i].x()-560)*scale;yy[i]=y+(bases[i].y()-610)*scale;}
        g.strokePolygon(xx,yy,4);
        for(WorldPoint f:fielderFeet){g.setFill(Color.web("#c5d99c"));g.fillOval(x+(f.x()-560)*scale-2,y+(f.y()-610)*scale-2,4,4);}
        for(Runner r:runnerController.runnersBackToFront()){g.setFill(Color.web("#eac36c"));g.fillOval(x+(r.feet().x()-560)*scale-2,y+(r.feet().y()-610)*scale-2,4,4);}
        Baseball b=liveBallController.ball();g.setFill(Color.WHITE);g.fillOval(clamp(x+(b.x()-560)*scale,998,1153)-3,clamp(y+(b.y()-610)*scale,460,611)-3,6,6);
    }

    private void drawLegacyGame() {
        GraphicsContext graphics = this.gameCanvas.getGraphicsContext2D();
        double width = this.gameCanvas.getWidth();
        double height = this.gameCanvas.getHeight();
        graphics.save();
        if (this.shakeStrength > 0.0 && this.phase != Phase.BATTING) {
            graphics.translate((this.random.nextDouble() - 0.5) * this.shakeStrength, (this.random.nextDouble() - 0.5) * this.shakeStrength);
        }
        graphics.setImageSmoothing(true);
        this.applyTrajectoryCamera(graphics, width, height);
        this.drawBackdrop(graphics, width, height);
        this.drawFieldGlow(graphics, width, height);
        this.drawBaseMarkers(graphics);
        this.drawAutomatedFielders(graphics);
        // Render the catcher first so the batter is always in front at home plate.
        this.drawCatcher(graphics, this.catcherMovement.x(), this.catcherMovement.y());
        
        if (this.fieldingActive) {
            this.drawFieldingChallenge(graphics);
        }
        if (this.phase == Phase.PITCHING) {
        double pitcherScale = this.playerScaleAt(new WorldPoint(this.pitcherX, this.pitcherY), 1.30);
        this.drawPitcherFieldingMascot(graphics, this.pitcherX, this.pitcherY, this.displayedPlayerMascot(), pitcherScale);
            this.drawRivalPlayer(graphics, 515.0, 456.0, true);
            double pitcherLabelY = Math.max(72.0, this.pitcherY - SPRITE_BOX_HEIGHT * pitcherScale - 8.0);
            this.drawRoleIndicator(graphics, "PITCHER", this.pitcherX, pitcherLabelY, Color.web((String)"#56f2e3"));
            this.drawRoleIndicator(graphics, "BATTER", 538.0, 454.0, Color.web((String)"#ffe066"));
            this.drawPlayerIndicator(graphics, this.displayedPlayerMascot(), "YOU", "PITCHER", this.pitcherX, Math.max(45.0, pitcherLabelY - 23.0), Color.web((String)"#56f2e3"));
        } else {
            double pitcherScale = this.playerScaleAt(new WorldPoint(this.pitcherX, this.pitcherY), 1.30);
            this.drawPitcherFieldingMascot(graphics, this.pitcherX, this.pitcherY, this.displayedRivalMascot(), pitcherScale);
            double batterScale = this.playerScaleAt(new WorldPoint(515.0 + this.visualBatOffsetX * 0.28, 456.0), 1.18);
            this.drawMascotPlayer(graphics, 515.0 + this.visualBatOffsetX * 0.28, 456.0, this.displayedPlayerMascot(), true, batterScale);
            double pitcherLabelY = Math.max(72.0, this.pitcherY - SPRITE_BOX_HEIGHT * pitcherScale - 8.0);
            this.drawRoleIndicator(graphics, "PITCHER", this.pitcherX, pitcherLabelY, Color.web((String)"#ff8a66"));
            this.drawRoleIndicator(graphics, "BATTER", 538.0 + this.visualBatOffsetX * 0.28, 454.0, Color.web((String)"#ffe066"));
            this.drawPlayerIndicator(graphics, this.displayedPlayerMascot(), "YOU", "BATTER", 538.0 + this.visualBatOffsetX * 0.28, 426.0, Color.web((String)"#56f2e3"));
        }
        if (this.pitcherFieldingStage == PitcherFieldingStage.PICKUP
                || this.pitcherFieldingStage == PitcherFieldingStage.HOLD
                || this.pitcherFieldingStage == PitcherFieldingStage.THROW
                || (this.pitchAnimation > 0.27 && this.pitchActive)) {
            double heldScale = this.playerScaleAt(new WorldPoint(this.pitcherX, this.pitcherY), 1.30);
            double heldWidth = SPRITE_BOX_WIDTH * heldScale;
            double heldHeight = SPRITE_BOX_HEIGHT * heldScale;
            this.drawHeldBall(graphics, this.pitcherX + heldWidth * 0.16, this.pitcherY - heldHeight * 0.54);
        }
        if (GameSettings.isStrikeZoneVisible()) {
            this.drawAimReticle(graphics);
        }
        if (this.pitchActive) {
            this.drawBallAndTrail(graphics);
        }
        this.drawSmoothRunners(graphics);
        if (!this.pitchActive && this.liveBallController.ball().active()) {
            this.drawControlledBall(graphics);
        }
        if (this.contactFlash > 0.0) {
            this.drawContactFlash(graphics);
        }
        this.drawParticles(graphics);
        this.drawFloatingText(graphics, width);
        this.drawTrajectoryScene(graphics, width);
        graphics.restore();
        if (this.paused) {
            graphics.setFill((Paint)Color.rgb((int)5, (int)8, (int)24, (double)0.82));
            graphics.fillRect(0.0, 0.0, width, height);
            graphics.setFont(Font.font((String)"Arial Rounded MT Bold", (FontWeight)FontWeight.EXTRA_BOLD, (double)46.0));
            graphics.setFill((Paint)Color.WHITE);
            graphics.fillText("PAUSED", width / 2.0 - 95.0, height / 2.0);
        }
    }

    private void drawBackdrop(GraphicsContext graphics, double width, double height) {
        if (this.stadiumBackground != null && !this.stadiumBackground.isError()) {
            this.drawCoverImage(graphics, this.stadiumBackground, width, height);
            graphics.setFill((Paint)Color.rgb((int)8, (int)14, (int)42, (double)0.18));
            graphics.fillRect(0.0, 0.0, width, height);
            return;
        }
        graphics.setFill((Paint)Color.web((String)"#12285b"));
        graphics.fillRect(0.0, 0.0, width, 170.0);
        graphics.setFill((Paint)Color.web((String)"#10213b"));
        graphics.fillRect(0.0, 105.0, width, 130.0);
        Color[] crowd = new Color[]{Color.web((String)"#ffe066"), Color.web((String)"#ff5b35"), Color.web((String)"#56f2e3"), Color.web((String)"#ff71ce"), Color.WHITE};
        int row = 0;
        while (row < 8) {
            int column = 0;
            while (column < 58) {
                graphics.setFill((Paint)crowd[(row * 2 + column) % crowd.length]);
                graphics.fillRect((double)(7 + column * 19), (double)(110 + row * 13), 7.0, 7.0);
                ++column;
            }
            ++row;
        }
        graphics.setFill((Paint)Color.web((String)"#2c9b52"));
        graphics.fillRect(0.0, 220.0, width, height - 220.0);
    }

    private void drawFieldGlow(GraphicsContext graphics, double width, double height) {
        graphics.save();
        graphics.setFill((Paint)Color.rgb((int)6, (int)10, (int)28, (double)0.26));
        graphics.fillRect(0.0, 0.0, width, 64.0);
        graphics.fillRect(0.0, height - 20.0, width, 20.0);
        graphics.restore();
    }

    private void drawAimReticle(GraphicsContext graphics) {
        double x = this.phase == Phase.BATTING ? 560.0 + this.visualBatOffsetX : this.visualAimY;
        Color color = this.isInsideStrikeZone(this.visualAimY) ? Color.web((String)"#56f2e3") : Color.web((String)"#ff6b6b");
        double pulse = Math.sin(this.animationClock * 7.0) * 1.2;
        graphics.save();
        graphics.setGlobalAlpha(0.55);
        graphics.setStroke((Paint)color);
        graphics.setLineWidth(1.5);
        graphics.strokeOval(x - 6.0 - pulse / 2.0, 542.0 - pulse / 2.0, 12.0 + pulse, 12.0 + pulse);
        graphics.restore();
    }

    private void drawRoleIndicator(GraphicsContext graphics, String role, double centerX, double feetY, Color color) {
        graphics.save();
        graphics.setFont(Font.font((String)"Arial Rounded MT Bold", (FontWeight)FontWeight.EXTRA_BOLD, (double)10.0));
        double width = (double)role.length() * 7.2 + 16.0;
        graphics.setFill((Paint)Color.rgb((int)5, (int)10, (int)28, (double)0.76));
        graphics.fillRoundRect(centerX - width / 2.0, feetY - 15.0, width, 17.0, 9.0, 9.0);
        graphics.setStroke((Paint)color);
        graphics.setLineWidth(1.0);
        graphics.strokeRoundRect(centerX - width / 2.0, feetY - 15.0, width, 17.0, 9.0, 9.0);
        graphics.setFill((Paint)color);
        graphics.fillText(role, centerX - (double)role.length() * 3.6, feetY - 3.0);
        graphics.restore();
    }

    private void drawTrajectoryScene(GraphicsContext graphics, double width) {
        CameraTransitionController.View view = this.cameraTransitionController.currentView();
        if (view == CameraTransitionController.View.BATTING
                || view == CameraTransitionController.View.HIGH_ISOMETRIC) return;
        String title = switch (view) {
            case GROUND_BALL -> "GROUND BALL • INFIELD";
            case LOW_LINE_DRIVE -> "LINE DRIVE • CENTER";
            case HIGH_FLY_BALL -> "HIGH FLY BALL • OUTFIELD";
            case LEFT_FIELD -> "LEFT FIELD";
            case RIGHT_FIELD -> "RIGHT FIELD";
            case DEEP_HOME_RUN -> "DEEP BALL • HOME RUN";
            default -> "BALL TRACKING";
        };
        graphics.save();
        graphics.setFill(Color.rgb(5, 10, 28, 0.82));
        graphics.fillRoundRect(width / 2.0 - 190.0, 22.0, 380.0, 30.0, 15.0, 15.0);
        graphics.setStroke(Color.web("#56f2e3"));
        graphics.setLineWidth(1.4);
        graphics.strokeRoundRect(width / 2.0 - 190.0, 22.0, 380.0, 30.0, 15.0, 15.0);
        graphics.setFill(Color.web("#d9fffa"));
        graphics.setFont(Font.font("Arial Rounded MT Bold", FontWeight.EXTRA_BOLD, 13.0));
        graphics.fillText(title, width / 2.0 - title.length() * 3.7, 42.0);
        graphics.restore();
    }

    private void applyTrajectoryCamera(GraphicsContext graphics, double width, double height) {
        CameraTransitionController camera = this.cameraTransitionController;
        CameraTransitionController.View from = camera.fromView();
        CameraTransitionController.View to = camera.targetView();
        double blend = camera.transitionProgress();
        WorldPoint fromFocus = this.cameraFocus(from);
        WorldPoint toFocus = this.cameraFocus(to);
        double focusX = fromFocus.x() + (toFocus.x() - fromFocus.x()) * blend;
        double focusY = fromFocus.y() + (toFocus.y() - fromFocus.y()) * blend;
        double zoom = this.cameraZoom(from) + (this.cameraZoom(to) - this.cameraZoom(from)) * blend;
        // A tiny, eased camera handoff keeps the batter view readable and then
        // follows the live ball toward the selected field sector.
        if (Math.abs(zoom - 1.0) < 0.001 && Math.abs(focusX - width / 2.0) < 0.5
                && Math.abs(focusY - height / 2.0) < 0.5) return;
        graphics.translate(width / 2.0, height / 2.0);
        graphics.scale(zoom, zoom);
        graphics.translate(-focusX, -focusY);
    }

    private WorldPoint cameraFocus(CameraTransitionController.View view) {
        return switch (view) {
            case GROUND_BALL -> new WorldPoint(560.0, 430.0);
            case LOW_LINE_DRIVE -> new WorldPoint(560.0, 395.0);
            case HIGH_FLY_BALL -> new WorldPoint(560.0, 305.0);
            case LEFT_FIELD -> new WorldPoint(260.0, 370.0);
            case RIGHT_FIELD -> new WorldPoint(900.0, 370.0);
            case DEEP_HOME_RUN -> new WorldPoint(590.0, 255.0);
            case HIGH_ISOMETRIC -> new WorldPoint(590.0, 360.0);
            case BATTING -> new WorldPoint(590.0, 325.0);
        };
    }

    private double cameraZoom(CameraTransitionController.View view) {
        return switch (view) {
            case GROUND_BALL, LEFT_FIELD, RIGHT_FIELD -> 1.08;
            case LOW_LINE_DRIVE -> 1.04;
            case HIGH_FLY_BALL -> 0.96;
            case DEEP_HOME_RUN -> 0.92;
            default -> 1.0;
        };
    }
    private void drawPlayerIndicator(GraphicsContext graphics, Mascot mascot, String prefix, String role,
                                     double centerX, double feetY, Color color) {
        String text = prefix + " • " + mascot.getDisplayName().toUpperCase() + " • " + role;
        graphics.save();
        graphics.setFont(Font.font("Arial Rounded MT Bold", FontWeight.EXTRA_BOLD, 11.0));
        double width = text.length() * 6.2 + 20.0;
        graphics.setFill(Color.rgb(5, 10, 28, 0.88));
        graphics.fillRoundRect(centerX - width / 2.0, feetY - 17.0, width, 19.0, 10.0, 10.0);
        graphics.setStroke(color);
        graphics.setLineWidth(1.5);
        graphics.strokeRoundRect(centerX - width / 2.0, feetY - 17.0, width, 19.0, 10.0, 10.0);
        graphics.setFill(color);
        graphics.fillText(text, centerX - width / 2.0 + 10.0, feetY - 4.0);
        graphics.restore();
    }

    private void drawSwingLane(GraphicsContext graphics) {
        if (this.phase != Phase.BATTING) {
            return;
        }
        graphics.save();
        graphics.setFill((Paint)Color.rgb((int)86, (int)242, (int)227, (double)0.08));
        graphics.fillRoundRect(482.0, 500.0, 156.0, 96.0, 18.0, 18.0);
        graphics.setStroke((Paint)Color.rgb((int)86, (int)242, (int)227, (double)0.42));
        graphics.setLineWidth(2.0);
        graphics.setLineDashes(new double[]{8.0, 7.0});
        graphics.strokeRoundRect(482.0, 500.0, 156.0, 96.0, 18.0, 18.0);
        graphics.setLineDashes(new double[0]);
        graphics.setFill((Paint)Color.rgb((int)255, (int)255, (int)255, (double)0.75));
        graphics.setFont(Font.font((String)"Arial Rounded MT Bold", (FontWeight)FontWeight.BOLD, (double)11.0));
        graphics.fillText("SWING WINDOW", 490.0, 493.0);
        graphics.restore();
    }

    private void drawBallAndTrail(GraphicsContext graphics) {
        double progress = this.pitchProgress(this.ballX);
        int step = 4;
        while (step >= 1) {
            double trailProgress = Math.max(0.0, progress - (double)step * 0.022);
            double trailX = this.trajectoryY(trailProgress, this.activePitch, this.pitchTargetY);
            double trailY = 273.0 + 275.0 * trailProgress;
            graphics.setFill((Paint)Color.rgb((int)255, (int)240, (int)170, (double)(0.11 * (double)(5 - step))));
            double size = 6 + (5 - step) * 2;
            graphics.fillOval(trailX - size / 2.0, trailY - size / 2.0, size, size);
            --step;
        }
        graphics.setFill((Paint)Color.WHITE);
        graphics.fillOval(this.ballX - BALL_DIAMETER/2, this.ballY - BALL_DIAMETER/2, BALL_DIAMETER, BALL_DIAMETER);
        graphics.setStroke((Paint)Color.web((String)"#ff5b35"));
        graphics.setLineWidth(2.0);
        graphics.strokeArc(this.ballX - 6.0, this.ballY - 6.0, 12.0, 12.0, 25.0, 100.0, ArcType.OPEN);
        graphics.strokeArc(this.ballX - 6.0, this.ballY - 6.0, 12.0, 12.0, 205.0, 100.0, ArcType.OPEN);
    }

    private void drawMascotPlayer(GraphicsContext graphics, double x, double y, Mascot mascot, boolean batting, double scale) {
        if (mascot == Mascot.TURBO_TANUKI && this.hasTurboTanukiSpriteSheet()) {
            MascotRenderController.Pose pose = batting ? (this.swingAnimation > 0.0 ? MascotRenderController.Pose.CONTACT : MascotRenderController.Pose.BATTING_STANCE) : (this.pitchAnimation > 0.27 ? MascotRenderController.Pose.PITCH_WINDUP : (this.pitchAnimation > 0.0 ? MascotRenderController.Pose.PITCH_RELEASE : MascotRenderController.Pose.READY));
            this.drawTurboTanukiFrame(graphics, x, y, pose, scale, batting);
        } else if (this.hasGameplaySpriteSheet() && mascot.ordinal() < 12) {
            this.drawSpriteMascotPlayer(graphics, x, y, mascot, batting, scale);
        } else {
            this.drawVectorMascotPlayer(graphics, x, y, mascot, batting, scale);
        }
    }

    private void drawSpriteMascotPlayer(GraphicsContext graphics, double x, double y, Mascot mascot, boolean batting, double scale) {
        double bob = 0.0;
        double displayWidth = 160.0 * scale;
        double displayHeight = 192.0 * scale;
        SpriteAction action = batting ? (this.swingAnimation > 0.0 ? SpriteAction.SWING : SpriteAction.IDLE) : (this.pitchAnimation > 0.0 ? SpriteAction.PITCH : SpriteAction.IDLE);
        graphics.save();
        graphics.setFill((Paint)Color.rgb((int)0, (int)0, (int)0, (double)0.34));
        graphics.fillOval(x + displayWidth * 0.12, y + bob + displayHeight * 0.82, displayWidth * 0.76, displayHeight * 0.08);
        graphics.restore();
        boolean facesRight = batting;
        graphics.save();
        graphics.translate(x + (facesRight ? 0.0 : displayWidth), y + bob);
        graphics.scale(facesRight ? scale : -scale, scale);
        double sourceX = (double)action.column * 320.0;
        double sourceY = (double)mascot.ordinal() * 384.0;
        graphics.drawImage(this.gameplaySprites, sourceX, sourceY, 320.0, 384.0, 0.0, 0.0, 160.0, 192.0);
        if (batting) {
            this.drawSpriteBat(graphics, mascot);
        } else {
            this.drawSpritePitchMotion(graphics, mascot);
        }
        graphics.restore();
    }

    private void drawSpriteBat(GraphicsContext graphics, Mascot mascot) {
        double swingStrength = this.swingAnimation > 0.0 ? GameController.clamp(this.swingAnimation / 0.16, 0.0, 1.0) : 0.0;
        double eased = Math.pow(swingStrength, 0.34);
        // The action atlas does not contain a reliable bat layer. Draw one over
        // every batting pose so the mascot visibly holds it while waiting,
        // swinging, making contact, and following through.
        double angle = Math.toRadians(-58.0 + eased * 105.0);
        double handX = 78.0;
        double handY = 76.0;
        double batLength = 65.0;
        double batEndX = handX + Math.cos(angle) * batLength;
        double batEndY = handY + Math.sin(angle) * batLength;
        graphics.save();
        graphics.setStroke(Color.web("#ffe066"));
        graphics.setLineWidth(7.0);
        graphics.strokeLine(handX, handY, batEndX, batEndY);
        graphics.setStroke(Color.web("#fff4b8"));
        graphics.setLineWidth(2.0);
        graphics.strokeLine(handX, handY, batEndX, batEndY);
        graphics.setFill(Color.web(mascot.getAccentColor()));
        graphics.fillOval(handX - 7.0, handY - 7.0, 14.0, 14.0);
        graphics.restore();
        if (this.swingAnimation > 0.0) {
            graphics.save();
            graphics.setGlobalAlpha(0.72 * swingStrength);
            graphics.setStroke((Paint)Color.WHITE);
            graphics.setLineWidth(4.0);
            graphics.strokeArc(18.0, 21.0, 112.0, 112.0, 198.0 - eased * 72.0, 52.0, ArcType.OPEN);
            graphics.setGlobalAlpha(0.34 * swingStrength);
            graphics.strokeArc(10.0, 13.0, 128.0, 128.0, 203.0 - eased * 72.0, 44.0, ArcType.OPEN);
            graphics.restore();
        }
    }

    private void drawSpritePitchMotion(GraphicsContext graphics, Mascot mascot) {
        if (this.pitchAnimation <= 0.0) {
            return;
        }
        if (this.pitchAnimation > 0.0) {
            graphics.save();
            graphics.setGlobalAlpha(0.48);
            graphics.setStroke((Paint)Color.WHITE);
            graphics.setLineWidth(3.0);
            graphics.strokeLine(73.0, 53.0, 96.0, 43.0);
            graphics.strokeLine(77.0, 66.0, 103.0, 62.0);
            graphics.strokeLine(76.0, 79.0, 100.0, 82.0);
            graphics.restore();
        }
    }

    private void drawTurboTanukiFrame(GraphicsContext graphics, double x, double y, MascotRenderController.Pose pose, double scale, boolean facesRight) {
        MascotRenderController.Frame frame = this.mascotRenderController.frame(pose);
        double sourceWidth = this.turboTanukiSprites.getWidth() / 4.0;
        double sourceHeight = this.turboTanukiSprites.getHeight() / 4.0;
        double width = SPRITE_BOX_WIDTH * scale;
        double height = SPRITE_BOX_HEIGHT * scale;
        graphics.save();
        graphics.setFill((Paint)Color.rgb((int)0, (int)0, (int)0, (double)0.3));
        graphics.fillOval(x + width * 0.1, y + height * 0.85, width * 0.72, height * 0.085);
        graphics.translate(x + (facesRight ? 0.0 : width), y);
        graphics.scale((double)(facesRight ? 1 : -1), 1.0);
        graphics.drawImage(this.turboTanukiSprites, (double)frame.column() * sourceWidth, (double)frame.row() * sourceHeight, sourceWidth, sourceHeight, 0.0, 0.0, width, height);
        graphics.restore();
    }

    private void drawVectorMascotPlayer(GraphicsContext graphics, double x, double y, Mascot mascot, boolean batting, double scale) {
        graphics.save();
        double bob = 0.0;
        graphics.translate(x, y + bob);
        graphics.scale(scale, scale);
        Color primary = Color.web((String)mascot.getPrimaryColor());
        Color accent = Color.web((String)mascot.getAccentColor());
        graphics.setFill((Paint)Color.rgb((int)0, (int)0, (int)0, (double)0.3));
        graphics.fillOval(-18.0, 82.0, 82.0, 18.0);
        graphics.setFill((Paint)primary);
        graphics.fillRoundRect(0.0, 25.0, 48.0, 55.0, 18.0, 18.0);
        graphics.setFill((Paint)accent);
        graphics.fillRect(0.0, 51.0, 48.0, 9.0);
        double footMotion = Math.sin(this.animationClock * 9.5) * (batting ? 2.5 : 1.5);
        graphics.setFill((Paint)Color.web((String)"#183153"));
        graphics.fillRoundRect(4.0 - footMotion, 75.0, 15.0, 27.0, 8.0, 8.0);
        graphics.fillRoundRect(30.0 + footMotion, 75.0, 15.0, 27.0, 8.0, 8.0);
        this.drawMascotHead(graphics, mascot, 24.0, 14.0, primary, accent);
        if (batting) {
            double swingStrength = this.swingAnimation > 0.0 ? GameController.clamp(this.swingAnimation / 0.16, 0.0, 1.0) : 0.0;
            double eased = Math.pow(swingStrength, 0.34);
            double angle = Math.toRadians(-130.0 + eased * 70.0);
            double handX = 37.0;
            double handY = 40.0;
            double batEndX = handX + Math.cos(angle) * 78.0;
            double batEndY = handY + Math.sin(angle) * 78.0;
            if (this.swingAnimation > 0.0) {
                graphics.save();
                graphics.setGlobalAlpha(0.58);
                graphics.setStroke((Paint)Color.web((String)"#ffffff"));
                graphics.setLineWidth(4.0);
                graphics.strokeArc(-38.0, -39.0, 128.0, 128.0, 197.0 - eased * 185.0, 55.0, ArcType.OPEN);
                graphics.setGlobalAlpha(0.34);
                graphics.strokeArc(-47.0, -48.0, 146.0, 146.0, 203.0 - eased * 185.0, 42.0, ArcType.OPEN);
                graphics.restore();
            }
            graphics.setStroke((Paint)Color.web((String)"#ffe066"));
            graphics.setLineWidth(8.0);
            graphics.strokeLine(handX, handY, batEndX, batEndY);
            graphics.setFill((Paint)accent);
            graphics.fillOval(31.0, 33.0, 18.0, 18.0);
        } else {
            double progress = this.pitchAnimation > 0.0 ? 1.0 - this.pitchAnimation / 0.54 : 0.0;
            double eased = Math.sin(GameController.clamp(progress, 0.0, 1.0) * Math.PI / 2.0);
            double angle = Math.toRadians(-68.0 + eased * 145.0);
            double armEndX = 39.0 + Math.cos(angle) * 47.0;
            double armEndY = 42.0 + Math.sin(angle) * 47.0;
            if (this.pitchAnimation > 0.0) {
                graphics.save();
                graphics.setGlobalAlpha(0.45);
                graphics.setStroke((Paint)Color.WHITE);
                graphics.setLineWidth(3.0);
                graphics.strokeLine(63.0, 2.0, 83.0, -8.0);
                graphics.strokeLine(67.0, 15.0, 91.0, 10.0);
                graphics.strokeLine(64.0, 28.0, 86.0, 30.0);
                graphics.restore();
            }
            graphics.setFill((Paint)Color.web((String)"#8a3d2f"));
            graphics.fillOval(-13.0, 39.0, 24.0, 22.0);
            graphics.setStroke((Paint)accent);
            graphics.setLineWidth(7.0);
            graphics.strokeLine(39.0, 42.0, armEndX, armEndY);
        }
        graphics.restore();
    }

    private void drawControlledBall(GraphicsContext graphics) {
        WorldPoint shadow = this.liveBallController.shadowPosition();
        WorldPoint position = this.liveBallController.screenPosition();
        double height = this.liveBallController.ball().height();
        double scale = this.liveBallController.perspectiveScale();
        double size = BALL_DIAMETER / 2.0;
        graphics.save();
        double shadowWidth = Math.max(5.0, 18.0 * scale - height * 0.025);
        graphics.setFill((Paint)Color.rgb((int)0, (int)0, (int)0, (double)Math.max(0.08, 0.34 - height * 7.0E-4)));
        graphics.fillOval(shadow.x() - shadowWidth / 2.0, shadow.y() - 3.0, shadowWidth, 6.0);
        double velocity = Math.hypot(this.liveBallController.ball().velocityX(), this.liveBallController.ball().velocityY());
        if (velocity > 45.0) {
            double unitX = this.liveBallController.ball().velocityX() / velocity;
            double unitY = (this.liveBallController.ball().velocityY() - this.liveBallController.ball().velocityHeight()) / velocity;
            graphics.setStroke((Paint)Color.rgb((int)220, (int)248, (int)255, (double)0.5));
            graphics.setLineWidth(Math.max(2.0, size * 0.3));
            graphics.strokeLine(position.x() - unitX * 26.0, position.y() - unitY * 20.0, position.x() - unitX * 6.0, position.y() - unitY * 5.0);
        }
        graphics.setFill((Paint)Color.rgb((int)70, (int)190, (int)255, (double)0.18));
        graphics.fillOval(position.x() - size, position.y() - size, size * 2.0, size * 2.0);
        graphics.setFill((Paint)Color.WHITE);
        graphics.fillOval(position.x() - size / 2.0, position.y() - size / 2.0, size, size);
        graphics.setStroke((Paint)Color.web((String)"#e84b4b"));
        graphics.setLineWidth(Math.max(1.0, scale * 1.5));
        graphics.strokeArc(position.x() - size * 0.38, position.y() - size * 0.38, size * 0.76, size * 0.76, 25.0, 100.0, ArcType.OPEN);
        graphics.restore();
    }

    private void drawHeldBall(GraphicsContext graphics, double x, double y) {
        graphics.save();
        graphics.setFill((Paint)Color.rgb((int)0, (int)0, (int)0, (double)0.28));
        graphics.fillOval(x - 8.0, y + 7.0, 16.0, 5.0);
        graphics.setFill((Paint)Color.WHITE);
        graphics.fillOval(x - 7.0, y - 7.0, 14.0, 14.0);
        graphics.setStroke((Paint)Color.web((String)"#d94343"));
        graphics.setLineWidth(1.4);
        graphics.strokeArc(x - 5.0, y - 5.0, 10.0, 10.0, 25.0, 110.0, ArcType.OPEN);
        graphics.strokeArc(x - 5.0, y - 5.0, 10.0, 10.0, 205.0, 110.0, ArcType.OPEN);
        graphics.restore();
    }

    private void drawSmoothRunners(GraphicsContext graphics) {
        for (Runner runner : this.runnerController.runnersBackToFront()) {
            WorldPoint feet = runner.feet();
            double depthScale = this.normalizedFieldDepth(feet);
            double width = ON_FIELD_MASCOT_WIDTH * depthScale;
            double height = ON_FIELD_MASCOT_HEIGHT * depthScale;
            double bob = 0.0;
            graphics.save();
            graphics.setFill((Paint)Color.rgb((int)0, (int)0, (int)0, (double)0.24));
            graphics.fillOval(feet.x() - width * 0.32, feet.y() - height * 0.035, width * 0.64, height * 0.09);
            this.drawActionSprite(graphics, runner.mascot(), runner.isRunning() ? SpriteAction.RUN : SpriteAction.IDLE, feet.x() - width / 2.0, feet.y() - height - bob, width, height, runner.facingRight(), 0.0);
            graphics.restore();
        }
    }

    private void drawBaseMarkers(GraphicsContext graphics) {
        double[][] bases = new double[][]{{560.0, 610.0}, {900.0, 450.0}, {560.0, 390.0}, {220.0, 450.0}};
        graphics.save();
        int index = 0;
        while (index < bases.length) {
            boolean occupied = index > 0 && this.runnerController.isOccupied(index);
            if (occupied) {
                double pulse = 0.5 + Math.sin(this.animationClock * 5.0 + (double)index) * 0.12;
                graphics.setFill((Paint)Color.rgb((int)86, (int)242, (int)227, (double)(pulse * 0.3)));
                graphics.fillOval(bases[index][0] - 11.0, bases[index][1] - 7.0, 22.0, 14.0);
                graphics.setStroke((Paint)Color.rgb((int)220, (int)255, (int)251, (double)0.7));
                graphics.setLineWidth(1.2);
                graphics.strokeOval(bases[index][0] - 8.0, bases[index][1] - 5.0, 16.0, 10.0);
            }
            ++index;
        }
        graphics.restore();
    }

    private void drawAutomatedFielders(GraphicsContext graphics) {
        Mascot[] lineup = Mascot.values();
        WorldPoint ballShadow = this.liveBallController.shadowPosition();
        double targetX = GameController.clamp(ballShadow.x(), 120.0, 1000.0);
        double[][] anchors = fieldingAnchors();
        int index = 0;
        while (index < this.fielderFeet.length) {
            double x = this.fielderFeet[index].x();
            double y = this.fielderFeet[index].y();
            int defensiveCaptain = this.phase == Phase.PITCHING ? this.playerMascot().ordinal() : this.rivalMascot().ordinal();
            Mascot fielder = lineup[(defensiveCaptain + index + 1) % lineup.length];
            boolean facesRight = this.fielderFacing[index];
            double depthScale = this.normalizedFieldDepth(this.fielderFeet[index]);
            double width = ON_FIELD_MASCOT_WIDTH * depthScale;
            double height = ON_FIELD_MASCOT_HEIGHT * depthScale;
            boolean moving = this.fielderMoving[index];
            graphics.setFill((Paint)Color.rgb((int)0, (int)0, (int)0, (double)0.22));
            graphics.fillOval(x - width * 0.34, y - height * 0.03, width * 0.68, Math.max(4.0, height * 0.08));
            this.drawActionSprite(graphics, fielder, moving ? SpriteAction.RUN : SpriteAction.IDLE, x - width / 2.0, y - height, width, height, facesRight, 0.0);
            GlovePose pose = this.glovePose(index);
            WorldPoint hand = pose.screen();
            if (index == this.heldFielder) this.drawHeldBall(graphics,hand.x(),hand.y());
            ++index;
        }
    }

    private void updateFielderFeet(double elapsed) {
        double[][] anchors=fieldingAnchors();
        boolean chasing=fieldingActive&&liveBallController.ball().active()&&!pitchActive;
        WorldPoint ball=liveBallController.shadowPosition();
        int nearest=fieldingFielderIndex,backup=-1;
        if(chasing&&nearest<0){assignLiveFielder();nearest=fieldingFielderIndex;}
        if(chasing)backup=defensiveAssignmentController.assign(List.of(fielderFeet),liveBallController.ball()).backupIndex();
        for(int i=0;i<fielderFeet.length;i++){
            if(i==heldFielder||i==groundHolder){fielderMoving[i]=false;continue;}
            WorldPoint target=new WorldPoint(anchors[i][0],anchors[i][1]);
            if(i==baseReceiver)target=FieldGeometry.FIRST;
            else if(chasing&&i==nearest){
                WorldPoint intercept=liveBallController.bounced()?ball:CatchGeometry.interceptionTarget(liveBallController.ball(),glovePose(i).height());
                double offset=glovePose(i).ground().x()-fielderFeet[i].x();
                target=boundField(new WorldPoint(intercept.x()-offset,intercept.y()));
            } else if(chasing&&i==backup&&nearest>=0)target=boundField(FielderAllocation.backupTarget(ball,fielderFeet[nearest],ball.x()<560));
            WorldPoint before=fielderFeet[i];
            int captain=phase==Phase.PITCHING?playerMascot().ordinal():rivalMascot().ordinal();
            Mascot fielder=Mascot.values()[(captain+i+1)%Mascot.values().length];
            if(!chasing||reactionRemaining==0)fielderFeet[i]=groundMovementController.approach(before,target,92*fielder.getSpeedMultiplier(),elapsed);
            double distance=before.distanceTo(fielderFeet[i]);fielderMoving[i]=distance>.001;
            fielderRunClock[i]+=distance/90;
            if(distance>.001)fielderFacing[i]=fielderFeet[i].x()>=before.x();
            fielderReach[i]=0;
        }
    }

    private double normalizedFieldDepth(WorldPoint feet) {
        double perspective = this.runnerController.path().perspectiveScale(feet);
        return GameController.clamp(perspective / 0.86, 0.82, 1.16);
    }

    private void updatePitcherPosition(double elapsed) {
        if (this.pitcherFieldingStage == PitcherFieldingStage.REACTION
                || this.pitcherFieldingStage == PitcherFieldingStage.CHASE
                || this.pitcherFieldingStage == PitcherFieldingStage.PICKUP
                || this.pitcherFieldingStage == PitcherFieldingStage.HOLD
                || this.pitcherFieldingStage == PitcherFieldingStage.THROW) {
            return;
        }
        double factor = Math.min(1.0, Math.max(0.0, elapsed * 5.5));
        this.pitcherX += (this.pitcherTargetX - this.pitcherX) * factor;
        this.pitcherY += (this.pitcherTargetY - this.pitcherY) * factor;
    }

    private boolean isPitcherGrounder() { return Math.abs(visualBatOffsetX)<2&&pitchAimHeight<8; }

    private void startPitcherFieldingSequence(double power,HitResult result,Mascot batter) {
        pitcherFieldingResult=result;pitcherFieldingBatter=batter;pitcherFieldingStage=PitcherFieldingStage.REACTION;
        pitcherFieldingTimer=.35;pitcherChaseSeconds=0;playOutcomeRecorded=false;runnerController.startLivePlay(batter);
        playStateController.transition(BaseballPlayStateController.State.BALL_IN_PLAY);
        liveBallController.launchGroundBall(ballX,ballY,pitchHeight,0,-(115+power*45),result);
        playStateController.transition(BaseballPlayStateController.State.FIELDING);
        chooseBaseReceiver(-1);statusLabel.setText("Ground ball · pitcher reacting");
    }

    /** Seconds spent reacting to and chasing the ball on the current play. */
    private double pitcherChaseSeconds;
    private static final double PITCHER_CHASE_LIMIT=6;
    /** Someone else has the ball, or the play has already been settled. */
    private boolean pitcherChasePreempted() {
        return heldFielder>=0||groundHolder>=0||defensiveThrowActive||playOutcomeRecorded
            ||!runnerController.isLivePlay()
            ||liveBallController.state()==LiveBallController.State.CAUGHT;
    }
    /** Drop the chase without adjudicating anything and walk back to the mound. */
    private void abandonPitcherFielding() {
        pitcherFieldingStage=PitcherFieldingStage.NONE;pitcherChaseSeconds=0;
        pitcherTargetX=560;pitcherTargetY=368;
    }
    private void updatePitcherFieldingSequence(double elapsed) {
        if(pitcherFieldingStage==PitcherFieldingStage.NONE)return;
        pitcherFieldingTimer=Math.max(0,pitcherFieldingTimer-elapsed);
        // The chase used to end only by reaching the ball. When a fielder caught
        // it first the pitcher went on chasing -- and a held ball travels with
        // whoever holds it -- so he trailed the catcher around the park in a
        // permanent run cycle, with updateGroundDefense blocked behind him for
        // as long as his stage was not NONE, so the play could not resolve.
        if(pitcherFieldingStage==PitcherFieldingStage.REACTION
                ||pitcherFieldingStage==PitcherFieldingStage.CHASE){
            pitcherChaseSeconds+=elapsed;
            if(pitcherChasePreempted()){abandonPitcherFielding();return;}
            // Nobody is going to reach it: call the batter safe rather than let
            // the pitcher chase a ball he cannot catch for the rest of the game.
            if(pitcherChaseSeconds>PITCHER_CHASE_LIMIT
                    ||liveBallController.state()==LiveBallController.State.DEAD_BALL){
                finishPitcherFieldingSequence(false);return;
            }
        } else pitcherChaseSeconds=0;
        switch(pitcherFieldingStage){
            case REACTION->{if(pitcherFieldingTimer==0)pitcherFieldingStage=PitcherFieldingStage.CHASE;}
            case CHASE->{
                Baseball b=liveBallController.ball();WorldPoint target=boundField(new WorldPoint(b.x(),b.y()));
                WorldPoint before=new WorldPoint(pitcherX,pitcherY);
                WorldPoint next=groundMovementController.approach(before,target,92*pitcherMascot().getSpeedMultiplier(),elapsed);
                pitcherRunClock+=before.distanceTo(next)/90;pitcherX=next.x();pitcherY=next.y();
                if(next.distanceTo(new WorldPoint(b.x(),b.y()))<=5&&b.height()<=5){
                    pitcherFieldingStage=PitcherFieldingStage.PICKUP;pitcherFieldingTimer=.45;
                    liveBallController.field(pitcherMascot().name(),new WorldPoint(b.x(),b.y()),b.height());
                    statusLabel.setText("Pitcher picks up the ball");
                }
            }
            case PICKUP->{
                WorldPoint hand=ParkSceneRenderer.gloveGround(new WorldPoint(pitcherX,pitcherY),true);
                liveBallController.moveHeldBall(hand,12*(1-pitcherFieldingTimer/.45));
                if(pitcherFieldingTimer==0){pitcherFieldingStage=PitcherFieldingStage.HOLD;pitcherFieldingTimer=.6;statusLabel.setText("Pitcher holds · setting the throw");}
            }
            case HOLD->{
                if(pitcherFieldingTimer==0&&baseReceiver>=0&&fielderFeet[baseReceiver].distanceTo(FieldGeometry.FIRST)<8){
                    pitcherFieldingStage=PitcherFieldingStage.THROW;
                    GlovePose glove=glovePose(baseReceiver);
                    liveBallController.throwFromFielder(liveBallController.shadowPosition(),12,glove.ground(),glove.height(),300*pitcherMascot().getThrowingMultiplier());
                    statusLabel.setText("Throw to first · runner is moving");
                }
            }
            case THROW->{
                if(liveBallController.state()==LiveBallController.State.CAUGHT){
                    boolean out=!runnerController.batterReachedFirst()&&baseReceiver>=0&&fielderFeet[baseReceiver].distanceTo(FieldGeometry.FIRST)<5;
                    finishPitcherFieldingSequence(out);
                }
            }
            default->{}
        }
    }

    private boolean isPitcherThrowOut() {
        return liveBallController.state()==LiveBallController.State.CAUGHT&&!runnerController.batterReachedFirst()
            &&baseReceiver>=0&&fielderFeet[baseReceiver].distanceTo(FieldGeometry.FIRST)<5;
    }

    private void finishPitcherFieldingSequence(boolean out) {
        pitcherFieldingStage=PitcherFieldingStage.NONE;pitcherChaseSeconds=0;
        pitcherTargetX=560;pitcherTargetY=368;
        finishDefensivePlay(out);
    }

    private Mascot pitcherMascot() {
        return this.phase == Phase.PITCHING ? this.playerMascot() : this.rivalMascot();
    }

    private void tryAutomaticFieldingCatch() {
        if(!fieldingActive||!catchRequested||fieldingFielderIndex<0||liveBallController.bounced())return;
        Baseball ball=liveBallController.ball();if(liveBallController.state()!=LiveBallController.State.AIRBORNE)return;
        GlovePose glove=glovePose(fieldingFielderIndex);
        if(CatchGeometry.intersects(previousBallX,previousBallY,previousBallHeight,ball.x(),ball.y(),ball.height(),glove.ground(),glove.height(),liveCatchRadius())){
            heldFielder=fieldingFielderIndex;heldSeconds=.75;fieldingActive=false;catchRequested=false;
            liveBallController.caught(fieldingMascot().name(),glove.ground(),glove.height());
            runnerController.flyOut();statusLabel.setText(fieldingMascot().getDisplayName()+" secured the fly ball");
        }
    }
    private Mascot fieldingMascot() {
        int index = Math.max(0, Math.min(this.fielderFeet.length - 1, this.fieldingFielderIndex));
        int captain = this.phase == Phase.PITCHING ? this.playerMascot().ordinal() : this.rivalMascot().ordinal();
        return Mascot.values()[(captain + index + 1) % Mascot.values().length];
    }

    private double[][] fieldingAnchors() {
        return new double[][]{
            {345,365}, {430,300}, {690,300}, {775,365},
            {120,-220}, {560,-410}, {1000,-220}
        };
    }

    private void drawMascotCrowdPulse(GraphicsContext graphics, double width) {
        double energy = this.crowdController.energy();
        graphics.save();
        int index = 0;
        while (index < 34) {
            Mascot fan = Mascot.values()[index % Mascot.values().length];
            double x = 26.0 + (double)index * (width - 52.0) / 33.0;
            double y = (double)(112 + index % 3 * 12) + Math.sin(this.animationClock * (3.0 + energy * 5.0) + (double)index) * 5.0 * energy;
            graphics.setFill((Paint)Color.web((String)fan.getPrimaryColor(), (double)(0.4 + energy * 0.28)));
            graphics.fillOval(x - 4.0, y - 4.0, 8.0, 8.0);
            graphics.fillPolygon(new double[]{x - 4.0, x - 1.0, x + 1.0, x + 4.0}, new double[]{y - 2.0, y - 8.0, y - 8.0, y - 2.0}, 4);
            ++index;
        }
        graphics.restore();
    }

    private void drawFieldingChallenge(GraphicsContext graphics) {
        if (!this.catchEligible()) return;
        WorldPoint hand=this.glovePose(this.fieldingFielderIndex).screen();
        graphics.setFill(Color.web("#091c2b"));
        graphics.fillRoundRect(hand.x()+7,hand.y()-34,80,22,8,8);
        graphics.setStroke(Color.web("#56f2e3"));
        graphics.setLineWidth(1);
        graphics.strokeRoundRect(hand.x()+7,hand.y()-34,80,22,8,8);
        graphics.strokeLine(hand.x()+12,hand.y()-12,hand.x(),hand.y());
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font("Arial",FontWeight.BOLD,11));
        graphics.fillText(this.catchRequested?"READY":"CATCH [P]",hand.x()+15,hand.y()-19);
    }
    private void drawActionSprite(GraphicsContext graphics, Mascot mascot, SpriteAction action, double x, double y, double width, double height, boolean facesRight, double rotation) {
        if (mascot == Mascot.TURBO_TANUKI && this.hasTurboTanukiSpriteSheet()) {
            MascotRenderController.Pose pose = switch (action) {
                case SpriteAction.RUN -> MascotRenderController.Pose.RUNNING;
                case SpriteAction.SWING -> MascotRenderController.Pose.CONTACT;
                case SpriteAction.PITCH -> MascotRenderController.Pose.PITCH_RELEASE;
                default -> MascotRenderController.Pose.READY;
            };
            MascotRenderController.Frame frame = this.mascotRenderController.frame(pose);
            double sourceWidth = this.turboTanukiSprites.getWidth() / 4.0;
            double sourceHeight = this.turboTanukiSprites.getHeight() / 4.0;
            graphics.save();
            graphics.translate(x + (facesRight ? 0.0 : width), y);
            graphics.rotate(rotation);
            graphics.scale((double)(facesRight ? 1 : -1), 1.0);
            graphics.drawImage(this.turboTanukiSprites, (double)frame.column() * sourceWidth, (double)frame.row() * sourceHeight, sourceWidth, sourceHeight, 0.0, 0.0, width, height);
            graphics.restore();
            return;
        }
        if (mascot.ordinal() >= 12 || !this.hasGameplaySpriteSheet()) {
            double scale = Math.max(0.35, height / 105.0);
            this.drawVectorMascotPlayer(graphics, x + width * 0.18, y, mascot, action == SpriteAction.SWING, scale);
            return;
        }
        graphics.save();
        graphics.translate(x + (facesRight ? 0.0 : width), y);
        graphics.rotate(rotation);
        graphics.scale((double)(facesRight ? 1 : -1), 1.0);
        graphics.drawImage(this.gameplaySprites, (double)action.column * 320.0, (double)mascot.ordinal() * 384.0, 320.0, 384.0, 0.0, 0.0, width, height);
        graphics.restore();
    }

    /** Consistent baseball kit overlay keeps every mascot readable as a player. */
    private void drawUniformKit(GraphicsContext graphics, Mascot mascot, double centerX, double feetY,
                                double width, double height) {
        Color primary = Color.web(mascot.getPrimaryColor());
        Color accent = Color.web(mascot.getAccentColor());
        graphics.save();
        graphics.setGlobalAlpha(0.9);
        graphics.setFill(primary);
        graphics.fillRoundRect(centerX - width * .17, feetY - height * .62,
                width * .34, height * .22, width * .06, width * .06);
        graphics.setStroke(Color.WHITE);
        graphics.setLineWidth(Math.max(1.0, width * .014));
        graphics.strokeLine(centerX, feetY - height * .61, centerX, feetY - height * .42);
        graphics.setFill(accent);
        graphics.fillRoundRect(centerX - width * .11, feetY - height * .50,
                width * .22, Math.max(2.0, height * .028), 3, 3);
        graphics.restore();
    }

    private void drawGlove(GraphicsContext graphics, double x, double y, double scale, boolean right) {
        double w=22*scale, h=16*scale;
        graphics.save();
        graphics.setFill(Color.web("#c58b4b"));
        graphics.fillRoundRect(x-(right?w*.22:w*.78), y-h*.52, w, h, h*.55, h*.55);
        graphics.setStroke(Color.web("#603b20"));
        graphics.setLineWidth(Math.max(1.0,scale*1.5));
        graphics.strokeRoundRect(x-(right?w*.22:w*.78), y-h*.52, w, h, h*.55, h*.55);
        graphics.strokeLine(x-(right?0:w),y-h*.08,x-(right?0:w),y+h*.25);
        graphics.restore();
    }

    private boolean hasGameplaySpriteSheet() {
        return this.gameplaySprites != null && !this.gameplaySprites.isError() && this.gameplaySprites.getWidth() >= 1280.0 && this.gameplaySprites.getHeight() >= 4608.0;
    }

    private boolean hasTurboTanukiSpriteSheet() {
        return this.turboTanukiSprites != null && !this.turboTanukiSprites.isError() && this.turboTanukiSprites.getWidth() >= 1000.0 && this.turboTanukiSprites.getHeight() >= 1000.0;
    }

    private void drawContactFlash(GraphicsContext graphics) {
        double centerX = 560.0 + this.visualBatOffsetX;
        double centerY = 548.0;
        double strength = GameController.clamp(this.contactFlash / 0.22, 0.0, 1.0);
        graphics.save();
        graphics.setGlobalAlpha(strength);
        graphics.setStroke((Paint)Color.WHITE);
        graphics.setLineWidth(5.0);
        int ray = 0;
        while (ray < 12) {
            double angle = (double)ray * Math.PI / 6.0;
            graphics.strokeLine(centerX + Math.cos(angle) * 18.0, centerY + Math.sin(angle) * 18.0, centerX + Math.cos(angle) * (42.0 + 18.0 * strength), centerY + Math.sin(angle) * (42.0 + 18.0 * strength));
            ++ray;
        }
        graphics.setFill((Paint)Color.web((String)"#ffe066"));
        graphics.fillOval(centerX - 14.0, centerY - 14.0, 28.0, 28.0);
        graphics.restore();
    }

    private void drawMascotHead(GraphicsContext graphics, Mascot mascot, double centerX, double centerY, Color primary, Color accent) {
        graphics.setFill((Paint)accent);
        switch (mascot) {
            case TURBO_TANUKI: {
                graphics.fillOval(centerX - 27.0, centerY - 23.0, 18.0, 18.0);
                graphics.fillOval(centerX + 9.0, centerY - 23.0, 18.0, 18.0);
                break;
            }
            case BUBBLE_BUNNY: {
                graphics.fillRoundRect(centerX - 20.0, centerY - 47.0, 13.0, 38.0, 10.0, 10.0);
                graphics.fillRoundRect(centerX + 7.0, centerY - 47.0, 13.0, 38.0, 10.0, 10.0);
                break;
            }
            case ROCKET_REX: {
                graphics.fillPolygon(new double[]{centerX - 22.0, centerX - 10.0, centerX, centerX + 10.0, centerX + 22.0}, new double[]{centerY - 9.0, centerY - 34.0, centerY - 13.0, centerY - 35.0, centerY - 7.0}, 5);
                break;
            }
            case NOVA_NEKO: {
                graphics.fillPolygon(new double[]{centerX - 24.0, centerX - 17.0, centerX - 2.0}, new double[]{centerY - 7.0, centerY - 36.0, centerY - 19.0}, 3);
                graphics.fillPolygon(new double[]{centerX + 2.0, centerX + 17.0, centerX + 24.0}, new double[]{centerY - 19.0, centerY - 36.0, centerY - 7.0}, 3);
                break;
            }
            case BLAZE_FALCON: {
                graphics.fillPolygon(new double[]{centerX - 8.0, centerX, centerX + 9.0}, new double[]{centerY - 19.0, centerY - 43.0, centerY - 18.0}, 3);
                break;
            }
            case FROST_WOLF: {
                graphics.fillPolygon(new double[]{centerX - 25.0, centerX - 18.0, centerX - 4.0}, new double[]{centerY - 7.0, centerY - 39.0, centerY - 17.0}, 3);
                graphics.fillPolygon(new double[]{centerX + 4.0, centerX + 18.0, centerX + 25.0}, new double[]{centerY - 17.0, centerY - 39.0, centerY - 7.0}, 3);
                break;
            }
            case REEF_SHARK: {
                graphics.fillPolygon(new double[]{centerX - 6.0, centerX + 2.0, centerX + 11.0}, new double[]{centerY - 17.0, centerY - 44.0, centerY - 17.0}, 3);
                break;
            }
            case MAPLE_RED_PANDA: {
                graphics.fillOval(centerX - 28.0, centerY - 27.0, 19.0, 19.0);
                graphics.fillOval(centerX + 9.0, centerY - 27.0, 19.0, 19.0);
                break;
            }
            case HARBOR_TURTLE: {
                graphics.fillOval(centerX - 31.0, centerY - 13.0, 15.0, 20.0);
                graphics.fillOval(centerX + 16.0, centerY - 13.0, 15.0, 20.0);
                break;
            }
            case EMBER_DRAGON: {
                graphics.fillPolygon(new double[]{centerX - 21.0, centerX - 13.0, centerX - 5.0}, new double[]{centerY - 17.0, centerY - 45.0, centerY - 19.0}, 3);
                graphics.fillPolygon(new double[]{centerX + 5.0, centerX + 13.0, centerX + 21.0}, new double[]{centerY - 19.0, centerY - 45.0, centerY - 17.0}, 3);
                break;
            }
            case VOLT_TIGER: {
                graphics.fillRoundRect(centerX - 25.0, centerY - 34.0, 17.0, 24.0, 7.0, 7.0);
                graphics.fillRoundRect(centerX + 8.0, centerY - 34.0, 17.0, 24.0, 7.0, 7.0);
                break;
            }
            case CIRCUIT_BOT: {
                graphics.fillRoundRect(centerX - 4.0, centerY - 44.0, 8.0, 20.0, 4.0, 4.0);
                graphics.fillOval(centerX - 7.0, centerY - 49.0, 14.0, 14.0);
            }
        }
        graphics.setFill((Paint)primary.brighter());
        graphics.fillRoundRect(centerX - 27.0, centerY - 20.0, 54.0, 43.0, 22.0, 22.0);
        graphics.setFill((Paint)Color.WHITE);
        graphics.fillOval(centerX - 17.0, centerY - 7.0, 13.0, 16.0);
        graphics.fillOval(centerX + 4.0, centerY - 7.0, 13.0, 16.0);
        graphics.setFill((Paint)Color.web((String)"#17223b"));
        graphics.fillOval(centerX - 12.0, centerY - 2.0, 6.0, 9.0);
        graphics.fillOval(centerX + 7.0, centerY - 2.0, 6.0, 9.0);
        graphics.setFill((Paint)accent);
        graphics.fillRoundRect(centerX - 31.0, centerY - 25.0, 62.0, 12.0, 10.0, 10.0);
        graphics.fillRoundRect(centerX - 7.0, centerY - 31.0, 35.0, 9.0, 8.0, 8.0);
    }

    private void drawRivalPlayer(GraphicsContext graphics, double x, double y, boolean batting) {
        this.drawMascotPlayer(graphics, x, y, this.displayedRivalMascot(), batting, ON_FIELD_MASCOT_SCALE);
    }

    private double playerScaleAt(WorldPoint feet, double roleBoost) {
        double depth = this.normalizedFieldDepth(feet);
        return GameController.clamp(ON_FIELD_MASCOT_SCALE * depth * roleBoost, 0.52, 0.96);
    }

    private void drawPitcherFieldingMascot(GraphicsContext graphics, double x, double y, Mascot mascot, double scale) {
        double width = SPRITE_BOX_WIDTH * scale;
        double height = SPRITE_BOX_HEIGHT * scale;
        // Field coordinates represent the mascot's feet.  Keep the pitcher on
        // the same ground-plane anchor in idle, wind-up, chase, pickup, hold,
        // and throw states so the sprite never appears to float above the
        // mound when its state changes.
        SpriteAction action = switch (this.pitcherFieldingStage) {
            case CHASE -> SpriteAction.RUN;
            case PICKUP, HOLD, THROW -> SpriteAction.PITCH;
            default -> SpriteAction.IDLE;
        };
        this.drawActionSprite(graphics, mascot, action,
                x - width / 2.0, y - height,
                width, height, false, 0.0);
    }

    private void drawCatcher(GraphicsContext graphics, double x, double y) {
        Mascot catcher = this.pitchSequenceController.catcher();
        double batterScale = this.phase == Phase.BATTING
                ? this.playerScaleAt(new WorldPoint(515,456),1.18) : ON_FIELD_MASCOT_SCALE;
        double size = SPRITE_BOX_HEIGHT * batterScale * 0.48;
        double center = x, feet = y;
        graphics.save();
        graphics.setFill(Color.rgb(0,0,0,0.25));
        graphics.fillOval(center-size*.30, feet-5, size*.60, 9);
        if (catcher == Mascot.CIRCUIT_BOT && this.catcherSprites != null) {
            int frame = this.catcherHoldTime > 0 ? 4 + Math.min(1,(int)((.65-this.catcherHoldTime)*8))
                    : this.pitchActive ? Math.min(3,(int)(this.pitchTravel*4)) : 0;
            double cellW = this.catcherSprites.getWidth()/4;
            double cellH = this.catcherSprites.getHeight()/4;
            graphics.drawImage(this.catcherSprites, frame%4*cellW, frame/4*cellH,
                    cellW,cellH, center-size/2,feet-size,size,size);
        } else {
            this.drawActionSprite(graphics,catcher,SpriteAction.IDLE,
                    center-size*.42,feet-size,size*.84,size,false,0);
        }
        // Visible catcher glove is attached to the same side used by the receive check.
        double gloveX = center + size * .24;
        double gloveY = feet - size * .46;
        if (this.catcherHoldTime > 0 && !this.pitchActive) this.drawHeldBall(graphics,center,feet-2);
        this.drawRoleIndicator(graphics,"C • "+catcher.getDisplayName(), center,feet+18,Color.web("#d9fffa"));
        graphics.restore();
    }
    private void drawParticles(GraphicsContext graphics) {
        for (Particle particle : this.particles) {
            graphics.setFill((Paint)Color.color((double)particle.color.getRed(), (double)particle.color.getGreen(), (double)particle.color.getBlue(), (double)GameController.clamp(particle.life, 0.0, 1.0)));
            graphics.fillRect(particle.x, particle.y, 7.0, 7.0);
        }
    }

    private void drawFloatingText(GraphicsContext graphics, double width) {
        if (this.floatingTextLife <= 0.0 || this.floatingText.isBlank()) {
            return;
        }
        double rise = (1.35 - this.floatingTextLife) * 28.0;
        graphics.setFont(Font.font((String)"Arial Rounded MT Bold", (FontWeight)FontWeight.EXTRA_BOLD, (double)31.0));
        graphics.setFill((Paint)Color.rgb((int)6, (int)10, (int)28, (double)0.78));
        graphics.fillRoundRect(width / 2.0 - 245.0, 82.0 - rise, 490.0, 53.0, 18.0, 18.0);
        graphics.setFill((Paint)this.floatingTextColor);
        graphics.fillText(this.floatingText, width / 2.0 - 220.0, 119.0 - rise);
    }

    private void drawCoverImage(GraphicsContext graphics, Image image, double width, double height) {
        double imageRatio = image.getWidth() / image.getHeight();
        double canvasRatio = width / height;
        double sourceX = 0.0;
        double sourceY = 0.0;
        double sourceWidth = image.getWidth();
        double sourceHeight = image.getHeight();
        if (imageRatio > canvasRatio) {
            sourceWidth = image.getHeight() * canvasRatio;
            sourceX = (image.getWidth() - sourceWidth) / 2.0;
        } else {
            sourceHeight = image.getWidth() / canvasRatio;
            sourceY = (image.getHeight() - sourceHeight) / 2.0;
        }
        graphics.drawImage(image, sourceX, sourceY, sourceWidth, sourceHeight, 0.0, 0.0, width, height);
    }

    private double pitchProgress(double currentX) {
        return GameController.clamp(this.pitchTravel, 0.0, 1.0);
    }

    private double trajectoryY(double progress, PitchType pitchType, double targetY) {
        double straightLine = 560.0 + (targetY - 560.0) * progress;
        double breakMultiplier = this.phase == Phase.BATTING ? GameSettings.getDifficulty().getBreakMultiplier() : GameSession.getPitchStyle().breakAmount();
        return straightLine + Math.sin(progress * Math.PI) * pitchType.curvePixels * breakMultiplier;
    }

    private boolean isInsideStrikeZone(double x) {
        return x>=557.167&&x<=562.833&&pitchEndHeight>=6&&pitchEndHeight<=14;
    }

    private static double zoneCenterY() {
        return 560.0;
    }

    private PitchType selectedPitch() {
        return GameSession.getPitchLoadout().get(Math.floorMod(this.selectedPitchIndex, GameSession.getPitchLoadout().size()));
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

    private static final class Particle {
        private double x;
        private double y;
        private double velocityX;
        private double velocityY;
        private final Color color;
        private double life;

        private Particle(double x, double y, double velocityX, double velocityY, Color color, double life) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.color = color;
            this.life = life;
        }
    }

    private static enum Phase {
        PITCHING,
        BATTING,
        FINISHED;

    }

    private static enum SpriteAction {
        IDLE(0),
        PITCH(1),
        SWING(2),
        RUN(3);

        private final int column;

        private SpriteAction(int column) {
            this.column = column;
        }
    }
}





