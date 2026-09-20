package application;

/** A live catch cannot score runners that left before the catch. */
public final class LiveRunnerTest {
    public static void main(String[] args) {
        RunnerController runners = new RunnerController();
        runners.advance(HitResult.TRIPLE, Mascot.TURBO_TANUKI);
        simulate(runners, 18);
        check(runners.isOccupied(3), "Existing runner reaches third physically");
        runners.startLivePlay(Mascot.BUBBLE_BUNNY);
        simulate(runners, 1);
        check(!runners.batterReachedFirst(), "Batter cannot finish a ninety-foot path in one second");
        simulate(runners, 6);
        check(runners.batterReachedFirst(), "Batter reaches first continuously");
        check(runners.drainScoredRuns() == 0, "Unresolved live play defers scoring");
        runners.flyOut();
        simulate(runners, 7);
        check(runners.isOccupied(3), "Runner returns to third after a caught fly");
        check(runners.drainScoredRuns() == 0, "Caught fly cannot score an untagged runner");
        check(runners.runnersBackToFront().size() == 1, "Caught batter runner is removed");

        runners.startLivePlay(Mascot.NOVA_NEKO);
        simulate(runners, .5);
        runners.forceOutBatter();
        runners.completeLivePlay();
        check(runners.runnersBackToFront().size() == 1, "Force out removes the actual current batter");

        runners.clear();
        runners.startLivePlay(Mascot.ROCKET_REX);
        runners.awardHomeRun();
        simulate(runners, 25);
        check(runners.drainScoredRuns() == 1, "Confirmed home run scores after running the full circuit");
        check(runners.runnersBackToFront().isEmpty(), "Scored runner leaves the field");
        System.out.println("LIVE_RUNNER=PASS");
    }
    private static void simulate(RunnerController runners, double seconds) {
        for (int i = 0; i < Math.ceil(seconds * 120); i++) runners.update(1.0 / 120);
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
