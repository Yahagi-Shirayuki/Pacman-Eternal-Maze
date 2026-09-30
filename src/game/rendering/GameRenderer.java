package game.rendering;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import game.Game;
import game.GameState;

/** Swing-independent rendering entry point used by {@link GamePanel}. */
public final class GameRenderer {

    private final Game game;

    public GameRenderer(Game game) {
        this.game = game;
    }

    public void render(Graphics2D graphics, int width, int height) {
        Graphics2D frameGraphics = (Graphics2D) graphics.create();
        try {
            game.setViewportSize(width, height);

            if (game.getState() == GameState.MENU) {
                game.drawMenu(frameGraphics);
                return;
            }

            if (game.getState() == GameState.OPTIONS) {
                game.drawOptions(frameGraphics);
                return;
            }

            if (game.getState() == GameState.HALL_OF_FAME) {
                game.drawHallOfFame(frameGraphics);
                return;
            }

            if (game.getState() == GameState.ALMANAC) {
                game.drawAlmanac(frameGraphics);
                return;
            }

            if (game.getState() == GameState.NAME_ENTRY) {
                game.drawNameEntry(frameGraphics);
                return;
            }

            game.updateCamera();

            Graphics2D boardGraphics = (Graphics2D) frameGraphics.create();
            boardGraphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            boardGraphics.translate(0, game.getHudHeight());
            boardGraphics.scale(game.getCameraZoom(), game.getCameraZoom());
            boardGraphics.translate(0, -game.getHudHeight());
            boardGraphics.setClip(
                    0,
                    game.getHudHeight(),
                    (int) Math.ceil(game.getVisibleViewportWidth()),
                    (int) Math.ceil(game.getVisibleViewportBoardHeight()));
            game.drawOuterWall(boardGraphics);
            game.drawMaze(boardGraphics);
            game.drawDebris(boardGraphics);
            game.drawVisualDecals(boardGraphics);
            game.drawIceTiles(boardGraphics);
            game.drawWaterTiles(boardGraphics);
            game.drawWaterEffects(boardGraphics);
            game.drawElectricTiles(boardGraphics);
            game.drawDots(boardGraphics);
            game.drawLevelTransitionPreview(boardGraphics);
            game.drawFruits(boardGraphics);
            game.drawPowerUps(boardGraphics);
            game.drawSpikeTraps(boardGraphics);
            game.drawFireTrails(boardGraphics);
            game.drawCactusSpikeProjectiles(boardGraphics);
            game.drawGhostSpikeTraps(boardGraphics);
            game.drawLevelTransitionFire(boardGraphics);
            game.drawAfterImages(boardGraphics);
            game.drawExitMarkers(boardGraphics);
            game.drawSpawnWarning(boardGraphics);
            game.drawGhosts(boardGraphics);
            game.drawGhostDeathEffects(boardGraphics);
            game.drawGhostLasers(boardGraphics);
            game.drawLaser(boardGraphics);
            game.drawPacCloneLasers(boardGraphics);
            game.drawBombExplosion(boardGraphics);
            game.drawMagnetAuras(boardGraphics);
            game.drawIceAuras(boardGraphics);
            game.drawFrozenGhosts(boardGraphics);
            game.drawPacClones(boardGraphics);
            game.drawPlayer(boardGraphics);
            game.drawSmokeTiles(boardGraphics);
            boardGraphics.dispose();
            game.drawOverScreen(frameGraphics);
            game.drawPauseScreen(frameGraphics);
            game.drawHud(frameGraphics);
        } finally {
            frameGraphics.dispose();
        }
    }
}
