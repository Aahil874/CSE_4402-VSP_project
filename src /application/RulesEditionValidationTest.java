package application;

import java.util.List;

public final class RulesEditionValidationTest {
    private RulesEditionValidationTest() {}
    public static void main(String[] args) {
        validatesSingleLiveBall();
        validatesRulesAndAssignments();
        validatesThreeDifficultiesAndRoster();
        System.out.println("RulesEditionValidationTest PASS");
    }

    private static void validatesSingleLiveBall() {
        LiveBallController live = new LiveBallController();
        Baseball same = live.ball();
        live.holdByPitcher("P", 560, 273);
        live.releasePitch(560, 273, 560, 548, 340, 20, 0);
        live.markPitchPosition(560, 530, 0, 340);
        live.launchBattedBall(560, 548, .9, HitResult.DOUBLE);
        require(same == live.ball(), "Contact must reuse the exact same baseball");
        require(live.state() == LiveBallController.State.AIRBORNE, "Batted ball must become airborne");
        live.field("SS", new WorldPoint(560, 320));
        live.throwFromFielder(new WorldPoint(560, 320), new WorldPoint(872, 280), 520);
        require(same == live.ball(), "Defensive throw must reuse the exact same baseball");
    }

    private static void validatesRulesAndAssignments() {
        BaseRunningDecisionController running = new BaseRunningDecisionController();
        require(running.forResult(HitResult.SINGLE, false).batterDestination() == 1, "single");
        require(running.forResult(HitResult.DOUBLE, false).batterDestination() == 2, "double");
        require(running.forResult(HitResult.TRIPLE, false).batterDestination() == 3, "triple");
        require(running.forResult(HitResult.HOME_RUN, false).allRunnersScore(), "home run");
        require(running.forResult(HitResult.OUT, true).tagUpRequired(), "fly out/tag up");
        DefensiveThrowController.ForcePlay play = new DefensiveThrowController().compare(
                new WorldPoint(560, 250), new WorldPoint(872, 280), 520, 190, 150);
        require(play.out(), "throw reaching base before runner must be an out");
        Baseball ball = new Baseball();
        ball.launch(560, 300, 80, -100, 150, 0, false);
        var assignment = new DefensiveAssignmentController().assign(
                List.of(new WorldPoint(300, 335), new WorldPoint(560, 250),
                        new WorldPoint(820, 335)), ball);
        require(assignment.primaryIndex() >= 0 && assignment.backupIndex() >= 0, "primary and backup");
    }

    private static void validatesThreeDifficultiesAndRoster() {
        require(GameSettings.Difficulty.values().length == 3, "Easy, Normal, Hard");
        require(Mascot.values().length == 12, "exact twelve-character roster");
        MascotRenderController render = new MascotRenderController();
        for (Mascot mascot : Mascot.values()) require(render.isEstablishedMascot(mascot), "roster");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
