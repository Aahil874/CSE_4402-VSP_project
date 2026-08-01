package application;


public final class BaseballPhysicsService {
    private static final double GRAVITY = 260;
    private static final double AIR_DRAG = 0.18;

    public void update(Baseball ball, double elapsed) {
        if (!ball.active()) {
            return;
        }
        double drag = Math.max(0.72, 1 - AIR_DRAG * elapsed);
        double vx = ball.velocityX() * drag;
        double vy = ball.velocityY() * drag;
        double vh = ball.velocityHeight() - GRAVITY * elapsed;
        double x = ball.x() + vx * elapsed;
        double y = ball.y() + vy * elapsed + ball.spin() * elapsed * 0.035;
        double height = Math.max(0, ball.height() + vh * elapsed);
        if (height == 0 && vh < 0) {
            vh = ball.homeRun() ? 0 : -vh * 0.22;
            vx *= 0.72;
            vy *= 0.72;
        }
        ball.setMotion(x, y, height, vx, vy, vh);
    }
}
