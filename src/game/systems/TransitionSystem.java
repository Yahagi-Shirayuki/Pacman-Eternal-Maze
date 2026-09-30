package game.systems;

import game.Game;

/** Owns the level-to-level transition update phase. */
public final class TransitionSystem {

    public void update(Game game) {
        game.updateLevelTransition();
    }
}
