package game.systems;

import game.Game;

/** Resolves interactions after entities have advanced for the frame. */
public final class CollisionSystem {

    public void updateActive(Game game) {
        game.electrocuteGhostsInElectricTiles();
        game.checkLaserGhostHits();
        game.checkPacCloneLaserHits();
        game.checkGhostLaserHits();
        game.checkFireTrailCollisions();
        game.checkPlayerElectricTileCollision();
        game.checkFrozenGhostCollisions();
        game.checkPacCloneGhostCollisions();
    }

    public void updatePersistent(Game game) {
        game.checkGhostSpikeTrapCollision();
        game.checkGhostCollision();
    }
}
