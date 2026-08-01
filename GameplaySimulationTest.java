package application;


public final class GameplaySimulationTest {
    private GameplaySimulationTest() {
    }

    public static void main(String[] args) {
        testGroundedBasePath();
        testSinglesAndHomeRunScoring();
        testForcedWalks();
        testBallFlight();
        testMatchState();
        System.out.println("GAMEPLAY_SIMULATION_TESTS=PASS");
    }

    private static void testGroundedBasePath() {
        BasePath path = new BasePath();
        require(path.pointForLapPosition(0).equals(path.point(Base.HOME)),
                "Runner must start at home.");
        require(path.pointForLapPosition(1).equals(path.point(Base.FIRST)),
                "Runner must stop exactly at first.");
        require(path.pointForLapPosition(2).equals(path.point(Base.SECOND)),
                "Runner must stop exactly at second.");
        require(path.pointForLapPosition(3).equals(path.point(Base.THIRD)),
                "Runner must stop exactly at third.");
        require(path.pointForLapPosition(4).equals(path.point(Base.HOME)),
                "Runner must finish exactly at home.");
    }

    private static void testSinglesAndHomeRunScoring() {
        RunnerController controller = new RunnerController();
        controller.advance(HitResult.SINGLE, Mascot.TURBO_TANUKI);
        simulate(controller, 3);
        require(controller.isOccupied(1), "Single must stop at first.");
        require(controller.drainScoredRuns() == 0, "Single must not score immediately.");

        controller.advance(HitResult.HOME_RUN, Mascot.BUBBLE_BUNNY);
        simulate(controller, 5);
        require(controller.drainScoredRuns() == 2,
                "Home run must score the occupied runner and batter.");
    }

    private static void testForcedWalks() {
        RunnerController controller = new RunnerController();
        controller.advance(HitResult.WALK, Mascot.TURBO_TANUKI);
        controller.advance(HitResult.WALK, Mascot.BUBBLE_BUNNY);
        controller.advance(HitResult.WALK, Mascot.ROCKET_REX);
        simulate(controller, 4);
        require(controller.isOccupied(1) && controller.isOccupied(2)
                        && controller.isOccupied(3),
                "Three walks must load the bases.");
        controller.advance(HitResult.WALK, Mascot.NOVA_NEKO);
        simulate(controller, 3);
        require(controller.drainScoredRuns() == 1,
                "A bases-loaded walk must force exactly one run.");
    }

    private static void testBallFlight() {
        LiveBallController controller = new LiveBallController();
        controller.launchBattedBall(205, 300, 0.96, HitResult.HOME_RUN);
        WorldPoint start = controller.screenPosition();
        for (int frame = 0; frame < 120; frame++) {
            controller.update(1.0 / 60.0);
        }
        WorldPoint later = controller.screenPosition();
        require(later.x() > start.x(), "Batted ball must travel into the field.");
        require(controller.ball().height() >= 0, "Ball height cannot become negative.");
    }

    private static void testMatchState() {
        MatchState state = new MatchState();
        state.recordHit(HitResult.SINGLE);
        state.recordHit(HitResult.DOUBLE);
        state.recordHit(HitResult.TRIPLE);
        state.recordHit(HitResult.HOME_RUN);
        state.addPlayerRuns(4);
        state.addStrike();
        state.addBall();
        state.addOut();
        require(state.hits() == 4 && state.singles() == 1 && state.doubles() == 1
                        && state.triples() == 1 && state.homeRuns() == 1,
                "Hit breakdown must be authoritative.");
        require(state.playerRuns() == 4 && state.strikes() == 1
                        && state.balls() == 1 && state.outs() == 1,
                "Scoreboard count must match match state.");
    }

    private static void simulate(RunnerController controller, double seconds) {
        int frames = (int) Math.ceil(seconds * 60);
        for (int frame = 0; frame < frames; frame++) {
            controller.update(1.0 / 60.0);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
