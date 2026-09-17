package application;

public final class GameLoop {
    private static final double MAX_STEP = 1.0 / 30.0;
    private long previousFrame;

    public double beginFrame(long now) {
        if (previousFrame == 0) {
            previousFrame = now;
            return 0;
        }
        double elapsed = (now - previousFrame) / 1_000_000_000.0;
        previousFrame = now;
        return Math.max(0, Math.min(MAX_STEP, elapsed));
    }

    public void reset() {
        previousFrame = 0;
    }
}
