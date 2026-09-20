package application;


public final class MatchResultService {
    public Result resolve(MatchState state) {
        int comparison = Integer.compare(state.playerRuns(), state.rivalRuns());
        return comparison > 0 ? Result.WIN : comparison < 0 ? Result.LOSS : Result.TIE;
    }

    public enum Result { WIN, LOSS, TIE }
}
