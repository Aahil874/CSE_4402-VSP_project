package application;


public final class InningController {
    private int inning = 1;
    private InningState.Half half = InningState.Half.TOP;
    private int outs;

    public Transition recordOut(int totalInnings) {
        outs++;
        if (outs < 3) {
            return Transition.CONTINUE;
        }
        outs = 0;
        if (half == InningState.Half.TOP) {
            half = InningState.Half.BOTTOM;
            return Transition.SWITCH_TO_BOTTOM;
        }
        if (inning >= totalInnings) {
            return Transition.GAME_OVER;
        }
        inning++;
        half = InningState.Half.TOP;
        return Transition.NEXT_INNING;
    }

    public InningState state() { return new InningState(inning, half, outs); }

    public enum Transition {
        CONTINUE, SWITCH_TO_BOTTOM, NEXT_INNING, GAME_OVER
    }
}
