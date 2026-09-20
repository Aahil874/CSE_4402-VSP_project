package application;

import java.time.LocalDateTime;


public record PlayerProfile(
        long id, String displayName, Mascot selectedMascot,
        GameSettings.Difficulty preferredDifficulty,
        PitchStyle pitchStyle, String pitchLoadout, String teamLineup,
        LocalDateTime registeredAt, LocalDateTime lastPlayed) {
}
