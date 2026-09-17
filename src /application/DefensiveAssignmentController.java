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
        return new BaseballPhysicsService().predictGroundContact(ball);
    }
}
