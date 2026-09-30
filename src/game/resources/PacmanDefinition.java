package game.resources;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record PacmanDefinition(
        String key,
        String name,
        Map<String, SpriteDefinition> sprites,
        List<String> spritePriority) {

    public PacmanDefinition {
        sprites = Collections.unmodifiableMap(new LinkedHashMap<>(sprites));
        spritePriority = List.copyOf(spritePriority);
    }

    public SpriteDefinition sprite(String state) {
        return sprites.get(state);
    }
}
