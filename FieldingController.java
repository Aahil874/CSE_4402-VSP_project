package application;


public final class FieldingController {
    private WorldPoint target = new WorldPoint(540, 240);
    private double remaining;
    private boolean active;

    public void begin(WorldPoint target, GameSettings.Difficulty difficulty) {
        this.target = target;
        remaining = difficulty == GameSettings.Difficulty.EASY ? 1.65 : 1.15;
        active = true;
    }

    public void update(double elapsed) {
        if (active) {
            remaining = Math.max(0, remaining - elapsed);
            active = remaining > 0;
        }
    }

    public boolean attempt(WorldPoint pointer, GameSettings.Difficulty difficulty) {
        double radius = difficulty == GameSettings.Difficulty.EASY ? 92 : 66;
        boolean caught = active && pointer.distanceTo(target) <= radius;
        if (caught) {
            active = false;
        }
        return caught;
    }

    public WorldPoint target() { return target; }
    public double remaining() { return remaining; }
    public boolean active() { return active; }
    public void cancel() { active = false; }
}
