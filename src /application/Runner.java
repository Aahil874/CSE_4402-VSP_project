package application;

/** Continuous acceleration/braking along the physical base paths. */
public final class Runner {
    private final Mascot mascot;
    private double lapPosition;
    private double targetLapPosition;
    private double worldSpeed;
    private double segmentLength;
    private WorldPoint feet;
    private double distanceTravelled;
    private boolean facingRight = true;
    private double dashRemaining, dashCooldown;
    private static final double MAX_SPEED = 90.0;
    private static final double ACCELERATION = 115.0;
    private static final double DECELERATION = 170.0;
    private static final double DASH_SECONDS = .85, DASH_RECHARGE = 2.0, DASH_BOOST = 1.5;

    public Runner(Mascot mascot, double startLapPosition, double ignoredLaneOffset, BasePath path) {
        this.mascot = mascot;
        this.lapPosition = Math.max(0, Math.min(4, startLapPosition));
        this.targetLapPosition = this.lapPosition;
        this.feet = path.pointForLapPosition(this.lapPosition);
        this.segmentLength = path.segmentLength(Math.min(3, (int)this.lapPosition));
    }

    public void advanceBases(int bases) { targetLapPosition = Math.min(4, targetLapPosition + Math.max(0, bases)); }
    public void targetBase(double base) { targetLapPosition = Math.max(0, Math.min(4, base)); }
    public void returnToBase(double base) { targetBase(base); }

    /**
     * A sprint burst, as RunnerController.dashRunners asks for. It is on a
     * recharge so holding the key down cannot turn it into a permanent speed
     * increase, and it only fires while the runner is actually between bases.
     */
    public boolean dash() {
        if (dashCooldown > 0 || !isRunning()) return false;
        dashRemaining = DASH_SECONDS;
        dashCooldown = DASH_RECHARGE;
        return true;
    }
    public boolean isDashing() { return dashRemaining > 0; }

    public void update(double elapsed, BasePath path) {
        if (!Double.isFinite(elapsed) || elapsed <= 0) return;
        dashRemaining = Math.max(0, dashRemaining - elapsed);
        dashCooldown = Math.max(0, dashCooldown - elapsed);
        double difference = targetLapPosition - lapPosition;
        int segment = Math.min(3, Math.max(0, (int)Math.floor(lapPosition)));
        if (difference < 0 && Math.abs(lapPosition - Math.rint(lapPosition)) < .000001)
            segment = Math.max(0, segment - 1);
        segmentLength = path.segmentLength(segment);
        double remaining = Math.abs(difference) * segmentLength;
        // the burst lifts top speed and acceleration together, so a dash both
        // pulls away and gets up to pace faster
        double multiplier = mascot.getSpeedMultiplier() * (dashRemaining > 0 ? DASH_BOOST : 1);
        if (remaining <= .05) {
            lapPosition = targetLapPosition;
            worldSpeed = 0;
        } else {
            double desired = Math.min(MAX_SPEED * multiplier,
                    Math.sqrt(2 * DECELERATION * multiplier * remaining));
            double rate = (desired < worldSpeed ? DECELERATION : ACCELERATION) * multiplier;
            double previousSpeed = worldSpeed;
            worldSpeed += Math.copySign(Math.min(Math.abs(desired - worldSpeed), rate * elapsed),
                    desired - worldSpeed);
            double step = Math.min(remaining, (previousSpeed + worldSpeed) * .5 * elapsed);
            WorldPoint before = feet;
            lapPosition += Math.copySign(step / segmentLength, difference);
            if (step >= remaining - .000001) { lapPosition = targetLapPosition; worldSpeed = 0; }
            feet = path.pointForLapPosition(lapPosition);
            distanceTravelled += before.distanceTo(feet);
            if (Math.abs(feet.x() - before.x()) > .001) facingRight = feet.x() > before.x();
        }
        // Feet land on the actual base; camera perspective must not shift the collision anchor.
        feet = path.pointForLapPosition(lapPosition);
    }

    public Mascot mascot() { return mascot; }
    public double lapPosition() { return lapPosition; }
    public double targetLapPosition() { return targetLapPosition; }
    /** Base segments per second, retained for existing consumers. */
    public double velocity() { return worldSpeed / Math.max(1, segmentLength); }
    public double worldSpeed() { return worldSpeed; }
    public double arrivalSeconds() {
        return Math.abs(targetLapPosition - lapPosition) * segmentLength / Math.max(30, worldSpeed);
    }
    public WorldPoint feet() { return feet; }
    public boolean isRunning() { return Math.abs(targetLapPosition - lapPosition) > .00001; }
    /** One full alternating-leg cycle per six feet, driven by distance rather than a random clock. */
    public int runFrame() { return (int)Math.floor(distanceTravelled / 6.0) % 4; }
    /** The same cycle as a continuous value, so the renderer can read a sub-frame stride phase. */
    public double runPhase() { return distanceTravelled / 6.0; }
    public boolean facingRight() { return facingRight; }
    public boolean hasScored() { return lapPosition >= 3.99999 && targetLapPosition >= 4; }
}
