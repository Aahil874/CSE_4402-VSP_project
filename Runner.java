package application;


public final class Runner {
    private final Mascot mascot;
    private final double laneOffset;
    private double lapPosition;
    private double targetLapPosition;
    private double velocity;
    private WorldPoint feet;

    public Runner(Mascot mascot, double startLapPosition, double laneOffset,
                  BasePath path) {
        this.mascot = mascot;
        this.lapPosition = startLapPosition;
        this.targetLapPosition = startLapPosition;
        this.laneOffset = laneOffset;
        this.feet = path.pointForLapPosition(startLapPosition);
    }

    public void advanceBases(int bases) {
        targetLapPosition = Math.min(4, targetLapPosition + Math.max(0, bases));
    }

    public void update(double elapsed, BasePath path) {
        double remaining = targetLapPosition - lapPosition;
        if (remaining <= 0.0001) {
            velocity = Math.max(0, velocity - elapsed * 7);
            lapPosition = targetLapPosition;
        } else {
            velocity = Math.min(1.34, velocity + elapsed * 3.8);
            lapPosition = Math.min(targetLapPosition,
                    lapPosition + velocity * elapsed);
        }
        WorldPoint ground = path.pointForLapPosition(lapPosition);
        double offset = laneOffset * path.perspectiveScale(ground);
        feet = new WorldPoint(ground.x() + offset, ground.y() + offset * 0.16);
    }

    public Mascot mascot() { return mascot; }
    public double lapPosition() { return lapPosition; }
    public double targetLapPosition() { return targetLapPosition; }
    public double velocity() { return velocity; }
    public WorldPoint feet() { return feet; }
    public boolean isRunning() { return targetLapPosition - lapPosition > 0.0001; }
    public boolean hasScored() { return lapPosition >= 3.9999; }
}
