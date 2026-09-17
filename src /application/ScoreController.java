package application;


public final class ScoreController {
    private final MatchState match;

    public ScoreController(MatchState match) {
        this.match = match;
    }

    public ScoreboardState snapshot() {
        return match.snapshot();
    }

    public String countText() {
        ScoreboardState state = snapshot();
        return "B " + state.balls() + "  S " + state.strikes()
                + "  O " + state.outs();
    }

    public String baseDiamondText() {
        ScoreboardState state = snapshot();
        return (state.secondOccupied() ? "\u25C6" : "\u25C7") + System.lineSeparator()
                + (state.thirdOccupied() ? "\u25C6" : "\u25C7") + " "
                + (state.firstOccupied() ? "\u25C6" : "\u25C7");
    }
}
