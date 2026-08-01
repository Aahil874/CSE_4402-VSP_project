package application;

import java.util.List;
import java.util.stream.Collectors;

public record TeamLineup(List<Slot> slots) {
    public TeamLineup { slots = List.copyOf(slots); }
    public Mascot mascotAt(int index) { return slots.get(Math.floorMod(index, slots.size())).mascot(); }
    public Mascot pitcher() { return slots.stream().filter(s -> s.position() == PlayerPosition.PITCHER)
            .findFirst().orElse(slots.getFirst()).mascot(); }
    public String summary() { return slots.stream().map(s -> s.position() + " " + s.mascot().getDisplayName())
            .collect(Collectors.joining("  •  ")); }
    public String storageValue() { return slots.stream().map(s -> s.position().name() + ":" + s.mascot().name())
            .collect(Collectors.joining(",")); }
    public record Slot(PlayerPosition position, Mascot mascot) {}
}
