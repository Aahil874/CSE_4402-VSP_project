package application;

public enum PlayerPosition {
    PITCHER("P"), CATCHER("C"), FIRST_BASE("1B"), SECOND_BASE("2B"),
    THIRD_BASE("3B"), SHORTSTOP("SS"), LEFT_FIELD("LF"), CENTER_FIELD("CF"), RIGHT_FIELD("RF");
    private final String shortName;
    PlayerPosition(String shortName) { this.shortName = shortName; }
    @Override public String toString() { return shortName; }
}
