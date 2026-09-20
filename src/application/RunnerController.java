package application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Physical base runners; live-play runs are committed after the play is adjudicated. */
public final class RunnerController {
    private final BasePath path = new BasePath();
    private final List<Runner> runners = new ArrayList<>();
    private final Map<Runner, Double> prePlayBases = new IdentityHashMap<>();
    private int scoredRuns;
    private boolean livePlay;
    private Runner liveBatter;
    private int liveBases;
    private boolean manualRunning;

    /**
     * Hand base running to the player for the rest of this play. "Advance"
     * sends everyone to the next base; "hold" returns anyone between bases to
     * the one behind them and leaves anyone already standing on a base put.
     * Runners who are forced by the rules cannot be held short.
     */
    public void commandRunning(boolean advance){
        if(!livePlay)return;
        manualRunning=true;
        for(Runner runner:runners){
            if(runner.hasScored())continue;
            double position=runner.lapPosition();
            double target=advance?Math.floor(position+.0001)+1:Math.floor(position);
            runner.targetBase(Math.max(forcedFloor(runner),Math.min(4,target)));
        }
    }

    /**
     * The earliest base a runner may be held at. The batter always has to
     * reach first, and a runner is forced along only when every base behind
     * him is occupied too -- otherwise he is free to stay where he is.
     */
    private double forcedFloor(Runner runner){
        if(runner==liveBatter)return 1;
        double start=prePlayBases.getOrDefault(runner,runner.lapPosition());
        for(double base=start-1;base>=1;base--)if(!startedOn(base))return start;
        return Math.min(4,start+1);
    }
    private boolean startedOn(double base){
        return prePlayBases.values().stream().anyMatch(value->Math.abs(value-base)<.0001);
    }

    /** A sprint burst for everyone on the move; true when at least one fired. */
    public boolean dashRunners(){
        boolean fired=false;
        for(Runner runner:runners)fired|=runner.dash();
        return fired;
    }
    public boolean anyDashing(){return runners.stream().anyMatch(Runner::isDashing);}
    /** True once the player has taken the bases over for this play. */
    public boolean isManualRunning(){return manualRunning;}
    /** The fielding heuristic's advances, which manual control overrides. */
    public void autoAdvanceLiveRunners(int bases){if(!manualRunning)advanceLiveRunners(bases);}

    /** Awarded advances, including walks and already confirmed home runs. */
    public void advance(HitResult result, Mascot batter) {
        int bases = result.bases();
        if (bases <= 0) return;
        if (result == HitResult.WALK) advanceForcedRunners();
        else runners.forEach(runner -> runner.advanceBases(bases));
        Runner runner = new Runner(batter, 0, 0, path);
        runner.advanceBases(bases);
        runners.add(runner);
    }

    /** Begin movement at contact without awarding a single/double/triple or a score. */
    public void startLivePlay(Mascot batter) {
        if (livePlay) throw new IllegalStateException("Previous base-running play is still live");
        prePlayBases.clear();
        for (Runner runner : runners) prePlayBases.put(runner, runner.targetLapPosition());
        livePlay = true;
        manualRunning=false;
        liveBases = 1;
        runners.forEach(runner -> runner.advanceBases(1));
        liveBatter = new Runner(batter, 0, 0, path);
        liveBatter.advanceBases(1);
        runners.add(liveBatter);
    }

    /** Total bases from contact, not an additional increment on each update. */
    public void advanceLiveRunners(int bases) {
        if (!livePlay) return;
        liveBases = Math.max(liveBases, Math.max(1, Math.min(4, bases)));
        for (var entry : prePlayBases.entrySet())
            entry.getKey().targetBase(Math.min(4, entry.getValue() + liveBases));
        if (liveBatter != null) liveBatter.targetBase(liveBases);
    }

    public void awardHomeRun() {
        if (!livePlay) return;
        advanceLiveRunners(4);
        completeLivePlay();
    }

    public boolean batterReachedFirst() {
        return liveBatter != null && liveBatter.lapPosition() >= 1 - .00001;
    }
    public int batterBasesReached() {
        return liveBatter == null ? 0 : Math.min(4, (int)Math.floor(liveBatter.lapPosition() + .00001));
    }
    public Runner liveBatter() { return liveBatter; }

    public void forceOutBatter() {
        if (liveBatter != null) runners.remove(liveBatter);
        liveBatter = null;
    }

    /** A catch cancels the batter and returns the other runners to their original bases. */
    public void flyOut() {
        forceOutBatter();
        for (var entry : prePlayBases.entrySet()) entry.getKey().returnToBase(entry.getValue());
        livePlay = false;
        liveBases = 0;
        prePlayBases.clear();
    }

    /** Call only after a defensive result; the next update commits completed home arrivals. */
    public void completeLivePlay() {
        livePlay = false;
        liveBases = 0;
        prePlayBases.clear();
        commitScoredRunners();
    }

    private void advanceForcedRunners() {
        Runner first = runnerTargeting(1);
        if (first == null) return;
        Runner second = runnerTargeting(2), third = runnerTargeting(3);
        if (second != null && third != null) third.advanceBases(1);
        if (second != null) second.advanceBases(1);
        first.advanceBases(1);
    }
    private Runner runnerTargeting(int base) {
        return runners.stream().filter(runner -> Math.round(runner.targetLapPosition()) == base)
                .findFirst().orElse(null);
    }

    public void update(double elapsed) {
        for (Runner runner : runners) runner.update(elapsed, path);
        if (!livePlay) commitScoredRunners();
    }
    private void commitScoredRunners() {
        Iterator<Runner> iterator = runners.iterator();
        while (iterator.hasNext()) {
            Runner runner = iterator.next();
            if (runner.hasScored()) { scoredRuns++; iterator.remove(); }
        }
    }
    public int drainScoredRuns() { int count = scoredRuns; scoredRuns = 0; return count; }

    public void cancelLastBatterAdvance() {
        if (livePlay) { flyOut(); return; }
        if (!runners.isEmpty()) runners.remove(runners.size() - 1);
    }

    public boolean isOccupied(int baseIndex) {
        return runners.stream().anyMatch(runner -> !runner.isRunning()
                && Math.abs(runner.lapPosition() - baseIndex) < .0001 && !runner.hasScored());
    }
    public List<Runner> runnersBackToFront() {
        return runners.stream().sorted(Comparator.comparingDouble(runner -> runner.feet().y())).toList();
    }
    public double nextRunnerArrivalSeconds() {
        return runners.stream().filter(Runner::isRunning).mapToDouble(Runner::arrivalSeconds)
                .min().orElse(0);
    }
    public boolean hasMovingRunners() { return runners.stream().anyMatch(Runner::isRunning); }
    public boolean isLivePlay() { return livePlay; }
    public BasePath path() { return path; }
    public void clear() {
        runners.clear(); prePlayBases.clear(); scoredRuns = 0; liveBatter = null; livePlay = false;
    }
}
