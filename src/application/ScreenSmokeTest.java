package application;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Loads real screens on the JavaFX thread and checks all role combinations. */
public final class ScreenSmokeTest extends Application {
    public static void main(String[] args) { Main.configureRendering(); launch(args); }
    @Override public void start(Stage stage) {
        try {
            DatabaseConnection.enableOfflineMode();
            GameSession.start("SCREEN TEST");
            PitchSequenceController roles = new PitchSequenceController();
            for (Mascot pitcher : Mascot.values()) for (Mascot batter : Mascot.values()) {
                roles.configure(pitcher, batter, pitcher);
                if (roles.catcher() == pitcher || roles.catcher() == batter)
                    throw new AssertionError("Duplicate catcher identity");
            }
            for (String name : new String[]{"roster.fxml", "game.fxml"}) {
                var root = FXMLLoader.<javafx.scene.Parent>load(getClass().getResource("/application/"+name));
                stage.setScene(new Scene(root,1280,900));
                stage.show();
                root.applyCss(); root.layout(); root.snapshot(null,null);
                System.out.println("SCREEN_LOADED="+name);
            }
            System.out.println("SCREEN_SMOKE=PASS");
            new javafx.animation.AnimationTimer() {
                long last, first; int frames, slow, slower; double total; java.util.ArrayList<Double> times = new java.util.ArrayList<>();
                public void handle(long now) {
                    if (first == 0) { first = last = now; return; }
                    double ms = (now-last)/1e6; last=now;
                    if (now-first > 1_000_000_000L) { total += ms; frames++; times.add(ms); if(ms>25) slow++; if(ms>33.3) slower++; }
                    if (now-first > 9_000_000_000L) {
                        System.out.printf("FRAME_MEAN_MS=%.2f SLOW_OVER_25MS=%d FRAMES=%d%n",total/frames,slow,frames);
                        java.util.Collections.sort(times);
                        System.out.printf("MEDIAN_MS=%.2f P95_MS=%.2f OVER_33MS=%d%n",times.get(times.size()/2),times.get((int)(times.size()*.95)),slower);
                        stop(); Platform.exit();
                    }
                }
            }.start();
        } catch (Throwable failure) { failure.printStackTrace(); System.exit(1); }
    }
}


