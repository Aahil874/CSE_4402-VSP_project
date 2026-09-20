package application;

import java.util.function.Consumer;

public final class AudioController {
    private Consumer<Cue> cueSink = cue -> { };
    private boolean muted;

    public void setCueSink(Consumer<Cue> cueSink) {
        this.cueSink = cueSink == null ? cue -> { } : cueSink;
    }

    public void play(Cue cue) {
        if (!muted) {
            cueSink.accept(cue);
        }
    }

    public void setMuted(boolean muted) { this.muted = muted; }
    public boolean isMuted() { return muted; }

    public enum Cue { PITCH, BAT_CRACK, CATCH, CROWD_CHEER, OUT, VICTORY }
}
