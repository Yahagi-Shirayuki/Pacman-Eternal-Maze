package game.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import game.Game;

/** Converts Swing key events into game-level input commands. */
public final class GameInput implements KeyListener {

    private final Game game;

    public GameInput(Game game) {
        this.game = game;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        game.handleKeyPressed(event.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent event) {
        game.handleKeyReleased(event.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent event) {
        // The game uses physical key codes, not typed characters.
    }
}
