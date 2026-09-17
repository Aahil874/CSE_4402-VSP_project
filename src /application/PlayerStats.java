package application;

import java.time.LocalDateTime;

public final class PlayerStats {
    private final String playerName;
    private final int gamesPlayed;
    private final int wins;
    private final int losses;
    private final int ties;
    private final int totalRuns;
    private final int totalRivalRuns;
    private final int totalHits;
    private final int homeRuns;
    private final int bestScore;
    private final int bestCombo;
    private final Mascot lastMascot;
    private final GameSettings.Difficulty lastDifficulty;
    private final LocalDateTime lastPlayed;

    public PlayerStats(String playerName, int gamesPlayed, int wins, int losses,
                       int ties, int totalRuns, int totalRivalRuns, int totalHits,
                       int homeRuns, int bestScore, int bestCombo, Mascot lastMascot,
                       GameSettings.Difficulty lastDifficulty, LocalDateTime lastPlayed) {
        this.playerName = playerName;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.losses = losses;
        this.ties = ties;
        this.totalRuns = totalRuns;
        this.totalRivalRuns = totalRivalRuns;
        this.totalHits = totalHits;
        this.homeRuns = homeRuns;
        this.bestScore = bestScore;
        this.bestCombo = bestCombo;
        this.lastMascot = lastMascot;
        this.lastDifficulty = lastDifficulty;
        this.lastPlayed = lastPlayed;
    }

    public String getPlayerName() { return playerName; }
    public int getGamesPlayed() { return gamesPlayed; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getTies() { return ties; }
    public int getTotalRuns() { return totalRuns; }
    public int getTotalRivalRuns() { return totalRivalRuns; }
    public int getTotalHits() { return totalHits; }
    public int getHomeRuns() { return homeRuns; }
    public int getBestScore() { return bestScore; }
    public int getBestCombo() { return bestCombo; }
    public Mascot getLastMascot() { return lastMascot; }
    public GameSettings.Difficulty getLastDifficulty() { return lastDifficulty; }
    public LocalDateTime getLastPlayed() { return lastPlayed; }

    public int getChampionshipPoints() {
        return wins * 3 + ties;
    }

    public double getWinPercentage() {
        return gamesPlayed == 0 ? 0 : wins * 100.0 / gamesPlayed;
    }
}
