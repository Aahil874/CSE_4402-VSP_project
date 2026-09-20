package application;

public final class BattingController {
    public Contact evaluate(double horizontalError, double timingError,
                            double generalWindow, double powerMultiplier,
                            boolean turbo) {
        double safeWindow = Math.max(1, generalWindow);
        double normalizedX = Math.abs(horizontalError) / safeWindow;
        double normalizedTiming = Math.abs(timingError) / safeWindow;
        double distance = Math.hypot(normalizedX, normalizedTiming);

        ContactRegion region;
        if (distance <= 0.14) {
            region = ContactRegion.PERFECT;
        } else if (distance <= 0.48) {
            region = ContactRegion.SOLID;
        } else if (distance <= 1.0) {
            region = ContactRegion.GENERAL;
        } else {
            return new Contact(ContactRegion.MISS, 0, 0, HitResult.OUT, false);
        }

        double quality = switch (region) {
            case PERFECT -> 0.96 + (0.14 - distance) / 0.14 * 0.04;
            case SOLID -> 0.66 + (0.48 - distance) / 0.34 * 0.28;
            case GENERAL -> 0.24 + (1.0 - distance) / 0.52 * 0.38;
            case MISS -> 0;
        };
        double turboBoost = turbo ? 0.12 : 0;
        double power = Math.min(1.35, quality * powerMultiplier + turboBoost);
        boolean foul = region == ContactRegion.GENERAL && distance > 0.78;

        HitResult result;
        if (region == ContactRegion.PERFECT && power >= 0.94) {
            result = HitResult.HOME_RUN;
        } else if (region == ContactRegion.SOLID && power >= 0.82) {
            result = HitResult.TRIPLE;
        } else if ((region == ContactRegion.SOLID && power >= 0.62)
                || (region == ContactRegion.PERFECT && power < 0.94)) {
            result = HitResult.DOUBLE;
        } else {
            result = HitResult.SINGLE;
        }
        return new Contact(region, quality, power, result, foul);
    }

    public enum ContactRegion {
        MISS, GENERAL, SOLID, PERFECT
    }

    public record Contact(ContactRegion region, double quality, double power,
                          HitResult result, boolean foul) {
        public boolean madeContact() { return region != ContactRegion.MISS; }
    }
}
