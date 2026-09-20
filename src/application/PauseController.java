package application;

public final class PauseController {
    private boolean paused;

    public boolean toggle() {
        paused = !paused;
        return paused;
    }

    public boolean isPaused() { return paused; }
    public void resume() { paused = false; }
}
