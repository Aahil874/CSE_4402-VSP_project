package application;
public final class CatchGeometryTest {
 public static void main(String[] args) {
  Baseball b=new Baseball(); WorldPoint g=new WorldPoint(100,100);
  b.launch(60,100,100,0,0,0,false); b.setMotion(60,100,42,100,0,0);
  check(CatchGeometry.eligible(b,g,42,24),"approaching ball");
  b.setMotion(60,100,42,-100,0,0);
  check(!CatchGeometry.eligible(b,g,42,24),"moving away");
  b.setMotion(60,100,0,100,0,0);
  check(!CatchGeometry.eligible(b,g,42,24),"ground ball");
  b.setMotion(60,100,200,100,0,0);
  check(!CatchGeometry.eligible(b,g,42,24),"unreachable height");
  check(CatchGeometry.intersects(80,100,42,120,100,42,g,42,8),"swept crossing");
  check(!CatchGeometry.intersects(80,100,0,120,100,0,g,42,8),"ground cannot be caught");
  b.stop(); check(!CatchGeometry.eligible(b,g,42,24),"held ball");
  System.out.println("CATCH_GEOMETRY=PASS");
 }
 private static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
}
