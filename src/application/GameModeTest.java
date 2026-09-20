package application;

/** Small headless check for the two focused play modes. */
public final class GameModeTest {
    private GameModeTest() { }

    public static void main(String[] args) {
        GameSettings.setGameMode(GameMode.BATTING_ONLY);
        require(GameSettings.getGameMode() == GameMode.BATTING_ONLY,
                "Batting-only mode was not selected.");
        GameSettings.setGameMode(GameMode.BOWLING_ONLY);
        require(GameSettings.getGameMode() == GameMode.BOWLING_ONLY,
                "Bowling-only mode was not selected.");
        GameSettings.setGameMode(GameMode.GRAND_PRIX);
        System.out.println("GAME_MODE_TESTS=PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
