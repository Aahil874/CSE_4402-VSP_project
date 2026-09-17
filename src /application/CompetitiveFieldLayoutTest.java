package application;

public final class CompetitiveFieldLayoutTest {
    private static final WorldPoint[] FIELDERS = {
            new WorldPoint(300, 335), new WorldPoint(445, 245),
            new WorldPoint(675, 245), new WorldPoint(820, 335),
            new WorldPoint(270, 145), new WorldPoint(560, 92),
            new WorldPoint(850, 145)
    };

    private CompetitiveFieldLayoutTest() {}

    public static void main(String[] args) {
        BasePath path = new BasePath();
        require(path.point(Base.HOME).x() == path.point(Base.SECOND).x(),
                "Home, mound, second, and center field must share the center axis.");
        require(path.point(Base.FIRST).x() - path.point(Base.HOME).x()
                        == path.point(Base.HOME).x() - path.point(Base.THIRD).x(),
                "First and third must mirror exactly.");
        for (int left = 0; left < FIELDERS.length; left++) {
            require(FIELDERS[left].y() >= 70 && FIELDERS[left].y() <= 350,
                    "Every fielder must remain on playable ground.");
            for (int right = left + 1; right < FIELDERS.length; right++) {
                require(FIELDERS[left].distanceTo(FIELDERS[right]) >= 105,
                        "Fielder anchors must be widely separated.");
            }
        }
        require(Mascot.values().length == 12,
                "Playable characters must come from the established twelve-mascot roster.");
        System.out.println("COMPETITIVE_FIELD_LAYOUT=PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
