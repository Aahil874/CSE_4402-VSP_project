package application;
/** Tests the true camera projection, role orientation, ray inversion and transition. */
public final class ParkCameraTest {
    public static void main(String[] args){
        ParkCamera c=new ParkCamera();
        for(ParkCamera.Mode mode:ParkCamera.Mode.values()){
            c.snap(mode);
            for(double y=-1000;y<=850;y+=37)for(double x=-300;x<=1420;x+=73){
                WorldPoint p=new WorldPoint(x,y);if(!c.isVisible(p))continue;
                WorldPoint screen=c.project(p,0),round=c.unprojectGround(screen.x(),screen.y());
                near(p.x(),round.x(),1e-7,"Ground x roundtrip");near(p.y(),round.y(),1e-7,"Ground y roundtrip");
                WorldPoint height=c.project(p,24),inverse=c.unprojectAtGroundY(height.x(),height.y(),p.y());
                near(p.x(),inverse.x(),1e-7,"Vertical-plane x roundtrip");near(24,inverse.y(),1e-7,"Vertical-plane height roundtrip");
                if(screen.y()>=0&&screen.y()<=650)require(c.projectedHeight(p,24)>0,"Heads extend above grounded feet");
            }
        }
        c.snap(ParkCamera.Mode.BATTING);
        require(c.project(FieldGeometry.HOME,0).y()>c.project(FieldGeometry.MOUND,0).y(),"Batting faces mound");
        require(c.projectedHeight(FieldGeometry.MOUND,24)>30,"Pitcher is readable at pitching distance");
        require(c.project(FieldGeometry.SECOND,0).y()>c.project(FieldGeometry.fencePoint(0),0).y()+20,"Outfield has projected depth");
        WorldPoint first=c.project(FieldGeometry.FIRST,0),third=c.project(FieldGeometry.THIRD,0);
        near(first.x()+third.x(),1180,1e-7,"Batting diamond is mirrored");
        c.setMode(ParkCamera.Mode.PITCHING);
        double prior=c.transitionProgress();
        for(int i=0;i<80;i++){
            c.update(.01);require(c.transitionProgress()>=prior,"Transition progresses monotonically");prior=c.transitionProgress();
            require(c.isVisible(FieldGeometry.HOME)&&c.isVisible(FieldGeometry.MOUND),"Orbit never flies through the battery");
        }
        require(c.project(FieldGeometry.MOUND,0).y()>c.project(FieldGeometry.HOME,0).y(),"Pitching faces home plate");
        require(c.projectedHeight(FieldGeometry.HOME,24)>35,"Batter is readable at pitching distance");
        require(!c.isVisible(FieldGeometry.fencePoint(0)),"Outfield is behind pitching camera");
        require(c.isVisible(new WorldPoint(560,820)),"Backstop is ahead of pitching camera");
        c.snap(ParkCamera.Mode.FIELDING);
        for(WorldPoint p:new WorldPoint[]{FieldGeometry.HOME,FieldGeometry.FIRST,FieldGeometry.SECOND,FieldGeometry.THIRD,FieldGeometry.fencePoint(0),FieldGeometry.fencePoint(-Math.PI/4),FieldGeometry.fencePoint(Math.PI/4)}){
            WorldPoint s=c.project(p,0);require(s.x()>=0&&s.x()<=1180&&s.y()>=0&&s.y()<=650,"Fielding shows entire diamond and fence");
        }
        System.out.println("PARK_CAMERA=PASS");
    }
    private static void near(double a,double b,double tolerance,String message){require(Math.abs(a-b)<tolerance,message+": "+a+" / "+b);}
    private static void require(boolean v,String m){if(!v)throw new AssertionError(m);}
}
