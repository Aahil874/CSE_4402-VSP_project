package application;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Deterministic scenarios driven through the actual FXML controller and renderer. */
public final class GameplayVisualTest extends Application {
    private GameController game;
    private Parent root;
    private Canvas canvas;
    private final Map<String,List<Double>> timings=new LinkedHashMap<>();
    private final Path output=Path.of(System.getProperty("game.evidence","build/visual-evidence"));
    private boolean prompted, held, contact;
    private Object fielderArray;
    private WorldPoint heldFeet;
    private String catchOwner;
    public static void main(String[] args) { Main.configureRendering(); launch(args); }
    @Override public void start(Stage stage) {
        try {
            Files.createDirectories(output);
            DatabaseConnection.enableOfflineMode(); GameSession.start("VISUAL VERIFICATION");
            GameSettings.setGameMode(GameMode.BATTING_ONLY);
            FXMLLoader loader=new FXMLLoader(getClass().getResource("/application/game.fxml"));
            root=loader.load(); game=loader.getController(); canvas=(Canvas)get("gameCanvas");
            set("automaticPitchDelay",0.0);
            stage.setScene(new Scene(root,1280,900)); stage.show(); root.applyCss(); root.layout();
            new AnimationTimer() {
                long first,last; int part=-1; boolean shot;
                public void handle(long now) {
                    try {
                        if(first==0) { first=last=now; return; }
                        double seconds=(now-first)/1e9, ms=(now-last)/1e6; last=now;
                        int next=seconds<2?0:seconds<5?1:seconds<9?2:seconds<12?3:4;
                        if(next!=part) { part=next; shot=false; setup(part); }
                        String name=switch(part) { case 0->"idle_warmup";case 1->"batting_pitch_contact";
                            case 2->"pursuit_catch_hold_runners";case 3->"unreachable_ball";default->"complete";};
                        if(part>0) timings.computeIfAbsent(name,k->new ArrayList<>()).add(ms);
                        if(part==0 && seconds>1 && !shot) { screenshot("01-stable-batting");shot=true; }
                        if(part==1 && (double)get("pitchTravel")>.83 && !contact) {
                            invoke("handleSwing"); contact=true; screenshot("02-batting-contact");
                        }
                        if(part==2) {
                            if(!shot) { screenshot("03-pursuit"); shot=true; }
                            if((boolean)invoke("catchEligible")&&!prompted) {
                                screenshot("04-glove-prompt");
                                GlovePose glove=(GlovePose)invoke("glovePose",new Class<?>[]{int.class},0);
                                check(glove.promptContains(glove.screen().x()+40,glove.screen().y()-22),"prompt input anchor");
                                invoke("attemptFieldingCatch");prompted=true;
                            }
                            if((int)get("heldFielder")==0) {
                                WorldPoint feet=((WorldPoint[])get("fielderFeet"))[0];
                                LiveBallController ball=(LiveBallController)get("liveBallController");
                                if(!held) { heldFeet=feet; catchOwner=ball.owner(); screenshot("05-secured-ball");held=true; }
                                check(heldFeet.equals(feet),"holding foot continuity");
                                check(catchOwner.equals(ball.owner()),"owner continuity");
                                check(!ball.ball().active()&&ball.trail().isEmpty(),"no extra flying ball or trail");
                                GlovePose glove=(GlovePose)invoke("glovePose",new Class<?>[]{int.class},0);
                                check(ball.screenPosition().distanceTo(glove.screen())<.001,"held ball attachment");
                            }
                            check(fielderArray==get("fielderFeet"),"persistent fielder collection");
                        }
                        if(part==3) {
                            check(!(boolean)invoke("catchEligible"),"unreachable ball has no prompt");
                            if(!shot) { screenshot("06-unreachable-no-prompt");shot=true; }
                        }
                        var tx=canvas.getGraphicsContext2D().getTransform();
                        check(Math.abs(tx.getMxx()-1)<1e-9&&Math.abs(tx.getMyy()-1)<1e-9,"camera scale");
                        if(part==4) {
                            check(prompted&&held,"reachable requested catch completed");
                            stage.hide();check((boolean)get("gameLoopStopped"),"timer cleanup on hide");
                            report();System.out.println("GAMEPLAY_VISUAL=PASS");stop();Platform.exit();
                        }
                    } catch(Throwable failure) { failure.printStackTrace();stop();Platform.exit();System.exit(1); }
                }
            }.start();
        } catch(Throwable failure) { failure.printStackTrace();System.exit(1); }
    }
    private void setup(int part) throws Exception {
        if(part==1) invoke("launchRivalPitch");
        if(part==2||part==3) {
            set("phase",Enum.valueOf((Class)get("phase").getClass(),"PITCHING"));
            invoke("updateControlsForPhase"); invoke("updateHud");
            set("pitchActive",false);set("automaticPitchDelay",0.0);set("heldSeconds",0.0);set("heldFielder",-1);
            set("fieldingActive",true);set("fieldingFielderIndex",0);set("reactionRemaining",0.0);
            set("catchRequested",false);
            WorldPoint[] feet=(WorldPoint[])get("fielderFeet");feet[0]=new WorldPoint(360,330);fielderArray=feet;
            ((boolean[])get("fielderFacing"))[0]=false;
            ((double[])get("fielderReach"))[0]=1;
            GlovePose glove=(GlovePose)invoke("glovePose",new Class<?>[]{int.class},0);
            LiveBallController live=(LiveBallController)get("liveBallController");live.deadBall();
            live.launchBattedBall(560,548,.5,HitResult.SINGLE,1,-1);
            if(part==2) {
                live.ball().setMotion(glove.ground().x()-70,330,glove.height()+5,180,0,55);
                ((RunnerController)get("runnerController")).clear();
                ((RunnerController)get("runnerController")).advance(HitResult.SINGLE,Mascot.ROCKET_REX);
            } else live.ball().setMotion(950,80,450,160,-30,100);
        }
    }
    private void report() throws Exception {
        StringBuilder text=new StringBuilder("Warm-up: first 2 seconds excluded. VSync="+System.getProperty("prism.vsync")+"\n");
        for(var item:timings.entrySet()) {
            if(item.getKey().equals("complete"))continue;
            List<Double> v=item.getValue();Collections.sort(v);
            text.append(String.format(Locale.ROOT,"%s: n=%d mean=%.2f median=%.2f p95=%.2f >25ms=%d >33.3ms=%d%n",
                item.getKey(),v.size(),v.stream().mapToDouble(x->x).average().orElse(0),v.get(v.size()/2),v.get((int)(v.size()*.95)),
                v.stream().filter(x->x>25).count(),v.stream().filter(x->x>33.3).count()));
        }
        Files.writeString(output.resolve("frame-times.txt"),text);System.out.print(text);
    }
    private Object get(String field) throws Exception {var f=GameController.class.getDeclaredField(field);f.setAccessible(true);return f.get(game);}
    private void set(String field,Object value) throws Exception {var f=GameController.class.getDeclaredField(field);f.setAccessible(true);f.set(game,value);}
    private Object invoke(String method) throws Exception {return invoke(method,new Class<?>[0]);}
    private Object invoke(String method,Class<?>[] signature,Object...args) throws Exception {
        var m=GameController.class.getDeclaredMethod(method,signature);m.setAccessible(true);return m.invoke(game,args);
    }
    private static void check(boolean condition,String message) {if(!condition)throw new AssertionError(message);}
    private void screenshot(String name) throws Exception {
        if ("false".equals(System.getProperty("game.capture"))) return;
        invoke("drawGame"); WritableImage im=root.snapshot(null,null);
        int width=(int)im.getWidth(),height=(int)im.getHeight();
        try(DataOutputStream file=new DataOutputStream(Files.newOutputStream(output.resolve(name+".png")))) {
            file.writeLong(0x89504e470d0a1a0aL);
            ByteArrayOutputStream header=new ByteArrayOutputStream();DataOutputStream h=new DataOutputStream(header);
            h.writeInt(width);h.writeInt(height);h.write(new byte[]{8,6,0,0,0});chunk(file,"IHDR",header.toByteArray());
            ByteArrayOutputStream compressed=new ByteArrayOutputStream();
            try(DeflaterOutputStream z=new DeflaterOutputStream(compressed)) {
                for(int y=0;y<height;y++) {z.write(0);for(int x=0;x<width;x++) {
                    int pixel=im.getPixelReader().getArgb(x,y);z.write(pixel>>16&255);z.write(pixel>>8&255);z.write(pixel&255);z.write(pixel>>>24);
                }}
            }
            chunk(file,"IDAT",compressed.toByteArray());chunk(file,"IEND",new byte[0]);
        }
    }
    private static void chunk(DataOutputStream file,String name,byte[] bytes)throws IOException {
        byte[] type=name.getBytes(java.nio.charset.StandardCharsets.US_ASCII);file.writeInt(bytes.length);file.write(type);file.write(bytes);
        CRC32 crc=new CRC32();crc.update(type);crc.update(bytes);file.writeInt((int)crc.getValue());
    }
}
