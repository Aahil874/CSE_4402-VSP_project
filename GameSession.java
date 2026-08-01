package application;

import java.util.List;

public final class GameSession {
    private static String playerName = "PLAYER 1";
    private static Mascot mascot = Mascot.TURBO_TANUKI;
    private static int score;
    private static int rivalScore;
    private static int hits;
    private static int homeRuns;
    private static int maxCombo;
    private static int innings = 1;
    private static int singles;
    private static int doubles;
    private static int triples;
    private static int walks;
    private static int strikeouts;
    private static int pitchCount;
    private static int durationSeconds;
    private static String opponentName = "GRAND PRIX RIVALS";
    private static GameSettings.Difficulty difficulty = GameSettings.Difficulty.EASY;
    private static PitchStyle pitchStyle = PitchStyle.CLASSIC_OVERHAND;
    private static List<PitchType> pitchLoadout = List.of(
            PitchType.FOUR_SEAM, PitchType.CURVEBALL, PitchType.SLIDER, PitchType.CHANGEUP);
    private static TeamLineup playerLineup = TeamGenerationController.generate(mascot);
    private static TeamLineup rivalLineup = TeamGenerationController.generateRival(mascot);

    private GameSession() {
    }

    public static void start(String name) {
        String cleanedName = name == null ? "" : name.trim();
        playerName = cleanedName.isEmpty() ? "PLAYER 1" : cleanedName;
        score = 0;
        rivalScore = 0;
        hits = 0;
        homeRuns = 0;
        maxCombo = 0;
        innings = 1;
        singles = 0;
        doubles = 0;
        triples = 0;
        walks = 0;
        strikeouts = 0;
        pitchCount = 0;
        durationSeconds = 0;
        difficulty = GameSettings.getDifficulty();
    }

    public static void selectMascot(Mascot selectedMascot) {
        if (selectedMascot != null) {
            mascot = selectedMascot;
            playerLineup = TeamGenerationController.generate(mascot);
            rivalLineup = TeamGenerationController.generateRival(mascot);
        }
    }

    public static void configurePitching(PitchStyle style, List<PitchType> loadout) {
        if (style != null) pitchStyle = style;
        if (loadout != null && loadout.size() == 4 && loadout.stream().distinct().count() == 4) {
            pitchLoadout = List.copyOf(loadout);
        }
    }

    public static void finish(int finalScore, int finalRivalScore,
                              int finalHits, int finalMaxCombo) {
        finish(finalScore, finalRivalScore, finalHits, 0, finalMaxCombo, 1);
    }

    public static void finish(int finalScore, int finalRivalScore,
                              int finalHits, int finalHomeRuns,
                              int finalMaxCombo, int completedInnings) {
        score = Math.max(0, finalScore);
        rivalScore = Math.max(0, finalRivalScore);
        hits = Math.max(0, finalHits);
        homeRuns = Math.max(0, finalHomeRuns);
        maxCombo = Math.max(0, finalMaxCombo);
        innings = Math.max(1, completedInnings);
    }

    public static void finish(MatchState state, String opponent) {
        finish(state.playerRuns(), state.rivalRuns(), state.hits(),
                state.homeRuns(), state.maxCombo(), state.currentInning());
        singles = state.singles();
        doubles = state.doubles();
        triples = state.triples();
        walks = state.walks();
        strikeouts = state.strikeouts();
        pitchCount = state.pitchCount();
        durationSeconds = state.durationSeconds();
        opponentName = opponent == null || opponent.isBlank()
                ? "GRAND PRIX RIVALS" : opponent;
    }

    public static String getPlayerName() {
        return playerName;
    }

    public static Mascot getMascot() {
        return mascot;
    }

    public static int getScore() {
        return score;
    }

    public static int getRivalScore() {
        return rivalScore;
    }

    public static int getHits() {
        return hits;
    }

    public static int getHomeRuns() {
        return homeRuns;
    }

    public static int getMaxCombo() {
        return maxCombo;
    }

    public static int getInnings() {
        return innings;
    }

    public static GameSettings.Difficulty getDifficulty() {
        return difficulty;
    }

    public static int getSingles() { return singles; }
    public static int getDoubles() { return doubles; }
    public static int getTriples() { return triples; }
    public static int getWalks() { return walks; }
    public static int getStrikeouts() { return strikeouts; }
    public static int getPitchCount() { return pitchCount; }
    public static int getDurationSeconds() { return durationSeconds; }
    public static String getOpponentName() { return opponentName; }
    public static PitchStyle getPitchStyle() { return pitchStyle; }
    public static List<PitchType> getPitchLoadout() { return pitchLoadout; }
    public static TeamLineup getPlayerLineup() { return playerLineup; }
    public static TeamLineup getRivalLineup() { return rivalLineup; }
}
