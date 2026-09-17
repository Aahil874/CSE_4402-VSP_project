package application;

public final class FieldOutcomeClassifierTest {
    public static void main(String[] args) {
        Baseball ball = new Baseball();
        ball.launch(560, 610, 0, -300, 200, 0, true);
        check(!ball.homeRun(), "Requested trajectory cannot award a homer at contact");
        ball.setMotion(560, -850, 20, 0, 0, 0);
        check(FieldOutcomeClassifier.classify(ball) == HitResult.OUT,
                "Deep ball distance alone cannot award a hit");
        check(FieldOutcomeClassifier.classify(ball, 1) == HitResult.SINGLE, "first physically reached");
        check(FieldOutcomeClassifier.classify(ball, 2) == HitResult.DOUBLE, "second physically reached");
        check(FieldOutcomeClassifier.classify(ball, 3) == HitResult.TRIPLE, "third physically reached");
        ball.markHomeRun();
        check(FieldOutcomeClassifier.classify(ball) == HitResult.HOME_RUN, "confirmed boundary homer");
        System.out.println("FIELD_OUTCOME=PASS");
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
