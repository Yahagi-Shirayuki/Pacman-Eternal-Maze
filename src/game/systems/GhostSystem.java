package game.systems;

import game.Game;

/** Ghost movement, AI, spawning, and special-state updates. */
public final class GhostSystem {

    public void update(Game game) {
        game.updateGhosts();
    }
}
