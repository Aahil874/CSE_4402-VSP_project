package application;

public final class CameraController {
    private double x;
    private double y;
    private double targetX;
    private double targetY;
    private double shake;

    public void follow(double targetX, double targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public void impact(double amount) {
        shake = Math.max(shake, Math.max(0, amount));
    }

    public void update(double elapsed) {
        double blend = 1 - Math.exp(-elapsed * 6.5);
        x += (targetX - x) * blend;
        y += (targetY - y) * blend;
        shake = Math.max(0, shake - elapsed * 28);
    }

    public double x() { return x; }
    public double y() { return y; }
    public double shake() { return shake; }
}
