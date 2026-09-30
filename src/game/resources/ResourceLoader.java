package game.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads resources from the classpath first, then from the project directory.
 *
 * <p>The fallback is useful for this game's source-tree layout, where
 * {@code res/} is intentionally outside {@code src/} and is not copied by a
 * plain {@code javac -d bin} build.</p>
 */
public final class ResourceLoader {

    private static final List<String> SPRITE_DIRECTORIES = List.of(
            "decal",
            "ghost",
            "other",
            "player",
            "power",
            "tiles");

    private ResourceLoader() {
    }

    public static InputStream open(String path) throws IOException {
        String normalizedPath = normalize(path);
        InputStream classpathStream = ResourceLoader.class.getClassLoader()
                .getResourceAsStream(normalizedPath);

        if (classpathStream != null) {
            return classpathStream;
        }

        Path file = Path.of(normalizedPath);
        if (Files.isRegularFile(file)) {
            return Files.newInputStream(file);
        }

        // The resource tree is grouped by asset kind now, while older game
        // code still asks for paths such as res/sprite/pac0.png. Keep those
        // callers working by resolving the basename through the sprite
        // folders when the legacy flat path is not present.
        String spritePrefix = "res/sprite/";
        if (normalizedPath.startsWith(spritePrefix)
                && !normalizedPath.substring(spritePrefix.length()).contains("/")) {
            String fileName = normalizedPath.substring(spritePrefix.length());
            for (String directory : SPRITE_DIRECTORIES) {
                Path groupedFile = Path.of(spritePrefix + directory + "/" + fileName);
                if (Files.isRegularFile(groupedFile)) {
                    return Files.newInputStream(groupedFile);
                }
            }
        }

        return null;
    }

    private static String normalize(String path) {
        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
