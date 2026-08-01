package application;


public final class ResultController {
    private final MatchResultService service = new MatchResultService();

    public String headline(MatchState state) {
        return switch (service.resolve(state)) {
            case WIN -> "VICTORY LAP!";
            case LOSS -> "RIVAL WINS — REMATCH!";
            case TIE -> "PHOTO FINISH TIE!";
        };
    }
}
