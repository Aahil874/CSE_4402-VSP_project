package application;


public final class CollisionService {
    public boolean circlesOverlap(WorldPoint first, double firstRadius,
                                  WorldPoint second, double secondRadius) {
        return first.distanceTo(second) <= Math.max(0, firstRadius) + Math.max(0, secondRadius);
    }

    public boolean insideRectangle(double x, double y, double left, double top,
                                   double width, double height) {
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }
}
