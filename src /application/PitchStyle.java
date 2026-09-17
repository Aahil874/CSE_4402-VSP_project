package application;

public enum PitchStyle {
    CLASSIC_OVERHAND("Classic Overhand", 1.00, 1.04, 1.00, "HIGH-GLOVE CATCH"),
    THREE_QUARTER("Three-Quarter", 1.04, 1.00, 1.04, "CROSS-BODY CATCH"),
    SIDEARM("Sidearm Sweep", 1.01, 0.94, 1.16, "SIDE-SCOOP CATCH"),
    SUBMARINE("Submarine Rise", 0.94, 0.91, 1.25, "LOW-SCOOP CATCH"),
    POWER_WINDUP("Power Windup", 1.12, 0.89, 0.96, "CHEST-LOCK CATCH"),
    QUICK_STEP("Quick Step", 1.06, 0.98, 0.91, "QUICK-SNAP CATCH");

    private final String displayName, catchAnimation;
    private final double speed, control, breakAmount;
    PitchStyle(String name, double speed, double control, double breakAmount, String catchAnimation) {
        this.displayName = name; this.speed = speed; this.control = control;
        this.breakAmount = breakAmount; this.catchAnimation = catchAnimation;
    }
    public double speed() { return speed; }
    public double control() { return control; }
    public double breakAmount() { return breakAmount; }
    public String catchAnimation() { return catchAnimation; }
    @Override public String toString() { return displayName; }
}
