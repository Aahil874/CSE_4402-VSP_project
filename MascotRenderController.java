package application;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public final class MascotRenderController {
    public enum Pose {
        IDLE, READY, PITCH_WINDUP, PITCH_RELEASE, BATTING_STANCE,
        SWING_ANTICIPATION, CONTACT, FOLLOW_THROUGH, RUNNING, BASE_ROUNDING,
        SLIDING, GROUND_FIELDING, CATCHING, THROWING, SAFE, OUT_REACTION
    }
    public record Frame(int column, int row, double footAnchorX, double footAnchorY,
                        double gloveAnchorX, double gloveAnchorY,
                        double handAnchorX, double handAnchorY,
                        double releaseAnchorX, double releaseAnchorY) {}

    private final Map<Pose, Frame> frames = new EnumMap<>(Pose.class);
    public MascotRenderController() {
        int index = 0;
        for (Pose pose : Pose.values()) {
            frames.put(pose, new Frame(index % 4, index / 4,
                    .50, .94, .68, .48, .54, .48, .76, .32));
            index++;
        }
    }
    public Frame frame(Pose pose) { return frames.get(pose); }
    public boolean isEstablishedMascot(Mascot mascot) {
        return mascot != null && EnumSet.allOf(Mascot.class).contains(mascot)
                && Mascot.values().length == 12;
    }
}
