package game.systems;

import game.Game;

/** Player movement and player-owned timers. */
public final class PlayerSystem {

    public void update(Game game) {
        game.updateGameTimers();
        game.updatePlayer();
    }

    public void updateDeath(Game game) {
        game.updateDeathAnimation();
    }
}
