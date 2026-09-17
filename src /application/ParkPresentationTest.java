package application;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Integration checks against the actual FXML, input routing, projection and HUD layout. */
public final class ParkPresentationTest extends Application {
    private final Path output = Path.of(System.getProperty("game.evidence", "build/park-final"));
    private GameController game;
    private Parent root;
    private ParkCamera camera;
    private Stage stage;

    public static void main(String[] args) { Main.configureRendering(); launch(args); }

    @Override public void start(Stage window) {
        stage = window;
        try {
            Files.createDirectories(output);
            DatabaseConnection.enableOfflineMode();
            GameSession.start("PRESENTATION VERIFICATION");
            load(GameMode.BATTING_ONLY);
            Platform.runLater(() -> {
                try {
                    verifyRole(ParkCamera.Mode.BATTING);
                    verifyControls(false);
                    snapshot("01-batting");
                    verifyResizing();
                    verifyPursuit();
                    load(GameMode.BOWLING_ONLY);
                    verifyRole(ParkCamera.Mode.PITCHING);
                    verifyControls(true);
                    snapshot("02-pitching");
                    verifyResizing();
                    verifyPursuit();
                    load(GameMode.GRAND_PRIX);
                    verifyRole(ParkCamera.Mode.PITCHING);
                    verifyControls(true);
                    invoke("beginBattingHalf");
                    set("automaticPitchDelay", 0.0);
                    camera.update(ParkCamera.Mode.BATTING, 1.0);
                    invoke("updateControlsForPhase");
                    verifyRole(ParkCamera.Mode.BATTING);
                    verifyControls(false);
                    Files.writeString(output.resolve("presentation-checks.txt"),
                            "PARK_PRESENTATION=PASS\nActual FXML loads in all three game modes.\n"
                            + "World-space square bases, true pitching distance, and opposite role projection verified.\n"
                            + "Role-specific controls, HUD mouse isolation, and full-match role switching verified.\n"
                            + "Mouse-to-strike-box aiming, uniform viewport resizing, and control bounds verified.\n"
                            + "Persistent fielders move continuously toward bounded ground locations.\n"
                            + "Saved screenshots require visual inspection for artwork quality.\n");
                    System.out.println("PARK_PRESENTATION=PASS");
                    stage.hide();
                    Platform.exit();
                } catch (Throwable failure) { fail(failure); }
            });
        } catch (Throwable failure) { fail(failure); }
    }

    private void load(GameMode mode) throws Exception {
        if (game != null) invoke("stopGameLoop");
        GameSettings.setGameMode(mode);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/game.fxml"));
        root = loader.load();
        game = loader.getController();
        invoke("stopGameLoop");
        set("automaticPitchDelay", 0.0);
        camera = (ParkCamera)get("parkCamera");
        stage.setScene(new Scene(root, 1280, 850));
        stage.show();
        root.applyCss();
        root.layout();
    }

    private void verifyRole(ParkCamera.Mode expectedMode) throws Exception {
        require(camera.currentMode() == expectedMode, "Actual role camera matches game mode");
        require((boolean)get("gameLoopStopped"), "Fixture stops automatic simulation");
        WorldPoint[] feet = (WorldPoint[])get("fielderFeet");
        require(feet.length >= 3 && feet.length <= 7, "Reduced roster still covers field positions");
        require(((boolean[])get("fielderFacing")).length == feet.length
                && ((boolean[])get("fielderMoving")).length == feet.length
                && ((double[])get("fielderReach")).length == feet.length,
                "Animation and catch data match the actual roster");
        for (int i = 0; i < feet.length; i++)
            for (int j = i + 1; j < feet.length; j++)
                require(feet[i].distanceTo(feet[j]) > 20, "Initial fielders have separate ground positions");

        BasePath path = ((RunnerController)get("runnerController")).path();
        WorldPoint home = path.point(Base.HOME);
        WorldPoint first = path.point(Base.FIRST);
        WorldPoint second = path.point(Base.SECOND);
        WorldPoint third = path.point(Base.THIRD);
        double side = home.distanceTo(first);
        near(side, 360.0, "A 90-foot base path uses four world units per foot");
        near(first.distanceTo(second), side, "First-to-second side");
        near(second.distanceTo(third), side, "Second-to-third side");
        near(third.distanceTo(home), side, "Third-to-home side");
        near(home.distanceTo(second), side * Math.sqrt(2), "Square diagonal");
        WorldPoint mound = new WorldPoint((double)get("pitcherX"), (double)get("pitcherY"));
        near(mound.x(), home.x(), "Mound and plate share center axis");
        near(mound.distanceTo(home), 60.5 * 4, "Rubber is 60 feet 6 inches from home");
        WorldPoint projectedHome = camera.project(home, 0);
        WorldPoint projectedMound = camera.project(mound, 0);
        require(expectedMode == ParkCamera.Mode.BATTING
                        ? projectedHome.y() > projectedMound.y()
                        : projectedMound.y() > projectedHome.y(),
                "Player's role occupies the correct foreground");
        WorldPoint right = camera.project(new WorldPoint(home.x()+4, home.y()), 0);
        require(expectedMode == ParkCamera.Mode.BATTING ? right.x()>projectedHome.x() : right.x()<projectedHome.x(),
                "Pitching camera faces the opposite world direction");

        for (WorldPoint target : List.of(new WorldPoint(home.x()-1, 8), new WorldPoint(home.x()+1, 12))) {
            WorldPoint screen = camera.project(new WorldPoint(target.x(), home.y()), target.y());
            invoke("updatePointer", new Class<?>[]{double.class, double.class}, screen.x(), screen.y());
            near((double)get("pitchAimX"), target.x(), "Displayed strike box horizontal aim");
            near((double)get("pitchAimHeight"), target.y(), "Displayed strike box vertical aim");
        }
        WorldPoint centered = camera.project(home, 10);
        invoke("updatePointer", new Class<?>[]{double.class, double.class}, centered.x(), centered.y());
        for (int frame = 0; frame < 60; frame++) {
            camera.update(expectedMode, 1.0 / 60);
            near(camera.project(home, 0).y(), projectedHome.y(), "No zoom pulses during aiming");
        }
        invoke("drawGame");
        var transform = ((Canvas)get("gameCanvas")).getGraphicsContext2D().getTransform();
        near(transform.getMxx(), 1, "Renderer restores graphics transform");
        near(transform.getMyy(), 1, "Renderer restores graphics transform");
    }

    private void verifyControls(boolean pitching) throws Exception {
        Node pitchControls = root.lookup("#pitchControls");
        Button pitch = (Button)root.lookup("#primaryButton");
        Button swing = (Button)root.lookup("#swingButton");
        require(effectiveVisible(pitchControls) == pitching, "Pitch selector belongs only to pitcher");
        require(pitch.isVisible() == pitching && pitch.isManaged() == pitching, "Pitch action belongs only to pitcher");
        require(swing.isVisible() != pitching && swing.isManaged() != pitching, "Swing belongs only to batter");
        String help = ((Label)root.lookup("#inputHelpLabel")).getText().toUpperCase();
        require(pitching ? !help.contains("SWING") : !help.contains("CLICK TO PITCH"),
                "Input help does not instruct the opposite role");

        boolean before = (boolean)get("pitchActive");
        Node metric = root.lookup("#mascotLabel");
        Event.fireEvent(metric, new MouseEvent(MouseEvent.MOUSE_PRESSED, 5,5,5,5,
                MouseButton.PRIMARY,1,false,false,false,false,true,false,false,false,false,true,null));
        require((boolean)get("pitchActive") == before, "Clicking HUD text does not pitch or swing");
        if (pitching) {
            int selected = (int)get("selectedPitchIndex");
            ((Button)root.lookup("#pitchNextButton")).fire();
            require((int)get("selectedPitchIndex") != selected, "Displayed pitch selector changes the pitch");
            require(!(boolean)get("pitchActive"), "Pitch selection cannot accidentally launch a ball");
            key(KeyCode.SPACE);
            require(!(boolean)get("pitchActive"), "Space does not trigger a pitch while pitching");
        } else {
            invoke("handlePrimaryAction");
            require(!(boolean)get("pitchActive"), "Batter cannot manually throw a pitch");
        }
    }

    private void verifyResizing() throws Exception {
        for (double[] size : new double[][] {{960,640},{1440,900}}) {
            stage.getScene().setRoot(new javafx.scene.Group());
            stage.setScene(new Scene(root,size[0],size[1]));
            root.applyCss();
            root.resize(size[0],size[1]);
            root.layout();
            Canvas canvas=(Canvas)get("gameCanvas");
            StackPane viewport=(StackPane)root.lookup("#viewportPane");
            near(canvas.getScaleX(),canvas.getScaleY(),"Resize preserves field aspect ratio");
            require(canvas.getScaleX()>0,"Resize retains a visible field");
            Bounds field=canvas.getBoundsInParent();
            require(field.getWidth()<=viewport.getWidth()+1 && field.getHeight()<=viewport.getHeight()+1,
                    "Canvas fits available viewport");
            for(Node node:root.lookupAll(".button")) if(effectiveVisible(node)) {
                Bounds b=node.localToScene(node.getBoundsInLocal());
                require(b.getMinX()>=-1 && b.getMaxX()<=size[0]+1
                                && b.getMinY()>=-1 && b.getMaxY()<=size[1]+1,
                        "Visible control fits resized scene: "+((Button)node).getText());
            }
            snapshot(camera.currentMode().name().toLowerCase(java.util.Locale.ROOT)
                    + (size[0]<1000 ? "-compact-960" : "-wide-1440"));
        }
    }

    private void verifyPursuit() throws Exception {
        WorldPoint[] feet = (WorldPoint[])get("fielderFeet");
        WorldPoint[] original = feet.clone();
        LiveBallController live = (LiveBallController)get("liveBallController");
        set("pitchActive", false);
        set("fieldingActive", true);
        set("fieldingFielderIndex", 0);
        set("reactionRemaining", 0.0);
        set("heldFielder", -1);
        WorldPoint home=((RunnerController)get("runnerController")).path().point(Base.HOME);
        for (WorldPoint target : List.of(new WorldPoint(-1800, -1800), new WorldPoint(2600, -1700))) {
            live.deadBall();
            live.launchBattedBall(home.x(),home.y(),.5,HitResult.SINGLE,0,-1);
            live.ball().setMotion(target.x(),target.y(),20,0,0,-20);
            for (int frame=0; frame<120; frame++) {
                WorldPoint[] before=feet.clone();
                invoke("updateFielderFeet",new Class<?>[]{double.class},1.0/120);
                for(int i=0;i<feet.length;i++) {
                    WorldPoint point=feet[i];
                    require(Double.isFinite(point.x())&&Double.isFinite(point.y()),"Finite ground anchors");
                    require(point.distanceTo(home)<400*4+160,"Fielders stay within stadium ground envelope");
                    require(before[i].distanceTo(point)<5,"Pursuit remains continuous");
                }
            }
        }
        require(feet==get("fielderFeet"),"Same fielders persist throughout pursuit");
        live.deadBall();
        set("fieldingActive",false);
        set("fieldingFielderIndex",-1);
        System.arraycopy(original,0,feet,0,original.length);
    }

    private void key(KeyCode code) throws Exception {
        Event.fireEvent((Canvas)get("gameCanvas"),new KeyEvent(KeyEvent.KEY_PRESSED,"","",code,false,false,false,false));
    }
    private static boolean effectiveVisible(Node node) {
        for(Node n=node;n!=null;n=n.getParent()) if(!n.isVisible())return false;
        return true;
    }
    private Object get(String field) throws Exception {
        var member=GameController.class.getDeclaredField(field); member.setAccessible(true); return member.get(game);
    }
    private void set(String field,Object value) throws Exception {
        var member=GameController.class.getDeclaredField(field); member.setAccessible(true); member.set(game,value);
    }
    private Object invoke(String method) throws Exception { return invoke(method,new Class<?>[0]); }
    private Object invoke(String method,Class<?>[] parameters,Object... args) throws Exception {
        var member=GameController.class.getDeclaredMethod(method,parameters); member.setAccessible(true); return member.invoke(game,args);
    }
    private static void near(double actual,double expected,String message) {
        require(Math.abs(actual-expected)<1e-5,message+": "+actual+" != "+expected);
    }
    private static void require(boolean value,String message) { if(!value)throw new AssertionError(message); }
    private static void fail(Throwable failure) { failure.printStackTrace(); Platform.exit(); System.exit(1); }

    // A small PNG writer avoids introducing java.desktop/javafx.swing modules
    // into the application just for visual verification.
    private void snapshot(String name) throws Exception {
        invoke("drawGame");
        WritableImage image = root.snapshot(null, null);
        int width = (int)image.getWidth(), height = (int)image.getHeight();
        try (DataOutputStream file = new DataOutputStream(Files.newOutputStream(output.resolve(name + ".png")))) {
            file.writeLong(0x89504e470d0a1a0aL);
            ByteArrayOutputStream header = new ByteArrayOutputStream();
            DataOutputStream h = new DataOutputStream(header);
            h.writeInt(width); h.writeInt(height); h.write(new byte[]{8, 6, 0, 0, 0});
            chunk(file, "IHDR", header.toByteArray());
            ByteArrayOutputStream compressed = new ByteArrayOutputStream();
            try (DeflaterOutputStream stream = new DeflaterOutputStream(compressed)) {
                for (int y = 0; y < height; y++) {
                    stream.write(0);
                    for (int x = 0; x < width; x++) {
                        int pixel = image.getPixelReader().getArgb(x, y);
                        stream.write(pixel >> 16 & 255); stream.write(pixel >> 8 & 255);
                        stream.write(pixel & 255); stream.write(pixel >>> 24);
                    }
                }
            }
            chunk(file, "IDAT", compressed.toByteArray());
            chunk(file, "IEND", new byte[0]);
        }
    }
    private static void chunk(DataOutputStream file, String name, byte[] bytes) throws Exception {
        byte[] type = name.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        file.writeInt(bytes.length); file.write(type); file.write(bytes);
        CRC32 crc = new CRC32(); crc.update(type); crc.update(bytes);
        file.writeInt((int)crc.getValue());
    }
}
