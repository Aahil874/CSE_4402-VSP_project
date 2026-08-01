package application;

public final class MatchState {
    private final long startedAtNanos = System.nanoTime();
    private int playerRuns;
    private int rivalRuns;
    private int hits;
    private int homeRuns;
    private int singles;
    private int doubles;
    private int triples;
    private int walks;
    private int strikeouts;
    private int pitchCount;
    private int currentInning = 1;
    private int balls;
    private int strikes;
    private int outs;
    private int combo;
    private int maxCombo;
    private double turbo;
    private boolean firstOccupied;
    private boolean secondOccupied;
    private boolean thirdOccupied;

    public void addPlayerRuns(int runs) { playerRuns += Math.max(0, runs); }
    public void addRivalRuns(int runs) { rivalRuns += Math.max(0, runs); }
    public void recordHit(HitResult result) {
        hits++;
        switch (result) {
            case SINGLE -> singles++;
            case DOUBLE -> doubles++;
            case TRIPLE -> triples++;
            case HOME_RUN -> homeRuns++;
            default -> { }
        }
    }
    public void recordWalk() { walks++; }
    public void recordStrikeout() { strikeouts++; }
    public void recordPitch() { pitchCount++; }
    public void nextInning() { currentInning++; }
    public void addBall() { balls++; }
    public void addStrike() { strikes++; }
    public void addOut() { outs++; }
    public void resetCount() { balls = 0; strikes = 0; }
    public void resetHalfInning() { resetCount(); outs = 0; combo = 0; }
    public void incrementCombo() {
        combo++;
        maxCombo = Math.max(maxCombo, combo);
    }
    public void resetCombo() { combo = 0; }
    public void addTurbo(double amount) { turbo = Math.min(100, turbo + Math.max(0, amount)); }
    public void consumeTurbo() { turbo = 0; }
    public void setOccupiedBases(boolean first, boolean second, boolean third) {
        firstOccupied = first;
        secondOccupied = second;
        thirdOccupied = third;
    }

    public int playerRuns() { return playerRuns; }
    public int rivalRuns() { return rivalRuns; }
    public int hits() { return hits; }
    public int homeRuns() { return homeRuns; }
    public int singles() { return singles; }
    public int doubles() { return doubles; }
    public int triples() { return triples; }
    public int walks() { return walks; }
    public int strikeouts() { return strikeouts; }
    public int pitchCount() { return pitchCount; }
    public int currentInning() { return currentInning; }
    public int balls() { return balls; }
    public int strikes() { return strikes; }
    public int outs() { return outs; }
    public int combo() { return combo; }
    public int maxCombo() { return maxCombo; }
    public double turbo() { return turbo; }
    public boolean firstOccupied() { return firstOccupied; }
    public boolean secondOccupied() { return secondOccupied; }
    public boolean thirdOccupied() { return thirdOccupied; }
    public int durationSeconds() {
        return (int) Math.max(1,
                (System.nanoTime() - startedAtNanos) / 1_000_000_000L);
    }

    public ScoreboardState snapshot() {
        return new ScoreboardState(playerRuns, rivalRuns, hits, homeRuns,
                balls, strikes, outs, currentInning, combo, maxCombo, pitchCount,
                singles, doubles, triples, walks, strikeouts,
                firstOccupied, secondOccupied, thirdOccupied);
    }
}
