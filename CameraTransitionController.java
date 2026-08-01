package application;

public final class CameraTransitionController {
    public enum View { BATTING, HIGH_ISOMETRIC }
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
    public View currentView() { return progress >= .5 ? to : from; }
    public View targetView() { return to; }
}
