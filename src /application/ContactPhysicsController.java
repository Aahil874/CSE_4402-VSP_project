package application;

public final class ContactPhysicsController {
    public record ContactMetrics(double quality, double exitVelocity, double launchAngle,
                                 double horizontalDirection, double spin) {}

    public ContactMetrics calculate(double timingError, double horizontalError,
                                    double verticalError, double pitchSpeed,
                                    Mascot batter, double difficultyMultiplier,
                                    boolean turbo) {
        double accuracy = 1 - clamp(Math.abs(timingError) * .008
                + Math.abs(horizontalError) * .006 + Math.abs(verticalError) * .005, 0, 1);
        double quality = clamp(accuracy * batter.getContactMultiplier()
                / Math.max(.75, difficultyMultiplier) + (turbo ? .08 : 0), 0, 1);
        double exit = 48 + quality * 65 * batter.getPowerMultiplier() + pitchSpeed * .08;
        double angle = -5 + quality * 43 - Math.abs(verticalError) * .08;
        double direction = clamp(horizontalError * .65, -42, 42);
        double spin = timingError * 1.8 + horizontalError * .6;
        return new ContactMetrics(quality, exit, angle, direction, spin);
    }
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
