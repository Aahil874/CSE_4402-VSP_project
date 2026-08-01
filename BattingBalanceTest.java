package application;

public final class BattingBalanceTest {
    private BattingBalanceTest() {}

    public static void main(String[] args) {
        BattingController controller = new BattingController();
        double window = 82;

        BattingController.Contact perfect =
                controller.evaluate(0, 0, window, 1.05, false);
        BattingController.Contact solid =
                controller.evaluate(25, 8, window, 1.05, false);
        BattingController.Contact general =
                controller.evaluate(58, 10, window, 1.05, false);
        BattingController.Contact foul =
                controller.evaluate(72, 8, window, 1.05, false);
        BattingController.Contact miss =
                controller.evaluate(92, 50, window, 1.05, false);

        require(perfect.region() == BattingController.ContactRegion.PERFECT,
                "Center contact must be perfect.");
        require(perfect.result() == HitResult.HOME_RUN,
                "A sufficiently powerful perfect contact may become a home run.");
        require(solid.region() == BattingController.ContactRegion.SOLID,
                "Middle contact region must be solid.");
        require(general.region() == BattingController.ContactRegion.GENERAL
                        && !general.foul(),
                "General region must support playable weak contact.");
        require(foul.region() == BattingController.ContactRegion.GENERAL
                        && foul.foul(),
                "Outer general region must produce foul balls.");
        require(!miss.madeContact(), "Outside the general ellipse must miss.");

        require(GameSettings.Difficulty.EASY.getContactWindowMultiplier()
                        > GameSettings.Difficulty.NORMAL.getContactWindowMultiplier(),
                "Easy must have the most forgiving contact window.");
        require(GameSettings.Difficulty.NORMAL.getContactWindowMultiplier()
                        > GameSettings.Difficulty.HARD.getContactWindowMultiplier(),
                "Hard must have the tightest contact window.");
        require(GameSettings.Difficulty.HARD.randomPitchSpeed(0.5)
                        > GameSettings.Difficulty.NORMAL.randomPitchSpeed(0.5),
                "Hard pitches must be faster than Normal.");
        require(GameSettings.Difficulty.NORMAL.randomPitchSpeed(0.5)
                        > GameSettings.Difficulty.EASY.randomPitchSpeed(0.5),
                "Normal pitches must be faster than Easy.");
        System.out.println("BATTING_BALANCE_TEST=PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
