package application;


public final class PitchController {
    public double speed(double baseSpeed, PitchType type, double mascotMultiplier) {
        return Math.max(1, baseSpeed) * type.speedMultiplier()
                * Math.max(0.5, mascotMultiplier);
    }

    public double trajectoryY(double progress, double releaseY,
                              double targetY, PitchType type) {
        double t = Math.max(0, Math.min(1, progress));
        return releaseY + (targetY - releaseY) * t
                + Math.sin(t * Math.PI) * type.curvePixels();
    }

    public boolean isStrike(double ballY, double top, double bottom) {
        return ballY >= top && ballY <= bottom;
    }
}
