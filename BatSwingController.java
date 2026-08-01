package application;

public final class BatSwingController {
    public enum Phase { READY, LOAD, SWING, CONTACT, FOLLOW_THROUGH, RECOVERY }
    private Phase phase = Phase.READY;
    private double elapsed;

    public void begin() { phase = Phase.LOAD; elapsed = 0; }
    public void update(double dt) {
        elapsed += Math.max(0, dt);
        phase = elapsed < .07 ? Phase.LOAD
                : elapsed < .14 ? Phase.SWING
                : elapsed < .18 ? Phase.CONTACT
                : elapsed < .31 ? Phase.FOLLOW_THROUGH
                : elapsed < .43 ? Phase.RECOVERY : Phase.READY;
    }
    public Phase phase() { return phase; }
    public boolean collisionEnabled() { return phase == Phase.SWING || phase == Phase.CONTACT; }
}
