package application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public final class RunnerController {
    private final BasePath path = new BasePath();
    private final List<Runner> runners = new ArrayList<>();
    private int scoredRuns;
    private int nextLane;

    public void advance(HitResult result, Mascot batter) {
        int bases = result.bases();
        if (bases <= 0) {
            return;
        }
        if (result == HitResult.WALK) {
            advanceForcedRunners();
        } else {
            runners.stream()
                    .sorted(Comparator.comparingDouble(Runner::targetLapPosition).reversed())
                    .forEach(runner -> runner.advanceBases(bases));
        }
        double laneOffset = switch (nextLane++ % 3) {
            case 1 -> -8;
            case 2 -> 8;
            default -> 0;
        };
        Runner newRunner = new Runner(batter, 0, laneOffset, path);
        newRunner.advanceBases(bases);
        runners.add(newRunner);
    }

    private void advanceForcedRunners() {
        Runner first = runnerTargeting(1);
        if (first == null) {
            return;
        }
        Runner second = runnerTargeting(2);
        Runner third = runnerTargeting(3);
        if (second != null && third != null) {
            third.advanceBases(1);
        }
        if (second != null) {
            second.advanceBases(1);
        }
        first.advanceBases(1);
    }

    private Runner runnerTargeting(int base) {
        return runners.stream()
                .filter(runner -> Math.round(runner.targetLapPosition()) == base)
                .findFirst()
                .orElse(null);
    }

    public void update(double elapsed) {
        Iterator<Runner> iterator = runners.iterator();
        while (iterator.hasNext()) {
            Runner runner = iterator.next();
            runner.update(elapsed, path);
            if (runner.hasScored()) {
                scoredRuns++;
                iterator.remove();
            }
        }
    }

    public int drainScoredRuns() {
        int runs = scoredRuns;
        scoredRuns = 0;
        return runs;
    }

    public boolean isOccupied(int baseIndex) {
        return runners.stream().anyMatch(runner -> {
            int target = (int) Math.round(runner.targetLapPosition());
            return target == baseIndex && !runner.hasScored();
        });
    }

    public List<Runner> runnersBackToFront() {
        return runners.stream()
                .sorted(Comparator.comparingDouble(runner -> runner.feet().y()))
                .toList();
    }

    public BasePath path() { return path; }

    public void clear() {
        runners.clear();
        scoredRuns = 0;
    }
}
