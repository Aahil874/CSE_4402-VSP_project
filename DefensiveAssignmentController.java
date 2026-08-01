package application;

import java.util.List;

public final class DefensiveAssignmentController {
    public record Assignment(int primaryIndex, int backupIndex, WorldPoint predictedLanding) {}

    public Assignment assign(List<WorldPoint> fielders, Baseball ball) {
        WorldPoint landing = predictLanding(ball);
        int primary = -1, backup = -1;
        double first = Double.MAX_VALUE, second = Double.MAX_VALUE;
        for (int i = 0; i < fielders.size(); i++) {
            double distance = fielders.get(i).distanceTo(landing);
            if (distance < first) {
                backup = primary; second = first; primary = i; first = distance;
            } else if (distance < second) {
                backup = i; second = distance;
            }
        }
        return new Assignment(primary, backup, landing);
    }

    public WorldPoint predictLanding(Baseball ball) {
        double t = ball.height() <= 0 ? .22 : Math.max(.08,
                (ball.velocityHeight() + Math.sqrt(Math.max(0,
                        ball.velocityHeight() * ball.velocityHeight() + 520 * ball.height()))) / 260);
        return new WorldPoint(ball.x() + ball.velocityX() * t * .82,
                ball.y() + ball.velocityY() * t * .82);
    }
}
