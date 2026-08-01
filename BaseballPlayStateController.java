package application;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/** Authoritative state machine shared by pitching, batting, fielding and runners. */
public final class BaseballPlayStateController {
    public enum State {
        READY_FOR_PITCH, PITCH_WINDUP, PITCH_IN_FLIGHT, BATTER_SWINGING,
        BALL_IN_PLAY, FIELDING, DEFENSIVE_THROW, RUNNERS_ADVANCING,
        PLAY_RESOLVED, BETWEEN_BATTERS, HALF_INNING_OVER, MATCH_OVER
    }

    private static final Map<State, EnumSet<State>> LEGAL = new EnumMap<>(State.class);
    static {
        allow(State.READY_FOR_PITCH, State.PITCH_WINDUP, State.MATCH_OVER);
        allow(State.PITCH_WINDUP, State.PITCH_IN_FLIGHT);
        allow(State.PITCH_IN_FLIGHT, State.BATTER_SWINGING, State.PLAY_RESOLVED);
        allow(State.BATTER_SWINGING, State.BALL_IN_PLAY, State.PLAY_RESOLVED);
        allow(State.BALL_IN_PLAY, State.FIELDING, State.RUNNERS_ADVANCING, State.PLAY_RESOLVED);
        allow(State.FIELDING, State.DEFENSIVE_THROW, State.PLAY_RESOLVED);
        allow(State.DEFENSIVE_THROW, State.RUNNERS_ADVANCING, State.PLAY_RESOLVED);
        allow(State.RUNNERS_ADVANCING, State.FIELDING, State.DEFENSIVE_THROW, State.PLAY_RESOLVED);
        allow(State.PLAY_RESOLVED, State.BETWEEN_BATTERS, State.HALF_INNING_OVER, State.MATCH_OVER);
        allow(State.BETWEEN_BATTERS, State.READY_FOR_PITCH);
        allow(State.HALF_INNING_OVER, State.READY_FOR_PITCH, State.MATCH_OVER);
        allow(State.MATCH_OVER);
    }

    private State state = State.READY_FOR_PITCH;
    private long playNumber;

    private static void allow(State from, State... to) {
        LEGAL.put(from, to.length == 0
                ? EnumSet.noneOf(State.class) : EnumSet.of(to[0], to));
    }

    public State state() { return state; }
    public long playNumber() { return playNumber; }
    public boolean is(State expected) { return state == expected; }
    public boolean ballMayBeLive() {
        return switch (state) {
            case PITCH_IN_FLIGHT, BATTER_SWINGING, BALL_IN_PLAY,
                    FIELDING, DEFENSIVE_THROW, RUNNERS_ADVANCING -> true;
            default -> false;
        };
    }

    public void transition(State next) {
        if (next == state) {
            return;
        }
        if (!LEGAL.getOrDefault(state, EnumSet.noneOf(State.class)).contains(next)) {
            throw new IllegalStateException("Illegal baseball transition: " + state + " -> " + next);
        }
        state = next;
        if (next == State.PITCH_WINDUP) {
            playNumber++;
        }
    }

    public void forceReadyAfterResolvedPlay() {
        if (state == State.PLAY_RESOLVED) transition(State.BETWEEN_BATTERS);
        if (state == State.BETWEEN_BATTERS) transition(State.READY_FOR_PITCH);
    }
}
