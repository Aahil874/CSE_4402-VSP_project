package application;

public enum Base {
    HOME(0),
    FIRST(1),
    SECOND(2),
    THIRD(3);

    private final int index;

    Base(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }

    public static Base fromIndex(int index) {
        return values()[Math.floorMod(index, values().length)];
    }
}
