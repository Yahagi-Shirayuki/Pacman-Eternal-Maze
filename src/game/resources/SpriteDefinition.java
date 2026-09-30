package game.resources;

public record SpriteDefinition(String file, String prefix, Integer count) {

    public boolean isFile() {
        return file != null && !file.isBlank();
    }

    public boolean isPrefix() {
        return prefix != null && !prefix.isBlank();
    }
}
