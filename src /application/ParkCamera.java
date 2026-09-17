package application;
import java.util.Objects;
/** Pinhole camera: field coordinates, heights and shadows share one basis. */
public final class ParkCamera {
    public enum Mode { BATTING,PITCHING,FIELDING }
    public static final double WIDTH=1180,HEIGHT=650,CENTER_X=WIDTH/2;
    public static final double WORLD_CENTER_X=560,WORLD_MOUND_Y=368,TRANSITION_SECONDS=.70;
    private record View(double x,double y,double z,double yaw,double pitch,double focal,double cy){}
    private Mode mode=Mode.BATTING,fromMode=Mode.BATTING;
    private View view=preset(mode),from=view,to=view;
    private double elapsed=TRANSITION_SECONDS;
    public ParkCamera(){}
    public ParkCamera(Mode mode){snap(mode);}
    private static View preset(Mode m){return switch(m){
        case BATTING->new View(560,730,60,-Math.PI/2,.24,820,330);
        case PITCHING->new View(510,168,65,Math.atan2(442,50),.12,1150,330);
        // Keep the live play readable after contact.  This is a medium-wide
        // broadcast angle: the infield, runner and active fielder remain in
        // frame while mascots stay large enough to read their animations.
        case FIELDING->new View(560,1200,650,-Math.PI/2,.49,960,260);
    };}
    public void snap(Mode next){mode=fromMode=Objects.requireNonNull(next);view=from=to=preset(mode);elapsed=TRANSITION_SECONDS;}
    public void snapTo(Mode next){snap(next);}
    public void setMode(Mode next){
        if(mode==Objects.requireNonNull(next))return;fromMode=mode;mode=next;from=view;to=preset(next);elapsed=0;
    }
    public void update(Mode next,double seconds){setMode(next);update(seconds);}
    public void update(double seconds){
        if(!Double.isFinite(seconds)||seconds<=0)return;
        elapsed=Math.min(TRANSITION_SECONDS,elapsed+seconds);double t=transitionProgress();
        double yawDelta=Math.atan2(Math.sin(to.yaw-from.yaw),Math.cos(to.yaw-from.yaw));
        double eyeX=lerp(from.x,to.x,t),eyeY=lerp(from.y,to.y,t);
        if(fromMode!=Mode.FIELDING&&mode!=Mode.FIELDING){
            // Orbit around the live battery instead of flying through its players.
            double start=Math.atan2(from.y-500,from.x-560),end=Math.atan2(to.y-500,to.x-560);
            double delta=Math.atan2(Math.sin(end-start),Math.cos(end-start));
            double angle=start+delta*t,radius=lerp(Math.hypot(from.x-560,from.y-500),Math.hypot(to.x-560,to.y-500),t);
            eyeX=560+Math.cos(angle)*radius;eyeY=500+Math.sin(angle)*radius;
        }
        view=new View(eyeX,eyeY,lerp(from.z,to.z,t),from.yaw+yawDelta*t,
            lerp(from.pitch,to.pitch,t),lerp(from.focal,to.focal,t),lerp(from.cy,to.cy,t));
    }
    public Mode currentMode(){return mode;}
    public Mode targetMode(){return mode;}
    public double transitionProgress(){double t=elapsed/TRANSITION_SECONDS;return t*t*(3-2*t);}
    public double depthAt(WorldPoint p){return depthAt(p,0);}
    public double depthAt(WorldPoint p,double h){
        double forward=(p.x()-view.x)*Math.cos(view.yaw)+(p.y()-view.y)*Math.sin(view.yaw);
        return forward*Math.cos(view.pitch)+(view.z-h)*Math.sin(view.pitch);
    }
    public boolean isVisible(WorldPoint p){return depthAt(p)>8;}
    public WorldPoint project(WorldPoint p,double h){
        double dx=p.x()-view.x,dy=p.y()-view.y;
        double right=-Math.sin(view.yaw)*dx+Math.cos(view.yaw)*dy;
        double forward=Math.cos(view.yaw)*dx+Math.sin(view.yaw)*dy;
        double up=forward*Math.sin(view.pitch)+(h-view.z)*Math.cos(view.pitch);
        double depth=Math.max(1,forward*Math.cos(view.pitch)+(view.z-h)*Math.sin(view.pitch));
        return new WorldPoint(CENTER_X+view.focal*right/depth,view.cy-view.focal*up/depth);
    }
    public double scaleAt(WorldPoint ground){return view.focal/Math.max(1,depthAt(ground));}
    public double projectedHeight(WorldPoint feet,double height){return project(feet,0).y()-project(feet,height).y();}
    public WorldPoint unprojectGround(double sx,double sy){
        double[] ray=ray(sx,sy);double t=-view.z/ray[2];return new WorldPoint(view.x+t*ray[0],view.y+t*ray[1]);
    }
    /** Returns (world x, world height) on a vertical plane at worldY. */
    public WorldPoint unprojectAtGroundY(double sx,double sy,double worldY){
        double[] ray=ray(sx,sy);double t=(worldY-view.y)/ray[1];return new WorldPoint(view.x+t*ray[0],view.z+t*ray[2]);
    }
    private double[] ray(double sx,double sy){
        double rx=(sx-CENTER_X)/view.focal,up=(view.cy-sy)/view.focal;
        double horizontal=Math.cos(view.pitch)+up*Math.sin(view.pitch);
        return new double[]{Math.cos(view.yaw)*horizontal-Math.sin(view.yaw)*rx,
            Math.sin(view.yaw)*horizontal+Math.cos(view.yaw)*rx,-Math.sin(view.pitch)+up*Math.cos(view.pitch)};
    }
    private static double lerp(double a,double b,double t){return a+(b-a)*t;}
}
