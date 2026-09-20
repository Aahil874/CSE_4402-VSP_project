package application;

/** Separate a confirmed boundary result from the bases physically reached by the batter. */
public final class FieldOutcomeClassifier {
    private FieldOutcomeClassifier() {}

    /** Ball distance alone cannot award a single, double or triple. */
    public static HitResult classify(Baseball ball) { return classify(ball, 0); }

    public static HitResult classify(Baseball ball, int basesReached) {
        if (ball == null) return HitResult.OUT;
        if (ball.homeRun()) return HitResult.HOME_RUN;
        return switch (Math.max(0, Math.min(3, basesReached))) {
            case 1 -> HitResult.SINGLE;
            case 2 -> HitResult.DOUBLE;
            case 3 -> HitResult.TRIPLE;
            default -> HitResult.OUT;
        };
    }
}
