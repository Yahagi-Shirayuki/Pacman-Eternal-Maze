package game.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import game.Game;
import game.input.GameInput;
import game.rendering.GameRenderer;

/**
 * Swing adapter for the game.
 *
 * <p>This class deliberately contains no game rules. It owns the Swing
 * lifecycle, forwards keyboard events, and asks the renderer to paint the
 * current game state.</p>
 */
@SuppressWarnings({"serial", "this-escape"})
public class GamePanel extends JPanel implements Runnable {

    private static final long serialVersionUID = 1L;

    private final Game game;
    private final GameRenderer renderer;
    private final GameInput input;
    private transient Thread gameThread;

    public GamePanel() {
        game = new Game();
        renderer = new GameRenderer(game);
        input = new GameInput(game);

        super.setPreferredSize(new Dimension(game.getPreferredWidth(), game.getPreferredHeight()));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(input);
    }

    public void startGameThread() {
        if (gameThread != null && gameThread.isAlive()) {
            return;
        }

        game.setFrameListener(this::requestRepaint);
        game.setQuitHandler(this::quitApplication);
        gameThread = new Thread(this, "pacman-game-loop");
        gameThread.start();
    }

    public void stopGameThread() {
        game.stop();
    }

    @Override
    public void run() {
        game.start();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        syncPreferredSize();
        renderer.render((Graphics2D) graphics, getWidth(), getHeight());
    }

    private void requestRepaint() {
        repaint();
    }

    private void syncPreferredSize() {
        Dimension preferred = getPreferredSize();
        int expectedWidth = game.getPreferredWidth();
        int expectedHeight = game.getPreferredHeight();

        if (preferred.width == expectedWidth && preferred.height == expectedHeight) {
            return;
        }

        setPreferredSize(new Dimension(expectedWidth, expectedHeight));
        revalidate();

        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.pack();
        }
    }

    private void quitApplication() {
        game.stop();
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
        System.exit(0);
    }
}
