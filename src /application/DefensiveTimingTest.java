package application;

/** Deterministic checks for the readable pitcher ground-ball sequence primitives. */
public final class DefensiveTimingTest {
    private DefensiveTimingTest() { }

    public static void main(String[] args) {
        LiveBallController ball = new LiveBallController();
        ball.launchGroundBall(560.0, 548.0, 0.0, -190.0, HitResult.SINGLE);
        WorldPoint start = ball.shadowPosition();
        ball.update(0.25);
        require(ball.state() == LiveBallController.State.AIRBORNE
                || ball.state() == LiveBallController.State.ROLLING,
                "ground ball remains live while travelling");
        require(ball.shadowPosition().y() < start.y(), "ground ball moves toward pitcher");

        double elapsed = 0.25;
        while (elapsed < 1.10) {
            ball.update(0.05);
            elapsed += 0.05;
        }
        require(ball.state() == LiveBallController.State.AIRBORNE
                || ball.state() == LiveBallController.State.ROLLING,
                "short pitcher play stays live long enough to be seen");

        WorldPoint pitcher = new WorldPoint(535.0, 185.0);
        WorldPoint first = new BasePath().point(Base.FIRST);
        ball.field("PITCHER", pitcher);
        ball.throwFromFielder(pitcher, first, 315.0);
        double throwElapsed = 0.0;
        while (ball.state() == LiveBallController.State.THROWN_BY_FIELDER && throwElapsed < 3.0) {
            ball.update(0.05);
            throwElapsed += 0.05;
        }
        require(ball.state() == LiveBallController.State.CAUGHT, "throw reaches first base physically");

        DefensiveThrowController.ForcePlay play = new DefensiveThrowController().compare(
                pitcher, first, 315.0, 2.0, 1.0);
        require(play.ballArrivalSeconds() > 0.5, "throw timing is distance based");
        System.out.println("DefensiveTimingTest: PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
