package application;

public final class RunnerMotionTest {
    public static void main(String[] args) {
        BasePath path = new BasePath();
        Runner runner = new Runner(Mascot.VOLT_TIGER, 0, 8, path);
        runner.advanceBases(1);
        boolean accelerated = false, braked = false;
        boolean[] frames = new boolean[4];
        double priorSpeed = 0, time = 0;
        WorldPoint before = runner.feet();
        while (runner.isRunning() && time < 8) {
            runner.update(1.0 / 120, path);
            accelerated |= runner.worldSpeed() > priorSpeed + .001;
            braked |= runner.worldSpeed() < priorSpeed - .001;
            check(before.distanceTo(runner.feet()) < 2, "No per-frame runner teleport");
            frames[runner.runFrame()] = true;
            before = runner.feet();
            priorSpeed = runner.worldSpeed();
            time += 1.0 / 120;
        }
        check(accelerated && braked, "Runner accelerates and brakes");
        check(time > 3 && time < 6, "Ninety-foot run takes a plausible duration");
        check(runner.feet().distanceTo(path.point(Base.FIRST)) < .001, "Feet touch the actual base");
        for (boolean frame : frames) check(frame, "Both alternating leg phases render while moving");
        System.out.println("RUNNER_MOTION=PASS");
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
