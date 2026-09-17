package application;

/** Physical measurements: every ground and height axis uses four units per foot. */
public final class FieldGeometry {
    public static final double UNITS_PER_FOOT=4, BASE_SIDE=360;
    public static final double FOUL_FENCE_FEET=330, CENTER_FENCE_FEET=400, FENCE_HEIGHT=32, PLAYER_HEIGHT=24;
    public static final WorldPoint HOME=new WorldPoint(560,610), MOUND=new WorldPoint(560,368);
    public static final double BASE_DIAGONAL=BASE_SIDE/Math.sqrt(2);
    public static final WorldPoint FIRST=new WorldPoint(560+BASE_DIAGONAL,610-BASE_DIAGONAL);
    public static final WorldPoint SECOND=new WorldPoint(560,610-2*BASE_DIAGONAL);
    public static final WorldPoint THIRD=new WorldPoint(560-BASE_DIAGONAL,610-BASE_DIAGONAL);
    private FieldGeometry(){}
    public static double distanceFromHome(WorldPoint p){return HOME.distanceTo(p);}
    public static double sprayAngle(WorldPoint p){return Math.atan2(p.x()-560,610-p.y());}
    public static boolean isFair(WorldPoint p){return p.y()<=610.0001&&Math.abs(p.x()-560)<=610-p.y()+.0001;}
    public static double fenceRadius(double angle){return fenceRadius(angle,FOUL_FENCE_FEET,CENTER_FENCE_FEET);}
    public static double fenceRadius(double angle,double foulFeet,double centerFeet){
        double t=Math.min(1,Math.abs(angle)/(Math.PI/4));return 4*(centerFeet-(centerFeet-foulFeet)*t*t);
    }
    public static double fenceRadius(WorldPoint p){return fenceRadius(sprayAngle(p));}
    public static WorldPoint fencePoint(double angle){
        double r=fenceRadius(angle);return new WorldPoint(560+Math.sin(angle)*r,610-Math.cos(angle)*r);
    }
    public static boolean isBeyondFence(WorldPoint p){return isFair(p)&&distanceFromHome(p)>=fenceRadius(p);}
    public static double surfaceHeight(WorldPoint p){
        double radius=MOUND.distanceTo(p);return radius<=34?2.8:radius<36?2.8*(36-radius)/2:0;
    }
}
