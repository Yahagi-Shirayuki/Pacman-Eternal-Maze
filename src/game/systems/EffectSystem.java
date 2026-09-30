package game.systems;

import game.Game;

/** Updates temporary entities and visual effects in a stable order. */
public final class EffectSystem {

    public void updateBeforeGhosts(Game game) {
        game.updatePacClones();
        game.updateFireTrails();
        game.updateSmokeTiles();
        game.updateWaterEffects();
    }

    public void updateAfterGhosts(Game game) {
        game.updateCactusSpikeProjectiles();
        game.updateIceEffects();
    }

    public void updatePersistent(Game game) {
        game.updateGhostDeathEffects();
        game.updateAfterImages();
    }
}
