package application;

/** Shared attachment geometry for drawing, input, and physical interception. */
public record GlovePose(WorldPoint ground, double height) {
    public static GlovePose at(WorldPoint feet, double spriteHeight, boolean right, double reach) {
        double amount = Math.max(0, Math.min(1, reach));
        return new GlovePose(new WorldPoint(feet.x() + (right ? 1 : -1)
                * spriteHeight * (.15 + .12 * amount), feet.y()),
                spriteHeight * (.47 + .12 * amount));
    }
    public WorldPoint screen() { return new WorldPoint(ground.x(), ground.y()-height); }
    public boolean promptContains(double x, double y) {
        WorldPoint p = screen();
        return x >= p.x()+7 && x <= p.x()+87 && y >= p.y()-34 && y <= p.y()-12;
    }
}
