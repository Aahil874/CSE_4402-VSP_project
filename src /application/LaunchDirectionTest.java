package application;
public final class LaunchDirectionTest {
 public static void main(String[] args){
  for(double[] d:new double[][]{{-1,0},{0,-1},{1,0},{0,1}}){LiveBallController c=new LiveBallController();c.launchBattedBall(560,548,.8,HitResult.SINGLE,d[0],d[1]);if(Math.hypot(c.ball().velocityX(),c.ball().velocityY())<1)throw new AssertionError("zero launch");}
  System.out.println("LAUNCH_DIRECTIONS=PASS");
 }
}
