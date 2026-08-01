package application;

public record InningState(int inning, Half half, int outs) {
    public enum Half { TOP, BOTTOM }

    public InningState {
        inning = Math.max(1, inning);
        outs = Math.max(0, Math.min(3, outs));
    }
}
