package application;

public final class CatchAnimationController {
    public double catchRadius(Mascot mascot, GameSettings.Difficulty difficulty,
                              double ballHeight, double ballSpeed) {
        double base = difficulty.getCatchRadius();
        double heightPenalty = Math.max(0.72, 1.0 - Math.max(0, ballHeight - 55) / 500.0);
        double speedPenalty = Math.max(0.76, 1.0 - Math.max(0, ballSpeed - 180) / 1300.0);
        return base * mascot.getFieldingMultiplier() * heightPenalty * speedPenalty;
    }

    public String pose(PitchStyle style, Mascot mascot) {
        return mascot.getDisplayName() + " — " + style.catchAnimation();
    }
}
