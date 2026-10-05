package game;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Persistent lifetime statistics for one save slot. A run is merged only when it ends. */
public final class PlayerStats {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    private long pelletsConsumed;
    private long powerPelletsConsumed;
    private long fullRoomsCleared;
    private long highestLevel;
    private long totalLevelsCleared;
    private long tilesWalked;
    private long timeSpentFrames;
    private final Map<String, Long> powerCollected = new LinkedHashMap<>();
    private final Map<String, Long> ghostsKilledByCause = new LinkedHashMap<>();
    private final Map<String, Long> deathsByGhost = new LinkedHashMap<>();
    private final Map<String, Long> deathsByCause = new LinkedHashMap<>();
    private final Set<String> unlockedAchievements = new LinkedHashSet<>();
    private final Map<String, Long> achievementProgress = new LinkedHashMap<>();
    /** Transient rewards already paid during the current run; never saved or merged. */
    private final Set<String> runAchievementBonusesAwarded = new LinkedHashSet<>();

    public static PlayerStats load(File file) {
        PlayerStats stats = new PlayerStats();
        if (file == null || !file.isFile()) {
            return stats;
        }

        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                stats.applyLine(line);
            }
        } catch (IOException exception) {
            System.err.println("Could not load player stats: " + exception.getMessage());
        }

        return stats;
    }

    public void save(File file) {
        if (file == null) {
            return;
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new RuntimeException("Could not create save directory.");
        }

        List<String> lines = new ArrayList<>();
        lines.add("schemaVersion=" + CURRENT_SCHEMA_VERSION);
        lines.add("pelletsConsumed=" + pelletsConsumed);
        lines.add("powerPelletsConsumed=" + powerPelletsConsumed);
        lines.add("fullRoomsCleared=" + fullRoomsCleared);
        lines.add("highestLevel=" + highestLevel);
        lines.add("totalLevelsCleared=" + totalLevelsCleared);
        lines.add("tilesWalked=" + tilesWalked);
        lines.add("timeSpentFrames=" + timeSpentFrames);
        appendMap(lines, "powerCollected", powerCollected);
        appendMap(lines, "ghostsKilledByCause", ghostsKilledByCause);
        appendMap(lines, "deathsByGhost", deathsByGhost);
        appendMap(lines, "deathsByCause", deathsByCause);
        for (String key : unlockedAchievements) {
            lines.add("achievementUnlocked." + key + "=true");
        }
        appendMap(lines, "achievementProgress", achievementProgress);

        try {
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new RuntimeException("Could not save player stats.", exception);
        }
    }

    private void appendMap(List<String> lines, String prefix, Map<String, Long> values) {
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            lines.add(prefix + "." + entry.getKey() + "=" + entry.getValue());
        }
    }

    private void applyLine(String line) {
        if (line == null || line.isBlank() || line.startsWith("#")) {
            return;
        }

        String[] parts = line.split("=", 2);
        if (parts.length != 2) {
            return;
        }

        String key = parts[0].trim();
        String value = parts[1].trim();
        try {
            if (key.startsWith("achievementUnlocked.")) {
                if (Boolean.parseBoolean(value)) {
                    unlockedAchievements.add(key.substring("achievementUnlocked.".length()));
                }
                return;
            }
            if (key.equals("pelletsConsumed")) {
                pelletsConsumed = parseNonNegative(value);
            } else if (key.equals("powerPelletsConsumed")) {
                powerPelletsConsumed = parseNonNegative(value);
            } else if (key.equals("fullRoomsCleared")) {
                fullRoomsCleared = parseNonNegative(value);
            } else if (key.equals("highestLevel")) {
                highestLevel = parseNonNegative(value);
            } else if (key.equals("totalLevelsCleared")) {
                totalLevelsCleared = parseNonNegative(value);
            } else if (key.equals("tilesWalked")) {
                tilesWalked = parseNonNegative(value);
            } else if (key.equals("timeSpentFrames")) {
                timeSpentFrames = parseNonNegative(value);
            } else {
                applyMapValue(key, value);
            }
        } catch (NumberFormatException exception) {
            // Ignore one malformed counter and keep the rest of the slot usable.
        }
    }

    private void applyMapValue(String key, String value) {
        String[] parts = key.split("\\.", 2);
        if (parts.length != 2 || parts[1].isBlank()) {
            return;
        }

        Map<String, Long> target = switch (parts[0]) {
            case "powerCollected" -> powerCollected;
            case "ghostsKilledByCause" -> ghostsKilledByCause;
            case "deathsByGhost" -> deathsByGhost;
            case "deathsByCause" -> deathsByCause;
            default -> null;
        };
        if (target != null) {
            target.put(parts[1], parseNonNegative(value));
        }

        if (parts[0].equals("achievementProgress") && !parts[1].isBlank()) {
            achievementProgress.put(parts[1], parseNonNegative(value));
        }
    }

    private long parseNonNegative(String value) {
        return Math.max(0L, Long.parseLong(value));
    }

    public void merge(PlayerStats run) {
        if (run == null) {
            return;
        }

        pelletsConsumed += run.pelletsConsumed;
        powerPelletsConsumed += run.powerPelletsConsumed;
        fullRoomsCleared += run.fullRoomsCleared;
        highestLevel = Math.max(highestLevel, run.highestLevel);
        totalLevelsCleared += run.totalLevelsCleared;
        tilesWalked += run.tilesWalked;
        timeSpentFrames += run.timeSpentFrames;
        mergeMap(powerCollected, run.powerCollected);
        mergeMap(ghostsKilledByCause, run.ghostsKilledByCause);
        mergeMap(deathsByGhost, run.deathsByGhost);
        mergeMap(deathsByCause, run.deathsByCause);
        unlockedAchievements.addAll(run.unlockedAchievements);
        mergeMap(achievementProgress, run.achievementProgress);
    }

    private void mergeMap(Map<String, Long> target, Map<String, Long> source) {
        for (Map.Entry<String, Long> entry : source.entrySet()) {
            target.merge(entry.getKey(), entry.getValue(), Long::sum);
        }
    }

    public void recordPelletConsumed(boolean powerPellet) {
        pelletsConsumed++;
        if (powerPellet) {
            powerPelletsConsumed++;
        }
    }

    public void recordPowerCollected(String key) {
        increment(powerCollected, key);
    }

    public void recordGhostKilled(String cause) {
        increment(ghostsKilledByCause, cause);
    }

    public void recordDeathByGhost(String key) {
        increment(deathsByGhost, key);
    }

    public void recordDeathByCause(String cause) {
        increment(deathsByCause, cause);
    }

    private void increment(Map<String, Long> values, String key) {
        if (key != null && !key.isBlank()) {
            values.merge(key, 1L, Long::sum);
        }
    }

    public void recordFullRoomCleared() {
        fullRoomsCleared++;
    }

    public void recordLevelCleared(int clearedLevel) {
        totalLevelsCleared++;
        highestLevel = Math.max(highestLevel, clearedLevel);
    }

    public void recordLevelReached(int reachedLevel) {
        highestLevel = Math.max(highestLevel, reachedLevel);
    }

    public void recordTileWalked() {
        tilesWalked++;
    }

    public void recordTimeFrame() {
        timeSpentFrames++;
    }

    public boolean unlockAchievement(String key) {
        return key != null && !key.isBlank() && unlockedAchievements.add(key);
    }

    public boolean isAchievementUnlocked(String key) {
        return key != null && unlockedAchievements.contains(key);
    }

    public void addAchievementProgress(String key, long amount) {
        if (key != null && !key.isBlank() && amount > 0) {
            achievementProgress.merge(key, amount, Long::sum);
        }
    }

    public long getAchievementProgress(String key) {
        return achievementProgress.getOrDefault(key, 0L);
    }

    public boolean hasAchievementBonusAwarded(String key) {
        return key != null && runAchievementBonusesAwarded.contains(key);
    }

    public boolean markAchievementBonusAwarded(String key) {
        return key != null && !key.isBlank() && runAchievementBonusesAwarded.add(key);
    }

    public long getPelletsConsumed() {
        return pelletsConsumed;
    }

    public long getPowerPelletsConsumed() {
        return powerPelletsConsumed;
    }

    public long getFullRoomsCleared() {
        return fullRoomsCleared;
    }

    public long getHighestLevel() {
        return highestLevel;
    }

    public long getTotalLevelsCleared() {
        return totalLevelsCleared;
    }

    public long getTilesWalked() {
        return tilesWalked;
    }

    public long getTimeSpentFrames() {
        return timeSpentFrames;
    }

    public Map<String, Long> getPowerCollected() {
        return Map.copyOf(powerCollected);
    }

    public Map<String, Long> getGhostsKilledByCause() {
        return Map.copyOf(ghostsKilledByCause);
    }

    public Map<String, Long> getDeathsByGhost() {
        return Map.copyOf(deathsByGhost);
    }

    public Map<String, Long> getDeathsByCause() {
        return Map.copyOf(deathsByCause);
    }

    public boolean hasRecordedData() {
        return pelletsConsumed > 0 || powerPelletsConsumed > 0 || fullRoomsCleared > 0
                || highestLevel > 0 || totalLevelsCleared > 0 || tilesWalked > 0
                || timeSpentFrames > 0 || !powerCollected.isEmpty()
                || !ghostsKilledByCause.isEmpty() || !deathsByGhost.isEmpty()
                || !deathsByCause.isEmpty() || !unlockedAchievements.isEmpty()
                || !achievementProgress.isEmpty();
    }

    public Set<String> getUnlockedAchievements() {
        return Set.copyOf(unlockedAchievements);
    }
}
