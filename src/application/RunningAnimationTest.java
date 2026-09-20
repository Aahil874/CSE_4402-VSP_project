package application;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
/** Loads the actual used atlas, verifies transparency and distinct full-body strides. */
public final class RunningAnimationTest extends Application {
    private static Throwable failure;
    public static void main(String[] args){launch(args);if(failure!=null)throw new AssertionError("Running atlas failed",failure);}
    @Override public void start(Stage stage){
        try{
            require(RunningSpriteSheetMetadata.valid(),"Metadata matches generated181px cells");
            ParkSceneRenderer renderer=new ParkSceneRenderer();require(renderer.hasRunningAtlas(),"Actual running atlas loaded");
            Image image=renderer.runningAtlas();int cw=(int)image.getWidth()/4,ch=(int)image.getHeight()/12;
            require(cw==181&&ch==181,"Dimensions match actual packed resource");
            var reader=image.getPixelReader();
            for(Mascot m:Mascot.values()){
                long previous=0;
                for(int frame=0;frame<4;frame++){
                    int opaque=0,white=0,transparentEdge=0;long legs=17;
                    for(int y=0;y<ch;y++)for(int x=0;x<cw;x++){
                        int argb=reader.getArgb(frame*cw+x,m.ordinal()*ch+y),alpha=argb>>>24;
                        if(alpha>100){opaque++;if((argb&0x00ffffff)>0x00e0e0e0)white++;}
                        if((x==0||x==cw-1||y==0||y==ch-1)&&alpha==0)transparentEdge++;
                        if(y>ch*.55)legs=31*legs+argb;
                    }
                    require(opaque>cw*ch*.15,"Full body exists:"+m+"/"+frame);
                    require(white>8,"White uniform remains opaque:"+m+"/"+frame);
                    require(transparentEdge>(cw+ch)*1.5,"Checkerboard removed at cell edges:"+m+"/"+frame);
                    require(frame==0||legs!=previous,"Leg-region artwork changes between strides:"+m);
                    previous=legs;
                }
            }
            System.out.println("RUNNING_ANIMATION=PASS");
        }catch(Throwable t){failure=t;}finally{Platform.exit();}
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}

