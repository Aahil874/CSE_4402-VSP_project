package application;

/** Deterministic single-responder allocation with a buffer around ball and responder. */
public final class FielderAllocation {
    public static final double BACKUP_BUFFER = 82.0;
    private FielderAllocation() {}

    public static WorldPoint backupTarget(WorldPoint ball, WorldPoint active, boolean ballLeft) {
        double dx = ball.x() - active.x(), dy = ball.y() - active.y();
        double distance = Math.hypot(dx, dy);
        if (distance < .001) {
            dx = ballLeft ? -1 : 1;
            dy = .35;
            distance = Math.hypot(dx, dy);
        }
        double nx = dx / distance, ny = dy / distance;
        // Stay beyond the ball from the responder, shifted toward a throw-backup lane.
        // Positive forward projection guarantees separation from BOTH locations.
        double side = ballLeft ? -1 : 1;
        double ox = nx + side * -ny * .35, oy = ny + side * nx * .35;
        double scale = (BACKUP_BUFFER + 2) / Math.hypot(ox, oy);
        return new WorldPoint(ball.x() + ox * scale, ball.y() + oy * scale);
    }
}
