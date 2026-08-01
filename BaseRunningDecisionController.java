package application;

public final class BaseRunningDecisionController {
    public record Decision(int batterDestination, int forcedAdvance,
                           boolean tagUpRequired, boolean allRunnersScore) {}
    public Decision forResult(HitResult result, boolean flyCaught) {
        if (flyCaught) return new Decision(0, 0, true, false);
        return switch (result) {
            case WALK -> new Decision(1, 1, false, false);
            case SINGLE -> new Decision(1, 1, false, false);
            case DOUBLE -> new Decision(2, 2, false, false);
            case TRIPLE -> new Decision(3, 3, false, false);
            case HOME_RUN -> new Decision(4, 4, false, true);
            case OUT -> new Decision(0, 0, false, false);
        };
    }
}
