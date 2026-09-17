package application;

import java.util.EnumMap;
import java.util.Map;


public final class MascotAnimationController {
    public static final int EXPRESSION_CELL_WIDTH = 256;
    public static final int EXPRESSION_CELL_HEIGHT = 256;
    private static final double RUN_FRAME_DURATION = 0.09;

    private final Map<MascotAnimationState, Integer> expressionColumns =
            new EnumMap<>(MascotAnimationState.class);
    private final Map<MascotAnimationState, Integer> fieldingColumns =
            new EnumMap<>(MascotAnimationState.class);
    private double animationClock;

    public MascotAnimationController() {
        expressionColumns.put(MascotAnimationState.READY, 0);
        expressionColumns.put(MascotAnimationState.DETERMINED, 0);
        expressionColumns.put(MascotAnimationState.HAPPY, 1);
        expressionColumns.put(MascotAnimationState.HOME_RUN_CELEBRATION, 1);
        expressionColumns.put(MascotAnimationState.SURPRISED, 2);
        expressionColumns.put(MascotAnimationState.ANGRY, 3);
        expressionColumns.put(MascotAnimationState.STRIKEOUT_REACTION, 4);
        expressionColumns.put(MascotAnimationState.LOSING_REACTION, 4);
        expressionColumns.put(MascotAnimationState.TIRED, 4);
        expressionColumns.put(MascotAnimationState.VICTORY, 5);
        expressionColumns.put(MascotAnimationState.TEAM_CELEBRATION, 5);
        fieldingColumns.put(MascotAnimationState.SLIDING, 0);
        fieldingColumns.put(MascotAnimationState.FIELDING, 1);
        fieldingColumns.put(MascotAnimationState.CATCHING, 2);
        fieldingColumns.put(MascotAnimationState.THROWING, 3);
        fieldingColumns.put(MascotAnimationState.SAFE_CELEBRATION, 4);
        fieldingColumns.put(MascotAnimationState.STRIKEOUT_REACTION, 5);
        fieldingColumns.put(MascotAnimationState.LOSING_REACTION, 5);
    }

    public void update(double elapsed) {
        animationClock += Math.max(0, elapsed);
    }

    public AtlasFrame expressionFrame(Mascot mascot, MascotAnimationState state) {
        int column = expressionColumns.getOrDefault(state, 0);
        return new AtlasFrame(column * EXPRESSION_CELL_WIDTH,
                mascot.ordinal() * EXPRESSION_CELL_HEIGHT,
                EXPRESSION_CELL_WIDTH, EXPRESSION_CELL_HEIGHT,
                0.16, true, 0.5, 0.96);
    }

    public AtlasFrame fieldingFrame(Mascot mascot, MascotAnimationState state) {
        int column = fieldingColumns.getOrDefault(state, 1);
        return new AtlasFrame(column * EXPRESSION_CELL_WIDTH,
                mascot.ordinal() * EXPRESSION_CELL_HEIGHT,
                EXPRESSION_CELL_WIDTH, EXPRESSION_CELL_HEIGHT,
                0.14, false, 0.5, 0.96);
    }

    public double runBob() {
        return Math.sin(animationClock / RUN_FRAME_DURATION * Math.PI) * 1.8;
    }

    public double idleBreath() {
        return 1 + Math.sin(animationClock * 2.1) * 0.012;
    }

    public record AtlasFrame(int x, int y, int width, int height,
                             double duration, boolean looping,
                             double anchorX, double anchorY) {
    }
}
