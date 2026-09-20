package application;

public record ScoreboardState(
        int playerRuns, int rivalRuns, int hits, int homeRuns,
        int balls, int strikes, int outs, int inning,
        int combo, int maxCombo, int pitchCount,
        int singles, int doubles, int triples, int walks, int strikeouts,
        boolean firstOccupied, boolean secondOccupied, boolean thirdOccupied) {
}
