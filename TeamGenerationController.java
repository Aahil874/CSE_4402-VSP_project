package application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class TeamGenerationController {
    private static final Random RANDOM = new Random();
    private TeamGenerationController() {}
    public static TeamLineup generate(Mascot captain) {
        List<Mascot> pool = new ArrayList<>(List.of(Mascot.values()));
        pool.remove(captain); Collections.shuffle(pool, RANDOM); pool.addFirst(captain);
        List<TeamLineup.Slot> slots = new ArrayList<>();
        PlayerPosition[] positions = PlayerPosition.values();
        for (int i = 0; i < positions.length; i++) slots.add(new TeamLineup.Slot(positions[i], pool.get(i)));
        return new TeamLineup(slots);
    }
    public static TeamLineup generateRival(Mascot playerCaptain) {
        List<Mascot> choices = new ArrayList<>(List.of(Mascot.values()));
        choices.remove(playerCaptain); Collections.shuffle(choices, RANDOM);
        return generate(choices.getFirst());
    }
}
