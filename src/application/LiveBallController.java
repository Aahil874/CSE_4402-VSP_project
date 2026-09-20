package application;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Owns the game's one live Baseball and its physical possession/boundary state. */
public final class LiveBallController {
    public enum State {
        HELD_BY_PITCHER, PITCHED, CONTACTED, AIRBORNE, ROLLING,
        HELD_BY_FIELDER, THROWN_BY_FIELDER, CAUGHT, DEAD_BALL
    }
    public enum BoundaryOutcome { NONE, HOME_RUN, GROUND_RULE_DOUBLE, FOUL }
    public record TrailPoint(double x, double y, double height) { }

    private final Baseball ball = new Baseball();
    private final BaseballPhysicsService physics = new BaseballPhysicsService();
    private final Deque<WorldPoint> trail = new ArrayDeque<>();
    private final Deque<TrailPoint> worldTrail = new ArrayDeque<>();
    private State state = State.DEAD_BALL;
    private BoundaryOutcome boundaryOutcome = BoundaryOutcome.NONE;
    private double age;
    private double exitVelocity;
    private double launchAngle;
    private boolean groundBall;
    private HitResult hitResult = HitResult.OUT;
    private WorldPoint throwTarget = new WorldPoint(0, 0);
    private double throwStartHeight, throwTargetHeight, throwDuration, throwAge;
    private WorldPoint throwStart;
    private String owner = "";

    public void holdByPitcher(String pitcherId, double releaseX, double releaseY) {
        holdByPitcher(pitcherId, releaseX, releaseY, 21);
    }
    public void holdByPitcher(String pitcherId, double releaseX, double releaseY, double height) {
        owner = pitcherId;
        state = State.HELD_BY_PITCHER;
        resetPlay();
        ball.launch(releaseX, releaseY, 0, 0, 0, 0, false);
        ball.setMotion(releaseX, releaseY, height, 0, 0, 0);
        ball.stop();
    }

    public void releasePitch(double startX, double startY, double targetX, double targetY,
                             double speed, double spin, double breakPixels) {
        require(State.HELD_BY_PITCHER);
        double height = ball.height();
        owner = "";
        state = State.PITCHED;
        resetPlay();
        double dx = targetX - startX, dy = targetY - startY;
        double distance = Math.max(1, Math.hypot(dx, dy));
        ball.launch(startX, startY, speed * dx / distance, speed * dy / distance,
                0, spin + breakPixels * 0.5, false);
        ball.setMotion(startX, startY, height, speed * dx / distance, speed * dy / distance, 0);
    }

    public void markPitchPosition(double x, double y, double vx, double vy) {
        markPitchPosition(x, y, ball.height(), vx, vy, ball.velocityHeight());
    }

    /** Rendering and collision consumers read this same position and height. */
    public void markPitchPosition(double x, double y, double height, double vx, double vy, double vh) {
        if (state != State.PITCHED) return;
        remember();
        ball.setMotion(x, y, Math.max(0, height), vx, vy, vh);
    }

    public void launchBattedBall(double contactX, double contactY, double quality, HitResult result) {
        launchBattedBall(contactX, contactY, quality, result, 1.0, -0.42);
    }

    /** Direction is in world coordinates: x left/right, negative y toward center field. */
    public void launchBattedBall(double contactX, double contactY, double quality, HitResult result,
                                double directionX, double directionY) {
        quality = Math.max(0, Math.min(1, quality));
        double angle = switch (result) {
            case SINGLE -> 12 + quality * 8;
            case DOUBLE -> 20 + quality * 9;
            case TRIPLE -> 26 + quality * 10;
            case HOME_RUN -> 31 + quality * 9;
            default -> 16;
        };
        launchContact(contactX, contactY, state == State.PITCHED ? ball.height() : 10,
                58 + quality * 62, angle, directionX, directionY, (quality - .5) * 34);
        hitResult = result; // A trajectory hint only; scoring must use physical outcomes.
    }

    /** Exact contact metrics can be supplied without preselecting a hit result. */
    public void launchContact(double x, double y, double height, double speedMph, double angleDegrees,
                              double directionX, double directionY, double spin) {
        if (state != State.DEAD_BALL && state != State.PITCHED && state != State.CONTACTED)
            throw new IllegalStateException("Ball cannot be contacted from " + state);
        resetPlay();
        owner = "";
        exitVelocity = Math.max(1, speedMph);
        launchAngle = Math.max(-20, Math.min(75, angleDegrees));
        double directionLength = Math.hypot(directionX, directionY);
        if (directionLength < .001) { directionX = 0; directionY = -1; directionLength = 1; }
        double speed = exitVelocity * BaseballPhysicsService.MPH_TO_WORLD_PER_SECOND;
        double radians = Math.toRadians(launchAngle);
        double vx = speed * Math.cos(radians) * directionX / directionLength;
        double vy = speed * Math.cos(radians) * directionY / directionLength;
        double vh = speed * Math.sin(radians);
        ball.launch(x, y, vx, vy, vh, spin, false);
        ball.setMotion(x, y, Math.max(0, height), vx, vy, vh);
        state = State.AIRBORNE;
        groundBall = launchAngle < 8;
    }

    public void launchGroundBall(double x, double y, double vx, double vy, HitResult result) {
        launchGroundBall(x, y, 0, vx, vy, result);
    }

    public void launchGroundBall(double x, double y, double height, double vx, double vy, HitResult result) {
        if (state != State.DEAD_BALL && state != State.PITCHED && state != State.CONTACTED)
            throw new IllegalStateException("Ball cannot be contacted from " + state);
        resetPlay();
        owner = "";
        hitResult = result;
        groundBall = true;
        exitVelocity = Math.hypot(vx, vy) / BaseballPhysicsService.MPH_TO_WORLD_PER_SECOND;
        launchAngle = 4;
        double vh = height > 0 ? -24 : 16;
        ball.launch(x, y, vx, vy, vh, 0, false);
        ball.setMotion(x, y, Math.max(0, height), vx, vy, vh);
        state = State.AIRBORNE;
    }

    public void update(double elapsed) {
        if (!ball.active() || !Double.isFinite(elapsed) || elapsed <= 0) return;
        if (state == State.PITCHED) return; // Controller advances the physical pitch exactly once.
        remember();
        age += elapsed;
        if (state == State.THROWN_BY_FIELDER) {
            updateThrow(elapsed);
            return;
        }
        if (state != State.AIRBORNE && state != State.ROLLING && state != State.CONTACTED) return;
        double remaining = elapsed;
        while (remaining > .000001 && ball.active()) {
            double step = Math.min(1.0 / 120.0, remaining);
            WorldPoint before = shadowPosition();
            double oldHeight = ball.height();
            boolean bouncedBefore = ball.touchedGround();
            physics.update(ball, step);
            if (ball.touchedGround()) state = State.ROLLING;
            inspectFence(before, oldHeight, bouncedBefore);
            remaining -= step;
        }
        // Foul territory uses the actual trajectory. Near-home grounders can roll fair.
        if (ball.active() && !FieldGeometry.isFair(shadowPosition())
                && ball.touchedGround()
                && (FieldGeometry.distanceFromHome(shadowPosition()) > FieldGeometry.BASE_SIDE
                    || Math.hypot(ball.velocityX(), ball.velocityY()) < 1)) {
            boundaryOutcome = BoundaryOutcome.FOUL;
            endAtCurrentPosition();
        }
        // A stationary ball remains available for retrieval. This guard only prevents a stuck play.
        if (age > 30) endAtCurrentPosition();
    }

    private void inspectFence(WorldPoint before, double oldHeight, boolean bouncedBefore) {
        WorldPoint after = shadowPosition();
        if (!FieldGeometry.isFair(after) || !FieldGeometry.isBeyondFence(after)
                || FieldGeometry.isBeyondFence(before)) return;
        double lo = 0, hi = 1;
        for (int i = 0; i < 24; i++) {
            double mid = (lo + hi) * .5;
            if (FieldGeometry.isBeyondFence(before.interpolate(after, mid))) hi = mid;
            else lo = mid;
        }
        WorldPoint crossing = before.interpolate(after, hi);
        double height = oldHeight + (ball.height() - oldHeight) * hi;
        if (height > FieldGeometry.FENCE_HEIGHT) {
            boundaryOutcome = bouncedBefore ? BoundaryOutcome.GROUND_RULE_DOUBLE : BoundaryOutcome.HOME_RUN;
            if (boundaryOutcome == BoundaryOutcome.HOME_RUN) ball.markHomeRun();
            ball.setMotion(crossing.x(), crossing.y(), height, 0, 0, 0);
            endAtCurrentPosition();
        } else {
            // A low drive strikes the wall and returns to the field instead of becoming a homer.
            double dx = crossing.x() - FieldGeometry.HOME.x();
            double dy = crossing.y() - FieldGeometry.HOME.y();
            double distance = Math.max(1, Math.hypot(dx, dy));
            double nx = dx / distance, ny = dy / distance;
            double dot = ball.velocityX() * nx + ball.velocityY() * ny;
            ball.setMotion(crossing.x() - nx * .25, crossing.y() - ny * .25, height,
                    (ball.velocityX() - 2 * dot * nx) * .40,
                    (ball.velocityY() - 2 * dot * ny) * .40, ball.velocityHeight());
        }
    }

    public void field(String fielderId, WorldPoint glove) { field(fielderId, glove, ball.height()); }
    public void field(String fielderId, WorldPoint glove, double height) {
        if (state != State.AIRBORNE && state != State.ROLLING) return;
        owner = fielderId;
        ball.setMotion(glove.x(), glove.y(), Math.max(0, height), 0, 0, 0);
        ball.stop();
        state = State.HELD_BY_FIELDER;
        clearTrail();
    }

    /** Follow an existing owner's animated hand without spawning a replacement baseball. */
    public void moveHeldBall(WorldPoint glove, double height) {
        if (state == State.HELD_BY_PITCHER || state == State.HELD_BY_FIELDER || state == State.CAUGHT)
            ball.setMotion(glove.x(), glove.y(), Math.max(0, height), 0, 0, 0);
    }

    public void throwFromFielder(WorldPoint glove, WorldPoint base, double speed) {
        throwFromFielder(glove, Math.max(8, ball.height()), base, 10, speed);
    }
    public void throwFromFielder(WorldPoint glove, double height, WorldPoint base,
                                double receiverHeight, double speed) {
        require(State.HELD_BY_FIELDER);
        owner = "";
        throwStart = glove;
        throwTarget = base;
        throwStartHeight = height;
        throwTargetHeight = receiverHeight;
        throwAge = 0;
        throwDuration = Math.max(.06, glove.distanceTo(base) / Math.max(1, speed));
        double vx = (base.x() - glove.x()) / throwDuration;
        double vy = (base.y() - glove.y()) / throwDuration;
        double vh = (receiverHeight - height) / throwDuration
                + .5 * BaseballPhysicsService.GRAVITY * throwDuration;
        ball.launch(glove.x(), glove.y(), vx, vy, vh, 0, false);
        ball.setMotion(glove.x(), glove.y(), height, vx, vy, vh);
        state = State.THROWN_BY_FIELDER;
        clearTrail();
    }

    private void updateThrow(double elapsed) {
        throwAge = Math.min(throwDuration, throwAge + elapsed);
        double t = throwAge / throwDuration;
        WorldPoint ground = throwStart.interpolate(throwTarget, t);
        double initialVh = (throwTargetHeight - throwStartHeight) / throwDuration
                + .5 * BaseballPhysicsService.GRAVITY * throwDuration;
        double height = throwStartHeight + initialVh * throwAge
                - .5 * BaseballPhysicsService.GRAVITY * throwAge * throwAge;
        ball.setMotion(ground.x(), ground.y(), Math.max(0, height), ball.velocityX(), ball.velocityY(),
                initialVh - BaseballPhysicsService.GRAVITY * throwAge);
        if (throwAge >= throwDuration) {
            ball.setMotion(throwTarget.x(), throwTarget.y(), throwTargetHeight, 0, 0, 0);
            state = State.CAUGHT;
            ball.stop();
            clearTrail();
        }
    }

    public void caught(String fielderId, WorldPoint glove) { caught(fielderId, glove, 0); }
    public void caught(String fielderId, WorldPoint glove, double height) {
        owner = fielderId;
        ball.setMotion(glove.x(), glove.y(), Math.max(0, height), 0, 0, 0);
        ball.stop();
        state = State.CAUGHT;
        clearTrail();
    }

    public void deadBall() {
        endAtCurrentPosition();
        owner = "";
        clearTrail();
    }
    private void endAtCurrentPosition() { ball.stop(); state = State.DEAD_BALL; }
    private void resetPlay() { age = 0; groundBall = false; boundaryOutcome = BoundaryOutcome.NONE; clearTrail(); }
    private void clearTrail() { trail.clear(); worldTrail.clear(); }
    private void remember() {
        trail.addFirst(screenPosition());
        worldTrail.addFirst(new TrailPoint(ball.x(), ball.y(), ball.height()));
        while (trail.size() > 10) trail.removeLast();
        while (worldTrail.size() > 10) worldTrail.removeLast();
    }
    private void require(State expected) {
        if (state != expected) throw new IllegalStateException("Expected " + expected + ", got " + state);
    }

    public State state() { return state; }
    public BoundaryOutcome boundaryOutcome() { return boundaryOutcome; }
    public boolean bounced() { return ball.touchedGround(); }
    public boolean groundBall() { return groundBall; }
    public double age() { return age; }
    public boolean isLive() {
        return state != State.DEAD_BALL && state != State.CAUGHT
                && state != State.HELD_BY_PITCHER && state != State.HELD_BY_FIELDER;
    }
    public String owner() { return owner; }
    public List<WorldPoint> trail() { return List.copyOf(trail); }
    public List<TrailPoint> worldTrail() { return List.copyOf(worldTrail); }
    /** Legacy only. New renderers project shadowPosition and ball.height through their camera. */
    public WorldPoint screenPosition() { return new WorldPoint(ball.x(), ball.y() - ball.height()); }
    public WorldPoint shadowPosition() { return new WorldPoint(ball.x(), ball.y()); }
    public double perspectiveScale() { return Math.max(.42, Math.min(1.18, .62 + ball.y() / 760)); }
    public Baseball ball() { return ball; }
    public double exitVelocity() { return exitVelocity; }
    public double launchAngle() { return launchAngle; }
    public HitResult hitResult() { return hitResult; }
}
