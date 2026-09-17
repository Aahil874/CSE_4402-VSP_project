package application;

public enum HitResult {
    OUT(0),
    WALK(1),
    SINGLE(1),
    DOUBLE(2),
    TRIPLE(3),
    HOME_RUN(4);

    private final int bases;

    HitResult(int bases) {
        this.bases = bases;
    }

    public int bases() {
        return bases;
    }
}
