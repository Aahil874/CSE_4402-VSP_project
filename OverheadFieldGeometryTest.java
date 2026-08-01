package application;

public final class OverheadFieldGeometryTest {
    private OverheadFieldGeometryTest() {}

    public static void main(String[] args) {
        BasePath path = new BasePath();
        WorldPoint home = path.point(Base.HOME);
        WorldPoint first = path.point(Base.FIRST);
        WorldPoint second = path.point(Base.SECOND);
        WorldPoint third = path.point(Base.THIRD);

        require(home.x() == second.x(), "Home and second base must share the center axis.");
        require(first.y() == third.y(), "First and third base must be level.");
        require(close(first.x() - home.x(), home.x() - third.x()),
                "First and third base must mirror each other.");
        require(close(path.segmentLength(0), path.segmentLength(3)),
                "Home-side base paths must be symmetrical.");
        require(close(path.segmentLength(1), path.segmentLength(2)),
                "Outfield-side base paths must be symmetrical.");
        require(path.segmentLength(0) > path.segmentLength(1),
                "Forced perspective must make the foreground base path appear larger.");
        require(path.perspectiveScale(second) < path.perspectiveScale(home),
                "Distant runners must render smaller.");

        Runner runner = new Runner(Mascot.TURBO_TANUKI, 0, 0, path);
        runner.advanceBases(4);
        for (int frame = 0; frame < 800 && !runner.hasScored(); frame++) {
            runner.update(1.0 / 60.0, path);
        }
        require(runner.hasScored(), "A home-run runner must complete the full diamond.");
        require(runner.feet().distanceTo(home) < 0.01,
                "A completed runner must finish exactly on home plate.");
        System.out.println("OVERHEAD_FIELD_GEOMETRY=PASS");
    }

    private static boolean close(double left, double right) {
        return Math.abs(left - right) < 0.001;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
