package game.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reads the editable game content files from the external {@code res/} tree. */
public final class GameContent {

    public static final String GHOST_DIRECTORY = "res/sprite/ghost/";
    public static final String PLAYER_DIRECTORY = "res/sprite/player/";

    private GameContent() {
    }

    public static List<GhostDefinition> loadGhostDefinitions() throws IOException {
        Map<String, Object> root = readObject(GHOST_DIRECTORY + "ghost.json");
        List<GhostDefinition> definitions = new ArrayList<>();

        for (Object value : JsonParser.array(root.get("ghosts"))) {
            Map<String, Object> ghost = JsonParser.object(value);
            String key = requiredString(ghost, "key");
            String sprite = requiredString(ghost, "sprite");
            Double speedOverride = numberAsDouble(ghost.get("speedOverride"));

            definitions.add(new GhostDefinition(
                    key,
                    requiredString(ghost, "name"),
                    JsonParser.string(ghost, "power", "none"),
                    sprite,
                    JsonParser.string(ghost, "movement", "random"),
                    JsonParser.string(ghost, "target", "pacman"),
                    JsonParser.string(ghost, "tileFilter", ""),
                    JsonParser.bool(ghost, "scared", true),
                    speedOverride));
        }

        return List.copyOf(definitions);
    }

    public static List<AlmanacEntry> loadAlmanacEntries() throws IOException {
        Map<String, Object> root = readObject(GHOST_DIRECTORY + "almanac.json");
        List<AlmanacEntry> entries = new ArrayList<>();

        for (Object value : JsonParser.array(root.get("entries"))) {
            Map<String, Object> entry = JsonParser.object(value);
            entries.add(new AlmanacEntry(
                    requiredString(entry, "key"),
                    requiredString(entry, "display"),
                    JsonParser.string(entry, "desc", "")));
        }

        return List.copyOf(entries);
    }

    public static PacmanDefinition loadPacmanDefinition() throws IOException {
        Map<String, Object> root = readObject(PLAYER_DIRECTORY + "pacman.json");
        Map<String, SpriteDefinition> sprites = new java.util.LinkedHashMap<>();

        Map<String, Object> spriteObject = JsonParser.object(root.get("sprites"));
        for (Map.Entry<String, Object> entry : spriteObject.entrySet()) {
            Map<String, Object> sprite = JsonParser.object(entry.getValue());
            sprites.put(entry.getKey(), new SpriteDefinition(
                    JsonParser.string(sprite, "file", null),
                    JsonParser.string(sprite, "prefix", null),
                    JsonParser.integer(sprite, "count")));
        }

        List<String> priority = new ArrayList<>();
        Object priorityValue = root.get("spritePriority");
        if (priorityValue != null) {
            for (Object value : JsonParser.array(priorityValue)) {
                if (value instanceof String state) {
                    priority.add(state);
                }
            }
        }

        return new PacmanDefinition(
                JsonParser.string(root, "key", "pacman"),
                JsonParser.string(root, "name", "Pacman"),
                sprites,
                priority);
    }

    private static Map<String, Object> readObject(String path) throws IOException {
        try (InputStream stream = ResourceLoader.open(path)) {
            if (stream == null) {
                throw new IOException("Missing content file: " + path);
            }

            String source = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return JsonParser.object(JsonParser.parse(source));
        }
    }

    private static String requiredString(Map<String, Object> object, String key) {
        String value = JsonParser.string(object, key, null);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required JSON field: " + key);
        }

        return value;
    }

    private static Double numberAsDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : null;
    }
}
