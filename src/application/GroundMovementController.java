package application;

public final class GroundMovementController {
    public WorldPoint approach(WorldPoint feet, WorldPoint target, double speed, double elapsed) {
        double dx = target.x() - feet.x();
        double dy = target.y() - feet.y();
        double distance = Math.hypot(dx, dy);
        if (distance < 0.01) return target;
        double step = Math.min(distance, Math.max(0, speed) * elapsed);
        return new WorldPoint(feet.x() + dx / distance * step, feet.y() + dy / distance * step);
    }
}
