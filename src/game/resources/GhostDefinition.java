package game.resources;

import java.util.Locale;

public record GhostDefinition(
        String key,
        String name,
        String power,
        String sprite,
        String movement,
        String target,
        String tileFilter,
        boolean scared,
        Double speedOverride) {

    public String normalizedMovement() {
        return movement == null || movement.isBlank() ? "random" : movement.toLowerCase(Locale.ROOT);
    }

    public String normalizedTarget() {
        return target == null || target.isBlank() ? "pacman" : target.toLowerCase(Locale.ROOT);
    }

    public String normalizedTileFilter() {
        return tileFilter == null ? "" : tileFilter.toLowerCase(Locale.ROOT);
    }
}
