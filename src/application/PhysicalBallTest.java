package application;

/** Deterministic world-scale physics, real fence crossings, prediction and possession tests. */
public final class PhysicalBallTest {
    public static void main(String[] args) {
        pitchHeightIsAuthoritative();
        boundaryAndFlight();
        wallRebound();
        predictionMatchesLiveFlight();
        throwFlightIsContinuous();
        System.out.println("PHYSICAL_BALL=PASS");
    }

    private static void pitchHeightIsAuthoritative() {
        LiveBallController live = new LiveBallController();
        live.holdByPitcher("P", 560, 368, 22);
        live.releasePitch(560, 368, 560, 610, 450, 0, 0);
        live.markPitchPosition(561, 490, 15, 2, 450, -5);
        live.update(.016);
        check(live.ball().height() == 15 && live.ball().y() == 490,
                "Pitch collision and display use the same controlled height");
        live.caught("C", new WorldPoint(561, 620), 12);
        Baseball identity = live.ball();
        live.moveHeldBall(new WorldPoint(562, 620), 13);
        check(live.ball() == identity && live.ball().height() == 13 && !live.ball().active(),
                "Held ball follows the same hand and identity");
    }

    private static void boundaryAndFlight() {
        LiveBallController live = new LiveBallController();
        live.launchContact(560, 610, 10, 110, 34, 0, -1, 0);
        boolean crossedOldKillLine = false;
        double time = 0;
        while (live.isLive() && time < 10) {
            live.update(1.0 / 120);
            time += 1.0 / 120;
            if (live.ball().y() < -220 && !FieldGeometry.isBeyondFence(live.shadowPosition()))
                crossedOldKillLine |= live.ball().active();
        }
        check(crossedOldKillLine, "Deep outfield must remain live beyond the old viewport boundary");
        check(time > 2, "A deep hit must visibly travel for multiple seconds");
        check(live.boundaryOutcome() == LiveBallController.BoundaryOutcome.HOME_RUN,
                "Fair airborne fence crossing is a homer");
        check(live.ball().height() > FieldGeometry.FENCE_HEIGHT, "Homer clears actual fence height");
        LiveBallController bounced = new LiveBallController();
        bounced.launchContact(560, -980, 100, 30, 0, 0, -1, 0);
        bounced.ball().markGroundContact();
        bounced.update(.20);
        check(bounced.boundaryOutcome() == LiveBallController.BoundaryOutcome.GROUND_RULE_DOUBLE,
                "Bounced ball leaving the field cannot become a home run");
    }

    private static void wallRebound() {
        LiveBallController live = new LiveBallController();
        live.launchContact(560, -980, 12, 30, 0, 0, -1, 0);
        live.update(.20);
        check(live.boundaryOutcome() == LiveBallController.BoundaryOutcome.NONE,
                "Low wall strike is not a homer");
        check(live.ball().velocityY() > 0, "Low drive rebounds back toward the field");
        check(live.isLive(), "Rebounded ball remains available for retrieval");
    }

    private static void predictionMatchesLiveFlight() {
        Baseball ball = new Baseball();
        ball.launch(560, 610, -230, -350, 220, 35, false);
        ball.setMotion(560, 610, 12, -230, -350, 220);
        BaseballPhysicsService physics = new BaseballPhysicsService();
        WorldPoint predicted = physics.predictGroundContact(ball);
        for (int i = 0; i < 1800 && !ball.touchedGround(); i++) physics.update(ball, 1.0 / 120);
        check(predicted.distanceTo(new WorldPoint(ball.x(), ball.y())) < .001,
                "Fielder prediction must match exact live drag/spin/gravity integration");
        check(ball.x() < 560, "Left contact stays left before spin can alter the flight");
    }

    private static void throwFlightIsContinuous() {
        LiveBallController live = new LiveBallController();
        live.launchGroundBall(560, 500, 0, -100, HitResult.SINGLE);
        WorldPoint glove = new WorldPoint(560, 368), first = new BasePath().point(Base.FIRST);
        live.field("P", glove, 18);
        live.throwFromFielder(glove, 18, first, 14, 315);
        double duration = glove.distanceTo(first) / 315;
        live.update(duration / 2);
        check(live.state() == LiveBallController.State.THROWN_BY_FIELDER, "Throw travels before reception");
        check(live.shadowPosition().distanceTo(glove.interpolate(first, .5)) < .001,
                "Throw advances continuously by distance and speed");
        check(live.ball().height() > 18, "Throw follows a gravity arc");
        live.update(duration / 2);
        check(live.state() == LiveBallController.State.CAUGHT && live.ball().height() == 14,
                "Throw is received at the base glove after flight");
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
