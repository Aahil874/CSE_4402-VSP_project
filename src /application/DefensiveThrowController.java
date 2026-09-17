package application;

public final class DefensiveThrowController {
    public record ForcePlay(double ballArrivalSeconds, double runnerArrivalSeconds,
                            boolean out) {}
    public ForcePlay compare(WorldPoint glove, WorldPoint base, double throwSpeed,
                             double runnerDistance, double runnerSpeed) {
        double ballTime = glove.distanceTo(base) / Math.max(1, throwSpeed);
        double runnerTime = runnerDistance / Math.max(1, runnerSpeed);
        return new ForcePlay(ballTime, runnerTime, ballTime + .025 < runnerTime);
    }
}
