package application;

/** Identifies the only pitcher, batter and catcher permitted to animate in a pitch. */
public final class PitchSequenceController {
    private Mascot pitcher = Mascot.TURBO_TANUKI;
    private Mascot batter = Mascot.BUBBLE_BUNNY;
    private Mascot catcher = Mascot.CIRCUIT_BOT;
    private long sequence;

    public void configure(Mascot pitcher, Mascot batter, Mascot catcher) {
        this.pitcher = pitcher;
        this.batter = batter;
        this.catcher = catcher;
        if (catcher == pitcher || catcher == batter) {
            for (Mascot candidate : Mascot.values()) {
                if (candidate != pitcher && candidate != batter) { this.catcher = candidate; break; }
            }
        }
        sequence++;
    }
    public boolean isPitcher(Mascot mascot) { return mascot == pitcher; }
    public boolean isBatter(Mascot mascot) { return mascot == batter; }
    public boolean isCatcher(Mascot mascot) { return mascot == catcher; }
    public Mascot pitcher() { return pitcher; }
    public Mascot batter() { return batter; }
    public Mascot catcher() { return catcher; }
    public long sequence() { return sequence; }
}

