package application;

public record WorldPoint(double x, double y) {
    public WorldPoint interpolate(WorldPoint other, double amount) {
        double t = Math.max(0, Math.min(1, amount));
        return new WorldPoint(x + (other.x - x) * t, y + (other.y - y) * t);
    }

    public double distanceTo(WorldPoint other) {
        return Math.hypot(other.x - x, other.y - y);
    }
}
