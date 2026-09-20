package application;

/** Ball motion in field units (four units per foot), independent of camera pixels. */
public final class BaseballPhysicsService {
    public static final double GRAVITY = 32.174 * 4.0;
    public static final double AIR_DRAG = 0.10;
    public static final double MPH_TO_WORLD_PER_SECOND = 5280.0 / 3600.0 * 4.0;
    private static final double ROLLING_FRICTION = 21.0;
    private static final double STEP = 1.0 / 120.0;

    public void update(Baseball ball, double elapsed) {
        if (!ball.active() || !Double.isFinite(elapsed) || elapsed <= 0) return;
        double remaining = elapsed;
        while (remaining > .000001) {
            double dt = Math.min(STEP, remaining);
            step(ball, dt);
            remaining -= dt;
        }
    }

    private void step(Baseball ball, double dt) {
        boolean rolling = ball.height() <= .001 && ball.velocityHeight() <= 0;
        double vx = ball.velocityX(), vy = ball.velocityY(), vh = ball.velocityHeight();
        double oldVx = vx, oldVy = vy, oldVh = vh;
        if (rolling) {
            double speed = Math.hypot(vx, vy);
            double after = Math.max(0, speed - ROLLING_FRICTION * dt);
            if (speed > 0) { vx *= after / speed; vy *= after / speed; }
            vh = 0;
        } else {
            double drag = Math.exp(-AIR_DRAG * dt);
            // Spin bends flight perpendicular to travel, not toward a fixed screen edge.
            double turn = ball.spin() * .00030 * dt;
            vx = (oldVx * Math.cos(turn) - oldVy * Math.sin(turn)) * drag;
            vy = (oldVx * Math.sin(turn) + oldVy * Math.cos(turn)) * drag;
            vh -= GRAVITY * dt;
        }
        double x = ball.x() + (oldVx + vx) * .5 * dt;
        double y = ball.y() + (oldVy + vy) * .5 * dt;
        double height = rolling ? 0 : ball.height() + (oldVh + vh) * .5 * dt;
        if (height <= 0 && !rolling && vh < 0) {
            ball.markGroundContact();
            height = 0;
            vh = -vh * .22;
            if (vh < 9) vh = 0;
            vx *= .78;
            vy *= .78;
        }
        ball.setMotion(x, y, Math.max(0, height), vx, vy, vh);
    }

    /** Predict with the exact live integrator, including drag, spin and gravity. */
    public WorldPoint predictGroundContact(Baseball source) {
        Baseball prediction = new Baseball();
        prediction.launch(source.x(), source.y(), source.velocityX(), source.velocityY(),
                source.velocityHeight(), source.spin(), false);
        prediction.setMotion(source.x(), source.y(), source.height(), source.velocityX(),
                source.velocityY(), source.velocityHeight());
        if (source.height() <= 0 && source.velocityHeight() <= 0)
            return new WorldPoint(source.x(), source.y());
        for (int step = 0; step < 1800; step++) {
            update(prediction, 1.0 / 120.0);
            if (prediction.touchedGround()) break;
        }
        return new WorldPoint(prediction.x(), prediction.y());
    }
}
