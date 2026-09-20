package application;

public final class Baseball {
    private double x;
    private double y;
    private double height;
    private double velocityX;
    private double velocityY;
    private double velocityHeight;
    private double spin;
    private boolean active;
    private boolean homeRun;
    private boolean touchedGround;

    public void launch(double x, double y, double velocityX, double velocityY,
                       double velocityHeight, double spin, boolean homeRun) {
        this.x = x;
        this.y = y;
        this.height = 0;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityHeight = velocityHeight;
        this.spin = spin;
        // Requested hit types never establish a home run before crossing the fence.
        this.homeRun = false;
        this.touchedGround = false;
        this.active = true;
    }

    public void stop() { active = false; }
    public double x() { return x; }
    public double y() { return y; }
    public double height() { return height; }
    public double velocityX() { return velocityX; }
    public double velocityY() { return velocityY; }
    public double velocityHeight() { return velocityHeight; }
    public double spin() { return spin; }
    public boolean active() { return active; }
    public boolean homeRun() { return homeRun; }
    public boolean touchedGround() { return touchedGround; }
    void markGroundContact() { touchedGround = true; }
    void markHomeRun() { homeRun = true; }

    public void setMotion(double x, double y, double height, double velocityX,
                          double velocityY, double velocityHeight) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityHeight = velocityHeight;
    }
}
