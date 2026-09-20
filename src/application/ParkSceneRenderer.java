package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/** An illustrated park drawn from physical field geometry, with grounded actors. */
public final class ParkSceneRenderer {
    /**
     * WINDUP is the lead-in stance before the ball is released; PITCH is the
     * release frame itself. RUN is a fielder chasing the ball and carries a
     * glove; BASERUN is a batter running the bases, who should not be, so it
     * uses the empty-handed run frame from the action sheet instead.
     */
    public enum Pose { READY,BATTING,SWING,PITCH,WINDUP,RUN,BASERUN,CATCH,PICKUP,HOLD,THROW }
    // The v22 sheets are prepared offline: the baked checkerboard background is
    // replaced with a real anti-aliased matte and every mascot row is scaled to a
    // common body height and footing. They are kept at full source resolution so
    // there is detail in hand for RENDER_SCALE; the running sheets are capped by
    // their own 181px source art and cannot go higher, which is why the batter
    // -- the largest actor on screen -- is drawn from the action atlas instead.
    // The original v16/v19/v21 sheets are still packaged and still used by the
    // roster screen and the atlas validation tests.
    private final Image atlas=load("mascot-action-atlas-v22.png");
    private final Image tanuki=loadTanuki();
    private final Image stadiumBackdrop=load("stadium-mascot-crowd-v12-source.png");
    private final Image catcher=load("circuit-catcher-v22.png");
    private final Image catcherFront=load("circuit-catcher-front-v22.png");
    private final Image running=load("mascot-running-atlas-v22.png");
    /**
     * Batting stance and swing, rebuilt so the batter -- the largest actor on
     * screen and the one on it for the whole at-bat -- is no longer the worst
     * art in the game. Both frames are normalised to one body height, one
     * foot baseline and one body centre inside a fixed cell, so the crop is
     * the raw cell and the batter cannot shift or resize between the two.
     */
    private final Image batting=load("mascot-batting-atlas-v25.png");
    /** Six empty-handed run frames per mascot, for batters running the bases. */
    private final Image baserunning=load("mascot-baserunning-atlas-v23.png");
    private final Map<String,Rectangle2D> crops=new HashMap<>();
    private final Map<Image,Image[]> mipChains=new HashMap<>();
    /** One team marker per actor drawn this frame, painted above every sprite. */
    private record Marker(double x,double y,double height,Color color){}
    private final List<Marker> markers=new ArrayList<>();
    private final Map<String,Double> headCentres=new HashMap<>();
    private final Map<Boolean,Image> stands=new HashMap<>();
    private final Map<ParkCamera.Mode,Image> settledFields=new HashMap<>();
    private static Image load(String name){
        var u=ParkSceneRenderer.class.getResource("/application/"+name);return u==null?null:new Image(u.toExternalForm());
    }
    private static Image loadTanuki(){
        Image original=load("turbo-tanuki-action-atlas-v22.png");if(original==null)return null;
        int width=(int)original.getWidth(),height=(int)original.getHeight();
        WritableImage clean=new WritableImage(original.getPixelReader(),width,height);
        // Pitch/contact artwork contains a detached illustration of a baseball.
        // The live physics ball is rendered separately, so keep the actor component.
        for(int frame:new int[]{3,6}){
            int sx=(int)Math.round((frame%4)*width/4.0),sy=(int)Math.round((frame/4)*height/4.0);
            int cw=(int)Math.round(((frame%4)+1)*width/4.0)-sx,ch=(int)Math.round(((frame/4)+1)*height/4.0)-sy;
            int[] components=new int[cw*ch],queue=new int[cw*ch];int id=0,largest=0,largestSize=0;
            for(int seed=0;seed<components.length;seed++){
                if(components[seed]!=0||(original.getPixelReader().getArgb(sx+seed%cw,sy+seed/cw)>>>24)<40)continue;
                id++;int head=0,count=1;queue[0]=seed;components[seed]=id;
                while(head<count){
                    int cell=queue[head++],x=cell%cw,y=cell/cw;
                    for(int direction=0;direction<4;direction++){
                        int nx=x+(direction==0?-1:direction==1?1:0),ny=y+(direction==2?-1:direction==3?1:0);
                        if(nx<0||nx>=cw||ny<0||ny>=ch)continue;int next=ny*cw+nx;
                        if(components[next]!=0||(original.getPixelReader().getArgb(sx+nx,sy+ny)>>>24)<40)continue;
                        components[next]=id;queue[count++]=next;
                    }
                }
                if(count>largestSize){largest=id;largestSize=count;}
            }
            for(int p=0;p<components.length;p++)if(components[p]!=largest)clean.getPixelWriter().setArgb(sx+p%cw,sy+p/cw,0);
        }
        return clean;
    }
    // The running and batting sheets used to be de-checkerboarded here at start
    // up with a hard on/off alpha test, which left jagged silhouettes and a pale
    // fringe. They now ship with a proper anti-aliased matte already applied.
    public boolean hasRunningAtlas(){return running!=null;}
    public Image runningAtlas(){return running;}

    /**
     * Pixels drawn per logical unit. The scene is authored against a fixed
     * 1180x650 coordinate space and then stretched to whatever size the window
     * is, so on a large display every logical pixel was being blown up and the
     * whole scene looked soft. Rendering the backing canvas at this multiple and
     * scaling the node down by the same amount keeps the coordinate space intact
     * while giving the display real pixels to work with.
     */
    public static final double RENDER_SCALE=chooseRenderScale();
    /**
     * At least two device pixels per logical unit, and more on a display wide
     * enough to show the scene larger than that, so a maximised window is
     * handed real pixels instead of a magnified 2x image. Capped at 3: the turf
     * is generated pixel by pixel at this scale, so the cost is quadratic.
     */
    private static double chooseRenderScale(){
        try{
            double width=javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
            return Math.max(2,Math.min(3,Math.ceil(width/VIEW_WIDTH)));
        }catch(RuntimeException noToolkit){return 2;}
    }
    public static final double VIEW_WIDTH=1180,VIEW_HEIGHT=650;

    public void field(GraphicsContext g,ParkCamera camera,BasePath path){
        markers.clear();
        if(camera.transitionProgress()>=.999999){
            Image terrain=settledFields.computeIfAbsent(camera.currentMode(),mode->{
                Canvas layer=new Canvas(VIEW_WIDTH*RENDER_SCALE,VIEW_HEIGHT*RENDER_SCALE);
                GraphicsContext lg=layer.getGraphicsContext2D();
                lg.scale(RENDER_SCALE,RENDER_SCALE);paintField(lg,camera,path,true);
                SnapshotParameters params=new SnapshotParameters();params.setFill(Color.TRANSPARENT);
                return layer.snapshot(params,null);
            });
            // cached at RENDER_SCALE, drawn back into the logical coordinate space
            g.drawImage(terrain,0,0,VIEW_WIDTH,VIEW_HEIGHT);
        }else paintField(g,camera,path,false);
    }

    /**
     * Wind moving over the turf. The ground itself is baked into a cached
     * snapshot because it is far too expensive to redraw per frame, so the
     * motion lives in a separate cheap layer: a handful of broad, soft patches
     * drifting across the outfield in world space, which the camera projects
     * like anything else on the ground plane. Stacked at very low alpha so it
     * reads as gusts catching the blades rather than shapes sliding over a
     * picture of grass.
     */
    public void wind(GraphicsContext g,ParkCamera camera,double clock){
        g.save();
        if(boundaryCache==null)boundaryCache=parkBoundary().toArray(WorldPoint[]::new);
        clip(g,camera,boundaryCache);
        for(int i=0;i<7;i++){
            double speed=150+28*(i%3),span=2900;
            // each gust crosses the park and wraps around behind the camera
            double travel=Math.floorMod((long)((clock*speed)+i*617),(long)span)-950;
            double drift=Math.sin(clock*.23+i*1.7)*70;
            WorldPoint centre=new WorldPoint(-620+travel,-560+i*230+drift);
            double lift=Math.sin(clock*.45+i)*.5+.5;
            Color tint=Color.rgb(214,236,168,.030+.016*lift);
            groundOval(g,camera,centre,520,168,0,tint);
            groundOval(g,camera,centre,360,116,0,tint);
            groundOval(g,camera,centre,190,64,0,tint);
        }
        g.restore();
    }

    private final Map<ParkCamera.Mode,Image> turfLayers=new HashMap<>();
    /** The park outline never changes; the wind layer clips against it every frame. */
    private WorldPoint[] boundaryCache;

    /** Integer hash; cheap enough to run a few million times while the field bakes. */
    private static double hash(int ix,int iy,int seed){
        int n=ix*374761393+iy*668265263+seed*1442695;
        n=(n^(n>>>13))*1274126177;
        return ((n^(n>>>16))&0xFFFF)/65535.0;
    }
    /** Smoothed value noise on the world-unit lattice. */
    private static double vnoise(double x,double y,int seed){
        int ix=(int)Math.floor(x),iy=(int)Math.floor(y);
        double fx=x-ix,fy=y-iy,ux=fx*fx*(3-2*fx),uy=fy*fy*(3-2*fy);
        double a=hash(ix,iy,seed),b=hash(ix+1,iy,seed),c=hash(ix,iy+1,seed),d=hash(ix+1,iy+1,seed);
        return (a*(1-ux)+b*ux)*(1-uy)+(c*(1-ux)+d*ux)*uy;
    }

    /**
     * Builds the turf for one camera, one pixel at a time, by asking the camera
     * which patch of ground each pixel lands on. Detail finer than a pixel can
     * resolve is faded out rather than drawn, so the far outfield settles into
     * smooth colour instead of boiling into noise. Baked once per camera and
     * cached; it never runs during play.
     */
    private Image turf(ParkCamera camera){
        return turfLayers.computeIfAbsent(camera.currentMode(),mode->{
            int w=(int)VIEW_WIDTH,h=(int)VIEW_HEIGHT;
            WritableImage image=new WritableImage(w,h);
            // filled into a buffer and handed over in one call; a per-pixel
            // setArgb costs more than the noise itself
            int[] buffer=new int[w*h];
            double[] px=new double[w+1],py=new double[w+1],qx=new double[w+1],qy=new double[w+1];
            for(int x=0;x<=w;x++){WorldPoint p=camera.unprojectGround(x,0);px[x]=p.x();py[x]=p.y();}
            for(int y=0;y<h;y++){
                for(int x=0;x<=w;x++){WorldPoint p=camera.unprojectGround(x,y+1);qx[x]=p.x();qy[x]=p.y();}
                for(int x=0;x<w;x++){
                    double wx=px[x],wy=py[x];
                    if(camera.depthAt(new WorldPoint(wx,wy))<=1)continue;
                    // world units covered by this pixel, horizontally and vertically
                    double foot=Math.max(1e-4,Math.hypot(px[x+1]-wx,py[x+1]-wy)+Math.hypot(qx[x]-wx,qy[x]-wy));
                    double value=.55*vnoise(wx/260,wy/260,1);
                    double d70=fade(70,foot);  if(d70>0)value+=.28*vnoise(wx/70,wy/70,2)*d70;
                    double d18=fade(18,foot);  if(d18>0)value+=.16*vnoise(wx/18,wy/18,3)*d18;
                    double blade=0,fine=0;
                    double db=fade(3.0,foot);  if(db>0)blade=(vnoise(wx/1.7,wy/5.5,4)-.5)*db;
                    double df=fade(1.5,foot);  if(df>0)fine=(vnoise(wx/.8,wy/2.2,5)-.5)*df;
                    // mow stripes: the mower bends blades toward or away from the
                    // camera in 18ft passes, and the edges are never crisp
                    double band=Math.sin(wy/72*Math.PI);
                    double stripe=Math.signum(band)*Math.pow(Math.abs(band),.35)*fade(60,foot);
                    // Deliberately clean and bright rather than photoreal: the
                    // look being chased here is an early Wii sports title, where
                    // the ground is bold flat colour with soft mown banding and
                    // almost no grain, and the character comes from the stylised
                    // blades drawn over it.
                    double mix=clamp01(value/.99*.66+.20+stripe*.15+blade*.15+fine*.06);
                    int r=(int)(58+(124-58)*mix+14*mix);
                    int gg=(int)(124+(186-124)*mix+8*mix);
                    int b=(int)(52+(84-52)*mix);
                    buffer[y*w+x]=0xFF000000|Math.min(255,r)<<16|Math.min(255,gg)<<8|Math.min(255,b);
                }
                double[] tx=px,ty=py;px=qx;py=qy;qx=tx;qy=ty;
            }
            PixelWriter out=image.getPixelWriter();
            out.setPixels(0,0,w,h,javafx.scene.image.PixelFormat.getIntArgbInstance(),buffer,0,w);
            return image;
        });
    }
    /** 1 where a feature of this size is comfortably resolvable, 0 where it would alias. */
    private static double fade(double wavelength,double footprint){
        return clamp01(wavelength/(footprint*2));
    }
    private static double clamp01(double v){return v<0?0:v>1?1:v;}

    /**
     * Short-lived dust and sparkles, held in world space so the camera places
     * and scales them like everything else standing on the field. Dust is kicked
     * up under running feet; sparkles fire on clean contact and on star moves.
     */
    private static final class Spark {
        double x,y,h,vx,vy,vh,age,life,size;Color color;boolean star;
    }
    private final List<Spark> sparks=new ArrayList<>();
    private final Random effectRandom=new Random(9137);

    /** Infield dust at ground level, under a running mascot. */
    public void puff(double wx,double wy) {
        if(sparks.size()>260)return;
        Spark s=new Spark();
        s.x=wx+(effectRandom.nextDouble()-.5)*5;s.y=wy+(effectRandom.nextDouble()-.5)*4;s.h=1;
        s.vx=(effectRandom.nextDouble()-.5)*18;s.vy=(effectRandom.nextDouble()-.5)*12;
        s.vh=9+effectRandom.nextDouble()*13;
        s.life=.42+effectRandom.nextDouble()*.34;s.size=3.4+effectRandom.nextDouble()*3.2;
        s.color=Color.rgb(212,186,148,.72);
        sparks.add(s);
    }
    /** A spinning star, for contact and star moves. */
    public void sparkle(double wx,double wy,double height,Color color,double spread) {
        if(sparks.size()>260)return;
        Spark s=new Spark();
        s.x=wx+(effectRandom.nextDouble()-.5)*spread;s.y=wy+(effectRandom.nextDouble()-.5)*spread*.6;
        s.h=height+(effectRandom.nextDouble()-.5)*spread*.5;
        s.vx=(effectRandom.nextDouble()-.5)*46;s.vy=(effectRandom.nextDouble()-.5)*30;
        s.vh=22+effectRandom.nextDouble()*30;
        s.life=.5+effectRandom.nextDouble()*.5;s.size=3.0+effectRandom.nextDouble()*2.6;
        s.color=color;s.star=true;
        sparks.add(s);
    }
    public void updateEffects(double elapsed) {
        if(!Double.isFinite(elapsed)||elapsed<=0)return;
        for(Spark s:sparks){
            s.age+=elapsed;
            s.x+=s.vx*elapsed;s.y+=s.vy*elapsed;s.h+=s.vh*elapsed;
            s.vh-=(s.star?38:25)*elapsed;
            if(s.h<0){s.h=0;s.vh=0;}
            s.vx*=1-Math.min(.9,2.2*elapsed);s.vy*=1-Math.min(.9,2.2*elapsed);
        }
        sparks.removeIf(s->s.age>=s.life);
    }
    public void drawEffects(GraphicsContext g,ParkCamera camera) {
        for(Spark s:sparks){
            WorldPoint ground=new WorldPoint(s.x,s.y);
            if(!camera.isVisible(ground))continue;
            WorldPoint p=camera.project(ground,s.h);
            double fade=Math.max(0,1-s.age/s.life);
            double scale=camera.scaleAt(ground);
            // dust swells as it disperses; stars hold their size and spin out
            double d=Math.max(1.3,scale*s.size*(s.star?1:1+(1-fade)*1.7));
            g.setFill(Color.color(s.color.getRed(),s.color.getGreen(),s.color.getBlue(),
                    s.color.getOpacity()*(s.star?Math.min(1,fade*1.6):fade*fade)));
            if(s.star)star(g,p.x(),p.y(),d,s.age*7.5);
            else g.fillOval(p.x()-d/2,p.y()-d/2,d,d);
        }
    }
    private void star(GraphicsContext g,double cx,double cy,double r,double spin) {
        double[] xs=new double[10],ys=new double[10];
        for(int i=0;i<10;i++){
            double a=-Math.PI/2+i*Math.PI/5+spin;
            double rr=(i%2==0)?r:r*.42;
            xs[i]=cx+Math.cos(a)*rr;ys[i]=cy+Math.sin(a)*rr;
        }
        g.fillPolygon(xs,ys,10);
    }

    private static final Color[] BLADE_BACK={Color.web("#3e6848"),Color.web("#3a6e4a")};
    private static final Color[] BLADE_FRONT={Color.web("#65a358"),Color.web("#76b464"),Color.web("#88c470")};

    /**
     * Stylised grass tufts: clumps of tapered blades standing on the ground and
     * swaying, drawn in world space so the camera handles their perspective.
     * They are kept to the outfield beyond the infield arc and off the warning
     * track, both because a groundskeeper would never let them grow around the
     * bases and because nothing should sprout in front of the plate while a play
     * is live. Far tufts are dropped once they project to less than a few pixels,
     * which is what keeps the count low enough to redraw every frame.
     */
    public void grass(GraphicsContext g,ParkCamera camera,double clock){
        g.save();
        if(boundaryCache==null)boundaryCache=parkBoundary().toArray(WorldPoint[]::new);
        clip(g,camera,boundaryCache);
        int drawn=0;
        // From the low batting and pitching angles the whole outfield collapses
        // into a thin band near the horizon, and full density piles up there as
        // a solid hedge. Those views get a quarter of the tufts.
        int step=camera.currentMode()==ParkCamera.Mode.FIELDING?52:104;
        for(int gx=-1400;gx<=1400&&drawn<TUFT_BUDGET;gx+=step){
            for(int gy=-1560;gy<=100&&drawn<TUFT_BUDGET;gy+=step){
                // Grass grows in patches, not as an even rash across the whole
                // outfield, so a coarse cell decides whether this area has any
                // at all before the fine cell places one.
                if(hash(Math.floorDiv(gx,156),Math.floorDiv(gy,156),21)<.42)continue;
                double jx=hash(gx,gy,11),jy=hash(gx,gy,12);
                WorldPoint base=new WorldPoint(560+gx+jx*52-26,610+gy+jy*52-26);
                double reach=FieldGeometry.distanceFromHome(base);
                if(reach<430||!FieldGeometry.isFair(base))continue;   // infield stays mown
                double outer=FieldGeometry.fenceRadius(base)-72;
                if(reach>outer)continue;                              // clear of the track
                // thin out toward both edges so the grass fades in and out
                // instead of stopping along a hard circle
                double edge=Math.min(Math.min((reach-430)/150,(outer-reach)/150),1);
                if(hash(gx,gy,16)>Math.max(0,edge))continue;
                if(camera.depthAt(base)<=20)continue;
                WorldPoint foot=camera.project(base,0);
                if(foot.x()<-80||foot.x()>1260||foot.y()<-40||foot.y()>720)continue;
                // A fielder stands 24 units tall, so the old 24-40 made grass as
                // tall as the players: giant spikes behind every play. Ankle to
                // shin height reads as long grass at this camera distance.
                double tall=7+hash(gx,gy,13)*6;
                double h=foot.y()-camera.project(base,tall).y();
                if(h<3.5)continue;                                    // too far to read
                double phase=hash(gx,gy,14)*6.28;
                double sway=Math.sin(clock*1.7+phase)*h*.17+Math.sin(clock*.7+phase*1.9)*h*.06;
                blades(g,foot.x(),foot.y(),h,sway,hash(gx,gy,15));
                drawn++;
            }
        }
        g.restore();
    }
    private static final int TUFT_BUDGET=900;

    /** One clump: a dark blade set behind a brighter one, as in hand-drawn tufts. */
    private void blades(GraphicsContext g,double x,double y,double h,double sway,double seed){
        int count=3+(int)(seed*3);
        for(int i=0;i<count;i++){
            double t=count==1?.5:(double)i/(count-1);
            double lean=(t-.5)*h*.62;
            boolean back=i==0||i==count-1;
            double length=h*(back?.72+seed*.18:.92+t*.12);
            double width=Math.max(.9,h*(back?.085:.105));
            double tipX=x+lean+sway*(back?.72:1.0);
            double tipY=y-length;
            g.setFill(back?BLADE_BACK[(int)(seed*BLADE_BACK.length)%BLADE_BACK.length]
                         :BLADE_FRONT[(i+(int)(seed*3))%BLADE_FRONT.length]);
            // broad at the root, tapering to a point, like a drawn leaf
            g.fillPolygon(new double[]{x-width,x+width,tipX},new double[]{y,y,tipY},3);
        }
    }

    private void paintField(GraphicsContext g,ParkCamera camera,BasePath path,boolean detailed){
        boolean backstop=camera.currentMode()==ParkCamera.Mode.PITCHING;
        List<WorldPoint> boundary=parkBoundary();
        if(camera.currentMode()==ParkCamera.Mode.FIELDING){
            g.setFill(new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#071a45")),new Stop(1,Color.web("#102e53"))));
            g.fillRect(0,0,1180,650);overheadStands(g,camera,boundary);
        }else{
            Image background=stands.computeIfAbsent(backstop,this::makeStands);
            g.drawImage(background,0,0,VIEW_WIDTH,VIEW_HEIGHT);
        }
        drawCrowdPlate(g,camera,boundary);
        polygon(g,camera,Color.web("#2d5f2a"),boundary.toArray(WorldPoint[]::new));
        g.save();
        clip(g,camera,boundary.toArray(WorldPoint[]::new));
        // Real turf, sampled per pixel on the ground plane. Flat bands of colour
        // with a scatter of hairline marks read as coloured paper at any size;
        // this puts blade grain, clumping and soft-edged mow stripes on the
        // ground itself so they compress with distance like the rest of the park.
        if(detailed)g.drawImage(turf(camera),0,0,VIEW_WIDTH,VIEW_HEIGHT);
        else for(int y=-1100;y<1000;y+=72){
            polygon(g,camera,Color.web(Math.floorMod(y/72,2)==0?"#33682e":"#3d7736"),
                new WorldPoint(-1800,y),new WorldPoint(2900,y),new WorldPoint(2900,y+72),new WorldPoint(-1800,y+72));
        }
        // A restrained floodlight wash makes the near grass warmer and brighter.
        g.setFill(new LinearGradient(0,.20,0,1,true,CycleMethod.NO_CYCLE,
                new Stop(0,Color.rgb(18,45,93,.14)),new Stop(.7,Color.rgb(255,235,142,.04)),new Stop(1,Color.rgb(255,219,105,.14))));
        g.fillRect(0,0,1180,650);
        Random marks=new Random(731);
        // Warning track: a band of clay just inside the fence, which is one of
        // the strongest cues that a field is a real ballpark rather than a flat
        // green sheet with a diamond painted on it.
        List<WorldPoint> track=new ArrayList<>();
        for(int n=0;n<=48;n++){
            double a=-Math.PI/4+Math.PI/2*n/48,r=FieldGeometry.fenceRadius(a);
            track.add(new WorldPoint(560+Math.sin(a)*r,610-Math.cos(a)*r));
        }
        for(int n=48;n>=0;n--){
            double a=-Math.PI/4+Math.PI/2*n/48,r=FieldGeometry.fenceRadius(a)-60;
            track.add(new WorldPoint(560+Math.sin(a)*r,610-Math.cos(a)*r));
        }
        polygon(g,camera,Color.web("#8d6144"),track.toArray(WorldPoint[]::new));

        List<WorldPoint> dirt=new ArrayList<>();dirt.add(new WorldPoint(560,628));dirt.add(new WorldPoint(FieldGeometry.FIRST.x()+20,FieldGeometry.FIRST.y()+10));
        for(int n=0;n<=32;n++){
            double a=Math.toRadians(64-128.0*n/32);
            dirt.add(new WorldPoint(560+Math.sin(a)*390,368-Math.cos(a)*390));
        }
        dirt.add(new WorldPoint(FieldGeometry.THIRD.x()-20,FieldGeometry.THIRD.y()+10));
        polygon(g,camera,Color.web("#96643f"),dirt.toArray(WorldPoint[]::new));
        g.save();clip(g,camera,dirt.toArray(WorldPoint[]::new));
        for(int i=0;i<700;i++){
            WorldPoint p=new WorldPoint(160+marks.nextDouble()*800,-40+marks.nextDouble()*680);
            line(g,camera,p,new WorldPoint(p.x()+2.5,p.y()),Color.rgb(79,43,27,.20),.7);
        }
        g.restore();
        WorldPoint center=new WorldPoint(560,355.4415588);
        polygon(g,camera,Color.web("#36702f"),FieldGeometry.HOME.interpolate(center,.19),
                FieldGeometry.FIRST.interpolate(center,.16),FieldGeometry.SECOND.interpolate(center,.18),FieldGeometry.THIRD.interpolate(center,.16));
        groundOval(g,camera,FieldGeometry.HOME,52,52,0,Color.web("#8f5f3c"));
        groundOval(g,camera,FieldGeometry.MOUND,36,36,0,Color.web("#87573a"));
        groundOval(g,camera,FieldGeometry.MOUND,34,34,2.8,Color.web("#9d6a45"));
        line(g,camera,FieldGeometry.HOME,FieldGeometry.fencePoint(-Math.PI/4),Color.web("#fff2c6"),1.6);
        line(g,camera,FieldGeometry.HOME,FieldGeometry.fencePoint(Math.PI/4),Color.web("#fff2c6"),1.6);
        box(g,camera,542,598,16,24);box(g,camera,562,598,16,24);
        for(Base base:new Base[]{Base.FIRST,Base.SECOND,Base.THIRD}){
            WorldPoint p=path.point(base);
            polygon(g,camera,Color.web("#fff4c7"),new WorldPoint(p.x(),p.y()-3.54),new WorldPoint(p.x()+3.54,p.y()),
                new WorldPoint(p.x(),p.y()+3.54),new WorldPoint(p.x()-3.54,p.y()));
        }
        polygon(g,camera,Color.web("#fff8d8"),new WorldPoint(557.167,607.167),new WorldPoint(562.833,607.167),
            new WorldPoint(562.833,610),new WorldPoint(560,612.833),new WorldPoint(557.167,610));
        raisedPolygon(g,camera,Color.web("#fff4c7"),3,new WorldPoint(556,367),new WorldPoint(564,367),new WorldPoint(564,369),new WorldPoint(556,369));
        g.restore();
        // Outfield fence in the batting view; the opposite view reveals the backstop.
        for(int i=0;i<boundary.size();i++){
            WorldPoint a=boundary.get(i),b=boundary.get((i+1)%boundary.size());
            if(!camera.isVisible(a)||!camera.isVisible(b))continue;
            WorldPoint qa=camera.project(a,0),qb=camera.project(b,0);
            if(Math.max(qa.y(),qb.y())>360&&camera.currentMode()!=ParkCamera.Mode.FIELDING)continue;
            wall(g,camera,a,b);
        }
        if(backstop)drawBackstop(g,camera);
        else{
            for(double angle:new double[]{-Math.PI/4,Math.PI/4}){
                WorldPoint p=FieldGeometry.fencePoint(angle);
                vertical(g,camera,p,0,100,Color.web("#f5ca59"),2);
            }
            WorldPoint mark=camera.project(FieldGeometry.fencePoint(0),15);
            if(mark.y()>10&&mark.y()<400){
                g.setFont(Font.font("Segoe UI",FontWeight.BOLD,10));g.setFill(Color.web("#fff0ad"));
                g.fillText("400",mark.x()-9,mark.y());
            }
        }
    }
    private void drawCrowdPlate(GraphicsContext g,ParkCamera camera,List<WorldPoint> boundary){
        if(stadiumBackdrop==null||stadiumBackdrop.isError())return;
        // Fit the stands above the projected fence, before drawing the ground.
        // A fixed screen overlay would cover the ground under distant players.
        double fenceY=650;
        for(WorldPoint point:boundary)if(camera.isVisible(point))
            fenceY=Math.min(fenceY,camera.project(point,32).y());
        fenceY=Math.max(70,Math.min(400,fenceY));
        g.save();
        g.setImageSmoothing(true);
        double cropHeight=Math.min(stadiumBackdrop.getHeight()*.505,
            fenceY*stadiumBackdrop.getWidth()/1180);
        double cropTop=stadiumBackdrop.getHeight()*.505-cropHeight;
        g.drawImage(stadiumBackdrop,0,cropTop,stadiumBackdrop.getWidth(),cropHeight,0,0,1180,fenceY);
        g.restore();
    }
    private void overheadStands(GraphicsContext g,ParkCamera camera,List<WorldPoint> boundary){
        for(int row=8;row>=0;row--){
            List<WorldPoint> ring=new ArrayList<>();
            double expansion=1.03+row*.035;
            for(WorldPoint p:boundary)ring.add(new WorldPoint(560+(p.x()-560)*expansion,610+(p.y()-610)*expansion));
            polygon(g,camera,Color.web(row%2==0?"#123961":"#0a254d"),ring.toArray(WorldPoint[]::new));
        }
        for(int row=0;row<6;row++)for(int i=0;i<boundary.size();i++){
            WorldPoint p=boundary.get(i);double expansion=1.05+row*.04;
            WorldPoint seat=new WorldPoint(560+(p.x()-560)*expansion,610+(p.y()-610)*expansion);
            if(!camera.isVisible(seat))continue;
            WorldPoint s=camera.project(seat,0);
            Mascot fan=Mascot.values()[(i+row*3)%Mascot.values().length];
            Color kit=Color.web(fan.getPrimaryColor());
            double size=2.8+row*.25;
            // Compact mascot silhouettes keep the bowl populated at the
            // overhead scale without introducing photographic people.
            g.setFill(Color.color(kit.getRed(),kit.getGreen(),kit.getBlue(),.88));
            g.fillOval(s.x()-size*.5,s.y()-size*.72,size,size);
            g.fillRoundRect(s.x()-size*.72,s.y()+size*.05,size*1.44,size*.72,size*.35,size*.35);
            g.setFill(Color.rgb(255,213,119,.70));
            g.fillOval(s.x()-size*.16,s.y()-size*.38,size*.12,size*.12);
        }
    }
    private List<WorldPoint> parkBoundary(){
        List<WorldPoint> p=new ArrayList<>();
        for(int i=0;i<100;i++){
            double a=-Math.PI+2*Math.PI*i/100,ab=Math.abs(a),r;
            if(ab<=Math.PI/4)r=FieldGeometry.fenceRadius(a);
            else if(ab<Math.PI/2)r=1320-(1320-235)*(ab-Math.PI/4)/(Math.PI/4);
            else r=235;
            p.add(new WorldPoint(560+Math.sin(a)*r,610-Math.cos(a)*r));
        }
        return p;
    }
    private Image makeStands(boolean backstop){
        Canvas canvas=new Canvas(VIEW_WIDTH*RENDER_SCALE,VIEW_HEIGHT*RENDER_SCALE);
        GraphicsContext g=canvas.getGraphicsContext2D();
        g.scale(RENDER_SCALE,RENDER_SCALE);
        // The supplied high-resolution plate is the single source of truth for
        // the night palette, lights, skyline, and mascot-only crowd.  Draw the
        // complete plate so the broadcast view retains depth behind the live
        // field geometry; paintField() then replaces only the playable grass
        // and dirt while leaving the stands and crowd intact.
        if(stadiumBackdrop!=null&&!stadiumBackdrop.isError()){
            g.setImageSmoothing(true);
            g.drawImage(stadiumBackdrop,0,0,stadiumBackdrop.getWidth(),stadiumBackdrop.getHeight(),0,0,1180,650);
            // A very light tint keeps labels and the live sprites readable
            // without washing out the reference artwork's exact colors.
            g.setFill(Color.rgb(3,13,43,.10));g.fillRect(0,0,1180,650);
            SnapshotParameters hiRes=new SnapshotParameters();hiRes.setFill(Color.TRANSPARENT);
            return canvas.snapshot(hiRes,null);
        }
        g.setFill(new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#07143d")),new Stop(.45,Color.web("#112b5c")),new Stop(1,Color.web("#174866"))));
        g.fillRect(0,0,1180,650);
        Random random=new Random(backstop?318:732);
        for(int i=0;i<25;i++){
            double x=i*50-5,h=20+random.nextInt(42),y=132-h;
            g.setFill(Color.rgb(8,24,65,.78));g.fillRect(x,y,35,h+50);
            for(int k=0;k<8;k++){g.setFill(Color.rgb(255,194,87,.55));g.fillRect(x+5+(k%3)*9,y+8+(k/3)*10,3,3);}
        }
        double roof=backstop?100:116;
        for(int tier=0;tier<3;tier++){
            double y=roof+tier*42;
            g.setFill(Color.web(tier==0?"#153d62":"#102f54"));
            g.fillPolygon(new double[]{0,260,590,920,1180,1180,0},new double[]{y-26,y-3,y+14,y-3,y-26,y+53,y+53},7);
            g.setStroke(Color.rgb(255,183,83,.64));g.setLineWidth(3);g.strokeLine(0,y+29,1180,y+29);
            for(int row=0;row<4;row++)for(int col=0;col<154;col++){
                double x=col*8-4+(row%2)*3,arc=25*(1-Math.pow((x-590)/590,2)),cy=y-18+row*9+arc;
                if(random.nextDouble()<.08)continue;
                Mascot m=Mascot.values()[random.nextInt(Mascot.values().length)];
                if(atlas!=null){
                    Rectangle2D crop=crop(atlas,0,m.ordinal(),atlas.getWidth()/4,atlas.getHeight()/12);
                    double sh=crop.getHeight()*.40;
                    g.setGlobalAlpha(.52+tier*.08);
                    drawMipped(g,atlas,crop.getMinX()+crop.getWidth()*.17,crop.getMinY(),
                        crop.getWidth()*.66,sh,x,cy,7.4,8.6,1);
                    g.setGlobalAlpha(1);
                }
            }
        }
        g.setFill(Color.rgb(3,15,48,.28));g.fillRect(0,70,1180,180);
        if(!backstop){
            g.setFill(Color.web("#08183f"));g.fillRoundRect(472,77,236,72,5,5);
            g.setStroke(Color.web("#f3b54e"));g.setLineWidth(1);g.strokeRoundRect(477,82,226,62,3,3);
            g.setFont(Font.font("Segoe UI",FontWeight.BOLD,12));g.setFill(Color.web("#ffe58c"));g.fillText("MASCOT BASEBALL",545,100);
            g.setFont(Font.font("Segoe UI",FontWeight.NORMAL,10));g.setFill(Color.web("#75f3df"));g.fillText("GRAND PRIX STADIUM",545,127);
        }else{
            g.setFont(Font.font("Segoe UI",FontWeight.BOLD,17));g.setFill(Color.web("#ffe58c"));
            g.fillText("HOME OF THE MASCOTS",486,125);
        }
        for(double x:new double[]{145,1035}){
            g.setStroke(Color.rgb(70,126,158,.86));g.setLineWidth(3);g.strokeLine(x,18,x-8,156);g.strokeLine(x+18,18,x+23,156);
            g.setLineWidth(1);for(int k=0;k<5;k++){g.strokeLine(x,40+k*20,x+20,60+k*20);g.strokeLine(x+20,40+k*20,x,60+k*20);}
            g.setFill(new RadialGradient(0,0,x+10,24,66,false,CycleMethod.NO_CYCLE,new Stop(0,Color.rgb(255,237,167,.42)),new Stop(1,Color.TRANSPARENT)));
            g.fillOval(x-56,-42,132,132);
            g.setFill(Color.web("#16294e"));g.fillRect(x-18,8,64,24);
            g.setFill(Color.web("#fff0ae"));for(int k=0;k<12;k++)g.fillOval(x-13+(k%6)*9,11+(k/6)*10,6,6);
        }
        SnapshotParameters s=new SnapshotParameters();s.setFill(Color.TRANSPARENT);return canvas.snapshot(s,null);
    }
    private void wall(GraphicsContext g,ParkCamera c,WorldPoint a,WorldPoint b){
        WorldPoint a0=c.project(a,0),b0=c.project(b,0),at=c.project(a,32),bt=c.project(b,32);
        g.setFill(Color.web("#0b5d4a"));g.fillPolygon(new double[]{a0.x(),b0.x(),bt.x(),at.x()},new double[]{a0.y(),b0.y(),bt.y(),at.y()},4);
        g.setStroke(Color.web("#f4c454"));g.setLineWidth(2);g.strokeLine(at.x(),at.y(),bt.x(),bt.y());
        g.setStroke(Color.rgb(26,58,46,.42));g.setLineWidth(1);g.strokeLine(a0.x(),a0.y(),at.x(),at.y());
    }
    private void drawBackstop(GraphicsContext g,ParkCamera c){
        WorldPoint left=new WorldPoint(450,820),right=new WorldPoint(670,820);
        for(int i=0;i<=16;i++){WorldPoint p=left.interpolate(right,i/16.0);vertical(g,c,p,32,95,Color.rgb(180,195,177,.25),.6);}
        for(int h=34;h<=94;h+=10){
            WorldPoint a=c.project(left,h),b=c.project(right,h);g.setStroke(Color.rgb(180,195,177,.22));g.setLineWidth(.6);g.strokeLine(a.x(),a.y(),b.x(),b.y());
        }
        vertical(g,c,left,0,98,Color.web("#7c9483"),2);vertical(g,c,right,0,98,Color.web("#7c9483"),2);
    }
    private void box(GraphicsContext g,ParkCamera c,double x,double y,double w,double h){
        WorldPoint[] p={new WorldPoint(x,y),new WorldPoint(x+w,y),new WorldPoint(x+w,y+h),new WorldPoint(x,y+h)};
        for(int i=0;i<4;i++)line(g,c,p[i],p[(i+1)%4],Color.web("#f2e6c8"),1.35);
    }
    private List<WorldPoint> nearClip(ParkCamera c,WorldPoint[] input){
        List<WorldPoint> out=new ArrayList<>();if(input.length==0)return out;
        WorldPoint a=input[input.length-1];double da=c.depthAt(a);
        for(WorldPoint b:input){
            double db=c.depthAt(b);
            if((da>8)!=(db>8))out.add(a.interpolate(b,(8-da)/(db-da)));
            if(db>8)out.add(b);a=b;da=db;
        }
        return out;
    }
    private void polygon(GraphicsContext g,ParkCamera c,Color color,WorldPoint...points){
        List<WorldPoint> p=nearClip(c,points);if(p.size()<3)return;
        double[] xs=new double[p.size()],ys=new double[p.size()];
        for(int i=0;i<p.size();i++){WorldPoint q=c.project(p.get(i),0);xs[i]=q.x();ys[i]=q.y();}
        g.setFill(color);g.fillPolygon(xs,ys,p.size());
    }
    private void raisedPolygon(GraphicsContext g,ParkCamera c,Color color,double height,WorldPoint...p){
        if(p.length<3)return;for(WorldPoint v:p)if(!c.isVisible(v))return;
        double[] xs=new double[p.length],ys=new double[p.length];for(int i=0;i<p.length;i++){WorldPoint q=c.project(p[i],height);xs[i]=q.x();ys[i]=q.y();}
        g.setFill(color);g.fillPolygon(xs,ys,p.length);
    }
    private void clip(GraphicsContext g,ParkCamera c,WorldPoint...points){
        List<WorldPoint> p=nearClip(c,points);g.beginPath();
        for(int i=0;i<p.size();i++){WorldPoint q=c.project(p.get(i),0);if(i==0)g.moveTo(q.x(),q.y());else g.lineTo(q.x(),q.y());}
        g.closePath();g.clip();
    }
    private void line(GraphicsContext g,ParkCamera c,WorldPoint a,WorldPoint b,Color color,double width){
        double da=c.depthAt(a),db=c.depthAt(b);if(da<=8&&db<=8)return;
        if(da<=8)a=a.interpolate(b,(8-da)/(db-da));else if(db<=8)b=a.interpolate(b,(8-da)/(db-da));
        WorldPoint x=c.project(a,0),y=c.project(b,0);g.setStroke(color);g.setLineWidth(width);g.strokeLine(x.x(),x.y(),y.x(),y.y());
    }
    private void vertical(GraphicsContext g,ParkCamera c,WorldPoint p,double low,double high,Color color,double width){
        if(!c.isVisible(p))return;WorldPoint a=c.project(p,low),b=c.project(p,high);g.setStroke(color);g.setLineWidth(width);g.strokeLine(a.x(),a.y(),b.x(),b.y());
    }
    private void groundOval(GraphicsContext g,ParkCamera c,WorldPoint p,double rx,double ry,double height,Color color){
        WorldPoint[] points=new WorldPoint[48];for(int i=0;i<48;i++){double a=i*Math.PI/24;points[i]=new WorldPoint(p.x()+Math.cos(a)*rx,p.y()+Math.sin(a)*ry);}
        if(height==0)polygon(g,c,color,points);else raisedPolygon(g,c,color,height,points);
    }

    public static WorldPoint releaseGround(WorldPoint feet,boolean facingRight){return new WorldPoint(feet.x()+(facingRight?4:-4),feet.y());}
    public static double releaseHeight(double actorHeight){return actorHeight*.82;}
    public static WorldPoint gloveGround(WorldPoint feet,boolean facingRight){return new WorldPoint(feet.x()+(facingRight?3:-3),feet.y()-1);}
    public static double gloveHeight(double actorHeight){return actorHeight*.52;}

    /** Complete character frames share a stable cropped foot baseline. */
    public void mascot(GraphicsContext g,ParkCamera camera,Mascot mascot,WorldPoint feet,Pose pose,
                       boolean facingRight,double worldHeight,double clock,String label){
        mascot(g,camera,mascot,feet,pose,facingRight,worldHeight,clock,label,null);
    }
    /** Render an actor with the shared team kit while retaining its mascot art. */
    public void mascot(GraphicsContext g,ParkCamera camera,Mascot mascot,WorldPoint feet,Pose pose,
                       boolean facingRight,double worldHeight,double clock,String label,Color kitColor){
        if(!camera.isVisible(feet))return;
        double surface=FieldGeometry.surfaceHeight(feet);
        WorldPoint p=camera.project(feet,surface);
        double h=p.y()-camera.project(feet,surface+worldHeight).y();
        if(h<1||p.x()<-180||p.x()>1360||p.y()>800||p.y()<-80)return;
        Image source=atlas;int col=pose==Pose.RUN||pose==Pose.BASERUN?3:pose==Pose.PITCH||pose==Pose.THROW?1:pose==Pose.SWING?2:0;
        int row=mascot.ordinal();double cw=source==null?160:source.getWidth()/4,ch=source==null?192:source.getHeight()/12;
        // Sheets whose row holds several poses of one character share that row's
        // bounds; the catcher art is a single pinned frame and keeps its own.
        int columns=4;boolean sharedRow=true,cycle=false;double frameSeconds=.09;
        // Eleven mascots have one bat frame each, so their swing is animated
        // below. Turbo Tanuki has a sheet of its own with a second, authored
        // swing frame, and that drawn frame is used instead.
        boolean syntheticSwing=true;
        if(mascot==Mascot.TURBO_TANUKI&&tanuki!=null){
            source=tanuki;cw=source.getWidth()/4;ch=source.getHeight()/4;
            int index=switch(pose){case BATTING->4;case SWING->6;case PITCH->3;case RUN,BASERUN->8;case CATCH->12;case PICKUP->11;case THROW->13;default->1;};
            col=index%4;row=index/4;syntheticSwing=false;
        }
        if(pose==Pose.CATCH&&mascot==Mascot.CIRCUIT_BOT&&catcher!=null&&camera.currentMode()!=ParkCamera.Mode.PITCHING){
            source=catcher;cw=source.getWidth()/4;ch=source.getHeight()/4;col=0;row=0;sharedRow=false;
        }
        if(pose==Pose.CATCH&&mascot==Mascot.CIRCUIT_BOT&&camera.currentMode()==ParkCamera.Mode.PITCHING&&catcherFront!=null){
            source=catcherFront;cw=source.getWidth();ch=source.getHeight();col=0;row=0;sharedRow=false;
        }
        // Base runners use the same four-frame cycle as fielders so their feet
        // actually alternate; the action sheet's single empty-handed run frame
        // could not stride. The cycle is driven by distance travelled, so the
        // legs keep pace with how fast the runner is covering ground.
        // A batter running the bases carries no glove, and the dedicated
        // baserunning sheet has six empty-handed frames per mascot instead of
        // the fielder cycle's four, so the stride reads smoother as well.
        boolean baseRunSheet=pose==Pose.BASERUN&&baserunning!=null;
        if(baseRunSheet){
            source=baserunning;cw=source.getWidth()/6;ch=source.getHeight()/12;
            frameSeconds=.06;
            col=Math.floorMod((int)Math.floor(clock/frameSeconds),6);
            row=mascot.ordinal();columns=6;sharedRow=true;cycle=true;
        }
        // fielders chase the ball with a glove, and fall back to that cycle if
        // the baserunning sheet is ever missing
        boolean runSheet=(pose==Pose.RUN||(pose==Pose.BASERUN&&baserunning==null))&&running!=null;
        if(runSheet){source=running;cw=source.getWidth()/4;ch=source.getHeight()/12;col=Math.floorMod((int)Math.floor(clock/.09),4);row=mascot.ordinal();columns=4;sharedRow=true;cycle=true;}
        // The batting sheet's frames are pre-normalised, so its crop is the whole
        // cell: the body is already centred over the feet and the bat overhang is
        // padding. Deriving the box from the content instead would centre the
        // swing on its outstretched bat and shove the batter off his own anchor.
        boolean battingPose=pose==Pose.BATTING||pose==Pose.SWING;
        boolean rawCell=false;
        if(battingPose&&batting!=null){
            source=batting;cw=source.getWidth()/2;ch=source.getHeight()/12;
            col=pose==Pose.SWING?1:0;row=mascot.ordinal();rawCell=true;syntheticSwing=true;
        }
        if(source==null)return;
        Rectangle2D crop=rawCell?new Rectangle2D(col*cw,row*ch,cw,ch)
            :sharedRow?rowCrop(source,col,row,cw,ch,columns,cycle):crop(source,col,row,cw,ch);
        double w=h*crop.getWidth()/crop.getHeight();
        // Floodlights cast a restrained directionally offset world-ground shadow.
        WorldPoint shadeGround=new WorldPoint(feet.x()+worldHeight*.28,feet.y()+worldHeight*.17);
        WorldPoint shade=camera.project(shadeGround,FieldGeometry.surfaceHeight(shadeGround));
        g.setFill(Color.rgb(22,32,18,.12));
        g.fillPolygon(new double[]{p.x()-w*.20,p.x()+w*.20,shade.x()+w*.24,shade.x()-w*.24},
            new double[]{p.y(),p.y(),shade.y()+h*.025,shade.y()+h*.025},4);
        g.setFill(Color.rgb(20,29,17,.26));g.fillOval(p.x()-w*.26,p.y()-Math.max(1,h*.012),w*.52,Math.max(2,h*.026));
        // Four drawn frames read as a much longer cycle with a stride-synced
        // hop between them: the body lifts on the drive and settles on the
        // landing, twice per cycle, which is the motion the missing in-between
        // frames would have carried.
        double bob=cycle?Math.abs(Math.sin(clock/frameSeconds*Math.PI))*h*.035:0;
        g.save();g.translate(p.x(),p.y()-bob);g.scale(facingRight?1:-1,1);
        // A touch of squash and stretch over the drawn contact frame: the feet
        // stay planted, the shoulders lead and the bat -- part of the sprite --
        // sweeps with them, so the swing has follow-through rather than being one
        // held picture. For Pose.SWING the clock carries swing progress, 0 at the
        // load and 1 at the finish.
        if(pose==Pose.SWING&&syntheticSwing){
            double sweep=Math.sin(Math.PI*clamp01(clock*1.15));
            g.transform(1+.06*sweep,0,-.11*sweep,1-.03*sweep,0,0);
        }
        drawMipped(g,source,crop.getMinX(),crop.getMinY(),crop.getWidth(),crop.getHeight(),
            -w/2,-h,w,h,RENDER_SCALE);
        if(kitColor!=null){
            // The crop box is the tallest pose in the row (or, for the batting
            // cell, includes the bat overhang as padding), so its top edge is
            // well above this pose's head. Offset by the difference between this
            // frame's own content top and the box top to land on the head itself.
            Rectangle2D own=crop(source,col,row,cw,ch);
            double head=p.y()-h+Math.max(0,own.getMinY()-crop.getMinY())/crop.getHeight()*h;
            // And sideways onto the head rather than the middle of the box: a
            // tail or an outstretched bat drags the box centre off the mascot,
            // which put the marker beside the head instead of over it.
            double drift=(headCentre(source,col,row,cw,ch)-(crop.getMinX()+crop.getWidth()/2))
                    /crop.getWidth()*w*(facingRight?1:-1);
            markers.add(new Marker(p.x()+drift,head,h,kitColor));
        }
        g.restore();
        if(label!=null&&!label.isBlank()&&h>25)tag(g,p.x(),p.y()-h-19,label);
    }
    /**
     * A downward-pointing marker over the actor's head identifying its side.
     * <p>Drawn in its own pass after every sprite, so a marker is never painted
     * over by a mascot standing in front of its owner. It also used to sit at
     * the enemy's throwing hand rather than the head, where it landed inside
     * the body on half the poses and read as part of the sprite. This is the
     * only team-colour indicator drawn; earlier revisions also painted a
     * solid/pinstriped kit patch over the torso and legs, but it was dropped
     * in favour of just this pointer.
     */
    public void drawTeamPointers(GraphicsContext g){
        for(Marker m:markers){
            double size=Math.max(11,m.height()*.20);
            double cx=m.x(),tipY=m.y()-m.height()*.04;
            double[] xs={cx,cx-size*.45,cx+size*.45},ys={tipY,tipY-size*.9,tipY-size*.9};
            g.setFill(Color.rgb(14,16,22,.28));
            g.fillPolygon(new double[]{cx+size*.08,cx-size*.37,cx+size*.53},
                          new double[]{tipY+size*.08,tipY-size*.82,tipY-size*.82},3);
            g.setFill(m.color());g.fillPolygon(xs,ys,3);
            g.setStroke(Color.rgb(250,252,255,.72));g.setLineWidth(Math.max(1,size*.10));
            g.strokePolygon(xs,ys,3);
        }
        markers.clear();
    }
    /**
     * Content bounds shared by every frame in one row, so a character keeps a
     * constant height and footing as it changes pose or animation frame.
     * Cropping each cell to its own content instead (as {@link #crop} does)
     * stretches whatever that single pose happens to occupy up to the full actor
     * height, so a crouched run frame came out the same size as an upright
     * stance and the mascot appeared to grow and shrink as it moved.
     */
    private Rectangle2D rowCrop(Image source,int col,int row,double cw,double ch,int columns,boolean cycle){
        String key="row:"+System.identityHashCode(source)+":"+row;
        Rectangle2D union=crops.get(key);
        if(union==null){
            double left=cw,top=ch,right=0,bottom=0;
            for(int frame=0;frame<columns;frame++){Rectangle2D r=crop(source,frame,row,cw,ch);
                left=Math.min(left,r.getMinX()-frame*cw);right=Math.max(right,r.getMaxX()-frame*cw);
                top=Math.min(top,r.getMinY()-row*ch);bottom=Math.max(bottom,r.getMaxY()-row*ch);}
            union=new Rectangle2D(left,top,right-left,bottom-top);
            crops.put(key,union);
        }
        // Frames of one animation share the whole box. Their content box breathes
        // as arms and tails swing - up to 21px of width and 10px of centre on
        // Circuit Bot - and re-centring every frame made the body pump and slide
        // as it ran. A fixed box keeps the run glued together.
        if(cycle)return new Rectangle2D(col*cw+union.getMinX(),row*ch+union.getMinY(),union.getWidth(),union.getHeight());
        // Separate poses are a different case: they change rarely, so each keeps
        // its own horizontal bounds and stays centred over its feet. The batting
        // sheet's swing reaches far out with the bat, and folding that into a
        // shared box shoved the standing batter sideways off his own anchor.
        Rectangle2D own=crop(source,col,row,cw,ch);
        return new Rectangle2D(own.getMinX(),row*ch+union.getMinY(),own.getWidth(),union.getHeight());
    }
    /**
     * Horizontal centre of the head: the ink in the top 28% of this frame's
     * content. Centring on the bounding box instead puts the marker off to one
     * side on every mascot with a tail, a fin or a wing, because those widen
     * the box without moving the head -- measured on the ready pose, ten of the
     * twelve are off, Dragon and Tiger by a fifth of their width.
     */
    private double headCentre(Image source,int col,int row,double cw,double ch){
        String key=System.identityHashCode(source)+":"+col+":"+row;
        Double cached=headCentres.get(key);
        if(cached!=null)return cached;
        Rectangle2D own=crop(source,col,row,cw,ch);
        var reader=source.getPixelReader();
        int x0=(int)own.getMinX(),y0=(int)own.getMinY();
        int width=(int)own.getWidth(),band=Math.max(1,(int)(own.getHeight()*.28));
        int minX=width,maxX=-1;
        for(int y=y0;y<y0+band&&y<source.getHeight();y++)
            for(int x=0;x<width&&x0+x<source.getWidth();x++)
                if((reader.getArgb(x0+x,y)>>>24)>80){if(x<minX)minX=x;if(x>maxX)maxX=x;}
        double centre=maxX<minX?own.getMinX()+own.getWidth()/2:x0+(minX+maxX)/2.0;
        headCentres.put(key,centre);
        return centre;
    }
    private Rectangle2D crop(Image source,int col,int row,double cw,double ch){
        String key=System.identityHashCode(source)+":"+col+":"+row;
        return crops.computeIfAbsent(key,k->{
            int sx=(int)(col*cw),sy=(int)(row*ch),minX=(int)cw,minY=(int)ch,maxX=0,maxY=0;
            var reader=source.getPixelReader();
            for(int y=0;y<(int)ch;y++)for(int x=0;x<(int)cw;x++)if((reader.getArgb(sx+x,sy+y)>>>24)>80){
                minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);}
            if(maxX<minX||maxY<minY)return new Rectangle2D(sx,sy,cw,ch);
            return new Rectangle2D(sx+minX,sy+minY,maxX-minX+1,maxY-minY+1);
        });
    }
    /**
     * Strict pinhole projection puts an outfielder at the fence on 16 screen
     * pixels, which reads as a smudge rather than a mascot. Flattening the far
     * end of the curve keeps every fielder legible while leaving the near
     * actors -- pitcher, catcher, batter -- at very close to their true size,
     * and preserves the ordering by depth so the field still reads as deep.
     * <p>This returns a world height, not a pixel height, so the caller can
     * feed the same value to the sprite and to {@link GlovePose}: the drawn
     * body and the glove it catches with can never disagree.
     */
    private static final double READABLE_NEAR=120,READABLE_FLOOR=30,READABLE_CURVE=.74;
    public static double readableHeight(ParkCamera camera,WorldPoint feet,double worldHeight){
        if(camera==null||!(worldHeight>0))return worldHeight;
        double pixels=camera.projectedHeight(feet,worldHeight);
        if(!(pixels>0)||pixels>=READABLE_NEAR)return worldHeight;
        double lifted=READABLE_FLOOR+(READABLE_NEAR-READABLE_FLOOR)
                *Math.pow(pixels/READABLE_NEAR,READABLE_CURVE);
        return worldHeight*lifted/pixels;
    }

    /**
     * Mip chain for one atlas, halved with alpha-weighted averaging.
     * <p>A fielder at the fence is about 50px tall while his atlas cell is
     * 384px, and one bilinear step across an 8:1 reduction samples four
     * texels out of sixty-four: whole features drop out and the survivors
     * crawl frame to frame, which is the blocky speckle on the distant
     * mascots. Sampling a level that is already close to the drawn size
     * means every source pixel contributes.
     */
    private Image[] mipChain(Image source){
        return mipChains.computeIfAbsent(source,base->{
            List<Image> levels=new ArrayList<>();levels.add(base);
            Image current=base;
            while(current.getWidth()>=32&&current.getHeight()>=32&&levels.size()<8){
                current=halve(current);if(current==null)break;levels.add(current);
            }
            return levels.toArray(new Image[0]);
        });
    }
    /** One 2x box reduction. Averaging happens premultiplied, or edge pixels
     *  borrow colour from fully transparent neighbours and sprout dark halos. */
    private static Image halve(Image source){
        int w=(int)source.getWidth()/2,h=(int)source.getHeight()/2;
        if(w<1||h<1)return null;
        var reader=source.getPixelReader();if(reader==null)return null;
        int sw=(int)source.getWidth(),sh=(int)source.getHeight();
        int[] in=new int[sw*sh];
        reader.getPixels(0,0,sw,sh,javafx.scene.image.PixelFormat.getIntArgbInstance(),in,0,sw);
        int[] out=new int[w*h];
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            int alpha=0,red=0,green=0,blue=0;
            for(int dy=0;dy<2;dy++)for(int dx=0;dx<2;dx++){
                int argb=in[(y*2+dy)*sw+x*2+dx],a=argb>>>24;
                alpha+=a;red+=((argb>>16)&255)*a;green+=((argb>>8)&255)*a;blue+=(argb&255)*a;
            }
            out[y*w+x]=alpha==0?0:((alpha/4)<<24)|((red/alpha)<<16)|((green/alpha)<<8)|(blue/alpha);
        }
        WritableImage image=new WritableImage(w,h);
        image.getPixelWriter().setPixels(0,0,w,h,
            javafx.scene.image.PixelFormat.getIntArgbInstance(),out,0,w);
        return image;
    }
    /** drawImage through the mip chain, choosing the level nearest the drawn size. */
    private void drawMipped(GraphicsContext g,Image source,double sx,double sy,double sw,double sh,
                            double dx,double dy,double dw,double dh,double devicePixels){
        Image[] chain=mipChain(source);
        double targetW=Math.abs(dw)*devicePixels,targetH=Math.abs(dh)*devicePixels;
        int level=0;
        while(level+1<chain.length&&sw/(1<<level+1)>=targetW&&sh/(1<<level+1)>=targetH)level++;
        double shrink=1<<level;
        g.drawImage(chain[level],sx/shrink,sy/shrink,sw/shrink,sh/shrink,dx,dy,dw,dh);
    }
    public void tag(GraphicsContext g,double x,double y,String text){
        double w=text.length()*6.1+18;x=Math.max(w/2+6,Math.min(1174-w/2,x));y=Math.max(18,y);
        g.setFill(Color.rgb(19,37,34,.9));g.fillRoundRect(x-w/2,y-13,w,22,4,4);
        g.setFont(Font.font("Segoe UI",FontWeight.BOLD,10));g.setFill(Color.web("#ebefce"));g.fillText(text,x-w/2+9,y+2);
        g.setFill(Color.web("#d9d982"));g.fillPolygon(new double[]{x-3,x+3,x},new double[]{y+9,y+9,y+14},3);
    }
    public void ball(GraphicsContext g,WorldPoint p,WorldPoint shadow,double diameter){
        if(!Double.isFinite(p.x())||!Double.isFinite(p.y()))return;
        double d=Math.max(3,diameter);
        g.setFill(Color.rgb(20,32,16,.25));g.fillOval(shadow.x()-d*.5,shadow.y()-d*.2,d,d*.4);
        g.setStroke(Color.rgb(255,244,184,.30));g.setLineWidth(1.6);g.strokeOval(p.x()-d*.72,p.y()-d*.72,d*1.44,d*1.44);
        g.setFill(Color.web("#fffbed"));g.fillOval(p.x()-d/2,p.y()-d/2,d,d);
        if(d>=5){g.setStroke(Color.web("#af694f"));g.setLineWidth(.8);g.strokeArc(p.x()-d*.3,p.y()-d*.3,d*.6,d*.6,35,115,javafx.scene.shape.ArcType.OPEN);}
    }
    public void zone(GraphicsContext g,ParkCamera c,double aimX,double aimHeight,boolean contact){
        WorldPoint[] corners={c.project(new WorldPoint(557.167,610),6),c.project(new WorldPoint(562.833,610),6),
            c.project(new WorldPoint(562.833,610),14),c.project(new WorldPoint(557.167,610),14)};
        double[] x=new double[4],y=new double[4];for(int i=0;i<4;i++){x[i]=corners[i].x();y[i]=corners[i].y();}
        g.setFill(Color.rgb(20,36,30,.30));g.fillPolygon(x,y,4);
        g.setStroke(Color.rgb(255,249,215,.96));g.setLineWidth(1.6);g.strokePolygon(x,y,4);
        // Corner guides improve acquisition without changing the real strike area.
        g.setStroke(Color.rgb(242,235,163,.85));g.setLineWidth(1.1);
        for(int i=0;i<4;i++){
            double dx=(i==0||i==3?-1:1)*3,dy=(i<2?1:-1)*3;
            g.strokeLine(x[i]+dx,y[i]+dy,x[i]+dx*2.5,y[i]+dy);
            g.strokeLine(x[i]+dx,y[i]+dy,x[i]+dx,y[i]+dy*2.5);
        }
        g.setStroke(Color.rgb(249,244,211,.16));g.setLineWidth(.6);
        for(int i=1;i<3;i++){
            WorldPoint a=c.project(new WorldPoint(557.167+5.666*i/3,610),6),b=c.project(new WorldPoint(557.167+5.666*i/3,610),14);
            g.strokeLine(a.x(),a.y(),b.x(),b.y());
            a=c.project(new WorldPoint(557.167,610),6+8.0*i/3);b=c.project(new WorldPoint(562.833,610),6+8.0*i/3);
            g.strokeLine(a.x(),a.y(),b.x(),b.y());
        }
        WorldPoint aim=c.project(new WorldPoint(aimX,610),aimHeight);
        g.setStroke(contact?Color.web("#e6e786"):Color.web("#e7edbf"));g.setLineWidth(1.5);
        g.strokeOval(aim.x()-5,aim.y()-5,10,10);g.strokeLine(aim.x()-9,aim.y(),aim.x()+9,aim.y());g.strokeLine(aim.x(),aim.y()-9,aim.x(),aim.y()+9);
    }
    /** Pitch choices live in the interactive FXML controls; no fake controller prompts. */
    public void pitchMenu(GraphicsContext g,boolean visible,String selected){}
}
