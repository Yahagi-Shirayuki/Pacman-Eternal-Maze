package game;

import javax.swing.JFrame;

public class GameMain {

    public static void main(String[] args) {

        JFrame window = new JFrame("Pacman: Eternal Maze");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);
        gamePanel.requestFocusInWindow();

        gamePanel.startGameThread();
    }
}
