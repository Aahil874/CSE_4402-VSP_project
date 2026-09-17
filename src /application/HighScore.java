package application;

import java.time.LocalDateTime;

public final class HighScore {
    private final int id;
    private final String playerName;
    private final int score;
    private final int rivalScore;
    private final int hits;
    private final int homeRuns;
    private final int innings;
    private final int maxCombo;
    private final Mascot mascot;
    private final GameSettings.Difficulty difficulty;
    private final LocalDateTime playedAt;
    private final int singles;
    private final int doubles;
    private final int triples;
    private final int walks;
    private final int strikeouts;
    private final int pitchCount;
    private final int durationSeconds;
    private final String opponentName;

    public HighScore(int id, String playerName, int score, int rivalScore,
                     int hits, int homeRuns, int innings, int maxCombo, Mascot mascot,
                     GameSettings.Difficulty difficulty, LocalDateTime playedAt) {
        this(id, playerName, score, rivalScore, hits, homeRuns, innings, maxCombo,
                mascot, difficulty, playedAt,
                0, 0, 0, 0, 0, 0, 0, "GRAND PRIX RIVALS");
    }

    public HighScore(int id, String playerName, int score, int rivalScore,
                     int hits, int homeRuns, int innings, int maxCombo, Mascot mascot,
                     GameSettings.Difficulty difficulty, LocalDateTime playedAt,
                     int singles, int doubles, int triples, int walks,
                     int strikeouts, int pitchCount, int durationSeconds,
                     String opponentName) {
        this.id = id;
        this.playerName = playerName;
        this.score = score;
        this.rivalScore = rivalScore;
        this.hits = hits;
        this.homeRuns = homeRuns;
        this.innings = innings;
        this.maxCombo = maxCombo;
        this.mascot = mascot;
        this.difficulty = difficulty;
        this.playedAt = playedAt;
        this.singles = singles;
        this.doubles = doubles;
        this.triples = triples;
        this.walks = walks;
        this.strikeouts = strikeouts;
        this.pitchCount = pitchCount;
        this.durationSeconds = durationSeconds;
        this.opponentName = opponentName;
    }

    public HighScore(int id, String playerName, int score, int rivalScore,
                     int hits, int innings, int maxCombo, Mascot mascot,
                     GameSettings.Difficulty difficulty, LocalDateTime playedAt) {
        this(id, playerName, score, rivalScore, hits, 0, innings, maxCombo,
                mascot, difficulty, playedAt);
    }

    public HighScore(String playerName, int score, int rivalScore, int hits,
                     int innings, int maxCombo, Mascot mascot,
                     GameSettings.Difficulty difficulty) {
        this(0, playerName, score, rivalScore, hits, 0, innings, maxCombo,
                mascot, difficulty, LocalDateTime.now());
    }

    public HighScore(String playerName, int score, int rivalScore, int hits,
                     int homeRuns, int innings, int maxCombo, Mascot mascot,
                     GameSettings.Difficulty difficulty) {
        this(0, playerName, score, rivalScore, hits, homeRuns, innings, maxCombo,
                mascot, difficulty, LocalDateTime.now());
    }

    public HighScore(String playerName, int score, int rivalScore, int hits,
                     int homeRuns, int innings, int maxCombo, Mascot mascot,
                     GameSettings.Difficulty difficulty,
                     int singles, int doubles, int triples, int walks,
                     int strikeouts, int pitchCount, int durationSeconds,
                     String opponentName) {
        this(0, playerName, score, rivalScore, hits, homeRuns, innings, maxCombo,
                mascot, difficulty, LocalDateTime.now(),
                singles, doubles, triples, walks, strikeouts, pitchCount,
                durationSeconds, opponentName);
    }

    public int getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public int getRivalScore() {
        return rivalScore;
    }

    public int getHits() {
        return hits;
    }

    public int getHomeRuns() {
        return homeRuns;
    }

    public int getInnings() {
        return innings;
    }

    public int getMaxCombo() {
        return maxCombo;
    }

    public Mascot getMascot() {
        return mascot;
    }

    public GameSettings.Difficulty getDifficulty() {
        return difficulty;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public int getSingles() { return singles; }
    public int getDoubles() { return doubles; }
    public int getTriples() { return triples; }
    public int getWalks() { return walks; }
    public int getStrikeouts() { return strikeouts; }
    public int getPitchCount() { return pitchCount; }
    public int getDurationSeconds() { return durationSeconds; }
    public String getOpponentName() { return opponentName; }
}
