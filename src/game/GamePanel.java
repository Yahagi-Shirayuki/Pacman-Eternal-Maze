package game;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    private static final long serialVersionUID = 1L;

    // Grid settings
    final int tileSize = 24;
    int maxScreenCol = 25; // width
    int maxScreenRow = 31; // height
    

    int screenWidth = tileSize * maxScreenCol;
    int boardHeight = tileSize * maxScreenRow;
    final int hudHeight = 48;
    int screenHeight = boardHeight + hudHeight;
    
    boolean[][] maze;
    boolean[][] dots;
    boolean[][] bigDots;
    Random random = new Random();

    int tunnelY = maxScreenRow / 2;
    // Pacman speed
    double playerSpeed = 3.5;
    double ghostSpeed = 1.5;
    //ghost animation
    // Larger value = slower animation.
    final int animationDelay = 4;
    final int framesPerSecond = 60;
    int powerPelletDuration = framesPerSecond * 5; //pellet timer
    final int powerPelletWarningTime = framesPerSecond * 2;
    int ghostSpawnInterval = framesPerSecond * 10; //ghost spawn per second
    final int boardGhostDelay = framesPerSecond * 2;
    final int carriedGhostSpawnInterval = framesPerSecond;
    final int ghostSpawnWarningTime = framesPerSecond;
    final int fruitScoreInterval = 10000;
    final int maxFruitsPerLevel = 5;
    final int powerUpDropSmallDots = 50;
    int powerUpDuration = framesPerSecond * 5; // power timer
    final int spikeTrapCount = 20;
    final int ghostDeathFrameTime = 8;
    final double ghostEyesSpeed = 4.0;

    BufferedImage[] pacSprites = new BufferedImage[5];
    BufferedImage[] ghostSprites = new BufferedImage[6];
    BufferedImage ghostDeathSprite;
    BufferedImage ghostEyesSprite;
    BufferedImage[] outSprites = new BufferedImage[4];
    BufferedImage[] fruitSprites = new BufferedImage[4];
    BufferedImage[] powerUpSprites = new BufferedImage[5];
    BufferedImage[] spikeSprites = new BufferedImage[2];
    BufferedImage[] warnSprites = new BufferedImage[2];
    BufferedImage dotSmall;
    BufferedImage dotBig;
    BufferedImage overScreen;
    BufferedImage pauseScreen;
    BufferedImage menuScreen;
    BufferedImage blankMenuScreen;

    static final int STATE_MENU = 0;
    static final int STATE_OPTIONS = 1;
    static final int STATE_GAME = 2;
    static final int STATE_HALL_OF_FAME = 3;
    static final int STATE_NAME_ENTRY = 4;
    int screenState = STATE_MENU;
    int menuChoice = 0;
    int optionChoice = 0;
    int optionActionChoice = 0;
    final File scoreFile = new File("playerscore.sav");
    String[] highScoreNames = { "DEV", "PRO", "NUB" };
    int[] highScoreValues = { 999999, 50000, 1000 };
    char[] nameEntry = { 'A', 'A', 'A' };
    int nameEntryIndex = 0;
    int pendingFinalScore = 0;
    boolean finalScoreHandled = false;

    boolean useWasdMovement = false;
    boolean draftUseWasdMovement = false;
    double draftGhostSpeed = ghostSpeed;
    double draftPlayerSpeed = playerSpeed;
    int draftGhostSpawnSeconds = 10;
    int draftPelletSeconds = 5;
    int draftPowerSeconds = 5;
    boolean noPowerMode = false;
    boolean draftNoPowerMode = false;
    int draftMazeWidth = maxScreenCol;
    int draftMazeHeight = maxScreenRow;

    double playerPixelX = (maxScreenCol - 2) * tileSize;
    double playerPixelY = tunnelY * tileSize;
    double targetPixelX = playerPixelX;
    double targetPixelY = playerPixelY;
    int directionX = 0;
    int directionY = 0;
    int nextDirectionX = 0;
    int nextDirectionY = 0;
    int lastDirectionX = 1;
    int lastDirectionY = 0;
    int lastPlayerTileX = maxScreenCol - 2;
    int lastPlayerTileY = tunnelY;
    int animationCounter = 0;
    int ghostAnimationCounter = 0;
    boolean gameStarted = false;
    boolean playerDead = false;
    boolean deathAnimationDone = false;
    boolean powerMode = false;
    boolean paused = false;
    boolean quitConfirmVisible = false;
    int deathAnimationCounter = 0;
    int deathFrame = 0;
    int score = 0;
    int level = 1;
    int roomInitialScore = 0;
    int nextFruitScore = fruitScoreInterval;
    int fruitsSpawnedThisLevel = 0;
    int elapsedFrames = 0;
    int powerModeTimer = 0;
    int ghostSpawnTimer = 0;
    int ghostEatScore = 200;
    int pendingCarriedGhosts = 0;
    int boardGhostDelayTimer = 0;
    int carriedGhostSpawnTimer = 0;
    int warningPortalX = -1;
    int smallDotsTowardPowerUp = 0;
    int[] powerUpTimers = new int[5];
    int[] collectedFruits = new int[4];
    // Larger value = slower death animation.
    final int deathFrameDelay = 8;
    boolean boardClear = false;

    ArrayList<Ghost> ghosts = new ArrayList<>();
    ArrayList<Fruit> fruits = new ArrayList<>();
    ArrayList<PowerUp> powerUps = new ArrayList<>();
    ArrayList<SpikeTrap> spikeTraps = new ArrayList<>();
    ArrayList<GhostDeathEffect> ghostDeathEffects = new ArrayList<>();
    Camera camera = new Camera();

    Thread gameThread;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(this);
        
        loadSprites();
        loadHighScores();
        resetGame();
        screenState = STATE_MENU;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (gameThread != null) {
            if (screenState == STATE_GAME && !paused) {
                if (playerDead) {
                    updateDeathAnimation();
                } else {
                    updateGameTimers();
                    updatePlayer();
                }

                if (gameStarted && !playerDead) {
                    updateGhosts();
                }

                updateGhostDeathEffects();

                if (!playerDead) {
                    checkGhostCollision();
                }
            }

            repaint();

            try {
                Thread.sleep(16); // roughly 60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    //paint components
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        if (screenState == STATE_MENU) {
            drawMenu(g2);
            g2.dispose();
            return;
        }

        if (screenState == STATE_OPTIONS) {
            drawOptions(g2);
            g2.dispose();
            return;
        }

        if (screenState == STATE_HALL_OF_FAME) {
            drawHallOfFame(g2);
            g2.dispose();
            return;
        }

        if (screenState == STATE_NAME_ENTRY) {
            drawNameEntry(g2);
            g2.dispose();
            return;
        }

        updateCamera();

        Graphics2D boardGraphics = (Graphics2D) g2.create();
        boardGraphics.setClip(0, hudHeight, getWidth(), getViewportBoardHeight());
        drawOuterWall(boardGraphics);
        drawMaze(boardGraphics);
        drawDots(boardGraphics);
        drawFruits(boardGraphics);
        drawPowerUps(boardGraphics);
        drawSpikeTraps(boardGraphics);
        drawExitMarkers(boardGraphics);
        drawSpawnWarning(boardGraphics);
        drawGhosts(boardGraphics);
        drawGhostDeathEffects(boardGraphics);
        drawPlayer(boardGraphics);
        boardGraphics.dispose();
        drawOverScreen(g2);
        drawPauseScreen(g2);
        drawHud(g2);

        g2.dispose();
    }
    
    //draw grid
    public void drawGrid(Graphics2D g2) {
        g2.setColor(Color.DARK_GRAY);

        // Vertical lines
        for (int col = 0; col <= maxScreenCol; col++) {
            int x = worldToScreenX(col * tileSize);
            g2.drawLine(x, hudHeight, x, hudHeight + getViewportBoardHeight());
        }

        // Horizontal lines
        for (int row = 0; row <= maxScreenRow; row++) {
            int y = worldBoundaryToScreenY(row * tileSize);
            g2.drawLine(0, y, getWidth(), y);
        }
    }

    //draw wall
    public void drawOuterWall(Graphics2D g2) {
        g2.setColor(Color.RED.darker().darker().darker());

        // Top and bottom walls
        for (int x = 0; x < maxScreenCol; x++) {
            drawTile(g2, x, 0);
            drawTile(g2, x, maxScreenRow - 1);
        }

        // Left and right walls
        for (int y = 0; y < maxScreenRow; y++) {

            // Skip tunnel row
            if (y == tunnelY) {
                continue;
            }

            drawTile(g2, 0, y);
            drawTile(g2, maxScreenCol - 1, y);
        }
    }
    
    //draw tiles
    public void drawTile(Graphics2D g2, int x, int y) {
        int screenX = worldToScreenX(x * tileSize);
        int screenY = worldToScreenY(y * tileSize);

        g2.fillRect(screenX, screenY, tileSize, tileSize);
    }

    public void drawImageAtTile(Graphics2D g2, BufferedImage image, int x, int y) {
        int screenX = worldToScreenX(x * tileSize);
        int screenY = worldToScreenY(y * tileSize);

        g2.drawImage(image, screenX, screenY, tileSize, tileSize, null);
    }

    public void updateCamera() {
        camera.update(
                playerPixelX + tileSize / 2.0,
                playerPixelY + tileSize / 2.0,
                screenWidth,
                boardHeight,
                Math.max(tileSize, getWidth()),
                getViewportBoardHeight());
    }

    public int getViewportBoardHeight() {
        return Math.max(tileSize, getHeight() - hudHeight);
    }

    public int worldToScreenX(double worldX) {
        return (int) Math.round(worldX - camera.viewX);
    }

    public int worldToScreenY(double worldY) {
        return hudHeight + (int) Math.round(getViewportBoardHeight() - tileSize - (worldY - camera.viewY));
    }

    public int worldBoundaryToScreenY(double worldY) {
        return hudHeight + (int) Math.round(getViewportBoardHeight() - (worldY - camera.viewY));
    }
    
    //maze gen algo
    public void generateMaze() {
        maze = new boolean[maxScreenCol][maxScreenRow];

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                maze[x][y] = true;
            }
        }

        carveMazeFrom(1, tunnelY);
        addExtraLoops();
        removeDeadEnds();

        maze[1][tunnelY] = false;
        maze[maxScreenCol - 2][tunnelY] = false;
    }

    public void carveMazeFrom(int x, int y) {
        maze[x][y] = false;

        int[][] directions = {
            { 2, 0 },
            { -2, 0 },
            { 0, 2 },
            { 0, -2 }
        };

        shuffleDirections(directions);

        for (int[] direction : directions) {
            int nextX = x + direction[0];
            int nextY = y + direction[1];

            if (isInsideMaze(nextX, nextY) && maze[nextX][nextY]) {
                maze[x + direction[0] / 2][y + direction[1] / 2] = false;
                carveMazeFrom(nextX, nextY);
            }
        }
    }

    public void shuffleDirections(int[][] directions) {
        for (int i = directions.length - 1; i > 0; i--) {
            int randomIndex = random.nextInt(i + 1);
            int[] temp = directions[i];
            directions[i] = directions[randomIndex];
            directions[randomIndex] = temp;
        }
    }

    public boolean isInsideMaze(int x, int y) {
        return x > 0 && x < maxScreenCol - 1 && y > 0 && y < maxScreenRow - 1;
    }

    public void addExtraLoops() {
        double extraLoopChance = 0.4;

        for (int x = 2; x < maxScreenCol - 2; x += 2) {
            for (int y = 1; y < maxScreenRow - 1; y += 2) {
                if (maze[x][y] && !maze[x - 1][y] && !maze[x + 1][y] && random.nextDouble() < extraLoopChance) {
                    maze[x][y] = false;
                }
            }
        }

        for (int x = 1; x < maxScreenCol - 1; x += 2) {
            for (int y = 2; y < maxScreenRow - 2; y += 2) {
                if (maze[x][y] && !maze[x][y - 1] && !maze[x][y + 1] && random.nextDouble() < extraLoopChance) {
                    maze[x][y] = false;
                }
            }
        }
    }

    public void removeDeadEnds() {
        boolean changed = true;

        while (changed) {
            changed = false;

            for (int x = 1; x < maxScreenCol - 1; x++) {
                for (int y = 1; y < maxScreenRow - 1; y++) {
                    if (!maze[x][y] && countOpenNeighbors(x, y) <= 1) {
                        changed = openDeadEndExit(x, y) || changed;
                    }
                }
            }
        }
    }

    public int countOpenNeighbors(int x, int y) {
        int count = 0;
        int[][] directions = {
            { 1, 0 },
            { -1, 0 },
            { 0, 1 },
            { 0, -1 }
        };

        for (int[] direction : directions) {
            int nextX = x + direction[0];
            int nextY = y + direction[1];

            if (isPortalTile(nextX, nextY) || (isInsideMaze(nextX, nextY) && !maze[nextX][nextY])) {
                count++;
            }
        }

        return count;
    }

    public boolean openDeadEndExit(int x, int y) {
        int[][] directions = {
            { 1, 0 },
            { -1, 0 },
            { 0, 1 },
            { 0, -1 }
        };

        shuffleDirections(directions);

        for (int[] direction : directions) {
            int wallX = x + direction[0];
            int wallY = y + direction[1];
            int nextX = x + direction[0] * 2;
            int nextY = y + direction[1] * 2;

            if (isInsideMaze(wallX, wallY) && isInsideMaze(nextX, nextY) && maze[wallX][wallY] && !maze[nextX][nextY]) {
                maze[wallX][wallY] = false;
                return true;
            }
        }

        return false;
    }

    public void generateDots() {
        dots = new boolean[maxScreenCol][maxScreenRow];
        bigDots = new boolean[maxScreenCol][maxScreenRow];

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                dots[x][y] = !maze[x][y] && !isPortalEntrance(x, y);
            }
        }

        placePowerPellets();
    }

    public void placePowerPellets() {
        for (int[] corner : getCornerSpawnTargets()) {
            int[] pelletTile = findSpawnTileNear(corner[0], corner[1]);

            dots[pelletTile[0]][pelletTile[1]] = false;
            bigDots[pelletTile[0]][pelletTile[1]] = true;
        }
    }

    public boolean isPortalEntrance(int x, int y) {
        return y == tunnelY && (x == 1 || x == maxScreenCol - 2);
    }

    public boolean isPortalTile(int x, int y) {
        return y == tunnelY && (x == 0 || x == maxScreenCol - 1);
    }

    public int[][] getCornerSpawnTargets() {
        return new int[][] {
            { 1, 1 },
            { 1, maxScreenRow - 2 },
            { maxScreenCol - 2, 1 },
            { maxScreenCol - 2, maxScreenRow - 2 }
        };
    }

    	//sprite handling
    public void loadSprites() {
        try {
            pacSprites[0] = ImageIO.read(new File("res/sprite/pac0.png"));
            pacSprites[1] = ImageIO.read(new File("res/sprite/pac1.png"));
            pacSprites[2] = ImageIO.read(new File("res/sprite/pac2.png"));
            pacSprites[3] = ImageIO.read(new File("res/sprite/pac3.png"));
            pacSprites[4] = ImageIO.read(new File("res/sprite/pac4.png"));
            ghostSprites[0] = ImageIO.read(new File("res/sprite/ghost_0.png"));
            ghostSprites[1] = ImageIO.read(new File("res/sprite/ghost_1.png"));
            ghostSprites[2] = ImageIO.read(new File("res/sprite/ghost_2.png"));
            ghostSprites[3] = ImageIO.read(new File("res/sprite/ghost_3.png"));
            ghostSprites[4] = ImageIO.read(new File("res/sprite/ghost_4.png"));
            ghostSprites[5] = ImageIO.read(new File("res/sprite/ghost_5.png"));
            ghostDeathSprite = loadOptionalSprite("res/sprite/ghost_7.png", ghostSprites[4]);
            ghostEyesSprite = loadOptionalSprite("res/sprite/eyes.png", ghostDeathSprite);
            outSprites[0] = ImageIO.read(new File("res/sprite/out_0.png"));
            outSprites[1] = ImageIO.read(new File("res/sprite/out_1.png"));
            outSprites[2] = ImageIO.read(new File("res/sprite/out_2.png"));
            outSprites[3] = ImageIO.read(new File("res/sprite/out_3.png"));
            warnSprites[0] = ImageIO.read(new File("res/sprite/warn_0.png"));
            warnSprites[1] = ImageIO.read(new File("res/sprite/warn_1.png"));
            dotSmall = ImageIO.read(new File("res/sprite/dot_small.png"));
            dotBig = ImageIO.read(new File("res/sprite/dot_big.png"));
            fruitSprites[0] = ImageIO.read(new File("res/sprite/fruit_0.png"));
            fruitSprites[1] = ImageIO.read(new File("res/sprite/fruit_1.png"));
            fruitSprites[2] = ImageIO.read(new File("res/sprite/fruit_2.png"));
            fruitSprites[3] = ImageIO.read(new File("res/sprite/fruit_3.png"));
            powerUpSprites[0] = ImageIO.read(new File("res/sprite/pow_0.png"));
            powerUpSprites[1] = ImageIO.read(new File("res/sprite/pow_1.png"));
            powerUpSprites[2] = ImageIO.read(new File("res/sprite/pow_2.png"));
            powerUpSprites[3] = ImageIO.read(new File("res/sprite/pow_3.png"));
            powerUpSprites[4] = ImageIO.read(new File("res/sprite/pow_4.png"));
            spikeSprites[0] = ImageIO.read(new File("res/sprite/spike_0.png"));
            spikeSprites[1] = ImageIO.read(new File("res/sprite/spike_1.png"));
            overScreen = ImageIO.read(new File("res/sprite/overscreen.png"));
            pauseScreen = ImageIO.read(new File("res/sprite/pausescreen.png"));
            menuScreen = ImageIO.read(new File("res/sprite/menuscreen_0.png"));
            blankMenuScreen = ImageIO.read(new File("res/sprite/menuscreen_1.png"));
        } catch (IOException e) {
            throw new RuntimeException("Could not load sprite images from res/sprite.", e);
        }
    }

    public void loadHighScores() {
        if (!scoreFile.exists()) {
            writeHighScores();
            return;
        }

        try {
            List<String> lines = Files.readAllLines(scoreFile.toPath(), StandardCharsets.UTF_8);

            for (int i = 0; i < highScoreNames.length && i < lines.size(); i++) {
                String[] parts = lines.get(i).split(":");

                if (parts.length == 2) {
                    highScoreNames[i] = cleanPlayerName(parts[0]);
                    highScoreValues[i] = Integer.parseInt(parts[1].trim());
                }
            }
        } catch (IOException | NumberFormatException e) {
            highScoreNames = new String[] { "DEV", "PRO", "NUB" };
            highScoreValues = new int[] { 999999, 50000, 1000 };
            writeHighScores();
        }
    }

    public void writeHighScores() {
        ArrayList<String> lines = new ArrayList<>();

        for (int i = 0; i < highScoreNames.length; i++) {
            lines.add(highScoreNames[i] + ": " + highScoreValues[i]);
        }

        try {
            Files.write(scoreFile.toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not write playerscore.sav.", e);
        }
    }

    public String cleanPlayerName(String name) {
        String cleaned = name.trim().toUpperCase();

        if (cleaned.length() > 3) {
            return cleaned.substring(0, 3);
        }

        while (cleaned.length() < 3) {
            cleaned += "A";
        }

        return cleaned;
    }

    public BufferedImage loadOptionalSprite(String path, BufferedImage fallback) throws IOException {
        File spriteFile = new File(path);

        if (!spriteFile.exists()) {
            return fallback;
        }

        return ImageIO.read(spriteFile);
    }

    public void resetGame() {
        gameStarted = false;
        paused = false;
        quitConfirmVisible = false;
        playerDead = false;
        deathAnimationDone = false;
        deathAnimationCounter = 0;
        deathFrame = 0;
        animationCounter = 0;
        ghostAnimationCounter = 0;
        score = 0;
        level = 1;
        roomInitialScore = 0;
        nextFruitScore = fruitScoreInterval;
        fruitsSpawnedThisLevel = 0;
        elapsedFrames = 0;
        powerModeTimer = 0;
        ghostSpawnTimer = 0;
        ghostEatScore = 200;
        pendingCarriedGhosts = 0;
        boardGhostDelayTimer = 0;
        carriedGhostSpawnTimer = 0;
        warningPortalX = -1;
        smallDotsTowardPowerUp = 0;
        Arrays.fill(powerUpTimers, 0);
        collectedFruits = new int[4];
        finalScoreHandled = false;
        pendingFinalScore = 0;
        nameEntry = new char[] { 'A', 'A', 'A' };
        nameEntryIndex = 0;
        powerMode = false;
        boardClear = false;

        playerPixelX = (maxScreenCol - 2) * tileSize;
        playerPixelY = tunnelY * tileSize;
        targetPixelX = playerPixelX;
        targetPixelY = playerPixelY;
        directionX = 0;
        directionY = 0;
        nextDirectionX = 0;
        nextDirectionY = 0;
        lastDirectionX = 1;
        lastDirectionY = 0;
        lastPlayerTileX = maxScreenCol - 2;
        lastPlayerTileY = tunnelY;

        generateMaze();
        generateDots();
        fruits.clear();
        powerUps.clear();
        spikeTraps.clear();
        ghostDeathEffects.clear();
        spawnGhosts();
        eatDotAtPlayer();
    }

    public void spawnGhosts() {
        ghosts.clear();
        int[][] cornerSpawns = getCornerSpawnTargets();

        for (int i = 0; i < cornerSpawns.length; i++) {
            int[] spawnTile = findGhostSpawnTileNear(cornerSpawns[i][0], cornerSpawns[i][1]);
            int ghostType = random.nextInt(4);

            addGhostAtTile(ghostType, spawnTile[0], spawnTile[1]);
        }
    }

    public void addGhostAtTile(int ghostType, int tileX, int tileY) {
        Ghost ghost = new Ghost(ghostType, tileX, tileY, tileSize);
        ghost.speedOffset = getRandomGhostSpeedOffset();
        setRandomGhostDirection(ghost);
        ghosts.add(ghost);
    }

    public void spawnGhostThroughPortal() {
        spawnGhostThroughPortal(getWarningPortalX());
    }

    public void spawnGhostThroughPortal(int tileX) {
        boolean fromLeft = random.nextBoolean();
        if (tileX == 0) {
            fromLeft = true;
        } else if (tileX == maxScreenCol - 1) {
            fromLeft = false;
        }

        tileX = fromLeft ? 0 : maxScreenCol - 1;
        int directionX = fromLeft ? 1 : -1;
        Ghost ghost = new Ghost(random.nextInt(4), tileX, tunnelY, tileSize);

        ghost.speedOffset = getRandomGhostSpeedOffset();
        ghost.directionX = directionX;
        ghost.directionY = 0;
        ghost.targetPixelX = (tileX + directionX) * tileSize;
        ghost.targetPixelY = tunnelY * tileSize;
        ghosts.add(ghost);
        warningPortalX = -1;
    }

    public int getWarningPortalX() {
        if (warningPortalX == -1) {
            warningPortalX = random.nextBoolean() ? 0 : maxScreenCol - 1;
        }

        return warningPortalX;
    }

    public int[] findSpawnTileNear(int targetX, int targetY) {
        int playerStartX = maxScreenCol - 2;
        int playerStartY = tunnelY;
        int bestX = 1;
        int bestY = 1;
        int bestDistance = Integer.MAX_VALUE;

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                if (maze[x][y] || getManhattanDistance(x, y, playerStartX, playerStartY) < 8) {
                    continue;
                }

                int distance = getManhattanDistance(x, y, targetX, targetY);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestX = x;
                    bestY = y;
                }
            }
        }

        return new int[] { bestX, bestY };
    }

    public int[] findGhostSpawnTileNear(int targetX, int targetY) {
        int playerStartX = maxScreenCol - 2;
        int playerStartY = tunnelY;
        int bestX = 1;
        int bestY = 1;
        int bestDistance = Integer.MAX_VALUE;

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                if (maze[x][y] || bigDots[x][y] || getManhattanDistance(x, y, playerStartX, playerStartY) < 8) {
                    continue;
                }

                int distance = getManhattanDistance(x, y, targetX, targetY);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestX = x;
                    bestY = y;
                }
            }
        }

        return new int[] { bestX, bestY };
    }

    public void updatePlayer() {
        if (isPlayerMoving()) {
            animationCounter++;
        }

        if (!isPlayerMoving()) {
            chooseNextTarget();
        }

        if (isPlayerMoving()) {
            moveTowardTarget();
        }
    }

    public void updateGameTimers() {
        if (!gameStarted) {
            return;
        }

        elapsedFrames++;
        updatePowerUpTimers();
        updateSpikeTraps();

        if (!boardClear) {
            updateBoardGhostSpawns();

            if (boardGhostDelayTimer == 0 && pendingCarriedGhosts == 0) {
                ghostSpawnTimer++;
            }

            if (boardGhostDelayTimer == 0 && pendingCarriedGhosts == 0
                    && ghostSpawnTimer >= ghostSpawnInterval - ghostSpawnWarningTime) {
                getWarningPortalX();
            }

            if (boardGhostDelayTimer == 0 && pendingCarriedGhosts == 0 && ghostSpawnTimer >= ghostSpawnInterval) {
                ghostSpawnTimer = 0;
                spawnGhostThroughPortal(warningPortalX);
            }
        }

        if (powerMode) {
            powerModeTimer--;

            if (powerModeTimer <= 0) {
                stopPowerMode();
            }
        }
    }

    public void updatePowerUpTimers() {
        for (int i = 0; i < powerUpTimers.length; i++) {
            if (powerUpTimers[i] > 0) {
                powerUpTimers[i]--;
            }
        }
    }

    public void updateSpikeTraps() {
        for (int i = spikeTraps.size() - 1; i >= 0; i--) {
            SpikeTrap spikeTrap = spikeTraps.get(i);
            spikeTrap.timer--;

            if (spikeTrap.timer <= 0) {
                spikeTraps.remove(i);
            }
        }
    }

    public void updateBoardGhostSpawns() {
        if (boardGhostDelayTimer > 0) {
            boardGhostDelayTimer--;
            return;
        }

        if (pendingCarriedGhosts <= 0) {
            return;
        }

        carriedGhostSpawnTimer++;

        if (carriedGhostSpawnTimer >= carriedGhostSpawnInterval - ghostSpawnWarningTime) {
            getWarningPortalX();
        }

        if (carriedGhostSpawnTimer >= carriedGhostSpawnInterval) {
            carriedGhostSpawnTimer = 0;
            pendingCarriedGhosts--;
            spawnGhostThroughPortal(warningPortalX);
        }
    }

    public boolean isPlayerMoving() {
        return playerPixelX != targetPixelX || playerPixelY != targetPixelY;
    }

    public void chooseNextTarget() {
        int tileX = (int) (playerPixelX / tileSize);
        int tileY = (int) (playerPixelY / tileSize);

        if (canMove(tileX + nextDirectionX, tileY + nextDirectionY)) {
            directionX = nextDirectionX;
            directionY = nextDirectionY;
        }

        if (directionX == 0 && directionY == 0) {
            return;
        }

        if (!canMove(tileX + directionX, tileY + directionY)) {
            directionX = 0;
            directionY = 0;
            return;
        }

        targetPixelX = (tileX + directionX) * tileSize;
        targetPixelY = (tileY + directionY) * tileSize;
        lastDirectionX = directionX;
        lastDirectionY = directionY;
    }

    public void moveTowardTarget() {
        playerPixelX = moveValueToward(playerPixelX, targetPixelX, getPlayerSpeed());
        playerPixelY = moveValueToward(playerPixelY, targetPixelY, getPlayerSpeed());

        if (!isPlayerMoving()) {
            handlePortalWrap();
            updateLastPlayerTile();
            eatDotAtPlayer();
        }
    }

    public double moveValueToward(double currentValue, double targetValue, double speed) {
        if (currentValue < targetValue) {
            return Math.min(currentValue + speed, targetValue);
        }
        if (currentValue > targetValue) {
            return Math.max(currentValue - speed, targetValue);
        }

        return currentValue;
    }

    public double getPlayerSpeed() {
        return isPowerUpActive(2) ? playerSpeed + 1.0 : playerSpeed;
    }

    public double getRandomGhostSpeedOffset() {
        return random.nextDouble() - 0.5;
    }

    public double getGhostSpeed(Ghost ghost) {
        double adjustedSpeed = Math.max(0.1, ghostSpeed + ghost.speedOffset);
        return isPowerUpActive(2) ? adjustedSpeed * 0.35 : adjustedSpeed;
    }

    public boolean canMove(int x, int y) {
        if (isPortalTile(x, y)) {
            return true;
        }

        if (x <= 0 || x >= maxScreenCol - 1 || y <= 0 || y >= maxScreenRow - 1) {
            return false;
        }

        return !maze[x][y];
    }

    public void handlePortalWrap() {
        int tileX = (int) (playerPixelX / tileSize);
        int tileY = (int) (playerPixelY / tileSize);

        if (tileY != tunnelY) {
            return;
        }

        if (boardClear && (tileX == 0 || tileX == maxScreenCol - 1)) {
            advanceToNextBoard(tileX);
            return;
        }

        if (tileX == 0) {
            playerPixelX = (maxScreenCol - 1) * tileSize;
            targetPixelX = playerPixelX;
        } else if (tileX == maxScreenCol - 1) {
            playerPixelX = 0;
            targetPixelX = playerPixelX;
        }
    }

    public void advanceToNextBoard(int exitTileX) {
        int carriedGhostCount = ghosts.size();
        boolean enteredFromLeft = exitTileX == 0;

        level++;
        applyFruitBonuses();
        roomInitialScore = score;
        nextFruitScore = fruitScoreInterval;
        fruitsSpawnedThisLevel = 0;
        collectedFruits = new int[4];
        boardClear = false;
        powerMode = false;
        Arrays.fill(powerUpTimers, 0);
        powerModeTimer = 0;
        ghostEatScore = 200;
        ghostSpawnTimer = 0;
        pendingCarriedGhosts = carriedGhostCount;
        boardGhostDelayTimer = boardGhostDelay;
        carriedGhostSpawnTimer = 0;
        warningPortalX = -1;
        animationCounter = 0;
        ghostAnimationCounter = 0;

        generateMaze();
        generateDots();
        fruits.clear();
        powerUps.clear();
        spikeTraps.clear();
        ghostDeathEffects.clear();
        ghosts.clear();

        if (enteredFromLeft) {
            playerPixelX = (maxScreenCol - 2) * tileSize;
            lastDirectionX = -1;
        } else {
            playerPixelX = tileSize;
            lastDirectionX = 1;
        }

        playerPixelY = tunnelY * tileSize;
        targetPixelX = playerPixelX;
        targetPixelY = playerPixelY;
        directionX = 0;
        directionY = 0;
        nextDirectionX = 0;
        nextDirectionY = 0;
        lastDirectionY = 0;
        updateLastPlayerTile();
        eatDotAtPlayer();
    }

    public void applyFruitBonuses() {
        double multiplier = 1.0;
        double[] fruitMultipliers = { 1.5, 2.0, 2.5, 3.0 };

        for (int i = 0; i < collectedFruits.length; i++) {
            for (int count = 0; count < collectedFruits[i]; count++) {
                multiplier *= fruitMultipliers[i];
            }
        }

        if (multiplier > 1.0) {
            int roomScore = score - roomInitialScore;
            score = roomInitialScore + (int) Math.ceil(roomScore * multiplier);
        }
    }

    public void eatDotAtPlayer() {
        int tileX = (int) (playerPixelX / tileSize);
        int tileY = (int) (playerPixelY / tileSize);

        collectPelletAt(tileX, tileY, true);
        eatFruitAtPlayer(tileX, tileY);
        eatPowerUpAtPlayer(tileX, tileY);

        if (isPowerUpActive(0)) {
            collectMagnetPellets(tileX, tileY);
        }

        if (!boardClear && areAllPelletsEaten()) {
            boardClear = true;
            warningPortalX = -1;
        }
    }

    public void collectPelletAt(int tileX, int tileY, boolean countTowardPowerDrop) {
        if (dots[tileX][tileY]) {
            dots[tileX][tileY] = false;
            addScore(getSmallDotScore());

            if (countTowardPowerDrop) {
                smallDotsTowardPowerUp++;
                checkPowerUpDrop();
            }
        }

        if (bigDots[tileX][tileY]) {
            bigDots[tileX][tileY] = false;
            addScore(getBigDotScore());
            startPowerMode();
        }
    }

    public int getSmallDotScore() {
        return isPowerUpActive(3) ? 5 : 1;
    }

    public int getBigDotScore() {
        return isPowerUpActive(3) ? 50 : 10;
    }

    public void checkPowerUpDrop() {
        if (noPowerMode) {
            return;
        }

        while (smallDotsTowardPowerUp >= powerUpDropSmallDots) {
            smallDotsTowardPowerUp -= powerUpDropSmallDots;
            spawnPowerUp();
        }
    }

    public void collectMagnetPellets(int centerX, int centerY) {
        for (int x = centerX - 2; x <= centerX + 2; x++) {
            for (int y = centerY - 2; y <= centerY + 2; y++) {
                if (x > 0 && x < maxScreenCol - 1 && y > 0 && y < maxScreenRow - 1) {
                    collectPelletAt(x, y, false);
                }
            }
        }
    }

    public boolean areAllPelletsEaten() {
        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                if (dots[x][y] || bigDots[x][y]) {
                    return false;
                }
            }
        }

        return true;
    }

    public void addScore(int points) {
        score += points;
        updateFruitSpawns();
    }

    public void updateFruitSpawns() {
        while (score - roomInitialScore >= nextFruitScore) {
            if (fruitsSpawnedThisLevel < maxFruitsPerLevel) {
                spawnFruit();
            }
            nextFruitScore += fruitScoreInterval;
        }
    }

    public void spawnFruit() {
        if (fruitsSpawnedThisLevel >= maxFruitsPerLevel) {
            return;
        }

        ArrayList<int[]> spawnTiles = new ArrayList<>();

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                if (!maze[x][y] && !isPortalEntrance(x, y) && !hasFruitAt(x, y)
                        && !(x == getPlayerTileX() && y == getPlayerTileY())) {
                    spawnTiles.add(new int[] { x, y });
                }
            }
        }

        if (spawnTiles.isEmpty()) {
            return;
        }

        int[] tile = spawnTiles.get(random.nextInt(spawnTiles.size()));
        fruits.add(new Fruit(random.nextInt(fruitSprites.length), tile[0], tile[1]));
        fruitsSpawnedThisLevel++;
    }

    public boolean hasFruitAt(int tileX, int tileY) {
        for (Fruit fruit : fruits) {
            if (fruit.tileX == tileX && fruit.tileY == tileY) {
                return true;
            }
        }

        return false;
    }

    public void spawnPowerUp() {
        ArrayList<int[]> spawnTiles = new ArrayList<>();

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                if (!maze[x][y] && !isPortalEntrance(x, y) && !hasFruitAt(x, y) && !hasPowerUpAt(x, y)
                        && !hasSpikeAt(x, y) && !(x == getPlayerTileX() && y == getPlayerTileY())) {
                    spawnTiles.add(new int[] { x, y });
                }
            }
        }

        if (spawnTiles.isEmpty()) {
            return;
        }

        int[] tile = spawnTiles.get(random.nextInt(spawnTiles.size()));
        powerUps.add(new PowerUp(random.nextInt(powerUpSprites.length), tile[0], tile[1]));
    }

    public boolean hasPowerUpAt(int tileX, int tileY) {
        for (PowerUp powerUp : powerUps) {
            if (powerUp.tileX == tileX && powerUp.tileY == tileY) {
                return true;
            }
        }

        return false;
    }

    public void eatFruitAtPlayer(int tileX, int tileY) {
        for (int i = fruits.size() - 1; i >= 0; i--) {
            Fruit fruit = fruits.get(i);

            if (fruit.tileX == tileX && fruit.tileY == tileY) {
                collectedFruits[fruit.type]++;
                fruits.remove(i);
            }
        }
    }

    public void eatPowerUpAtPlayer(int tileX, int tileY) {
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);

            if (powerUp.tileX == tileX && powerUp.tileY == tileY) {
                activatePowerUp(powerUp.type);
                powerUps.remove(i);
            }
        }
    }

    public void activatePowerUp(int powerUpType) {
        if (powerUpType == 4) {
            startPowerMode();
            return;
        }

        powerUpTimers[powerUpType] += powerUpDuration;

        if (powerUpType == 1) {
            extendSpikeTraps(powerUpTimers[powerUpType]);
            placeSpikeTraps(powerUpTimers[powerUpType]);
        }
    }

    public void placeSpikeTraps(int timer) {
        for (int i = 0; i < spikeTrapCount; i++) {
            ArrayList<int[]> trapTiles = new ArrayList<>();

            for (int x = 1; x < maxScreenCol - 1; x++) {
                for (int y = 1; y < maxScreenRow - 1; y++) {
                    if (!maze[x][y] && !isPortalEntrance(x, y) && !hasSpikeAt(x, y)
                            && !(x == getPlayerTileX() && y == getPlayerTileY())) {
                        trapTiles.add(new int[] { x, y });
                    }
                }
            }

            if (trapTiles.isEmpty()) {
                return;
            }

            int[] tile = trapTiles.get(random.nextInt(trapTiles.size()));
            spikeTraps.add(new SpikeTrap(tile[0], tile[1], timer));
        }
    }

    public void extendSpikeTraps(int timer) {
        for (SpikeTrap spikeTrap : spikeTraps) {
            spikeTrap.timer = Math.max(spikeTrap.timer, timer);
        }
    }

    public boolean isPowerUpActive(int powerUpType) {
        return powerUpType >= 0 && powerUpType < powerUpTimers.length && powerUpTimers[powerUpType] > 0;
    }

    public boolean hasSpikeAt(int tileX, int tileY) {
        for (SpikeTrap spikeTrap : spikeTraps) {
            if (spikeTrap.tileX == tileX && spikeTrap.tileY == tileY) {
                return true;
            }
        }

        return false;
    }

    public void startPowerMode() {
        powerMode = true;
        powerModeTimer += powerPelletDuration;
        ghostEatScore = 200;

        for (Ghost ghost : ghosts) {
            ghost.path.clear();
        }
    }

    public void stopPowerMode() {
        powerMode = false;
        powerModeTimer = 0;
        ghostEatScore = 200;

        for (Ghost ghost : ghosts) {
            ghost.path.clear();
        }
    }

    public void updateLastPlayerTile() {
        lastPlayerTileX = (int) (playerPixelX / tileSize);
        lastPlayerTileY = (int) (playerPixelY / tileSize);
    }

    public void checkGhostCollision() {
        for (int i = ghosts.size() - 1; i >= 0; i--) {
            Ghost ghost = ghosts.get(i);

            if (Math.abs(playerPixelX - ghost.pixelX) < tileSize && Math.abs(playerPixelY - ghost.pixelY) < tileSize) {
                if (powerMode) {
                    addScore(getGhostEatScore());
                    ghostEatScore = Math.min(1600, ghostEatScore * 2);
                    spawnGhostDeathEffect(ghost);
                    ghosts.remove(i);
                    continue;
                }

                startDeathAnimation();
                return;
            }
        }
    }

    public void startDeathAnimation() {
        playerDead = true;
        gameStarted = false;
        directionX = 0;
        directionY = 0;
        nextDirectionX = 0;
        nextDirectionY = 0;
        targetPixelX = playerPixelX;
        targetPixelY = playerPixelY;
        deathAnimationCounter = 0;
        deathFrame = 0;
    }

    public int getGhostEatScore() {
        return isPowerUpActive(3) ? ghostEatScore * 5 : ghostEatScore;
    }

    public void spawnGhostDeathEffect(Ghost ghost) {
        int startX = clampInt((int) Math.round(ghost.pixelX / tileSize), 0, maxScreenCol - 1);
        int startY = clampInt((int) Math.round(ghost.pixelY / tileSize), 0, maxScreenRow - 1);
        ArrayList<int[]> path = getShortestEyesPathToPortal(startX, startY);

        ghostDeathEffects.add(new GhostDeathEffect(
                ghost.pixelX,
                ghost.pixelY,
                path,
                ghostDeathFrameTime));
    }

    public ArrayList<int[]> getShortestEyesPathToPortal(int startX, int startY) {
        ArrayList<int[]> leftPath = findAStarPath(startX, startY, 0, tunnelY);
        ArrayList<int[]> rightPath = findAStarPath(startX, startY, maxScreenCol - 1, tunnelY);

        if (startX == 0 && startY == tunnelY) {
            return leftPath;
        }
        if (startX == maxScreenCol - 1 && startY == tunnelY) {
            return rightPath;
        }
        if (leftPath.isEmpty()) {
            return rightPath;
        }
        if (rightPath.isEmpty()) {
            return leftPath;
        }

        return leftPath.size() <= rightPath.size() ? leftPath : rightPath;
    }

    public void updateGhostDeathEffects() {
        for (int i = ghostDeathEffects.size() - 1; i >= 0; i--) {
            GhostDeathEffect effect = ghostDeathEffects.get(i);

            if (effect.deathFrameTimer > 0) {
                effect.deathFrameTimer--;
                continue;
            }

            if (moveGhostEyesTowardPortal(effect)) {
                ghostDeathEffects.remove(i);
            }
        }
    }

    public boolean moveGhostEyesTowardPortal(GhostDeathEffect effect) {
        if (effect.pixelX == effect.targetPixelX && effect.pixelY == effect.targetPixelY) {
            if (effect.path.isEmpty()) {
                return true;
            }

            int[] nextTile = effect.path.remove(0);
            effect.targetPixelX = nextTile[0] * tileSize;
            effect.targetPixelY = nextTile[1] * tileSize;
        }

        double dx = effect.targetPixelX - effect.pixelX;
        double dy = effect.targetPixelY - effect.pixelY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= ghostEyesSpeed) {
            return true;
        }

        effect.pixelX += dx / distance * ghostEyesSpeed;
        effect.pixelY += dy / distance * ghostEyesSpeed;
        return false;
    }

    public void updateDeathAnimation() {
        if (deathAnimationDone) {
            return;
        }

        deathAnimationCounter++;

        if (deathAnimationCounter >= deathFrameDelay) {
            deathAnimationCounter = 0;
            deathFrame++;

            if (deathFrame >= pacSprites.length) {
                deathAnimationDone = true;
                handleFinalScore();
            }
        }
    }

    public void handleFinalScore() {
        if (finalScoreHandled) {
            return;
        }

        finalScoreHandled = true;
        pendingFinalScore = calculateFinalScore();
        score = pendingFinalScore;

        if (getHighScoreIndex(pendingFinalScore) != -1) {
            nameEntry = new char[] { 'A', 'A', 'A' };
            nameEntryIndex = 0;
            screenState = STATE_NAME_ENTRY;
        }
    }

    public int calculateFinalScore() {
        int elapsedSeconds = Math.max(1, elapsedFrames / framesPerSecond);
        int clearedLevels = Math.max(0, level - 1);
        int clearBonus = (score * clearedLevels) / elapsedSeconds;

        return score + clearBonus;
    }

    public int getHighScoreIndex(int newScore) {
        for (int i = 0; i < highScoreValues.length; i++) {
            if (newScore > highScoreValues[i]) {
                return i;
            }
        }

        return -1;
    }

    public void saveNewHighScore() {
        int insertIndex = getHighScoreIndex(pendingFinalScore);

        if (insertIndex == -1) {
            screenState = STATE_GAME;
            return;
        }

        for (int i = highScoreValues.length - 1; i > insertIndex; i--) {
            highScoreValues[i] = highScoreValues[i - 1];
            highScoreNames[i] = highScoreNames[i - 1];
        }

        highScoreValues[insertIndex] = pendingFinalScore;
        highScoreNames[insertIndex] = new String(nameEntry);
        writeHighScores();
        screenState = STATE_GAME;
    }
    
    // Updates ghost animation timer and movement logic.
    public void updateGhosts() {
        ghostAnimationCounter++;

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            Ghost ghost = ghosts.get(i);
            updateGhost(ghost);

            if (checkSpikeTrapCollision(ghost)) {
                spawnGhostDeathEffect(ghost);
                ghosts.remove(i);
            }
        }
    }

    public void updateGhost(Ghost ghost) {
        if (!isGhostMoving(ghost)) {
            chooseGhostTarget(ghost);
        }

        if (isGhostMoving(ghost)) {
            moveGhostTowardTarget(ghost);
        }
    }

    public boolean isGhostMoving(Ghost ghost) {
        return ghost.pixelX != ghost.targetPixelX || ghost.pixelY != ghost.targetPixelY;
    }

    public void chooseGhostTarget(Ghost ghost) {
        int tileX = (int) (ghost.pixelX / tileSize);
        int tileY = (int) (ghost.pixelY / tileSize);
        int[] direction = chooseGhostDirection(ghost, tileX, tileY);

        if (direction == null) {
            ghost.directionX = 0;
            ghost.directionY = 0;
            return;
        }

        ghost.directionX = direction[0];
        ghost.directionY = direction[1];
        ghost.targetPixelX = (tileX + ghost.directionX) * tileSize;
        ghost.targetPixelY = (tileY + ghost.directionY) * tileSize;
    }

	 // Ghost AI types:
	 // 0 = random wall bounce
	 // 1 = always prefers right turns
	 // 2 = always prefers left turns
	 // 3 = A* pathfinding toward player
    public int[] chooseGhostDirection(Ghost ghost, int tileX, int tileY) {
        if (powerMode) {
            return chooseFleeDirection(tileX, tileY);
        }

        if (ghost.type == 0) {
            return chooseRandomWallBounceDirection(ghost, tileX, tileY);
        }
        if (ghost.type == 1) {
            return chooseTurnDirection(ghost, tileX, tileY, true);
        }
        if (ghost.type == 2) {
            return chooseTurnDirection(ghost, tileX, tileY, false);
        }

        return chooseAStarDirection(ghost, tileX, tileY);
    }

    public int[] chooseFleeDirection(int tileX, int tileY) {
        int[][] directions = {
            { 1, 0 },
            { -1, 0 },
            { 0, 1 },
            { 0, -1 }
        };
        int bestDistance = -1;
        ArrayList<int[]> bestDirections = new ArrayList<>();

        for (int[] direction : directions) {
            int nextX = tileX + direction[0];
            int nextY = tileY + direction[1];

            if (!canMove(nextX, nextY)) {
                continue;
            }

            int distance = getManhattanDistance(nextX, nextY, getPlayerTileX(), getPlayerTileY());

            if (distance > bestDistance) {
                bestDistance = distance;
                bestDirections.clear();
                bestDirections.add(direction);
            } else if (distance == bestDistance) {
                bestDirections.add(direction);
            }
        }

        if (bestDirections.isEmpty()) {
            return null;
        }

        return bestDirections.get(random.nextInt(bestDirections.size()));
    }

    public int[] chooseRandomWallBounceDirection(Ghost ghost, int tileX, int tileY) {
        if (ghost.directionX == 0 && ghost.directionY == 0) {
            return getRandomValidDirection(tileX, tileY);
        }

        int[][] forwardChoices = {
            { ghost.directionX, ghost.directionY },
            { ghost.directionY, -ghost.directionX },
            { -ghost.directionY, ghost.directionX }
        };

        ArrayList<int[]> validForwardChoices = new ArrayList<>();

        for (int[] direction : forwardChoices) {
            if (canMove(tileX + direction[0], tileY + direction[1])) {
                validForwardChoices.add(direction);
            }
        }

        if (!validForwardChoices.isEmpty()) {
            return validForwardChoices.get(random.nextInt(validForwardChoices.size()));
        }

        int[] reverse = { -ghost.directionX, -ghost.directionY };
        if (canMove(tileX + reverse[0], tileY + reverse[1])) {
            return reverse;
        }

        return null;
    }

    public int[] chooseTurnDirection(Ghost ghost, int tileX, int tileY, boolean turnRight) {

        if (ghost.directionX == 0 && ghost.directionY == 0) {
            return getRandomValidDirection(tileX, tileY);
        }

        int[] right = { ghost.directionY, -ghost.directionX };
        int[] left = { -ghost.directionY, ghost.directionX };
        int[] straight = { ghost.directionX, ghost.directionY };
        int[] reverse = { -ghost.directionX, -ghost.directionY };

        int[] preferred = turnRight ? right : left;
        int[] secondary = turnRight ? left : right;

        // Force opposite turn after 4 preferred turns
        if (ghost.preferredTurnCount >= 4 &&
            canMove(tileX + secondary[0], tileY + secondary[1])) {

            ghost.preferredTurnCount = 0;
            return secondary;
        }

        if (canMove(tileX + preferred[0], tileY + preferred[1])) {
            ghost.preferredTurnCount++;
            return preferred;
        }

        if (canMove(tileX + straight[0], tileY + straight[1])) {
            return straight;
        }

        if (canMove(tileX + secondary[0], tileY + secondary[1])) {
            ghost.preferredTurnCount = 0;
            return secondary;
        }

        if (canMove(tileX + reverse[0], tileY + reverse[1])) {
            ghost.preferredTurnCount = 0;
            return reverse;
        }

        return null;
    }

    public int[] chooseAStarDirection(Ghost ghost, int tileX, int tileY) {
        if (ghost.path.isEmpty()) {
            ghost.goalX = getPlayerTileX();
            ghost.goalY = getPlayerTileY();
            ghost.path = findAStarPath(tileX, tileY, ghost.goalX, ghost.goalY);
        }

        if (ghost.path.isEmpty()) {
            return getRandomValidDirection(tileX, tileY);
        }

        int[] nextTile = ghost.path.remove(0);
        return new int[] { nextTile[0] - tileX, nextTile[1] - tileY };
    }

    public int[] getRandomValidDirection(int tileX, int tileY) {
        int[][] directions = {
            { 1, 0 },
            { -1, 0 },
            { 0, 1 },
            { 0, -1 }
        };

        shuffleDirections(directions);

        for (int[] direction : directions) {
            if (canMove(tileX + direction[0], tileY + direction[1])) {
                return direction;
            }
        }

        return null;
    }

    public void setRandomGhostDirection(Ghost ghost) {
        int tileX = (int) (ghost.pixelX / tileSize);
        int tileY = (int) (ghost.pixelY / tileSize);
        int[] direction = getRandomValidDirection(tileX, tileY);

        if (direction != null) {
            ghost.directionX = direction[0];
            ghost.directionY = direction[1];
        }
    }

	// Moves a ghost toward its target tile.
    public void moveGhostTowardTarget(Ghost ghost) {
        double speed = getGhostSpeed(ghost);
        ghost.pixelX = moveValueToward(ghost.pixelX, ghost.targetPixelX, speed);
        ghost.pixelY = moveValueToward(ghost.pixelY, ghost.targetPixelY, speed);

        if (!isGhostMoving(ghost)) {
            handleGhostPortalWrap(ghost);
        }
    }

    public boolean checkSpikeTrapCollision(Ghost ghost) {
        int tileX = (int) (ghost.pixelX / tileSize);
        int tileY = (int) (ghost.pixelY / tileSize);

        for (SpikeTrap spikeTrap : spikeTraps) {
            if (spikeTrap.tileX == tileX && spikeTrap.tileY == tileY) {
                spikeTrap.used = true;
                addScore(200);
                return true;
            }
        }

        return false;
    }

    public void handleGhostPortalWrap(Ghost ghost) {
        int tileX = (int) (ghost.pixelX / tileSize);
        int tileY = (int) (ghost.pixelY / tileSize);

        if (tileY != tunnelY) {
            return;
        }

        if (tileX == 0) {
            ghost.pixelX = (maxScreenCol - 1) * tileSize;
            ghost.targetPixelX = ghost.pixelX;
            ghost.path.clear();
        } else if (tileX == maxScreenCol - 1) {
            ghost.pixelX = 0;
            ghost.targetPixelX = ghost.pixelX;
            ghost.path.clear();
        }
    }

    public ArrayList<int[]> findAStarPath(int startX, int startY, int goalX, int goalY) {
        ArrayList<PathNode> openNodes = new ArrayList<>();
        boolean[][] closed = new boolean[maxScreenCol][maxScreenRow];
        int[][] bestCost = new int[maxScreenCol][maxScreenRow];

        for (int x = 0; x < maxScreenCol; x++) {
            for (int y = 0; y < maxScreenRow; y++) {
                bestCost[x][y] = Integer.MAX_VALUE;
            }
        }

        openNodes.add(new PathNode(startX, startY, 0, getAStarHeuristic(startX, startY, goalX, goalY), null));
        bestCost[startX][startY] = 0;

        while (!openNodes.isEmpty()) {
            PathNode current = removeBestNode(openNodes);

            if (current.x == goalX && current.y == goalY) {
                return buildPath(current);
            }

            closed[current.x][current.y] = true;

            for (int[] neighbor : getWalkableNeighbors(current.x, current.y)) {
                int nextX = neighbor[0];
                int nextY = neighbor[1];
                int nextCost = current.costFromStart + 1;

                if (closed[nextX][nextY] || nextCost >= bestCost[nextX][nextY]) {
                    continue;
                }

                bestCost[nextX][nextY] = nextCost;
                openNodes.add(new PathNode(nextX, nextY, nextCost, getAStarHeuristic(nextX, nextY, goalX, goalY), current));
            }
        }

        return new ArrayList<int[]>();
    }

    public PathNode removeBestNode(ArrayList<PathNode> openNodes) {
        int bestIndex = 0;

        for (int i = 1; i < openNodes.size(); i++) {
            if (openNodes.get(i).getTotalCost() < openNodes.get(bestIndex).getTotalCost()) {
                bestIndex = i;
            }
        }

        return openNodes.remove(bestIndex);
    }

    public ArrayList<int[]> buildPath(PathNode goalNode) {
        ArrayList<int[]> path = new ArrayList<>();
        PathNode current = goalNode;

        while (current.parent != null) {
            path.add(0, new int[] { current.x, current.y });
            current = current.parent;
        }

        return path;
    }

    public ArrayList<int[]> getWalkableNeighbors(int x, int y) {
        ArrayList<int[]> neighbors = new ArrayList<>();
        int[][] directions = {
            { 1, 0 },
            { -1, 0 },
            { 0, 1 },
            { 0, -1 }
        };

        for (int[] direction : directions) {
            int nextX = x + direction[0];
            int nextY = y + direction[1];

            if (canMove(nextX, nextY)) {
                neighbors.add(new int[] { nextX, nextY });
            }
        }

        if (isPortalTile(x, y)) {
            if (x == 0) {
                neighbors.add(new int[] { maxScreenCol - 1, y });
            } else {
                neighbors.add(new int[] { 0, y });
            }
        }

        return neighbors;
    }

    public int getPlayerTileX() {
        return lastPlayerTileX;
    }

    public int getPlayerTileY() {
        return lastPlayerTileY;
    }

    public int getManhattanDistance(int startX, int startY, int goalX, int goalY) {
        return Math.abs(startX - goalX) + Math.abs(startY - goalY);
    }

    public int getAStarHeuristic(int startX, int startY, int goalX, int goalY) {
    	return Math.abs(startX - goalX) + Math.abs(startY - goalY);
    }
    
    //draw maze func
    public void drawMaze(Graphics2D g2) {
    	
        g2.setColor(Color.YELLOW.darker().darker());

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                if (maze[x][y]) {
                    drawTile(g2, x, y);
                }
            }
        }
    }

    public void drawDots(Graphics2D g2) {
        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                if (dots[x][y]) {
                    drawImageAtTile(g2, dotSmall, x, y);
                } else if (bigDots[x][y]) {
                    drawImageAtTile(g2, dotBig, x, y);
                }
            }
        }
    }

    public void drawFruits(Graphics2D g2) {
        for (Fruit fruit : fruits) {
            drawImageAtTile(g2, fruitSprites[fruit.type], fruit.tileX, fruit.tileY);
        }
    }

    public void drawPowerUps(Graphics2D g2) {
        for (PowerUp powerUp : powerUps) {
            drawImageAtTile(g2, powerUpSprites[powerUp.type], powerUp.tileX, powerUp.tileY);
        }
    }

    public void drawSpikeTraps(Graphics2D g2) {
        for (SpikeTrap spikeTrap : spikeTraps) {
            drawImageAtTile(g2, spikeSprites[spikeTrap.used ? 1 : 0], spikeTrap.tileX, spikeTrap.tileY);
        }
    }

    public void drawExitMarkers(Graphics2D g2) {
        if (!boardClear) {
            return;
        }

        BufferedImage sprite = outSprites[(elapsedFrames / animationDelay) % outSprites.length];
        int leftX = tileSize;
        int rightX = (maxScreenCol - 2) * tileSize;
        int screenY = worldToScreenY(tunnelY * tileSize);

        g2.drawImage(sprite, worldToScreenX(leftX) + tileSize, screenY, -tileSize, tileSize, null);
        g2.drawImage(sprite, worldToScreenX(rightX), screenY, tileSize, tileSize, null);
    }

    public void drawSpawnWarning(Graphics2D g2) {
        if (warningPortalX == -1 || boardClear || playerDead) {
            return;
        }

        BufferedImage sprite = warnSprites[(elapsedFrames / animationDelay) % warnSprites.length];
        int screenX = worldToScreenX(warningPortalX * tileSize);
        int screenY = worldToScreenY(tunnelY * tileSize);

        if (warningPortalX == maxScreenCol - 1) {
            g2.drawImage(sprite, screenX + tileSize, screenY, -tileSize, tileSize, null);
        } else {
            g2.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        }
    }

    public void drawPlayer(Graphics2D g2) {
        if (deathAnimationDone) {
            return;
        }

        BufferedImage sprite = playerDead
                ? pacSprites[deathFrame]
                : pacSprites[(animationCounter / animationDelay) % 2];
        int screenX = worldToScreenX(playerPixelX);
        int screenY = worldToScreenY(playerPixelY);
        double angle = getPlayerAngle();

        Graphics2D playerGraphics = (Graphics2D) g2.create();
        playerGraphics.rotate(angle, screenX + tileSize / 2.0, screenY + tileSize / 2.0);
        playerGraphics.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        playerGraphics.dispose();
    }

    public double getPlayerAngle() {
        if (lastDirectionX < 0) {
            return Math.PI;
        }
        if (lastDirectionY > 0) {
            return -Math.PI / 2;
        }
        if (lastDirectionY < 0) {
            return Math.PI / 2;
        }
        return 0;
    }

    public void drawOverScreen(Graphics2D g2) {
        if (deathAnimationDone) {
            g2.drawImage(overScreen, 0, 0, getWidth(), getHeight(), null);
        }
    }

    public void drawPauseScreen(Graphics2D g2) {
        if (paused) {
            g2.drawImage(pauseScreen, 0, 0, getWidth(), getHeight(), null);

            if (quitConfirmVisible) {
                drawQuitConfirm(g2);
            }
        }
    }

    public void drawQuitConfirm(Graphics2D g2) {
        int boxWidth = Math.min(520, getWidth() - 48);
        int boxHeight = 120;
        int boxX = (getWidth() - boxWidth) / 2;
        int boxY = (getHeight() - boxHeight) / 2;

        g2.setColor(Color.BLACK);
        g2.fillRect(boxX, boxY, boxWidth, boxHeight);
        g2.setColor(Color.WHITE);
        g2.drawRect(boxX, boxY, boxWidth, boxHeight);

        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 18));
        drawCenteredWhiteText(g2, "ARE YOU SURE YOU WANT TO QUIT TO MENU?", getWidth() / 2, boxY + 42);
        drawCenteredWhiteText(g2, "PROGRESS WILL NOT BE SAVED", getWidth() / 2, boxY + 72);
        drawCenteredWhiteText(g2, "PRESS ESC AGAIN", getWidth() / 2, boxY + 100);
    }

    public void drawCenteredWhiteText(Graphics2D g2, String text, int centerX, int y) {
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.setColor(Color.WHITE);
        g2.drawString(text, centerX - textWidth / 2, y);
    }

    public void drawHud(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), hudHeight);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("SCORE " + score, 12, 20);
        g2.drawString("LEVEL " + level, getWidth() / 2 - 38, 20);
        g2.drawString("TIME " + getElapsedTimeText(), getWidth() - 115, 20);

        if (boardClear) {
            g2.setColor(Color.YELLOW);
            g2.drawString("BOARD CLEAR", getWidth() / 2 - 58, 42);
            return;
        }

        drawActiveEffectTimers(g2);
    }

    public void drawMenu(Graphics2D g2) {
        g2.drawImage(menuScreen, 0, 0, getWidth(), getHeight(), null);

        String[] options = { "NEW RUN", "OPTION", "HALL OF FAME", "QUIT" };
        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 30));

        int centerX = getWidth() / 2;
        int startY = Math.max(220, getHeight() / 2 - 35);

        for (int i = 0; i < options.length; i++) {
            String text = i == menuChoice ? ">" + options[i] + "<" : options[i];
            drawCenteredMenuText(g2, text, centerX, startY + i * 46);
        }
    }

    public void drawOptions(Graphics2D g2) {
        g2.drawImage(blankMenuScreen, 0, 0, getWidth(), getHeight(), null);
        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 20));

        String[] options = {
            "MOVE SET: " + (draftUseWasdMovement ? "WASD" : "ARROW"),
            "GHOST SPEED: " + formatDouble(draftGhostSpeed),
            "PACMAN SPEED: " + formatDouble(draftPlayerSpeed),
            "GHOST SPAWN INTERVAL: " + draftGhostSpawnSeconds,
            "PELLET TIME: " + draftPelletSeconds,
            "POWER TIME: " + draftPowerSeconds,
            "NO POWER MODE: " + draftNoPowerMode,
            "MAZE WIDTH: " + draftMazeWidth,
            "MAZE HEIGHT: " + draftMazeHeight
        };

        int centerX = getWidth() / 2;
        int startY = 90;

        for (int i = 0; i < options.length; i++) {
            String text = i == optionChoice ? ">" + options[i] + "<" : options[i];
            drawCenteredMenuText(g2, text, centerX, startY + i * 34);
        }

        String saveText = optionChoice == options.length && optionActionChoice == 0 ? ">SAVE<" : "SAVE";
        String cancelText = optionChoice == options.length && optionActionChoice == 1 ? ">CANCEL<" : "CANCEL";
        drawCenteredMenuText(g2, saveText + "   ||   " + cancelText, centerX, startY + options.length * 34 + 22);
    }

    public void drawHallOfFame(Graphics2D g2) {
        g2.drawImage(blankMenuScreen, 0, 0, getWidth(), getHeight(), null);
        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 28));
        drawCenteredMenuText(g2, "HALL OF FAME", getWidth() / 2, getHeight() / 2 - 90);
        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 24));

        for (int i = 0; i < highScoreNames.length; i++) {
            drawCenteredMenuText(g2, (i + 1) + ". " + highScoreNames[i] + ": " + highScoreValues[i],
                    getWidth() / 2, getHeight() / 2 - 35 + i * 42);
        }

        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 16));
        drawCenteredMenuText(g2, "PRESS ANY KEY", getWidth() / 2, getHeight() / 2 + 130);
    }

    public void drawNameEntry(Graphics2D g2) {
        g2.drawImage(blankMenuScreen, 0, 0, getWidth(), getHeight(), null);
        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 26));
        drawCenteredMenuText(g2, "NEW HALL OF FAME SCORE", getWidth() / 2, getHeight() / 2 - 100);
        drawCenteredMenuText(g2, String.valueOf(pendingFinalScore), getWidth() / 2, getHeight() / 2 - 62);

        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 44));
        int startX = getWidth() / 2 - 58;
        int y = getHeight() / 2 + 10;

        for (int i = 0; i < nameEntry.length; i++) {
            String letter = String.valueOf(nameEntry[i]);
            int x = startX + i * 58;
            drawOutlinedText(g2, letter, x, y);

            if (i == nameEntryIndex) {
                int letterWidth = g2.getFontMetrics().stringWidth(letter);
                g2.setColor(Color.YELLOW);
                g2.drawLine(x, y + 8, x + letterWidth, y + 8);
                g2.setColor(Color.WHITE);
                g2.drawLine(x, y + 10, x + letterWidth, y + 10);
            }
        }

        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 16));
        drawCenteredMenuText(g2, "UP/DOWN CHANGE  LEFT/RIGHT MOVE  ENTER CONFIRM", getWidth() / 2, getHeight() / 2 + 78);
    }

    public void drawCenteredMenuText(Graphics2D g2, String text, int centerX, int y) {
        int textWidth = g2.getFontMetrics().stringWidth(text);
        drawOutlinedText(g2, text, centerX - textWidth / 2, y);
    }

    public void drawOutlinedText(Graphics2D g2, String text, int x, int y) {
        g2.setColor(Color.WHITE);
        g2.drawString(text, x - 1, y);
        g2.drawString(text, x + 1, y);
        g2.drawString(text, x, y - 1);
        g2.drawString(text, x, y + 1);
        g2.setColor(Color.YELLOW);
        g2.drawString(text, x, y);
    }

    public String formatDouble(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        }

        return String.valueOf(value);
    }

    public void startNewRun() {
        resetGame();
        screenState = STATE_GAME;
    }

    public void openOptions() {
        draftUseWasdMovement = useWasdMovement;
        draftGhostSpeed = ghostSpeed;
        draftPlayerSpeed = playerSpeed;
        draftGhostSpawnSeconds = ghostSpawnInterval / framesPerSecond;
        draftPelletSeconds = powerPelletDuration / framesPerSecond;
        draftPowerSeconds = powerUpDuration / framesPerSecond;
        draftNoPowerMode = noPowerMode;
        draftMazeWidth = maxScreenCol;
        draftMazeHeight = maxScreenRow;
        optionChoice = 0;
        optionActionChoice = 0;
        screenState = STATE_OPTIONS;
    }

    public void saveOptions() {
        useWasdMovement = draftUseWasdMovement;
        ghostSpeed = draftGhostSpeed;
        playerSpeed = draftPlayerSpeed;
        ghostSpawnInterval = draftGhostSpawnSeconds * framesPerSecond;
        powerPelletDuration = draftPelletSeconds * framesPerSecond;
        powerUpDuration = draftPowerSeconds * framesPerSecond;
        noPowerMode = draftNoPowerMode;
        maxScreenCol = draftMazeWidth;
        maxScreenRow = draftMazeHeight;
        tunnelY = maxScreenRow / 2;
        updatePanelSize();
        screenState = STATE_MENU;
    }

    public void updatePanelSize() {
        screenWidth = tileSize * maxScreenCol;
        boardHeight = tileSize * maxScreenRow;
        screenHeight = boardHeight + hudHeight;
        setPreferredSize(new Dimension(tileSize * 25, tileSize * 31 + hudHeight));
        revalidate();

        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.pack();
        }
    }

    public void handleMenuKey(int keyCode) {
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_LEFT) {
            menuChoice = (menuChoice + 3) % 4;
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_RIGHT) {
            menuChoice = (menuChoice + 1) % 4;
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            if (menuChoice == 0) {
                startNewRun();
            } else if (menuChoice == 1) {
                openOptions();
            } else if (menuChoice == 2) {
                screenState = STATE_HALL_OF_FAME;
            } else {
                quitGame();
            }
        }
    }

    public void quitGame() {
        Window window = SwingUtilities.getWindowAncestor(this);

        if (window != null) {
            window.dispose();
        }

        System.exit(0);
    }

    public void handleOptionsKey(int keyCode) {
        int optionCount = 10;

        if (keyCode == KeyEvent.VK_UP) {
            optionChoice = (optionChoice + optionCount - 1) % optionCount;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            optionChoice = (optionChoice + 1) % optionCount;
        } else if (keyCode == KeyEvent.VK_LEFT) {
            changeOption(-1);
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            changeOption(1);
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            selectOption();
        }
    }

    public void changeOption(int direction) {
        if (optionChoice == 0) {
            draftUseWasdMovement = !draftUseWasdMovement;
        } else if (optionChoice == 1) {
            draftGhostSpeed = clampDouble(draftGhostSpeed + direction * 0.5, 1, 10);
        } else if (optionChoice == 2) {
            draftPlayerSpeed = clampDouble(draftPlayerSpeed + direction * 0.5, 1, 10);
        } else if (optionChoice == 3) {
            draftGhostSpawnSeconds = clampInt(draftGhostSpawnSeconds + direction, 1, 60);
        } else if (optionChoice == 4) {
            draftPelletSeconds = clampInt(draftPelletSeconds + direction, 1, 20);
        } else if (optionChoice == 5) {
            draftPowerSeconds = clampInt(draftPowerSeconds + direction, 1, 20);
        } else if (optionChoice == 6) {
            draftNoPowerMode = !draftNoPowerMode;
        } else if (optionChoice == 7) {
            draftMazeWidth = clampInt(draftMazeWidth + direction, 10, 50);
        } else if (optionChoice == 8) {
            draftMazeHeight = clampInt(draftMazeHeight + direction * 2, 11, 51);
            if (draftMazeHeight % 2 == 0) {
                draftMazeHeight += direction;
            }
            draftMazeHeight = clampInt(draftMazeHeight, 11, 51);
        } else {
            optionActionChoice = optionActionChoice == 0 ? 1 : 0;
        }
    }

    public void selectOption() {
        if (optionChoice < 9) {
            changeOption(1);
            return;
        }

        if (optionActionChoice == 0) {
            saveOptions();
        } else {
            screenState = STATE_MENU;
        }
    }

    public int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public String getPowerUpName(int powerUpType) {
        if (powerUpType == 0) {
            return "MAGNET";
        }
        if (powerUpType == 1) {
            return "SPIKE";
        }
        if (powerUpType == 2) {
            return "SPEED";
        }
        if (powerUpType == 3) {
            return "MULTI";
        }

        return "POWER";
    }

    public String getElapsedTimeText() {
        int totalSeconds = elapsedFrames / framesPerSecond;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public void drawActiveEffectTimers(Graphics2D g2) {
        int x = 12;
        int y = 42;

        if (powerMode) {
            g2.setColor(Color.CYAN);
            String text = "POWER " + getTimerSeconds(powerModeTimer);
            g2.drawString(text, x, y);
            x += g2.getFontMetrics().stringWidth(text) + 18;
        }

        g2.setColor(Color.GREEN);

        for (int i = 0; i < powerUpTimers.length; i++) {
            if (i == 4 || powerUpTimers[i] <= 0) {
                continue;
            }

            String text = getPowerUpName(i) + " " + getTimerSeconds(powerUpTimers[i]);
            g2.drawString(text, x, y);
            x += g2.getFontMetrics().stringWidth(text) + 18;

            if (x > getWidth() - 90) {
                return;
            }
        }
    }

    public int getTimerSeconds(int timer) {
        return (timer + framesPerSecond - 1) / framesPerSecond;
    }

    public void drawGhosts(Graphics2D g2) {
        for (Ghost ghost : ghosts) {
            drawGhost(g2, ghost);
        }
    }

    public void drawGhostDeathEffects(Graphics2D g2) {
        for (GhostDeathEffect effect : ghostDeathEffects) {
            BufferedImage sprite = effect.isShowingDeathFrame() ? ghostDeathSprite : ghostEyesSprite;
            int screenX = worldToScreenX(effect.pixelX);
            int screenY = worldToScreenY(effect.pixelY);

            g2.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        }
    }

    public void drawGhost(Graphics2D g2, Ghost ghost) {
        BufferedImage sprite = getGhostSprite(ghost);
        int screenX = worldToScreenX(ghost.pixelX);
        int screenY = worldToScreenY(ghost.pixelY);
        boolean flipped = (ghostAnimationCounter / animationDelay) % 2 == 1;

        if (flipped) {
            g2.drawImage(sprite, screenX + tileSize, screenY, -tileSize, tileSize, null);
        } else {
            g2.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        }
    }

    public BufferedImage getGhostSprite(Ghost ghost) {
        if (!powerMode) {
            return ghostSprites[ghost.type];
        }

        if (powerModeTimer <= powerPelletWarningTime && (ghostAnimationCounter / animationDelay) % 2 == 1) {
            return ghostSprites[5];
        }

        return ghostSprites[4];
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (screenState == STATE_MENU) {
            handleMenuKey(keyCode);
            return;
        }

        if (screenState == STATE_OPTIONS) {
            handleOptionsKey(keyCode);
            return;
        }

        if (screenState == STATE_HALL_OF_FAME) {
            screenState = STATE_MENU;
            return;
        }

        if (screenState == STATE_NAME_ENTRY) {
            handleNameEntryKey(keyCode);
            return;
        }

        if (keyCode == KeyEvent.VK_R) {
            resetGame();
            screenState = STATE_GAME;
            return;
        }

        if (keyCode == KeyEvent.VK_ESCAPE && !playerDead && !deathAnimationDone) {
            handleGameEscape();
            return;
        }

        if (keyCode == KeyEvent.VK_P && !playerDead && !deathAnimationDone) {
            paused = !paused;
            quitConfirmVisible = false;
            return;
        }

        if (playerDead) {
            return;
        }

        if (paused) {
            return;
        }

        if (isMoveUpKey(keyCode)) {
            gameStarted = true;
            nextDirectionX = 0;
            nextDirectionY = 1;
        } else if (isMoveDownKey(keyCode)) {
            gameStarted = true;
            nextDirectionX = 0;
            nextDirectionY = -1;
        } else if (isMoveLeftKey(keyCode)) {
            gameStarted = true;
            nextDirectionX = -1;
            nextDirectionY = 0;
        } else if (isMoveRightKey(keyCode)) {
            gameStarted = true;
            nextDirectionX = 1;
            nextDirectionY = 0;
        }
    }

    public void handleGameEscape() {
        if (!paused) {
            paused = true;
            quitConfirmVisible = false;
            return;
        }

        if (!quitConfirmVisible) {
            quitConfirmVisible = true;
            return;
        }

        resetGame();
        screenState = STATE_MENU;
    }

    public boolean isMoveUpKey(int keyCode) {
        return useWasdMovement ? keyCode == KeyEvent.VK_W : keyCode == KeyEvent.VK_UP;
    }

    public boolean isMoveDownKey(int keyCode) {
        return useWasdMovement ? keyCode == KeyEvent.VK_S : keyCode == KeyEvent.VK_DOWN;
    }

    public boolean isMoveLeftKey(int keyCode) {
        return useWasdMovement ? keyCode == KeyEvent.VK_A : keyCode == KeyEvent.VK_LEFT;
    }

    public boolean isMoveRightKey(int keyCode) {
        return useWasdMovement ? keyCode == KeyEvent.VK_D : keyCode == KeyEvent.VK_RIGHT;
    }

    public void handleNameEntryKey(int keyCode) {
        if (keyCode == KeyEvent.VK_LEFT) {
            nameEntryIndex = (nameEntryIndex + nameEntry.length - 1) % nameEntry.length;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            nameEntryIndex = (nameEntryIndex + 1) % nameEntry.length;
        } else if (keyCode == KeyEvent.VK_UP) {
            nameEntry[nameEntryIndex] = nameEntry[nameEntryIndex] == 'Z' ? 'A' : (char) (nameEntry[nameEntryIndex] + 1);
        } else if (keyCode == KeyEvent.VK_DOWN) {
            nameEntry[nameEntryIndex] = nameEntry[nameEntryIndex] == 'A' ? 'Z' : (char) (nameEntry[nameEntryIndex] - 1);
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            saveNewHighScore();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

}
