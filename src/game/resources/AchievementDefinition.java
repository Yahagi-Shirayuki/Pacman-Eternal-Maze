package game.resources;

public record AchievementDefinition(
        String key,
        String name,
        String icon,
        String description,
        String tips,
        boolean secret,
        AchievementCondition condition,
        boolean ingameBonus,
        int score,
        int order,
        boolean apOverride) {
}
