package application;

public final class GameSettings {
    public enum Difficulty {
        EASY("EASY", 255, 345, 0.20, 0.28, 1.75, 0.80, 0.88, 1.15, 88),
        NORMAL("NORMAL", 330, 465, 0.40, 0.39, 1.35, 0.91, 1.00, 1.00, 72),
        HARD("HARD", 420, 590, 0.62, 0.50, 1.02, 1.03, 1.16, 0.88, 58);

        private final String displayName;
        private final double minimumPitchSpeed, maximumPitchSpeed;
        private final double defensiveCatchChance, rivalContactChance;
        private final double fieldingReactionSeconds, aiAccuracy;
        private final double breakMultiplier, contactWindowMultiplier, catchRadius;

        Difficulty(String displayName, double minimumPitchSpeed,
                   double maximumPitchSpeed, double defensiveCatchChance,
                   double rivalContactChance, double fieldingReactionSeconds,
                   double aiAccuracy, double breakMultiplier,
                   double contactWindowMultiplier, double catchRadius) {
            this.displayName = displayName;
            this.minimumPitchSpeed = minimumPitchSpeed;
            this.maximumPitchSpeed = maximumPitchSpeed;
            this.defensiveCatchChance = defensiveCatchChance;
            this.rivalContactChance = rivalContactChance;
            this.fieldingReactionSeconds = fieldingReactionSeconds;
            this.aiAccuracy = aiAccuracy;
            this.breakMultiplier = breakMultiplier;
            this.contactWindowMultiplier = contactWindowMultiplier;
            this.catchRadius = catchRadius;
        }

        public double randomPitchSpeed(double randomValue) {
            double safeRandom = Math.max(0, Math.min(1, randomValue));
            return minimumPitchSpeed + safeRandom * (maximumPitchSpeed - minimumPitchSpeed);
        }
        public double getDefensiveCatchChance() { return defensiveCatchChance; }
        public double getRivalContactChance() { return rivalContactChance; }
        public double getFieldingReactionSeconds() { return fieldingReactionSeconds; }
        public double getAiAccuracy() { return aiAccuracy; }
        public double getBreakMultiplier() { return breakMultiplier; }
        public double getContactWindowMultiplier() { return contactWindowMultiplier; }
        public double getCatchRadius() { return catchRadius; }
        @Override public String toString() { return displayName; }
    }

    private static Difficulty difficulty = Difficulty.NORMAL;
    private static boolean strikeZoneVisible;
    private static boolean screenShakeEnabled = true;
    private GameSettings() {}
    public static Difficulty getDifficulty() { return difficulty; }
    public static void setDifficulty(Difficulty selected) { if (selected != null) difficulty = selected; }
    public static boolean isStrikeZoneVisible() { return strikeZoneVisible; }
    public static void setStrikeZoneVisible(boolean visible) { strikeZoneVisible = visible; }
    public static boolean isScreenShakeEnabled() { return screenShakeEnabled; }
    public static void setScreenShakeEnabled(boolean enabled) { screenShakeEnabled = enabled; }
}
