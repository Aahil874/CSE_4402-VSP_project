package application;
/** A catcher shuffles behind the plate toward the pitch, never into the batter. */
public final class CatcherMovementSimulation {
    public static final double RECEIVE_Y=646, CROUCH_HEIGHT=27, GLOVE_HEIGHT=13;
    private double x,y;
    public CatcherMovementSimulation(double startX,double startY,long unusedSeed){x=startX;y=startY;}
    public void update(double elapsed,boolean active,double ballX,double ballY){
        if(!Double.isFinite(elapsed)||elapsed<=0)return;
        double targetX=active?Math.max(551,Math.min(569,ballX)):560;
        double dx=targetX-x,dy=RECEIVE_Y-y,d=Math.hypot(dx,dy);
        if(d>0){double step=Math.min(d,38*elapsed);x+=dx/d*step;y+=dy/d*step;}
    }
    public double x(){return x;} public double y(){return y;}
}
