package application;

public final class CatchController {
    public boolean canCatch(WorldPoint glove, Baseball ball, double radius) {
        if (!ball.active() || ball.height() > radius * 1.35) return false;
        WorldPoint visibleBall = new WorldPoint(ball.x(), ball.y() - ball.height());
        return glove.distanceTo(visibleBall) <= Math.max(5, radius);
    }
    public boolean isFlyOut(LiveBallController.State state, int groundTouches) {
        return state == LiveBallController.State.CAUGHT && groundTouches == 0;
    }
}
