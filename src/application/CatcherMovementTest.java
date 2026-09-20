package application;
public final class CatcherMovementTest {
 public static void main(String[] args){CatcherMovementSimulation c=new CatcherMovementSimulation(560,635,7);double x=c.x(),y=c.y();for(int i=0;i<60;i++)c.update(1.0/60,true,600,605);if(Math.hypot(c.x()-x,c.y()-y)<=0)throw new AssertionError("catcher did not move");if(c.x()<510||c.x()>610||c.y()<605||c.y()>665)throw new AssertionError("catcher escaped bounds");System.out.println("CATCHER_MOVEMENT=PASS");}
}
