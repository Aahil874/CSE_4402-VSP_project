package application;
import java.util.List;
/** A square diamond measured in world units; the camera supplies perspective. */
public final class BasePath {
    private static final List<WorldPoint> BASES=List.of(FieldGeometry.HOME,FieldGeometry.FIRST,FieldGeometry.SECOND,FieldGeometry.THIRD);
    public WorldPoint point(Base base){return BASES.get(base.index());}
    public WorldPoint pointForLapPosition(double lapPosition){
        double n=Math.max(0,Math.min(4,lapPosition));if(n>=4)return point(Base.HOME);
        int segment=Math.min(3,(int)Math.floor(n));return BASES.get(segment).interpolate(BASES.get((segment+1)%4),n-segment);
    }
    public double perspectiveScale(WorldPoint point){return 1;}
    public double segmentLength(int segment){return BASES.get(Math.floorMod(segment,4)).distanceTo(BASES.get(Math.floorMod(segment+1,4)));}
}

