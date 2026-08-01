package application;

public final class CrowdController {
    private double energy = 0.25;

    public void react(Event event) {
        energy = Math.max(energy, switch (event) {
            case HOME_RUN, VICTORY -> 1.0;
            case HIT, STRIKEOUT, DIVING_CATCH -> 0.78;
            case WALK -> 0.48;
        });
    }

    public void update(double elapsed) {
        energy += (0.25 - energy) * Math.min(1, elapsed * 0.55);
    }

    public double energy() { return energy; }

    public enum Event { HIT, HOME_RUN, STRIKEOUT, WALK, DIVING_CATCH, VICTORY }
}
