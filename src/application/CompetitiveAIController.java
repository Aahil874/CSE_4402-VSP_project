package application;

import java.util.Random;

public final class CompetitiveAIController {
    private int earlySwings;
    private int lateSwings;
    private PitchType previousPitch = PitchType.FOUR_SEAM;

    public PitchType choosePitch(Random random, int inning,
                                 GameSettings.Difficulty difficulty) {
        if (earlySwings > lateSwings + 1 && random.nextDouble() < 0.58) {
            previousPitch = random.nextBoolean() ? PitchType.CHANGEUP : PitchType.CURVEBALL;
            return previousPitch;
        }
        if (lateSwings > earlySwings + 1 && random.nextDouble() < 0.58) {
            previousPitch = random.nextBoolean() ? PitchType.FOUR_SEAM : PitchType.CUTTER;
            return previousPitch;
        }
        PitchType[] values = PitchType.values();
        int unlocked = switch (difficulty) {
            case EASY -> Math.min(5, 3 + inning);
            case NORMAL -> Math.min(8, 5 + inning);
            case HARD -> values.length;
        };
        PitchType candidate;
        int attempts = 0;
        do {
            candidate = values[random.nextInt(unlocked)];
            attempts++;
        } while (candidate == previousPitch && attempts < 4);
        previousPitch = candidate;
        return candidate;
    }

    public double chooseTarget(Random random, GameSettings.Difficulty difficulty,
                               double minimum, double maximum) {
        double center = (minimum + maximum) / 2;
        double half = (maximum - minimum) / 2;
        double controlled = (random.nextDouble() * 2 - 1)
                * half * (1.25 - difficulty.getAiAccuracy() * 0.55);
        return Math.max(minimum - 22, Math.min(maximum + 22, center + controlled));
    }

    public void recordSwing(double timingDelta, BattingController.ContactRegion region) {
        if (region == BattingController.ContactRegion.PERFECT) {
            earlySwings = Math.max(0, earlySwings - 1);
            lateSwings = Math.max(0, lateSwings - 1);
        } else if (timingDelta < -8) {
            earlySwings++;
        } else if (timingDelta > 8) {
            lateSwings++;
        }
    }
}
