package game;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.awt.FontMetrics;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Stroke;
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
    boolean[][] burnedWalls;
    int[][] blockTextureIndexes;
    int[][] wallTextureIndexes;
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
    final int powerUpDropSmallDots = 25;
    int powerUpDuration = framesPerSecond * 5; // power timer
    final int spikeTrapCount = 20;
    final int ghostDeathFrameTime = 8;
    final double ghostEyesSpeed = 4.0;
    final int powerBombType = 5;
    final int powerLaserType = 6;
    final int powerCloneType = 7;
    final int powerFireType = 8;
    final int powerUpTypeCount = 9;
    final int ghostCloneType = 8;
    final int ghostLaserType = 9;
    final int ghostBonusType = 10;
    final int ghostSpeedType = 11;
    final int ghostFireType = 12;
    final int ghostMagnetType = 13;
    final int ghostBombType = 14;
    final int ghostSpikeType = 15;
    final int bombRadiusTiles = 3;
    final int bombExplosionFrameTime = 18;
    final int ghostSpeedRecoverTime = framesPerSecond * 2; // ghost_11 pause after it dashes into a wall.
    final int ghostLaserChargeTime = framesPerSecond * 5; // ghost_9 still charge time before it starts warning.
    final int ghostLaserWarningTime = framesPerSecond * 2; // ghost_9 flashing warning time before it fires.
    final int ghostLaserFireTime = framesPerSecond * 5; // ghost_9 active laser duration before it recharges.
    final int ghostBombFuseTime = framesPerSecond; // ghost_8 stops this long before exploding.
    final int ghostBombTriggerRadiusTiles = 1; // 1 tile around ghost = 3x3 danger zone.
    final double ghostBombStartSpeed = 0.5;
    final double ghostBombSpeedGainPerSecond = 0.1;
    final double ghostBombMaxSpeedOverPlayer = 0.5;
    final int cloneCount = 3;
    final double cloneSpeed = 2.5;
    final int clonePelletScore = 2;
    final int afterImageSpawnInterval = 4;
    final int fireTrailDuration = framesPerSecond;
    final int ghostSpikeTrapCount = 10;
    final int decalAshType = 0;
    final int decalBloodType = 1;
    final int laserWidth = 4;
    final int blockTextureFileCount = 7;
    final int wallTextureFileCount = 7;
    int specialGhostChancePercent = 10;
    final int minGhostSpawnInterval = framesPerSecond * 2;
    final double speedIncreasePerLevel = 0.2;
    final double maxPlayerSpeed = 10.0;
    final double maxGhostSpeed = 9.0;
    final int maxSmallDotScore = 10;

    BufferedImage[] pacSprites = new BufferedImage[5];
    BufferedImage[] pacBombSprites = new BufferedImage[2];
    BufferedImage[] pacCloneSprites = new BufferedImage[2];
    BufferedImage[] pacCloneBombSprites = new BufferedImage[2];
    BufferedImage[] ghostSprites = new BufferedImage[6];
    BufferedImage ghostCloneSprite;
    BufferedImage[] ghostBombSprites = new BufferedImage[3];
    BufferedImage ghostLaserSprite;
    BufferedImage ghostBonusSprite;
    BufferedImage ghostSpeedSprite;
    BufferedImage ghostFireSprite;
    BufferedImage[] ghostLaserChargeSprites = new BufferedImage[2];
    BufferedImage[] ghostMagnetSprites = new BufferedImage[2];
    BufferedImage[] ghostSpikeSprites = new BufferedImage[2];
    BufferedImage ghostDeathSprite;
    BufferedImage ghostEyesSprite;
    BufferedImage[] outSprites = new BufferedImage[4];
    BufferedImage[] fruitSprites = new BufferedImage[4];
    BufferedImage[] powerUpSprites = new BufferedImage[powerUpTypeCount];
    BufferedImage[] spikeSprites = new BufferedImage[2];
    BufferedImage[] decalSprites = new BufferedImage[2];
    BufferedImage[] warnSprites = new BufferedImage[2];
    BufferedImage[] fireSprites = new BufferedImage[14];
    BufferedImage[] ghostFireSprites = new BufferedImage[14];
    BufferedImage[] blockTextureSprites = new BufferedImage[blockTextureFileCount];
    int blockTextureCount = 0;
    BufferedImage[] wallTextureSprites = new BufferedImage[wallTextureFileCount];
    int wallTextureCount = 0;
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
    static final int STATE_ALMANAC = 5;
    int screenState = STATE_MENU;
    int menuChoice = 0;
    int optionChoice = 0;
    int optionActionChoice = 0;
    int almanacIndex = 0;
    ArrayList<String> almanacTitles = new ArrayList<>();
    ArrayList<String> almanacBodies = new ArrayList<>();
    final File scoreFile = new File("playerscore.sav");
    String[] highScoreNames = { "DEV", "PRO", "NUB" };
    int[] highScoreValues = { 999999, 50000, 1000 };
    char[] nameEntry = { 'A', 'A', 'A' };
    int nameEntryIndex = 0;
    int pendingFinalScore = 0;
    boolean finalScoreHandled = false;

    double draftGhostSpeed = ghostSpeed;
    double draftPlayerSpeed = playerSpeed;
    int draftGhostSpawnSeconds = 10;
    int draftPelletSeconds = 5;
    int draftPowerSeconds = 5;
    int draftSpecialGhostChancePercent = specialGhostChancePercent;
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
    int bombExplosionTimer = 0;
    double bombExplosionMinX = 0;
    double bombExplosionMinY = 0;
    double bombExplosionMaxX = 0;
    double bombExplosionMaxY = 0;
    int ghostEatScore = 200;
    int boardGhostDelayTimer = 0;
    int carriedGhostSpawnTimer = 0;
    int warningPortalX = -1;
    int warningPortalY = -1;
    int smallDotsTowardPowerUp = 0;
    int[] powerUpTimers = new int[powerUpTypeCount];
    int[] collectedFruits = new int[4];
    // Larger value = slower death animation.
    final int deathFrameDelay = 8;
    boolean boardClear = false;

    ArrayList<Ghost> ghosts = new ArrayList<>();
    ArrayList<GhostTransferData> pendingCarriedGhosts = new ArrayList<>();
    ArrayList<PacClone> pacClones = new ArrayList<>();
    ArrayList<FireTrail> fireTrails = new ArrayList<>();
    ArrayList<AfterImage> afterImages = new ArrayList<>();
    ArrayList<Fruit> fruits = new ArrayList<>();
    ArrayList<PowerUp> powerUps = new ArrayList<>();
    ArrayList<SpikeTrap> spikeTraps = new ArrayList<>();
    ArrayList<GhostSpikeTrap> ghostSpikeTraps = new ArrayList<>();
    ArrayList<GhostDeathEffect> ghostDeathEffects = new ArrayList<>();
    ArrayList<VisualDecal> visualDecals = new ArrayList<>();
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
        loadAlmanacEntries();
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
                    updatePacClones();
                    updateFireTrails();
                    updateGhosts();
                    checkLaserGhostHits();
                    checkPacCloneLaserHits();
                    checkGhostLaserHits();
                    checkFireTrailCollisions();
                    checkPacCloneGhostCollisions();
                }

                updateGhostDeathEffects();
                updateAfterImages();

                if (!playerDead) {
                    checkGhostSpikeTrapCollision();
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

        if (screenState == STATE_ALMANAC) {
            drawAlmanac(g2);
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
        drawFireTrails(boardGraphics);
        drawGhostSpikeTraps(boardGraphics);
        drawVisualDecals(boardGraphics);
        drawAfterImages(boardGraphics);
        drawExitMarkers(boardGraphics);
        drawSpawnWarning(boardGraphics);
        drawGhosts(boardGraphics);
        drawGhostDeathEffects(boardGraphics);
        drawGhostLasers(boardGraphics);
        drawLaser(boardGraphics);
        drawPacCloneLasers(boardGraphics);
        drawBombExplosion(boardGraphics);
        drawPacClones(boardGraphics);
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
            if (x == getTunnelX()) {
                continue;
            }

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
        Color originalColor = g2.getColor();

        if (isWallAffectedByBombExplosion(x, y)) {
            g2.setColor(originalColor.darker());
        }

        g2.fillRect(screenX, screenY, tileSize, tileSize);
        drawWallTexture(g2, x, y, screenX, screenY);
        g2.setColor(originalColor);
    }

    public void drawWallTexture(Graphics2D g2, int tileX, int tileY, int screenX, int screenY) {
        BufferedImage[] textureSprites = isOuterWallTile(tileX, tileY) ? wallTextureSprites : blockTextureSprites;
        int textureCount = isOuterWallTile(tileX, tileY) ? wallTextureCount : blockTextureCount;
        int[][] textureIndexes = isOuterWallTile(tileX, tileY) ? wallTextureIndexes : blockTextureIndexes;

        if (textureCount <= 0 || textureIndexes == null) {
            return;
        }

        int textureIndex = textureIndexes[tileX][tileY];

        if (textureIndex < 0 || textureIndex >= textureCount) {
            return;
        }

        g2.drawImage(textureSprites[textureIndex], screenX, screenY, tileSize, tileSize, null);
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
        maze[getTunnelX()][1] = false;
        maze[getTunnelX()][maxScreenRow - 2] = false;
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
        return (y == tunnelY && (x == 1 || x == maxScreenCol - 2))
                || (x == getTunnelX() && (y == 1 || y == maxScreenRow - 2));
    }

    public boolean isPortalTile(int x, int y) {
        return (y == tunnelY && (x == 0 || x == maxScreenCol - 1))
                || (x == getTunnelX() && (y == 0 || y == maxScreenRow - 1));
    }

    public int getTunnelX() {
        return maxScreenCol / 2;
    }

    public int[][] getPortalTiles() {
        return new int[][] {
            { 0, tunnelY },
            { maxScreenCol - 1, tunnelY },
            { getTunnelX(), 0 },
            { getTunnelX(), maxScreenRow - 1 }
        };
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
            pacBombSprites[0] = loadOptionalSprite("res/sprite/pacbomb_0.png", pacSprites[0]);
            pacBombSprites[1] = loadOptionalSprite("res/sprite/pacbomb_1.png", pacSprites[1]);
            pacCloneSprites[0] = ImageIO.read(new File("res/sprite/pacclone_0.png"));
            pacCloneSprites[1] = ImageIO.read(new File("res/sprite/pacclone_1.png"));
            pacCloneBombSprites[0] = loadOptionalSprite("res/sprite/clonebomb_0.png", pacCloneSprites[0]);
            pacCloneBombSprites[1] = loadOptionalSprite("res/sprite/clonebomb_1.png", pacCloneSprites[1]);
            ghostSprites[0] = ImageIO.read(new File("res/sprite/ghost_0.png"));
            ghostSprites[1] = ImageIO.read(new File("res/sprite/ghost_1.png"));
            ghostSprites[2] = ImageIO.read(new File("res/sprite/ghost_2.png"));
            ghostSprites[3] = ImageIO.read(new File("res/sprite/ghost_3.png"));
            ghostSprites[4] = ImageIO.read(new File("res/sprite/ghost_4.png"));
            ghostSprites[5] = ImageIO.read(new File("res/sprite/ghost_5.png"));
            ghostCloneSprite = ImageIO.read(new File("res/sprite/ghost_8.png"));
            ghostBombSprites[0] = ImageIO.read(new File("res/sprite/ghostbomb_0.png"));
            ghostBombSprites[1] = ImageIO.read(new File("res/sprite/ghostbomb_1.png"));
            ghostBombSprites[2] = ImageIO.read(new File("res/sprite/ghostbomb_2.png"));
            ghostLaserSprite = ImageIO.read(new File("res/sprite/ghost_9.png"));
            ghostBonusSprite = ImageIO.read(new File("res/sprite/ghost_10.png"));
            ghostSpeedSprite = ImageIO.read(new File("res/sprite/ghost_11.png"));
            ghostFireSprite = ImageIO.read(new File("res/sprite/ghost_12.png"));
            ghostLaserChargeSprites[0] = ImageIO.read(new File("res/sprite/ghostlazer_0.png"));
            ghostLaserChargeSprites[1] = ImageIO.read(new File("res/sprite/ghostlazer_1.png"));
            ghostMagnetSprites[0] = ImageIO.read(new File("res/sprite/ghostmag_0.png"));
            ghostMagnetSprites[1] = ImageIO.read(new File("res/sprite/ghostmag_1.png"));
            ghostSpikeSprites[0] = ImageIO.read(new File("res/sprite/ghostspike_0.png"));
            ghostSpikeSprites[1] = ImageIO.read(new File("res/sprite/ghostspike_1.png"));
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
            powerUpSprites[5] = ImageIO.read(new File("res/sprite/pow_5.png"));
            powerUpSprites[6] = ImageIO.read(new File("res/sprite/pow_6.png"));
            powerUpSprites[7] = ImageIO.read(new File("res/sprite/pow_7.png"));
            powerUpSprites[8] = ImageIO.read(new File("res/sprite/pow_8.png"));
            spikeSprites[0] = ImageIO.read(new File("res/sprite/spike_0.png"));
            spikeSprites[1] = ImageIO.read(new File("res/sprite/spike_1.png"));
            decalSprites[decalAshType] = ImageIO.read(new File("res/sprite/ash.png"));
            decalSprites[decalBloodType] = ImageIO.read(new File("res/sprite/blood.png"));
            loadFireSprites("fire_", fireSprites);
            loadFireSprites("ghostfire_", ghostFireSprites);
            loadWallTextureSprites();
            overScreen = ImageIO.read(new File("res/sprite/overscreen.png"));
            pauseScreen = ImageIO.read(new File("res/sprite/pausescreen.png"));
            menuScreen = ImageIO.read(new File("res/sprite/menuscreen_0.png"));
            blankMenuScreen = ImageIO.read(new File("res/sprite/menuscreen_1.png"));
        } catch (IOException e) {
            throw new RuntimeException("Could not load sprite images from res/sprite.", e);
        }
    }

    public void loadWallTextureSprites() throws IOException {
        blockTextureCount = loadTextureSprites("block_", blockTextureSprites);
        wallTextureCount = loadTextureSprites("wall_", wallTextureSprites);
    }

    public void loadFireSprites(String prefix, BufferedImage[] sprites) throws IOException {
        for (int i = 0; i < sprites.length; i++) {
            sprites[i] = ImageIO.read(new File("res/sprite/" + prefix + i + ".png"));
        }
    }

    public int loadTextureSprites(String prefix, BufferedImage[] sprites) throws IOException {
        int textureCount = 0;

        for (int i = 0; i < sprites.length; i++) {
            File textureFile = new File("res/sprite/" + prefix + i + ".png");

            if (textureFile.exists()) {
                sprites[textureCount] = ImageIO.read(textureFile);
                textureCount++;
            }
        }

        return textureCount;
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

    public void loadAlmanacEntries() {
        almanacTitles.clear();
        almanacBodies.clear();

        File almanacFile = new File("res/almanac/almanac entry.txt");

        if (!almanacFile.exists()) {
            almanacTitles.add("Almanac");
            almanacBodies.add("No almanac entries found.");
            return;
        }

        try {
            String content = Files.readString(almanacFile.toPath(), StandardCharsets.UTF_8);
            String[] entries = content.split("(?m)^---\\s*$");

            for (String entry : entries) {
                String cleanedEntry = cleanAlmanacText(entry.trim());

                if (cleanedEntry.isEmpty()) {
                    continue;
                }

                String[] lines = cleanedEntry.split("\\R", 2);
                almanacTitles.add(lines[0].trim());
                almanacBodies.add(lines.length > 1 ? lines[1].trim() : "");
            }
        } catch (IOException e) {
            almanacTitles.add("Almanac");
            almanacBodies.add("Could not load almanac entries.");
        }
    }

    public String cleanAlmanacText(String text) {
        return text
                .replace("Ã—", "x")
                .replace("â€”", "-")
                .replace("â€“", "-")
                .replace("â€œ", "\"")
                .replace("â€�", "\"")
                .replace("â€˜", "'")
                .replace("â€™", "'");
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
        clearBombExplosionEffect();
        ghostEatScore = 200;
        pendingCarriedGhosts.clear();
        boardGhostDelayTimer = 0;
        carriedGhostSpawnTimer = 0;
        clearWarningPortal();
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
        resetBoardVisuals();
        fruits.clear();
        powerUps.clear();
        spikeTraps.clear();
        ghostSpikeTraps.clear();
        pacClones.clear();
        fireTrails.clear();
        afterImages.clear();
        ghostDeathEffects.clear();
        spawnGhosts();
        eatDotAtPlayer();
    }

    public void spawnGhosts() {
        ghosts.clear();
        int[][] cornerSpawns = getCornerSpawnTargets();

        for (int i = 0; i < cornerSpawns.length; i++) {
            int[] spawnTile = findGhostSpawnTileNear(cornerSpawns[i][0], cornerSpawns[i][1]);
            int ghostType = getNaturalSpawnGhostType();

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
        chooseWarningPortal();
        spawnGhostThroughPortal(warningPortalX, warningPortalY, null);
    }

    public void spawnGhostThroughPortal(int tileX) {
        spawnGhostThroughPortal(tileX, tunnelY, null);
    }

    public void spawnGhostThroughPortal(int tileX, GhostTransferData transferData) {
        spawnGhostThroughPortal(tileX, tunnelY, transferData);
    }

    public void spawnGhostThroughPortal(int tileX, int tileY, GhostTransferData transferData) {
        if (!isPortalTile(tileX, tileY)) {
            int[] portal = getRandomPortalTile();
            tileX = portal[0];
            tileY = portal[1];
        }

        int directionX = getPortalInwardDirectionX(tileX, tileY);
        int directionY = getPortalInwardDirectionY(tileX, tileY);
        int ghostType = transferData == null ? getNaturalSpawnGhostType() : transferData.type;
        Ghost ghost = new Ghost(ghostType, tileX, tileY, tileSize);

        ghost.speedOffset = getRandomGhostSpeedOffset();
        ghost.directionX = directionX;
        ghost.directionY = directionY;
        ghost.targetPixelX = (tileX + directionX) * tileSize;
        ghost.targetPixelY = (tileY + directionY) * tileSize;
        ghosts.add(ghost);
        clearWarningPortal();
    }

    public ArrayList<GhostTransferData> getGhostTransferData() {
        ArrayList<GhostTransferData> transferData = new ArrayList<>();

        for (Ghost ghost : ghosts) {
            transferData.add(new GhostTransferData(ghost.type));
        }

        return transferData;
    }

    public int getNaturalSpawnGhostType() {
        if (random.nextDouble() >= getSpecialGhostSpawnChance()) {
            return random.nextInt(4);
        }

        int[] specialGhostTypes = {
            ghostCloneType,
            ghostBombType,
            ghostLaserType,
            ghostBonusType,
            ghostSpeedType,
            ghostMagnetType,
            ghostFireType
        };

        return specialGhostTypes[random.nextInt(specialGhostTypes.length)];
    }

    public double getSpecialGhostSpawnChance() {
        if (specialGhostChancePercent > 50) {
            return specialGhostChancePercent / 100.0;
        }

        int levelBonusPercent = Math.max(0, level - 1) * 2;
        int scaledChancePercent = Math.min(50, specialGhostChancePercent + levelBonusPercent);
        return scaledChancePercent / 100.0;
    }

    public int getWarningPortalX() {
        chooseWarningPortal();

        return warningPortalX;
    }

    public int getWarningPortalY() {
        chooseWarningPortal();

        return warningPortalY;
    }

    public void chooseWarningPortal() {
        if (warningPortalX != -1 && warningPortalY != -1) {
            return;
        }

        int[] portal = getRandomPortalTile();
        warningPortalX = portal[0];
        warningPortalY = portal[1];
    }

    public void clearWarningPortal() {
        warningPortalX = -1;
        warningPortalY = -1;
    }

    public int[] getRandomPortalTile() {
        int[][] portals = getPortalTiles();
        return portals[random.nextInt(portals.length)];
    }

    public int getPortalInwardDirectionX(int tileX, int tileY) {
        if (tileX == 0) {
            return 1;
        }
        if (tileX == maxScreenCol - 1) {
            return -1;
        }

        return 0;
    }

    public int getPortalInwardDirectionY(int tileX, int tileY) {
        if (tileY == 0) {
            return 1;
        }
        if (tileY == maxScreenRow - 1) {
            return -1;
        }

        return 0;
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
        updateBombExplosionEffect();
        updateSpikeTraps();
        updateGhostSpikeTraps();

        if (!boardClear) {
            updateBoardGhostSpawns();

            if (boardGhostDelayTimer == 0 && pendingCarriedGhosts.isEmpty()) {
                ghostSpawnTimer++;
            }

            if (boardGhostDelayTimer == 0 && pendingCarriedGhosts.isEmpty()
                    && ghostSpawnTimer >= getLevelGhostSpawnInterval() - ghostSpawnWarningTime) {
                chooseWarningPortal();
            }

            if (boardGhostDelayTimer == 0 && pendingCarriedGhosts.isEmpty()
                    && ghostSpawnTimer >= getLevelGhostSpawnInterval()) {
                ghostSpawnTimer = 0;
                spawnGhostThroughPortal(warningPortalX, warningPortalY, null);
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

                if (i == powerBombType && powerUpTimers[i] == 0) {
                    triggerBombExplosion();
                } else if (i == powerCloneType && powerUpTimers[i] == 0) {
                    turnPacClonesToAsh();
                }
            }
        }
    }

    public void updateBombExplosionEffect() {
        if (bombExplosionTimer > 0) {
            bombExplosionTimer--;
        }
    }

    public void clearBombExplosionEffect() {
        bombExplosionTimer = 0;
        bombExplosionMinX = 0;
        bombExplosionMinY = 0;
        bombExplosionMaxX = 0;
        bombExplosionMaxY = 0;
    }

    public void updateSpikeTraps() {
        for (int i = spikeTraps.size() - 1; i >= 0; i--) {
            SpikeTrap spikeTrap = spikeTraps.get(i);
            spikeTrap.timer--;

            if (spikeTrap.timer <= 0) {
                if (spikeTrap.used) {
                    addVisualDecal(decalBloodType, spikeTrap.tileX * tileSize, spikeTrap.tileY * tileSize);
                }
                spikeTraps.remove(i);
            }
        }
    }

    public void updateGhostSpikeTraps() {
        for (int i = ghostSpikeTraps.size() - 1; i >= 0; i--) {
            GhostSpikeTrap spikeTrap = ghostSpikeTraps.get(i);
            spikeTrap.timer--;

            if (spikeTrap.timer <= 0) {
                ghostSpikeTraps.remove(i);
            }
        }
    }

    public void updatePacClones() {
        for (PacClone clone : pacClones) {
            if (clone.pixelX == clone.targetPixelX && clone.pixelY == clone.targetPixelY) {
                choosePacCloneTarget(clone);
            }

            if (shouldSpawnAfterImage() && isPowerUpActive(2)
                    && (clone.pixelX != clone.targetPixelX || clone.pixelY != clone.targetPixelY)) {
                addAfterImage(getActivePacCloneSprite(), clone.pixelX, clone.pixelY,
                        getDirectionAngle(clone.directionX, clone.directionY), false);
            }

            clone.pixelX = moveValueToward(clone.pixelX, clone.targetPixelX, getPacCloneSpeed());
            clone.pixelY = moveValueToward(clone.pixelY, clone.targetPixelY, getPacCloneSpeed());

            if (clone.pixelX == clone.targetPixelX && clone.pixelY == clone.targetPixelY) {
                dropPacCloneFireTrail(clone);
                eatPelletAtPacClone(clone);
            }
        }
    }

    public double getPacCloneSpeed() {
        return isPowerUpActive(2) ? cloneSpeed + 1.0 : cloneSpeed;
    }

    public void updateFireTrails() {
        for (int i = fireTrails.size() - 1; i >= 0; i--) {
            FireTrail fireTrail = fireTrails.get(i);
            fireTrail.age++;
            fireTrail.timer--;

            if (fireTrail.timer <= 0) {
                fireTrails.remove(i);
            }
        }
    }

    public void updateAfterImages() {
        for (int i = afterImages.size() - 1; i >= 0; i--) {
            AfterImage afterImage = afterImages.get(i);
            afterImage.alpha -= 10;

            if (afterImage.alpha <= 0) {
                afterImages.remove(i);
            }
        }
    }

    public void addAfterImage(BufferedImage sprite, double pixelX, double pixelY, double angle, boolean flipped) {
        if (sprite == null) {
            return;
        }

        afterImages.add(new AfterImage(sprite, pixelX, pixelY, angle, flipped));
    }

    public boolean shouldSpawnAfterImage() {
        return elapsedFrames % afterImageSpawnInterval == 0;
    }

    public void choosePacCloneTarget(PacClone clone) {
        int tileX = (int) (clone.pixelX / tileSize);
        int tileY = (int) (clone.pixelY / tileSize);

        if (boardClear) {
            clone.path.clear();
            choosePacCloneRandomTarget(clone, tileX, tileY);
            return;
        }

        if (clone.path.isEmpty()) {
            int[] pelletTile = findNearestPelletTile(tileX, tileY);

            if (pelletTile == null) {
                choosePacCloneRandomTarget(clone, tileX, tileY);
                return;
            }

            clone.path = findAStarPath(tileX, tileY, pelletTile[0], pelletTile[1]);
        }

        if (clone.path.isEmpty()) {
            return;
        }

        int[] nextTile = clone.path.remove(0);
        clone.directionX = nextTile[0] - tileX;
        clone.directionY = nextTile[1] - tileY;
        clone.targetPixelX = nextTile[0] * tileSize;
        clone.targetPixelY = nextTile[1] * tileSize;
    }

    public void choosePacCloneRandomTarget(PacClone clone, int tileX, int tileY) {
        int[] direction = chooseRandomWallBounceDirection(clone.directionX, clone.directionY, tileX, tileY);

        if (direction == null) {
            clone.directionX = 0;
            clone.directionY = 0;
            return;
        }

        clone.directionX = direction[0];
        clone.directionY = direction[1];
        clone.targetPixelX = (tileX + clone.directionX) * tileSize;
        clone.targetPixelY = (tileY + clone.directionY) * tileSize;
    }

    public int[] findNearestPelletTile(int startX, int startY) {
        int bestDistance = Integer.MAX_VALUE;
        ArrayList<int[]> bestTiles = new ArrayList<>();

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                if (!dots[x][y] && !bigDots[x][y]) {
                    continue;
                }

                ArrayList<int[]> path = findAStarPath(startX, startY, x, y);

                if (path.isEmpty() && (startX != x || startY != y)) {
                    continue;
                }

                int distance = path.size();

                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestTiles.clear();
                    bestTiles.add(new int[] { x, y });
                } else if (distance == bestDistance) {
                    bestTiles.add(new int[] { x, y });
                }
            }
        }

        return bestTiles.isEmpty() ? null : bestTiles.get(random.nextInt(bestTiles.size()));
    }

    public void updateBoardGhostSpawns() {
        if (boardGhostDelayTimer > 0) {
            boardGhostDelayTimer--;
            return;
        }

        if (pendingCarriedGhosts.isEmpty()) {
            return;
        }

        carriedGhostSpawnTimer++;

        if (carriedGhostSpawnTimer >= carriedGhostSpawnInterval - ghostSpawnWarningTime) {
            chooseWarningPortal();
        }

        if (carriedGhostSpawnTimer >= carriedGhostSpawnInterval) {
            carriedGhostSpawnTimer = 0;
            GhostTransferData transferData = pendingCarriedGhosts.remove(0);
            spawnGhostThroughPortal(warningPortalX, warningPortalY, transferData);
        }
    }

    public boolean isPlayerMoving() {
        return playerPixelX != targetPixelX || playerPixelY != targetPixelY;
    }

    public int getLevelGhostSpawnInterval() {
        int completedTwoLevelSets = Math.max(0, level - 1) / 2;
        return Math.max(minGhostSpawnInterval, ghostSpawnInterval - completedTwoLevelSets * framesPerSecond);
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
        if (shouldSpawnAfterImage() && isPowerUpActive(2)) {
            addAfterImage(getActivePlayerSprite(), playerPixelX, playerPixelY, getPlayerAngle(), false);
        }

        playerPixelX = moveValueToward(playerPixelX, targetPixelX, getPlayerSpeed());
        playerPixelY = moveValueToward(playerPixelY, targetPixelY, getPlayerSpeed());

        if (!isPlayerMoving()) {
            handlePortalWrap();
            updateLastPlayerTile();
            dropPlayerFireTrail();
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
        double adjustedSpeed = getLevelPlayerSpeed();

        if (isPowerUpActive(2)) {
            adjustedSpeed += 1.0;
        }

        return Math.min(maxPlayerSpeed, adjustedSpeed);
    }

    public double getRandomGhostSpeedOffset() {
        return random.nextDouble() - 0.5;
    }

    public double getLevelPlayerSpeed() {
        return Math.min(maxPlayerSpeed, playerSpeed + Math.max(0, level - 1) * speedIncreasePerLevel);
    }

    public double getLevelGhostSpeed() {
        return Math.min(maxGhostSpeed, ghostSpeed + Math.max(0, level - 1) * speedIncreasePerLevel);
    }

    public double getGhostSpeed(Ghost ghost) {
        if (ghost.type == ghostBombType) {
            if (ghost.fuseTimer > 0) {
                return 0.0;
            }

            return Math.min(
                    getPlayerSpeed() + ghostBombMaxSpeedOverPlayer,
                    ghostBombStartSpeed + (ghost.speedRampTimer / (double) framesPerSecond) * ghostBombSpeedGainPerSecond);
        }
        if (ghost.type == ghostMagnetType) {
            return 1.0;
        }
        if (ghost.type == ghostLaserType && !ghost.laserActive) {
            return 0.0;
        }
        if (ghost.type == ghostSpeedType && ghost.speedDashActive) {
            return getPlayerSpeed() + 1.5;
        }

        double adjustedSpeed = Math.min(maxGhostSpeed, Math.max(0.1, getLevelGhostSpeed() + ghost.speedOffset));
        return isPowerUpActive(2) ? Math.max(0.1, adjustedSpeed * 0.35) : adjustedSpeed;
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

        if (!isPortalTile(tileX, tileY)) {
            return;
        }

        if (boardClear) {
            advanceToNextBoard(tileX, tileY);
            return;
        }

        if (tileX == 0) {
            playerPixelX = (maxScreenCol - 1) * tileSize;
            targetPixelX = playerPixelX;
        } else if (tileX == maxScreenCol - 1) {
            playerPixelX = 0;
            targetPixelX = playerPixelX;
        } else if (tileY == 0) {
            playerPixelY = (maxScreenRow - 1) * tileSize;
            targetPixelY = playerPixelY;
        } else if (tileY == maxScreenRow - 1) {
            playerPixelY = 0;
            targetPixelY = playerPixelY;
        }
    }

    public void advanceToNextBoard(int exitTileX, int exitTileY) {
        ArrayList<GhostTransferData> carriedGhosts = getGhostTransferData();

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
        clearBombExplosionEffect();
        pendingCarriedGhosts.clear();
        pendingCarriedGhosts.addAll(carriedGhosts);
        boardGhostDelayTimer = boardGhostDelay;
        carriedGhostSpawnTimer = 0;
        clearWarningPortal();
        animationCounter = 0;
        ghostAnimationCounter = 0;

        generateMaze();
        generateDots();
        resetBoardVisuals();
        fruits.clear();
        powerUps.clear();
        spikeTraps.clear();
        ghostSpikeTraps.clear();
        pacClones.clear();
        fireTrails.clear();
        afterImages.clear();
        ghostDeathEffects.clear();
        ghosts.clear();

        if (exitTileX == 0) {
            playerPixelX = (maxScreenCol - 2) * tileSize;
            playerPixelY = tunnelY * tileSize;
            lastDirectionX = -1;
            lastDirectionY = 0;
        } else if (exitTileX == maxScreenCol - 1) {
            playerPixelX = tileSize;
            playerPixelY = tunnelY * tileSize;
            lastDirectionX = 1;
            lastDirectionY = 0;
        } else if (exitTileY == 0) {
            playerPixelX = getTunnelX() * tileSize;
            playerPixelY = (maxScreenRow - 2) * tileSize;
            lastDirectionX = 0;
            lastDirectionY = -1;
        } else {
            playerPixelX = getTunnelX() * tileSize;
            playerPixelY = tileSize;
            lastDirectionX = 0;
            lastDirectionY = 1;
        }

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

    public void resetBoardVisuals() {
        burnedWalls = new boolean[maxScreenCol][maxScreenRow];
        randomizeWallTextures();
        visualDecals.clear();
        clearBombExplosionEffect();
    }

    public void randomizeWallTextures() {
        blockTextureIndexes = new int[maxScreenCol][maxScreenRow];
        wallTextureIndexes = new int[maxScreenCol][maxScreenRow];

        for (int x = 0; x < maxScreenCol; x++) {
            for (int y = 0; y < maxScreenRow; y++) {
                blockTextureIndexes[x][y] = blockTextureCount > 0 ? random.nextInt(blockTextureCount) : -1;
                wallTextureIndexes[x][y] = wallTextureCount > 0 ? random.nextInt(wallTextureCount) : -1;
            }
        }
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
            clearWarningPortal();
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
        int score = getLevelSmallDotScore();
        return isPowerUpActive(3) ? score * 5 : score;
    }

    public int getBigDotScore() {
        return isPowerUpActive(3) ? 50 : 10;
    }

    public int getLevelSmallDotScore() {
        return Math.min(maxSmallDotScore, 1 + Math.max(0, level - 1) / 3);
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
                    collectSmallPelletAt(x, y);
                }
            }
        }
    }

    public void collectSmallPelletAt(int tileX, int tileY) {
        if (dots[tileX][tileY]) {
            dots[tileX][tileY] = false;
            addScore(getSmallDotScore());
        }
    }

    public void eatPelletAtPacClone(PacClone clone) {
        int tileX = (int) (clone.pixelX / tileSize);
        int tileY = (int) (clone.pixelY / tileSize);

        if (dots[tileX][tileY]) {
            dots[tileX][tileY] = false;
            clone.path.clear();
            addScore(getClonePelletScore());
        } else if (bigDots[tileX][tileY]) {
            bigDots[tileX][tileY] = false;
            clone.path.clear();
            addScore(getClonePelletScore());
        }

        if (isPowerUpActive(0)) {
            collectPacCloneMagnetPellets(tileX, tileY);
        }

        if (!boardClear && areAllPelletsEaten()) {
            boardClear = true;
            clearWarningPortal();
        }
    }

    public void collectPacCloneMagnetPellets(int centerX, int centerY) {
        for (int x = centerX - 2; x <= centerX + 2; x++) {
            for (int y = centerY - 2; y <= centerY + 2; y++) {
                if (x > 0 && x < maxScreenCol - 1 && y > 0 && y < maxScreenRow - 1 && dots[x][y]) {
                    dots[x][y] = false;
                    addScore(getClonePelletScore());
                }
            }
        }
    }

    public int getClonePelletScore() {
        return isPowerUpActive(3) ? clonePelletScore * 5 : clonePelletScore;
    }

    public void spawnPacClones() {
        for (int i = 0; i < cloneCount; i++) {
            PacClone clone = new PacClone(playerPixelX, playerPixelY);
            pacClones.add(clone);
        }
    }

    public void turnPacClonesToAsh() {
        for (PacClone clone : pacClones) {
            addVisualDecal(decalAshType, clone.pixelX, clone.pixelY);
        }

        pacClones.clear();
    }

    public void eatPelletsAtGhost(Ghost ghost) {
        if (ghost.type != ghostBonusType && ghost.type != ghostMagnetType) {
            return;
        }

        int tileX = (int) Math.round(ghost.pixelX / tileSize);
        int tileY = (int) Math.round(ghost.pixelY / tileSize);

        if (ghost.type == ghostBonusType) {
            ghostEatPelletAt(ghost, tileX, tileY, false);
        } else {
            collectGhostMagnetPellets(ghost, tileX, tileY);
        }

        if (!boardClear && areAllPelletsEaten()) {
            boardClear = true;
            clearWarningPortal();
        }
    }

    public void collectGhostMagnetPellets(Ghost ghost, int centerX, int centerY) {
        for (int x = centerX - 2; x <= centerX + 2; x++) {
            for (int y = centerY - 2; y <= centerY + 2; y++) {
                if (x > 0 && x < maxScreenCol - 1 && y > 0 && y < maxScreenRow - 1) {
                    ghostEatPelletAt(ghost, x, y, true);
                }
            }
        }
    }

    public void ghostEatPelletAt(Ghost ghost, int tileX, int tileY, boolean countForMagnetScore) {
        if (dots[tileX][tileY]) {
            dots[tileX][tileY] = false;
            addScore(-10);
            if (countForMagnetScore) {
                ghost.pelletsCollected++;
            }
        }

        if (!countForMagnetScore && bigDots[tileX][tileY]) {
            bigDots[tileX][tileY] = false;
            addScore(-10);

            if (ghost.type == ghostBonusType) {
                splitBonusGhostAtPowerPellet(ghost);
            }
        }
    }

    public void splitBonusGhostAtPowerPellet(Ghost ghost) {
        int index = ghosts.indexOf(ghost);
        int tileX = clampInt((int) Math.round(ghost.pixelX / tileSize), 1, maxScreenCol - 2);
        int tileY = clampInt((int) Math.round(ghost.pixelY / tileSize), 1, maxScreenRow - 2);

        if (index != -1) {
            addVisualDecal(decalAshType, ghost.pixelX, ghost.pixelY);
            ghosts.remove(index);
        }

        for (int i = 0; i < 2; i++) {
            int[] spawnTile = findOpenTileNear(tileX, tileY);
            addGhostAtTile(getRandomPowerGhostType(), spawnTile[0], spawnTile[1]);
        }
    }

    public int getRandomPowerGhostType() {
        int[] powerGhostTypes = {
            ghostCloneType,
            ghostBombType,
            ghostLaserType,
            ghostBonusType,
            ghostSpeedType,
            ghostMagnetType,
            ghostFireType
        };

        return powerGhostTypes[random.nextInt(powerGhostTypes.length)];
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

    public int getRemainingPelletCount() {
        int count = 0;

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                if (dots[x][y]) {
                    count++;
                }
                if (bigDots[x][y]) {
                    count++;
                }
            }
        }

        return count;
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

    public void eatPowerUpAtGhost(Ghost ghost) {
        if (isPoweredGhost(ghost) && ghost.type != ghostBonusType) {
            return;
        }

        int tileX = (int) Math.round(ghost.pixelX / tileSize);
        int tileY = (int) Math.round(ghost.pixelY / tileSize);

        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);

            if (powerUp.tileX == tileX && powerUp.tileY == tileY) {
                activateGhostPowerUp(ghost, powerUp.type);
                powerUps.remove(i);
                return;
            }
        }
    }

    public boolean isPoweredGhost(Ghost ghost) {
        return ghost.type >= ghostCloneType;
    }

    public void activateGhostPowerUp(Ghost ghost, int powerUpType) {
        if (powerUpType == 0) {
            transformGhost(ghost, ghostMagnetType);
        } else if (powerUpType == 1) {
            placeGhostSpikeTraps(powerUpDuration);
        } else if (powerUpType == 2) {
            transformGhost(ghost, ghostSpeedType);
        } else if (powerUpType == 3) {
            transformGhost(ghost, ghostBonusType);
        } else if (powerUpType == powerBombType) {
            transformGhost(ghost, ghostBombType);
        } else if (powerUpType == powerLaserType) {
            transformGhost(ghost, ghostLaserType);
            ghost.chargeTimer = ghostLaserChargeTime;
            ghost.warningTimer = ghostLaserWarningTime;
            ghost.activeTimer = ghostLaserFireTime;
            ghost.laserActive = false;
        } else if (powerUpType == powerCloneType) {
            spawnCloneGhosts(ghost);
        } else if (powerUpType == powerFireType) {
            transformGhost(ghost, ghostFireType);
        }
    }

    public void transformGhost(Ghost ghost, int type) {
        ghost.type = type;
        ghost.path.clear();
        ghost.goalX = -1;
        ghost.goalY = -1;
        ghost.speedDashActive = false;
        ghost.restTimer = 0;
        ghost.chargeTimer = 0;
        ghost.warningTimer = 0;
        ghost.activeTimer = 0;
        ghost.fuseTimer = 0;
        ghost.speedRampTimer = 0;
        ghost.laserActive = type != ghostLaserType;
        ghost.pelletsCollected = 0;
    }

    public void spawnCloneGhosts(Ghost sourceGhost) {
        int tileX = clampInt((int) Math.round(sourceGhost.pixelX / tileSize), 1, maxScreenCol - 2);
        int tileY = clampInt((int) Math.round(sourceGhost.pixelY / tileSize), 1, maxScreenRow - 2);

        for (int i = 0; i < cloneCount; i++) {
            int[] spawnTile = findOpenTileNear(tileX, tileY);
            addGhostAtTile(ghostCloneType, spawnTile[0], spawnTile[1]);
        }
    }

    public int[] findOpenTileNear(int originX, int originY) {
        ArrayList<int[]> candidates = new ArrayList<>();

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                if (!maze[x][y] && !isPortalEntrance(x, y)) {
                    candidates.add(new int[] { x, y });
                }
            }
        }

        if (candidates.isEmpty()) {
            return new int[] { originX, originY };
        }

        int bestDistance = Integer.MAX_VALUE;
        ArrayList<int[]> bestTiles = new ArrayList<>();

        for (int[] candidate : candidates) {
            int distance = getManhattanDistance(originX, originY, candidate[0], candidate[1]);

            if (distance < bestDistance) {
                bestDistance = distance;
                bestTiles.clear();
                bestTiles.add(candidate);
            } else if (distance == bestDistance) {
                bestTiles.add(candidate);
            }
        }

        return bestTiles.get(random.nextInt(bestTiles.size()));
    }

    public void activatePowerUp(int powerUpType) {
        if (powerUpType == 4) {
            startPowerMode();
            return;
        }

        if (powerUpType < 0 || powerUpType >= powerUpTimers.length) {
            return;
        }

        powerUpTimers[powerUpType] += powerUpDuration;

        if (powerUpType == 1) {
            extendSpikeTraps(powerUpTimers[powerUpType]);
            placeSpikeTraps(powerUpTimers[powerUpType]);
        } else if (powerUpType == powerCloneType) {
            spawnPacClones();
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

    public void placeGhostSpikeTraps(int timer) {
        for (int i = 0; i < ghostSpikeTrapCount; i++) {
            ArrayList<int[]> trapTiles = new ArrayList<>();

            for (int x = 1; x < maxScreenCol - 1; x++) {
                for (int y = 1; y < maxScreenRow - 1; y++) {
                    if (!maze[x][y] && !isPortalEntrance(x, y) && !hasSpikeAt(x, y) && !hasGhostSpikeAt(x, y)
                            && !(x == getPlayerTileX() && y == getPlayerTileY())) {
                        trapTiles.add(new int[] { x, y });
                    }
                }
            }

            if (trapTiles.isEmpty()) {
                return;
            }

            int[] tile = trapTiles.get(random.nextInt(trapTiles.size()));
            ghostSpikeTraps.add(new GhostSpikeTrap(tile[0], tile[1], timer));
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

    public boolean hasGhostSpikeAt(int tileX, int tileY) {
        for (GhostSpikeTrap spikeTrap : ghostSpikeTraps) {
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

    public void dropPlayerFireTrail() {
        if (!isPowerUpActive(powerFireType)) {
            return;
        }

        addFireTrail(getPlayerTileX(), getPlayerTileY(), false);
    }

    public void dropPacCloneFireTrail(PacClone clone) {
        if (!isPowerUpActive(powerFireType)) {
            return;
        }

        int tileX = clampInt((int) (clone.pixelX / tileSize), 0, maxScreenCol - 1);
        int tileY = clampInt((int) (clone.pixelY / tileSize), 0, maxScreenRow - 1);

        if (tileX == clone.lastFireTileX && tileY == clone.lastFireTileY) {
            return;
        }

        clone.lastFireTileX = tileX;
        clone.lastFireTileY = tileY;
        addFireTrail(tileX, tileY, false);
    }

    public void addFireTrail(int tileX, int tileY, boolean ghostFire) {
        if (tileX <= 0 || tileX >= maxScreenCol - 1 || tileY <= 0 || tileY >= maxScreenRow - 1 || maze[tileX][tileY]) {
            return;
        }

        fireTrails.add(new FireTrail(tileX, tileY, fireTrailDuration, ghostFire));
    }

    public void checkGhostCollision() {
        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            Ghost ghost = ghosts.get(i);

            if (Math.abs(playerPixelX - ghost.pixelX) < tileSize && Math.abs(playerPixelY - ghost.pixelY) < tileSize) {
                if (isPowerUpActive(powerBombType)) {
                    triggerBombExplosion();
                    return;
                }

                if (powerMode) {
                    killGhostAt(i, getGhostEatScore());
                    ghostEatScore = Math.min(1600, ghostEatScore * 2);
                    continue;
                }

                if (ghost.type == ghostLaserType) {
                    startDeathAnimation();
                    return;
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

    public void killGhostAt(int index, int points) {
        killGhostAt(index, points, false);
    }

    public void killGhostAt(int index, int points, boolean leaveAsh) {
        Ghost ghost = ghosts.get(index);
        if (points != 0) {
            addScore(getAdjustedGhostKillScore(ghost, points));
        }
        spawnGhostDeathEffect(ghost);
        if (leaveAsh) {
            addVisualDecal(decalAshType, ghost.pixelX, ghost.pixelY);
        }
        ghosts.remove(index);
        if (ghost.type == ghostBombType) {
            triggerGhostBombExplosion(ghost.pixelX + tileSize / 2.0, ghost.pixelY + tileSize / 2.0);
        }
    }

    public int getAdjustedGhostKillScore(Ghost ghost, int basePoints) {
        if (ghost.type == ghostBonusType) {
            return basePoints + 3200;
        }
        if (ghost.type == ghostMagnetType) {
            return 5000 - ghost.pelletsCollected * 10;
        }

        return basePoints;
    }

    public void triggerBombExplosion() {
        powerUpTimers[powerBombType] = 0;
        double[] blast = getBombBlastWorldBounds();
        startBombExplosionEffect(blast);

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            if (ghostIntersectsRect(ghosts.get(i), blast[0], blast[1], blast[2], blast[3])) {
                killGhostAt(i, getGhostEatScore(), true);
            }
        }
    }

    public void triggerGhostBombExplosion(double centerX, double centerY) {
        double[] blast = getBombBlastWorldBounds(centerX, centerY);
        startBombExplosionEffect(blast);

        if (playerIntersectsRect(blast[0], blast[1], blast[2], blast[3])) {
            startDeathAnimation();
        }

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            if (ghostIntersectsRect(ghosts.get(i), blast[0], blast[1], blast[2], blast[3])) {
                killGhostAt(i, 0, true);
            }
        }
    }

    public void triggerPacCloneBombExplosion(PacClone clone) {
        double[] blast = getBombBlastWorldBounds(clone.pixelX + tileSize / 2.0, clone.pixelY + tileSize / 2.0);
        startBombExplosionEffect(blast);

        if (playerIntersectsRect(blast[0], blast[1], blast[2], blast[3])) {
            startDeathAnimation();
        }

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            if (ghostIntersectsRect(ghosts.get(i), blast[0], blast[1], blast[2], blast[3])) {
                killGhostAt(i, 0, true);
            }
        }
    }

    public void startBombExplosionEffect(double[] blast) {
        bombExplosionMinX = blast[0];
        bombExplosionMinY = blast[1];
        bombExplosionMaxX = blast[2];
        bombExplosionMaxY = blast[3];
        bombExplosionTimer = bombExplosionFrameTime;
        burnWallsInRect(bombExplosionMinX, bombExplosionMinY, bombExplosionMaxX, bombExplosionMaxY);
    }

    public double[] getBombBlastWorldBounds() {
        return getBombBlastWorldBounds(playerPixelX + tileSize / 2.0, playerPixelY + tileSize / 2.0);
    }

    public double[] getBombBlastWorldBounds(double centerX, double centerY) {
        double halfBlastSize = (bombRadiusTiles * 2 + 1) * tileSize / 2.0;

        return new double[] {
            centerX - halfBlastSize,
            centerY - halfBlastSize,
            centerX + halfBlastSize,
            centerY + halfBlastSize
        };
    }

    public void checkLaserGhostHits() {
        if (!isPowerUpActive(powerLaserType)) {
            return;
        }

        double[] beam = getLaserBeamWorldBounds();
        burnLaserTargetWall();

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            if (ghostIntersectsRect(ghosts.get(i), beam[0], beam[1], beam[2], beam[3])) {
                killGhostAt(i, getGhostEatScore(), true);
            }
        }
    }

    public void checkPacCloneLaserHits() {
        if (!isPowerUpActive(powerLaserType)) {
            return;
        }

        for (PacClone clone : pacClones) {
            double[] beam = getPacCloneLaserBeamWorldBounds(clone);
            burnPacCloneLaserTargetWall(clone);

            for (int i = ghosts.size() - 1; i >= 0; i--) {
                if (i >= ghosts.size()) {
                    continue;
                }

                if (ghostIntersectsRect(ghosts.get(i), beam[0], beam[1], beam[2], beam[3])) {
                    killGhostAt(i, 0, true);
                }
            }
        }
    }

    public void burnLaserTargetWall() {
        int tileX = getPlayerCenterTileX();
        int tileY = getPlayerCenterTileY();
        int beamDirectionX = lastDirectionX;
        int beamDirectionY = lastDirectionY;

        while (canMove(tileX + beamDirectionX, tileY + beamDirectionY)) {
            tileX += beamDirectionX;
            tileY += beamDirectionY;
        }

        burnWallAt(tileX + beamDirectionX, tileY + beamDirectionY);
    }

    public void addVisualDecal(int type, double pixelX, double pixelY) {
        visualDecals.add(new VisualDecal(type, pixelX, pixelY));
    }

    public double[] getLaserBeamWorldBounds() {
        double centerX = playerPixelX + tileSize / 2.0;
        double centerY = playerPixelY + tileSize / 2.0;
        int tileX = getPlayerCenterTileX();
        int tileY = getPlayerCenterTileY();
        int beamDirectionX = lastDirectionX;
        int beamDirectionY = lastDirectionY;
        double halfWidth = laserWidth / 2.0;

        if (beamDirectionX != 0) {
            int endTileX = tileX;

            while (canMove(endTileX + beamDirectionX, tileY)) {
                endTileX += beamDirectionX;
            }

            double endX = beamDirectionX > 0 ? (endTileX + 1) * tileSize : endTileX * tileSize;
            return new double[] {
                Math.min(centerX, endX),
                centerY - halfWidth,
                Math.max(centerX, endX),
                centerY + halfWidth
            };
        }

        int endTileY = tileY;

        while (canMove(tileX, endTileY + beamDirectionY)) {
            endTileY += beamDirectionY;
        }

        double endY = beamDirectionY > 0 ? (endTileY + 1) * tileSize : endTileY * tileSize;
        return new double[] {
            centerX - halfWidth,
            Math.min(centerY, endY),
            centerX + halfWidth,
            Math.max(centerY, endY)
        };
    }

    public void checkGhostLaserHits() {
        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            Ghost ghost = ghosts.get(i);

            if (ghost.type != ghostLaserType || !ghost.laserActive) {
                continue;
            }

            double[] beam = getGhostLaserBeamWorldBounds(ghost);
            burnGhostLaserTargetWall(ghost);

            if (playerIntersectsRect(beam[0], beam[1], beam[2], beam[3])) {
                startDeathAnimation();
                return;
            }

            for (int j = ghosts.size() - 1; j >= 0; j--) {
                if (i == j) {
                    continue;
                }

                if (ghostIntersectsRect(ghosts.get(j), beam[0], beam[1], beam[2], beam[3])) {
                    killGhostAt(j, 0, true);
                    return;
                }
            }
        }
    }

    public void checkFireTrailCollisions() {
        for (int fireIndex = fireTrails.size() - 1; fireIndex >= 0; fireIndex--) {
            FireTrail fireTrail = fireTrails.get(fireIndex);
            double minX = fireTrail.tileX * tileSize;
            double minY = fireTrail.tileY * tileSize;
            double maxX = minX + tileSize;
            double maxY = minY + tileSize;

            if (fireTrail.ghostFire) {
                if (playerIntersectsRect(minX, minY, maxX, maxY)) {
                    startDeathAnimation();
                    return;
                }

                for (int i = ghosts.size() - 1; i >= 0; i--) {
                    if (i >= ghosts.size()) {
                        continue;
                    }

                    Ghost ghost = ghosts.get(i);
                    if (ghost.type == ghostBombType && ghostIntersectsRect(ghost, minX, minY, maxX, maxY)) {
                        killGhostAt(i, 0, true);
                        return;
                    }
                }
            } else {
                for (int i = ghosts.size() - 1; i >= 0; i--) {
                    if (i >= ghosts.size()) {
                        continue;
                    }

                    if (ghostIntersectsRect(ghosts.get(i), minX, minY, maxX, maxY)) {
                        killGhostAt(i, 0, true);
                        return;
                    }
                }
            }
        }
    }

    public void checkPacCloneGhostCollisions() {
        for (int cloneIndex = pacClones.size() - 1; cloneIndex >= 0; cloneIndex--) {
            PacClone clone = pacClones.get(cloneIndex);

            for (int ghostIndex = ghosts.size() - 1; ghostIndex >= 0; ghostIndex--) {
                if (ghostIndex >= ghosts.size()) {
                    continue;
                }

                Ghost ghost = ghosts.get(ghostIndex);

                if (rectsIntersect(
                        clone.pixelX,
                        clone.pixelY,
                        clone.pixelX + tileSize,
                        clone.pixelY + tileSize,
                        ghost.pixelX,
                        ghost.pixelY,
                        ghost.pixelX + tileSize,
                        ghost.pixelY + tileSize)) {
                    addVisualDecal(decalAshType, clone.pixelX, clone.pixelY);
                    pacClones.remove(cloneIndex);
                    if (isPowerUpActive(powerBombType)) {
                        triggerPacCloneBombExplosion(clone);
                    } else {
                        killGhostAt(ghostIndex, 0, false);
                    }
                    break;
                }
            }
        }
    }

    public double[] getGhostLaserBeamWorldBounds(Ghost ghost) {
        setGhostLaserDirection(ghost);

        return getDirectedLaserBeamWorldBounds(
                ghost.pixelX + tileSize / 2.0,
                ghost.pixelY + tileSize / 2.0,
                (int) ((ghost.pixelX + tileSize / 2.0) / tileSize),
                (int) ((ghost.pixelY + tileSize / 2.0) / tileSize),
                ghost.laserDirectionX,
                ghost.laserDirectionY);
    }

    public double[] getPacCloneLaserBeamWorldBounds(PacClone clone) {
        int beamDirectionX = clone.directionX;
        int beamDirectionY = clone.directionY;

        if (beamDirectionX == 0 && beamDirectionY == 0) {
            beamDirectionX = 1;
        }

        return getDirectedLaserBeamWorldBounds(
                clone.pixelX + tileSize / 2.0,
                clone.pixelY + tileSize / 2.0,
                (int) ((clone.pixelX + tileSize / 2.0) / tileSize),
                (int) ((clone.pixelY + tileSize / 2.0) / tileSize),
                beamDirectionX,
                beamDirectionY);
    }

    public double[] getDirectedLaserBeamWorldBounds(double centerX, double centerY, int tileX, int tileY,
            int beamDirectionX, int beamDirectionY) {
        double halfWidth = laserWidth / 2.0;

        if (beamDirectionX != 0) {
            int endTileX = tileX;

            while (canMove(endTileX + beamDirectionX, tileY)) {
                endTileX += beamDirectionX;
            }

            double endX = beamDirectionX > 0 ? (endTileX + 1) * tileSize : endTileX * tileSize;
            return new double[] {
                Math.min(centerX, endX),
                centerY - halfWidth,
                Math.max(centerX, endX),
                centerY + halfWidth
            };
        }

        int endTileY = tileY;

        while (canMove(tileX, endTileY + beamDirectionY)) {
            endTileY += beamDirectionY;
        }

        double endY = beamDirectionY > 0 ? (endTileY + 1) * tileSize : endTileY * tileSize;
        return new double[] {
            centerX - halfWidth,
            Math.min(centerY, endY),
            centerX + halfWidth,
            Math.max(centerY, endY)
        };
    }

    public void burnGhostLaserTargetWall(Ghost ghost) {
        setGhostLaserDirection(ghost);

        int tileX = (int) ((ghost.pixelX + tileSize / 2.0) / tileSize);
        int tileY = (int) ((ghost.pixelY + tileSize / 2.0) / tileSize);
        int beamDirectionX = ghost.laserDirectionX;
        int beamDirectionY = ghost.laserDirectionY;

        while (canMove(tileX + beamDirectionX, tileY + beamDirectionY)) {
            tileX += beamDirectionX;
            tileY += beamDirectionY;
        }

        burnWallAt(tileX + beamDirectionX, tileY + beamDirectionY);
    }

    public void burnPacCloneLaserTargetWall(PacClone clone) {
        int tileX = (int) ((clone.pixelX + tileSize / 2.0) / tileSize);
        int tileY = (int) ((clone.pixelY + tileSize / 2.0) / tileSize);
        int beamDirectionX = clone.directionX;
        int beamDirectionY = clone.directionY;

        if (beamDirectionX == 0 && beamDirectionY == 0) {
            beamDirectionX = 1;
        }

        while (canMove(tileX + beamDirectionX, tileY + beamDirectionY)) {
            tileX += beamDirectionX;
            tileY += beamDirectionY;
        }

        burnWallAt(tileX + beamDirectionX, tileY + beamDirectionY);
    }

    public boolean ghostIntersectsRect(Ghost ghost, double minX, double minY, double maxX, double maxY) {
        return ghost.pixelX < maxX
                && ghost.pixelX + tileSize > minX
                && ghost.pixelY < maxY
                && ghost.pixelY + tileSize > minY;
    }

    public boolean playerIntersectsRect(double minX, double minY, double maxX, double maxY) {
        return playerPixelX < maxX
                && playerPixelX + tileSize > minX
                && playerPixelY < maxY
                && playerPixelY + tileSize > minY;
    }

    public boolean isWallAffectedByBombExplosion(int tileX, int tileY) {
        return burnedWalls != null
                && tileX >= 0
                && tileX < maxScreenCol
                && tileY >= 0
                && tileY < maxScreenRow
                && burnedWalls[tileX][tileY];
    }

    public void burnWallsInRect(double minX, double minY, double maxX, double maxY) {
        if (burnedWalls == null) {
            burnedWalls = new boolean[maxScreenCol][maxScreenRow];
        }

        for (int x = 0; x < maxScreenCol; x++) {
            for (int y = 0; y < maxScreenRow; y++) {
                if (isDrawnWallTile(x, y)
                        && rectsIntersect(
                                x * tileSize,
                                y * tileSize,
                                (x + 1) * tileSize,
                                (y + 1) * tileSize,
                                minX,
                                minY,
                                maxX,
                                maxY)) {
                    burnedWalls[x][y] = true;
                }
            }
        }
    }

    public void burnWallAt(int tileX, int tileY) {
        if (tileX < 0 || tileX >= maxScreenCol || tileY < 0 || tileY >= maxScreenRow || !isDrawnWallTile(tileX, tileY)) {
            return;
        }

        if (burnedWalls == null) {
            burnedWalls = new boolean[maxScreenCol][maxScreenRow];
        }

        burnedWalls[tileX][tileY] = true;
    }

    public boolean isDrawnWallTile(int tileX, int tileY) {
        if (tileX < 0 || tileX >= maxScreenCol || tileY < 0 || tileY >= maxScreenRow) {
            return false;
        }

        if (tileX > 0 && tileX < maxScreenCol - 1 && tileY > 0 && tileY < maxScreenRow - 1) {
            return maze[tileX][tileY];
        }

        return !isPortalTile(tileX, tileY)
                && (tileX == 0 || tileX == maxScreenCol - 1 || tileY == 0 || tileY == maxScreenRow - 1);
    }

    public boolean isOuterWallTile(int tileX, int tileY) {
        return tileX == 0 || tileX == maxScreenCol - 1 || tileY == 0 || tileY == maxScreenRow - 1;
    }

    public boolean rectsIntersect(double minX1, double minY1, double maxX1, double maxY1,
            double minX2, double minY2, double maxX2, double maxY2) {
        return minX1 < maxX2
                && maxX1 > minX2
                && minY1 < maxY2
                && maxY1 > minY2;
    }

    public int getPlayerCenterTileX() {
        return clampInt((int) ((playerPixelX + tileSize / 2.0) / tileSize), 0, maxScreenCol - 1);
    }

    public int getPlayerCenterTileY() {
        return clampInt((int) ((playerPixelY + tileSize / 2.0) / tileSize), 0, maxScreenRow - 1);
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
        ArrayList<int[]> bestPath = new ArrayList<>();

        for (int[] portal : getPortalTiles()) {
            ArrayList<int[]> path = findAStarPath(startX, startY, portal[0], portal[1]);

            if (startX == portal[0] && startY == portal[1]) {
                return path;
            }
            if (path.isEmpty()) {
                continue;
            }
            if (bestPath.isEmpty() || path.size() < bestPath.size()) {
                bestPath = path;
            }
        }

        return bestPath;
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
            effect.pixelX = effect.targetPixelX;
            effect.pixelY = effect.targetPixelY;
            return effect.path.isEmpty();
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
            if (i >= ghosts.size()) {
                continue;
            }

            Ghost ghost = ghosts.get(i);
            updateGhost(ghost);
            dropGhostFireTrail(ghost);
            eatPowerUpAtGhost(ghost);
            eatPelletsAtGhost(ghost);

            if (!ghosts.contains(ghost)) {
                continue;
            }

            if (checkSpikeTrapCollision(ghost)) {
                killGhostAt(i, 200);
            }
        }
    }

    public void dropGhostFireTrail(Ghost ghost) {
        if (ghost.type != ghostFireType || isGhostMoving(ghost)) {
            return;
        }

        int tileX = (int) (ghost.pixelX / tileSize);
        int tileY = (int) (ghost.pixelY / tileSize);

        if (ghost.lastFireTileX == tileX && ghost.lastFireTileY == tileY) {
            return;
        }

        ghost.lastFireTileX = tileX;
        ghost.lastFireTileY = tileY;
        addFireTrail(tileX, tileY, true);
    }

    public void updateGhost(Ghost ghost) {
        if (updateGhostSpecialState(ghost)) {
            return;
        }

        if (!isGhostMoving(ghost)) {
            chooseGhostTarget(ghost);
        }

        if (isGhostMoving(ghost)) {
            moveGhostTowardTarget(ghost);
        }
    }

    public boolean updateGhostSpecialState(Ghost ghost) {
        if (ghost.type == ghostBombType) {
            return updateBombGhostState(ghost);
        }

        if (ghost.type == ghostLaserType) {
            return updateLaserGhostState(ghost);
        }

        if (ghost.type == ghostSpeedType) {
            return updateSpeedGhostState(ghost);
        }

        return false;
    }

    public boolean updateBombGhostState(Ghost ghost) {
        if (ghost.fuseTimer > 0) {
            ghost.fuseTimer--;
            ghost.targetPixelX = ghost.pixelX;
            ghost.targetPixelY = ghost.pixelY;

            if (ghost.fuseTimer <= 0) {
                killBombGhostWithoutRespawn(ghost);
            }

            return true;
        }

        ghost.speedRampTimer++;

        if (isPlayerInBombGhostTriggerZone(ghost)) {
            ghost.fuseTimer = ghostBombFuseTime;
            ghost.directionX = 0;
            ghost.directionY = 0;
            ghost.targetPixelX = ghost.pixelX;
            ghost.targetPixelY = ghost.pixelY;
            return true;
        }

        return false;
    }

    public boolean isPlayerInBombGhostTriggerZone(Ghost ghost) {
        int ghostTileX = clampInt((int) ((ghost.pixelX + tileSize / 2.0) / tileSize), 0, maxScreenCol - 1);
        int ghostTileY = clampInt((int) ((ghost.pixelY + tileSize / 2.0) / tileSize), 0, maxScreenRow - 1);

        return Math.abs(getPlayerCenterTileX() - ghostTileX) <= ghostBombTriggerRadiusTiles
                && Math.abs(getPlayerCenterTileY() - ghostTileY) <= ghostBombTriggerRadiusTiles;
    }

    public void killBombGhostWithoutRespawn(Ghost ghost) {
        int index = ghosts.indexOf(ghost);

        if (index == -1) {
            return;
        }

        addVisualDecal(decalAshType, ghost.pixelX, ghost.pixelY);
        ghosts.remove(index);
        triggerGhostBombExplosion(ghost.pixelX + tileSize / 2.0, ghost.pixelY + tileSize / 2.0);
    }

    public boolean updateLaserGhostState(Ghost ghost) {
        if (ghost.laserActive) {
            ghost.activeTimer--;

            if (ghost.activeTimer <= 0) {
                resetLaserGhostCycle(ghost);
                return true;
            }

            return false;
        }

        ghost.targetPixelX = ghost.pixelX;
        ghost.targetPixelY = ghost.pixelY;

        if (ghost.chargeTimer > 0) {
            ghost.chargeTimer--;
            return true;
        }

        if (ghost.warningTimer > 0) {
            ghost.warningTimer--;
            return true;
        }

        ghost.laserActive = true;
        setGhostLaserDirection(ghost);
        ghost.activeTimer = ghostLaserFireTime;
        return false;
    }

    public void setGhostLaserDirection(Ghost ghost) {
        if (ghost.directionX != 0 || ghost.directionY != 0) {
            ghost.laserDirectionX = ghost.directionX;
            ghost.laserDirectionY = ghost.directionY;
        }
    }

    public void resetLaserGhostCycle(Ghost ghost) {
        ghost.laserActive = false;
        ghost.chargeTimer = ghostLaserChargeTime;
        ghost.warningTimer = ghostLaserWarningTime;
        ghost.activeTimer = ghostLaserFireTime;
    }

    public boolean updateSpeedGhostState(Ghost ghost) {
        if (ghost.speedDashActive) {
            if (!isGhostMoving(ghost)) {
                ghost.speedDashActive = false;
                ghost.restTimer = ghostSpeedRecoverTime;
                return true;
            }
            return false;
        }

        int[] sightDirection = getLineOfSightDirectionToPlayer((int) (ghost.pixelX / tileSize), (int) (ghost.pixelY / tileSize));

        if (ghost.restTimer > 0) {
            ghost.restTimer--;

            if (sightDirection != null) {
                startSpeedGhostDash(ghost, sightDirection);
                return false;
            }

            return true;
        }

        if (sightDirection != null) {
            startSpeedGhostDash(ghost, sightDirection);
            return false;
        }

        return false;
    }

    public void startSpeedGhostDash(Ghost ghost, int[] direction) {
        int tileX = (int) (ghost.pixelX / tileSize);
        int tileY = (int) (ghost.pixelY / tileSize);
        int[] endTile = getDashEndTile(tileX, tileY, direction[0], direction[1]);

        ghost.directionX = direction[0];
        ghost.directionY = direction[1];
        ghost.targetPixelX = endTile[0] * tileSize;
        ghost.targetPixelY = endTile[1] * tileSize;
        ghost.speedDashActive = true;
        ghost.restTimer = 0;
        ghost.path.clear();

        if (tileX == getPlayerCenterTileX() && tileY == getPlayerCenterTileY()) {
            ghost.speedDashActive = false;
            ghost.restTimer = ghostSpeedRecoverTime;
        }
    }

    public int[] getDashEndTile(int tileX, int tileY, int directionX, int directionY) {
        while (canMove(tileX + directionX, tileY + directionY)) {
            tileX += directionX;
            tileY += directionY;
        }

        return new int[] { tileX, tileY };
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
        if (ghost.type == ghostCloneType || ghost.type == ghostFireType) {
            return chooseRandomWallBounceDirection(ghost, tileX, tileY);
        }

        if (ghost.type == ghostSpeedType) {
            return chooseRandomWallBounceDirection(ghost, tileX, tileY);
        }

        if (ghost.type == ghostBombType || ghost.type == ghostLaserType) {
            return chooseAStarDirection(ghost, tileX, tileY);
        }

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
        if (ghost.type == ghostMagnetType || ghost.type == ghostBonusType || ghost.type == ghostSpikeType) {
            return chooseAStarDirection(ghost, tileX, tileY);
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
        return chooseRandomWallBounceDirection(ghost.directionX, ghost.directionY, tileX, tileY);
    }

    public int[] chooseRandomWallBounceDirection(int currentDirectionX, int currentDirectionY, int tileX, int tileY) {
        if (currentDirectionX == 0 && currentDirectionY == 0) {
            return getRandomValidDirection(tileX, tileY);
        }

        int[][] forwardChoices = {
            { currentDirectionX, currentDirectionY },
            { currentDirectionY, -currentDirectionX },
            { -currentDirectionY, currentDirectionX }
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

        int[] reverse = { -currentDirectionX, -currentDirectionY };
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

        if (shouldSpawnAfterImage() && ghost.type == ghostSpeedType && ghost.speedDashActive && isGhostMoving(ghost)) {
            addAfterImage(getGhostSprite(ghost), ghost.pixelX, ghost.pixelY, 0,
                    ghost.type != ghostMagnetType && (ghostAnimationCounter / animationDelay) % 2 == 1);
        }

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

    public void checkGhostSpikeTrapCollision() {
        int tileX = (int) (playerPixelX / tileSize);
        int tileY = (int) (playerPixelY / tileSize);

        for (GhostSpikeTrap spikeTrap : ghostSpikeTraps) {
            if (spikeTrap.tileX == tileX && spikeTrap.tileY == tileY) {
                spikeTrap.used = true;
                startDeathAnimation();
                return;
            }
        }
    }

    public void handleGhostPortalWrap(Ghost ghost) {
        int tileX = (int) (ghost.pixelX / tileSize);
        int tileY = (int) (ghost.pixelY / tileSize);

        if (!isPortalTile(tileX, tileY)) {
            return;
        }

        int[] oppositePortal = getOppositePortalTile(tileX, tileY);
        ghost.pixelX = oppositePortal[0] * tileSize;
        ghost.pixelY = oppositePortal[1] * tileSize;
        ghost.targetPixelX = ghost.pixelX;
        ghost.targetPixelY = ghost.pixelY;
        ghost.path.clear();
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
            neighbors.add(getOppositePortalTile(x, y));
        }

        return neighbors;
    }

    public int[] getOppositePortalTile(int tileX, int tileY) {
        if (tileX == 0 && tileY == tunnelY) {
            return new int[] { maxScreenCol - 1, tunnelY };
        }
        if (tileX == maxScreenCol - 1 && tileY == tunnelY) {
            return new int[] { 0, tunnelY };
        }
        if (tileX == getTunnelX() && tileY == 0) {
            return new int[] { getTunnelX(), maxScreenRow - 1 };
        }
        if (tileX == getTunnelX() && tileY == maxScreenRow - 1) {
            return new int[] { getTunnelX(), 0 };
        }

        return new int[] { tileX, tileY };
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

    public int[] getLineOfSightDirectionToPlayer(int tileX, int tileY) {
        int playerTileX = getPlayerCenterTileX();
        int playerTileY = getPlayerCenterTileY();

        if (tileX == playerTileX) {
            int directionY = playerTileY > tileY ? 1 : -1;
            return hasClearLine(tileX, tileY, playerTileX, playerTileY, 0, directionY)
                    ? new int[] { 0, directionY }
                    : null;
        }

        if (tileY == playerTileY) {
            int directionX = playerTileX > tileX ? 1 : -1;
            return hasClearLine(tileX, tileY, playerTileX, playerTileY, directionX, 0)
                    ? new int[] { directionX, 0 }
                    : null;
        }

        return null;
    }

    public boolean hasClearLine(int startX, int startY, int goalX, int goalY, int directionX, int directionY) {
        int tileX = startX + directionX;
        int tileY = startY + directionY;

        while (tileX != goalX || tileY != goalY) {
            if (!canMove(tileX, tileY)) {
                return false;
            }

            tileX += directionX;
            tileY += directionY;
        }

        return true;
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

    public void drawFireTrails(Graphics2D g2) {
        for (FireTrail fireTrail : fireTrails) {
            BufferedImage[] sprites = fireTrail.ghostFire ? ghostFireSprites : fireSprites;
            BufferedImage sprite = sprites[getFireFrame(fireTrail)];
            drawImageAtTile(g2, sprite, fireTrail.tileX, fireTrail.tileY);
        }
    }

    public int getFireFrame(FireTrail fireTrail) {
        if (fireTrail.age < 18) {
            return Math.min(5, fireTrail.age / 3);
        }

        if (fireTrail.timer <= 16) {
            return 10 + Math.min(3, (16 - fireTrail.timer) / 4);
        }

        return 6 + ((fireTrail.age / animationDelay) % 4);
    }

    public void drawGhostSpikeTraps(Graphics2D g2) {
        for (GhostSpikeTrap spikeTrap : ghostSpikeTraps) {
            drawImageAtTile(g2, ghostSpikeSprites[spikeTrap.used ? 1 : 0], spikeTrap.tileX, spikeTrap.tileY);
        }
    }

    public void drawVisualDecals(Graphics2D g2) {
        for (VisualDecal decal : visualDecals) {
            if (decal.type < 0 || decal.type >= decalSprites.length || decalSprites[decal.type] == null) {
                continue;
            }

            int screenX = worldToScreenX(decal.pixelX);
            int screenY = worldToScreenY(decal.pixelY);
            g2.drawImage(decalSprites[decal.type], screenX, screenY, tileSize, tileSize, null);
        }
    }

    public void drawAfterImages(Graphics2D g2) {
        Composite originalComposite = g2.getComposite();

        for (AfterImage afterImage : afterImages) {
            int screenX = worldToScreenX(afterImage.pixelX);
            int screenY = worldToScreenY(afterImage.pixelY);
            float alpha = clampInt(afterImage.alpha, 0, 255) / 255.0f;
            Graphics2D afterGraphics = (Graphics2D) g2.create();

            afterGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            afterGraphics.rotate(afterImage.angle, screenX + tileSize / 2.0, screenY + tileSize / 2.0);

            if (afterImage.flipped) {
                afterGraphics.drawImage(afterImage.sprite, screenX + tileSize, screenY, -tileSize, tileSize, null);
            } else {
                afterGraphics.drawImage(afterImage.sprite, screenX, screenY, tileSize, tileSize, null);
            }

            afterGraphics.dispose();
        }

        g2.setComposite(originalComposite);
    }

    public void drawExitMarkers(Graphics2D g2) {
        if (!boardClear) {
            return;
        }

        BufferedImage sprite = outSprites[(elapsedFrames / animationDelay) % outSprites.length];
        int leftScreenY = worldToScreenY(tunnelY * tileSize);
        g2.drawImage(sprite, worldToScreenX(tileSize) + tileSize, leftScreenY, -tileSize, tileSize, null);
        g2.drawImage(sprite, worldToScreenX((maxScreenCol - 2) * tileSize), leftScreenY, tileSize, tileSize, null);
        drawRotatedTileSprite(g2, sprite, getTunnelX(), 1, -Math.PI / 2);
        drawRotatedTileSprite(g2, sprite, getTunnelX(), maxScreenRow - 2, Math.PI / 2);
    }

    public void drawRotatedTileSprite(Graphics2D g2, BufferedImage sprite, int tileX, int tileY, double angle) {
        int screenX = worldToScreenX(tileX * tileSize);
        int screenY = worldToScreenY(tileY * tileSize);
        Graphics2D rotatedGraphics = (Graphics2D) g2.create();

        rotatedGraphics.rotate(angle, screenX + tileSize / 2.0, screenY + tileSize / 2.0);
        rotatedGraphics.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        rotatedGraphics.dispose();
    }

    public void drawSpawnWarning(Graphics2D g2) {
        if (warningPortalX == -1 || warningPortalY == -1 || boardClear || playerDead) {
            return;
        }

        BufferedImage sprite = warnSprites[(elapsedFrames / animationDelay) % warnSprites.length];
        int screenX = worldToScreenX(warningPortalX * tileSize);
        int screenY = worldToScreenY(warningPortalY * tileSize);

        if (warningPortalX == maxScreenCol - 1) {
            g2.drawImage(sprite, screenX + tileSize, screenY, -tileSize, tileSize, null);
        } else {
            g2.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        }
    }

    public void drawLaser(Graphics2D g2) {
        if (!isPowerUpActive(powerLaserType) || playerDead || deathAnimationDone) {
            return;
        }

        drawLaserBeam(g2, getLaserBeamWorldBounds());
    }

    public void drawGhostLasers(Graphics2D g2) {
        for (Ghost ghost : ghosts) {
            if (ghost.type == ghostLaserType && ghost.laserActive) {
                drawLaserBeam(g2, getGhostLaserBeamWorldBounds(ghost));
            }
        }
    }

    public void drawPacCloneLasers(Graphics2D g2) {
        if (!isPowerUpActive(powerLaserType) || playerDead || deathAnimationDone) {
            return;
        }

        for (PacClone clone : pacClones) {
            drawLaserBeam(g2, getPacCloneLaserBeamWorldBounds(clone));
        }
    }

    public void drawLaserBeam(Graphics2D g2, double[] beam) {
        int minScreenX = worldToScreenX(beam[0]);
        int maxScreenX = worldToScreenX(beam[2]);
        int minScreenY = worldBoundaryToScreenY(beam[3]);
        int maxScreenY = worldBoundaryToScreenY(beam[1]);
        Color originalColor = g2.getColor();

        g2.setColor(new Color(0xff7f00));
        g2.fillRect(
                Math.min(minScreenX, maxScreenX),
                Math.min(minScreenY, maxScreenY),
                Math.max(laserWidth, Math.abs(maxScreenX - minScreenX)),
                Math.max(laserWidth, Math.abs(maxScreenY - minScreenY)));
        g2.setColor(originalColor);
    }

    public void drawBombExplosion(Graphics2D g2) {
        if (bombExplosionTimer <= 0) {
            return;
        }

        int minScreenX = worldToScreenX(bombExplosionMinX);
        int maxScreenX = worldToScreenX(bombExplosionMaxX);
        int minScreenY = worldBoundaryToScreenY(bombExplosionMaxY);
        int maxScreenY = worldBoundaryToScreenY(bombExplosionMinY);
        Stroke originalStroke = g2.getStroke();
        Color originalColor = g2.getColor();

        g2.setColor(new Color(0xff0000));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(
                Math.min(minScreenX, maxScreenX),
                Math.min(minScreenY, maxScreenY),
                Math.abs(maxScreenX - minScreenX),
                Math.abs(maxScreenY - minScreenY));
        g2.setStroke(originalStroke);
        g2.setColor(originalColor);
    }

    public void drawPacClones(Graphics2D g2) {
        BufferedImage sprite = getActivePacCloneSprite();

        for (PacClone clone : pacClones) {
            int screenX = worldToScreenX(clone.pixelX);
            int screenY = worldToScreenY(clone.pixelY);
            double angle = getDirectionAngle(clone.directionX, clone.directionY);

            Graphics2D cloneGraphics = (Graphics2D) g2.create();
            cloneGraphics.rotate(angle, screenX + tileSize / 2.0, screenY + tileSize / 2.0);
            cloneGraphics.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
            cloneGraphics.dispose();
        }
    }

    public BufferedImage getActivePacCloneSprite() {
        BufferedImage[] sprites = isPowerUpActive(powerBombType) ? pacCloneBombSprites : pacCloneSprites;
        return sprites[(animationCounter / animationDelay) % sprites.length];
    }

    public double getDirectionAngle(int directionX, int directionY) {
        if (directionX < 0) {
            return Math.PI;
        }
        if (directionY > 0) {
            return -Math.PI / 2;
        }
        if (directionY < 0) {
            return Math.PI / 2;
        }
        return 0;
    }

    public void drawPlayer(Graphics2D g2) {
        if (deathAnimationDone) {
            return;
        }

        BufferedImage sprite = playerDead
                ? pacSprites[deathFrame]
                : getActivePlayerSprite();
        int screenX = worldToScreenX(playerPixelX);
        int screenY = worldToScreenY(playerPixelY);
        double angle = getPlayerAngle();

        Graphics2D playerGraphics = (Graphics2D) g2.create();
        playerGraphics.rotate(angle, screenX + tileSize / 2.0, screenY + tileSize / 2.0);
        playerGraphics.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        playerGraphics.dispose();
    }

    public BufferedImage getActivePlayerSprite() {
        int frame = (animationCounter / animationDelay) % 2;

        if (isPowerUpActive(powerBombType)) {
            return pacBombSprites[frame];
        }

        return pacSprites[frame];
    }

    public double getPlayerAngle() {
        return getDirectionAngle(lastDirectionX, lastDirectionY);
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

        drawBoardStats(g2);
        drawActiveEffectTimers(g2);
    }

    public void drawBoardStats(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.drawString("PELLETS " + getRemainingPelletCount(), 12, 42);
        g2.drawString("GHOSTS " + ghosts.size(), 122, 42);
    }

    public void drawMenu(Graphics2D g2) {
        g2.drawImage(menuScreen, 0, 0, getWidth(), getHeight(), null);

        String[] options = { "NEW RUN", "OPTION", "ALMANAC", "HALL OF FAME", "QUIT" };
        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 30));

        int centerX = getWidth() / 2;
        int startY = Math.max(200, getHeight() / 2 - 70);

        for (int i = 0; i < options.length; i++) {
            String text = i == menuChoice ? ">" + options[i] + "<" : options[i];
            drawCenteredMenuText(g2, text, centerX, startY + i * 46);
        }
    }

    public void drawOptions(Graphics2D g2) {
        g2.drawImage(blankMenuScreen, 0, 0, getWidth(), getHeight(), null);
        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 20));

        String[] options = {
            "GHOST SPEED: " + formatDouble(draftGhostSpeed),
            "PACMAN SPEED: " + formatDouble(draftPlayerSpeed),
            "GHOST SPAWN INTERVAL: " + draftGhostSpawnSeconds,
            "PELLET TIME: " + draftPelletSeconds,
            "POWER TIME: " + draftPowerSeconds,
            "SPECIAL GHOST CHANCE: " + draftSpecialGhostChancePercent + "%",
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

    public void drawAlmanac(Graphics2D g2) {
        g2.drawImage(blankMenuScreen, 0, 0, getWidth(), getHeight(), null);

        if (almanacTitles.isEmpty()) {
            loadAlmanacEntries();
        }

        almanacIndex = clampInt(almanacIndex, 0, Math.max(0, almanacTitles.size() - 1));
        BufferedImage sprite = getAlmanacSprite(almanacIndex);
        int spriteSize = 112;
        int spriteX = 78;
        int spriteY = 138;

        if (sprite != null) {
            drawAlmanacSprite(g2, sprite, spriteX, spriteY, spriteSize);
        }

        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 28));
        drawAlmanacText(g2, almanacTitles.get(almanacIndex), 230, 105);

        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 16));
        drawWrappedAlmanacText(g2, almanacBodies.get(almanacIndex), 230, 145, getWidth() - 270, 25);

        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 15));
        drawCenteredAlmanacText(g2,
                (almanacIndex + 1) + "/" + almanacTitles.size() + "   LEFT/RIGHT PAGE   ESC BACK",
                getWidth() / 2,
                getHeight() - 58);
    }

    public void drawAlmanacSprite(Graphics2D g2, BufferedImage sprite, int x, int y, int size) {
        boolean flipped = almanacIndex != 0 && almanacIndex != 11 && getMenuAnimationFrame(2) == 1;

        if (flipped) {
            g2.drawImage(sprite, x + size, y, -size, size, null);
        } else {
            g2.drawImage(sprite, x, y, size, size, null);
        }
    }

    public BufferedImage getAlmanacSprite(int index) {
        int frame = getMenuAnimationFrame(2);

        if (index == 0) {
            return pacSprites[frame];
        }
        if (index >= 1 && index <= 4) {
            return ghostSprites[index - 1];
        }
        if (index == 5) {
            return ghostCloneSprite;
        }
        if (index == 6) {
            return ghostLaserSprite;
        }
        if (index == 7) {
            return ghostBonusSprite;
        }
        if (index == 8) {
            return ghostSpeedSprite;
        }
        if (index == 9) {
            return ghostFireSprite;
        }
        if (index == 10) {
            return ghostBombSprites[0];
        }
        if (index == 11) {
            return ghostMagnetSprites[frame];
        }

        return ghostSprites[0];
    }

    public int getMenuAnimationFrame(int frameCount) {
        if (frameCount <= 1) {
            return 0;
        }

        return (int) ((System.currentTimeMillis() / 180) % frameCount);
    }

    public void drawWrappedAlmanacText(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics metrics = g2.getFontMetrics();
        int currentY = y;
        String[] paragraphs = text.split("\\R\\s*\\R");

        for (String paragraph : paragraphs) {
            String[] words = paragraph.replace('\n', ' ').trim().split("\\s+");
            String line = "";

            for (String word : words) {
                String candidate = line.isEmpty() ? word : line + " " + word;

                if (metrics.stringWidth(candidate) > maxWidth && !line.isEmpty()) {
                    drawAlmanacText(g2, line, x, currentY);
                    currentY += lineHeight;
                    line = word;
                } else {
                    line = candidate;
                }
            }

            if (!line.isEmpty()) {
                drawAlmanacText(g2, line, x, currentY);
                currentY += lineHeight;
            }

            currentY += lineHeight / 2;
        }
    }

    public void drawCenteredAlmanacText(Graphics2D g2, String text, int centerX, int y) {
        int textWidth = g2.getFontMetrics().stringWidth(text);
        drawAlmanacText(g2, text, centerX - textWidth / 2, y);
    }

    public void drawAlmanacText(Graphics2D g2, String text, int x, int y) {
        g2.setColor(Color.WHITE);
        g2.drawString(text, x - 1, y);
        g2.drawString(text, x + 1, y);
        g2.drawString(text, x, y - 1);
        g2.drawString(text, x, y + 1);
        g2.setColor(Color.BLACK);
        g2.drawString(text, x, y);
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
        draftGhostSpeed = ghostSpeed;
        draftPlayerSpeed = playerSpeed;
        draftGhostSpawnSeconds = ghostSpawnInterval / framesPerSecond;
        draftPelletSeconds = powerPelletDuration / framesPerSecond;
        draftPowerSeconds = powerUpDuration / framesPerSecond;
        draftSpecialGhostChancePercent = specialGhostChancePercent;
        draftNoPowerMode = noPowerMode;
        draftMazeWidth = normalizeOddInt(maxScreenCol, 11, 51);
        draftMazeHeight = normalizeOddInt(maxScreenRow, 11, 51);
        optionChoice = 0;
        optionActionChoice = 0;
        screenState = STATE_OPTIONS;
    }

    public void saveOptions() {
        ghostSpeed = draftGhostSpeed;
        playerSpeed = draftPlayerSpeed;
        ghostSpawnInterval = draftGhostSpawnSeconds * framesPerSecond;
        powerPelletDuration = draftPelletSeconds * framesPerSecond;
        powerUpDuration = draftPowerSeconds * framesPerSecond;
        specialGhostChancePercent = draftSpecialGhostChancePercent;
        noPowerMode = draftNoPowerMode;
        maxScreenCol = normalizeOddInt(draftMazeWidth, 11, 51);
        maxScreenRow = normalizeOddInt(draftMazeHeight, 11, 51);
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
        if (isMoveUpKey(keyCode) || isMoveLeftKey(keyCode)) {
            menuChoice = (menuChoice + 4) % 5;
        } else if (isMoveDownKey(keyCode) || isMoveRightKey(keyCode)) {
            menuChoice = (menuChoice + 1) % 5;
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            if (menuChoice == 0) {
                startNewRun();
            } else if (menuChoice == 1) {
                openOptions();
            } else if (menuChoice == 2) {
                almanacIndex = 0;
                screenState = STATE_ALMANAC;
            } else if (menuChoice == 3) {
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

        if (isMoveUpKey(keyCode)) {
            optionChoice = (optionChoice + optionCount - 1) % optionCount;
        } else if (isMoveDownKey(keyCode)) {
            optionChoice = (optionChoice + 1) % optionCount;
        } else if (isMoveLeftKey(keyCode)) {
            changeOption(-1);
        } else if (isMoveRightKey(keyCode)) {
            changeOption(1);
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            selectOption();
        }
    }

    public void changeOption(int direction) {
        if (optionChoice == 0) {
            draftGhostSpeed = clampDouble(draftGhostSpeed + direction * 0.5, 1, 10);
        } else if (optionChoice == 1) {
            draftPlayerSpeed = clampDouble(draftPlayerSpeed + direction * 0.5, 1, 10);
        } else if (optionChoice == 2) {
            draftGhostSpawnSeconds = clampInt(draftGhostSpawnSeconds + direction, 1, 60);
        } else if (optionChoice == 3) {
            draftPelletSeconds = clampInt(draftPelletSeconds + direction, 1, 20);
        } else if (optionChoice == 4) {
            draftPowerSeconds = clampInt(draftPowerSeconds + direction, 1, 20);
        } else if (optionChoice == 5) {
            draftSpecialGhostChancePercent = clampInt(draftSpecialGhostChancePercent + direction * 5, 0, 100);
        } else if (optionChoice == 6) {
            draftNoPowerMode = !draftNoPowerMode;
        } else if (optionChoice == 7) {
            draftMazeWidth = normalizeOddInt(draftMazeWidth + direction * 2, 11, 51);
        } else if (optionChoice == 8) {
            draftMazeHeight = normalizeOddInt(draftMazeHeight + direction * 2, 11, 51);
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

    public int normalizeOddInt(int value, int min, int max) {
        int normalized = clampInt(value, min, max);

        if (normalized % 2 == 0) {
            normalized += normalized < max ? 1 : -1;
        }

        return clampInt(normalized, min, max);
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
        if (powerUpType == powerBombType) {
            return "BOMB";
        }
        if (powerUpType == powerLaserType) {
            return "LAZE";
        }
        if (powerUpType == powerCloneType) {
            return "CLONE";
        }
        if (powerUpType == powerFireType) {
            return "FIRE";
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
        int x = 220;
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

        if (ghost.type == ghostMagnetType) {
            g2.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        } else if (flipped) {
            g2.drawImage(sprite, screenX + tileSize, screenY, -tileSize, tileSize, null);
        } else {
            g2.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        }
    }

    public BufferedImage getGhostSprite(Ghost ghost) {
        if (ghost.type == ghostCloneType) {
            return ghostCloneSprite;
        }
        if (ghost.type == ghostBombType) {
            if (ghost.fuseTimer > 0) {
                return ghostBombSprites[1 + (ghostAnimationCounter / animationDelay) % 2];
            }

            return ghostBombSprites[0];
        }
        if (ghost.type == ghostLaserType) {
            if (!ghost.laserActive) {
                if (ghost.chargeTimer > 0) {
                    return ghostLaserChargeSprites[0];
                }

                return ghostLaserChargeSprites[(ghostAnimationCounter / animationDelay) % ghostLaserChargeSprites.length];
            }

            return ghostLaserSprite;
        }
        if (ghost.type == ghostBonusType) {
            return ghostBonusSprite;
        }
        if (ghost.type == ghostSpeedType) {
            return ghostSpeedSprite;
        }
        if (ghost.type == ghostFireType) {
            return ghostFireSprite;
        }
        if (ghost.type == ghostMagnetType) {
            return ghostMagnetSprites[(ghostAnimationCounter / animationDelay) % ghostMagnetSprites.length];
        }
        if (ghost.type == ghostSpikeType) {
            return ghostSpikeSprites[0];
        }
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

        if (screenState == STATE_ALMANAC) {
            handleAlmanacKey(keyCode);
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
        return keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP;
    }

    public boolean isMoveDownKey(int keyCode) {
        return keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN;
    }

    public boolean isMoveLeftKey(int keyCode) {
        return keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT;
    }

    public boolean isMoveRightKey(int keyCode) {
        return keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT;
    }

    public void handleAlmanacKey(int keyCode) {
        if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            screenState = STATE_MENU;
            return;
        }

        if (almanacTitles.isEmpty()) {
            return;
        }

        if (isMoveLeftKey(keyCode) || isMoveUpKey(keyCode)) {
            almanacIndex = (almanacIndex + almanacTitles.size() - 1) % almanacTitles.size();
        } else if (isMoveRightKey(keyCode) || isMoveDownKey(keyCode)) {
            almanacIndex = (almanacIndex + 1) % almanacTitles.size();
        }
    }

    public void handleNameEntryKey(int keyCode) {
        if (isMoveLeftKey(keyCode)) {
            nameEntryIndex = (nameEntryIndex + nameEntry.length - 1) % nameEntry.length;
        } else if (isMoveRightKey(keyCode)) {
            nameEntryIndex = (nameEntryIndex + 1) % nameEntry.length;
        } else if (isMoveUpKey(keyCode)) {
            nameEntry[nameEntryIndex] = nameEntry[nameEntryIndex] == 'Z' ? 'A' : (char) (nameEntry[nameEntryIndex] + 1);
        } else if (isMoveDownKey(keyCode)) {
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
