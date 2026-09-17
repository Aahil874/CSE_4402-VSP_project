package application;

/** Selects which side the human controls for the current session. */
public enum GameMode {
    GRAND_PRIX("GRAND PRIX"),
    BATTING_ONLY("BATTING ONLY"),
    BOWLING_ONLY("BOWLING ONLY");

    private final String displayName;

    GameMode(String displayName) { this.displayName = displayName; }
    @Override public String toString() { return displayName; }
}
