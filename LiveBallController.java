package application;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Owns the game's one and only live Baseball instance. */
public final class LiveBallController {
    public enum State {
        HELD_BY_PITCHER, PITCHED, CONTACTED, AIRBORNE, ROLLING,
        HELD_BY_FIELDER, THROWN_BY_FIELDER, CAUGHT, DEAD_BALL
    }

    private final Baseball ball = new Baseball();
    private final BaseballPhysicsService physics = new BaseballPhysicsService();
    private final Deque<WorldPoint> trail = new ArrayDeque<>();
    private State state = State.DEAD_BALL;
    private double age;
    private double exitVelocity;
    private double launchAngle;
    private HitResult hitResult = HitResult.OUT;
    private WorldPoint throwTarget = new WorldPoint(0, 0);
    private String owner = "";

    public void holdByPitcher(String pitcherId, double releaseX, double releaseY) {
        owner = pitcherId;
        state = State.HELD_BY_PITCHER;
        age = 0;
        trail.clear();
        ball.setMotion(releaseX, releaseY, 0, 0, 0, 0);
        ball.stop();
    }

    public void releasePitch(double startX, double startY, double targetX, double targetY,
                             double speed, double spin, double breakPixels) {
        require(State.HELD_BY_PITCHER);
        owner = "";
        state = State.PITCHED;
        age = 0;
        trail.clear();
        double dx = targetX - startX;
        double dy = targetY - startY;
        double distance = Math.max(1, Math.hypot(dx, dy));
        ball.launch(startX, startY, speed * dx / distance, speed * dy / distance,
                0, spin + breakPixels * 0.5, false);
    }

    public void markPitchPosition(double x, double y, double vx, double vy) {
        if (state != State.PITCHED) return;
        remember();
        ball.setMotion(x, y, 0, vx, vy, 0);
    }

    public void launchBattedBall(double contactX, double contactY,
                                  double quality, HitResult result) {
        if (state == State.DEAD_BALL) {
            state = State.CONTACTED; // permits deterministic simulations and tests
        } else if (state == State.PITCHED) {
            state = State.CONTACTED;
        } else if (state != State.CONTACTED) {
            throw new IllegalStateException("Ball cannot be contacted from " + state);
        }
        hitResult = result;
        exitVelocity = 58 + quality * 62;
        launchAngle = switch (result) {
            case SINGLE -> 8 + quality * 7;
            case DOUBLE -> 18 + quality * 8;
            case TRIPLE -> 24 + quality * 8;
            case HOME_RUN -> 31 + quality * 12;
            default -> 12;
        };
        double radians = Math.toRadians(launchAngle);
        double screenSpeed = exitVelocity * 4.2;
        double lateral = result == HitResult.SINGLE ? 0.55 : 0.82;
        ball.launch(contactX, contactY,
                screenSpeed * Math.cos(radians) * lateral,
                -screenSpeed * Math.cos(radians) * 0.42,
                screenSpeed * Math.sin(radians),
                (quality - 0.5) * 34,
                result == HitResult.HOME_RUN);
        age = 0;
        trail.clear();
        state = ball.velocityHeight() > 1 ? State.AIRBORNE : State.ROLLING;
    }

    public void update(double elapsed) {
        if (!ball.active()) return;
        remember();
        age += elapsed;
        if (state == State.AIRBORNE || state == State.ROLLING || state == State.CONTACTED) {
            double beforeHeight = ball.height();
            physics.update(ball, elapsed);
            if (state == State.AIRBORNE && beforeHeight > 0 && ball.height() <= 0.01) {
                state = State.ROLLING;
            }
            double lifetime = ball.homeRun() ? 4.6 : 4.0;
            if (age >= lifetime || ball.x() > 1320 || ball.y() < -220) deadBall();
        } else if (state == State.THROWN_BY_FIELDER) {
            double dx = throwTarget.x() - ball.x();
            double dy = throwTarget.y() - ball.y();
            if (Math.hypot(dx, dy) <= 9) {
                ball.setMotion(throwTarget.x(), throwTarget.y(), 0, 0, 0, 0);
                state = State.CAUGHT;
                ball.stop();
            }
        }
    }

    public void field(String fielderId, WorldPoint glove) {
        if (state != State.AIRBORNE && state != State.ROLLING) return;
        owner = fielderId;
        ball.setMotion(glove.x(), glove.y(), 0, 0, 0, 0);
        ball.stop();
        state = State.HELD_BY_FIELDER;
        trail.clear();
    }

    public void throwFromFielder(WorldPoint glove, WorldPoint base, double speed) {
        require(State.HELD_BY_FIELDER);
        owner = "";
        throwTarget = base;
        double dx = base.x() - glove.x();
        double dy = base.y() - glove.y();
        double d = Math.max(1, Math.hypot(dx, dy));
        ball.launch(glove.x(), glove.y(), speed * dx / d, speed * dy / d, 45, 0, false);
        state = State.THROWN_BY_FIELDER;
        trail.clear();
    }

    public void caught(String fielderId, WorldPoint glove) {
        owner = fielderId;
        ball.setMotion(glove.x(), glove.y(), 0, 0, 0, 0);
        ball.stop();
        state = State.CAUGHT;
        trail.clear();
    }

    public void deadBall() {
        ball.stop();
        state = State.DEAD_BALL;
        owner = "";
        trail.clear();
    }

    private void remember() {
        trail.addFirst(screenPosition());
        while (trail.size() > 5) trail.removeLast();
    }

    private void require(State expected) {
        if (state != expected) throw new IllegalStateException("Expected " + expected + ", got " + state);
    }

    public State state() { return state; }
    public boolean isLive() {
        return state != State.DEAD_BALL && state != State.CAUGHT
                && state != State.HELD_BY_PITCHER && state != State.HELD_BY_FIELDER;
    }
    public String owner() { return owner; }
    public List<WorldPoint> trail() { return List.copyOf(trail); }
    public WorldPoint screenPosition() { return new WorldPoint(ball.x(), ball.y() - ball.height()); }
    public WorldPoint shadowPosition() { return new WorldPoint(ball.x(), ball.y()); }
    public double perspectiveScale() {
        return Math.max(0.42, Math.min(1.18, 0.62 + ball.y() / 760.0));
    }
    public Baseball ball() { return ball; }
    public double exitVelocity() { return exitVelocity; }
    public double launchAngle() { return launchAngle; }
    public HitResult hitResult() { return hitResult; }
}
