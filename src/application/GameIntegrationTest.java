package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

/** Exercises actual input, physical pitch, live hit and complete defensive sequence. */
public final class GameIntegrationTest extends Application {
    private GameController game; private Parent root; private Stage stage;
    private final Path output=Path.of(System.getProperty("game.evidence","build/final-review"));
    private static Throwable failure;
    public static void main(String[]args){Main.configureRendering();launch(args);if(failure!=null)throw new AssertionError(failure);}
    public void start(Stage window){stage=window;try{
        Files.createDirectories(output);DatabaseConnection.enableOfflineMode();GameSession.start("MATCH VERIFICATION");
        load(GameMode.BATTING_ONLY);snapshot("01-batting");
        invoke("launchRivalPitch");set("pitchEndX",560.0);set("pitchEndHeight",10.0);
        LiveBallController live=(LiveBallController)get("liveBallController");
        WorldPoint release=live.shadowPosition();step(.25);
        check(live.state()==LiveBallController.State.HELD_BY_PITCHER,"Windup holds original ball");
        check(release.distanceTo(live.shadowPosition())<.001,"No flight before release");
        while((double)get("pitchTravel")<.95)step(1.0/120);
        check(live.ball().y()>550&&live.ball().y()<611,"Pitch traverses world ground continuously");
        check(Math.abs(live.ball().height()-(double)get("pitchHeight"))<.00001,"Rendered and authoritative pitch height agree");
        snapshot("03-pitch-approach");
        set("batOffsetX",live.ball().x()-560);set("visualBatOffsetX",live.ball().x()-560);set("pitchAimHeight",live.ball().height());
        WorldPoint before=live.shadowPosition();double beforeHeight=live.ball().height();
        invoke("handleSwing");
        check(live.state()==LiveBallController.State.AIRBORNE,"Real swing creates live hit");
        check(before.distanceTo(live.shadowPosition())<.001&&Math.abs(beforeHeight-live.ball().height())<.001,"Contact never teleports ball");
        check(((MatchState)get("matchState")).playerRuns()==0,"No automatic run at contact");snapshot("04-contact");
        step(1.0);snapshot("05-wide-fielding");
        load(GameMode.BOWLING_ONLY);snapshot("02-pitching");
        int count=((MatchState)get("matchState")).pitchCount();invoke("handleSwing");
        check(((MatchState)get("matchState")).pitchCount()==count,"Pitcher cannot swing");
        load(GameMode.BATTING_ONLY);invoke("launchRivalPitch");
        while((double)get("windupRemaining")>0)step(1.0/120);
        set("pitchActive",false);set("ballX",560.0);set("ballY",608.0);set("pitchHeight",6.0);
        ((BaseballPlayStateController)get("playStateController")).transition(BaseballPlayStateController.State.BATTER_SWINGING);
        invoke("startPitcherFieldingSequence",new Class<?>[]{double.class,HitResult.class,Mascot.class},.6,HitResult.SINGLE,Mascot.ROCKET_REX);
        boolean pickup=false,hold=false,throwing=false,done=false;int initialOuts=((MatchState)get("matchState")).outs();
        for(int i=0;i<2400;i++){
            String state=get("pitcherFieldingStage").toString();
            if(!state.equals("THROW")&&!state.equals("NONE"))check(((MatchState)get("matchState")).outs()==initialOuts,"No out before throw reception");
            if(state.equals("PICKUP")&&!pickup){pickup=true;snapshot("06-pickup");}
            if(state.equals("HOLD")&&!hold){hold=true;snapshot("07-hold");}
            if(state.equals("THROW")&&!throwing){throwing=true;step(.1);snapshot("08-throw");}
            if(state.equals("NONE")){done=true;snapshot("09-result");break;}
            step(1.0/120);
        }
        check(pickup&&hold&&throwing&&done,"Pitcher reaches, picks up, holds, throws and completes actual play");
        Files.writeString(output.resolve("integration-checks.txt"),"GAME_INTEGRATION=PASS\nActual FXML input and gameplay exercised.\nContinuous release/contact, world ball height, wide camera, pickup/hold/throw/result verified.\n");
        System.out.println("GAME_INTEGRATION=PASS");
    }catch(Throwable t){failure=t;t.printStackTrace();}finally{try{if(game!=null)invoke("stopGameLoop");}catch(Exception ignored){}stage.hide();Platform.exit();}}
    private void load(GameMode mode)throws Exception{
        if(game!=null)invoke("stopGameLoop");GameSettings.setGameMode(mode);
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/application/game.fxml"));root=loader.load();game=loader.getController();
        invoke("stopGameLoop");set("automaticPitchDelay",0.0);stage.setScene(new Scene(root,1280,850));stage.show();root.applyCss();root.layout();
    }
    private void step(double seconds)throws Exception{for(double t=0;t<seconds-.0000001;t+=1.0/120){invoke("updateGame",new Class<?>[]{double.class},1.0/120);invoke("updateEffects",new Class<?>[]{double.class},1.0/120);}}
    private Object get(String name)throws Exception{var f=GameController.class.getDeclaredField(name);f.setAccessible(true);return f.get(game);}
    private void set(String name,Object value)throws Exception{var f=GameController.class.getDeclaredField(name);f.setAccessible(true);f.set(game,value);}
    private Object invoke(String name)throws Exception{return invoke(name,new Class<?>[0]);}
    private Object invoke(String name,Class<?>[] signature,Object...args)throws Exception{var m=GameController.class.getDeclaredMethod(name,signature);m.setAccessible(true);return m.invoke(game,args);}
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    private void snapshot(String name)throws Exception{
        invoke("drawGame");root.applyCss();root.layout();WritableImage im=root.snapshot(null,null);int w=(int)im.getWidth(),h=(int)im.getHeight();
        try(DataOutputStream out=new DataOutputStream(Files.newOutputStream(output.resolve(name+".png")))){
            out.writeLong(0x89504e470d0a1a0aL);ByteArrayOutputStream header=new ByteArrayOutputStream();DataOutputStream d=new DataOutputStream(header);
            d.writeInt(w);d.writeInt(h);d.write(new byte[]{8,6,0,0,0});chunk(out,"IHDR",header.toByteArray());ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            try(DeflaterOutputStream z=new DeflaterOutputStream(bytes)){for(int y=0;y<h;y++){z.write(0);for(int x=0;x<w;x++){int p=im.getPixelReader().getArgb(x,y);z.write(p>>16&255);z.write(p>>8&255);z.write(p&255);z.write(p>>>24);}}}
            chunk(out,"IDAT",bytes.toByteArray());chunk(out,"IEND",new byte[0]);
        }
    }
    private static void chunk(DataOutputStream out,String name,byte[]bytes)throws IOException{byte[]type=name.getBytes(java.nio.charset.StandardCharsets.US_ASCII);out.writeInt(bytes.length);out.write(type);out.write(bytes);CRC32 c=new CRC32();c.update(type);c.update(bytes);out.writeInt((int)c.getValue());}
}
