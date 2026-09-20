package application;

public enum PitchType {
    FOUR_SEAM("FOUR-SEAM FASTBALL", 1.20, 0, 0.03, 96, 94, 82, 16, 20, "Rising heat"),
    TWO_SEAM("TWO-SEAM FASTBALL", 1.13, 16, 0.05, 92, 86, 78, 19, 25, "Arm-side run"),
    CURVEBALL("RAINBOW CURVE", 0.91, 48, 0.09, 76, 98, 68, 28, 43, "Deep vertical break"),
    SLIDER("NEON SLIDER", 1.03, -35, 0.07, 84, 91, 73, 24, 35, "Late glove-side cut"),
    CHANGEUP("CLOUD CHANGEUP", 0.78, 19, 0.05, 72, 79, 88, 13, 24, "Speed deception"),
    SINKER("ANCHOR SINKER", 1.06, 31, 0.07, 88, 87, 75, 22, 32, "Heavy downward dive"),
    CUTTER("FIRE CUTTER", 1.09, -22, 0.06, 90, 85, 80, 20, 29, "Sharp late cut"),
    FORKBALL("DRAGON FORK", 0.88, 43, 0.10, 78, 93, 65, 31, 47, "Sudden bottom drop"),
    SCREWBALL("VOLT SCREWBALL", 0.92, 39, 0.11, 80, 96, 63, 34, 52, "Reverse sweeping break"),
    KNUCKLEBALL("CHAOS KNUCKLER", 0.73, -46, 0.13, 66, 35, 57, 38, 58, "Unpredictable flutter");

    final String displayName;
    final double speedMultiplier;
    final double curvePixels;
    final double breakDifficulty;
    private final int speed, spin, accuracy, staminaCost, turboCost;
    private final String effect;

    PitchType(String name, double speedMultiplier, double curvePixels, double breakDifficulty,
              int speed, int spin, int accuracy, int staminaCost, int turboCost, String effect) {
        this.displayName = name; this.speedMultiplier = speedMultiplier;
        this.curvePixels = curvePixels; this.breakDifficulty = breakDifficulty;
        this.speed = speed; this.spin = spin; this.accuracy = accuracy;
        this.staminaCost = staminaCost; this.turboCost = turboCost; this.effect = effect;
    }
    public String displayName() { return displayName; }
    public double speedMultiplier() { return speedMultiplier; }
    public double curvePixels() { return curvePixels; }
    public double breakDifficulty() { return breakDifficulty; }
    public int speed() { return speed; }
    public int spin() { return spin; }
    public int accuracy() { return accuracy; }
    public int staminaCost() { return staminaCost; }
    public int turboCost() { return turboCost; }
    public String effect() { return effect; }
    @Override public String toString() { return displayName; }
}
