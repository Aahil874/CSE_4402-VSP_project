package application;
/** The simulation diamond is square; perspective only occurs at render time. */
public final class OverheadFieldGeometryTest {
    public static void main(String[] args){
        BasePath path=new BasePath();
        for(int i=0;i<4;i++)near(path.segmentLength(i),360,"Each base path is90ft");
        near(path.point(Base.HOME).distanceTo(FieldGeometry.MOUND),242,"Pitching distance is60ft6in");
        near(path.point(Base.HOME).distanceTo(path.point(Base.SECOND)),360*Math.sqrt(2),"Square diagonal");
        near(path.point(Base.FIRST).y(),path.point(Base.THIRD).y(),"Mirrored base depth");
        WorldPoint quarter=path.pointForLapPosition(.25),half=path.pointForLapPosition(.5);
        near(quarter.distanceTo(path.point(Base.HOME)),90,"Movement distance is linear");
        near(half.distanceTo(quarter),90,"Equal world time does not bake in perspective easing");
        near(FieldGeometry.fenceRadius(0),1600,"Center fence is400ft");
        near(FieldGeometry.fenceRadius(Math.PI/4),1320,"Foul-line fence is330ft");
        if(!FieldGeometry.isFair(FieldGeometry.fencePoint(.6)))throw new AssertionError("Fence in fair territory");
        if(FieldGeometry.isFair(new WorldPoint(1000,600)))throw new AssertionError("Sideways ground is foul");
        Runner runner=new Runner(Mascot.TURBO_TANUKI,0,0,path);runner.advanceBases(4);
        for(int frame=0;frame<3000&&!runner.hasScored();frame++)runner.update(1.0/60,path);
        if(!runner.hasScored())throw new AssertionError("Runner completes four actual bases");
        near(runner.feet().distanceTo(FieldGeometry.HOME),0,"Scoring returns to home");
        System.out.println("OVERHEAD_FIELD_GEOMETRY=PASS");
    }
    private static void near(double a,double b,String m){if(Math.abs(a-b)>.001)throw new AssertionError(m+": "+a+"/"+b);}
}

