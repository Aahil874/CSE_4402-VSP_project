package application;

import java.util.List;
public final class BasePath {
    private static final List<WorldPoint> BASES = List.of(
            new WorldPoint(560, 548),
            new WorldPoint(872, 280),
            new WorldPoint(560, 191),
            new WorldPoint(248, 280));

    public WorldPoint point(Base base) {
        return BASES.get(base.index());
    }

    public WorldPoint pointForLapPosition(double lapPosition) {
        double normalized = Math.max(0, Math.min(4, lapPosition));
        if (normalized >= 4) {
            return point(Base.HOME);
        }
        int segment = Math.min(3, (int) Math.floor(normalized));
        double local = normalized - segment;
        double eased = local * local * (3 - 2 * local);
        WorldPoint start = BASES.get(segment);
        WorldPoint end = BASES.get((segment + 1) % BASES.size());
        return start.interpolate(end, eased);
    }

    public double perspectiveScale(WorldPoint point) {
        double depth = Math.max(0, Math.min(1, (548 - point.y()) / 380.0));
        return 1.0 - depth * 0.40;
    }

    public double segmentLength(int segment) {
        WorldPoint start = BASES.get(Math.floorMod(segment, 4));
        WorldPoint end = BASES.get(Math.floorMod(segment + 1, 4));
        return start.distanceTo(end);
    }
}
