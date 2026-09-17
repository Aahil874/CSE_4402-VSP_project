package application;

/** Catch tests use the same field axes, height and integration as live ball motion. */
public final class CatchGeometry {
    private CatchGeometry() {}
    public static WorldPoint interceptionTarget(Baseball ball, double gloveHeight) {
        if (ball.touchedGround() || (ball.height() <= gloveHeight && ball.velocityHeight() <= 0))
            return new WorldPoint(ball.x(), ball.y());
        Baseball prediction = copy(ball);
        BaseballPhysicsService physics = new BaseballPhysicsService();
        for (int frame = 0; frame < 1200; frame++) {
            double prior = prediction.height();
            WorldPoint before = new WorldPoint(prediction.x(), prediction.y());
            physics.update(prediction, 1.0 / 120);
            if (prediction.velocityHeight() <= 0 && prediction.height() <= gloveHeight) {
                double t = Math.max(0, Math.min(1, (prior - gloveHeight) /
                        Math.max(.000001, prior - prediction.height())));
                return before.interpolate(new WorldPoint(prediction.x(), prediction.y()), t);
            }
        }
        return new WorldPoint(prediction.x(), prediction.y());
    }

    public static boolean eligible(Baseball ball, WorldPoint glove, double gloveHeight, double radius) {
        if (!ball.active() || ball.touchedGround() || ball.height() < 1) return false;
        double dx = glove.x() - ball.x(), dy = glove.y() - ball.y();
        if (dx * ball.velocityX() + dy * ball.velocityY() < 0
                && Math.hypot(dx, dy) > radius) return false;
        Baseball prediction = copy(ball);
        BaseballPhysicsService physics = new BaseballPhysicsService();
        for (int frame = 0; frame < 54; frame++) {
            double ax = prediction.x(), ay = prediction.y(), ah = prediction.height();
            physics.update(prediction, 1.0 / 120);
            if (intersects(ax, ay, ah, prediction.x(), prediction.y(), prediction.height(),
                    glove, gloveHeight, radius)) return true;
            if (prediction.touchedGround()) return false;
        }
        return false;
    }

    private static Baseball copy(Baseball source) {
        Baseball result = new Baseball();
        result.launch(source.x(), source.y(), source.velocityX(), source.velocityY(),
                source.velocityHeight(), source.spin(), false);
        result.setMotion(source.x(), source.y(), source.height(), source.velocityX(),
                source.velocityY(), source.velocityHeight());
        return result;
    }

    public static boolean intersects(double ax, double ay, double ah, double bx, double by, double bh,
                                     WorldPoint glove, double height, double radius) {
        double dx = bx - ax, dy = by - ay, dh = bh - ah;
        double distanceSquared = dx * dx + dy * dy + dh * dh;
        double t = distanceSquared == 0 ? 0 : Math.max(0, Math.min(1,
                ((glove.x() - ax) * dx + (glove.y() - ay) * dy + (height - ah) * dh) / distanceSquared));
        double x = ax + t * dx - glove.x(), y = ay + t * dy - glove.y(), z = ah + t * dh - height;
        return x * x + y * y + z * z <= radius * radius && Math.min(ah, bh) > 0;
    }
}
