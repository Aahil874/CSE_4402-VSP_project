package application;

public final class CameraTransitionController {
    public enum View {
        BATTING, HIGH_ISOMETRIC, GROUND_BALL, LOW_LINE_DRIVE,
        HIGH_FLY_BALL, LEFT_FIELD, RIGHT_FIELD, DEEP_HOME_RUN
    }
    private View from = View.BATTING;
    private View to = View.BATTING;
    private double progress = 1;
    public void transitionTo(View view) {
        if (view == to) return;
        from = currentView();
        to = view;
        progress = 0;
    }
    public void update(double dt) { progress = Math.min(1, progress + Math.max(0, dt) / .38); }
    public double easedProgress() {
        return progress * progress * (3 - 2 * progress);
    }
    /** View being left during the current camera blend. */
    public View fromView() { return from; }
    /** Linear transition progress, exposed for render interpolation. */
    public double transitionProgress() { return easedProgress(); }
    public View currentView() { return progress >= .5 ? to : from; }
    public View targetView() { return to; }
}
