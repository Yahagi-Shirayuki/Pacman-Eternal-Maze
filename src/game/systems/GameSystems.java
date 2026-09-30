package game.systems;

import game.Game;
import game.GameState;

/** Coordinates the deterministic update order for one simulation frame. */
public final class GameSystems {

    private final PlayerSystem playerSystem = new PlayerSystem();
    private final GhostSystem ghostSystem = new GhostSystem();
    private final CollisionSystem collisionSystem = new CollisionSystem();
    private final EffectSystem effectSystem = new EffectSystem();
    private final TransitionSystem transitionSystem = new TransitionSystem();

    public void update(Game game) {
        if (game.getState() != GameState.PLAYING || game.isPaused()) {
            return;
        }

        if (game.isLevelTransitionActive()) {
            transitionSystem.update(game);
        } else if (game.isPlayerDead()) {
            playerSystem.updateDeath(game);
        } else {
            playerSystem.update(game);
        }

        if (game.isGameStarted() && !game.isPlayerDead() && !game.isLevelTransitionActive()) {
            effectSystem.updateBeforeGhosts(game);
            ghostSystem.update(game);
            effectSystem.updateAfterGhosts(game);
            collisionSystem.updateActive(game);
        }

        effectSystem.updatePersistent(game);

        if (!game.isPlayerDead()) {
            collisionSystem.updatePersistent(game);
        }
    }
}
