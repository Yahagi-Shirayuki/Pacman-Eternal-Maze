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
import java.awt.RenderingHints;
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
    final int hudHeight = 96;
    int screenHeight = boardHeight + hudHeight;
    final double defaultCameraZoom = 1.0;
    final double minCameraZoom = 0.5;
    final double maxCameraZoom = 3.0;
    final double cameraZoomStep = 0.25;
    final int cameraZoomAnimationDuration = 12;
    double cameraZoom = defaultCameraZoom;
    double targetCameraZoom = defaultCameraZoom;
    double startCameraZoom = defaultCameraZoom;
    int cameraZoomAnimationFrame = cameraZoomAnimationDuration;
    
    boolean[][] maze;
    boolean[][] dots;
    boolean[][] bigDots;
    boolean[][] burnedWalls;
    int[][] blockTextureIndexes;
    int[][] wallTextureIndexes;
    int[][] debrisIndexes;
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
    int ghostPerWave = 1;
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
    final int powerIceType = 9;
    final int powerMetalType = 10;
    final int powerWaterType = 11;
    final int powerElectricType = 12;
    final int powerUpTypeCount = 13;
    final int ghostCloneType = 8;
    final int ghostLaserType = 9;
    final int ghostBonusType = 10;
    final int ghostSpeedType = 11;
    final int ghostFireType = 12;
    final int ghostMagnetType = 13;
    final int ghostBombType = 14;
    final int ghostSpikeType = 15;
    final int ghostIceType = 16;
    final int ghostCactusType = 17;
    final int ghostMetalType = 18;
    final int ghostWaveType = 19;
    final int ghostFlashType = 20;
    final int bombRadiusTiles = 3;
    final int bombExplosionFrameTime = 18;
    final int ghostSpeedRecoverTime = framesPerSecond * 2; // ghost_11 pause after it dashes into a wall.
    final int ghostLaserChargeTime = framesPerSecond * 5; // ghost_9 still charge time before it starts warning.
    final int ghostLaserWarningTime = framesPerSecond * 2; // ghost_9 flashing warning time before it fires.
    final int ghostLaserFireTime = framesPerSecond * 5; // ghost_9 active laser duration before it recharges.
    final int ghostBombFuseTime = framesPerSecond; // ghost_8 stops this long before exploding.
    final int ghostBombTriggerRadiusTiles = 2; // 2 tiles around ghost = 5x5 danger zone.
    final double ghostBombStartSpeed = 0.5;
    final double ghostBombSpeedGainPerSecond = 0.1;
    final double ghostBombMaxSpeedOverPlayer = 0.5;
    final int cloneCount = 3;
    final double cloneSpeed = 2.5;
    final int clonePelletScore = 2;
    final int afterImageSpawnInterval = 4;
    final int fireTrailDuration = framesPerSecond;
    final int ghostSpikeTrapCount = 10;
    final int bonusGhostPelletSpawnInterval = 50;
    final int iceFreezeTime = framesPerSecond / 2; // Pacman/clone ice aura: ghost freezes after this many frames.
    final int iceGhostFreezeTime = framesPerSecond; // Icy aura: Pacman freezes after this many frames.
    final int iceGhostSlowDuration = framesPerSecond * 5; // Slow duration after Pacman eats Icy.
    final double iceAuraSpeedPenalty = 1.5; // Flat speed loss for ice aura slow and Icy eat penalty.
    final int waterEffectHoldDuration = framesPerSecond;
    final int waterEffectDuration = framesPerSecond / 2; // Water shrink duration after its one-second hold.
    final double waterTileSpeedPenalty = 1.5;
    final int waterFreezeChainTime = framesPerSecond / 3; // Water-to-ice chain delay after an ice aura touches water.
    final int ghostFlashChargedTime = framesPerSecond * 5;
    final int ghostFlashExhaustedTime = framesPerSecond * 5;
    final int ghostFlashRechargeWarningTime = framesPerSecond; // Flashy uses ghostpikaim_* this long before recharging.
    final int ghostFlashElectricRadiusTiles = 1; // Flashy electric area: 2 means a 5x5 tile aura. Lower this to nerf the shock zone.
    final double ghostFlashSpeedBehindPlayer = 1.0; // Flashy moving speed: getPlayerSpeed() - this value. Raise this to slow him down.
    final double cactusSpikeSpeed = 4.0; // Cacty projectile speed. Try 4.0 here for faster cactus spikes.
    final int decalAshType = 0;
    final int decalBloodType = 1;
    final int ghostKillSoundEat = 0;
    final int ghostKillSoundFire = 1;
    final int ghostKillSoundSpike = 2;
    final int laserWidth = 4;
    final int blockTextureFileCount = 7;
    final int wallTextureFileCount = 7;
    final int debrisFileCount = 4;
    int specialGhostChancePercent = 10;
    int specialGhostPowerDropChancePercent = 20;
    final int minGhostSpawnInterval = framesPerSecond * 2;
    final double speedIncreasePerLevel = 0.2;
    final double maxPlayerSpeed = 10.0;
    final double maxGhostSpeed = 9.0;
    final int maxSmallDotScore = 10;

    BufferedImage[] pacSprites = new BufferedImage[5];
    BufferedImage[] pacBombSprites = new BufferedImage[2];
    BufferedImage[] pacPowerSprites = new BufferedImage[2];
    BufferedImage[] pacMetalSprites = new BufferedImage[2];
    BufferedImage[] pacMetalHurtSprites = new BufferedImage[2];
    BufferedImage[] pacElectrocutedSprites = new BufferedImage[2];
    BufferedImage[] pacElectricSprites = new BufferedImage[2];
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
    BufferedImage[] ghostIceSprites = new BufferedImage[2];
    BufferedImage[] ghostCactusSprites = new BufferedImage[2];
    BufferedImage[] ghostMetalSprites = new BufferedImage[4];
    BufferedImage[] ghostElectrocutedSprites = new BufferedImage[2];
    BufferedImage[] ghostWaveSprites = new BufferedImage[2];
    BufferedImage[] ghostPikaChargedSprites = new BufferedImage[8];
    BufferedImage[] ghostPikaExhaustedSprites = new BufferedImage[2];
    BufferedImage[] ghostPikaAimSprites = new BufferedImage[2];
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
    BufferedImage iceCubeSprite;
    BufferedImage iceTileSprite;
    BufferedImage waterSprite;
    BufferedImage[][] waterTileSprites = new BufferedImage[2][4];
    BufferedImage[] electricTileSprites = new BufferedImage[7];
    BufferedImage cactusSpikeSprite;
    BufferedImage pacFrozeSprite;
    BufferedImage[] blockTextureSprites = new BufferedImage[blockTextureFileCount];
    int blockTextureCount = 0;
    BufferedImage[] wallTextureSprites = new BufferedImage[wallTextureFileCount];
    int wallTextureCount = 0;
    BufferedImage[] debrisSprites = new BufferedImage[debrisFileCount];
    int debrisCount = 0;
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
    int optionCategory = -1;
    int optionChoice = 0;
    int optionActionChoice = 0;
    int almanacIndex = 0;
    ArrayList<String> almanacTitles = new ArrayList<>();
    ArrayList<String> almanacBodies = new ArrayList<>();
    final File scoreFile = new File("playerscore.sav");
    final File optionFile = new File("option.sav");
    String[] highScoreNames = { "DEV", "PRO", "NUB" };
    int[] highScoreValues = { 999999, 50000, 1000 };
    char[] nameEntry = { 'A', 'A', 'A' };
    int nameEntryIndex = 0;
    int pendingFinalScore = 0;
    boolean finalScoreHandled = false;

    double draftGhostSpeed = ghostSpeed;
    double draftPlayerSpeed = playerSpeed;
    int draftGhostSpawnSeconds = 10;
    int draftGhostPerWave = ghostPerWave;
    int draftPelletSeconds = 5;
    int draftPowerSeconds = 5;
    int draftSpecialGhostChancePercent = specialGhostChancePercent;
    int draftSpecialGhostPowerDropChancePercent = specialGhostPowerDropChancePercent;
    boolean noPowerMode = false;
    boolean noSuperGhostMode = false;
    boolean draftNoPowerMode = false;
    boolean draftNoSuperGhostMode = false;
    int draftMazeWidth = maxScreenCol;
    int draftMazeHeight = maxScreenRow;
    int masterVolume = 100;
    int musicVolume = 100;
    int sfxVolume = 100;
    int draftMasterVolume = masterVolume;
    int draftMusicVolume = musicVolume;
    int draftSfxVolume = sfxVolume;
    boolean showHudScore = true;
    boolean showHudTime = true;
    boolean showHudActivePower = true;
    boolean showHudBoardState = true;
    boolean showHudPelletCount = true;
    boolean showHudGhostCount = true;
    boolean draftShowHudScore = showHudScore;
    boolean draftShowHudTime = showHudTime;
    boolean draftShowHudActivePower = showHudActivePower;
    boolean draftShowHudBoardState = showHudBoardState;
    boolean draftShowHudPelletCount = showHudPelletCount;
    boolean draftShowHudGhostCount = showHudGhostCount;
    final Color menuTextColor = new Color(0xff7f00);

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
    boolean playerFrozenDeath = false;
    boolean playerElectrocutedDeath = false;
    boolean playerElectrocutedAshVisible = false;
    boolean pacMetalDamaged = false;
    boolean electricKeyHeld = false;
    boolean electricityChanneling = false;
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
    int playerIceGhostExposureTimer = 0;
    int iceGhostSlowTimer = 0;
    int ghostSpawnTimer = 0;
    int bombExplosionTimer = 0;
    double bombExplosionMinX = 0;
    double bombExplosionMinY = 0;
    double bombExplosionMaxX = 0;
    double bombExplosionMaxY = 0;
    ArrayList<int[]> bombExplosionTiles = new ArrayList<>();
    int ghostEatScore = 200;
    int boardGhostDelayTimer = 0;
    int carriedGhostSpawnTimer = 0;
    int warningPortalX = -1;
    int warningPortalY = -1;
    ArrayList<int[]> warningPortals = new ArrayList<>();
    int smallDotsTowardPowerUp = 0;
    int initialPelletCount = 0;
    boolean boardHalfSoundPlayed = false;
    boolean boardFullClearAwarded = false;
    boolean powerPelletWarningPlayed = false;
    int consecutiveBoardClears = 0;
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
    ArrayList<FrozenGhost> frozenGhosts = new ArrayList<>();
    ArrayList<IceTile> iceTiles = new ArrayList<>();
    ArrayList<WaterEffect> waterEffects = new ArrayList<>();
    ArrayList<WaterTile> waterTiles = new ArrayList<>();
    ArrayList<ElectricTile> electricTiles = new ArrayList<>();
    ArrayList<CactusSpikeProjectile> cactusSpikeProjectiles = new ArrayList<>();
    ArrayList<GhostDeathEffect> ghostDeathEffects = new ArrayList<>();
    ArrayList<VisualDecal> visualDecals = new ArrayList<>();
    Camera camera = new Camera();
    SoundManager soundManager = new SoundManager();

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
        loadOptions();
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
                    updateWaterEffects();
                    updateGhosts();
                    updateCactusSpikeProjectiles();
                    updateIceEffects();
                    electrocuteGhostsInElectricTiles();
                    checkLaserGhostHits();
                    checkPacCloneLaserHits();
                    checkGhostLaserHits();
                    checkFireTrailCollisions();
                    checkPlayerElectricTileCollision();
                    checkFrozenGhostCollisions();
                    checkPacCloneGhostCollisions();
                }

                updateGhostDeathEffects();
                updateAfterImages();

                if (!playerDead) {
                    checkGhostSpikeTrapCollision();
                    checkGhostCollision();
                }
            }

            updateCameraZoomAnimation();
            syncContinuousSoundEffects();

            repaint();
            soundManager.update();

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
        boardGraphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        boardGraphics.translate(0, hudHeight);
        boardGraphics.scale(cameraZoom, cameraZoom);
        boardGraphics.translate(0, -hudHeight);
        boardGraphics.setClip(
                0,
                hudHeight,
                (int) Math.ceil(getVisibleViewportWidth()),
                (int) Math.ceil(getVisibleViewportBoardHeight()));
        drawOuterWall(boardGraphics);
        drawMaze(boardGraphics);
        drawDebris(boardGraphics);
        drawVisualDecals(boardGraphics);
        drawIceTiles(boardGraphics);
        drawWaterTiles(boardGraphics);
        drawWaterEffects(boardGraphics);
        drawElectricTiles(boardGraphics);
        drawDots(boardGraphics);
        drawFruits(boardGraphics);
        drawPowerUps(boardGraphics);
        drawSpikeTraps(boardGraphics);
        drawFireTrails(boardGraphics);
        drawCactusSpikeProjectiles(boardGraphics);
        drawGhostSpikeTraps(boardGraphics);
        drawAfterImages(boardGraphics);
        drawExitMarkers(boardGraphics);
        drawSpawnWarning(boardGraphics);
        drawGhosts(boardGraphics);
        drawGhostDeathEffects(boardGraphics);
        drawGhostLasers(boardGraphics);
        drawLaser(boardGraphics);
        drawPacCloneLasers(boardGraphics);
        drawBombExplosion(boardGraphics);
        drawMagnetAuras(boardGraphics);
        drawIceAuras(boardGraphics);
        drawFrozenGhosts(boardGraphics);
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
            g2.drawLine(x, hudHeight, x, hudHeight + (int) Math.round(getVisibleViewportBoardHeight()));
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
                getVisibleViewportWidth(),
                getVisibleViewportBoardHeight());
    }

    public int getViewportBoardHeight() {
        return Math.max(tileSize, getHeight() - hudHeight);
    }

    public double getVisibleViewportWidth() {
        return Math.max(tileSize, getWidth() / cameraZoom);
    }

    public double getVisibleViewportBoardHeight() {
        return Math.max(tileSize, getViewportBoardHeight() / cameraZoom);
    }

    public int worldToScreenX(double worldX) {
        return (int) Math.round(worldX - camera.viewX);
    }

    public int worldToScreenY(double worldY) {
        return hudHeight + (int) Math.round(getVisibleViewportBoardHeight() - tileSize - (worldY - camera.viewY));
    }

    public int worldBoundaryToScreenY(double worldY) {
        return hudHeight + (int) Math.round(getVisibleViewportBoardHeight() - (worldY - camera.viewY));
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

    public void resetBoardPelletProgress() {
        initialPelletCount = getRemainingPelletCount();
        boardHalfSoundPlayed = false;
        boardFullClearAwarded = false;
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
            pacPowerSprites[0] = loadOptionalSprite("res/sprite/pacpower_0.png", pacSprites[0]);
            pacPowerSprites[1] = loadOptionalSprite("res/sprite/pacpower_1.png", pacSprites[1]);
            pacMetalSprites[0] = loadOptionalSprite("res/sprite/pacmetal_0.png", pacSprites[0]);
            pacMetalSprites[1] = loadOptionalSprite("res/sprite/pacmetal_1.png", pacSprites[1]);
            pacMetalHurtSprites[0] = loadOptionalSprite("res/sprite/pacmetalhurt_0.png", pacMetalSprites[0]);
            pacMetalHurtSprites[1] = loadOptionalSprite("res/sprite/pacmetalhurt_1.png", pacMetalSprites[1]);
            pacElectrocutedSprites[0] = loadOptionalSprite("res/sprite/pacelectrocuted_0.png", pacMetalHurtSprites[0]);
            pacElectrocutedSprites[1] = loadOptionalSprite("res/sprite/pacelectrocuted_1.png", pacElectrocutedSprites[0]);
            pacElectricSprites[0] = loadOptionalSprite("res/sprite/pactricity_0.png", pacSprites[0]);
            pacElectricSprites[1] = loadOptionalSprite("res/sprite/pactricity_1.png", pacElectricSprites[0]);
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
            ghostIceSprites[0] = ImageIO.read(new File("res/sprite/ghostice_0.png"));
            ghostIceSprites[1] = ImageIO.read(new File("res/sprite/ghostice_1.png"));
            ghostCactusSprites[0] = ImageIO.read(new File("res/sprite/ghostcactus_0.png"));
            ghostCactusSprites[1] = ImageIO.read(new File("res/sprite/ghostcactus_1.png"));
            ghostMetalSprites[0] = loadOptionalSprite("res/sprite/ghostmetal_0.png", ghostSprites[0]);
            ghostMetalSprites[1] = loadOptionalSprite("res/sprite/ghostmetal_1.png", ghostMetalSprites[0]);
            ghostMetalSprites[2] = loadOptionalSprite("res/sprite/ghostmetal_2.png", ghostMetalSprites[0]);
            ghostMetalSprites[3] = loadOptionalSprite("res/sprite/ghostmetal_3.png", ghostMetalSprites[1]);
            ghostElectrocutedSprites[0] = loadOptionalSprite("res/sprite/ghostelectrocuted_0.png", ghostMetalSprites[0]);
            ghostElectrocutedSprites[1] = loadOptionalSprite("res/sprite/ghostelectrocuted_1.png", ghostElectrocutedSprites[0]);
            ghostWaveSprites[0] = loadOptionalSprite("res/sprite/ghostwave_0.png", ghostSprites[0]);
            ghostWaveSprites[1] = loadOptionalSprite("res/sprite/ghostwave_1.png", ghostWaveSprites[0]);
            for (int frame = 0; frame < ghostPikaChargedSprites.length; frame++) {
                ghostPikaChargedSprites[frame] = loadOptionalSprite("res/sprite/ghostpikacharged_" + frame + ".png", ghostWaveSprites[0]);
            }
            ghostPikaExhaustedSprites[0] = loadOptionalSprite("res/sprite/ghostpikaexhausted_0.png", ghostPikaChargedSprites[0]);
            ghostPikaExhaustedSprites[1] = loadOptionalSprite("res/sprite/ghostpikaexhausted_1.png", ghostPikaExhaustedSprites[0]);
            ghostPikaAimSprites[0] = loadOptionalSprite("res/sprite/ghostpikaim_0.png", ghostPikaExhaustedSprites[0]);
            ghostPikaAimSprites[1] = loadOptionalSprite("res/sprite/ghostpikaim_1.png", ghostPikaAimSprites[0]);
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
            powerUpSprites[9] = ImageIO.read(new File("res/sprite/pow_9.png"));
            powerUpSprites[10] = ImageIO.read(new File("res/sprite/pow_10.png"));
            powerUpSprites[11] = ImageIO.read(new File("res/sprite/pow_11.png"));
            powerUpSprites[12] = ImageIO.read(new File("res/sprite/pow_12.png"));
            spikeSprites[0] = ImageIO.read(new File("res/sprite/spike_0.png"));
            spikeSprites[1] = ImageIO.read(new File("res/sprite/spike_1.png"));
            decalSprites[decalAshType] = ImageIO.read(new File("res/sprite/ash.png"));
            decalSprites[decalBloodType] = ImageIO.read(new File("res/sprite/blood.png"));
            iceCubeSprite = ImageIO.read(new File("res/sprite/icecube.png"));
            iceTileSprite = ImageIO.read(new File("res/sprite/icetile.png"));
            waterSprite = ImageIO.read(new File("res/sprite/water.png"));
            for (int frame = 0; frame < waterTileSprites[0].length; frame++) {
                waterTileSprites[0][frame] = ImageIO.read(new File("res/sprite/water0_" + frame + ".png"));
                waterTileSprites[1][frame] = ImageIO.read(new File("res/sprite/water1_" + frame + ".png"));
            }
            for (int frame = 0; frame < electricTileSprites.length; frame++) {
                electricTileSprites[frame] = ImageIO.read(new File("res/sprite/electrictile_" + frame + ".png"));
            }
            cactusSpikeSprite = loadOptionalSprite(
                    "res/sprite/cactusspike.png",
                    loadOptionalSprite("res/sprite/castusspike.png", spikeSprites[0]));
            pacFrozeSprite = ImageIO.read(new File("res/sprite/pacfroze.png"));
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
        debrisCount = loadTextureSprites("debris_", debrisSprites);
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

    public void loadOptions() {
        if (!optionFile.exists()) {
            writeOptions();
            return;
        }

        try {
            List<String> lines = Files.readAllLines(optionFile.toPath(), StandardCharsets.UTF_8);

            for (String line : lines) {
                String[] parts = line.split("=", 2);

                if (parts.length != 2) {
                    continue;
                }

                applyOptionValue(parts[0].trim(), parts[1].trim());
            }

            normalizeLoadedOptions();
            applyAudioVolumes(masterVolume, musicVolume, sfxVolume);
            tunnelY = maxScreenRow / 2;
            updatePanelSize();
        } catch (IOException | NumberFormatException e) {
            normalizeLoadedOptions();
            writeOptions();
        }
    }

    public void applyOptionValue(String key, String value) {
        if (key.equals("ghostSpeed")) {
            ghostSpeed = Double.parseDouble(value);
        } else if (key.equals("playerSpeed")) {
            playerSpeed = Double.parseDouble(value);
        } else if (key.equals("ghostSpawnSeconds")) {
            ghostSpawnInterval = Integer.parseInt(value) * framesPerSecond;
        } else if (key.equals("ghostPerWave")) {
            ghostPerWave = Integer.parseInt(value);
        } else if (key.equals("powerPelletSeconds")) {
            powerPelletDuration = Integer.parseInt(value) * framesPerSecond;
        } else if (key.equals("powerUpSeconds")) {
            powerUpDuration = Integer.parseInt(value) * framesPerSecond;
        } else if (key.equals("specialGhostChancePercent")) {
            specialGhostChancePercent = Integer.parseInt(value);
        } else if (key.equals("specialGhostPowerDropChancePercent")) {
            specialGhostPowerDropChancePercent = Integer.parseInt(value);
        } else if (key.equals("noPowerMode")) {
            noPowerMode = Boolean.parseBoolean(value);
        } else if (key.equals("noSuperGhostMode")) {
            noSuperGhostMode = Boolean.parseBoolean(value);
        } else if (key.equals("mazeWidth")) {
            maxScreenCol = Integer.parseInt(value);
        } else if (key.equals("mazeHeight")) {
            maxScreenRow = Integer.parseInt(value);
        } else if (key.equals("masterVolume")) {
            masterVolume = Integer.parseInt(value);
        } else if (key.equals("musicVolume")) {
            musicVolume = Integer.parseInt(value);
        } else if (key.equals("sfxVolume")) {
            sfxVolume = Integer.parseInt(value);
        } else if (key.equals("showHudScore")) {
            showHudScore = Boolean.parseBoolean(value);
        } else if (key.equals("showHudTime")) {
            showHudTime = Boolean.parseBoolean(value);
        } else if (key.equals("showHudActivePower")) {
            showHudActivePower = Boolean.parseBoolean(value);
        } else if (key.equals("showHudBoardState")) {
            showHudBoardState = Boolean.parseBoolean(value);
        } else if (key.equals("showHudPelletCount")) {
            showHudPelletCount = Boolean.parseBoolean(value);
        } else if (key.equals("showHudGhostCount")) {
            showHudGhostCount = Boolean.parseBoolean(value);
        }
    }

    public void normalizeLoadedOptions() {
        ghostSpeed = clampDouble(ghostSpeed, 1, 10);
        playerSpeed = clampDouble(playerSpeed, 1, 10);
        ghostSpawnInterval = clampInt(ghostSpawnInterval / framesPerSecond, 1, 60) * framesPerSecond;
        ghostPerWave = clampInt(ghostPerWave, 1, 4);
        powerPelletDuration = clampInt(powerPelletDuration / framesPerSecond, 1, 20) * framesPerSecond;
        powerUpDuration = clampInt(powerUpDuration / framesPerSecond, 1, 20) * framesPerSecond;
        specialGhostChancePercent = clampInt(specialGhostChancePercent, 0, 100);
        specialGhostPowerDropChancePercent = clampInt(specialGhostPowerDropChancePercent, 0, 100);
        maxScreenCol = normalizeOddInt(maxScreenCol, 11, 51);
        maxScreenRow = normalizeOddInt(maxScreenRow, 11, 51);
        masterVolume = clampInt(masterVolume, 0, 100);
        musicVolume = clampInt(musicVolume, 0, 100);
        sfxVolume = clampInt(sfxVolume, 0, 100);
    }

    public void writeOptions() {
        ArrayList<String> lines = new ArrayList<>();

        lines.add("ghostSpeed=" + ghostSpeed);
        lines.add("playerSpeed=" + playerSpeed);
        lines.add("ghostSpawnSeconds=" + (ghostSpawnInterval / framesPerSecond));
        lines.add("ghostPerWave=" + ghostPerWave);
        lines.add("powerPelletSeconds=" + (powerPelletDuration / framesPerSecond));
        lines.add("powerUpSeconds=" + (powerUpDuration / framesPerSecond));
        lines.add("specialGhostChancePercent=" + specialGhostChancePercent);
        lines.add("specialGhostPowerDropChancePercent=" + specialGhostPowerDropChancePercent);
        lines.add("noPowerMode=" + noPowerMode);
        lines.add("noSuperGhostMode=" + noSuperGhostMode);
        lines.add("mazeWidth=" + maxScreenCol);
        lines.add("mazeHeight=" + maxScreenRow);
        lines.add("masterVolume=" + masterVolume);
        lines.add("musicVolume=" + musicVolume);
        lines.add("sfxVolume=" + sfxVolume);
        lines.add("showHudScore=" + showHudScore);
        lines.add("showHudTime=" + showHudTime);
        lines.add("showHudActivePower=" + showHudActivePower);
        lines.add("showHudBoardState=" + showHudBoardState);
        lines.add("showHudPelletCount=" + showHudPelletCount);
        lines.add("showHudGhostCount=" + showHudGhostCount);

        try {
            Files.write(optionFile.toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not write option.sav.", e);
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
        playerFrozenDeath = false;
        playerElectrocutedDeath = false;
        playerElectrocutedAshVisible = false;
        pacMetalDamaged = false;
        electricKeyHeld = false;
        electricityChanneling = false;
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
        playerIceGhostExposureTimer = 0;
        iceGhostSlowTimer = 0;
        ghostSpawnTimer = 0;
        clearBombExplosionEffect();
        ghostEatScore = 200;
        pendingCarriedGhosts.clear();
        boardGhostDelayTimer = 0;
        carriedGhostSpawnTimer = 0;
        clearWarningPortal();
        smallDotsTowardPowerUp = 0;
        initialPelletCount = 0;
        boardHalfSoundPlayed = false;
        boardFullClearAwarded = false;
        powerPelletWarningPlayed = false;
        consecutiveBoardClears = 0;
        Arrays.fill(powerUpTimers, 0);
        collectedFruits = new int[4];
        finalScoreHandled = false;
        pendingFinalScore = 0;
        nameEntry = new char[] { 'A', 'A', 'A' };
        nameEntryIndex = 0;
        powerMode = false;
        boardClear = false;
        soundManager.stopLaser();

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
        resetBoardPelletProgress();
        resetBoardVisuals();
        fruits.clear();
        powerUps.clear();
        spikeTraps.clear();
        ghostSpikeTraps.clear();
        frozenGhosts.clear();
        iceTiles.clear();
        waterEffects.clear();
        waterTiles.clear();
        electricTiles.clear();
        cactusSpikeProjectiles.clear();
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
        initializeSpawnedGhostState(ghost);
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
        spawnGhostThroughPortal(tileX, tileY, transferData, true);
    }

    public void spawnGhostThroughPortal(int tileX, int tileY, GhostTransferData transferData, boolean clearWarningAfterSpawn) {
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
        initializeSpawnedGhostState(ghost);
        ghosts.add(ghost);
        if (clearWarningAfterSpawn) {
            clearWarningPortal();
        }
    }

    public void spawnNaturalGhostWave() {
        int waveCount = clampInt(ghostPerWave, 1, 4);
        chooseWarningPortals(waveCount);
        spawnGhostWave(null, waveCount);
    }

    public void spawnCarriedGhostWave() {
        int waveCount = Math.min(clampInt(ghostPerWave, 1, 4), pendingCarriedGhosts.size());
        ArrayList<GhostTransferData> transferData = new ArrayList<>();

        for (int i = 0; i < waveCount; i++) {
            transferData.add(pendingCarriedGhosts.remove(0));
        }

        chooseWarningPortals(waveCount);
        spawnGhostWave(transferData, waveCount);
    }

    public void spawnGhostWave(ArrayList<GhostTransferData> transferData, int waveCount) {
        if (warningPortals.isEmpty()) {
            chooseWarningPortals(waveCount);
        }

        int spawnCount = Math.min(waveCount, warningPortals.size());

        for (int i = 0; i < spawnCount; i++) {
            int[] portal = warningPortals.get(i);
            GhostTransferData carriedGhost = transferData == null ? null : transferData.get(i);
            spawnGhostThroughPortal(portal[0], portal[1], carriedGhost, false);
        }

        clearWarningPortal();
    }

    public void initializeSpawnedGhostState(Ghost ghost) {
        if (ghost.type == ghostLaserType) {
            resetLaserGhostCycle(ghost);
        } else if (ghost.type == ghostFlashType) {
            resetFlashGhostCycle(ghost);
        }
    }

    public ArrayList<GhostTransferData> getGhostTransferData() {
        ArrayList<GhostTransferData> transferData = new ArrayList<>();

        for (Ghost ghost : ghosts) {
            transferData.add(new GhostTransferData(ghost.type));
        }

        return transferData;
    }

    public int getNaturalSpawnGhostType() {
        if (noSuperGhostMode) {
            return random.nextInt(4);
        }

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
            ghostFireType,
            ghostIceType,
            ghostCactusType,
            ghostMetalType,
            ghostWaveType,
            ghostFlashType
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
        chooseWarningPortals(1);
    }

    public void chooseWarningPortals(int count) {
        if (!warningPortals.isEmpty()) {
            return;
        }

        int[][] portals = getPortalTiles();
        ArrayList<int[]> availablePortals = new ArrayList<>();

        for (int[] portal : portals) {
            availablePortals.add(new int[] { portal[0], portal[1] });
        }

        int portalCount = clampInt(count, 1, availablePortals.size());

        while (warningPortals.size() < portalCount && !availablePortals.isEmpty()) {
            int index = random.nextInt(availablePortals.size());
            warningPortals.add(availablePortals.remove(index));
        }

        if (!warningPortals.isEmpty()) {
            warningPortalX = warningPortals.get(0)[0];
            warningPortalY = warningPortals.get(0)[1];
        }
    }

    public void clearWarningPortal() {
        warningPortalX = -1;
        warningPortalY = -1;
        warningPortals.clear();
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

        checkPacmetalWaterElectrocution();
    }

    public void updateGameTimers() {
        if (!gameStarted) {
            return;
        }

        elapsedFrames++;
        updatePowerUpTimers();
        updateElectricityChanneling();
        updateBombExplosionEffect();
        updateSpikeTraps();
        updateGhostSpikeTraps();

        if (!boardFullClearAwarded) {
            updateBoardGhostSpawns();

            if (boardGhostDelayTimer == 0 && pendingCarriedGhosts.isEmpty()) {
                ghostSpawnTimer++;
            }

            if (boardGhostDelayTimer == 0 && pendingCarriedGhosts.isEmpty()
                    && ghostSpawnTimer >= getLevelGhostSpawnInterval() - ghostSpawnWarningTime) {
                chooseWarningPortals(ghostPerWave);
            }

            if (boardGhostDelayTimer == 0 && pendingCarriedGhosts.isEmpty()
                    && ghostSpawnTimer >= getLevelGhostSpawnInterval()) {
                ghostSpawnTimer = 0;
                spawnNaturalGhostWave();
            }
        }

        if (powerMode) {
            powerModeTimer--;

            if (powerModeTimer > 0 && powerModeTimer <= framesPerSecond && !powerPelletWarningPlayed) {
                playGameSoundPelletDown();
                powerPelletWarningPlayed = true;
            }

            if (powerModeTimer <= 0) {
                stopPowerMode();
            }
        }
    }

    public void syncContinuousSoundEffects() {
        soundManager.setLaserActive(isAnyLaserActiveForSound());
    }

    public boolean isAnyLaserActiveForSound() {
        if (screenState != STATE_GAME || paused || playerDead || deathAnimationDone) {
            return false;
        }

        if (isPowerUpActive(powerLaserType)) {
            return true;
        }

        for (Ghost ghost : ghosts) {
            if (ghost.type == ghostLaserType && ghost.laserActive) {
                return true;
            }
        }

        return false;
    }

    public void updatePowerUpTimers() {
        for (int i = 0; i < powerUpTimers.length; i++) {
            if (powerUpTimers[i] > 0) {
                powerUpTimers[i]--;

                if (i == powerBombType && powerUpTimers[i] == 0) {
                    triggerBombExplosion();
                } else if (i == powerCloneType && powerUpTimers[i] == 0) {
                    turnPacClonesToAsh();
                } else if (i == powerMetalType && powerUpTimers[i] == 0) {
                    pacMetalDamaged = false;
                }
            }
        }

        if (iceGhostSlowTimer > 0) {
            iceGhostSlowTimer--;
        }
    }

    public void updateElectricityChanneling() {
        boolean shouldChannel = electricKeyHeld
                && isPowerUpActive(powerElectricType)
                && !playerDead
                && !deathAnimationDone
                && !paused;

        if (!shouldChannel) {
            electricityChanneling = false;
            clearNonWaterElectricTiles();
            return;
        }

        electricityChanneling = true;
        directionX = 0;
        directionY = 0;
        nextDirectionX = 0;
        nextDirectionY = 0;
        targetPixelX = playerPixelX;
        targetPixelY = playerPixelY;

        powerUpTimers[powerElectricType] = Math.max(0, powerUpTimers[powerElectricType] - 4);
        if (powerUpTimers[powerElectricType] <= 0) {
            electricityChanneling = false;
            clearNonWaterElectricTiles();
            return;
        }

        rebuildElectricTiles();
        electrocuteGhostsInElectricTiles();
    }

    public void rebuildElectricTiles() {
        ArrayList<int[]> wantedTiles = new ArrayList<>();
        int centerTileX = getPlayerCenterTileX();
        int centerTileY = getPlayerCenterTileY();
        boolean touchingWater = false;

        for (int offsetX = -bombRadiusTiles; offsetX <= bombRadiusTiles; offsetX++) {
            for (int offsetY = -bombRadiusTiles; offsetY <= bombRadiusTiles; offsetY++) {
                int tileX = centerTileX + offsetX;
                int tileY = centerTileY + offsetY;

                if (tileX >= 0 && tileX < maxScreenCol && tileY >= 0 && tileY < maxScreenRow
                        && isTileInBombBlastShape(offsetX, offsetY)) {
                    wantedTiles.add(new int[] { tileX, tileY });
                    touchingWater = touchingWater || hasWaterTileAt(tileX, tileY);
                }
            }
        }

        if (touchingWater) {
            for (WaterTile waterTile : waterTiles) {
                wantedTiles.add(new int[] { waterTile.tileX, waterTile.tileY });
            }
        }

        syncElectricTiles(wantedTiles);
    }

    public void syncElectricTiles(ArrayList<int[]> wantedTiles) {
        for (int i = electricTiles.size() - 1; i >= 0; i--) {
            ElectricTile electricTile = electricTiles.get(i);
            if (!containsTile(wantedTiles, electricTile.tileX, electricTile.tileY)
                    && !hasWaterTileAt(electricTile.tileX, electricTile.tileY)) {
                electricTiles.remove(i);
            }
        }

        for (int[] tile : wantedTiles) {
            addElectricTileAt(tile[0], tile[1]);
        }
    }

    public void addElectricTileAt(int tileX, int tileY) {
        addElectricTileAt(tileX, tileY, null);
    }

    public void addElectricTileAt(int tileX, int tileY, Ghost sourceGhost) {
        if (tileX <= 0
                || tileX >= maxScreenCol - 1
                || tileY <= 0
                || tileY >= maxScreenRow - 1
                || maze[tileX][tileY]) {
            return;
        }

        ElectricTile electricTile = getElectricTileAt(tileX, tileY);
        if (electricTile != null) {
            if (electricTile.sourceGhost != sourceGhost) {
                electricTile.sourceGhost = null;
            }
            return;
        }

        electricTiles.add(new ElectricTile(tileX, tileY, random.nextInt(electricTileSprites.length), sourceGhost));
    }

    public void clearNonWaterElectricTiles() {
        for (int i = electricTiles.size() - 1; i >= 0; i--) {
            ElectricTile electricTile = electricTiles.get(i);
            if (!hasWaterTileAt(electricTile.tileX, electricTile.tileY)) {
                electricTiles.remove(i);
            }
        }
    }

    public void removeElectricTileAt(int tileX, int tileY) {
        for (int i = electricTiles.size() - 1; i >= 0; i--) {
            ElectricTile electricTile = electricTiles.get(i);
            if (electricTile.tileX == tileX && electricTile.tileY == tileY) {
                electricTiles.remove(i);
            }
        }
    }

    public boolean hasElectricTileAt(int tileX, int tileY) {
        return getElectricTileAt(tileX, tileY) != null;
    }

    public ElectricTile getElectricTileAt(int tileX, int tileY) {
        for (ElectricTile electricTile : electricTiles) {
            if (electricTile.tileX == tileX && electricTile.tileY == tileY) {
                return electricTile;
            }
        }

        return null;
    }

    public void electrocuteGhostsInElectricTiles() {
        boolean playedSound = false;

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            Ghost ghost = ghosts.get(i);
            if ((ghost.type == ghostMetalType && ghost.electrocutedFuse) || ghost.electrocutedTimer > 0) {
                continue;
            }
            ElectricTile electricTile = getElectricTileAt(getGhostCenterTileX(ghost), getGhostCenterTileY(ghost));
            if (electricTile != null) {
                if (ghost.type == ghostFlashType && electricTile.sourceGhost == ghost) {
                    continue;
                }

                if (ghost.type == ghostFlashType && ghost.flashyCharged) {
                    continue;
                }

                if (!playedSound && shouldPlayGameSound()) {
                    soundManager.playElectrocuted();
                    playedSound = true;
                }

                if (ghost.type == ghostFlashType) {
                    startFlashGhostRechargeShock(ghost);
                    continue;
                }

                electrocuteGhostAt(i);
            }
        }
    }

    public void startFlashGhostRechargeShock(Ghost ghost) {
        ghost.electrocutedTimer = framesPerSecond;
        ghost.electrocutedLargeExplosion = false;
        ghost.flashyCharged = false;
        ghost.speedDashActive = false;
        ghost.fuseTimer = 0;
        ghost.electrocutedFuse = false;
        ghost.directionX = 0;
        ghost.directionY = 0;
        ghost.targetPixelX = ghost.pixelX;
        ghost.targetPixelY = ghost.pixelY;
        ghost.path.clear();
    }

    public void chargeFlashGhostFromElectricity(Ghost ghost) {
        ghost.flashyCharged = true;
        ghost.flashyTimer = ghostFlashChargedTime;
        ghost.path.clear();
    }

    public void electrocuteGhostAt(int ghostIndex) {
        if (ghostIndex < 0 || ghostIndex >= ghosts.size()) {
            return;
        }

        Ghost ghost = ghosts.get(ghostIndex);
        ghost.electrocutedTimer = framesPerSecond;
        ghost.electrocutedLargeExplosion = ghost.type == ghostBombType || ghost.type == ghostMetalType;
        ghost.speedDashActive = false;
        ghost.fuseTimer = 0;
        ghost.electrocutedFuse = false;
        ghost.directionX = 0;
        ghost.directionY = 0;
        ghost.targetPixelX = ghost.pixelX;
        ghost.targetPixelY = ghost.pixelY;
        ghost.path.clear();
    }

    public void checkPlayerElectricTileCollision() {
        if (playerDead || deathAnimationDone || isPowerUpActive(powerElectricType)) {
            return;
        }

        if (hasElectricTileAt(getPlayerCenterTileX(), getPlayerCenterTileY())) {
            startElectrocutedDeath();
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
        bombExplosionTiles.clear();
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
        for (int cloneIndex = pacClones.size() - 1; cloneIndex >= 0; cloneIndex--) {
            if (cloneIndex >= pacClones.size()) {
                continue;
            }

            PacClone clone = pacClones.get(cloneIndex);
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
                dropPacCloneWaterTile(clone);
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

    public void updateWaterEffects() {
        for (int i = waterEffects.size() - 1; i >= 0; i--) {
            WaterEffect waterEffect = waterEffects.get(i);
            if (waterEffect.holdTimer > 0) {
                waterEffect.holdTimer--;
                continue;
            }

            waterEffect.timer--;

            if (waterEffect.timer <= 0) {
                waterEffects.remove(i);
            }
        }
    }

    public void updateCactusSpikeProjectiles() {
        for (int i = cactusSpikeProjectiles.size() - 1; i >= 0; i--) {
            CactusSpikeProjectile projectile = cactusSpikeProjectiles.get(i);
            projectile.pixelX += projectile.directionX * cactusSpikeSpeed;
            projectile.pixelY += projectile.directionY * cactusSpikeSpeed;

            if (projectile.pixelX < -tileSize
                    || projectile.pixelY < -tileSize
                    || projectile.pixelX > maxScreenCol * tileSize
                    || projectile.pixelY > maxScreenRow * tileSize) {
                cactusSpikeProjectiles.remove(i);
                continue;
            }

            int tileX = clampInt((int) ((projectile.pixelX + tileSize / 2.0) / tileSize), 0, maxScreenCol - 1);
            int tileY = clampInt((int) ((projectile.pixelY + tileSize / 2.0) / tileSize), 0, maxScreenRow - 1);

            if (!canMove(tileX, tileY)) {
                cactusSpikeProjectiles.remove(i);
                continue;
            }

            if (playerIntersectsRect(
                    projectile.pixelX,
                    projectile.pixelY,
                    projectile.pixelX + tileSize,
                    projectile.pixelY + tileSize)) {
                playGameSoundSpikeKill();
                startDeathAnimation();
                cactusSpikeProjectiles.remove(i);
                continue;
            }

            if (destroyPacCloneHitByCactusSpike(projectile)) {
                cactusSpikeProjectiles.remove(i);
                continue;
            }

            if (killGhostHitByCactusSpike(projectile)) {
                cactusSpikeProjectiles.remove(i);
            }
        }
    }

    public boolean destroyPacCloneHitByCactusSpike(CactusSpikeProjectile projectile) {
        for (int cloneIndex = pacClones.size() - 1; cloneIndex >= 0; cloneIndex--) {
            if (pacCloneIntersectsRect(
                    pacClones.get(cloneIndex),
                    projectile.pixelX,
                    projectile.pixelY,
                    projectile.pixelX + tileSize,
                    projectile.pixelY + tileSize)) {
                playGameSoundSpikeKill();
                destroyPacCloneAt(cloneIndex);
                return true;
            }
        }

        return false;
    }

    public boolean killGhostHitByCactusSpike(CactusSpikeProjectile projectile) {
        for (int ghostIndex = ghosts.size() - 1; ghostIndex >= 0; ghostIndex--) {
            if (ghostIndex >= ghosts.size()) {
                continue;
            }

            Ghost ghost = ghosts.get(ghostIndex);
            if (ghost.type == ghostMetalType) {
                continue;
            }

            if (ghostIntersectsRect(
                    ghost,
                    projectile.pixelX,
                    projectile.pixelY,
                    projectile.pixelX + tileSize,
                    projectile.pixelY + tileSize)) {
                killGhostAt(ghostIndex, 0, true, ghostKillSoundSpike);
                return true;
            }
        }

        return false;
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

        if (boardFullClearAwarded) {
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

    public int[] findNearestUnicedFloorTile(int startX, int startY) {
        int bestDistance = Integer.MAX_VALUE;
        ArrayList<int[]> bestTiles = new ArrayList<>();

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                if ((x == startX && y == startY) || !canMove(x, y) || hasIceTileAt(x, y)) {
                    continue;
                }

                ArrayList<int[]> path = findAStarPath(startX, startY, x, y);

                if (path.isEmpty()) {
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

    public int[] findNearestUnwateredFloorTile(int startX, int startY) {
        int bestDistance = Integer.MAX_VALUE;
        ArrayList<int[]> bestTiles = new ArrayList<>();

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                if ((x == startX && y == startY) || !canMove(x, y) || hasWaterTileAt(x, y) || hasIceTileAt(x, y)) {
                    continue;
                }

                ArrayList<int[]> path = findAStarPath(startX, startY, x, y);

                if (path.isEmpty()) {
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
            chooseWarningPortals(Math.min(ghostPerWave, pendingCarriedGhosts.size()));
        }

        if (carriedGhostSpawnTimer >= carriedGhostSpawnInterval) {
            carriedGhostSpawnTimer = 0;
            spawnCarriedGhostWave();
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

        boolean hasCurrentDirection = directionX != 0 || directionY != 0;
        boolean slidingOnIce = !isMetalPacmanActive()
                && hasIceTileAt(tileX, tileY)
                && hasCurrentDirection
                && canMove(tileX + directionX, tileY + directionY);

        if (!slidingOnIce && canMove(tileX + nextDirectionX, tileY + nextDirectionY)) {
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
            dropPlayerWaterTile();
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

        if (!isMetalPacmanActive() && (iceGhostSlowTimer > 0 || isPlayerInIceGhostAura())) {
            adjustedSpeed -= iceAuraSpeedPenalty;
        }

        adjustedSpeed = Math.max(0.5, adjustedSpeed);
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
        if (ghost.type == ghostMetalType && ghost.fuseTimer > 0) {
            return 0.0;
        }
        if (ghost.type == ghostLaserType && !ghost.laserActive) {
            return 0.0;
        }
        if ((ghost.type == ghostSpeedType || ghost.type == ghostMetalType || ghost.type == ghostMagnetType)
                && ghost.speedDashActive) {
            return getPlayerSpeed() + 1.5;
        }
        if (ghost.type == ghostMagnetType) {
            return 1.0;
        }
        if (ghost.type == ghostFlashType) {
            return Math.max(0.1, getPlayerSpeed() - ghostFlashSpeedBehindPlayer);
        }

        double adjustedSpeed = Math.min(maxGhostSpeed, Math.max(0.1, getLevelGhostSpeed() + ghost.speedOffset));
        if (ghost.type != ghostMetalType && isGhostInFriendlyIceAura(ghost)) {
            adjustedSpeed -= iceAuraSpeedPenalty;
        }
        if (ghost.type != ghostWaveType && ghost.type != ghostFlashType && isGhostOnWaterTile(ghost)) {
            adjustedSpeed -= waterTileSpeedPenalty;
        }
        adjustedSpeed = Math.max(0.1, adjustedSpeed);
        return isPowerUpActive(2) ? Math.max(0.1, adjustedSpeed * 0.35) : adjustedSpeed;
    }

    public boolean isGhostOnWaterTile(Ghost ghost) {
        return hasWaterTileAt(getGhostCenterTileX(ghost), getGhostCenterTileY(ghost));
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
        boolean leftEarly = !boardFullClearAwarded;

        level++;
        applyFruitBonuses();
        if (leftEarly) {
            consecutiveBoardClears = 0;
        }
        roomInitialScore = score;
        nextFruitScore = fruitScoreInterval;
        fruitsSpawnedThisLevel = 0;
        collectedFruits = new int[4];
        boardClear = false;
        powerMode = false;
        Arrays.fill(powerUpTimers, 0);
        soundManager.stopLaser();
        powerModeTimer = 0;
        powerPelletWarningPlayed = false;
        playerIceGhostExposureTimer = 0;
        iceGhostSlowTimer = 0;
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
        resetBoardPelletProgress();
        resetBoardVisuals();
        fruits.clear();
        powerUps.clear();
        spikeTraps.clear();
        ghostSpikeTraps.clear();
        frozenGhosts.clear();
        iceTiles.clear();
        waterEffects.clear();
        waterTiles.clear();
        electricTiles.clear();
        cactusSpikeProjectiles.clear();
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
        resetDebris();
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

    public void resetDebris() {
        debrisIndexes = new int[maxScreenCol][maxScreenRow];

        for (int x = 0; x < maxScreenCol; x++) {
            for (int y = 0; y < maxScreenRow; y++) {
                debrisIndexes[x][y] = -1;
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

        checkBoardPelletProgress();
    }

    public void collectPelletAt(int tileX, int tileY, boolean countTowardPowerDrop) {
        if (dots[tileX][tileY]) {
            dots[tileX][tileY] = false;
            addScore(getSmallDotScore());
            playGameSoundEatDot();

            if (countTowardPowerDrop) {
                smallDotsTowardPowerUp++;
                checkPowerUpDrop();
            }
        }

        if (bigDots[tileX][tileY]) {
            bigDots[tileX][tileY] = false;
            addScore(getBigDotScore());
            playGameSoundEatPower();
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
            playGameSoundEatDot();
        }
    }

    public void eatPelletAtPacClone(PacClone clone) {
        int tileX = (int) (clone.pixelX / tileSize);
        int tileY = (int) (clone.pixelY / tileSize);

        if (dots[tileX][tileY]) {
            dots[tileX][tileY] = false;
            clone.path.clear();
            addScore(getClonePelletScore());
            playGameSoundEatDot();
        } else if (bigDots[tileX][tileY]) {
            bigDots[tileX][tileY] = false;
            clone.path.clear();
            addScore(getClonePelletScore());
            playGameSoundEatPower();
        }

        if (isPowerUpActive(0)) {
            collectPacCloneMagnetPellets(tileX, tileY);
        }

        checkBoardPelletProgress();
    }

    public void collectPacCloneMagnetPellets(int centerX, int centerY) {
        for (int x = centerX - 2; x <= centerX + 2; x++) {
            for (int y = centerY - 2; y <= centerY + 2; y++) {
                if (x > 0 && x < maxScreenCol - 1 && y > 0 && y < maxScreenRow - 1 && dots[x][y]) {
                    dots[x][y] = false;
                    addScore(getClonePelletScore());
                    playGameSoundEatDot();
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

        checkBoardPelletProgress();
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
            if (ghost.type == ghostBonusType) {
                ghost.pelletsCollected++;
                ghost.path.clear();
                spawnBonusGhostMilestoneGhostIfNeeded(ghost);
            }
            if (countForMagnetScore) {
                ghost.pelletsCollected++;
            }
        }

        if (!countForMagnetScore && bigDots[tileX][tileY]) {
            bigDots[tileX][tileY] = false;
            addScore(-10);
            ghost.path.clear();

            if (ghost.type == ghostBonusType) {
                ghost.pelletsCollected++;
                spawnBonusGhostMilestoneGhostIfNeeded(ghost);
                spawnBonusGhostPowerPelletGhosts(ghost);
            }
        }
    }

    public void spawnBonusGhostMilestoneGhostIfNeeded(Ghost ghost) {
        if (ghost.pelletsCollected > 0 && ghost.pelletsCollected % bonusGhostPelletSpawnInterval == 0) {
            spawnBonusGhostAt(ghost, getRandomBonusGhostSpawnType());
        }
    }

    public void spawnBonusGhostPowerPelletGhosts(Ghost ghost) {
        if (noSuperGhostMode) {
            spawnBonusGhostAt(ghost, random.nextInt(4));
            spawnBonusGhostAt(ghost, random.nextInt(4));
            return;
        }

        for (int i = 0; i < 2; i++) {
            spawnBonusGhostAt(ghost, getRandomPowerGhostType());
        }
    }

    public void spawnBonusGhostAt(Ghost sourceGhost, int ghostType) {
        int tileX = clampInt((int) Math.round(sourceGhost.pixelX / tileSize), 1, maxScreenCol - 2);
        int tileY = clampInt((int) Math.round(sourceGhost.pixelY / tileSize), 1, maxScreenRow - 2);

        int[] spawnTile = findOpenTileNear(tileX, tileY);
        addGhostAtTile(ghostType, spawnTile[0], spawnTile[1]);
    }

    public int getRandomBonusGhostSpawnType() {
        if (noSuperGhostMode) {
            return random.nextInt(4);
        }

        if (random.nextBoolean()) {
            return getRandomPowerGhostType();
        }

        return random.nextInt(4);
    }

    public int getRandomPowerGhostType() {
        if (noSuperGhostMode) {
            return random.nextInt(4);
        }

        int[] powerGhostTypes = {
            ghostCloneType,
            ghostBombType,
            ghostLaserType,
            ghostBonusType,
            ghostSpeedType,
            ghostMagnetType,
            ghostFireType,
            ghostIceType,
            ghostCactusType,
            ghostMetalType,
            ghostWaveType,
            ghostFlashType
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

    public void checkBoardPelletProgress() {
        if (initialPelletCount <= 0 || boardFullClearAwarded) {
            return;
        }

        int remainingPellets = getRemainingPelletCount();
        int clearedPellets = initialPelletCount - remainingPellets;

        if (!boardClear && clearedPellets >= getBoardExitRequirement()) {
            markBoardExitOpen();
        }

        if (remainingPellets == 0) {
            awardBoardClearBonus();
        }
    }

    public int getBoardExitRequirement() {
        return initialPelletCount / 2;
    }

    public void markBoardExitOpen() {
        if (boardClear) {
            return;
        }

        boardClear = true;
        clearWarningPortal();
        boardHalfSoundPlayed = true;
        playGameSoundBoardHalf();
    }

    public void awardBoardClearBonus() {
        if (boardFullClearAwarded) {
            return;
        }

        if (!boardClear) {
            markBoardExitOpen();
        }

        int bonus = getBoardClearBonus();
        if (bonus > 0) {
            addScore(bonus);
        }

        boardFullClearAwarded = true;
        consecutiveBoardClears++;
        playGameSoundBoardClear();
    }

    public int getBoardClearBonus() {
        return initialPelletCount * getBoardClearBonusMultiplier();
    }

    public int getBoardClearBonusMultiplier() {
        return Math.min(100, (consecutiveBoardClears + 1) * 10);
    }

    public boolean shouldPlayGameSound() {
        return screenState == STATE_GAME && gameStarted;
    }

    public void playGameSoundEatDot() {
        if (shouldPlayGameSound()) {
            soundManager.playEatDot();
        }
    }

    public void playGameSoundEatPower() {
        if (shouldPlayGameSound()) {
            soundManager.playEatPower();
        }
    }

    public void playGameSoundEatFruit() {
        if (shouldPlayGameSound()) {
            soundManager.playEatFruit();
        }
    }

    public void playGameSoundEatIce() {
        if (shouldPlayGameSound()) {
            soundManager.playEatIce();
        }
    }

    public void playGameSoundBoardHalf() {
        if (shouldPlayGameSound()) {
            soundManager.playBoardHalf();
        }
    }

    public void playGameSoundBoardClear() {
        if (shouldPlayGameSound()) {
            soundManager.playBoardClear();
        }
    }

    public void playGameSoundPelletDown() {
        if (shouldPlayGameSound()) {
            soundManager.playPelletDown();
        }
    }

    public void playGameSoundFreeze() {
        if (shouldPlayGameSound()) {
            soundManager.playFreeze();
        }
    }

    public void playGameSoundSpikeKill() {
        if (shouldPlayGameSound()) {
            soundManager.playSpikeKill();
        }
    }

    public void playGameSoundFireDie() {
        if (shouldPlayGameSound()) {
            soundManager.playFireDie();
        }
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
                playGameSoundEatFruit();
            }
        }
    }

    public void eatPowerUpAtPlayer(int tileX, int tileY) {
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);

            if (powerUp.tileX == tileX && powerUp.tileY == tileY) {
                playGameSoundEatPower();
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
                if (ghost.type == ghostBonusType) {
                    activateBonusGhostPowerUp(ghost, powerUp.type);
                } else {
                    activateGhostPowerUp(ghost, powerUp.type);
                }
                powerUps.remove(i);
                return;
            }
        }
    }

    public boolean isPoweredGhost(Ghost ghost) {
        return ghost.type >= ghostCloneType;
    }

    public void activateGhostPowerUp(Ghost ghost, int powerUpType) {
        if (noSuperGhostMode) {
            return;
        }

        if (powerUpType == 0) {
            transformGhost(ghost, ghostMagnetType);
        } else if (powerUpType == 1) {
            placeGhostSpikeTraps(powerUpDuration);
            transformGhost(ghost, ghostCactusType);
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
        } else if (powerUpType == powerIceType) {
            transformGhost(ghost, ghostIceType);
        } else if (powerUpType == powerMetalType) {
            transformGhost(ghost, ghostMetalType);
        } else if (powerUpType == powerWaterType) {
            transformGhost(ghost, ghostWaveType);
        } else if (powerUpType == powerElectricType) {
            transformGhost(ghost, ghostFlashType);
        }
    }

    public void activateBonusGhostPowerUp(Ghost ghost, int powerUpType) {
        if (noSuperGhostMode) {
            return;
        }

        if (powerUpType == 4) {
            spawnBonusGhostPowerPelletGhosts(ghost);
            return;
        }

        if (powerUpType == 1) {
            placeGhostSpikeTraps(powerUpDuration);
        }

        int spawnedGhostType = getGhostTypeForPowerUp(powerUpType);

        if (spawnedGhostType != -1) {
            spawnBonusGhostAt(ghost, spawnedGhostType);
        }
    }

    public int getGhostTypeForPowerUp(int powerUpType) {
        if (powerUpType == 0) {
            return ghostMagnetType;
        }
        if (powerUpType == 1) {
            return ghostCactusType;
        }
        if (powerUpType == 2) {
            return ghostSpeedType;
        }
        if (powerUpType == 3) {
            return ghostBonusType;
        }
        if (powerUpType == powerBombType) {
            return ghostBombType;
        }
        if (powerUpType == powerLaserType) {
            return ghostLaserType;
        }
        if (powerUpType == powerCloneType) {
            return ghostCloneType;
        }
        if (powerUpType == powerFireType) {
            return ghostFireType;
        }
        if (powerUpType == powerIceType) {
            return ghostIceType;
        }
        if (powerUpType == powerMetalType) {
            return ghostMetalType;
        }
        if (powerUpType == powerWaterType) {
            return ghostWaveType;
        }
        if (powerUpType == powerElectricType) {
            return ghostFlashType;
        }

        return -1;
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
        ghost.electrocutedFuse = false;
        ghost.speedRampTimer = 0;
        ghost.laserActive = type != ghostLaserType;
        ghost.pelletsCollected = 0;
        ghost.flashyTimer = 0;
        ghost.flashyCharged = true;
        if (type == ghostFlashType) {
            resetFlashGhostCycle(ghost);
        }
    }

    public void resetFlashGhostCycle(Ghost ghost) {
        ghost.flashyCharged = true;
        ghost.flashyTimer = ghostFlashChargedTime;
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

        if ((powerUpType == powerFireType && isPowerUpActive(powerIceType))
                || (powerUpType == powerIceType && isPowerUpActive(powerFireType))) {
            powerUpTimers[powerFireType] = 0;
            powerUpTimers[powerIceType] = 0;
            return;
        }

        if ((powerUpType == powerFireType && isPowerUpActive(powerWaterType))
                || (powerUpType == powerWaterType && isPowerUpActive(powerFireType))) {
            powerUpTimers[powerFireType] = 0;
            powerUpTimers[powerWaterType] = 0;
            return;
        }

        if (powerUpType == powerMetalType) {
            pacMetalDamaged = false;
        }

        powerUpTimers[powerUpType] += powerUpType == powerElectricType ? powerUpDuration * 2 : powerUpDuration;

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
        powerPelletWarningPlayed = false;
        ghostEatScore = 200;

        for (Ghost ghost : ghosts) {
            ghost.path.clear();
        }
    }

    public void stopPowerMode() {
        powerMode = false;
        powerModeTimer = 0;
        powerPelletWarningPlayed = false;
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

    public void dropPlayerWaterTile() {
        if (!isPowerUpActive(powerWaterType)) {
            return;
        }

        addWaterTile(getPlayerTileX(), getPlayerTileY());
    }

    public void dropPacCloneWaterTile(PacClone clone) {
        if (!isPowerUpActive(powerWaterType)) {
            return;
        }

        int tileX = clampInt((int) (clone.pixelX / tileSize), 0, maxScreenCol - 1);
        int tileY = clampInt((int) (clone.pixelY / tileSize), 0, maxScreenRow - 1);
        addWaterTile(tileX, tileY);
    }

    public void addWaterTile(int tileX, int tileY) {
        if (tileX <= 0 || tileX >= maxScreenCol - 1 || tileY <= 0 || tileY >= maxScreenRow - 1
                || maze[tileX][tileY] || hasIceTileAt(tileX, tileY)) {
            return;
        }

        removeFireTrailAt(tileX, tileY);
        clearDecalsAt(tileX, tileY);
        if (hasWaterTileAt(tileX, tileY)) {
            return;
        }

        int variation = random.nextInt(100) < 25 ? 1 : 0;
        waterTiles.add(new WaterTile(tileX, tileY, variation));
    }

    public boolean hasWaterTileAt(int tileX, int tileY) {
        for (WaterTile waterTile : waterTiles) {
            if (waterTile.tileX == tileX && waterTile.tileY == tileY) {
                return true;
            }
        }

        return false;
    }

    public boolean removeWaterTileAt(int tileX, int tileY) {
        for (int i = waterTiles.size() - 1; i >= 0; i--) {
            WaterTile waterTile = waterTiles.get(i);
            if (waterTile.tileX == tileX && waterTile.tileY == tileY) {
                waterTiles.remove(i);
                removeElectricTileAt(tileX, tileY);
                return true;
            }
        }

        return false;
    }

    public void removeFireTrailAt(int tileX, int tileY) {
        for (int i = fireTrails.size() - 1; i >= 0; i--) {
            FireTrail fireTrail = fireTrails.get(i);
            if (fireTrail.tileX == tileX && fireTrail.tileY == tileY) {
                fireTrails.remove(i);
            }
        }
    }

    public void clearDecalsAt(int tileX, int tileY) {
        for (int i = visualDecals.size() - 1; i >= 0; i--) {
            VisualDecal decal = visualDecals.get(i);
            if ((int) (decal.pixelX / tileSize) == tileX && (int) (decal.pixelY / tileSize) == tileY) {
                visualDecals.remove(i);
            }
        }
    }

    public void addFireTrail(int tileX, int tileY, boolean ghostFire) {
        if (tileX <= 0 || tileX >= maxScreenCol - 1 || tileY <= 0 || tileY >= maxScreenRow - 1 || maze[tileX][tileY]) {
            return;
        }

        if (hasWaterTileAt(tileX, tileY)) {
            return;
        }

        if (removeIceTileAt(tileX, tileY, true)) {
            return;
        }

        fireTrails.add(new FireTrail(tileX, tileY, fireTrailDuration, ghostFire));
        if (shouldPlayGameSound()) {
            soundManager.playFire();
        }
    }

    public void dropGhostIceTile(Ghost ghost) {
        if (ghost.type != ghostIceType || isGhostMoving(ghost)) {
            return;
        }

        int tileX = clampInt((int) (ghost.pixelX / tileSize), 0, maxScreenCol - 1);
        int tileY = clampInt((int) (ghost.pixelY / tileSize), 0, maxScreenRow - 1);
        if (hasWaterTileAt(tileX, tileY)) {
            freezeWaterTile(tileX, tileY, ghost);
            return;
        }

        addIceTile(tileX, tileY);
    }

    public void dropGhostWaterTile(Ghost ghost) {
        if (ghost.type != ghostWaveType || isGhostMoving(ghost)) {
            return;
        }

        int tileX = clampInt((int) (ghost.pixelX / tileSize), 0, maxScreenCol - 1);
        int tileY = clampInt((int) (ghost.pixelY / tileSize), 0, maxScreenRow - 1);
        addWaterTile(tileX, tileY);
    }

    public void addIceTile(int tileX, int tileY) {
        if (tileX <= 0 || tileX >= maxScreenCol - 1 || tileY <= 0 || tileY >= maxScreenRow - 1
                || maze[tileX][tileY] || hasIceTileAt(tileX, tileY)) {
            return;
        }

        iceTiles.add(new IceTile(tileX, tileY));
    }

    public boolean hasIceTileAt(int tileX, int tileY) {
        for (IceTile iceTile : iceTiles) {
            if (iceTile.tileX == tileX && iceTile.tileY == tileY) {
                return true;
            }
        }

        return false;
    }

    public boolean removeIceTileAt(int tileX, int tileY, boolean spawnWater) {
        for (int i = iceTiles.size() - 1; i >= 0; i--) {
            IceTile iceTile = iceTiles.get(i);
            if (iceTile.tileX == tileX && iceTile.tileY == tileY) {
                iceTiles.remove(i);
                if (spawnWater) {
                    spawnWaterEffect(tileX, tileY);
                }
                return true;
            }
        }

        return false;
    }

    public void spawnWaterEffect(int tileX, int tileY) {
        waterEffects.add(new WaterEffect(tileX, tileY, waterEffectHoldDuration, waterEffectDuration));
    }

    public void meltIceTilesInRect(double minX, double minY, double maxX, double maxY) {
        for (int i = iceTiles.size() - 1; i >= 0; i--) {
            IceTile iceTile = iceTiles.get(i);
            if (rectsIntersect(
                    iceTile.tileX * tileSize,
                    iceTile.tileY * tileSize,
                    (iceTile.tileX + 1) * tileSize,
                    (iceTile.tileY + 1) * tileSize,
                    minX,
                    minY,
                    maxX,
                    maxY)) {
                iceTiles.remove(i);
                spawnWaterEffect(iceTile.tileX, iceTile.tileY);
            }
        }
    }

    public void updateIceEffects() {
        removeFireTrailsInIceAuras();
        updateWaterFreezeChain();
        killFireGhostsInIceAuras();
        updateFriendlyIceAuras();
        updateIceGhostAuras();
    }

    public void updateWaterFreezeChain() {
        for (WaterTile waterTile : waterTiles) {
            if (waterTile.freezeTimer < 0 && isTileInAnyIceAura(waterTile.tileX, waterTile.tileY)) {
                waterTile.freezeTimer = waterFreezeChainTime;
            }
        }

        ArrayList<int[]> frozenTiles = new ArrayList<>();
        for (int i = waterTiles.size() - 1; i >= 0; i--) {
            WaterTile waterTile = waterTiles.get(i);
            if (waterTile.freezeTimer < 0) {
                continue;
            }

            waterTile.freezeTimer--;
            if (waterTile.freezeTimer <= 0) {
                frozenTiles.add(new int[] { waterTile.tileX, waterTile.tileY });
                waterTiles.remove(i);
            }
        }

        for (int[] tile : frozenTiles) {
            freezeWaterTile(tile[0], tile[1]);
        }
    }

    public void freezeWaterTile(int tileX, int tileY) {
        freezeWaterTile(tileX, tileY, null);
    }

    public void freezeWaterTile(int tileX, int tileY, Ghost ignoredGhost) {
        removeWaterTileAt(tileX, tileY);
        addIceTile(tileX, tileY);

        if (getPlayerCenterTileX() == tileX && getPlayerCenterTileY() == tileY) {
            startFrozenDeath();
        }

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            Ghost ghost = ghosts.get(i);
            if (ghost != ignoredGhost && getGhostCenterTileX(ghost) == tileX && getGhostCenterTileY(ghost) == tileY) {
                freezeGhostAt(i);
            }
        }

        for (int cloneIndex = pacClones.size() - 1; cloneIndex >= 0; cloneIndex--) {
            PacClone clone = pacClones.get(cloneIndex);
            if (getPacCloneCenterTileX(clone) == tileX && getPacCloneCenterTileY(clone) == tileY) {
                destroyPacCloneAt(cloneIndex);
            }
        }

        startAdjacentWaterFreeze(tileX, tileY);
    }

    public void startAdjacentWaterFreeze(int tileX, int tileY) {
        int[][] directions = {
            { 1, 0 },
            { -1, 0 },
            { 0, 1 },
            { 0, -1 }
        };

        for (int[] direction : directions) {
            startWaterFreezeAt(tileX + direction[0], tileY + direction[1]);
        }
    }

    public void startWaterFreezeAt(int tileX, int tileY) {
        for (WaterTile waterTile : waterTiles) {
            if (waterTile.tileX == tileX && waterTile.tileY == tileY && waterTile.freezeTimer < 0) {
                waterTile.freezeTimer = waterFreezeChainTime;
                return;
            }
        }
    }

    public void removeFireTrailsInIceAuras() {
        for (int i = fireTrails.size() - 1; i >= 0; i--) {
            FireTrail fireTrail = fireTrails.get(i);
            if (isTileInAnyIceAura(fireTrail.tileX, fireTrail.tileY)) {
                fireTrails.remove(i);
            }
        }
    }

    public void killFireGhostsInIceAuras() {
        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            Ghost ghost = ghosts.get(i);
            if (ghost.type == ghostFireType && isGhostInAnyIceAura(ghost)) {
                killGhostAt(i, 0, true);
            }
        }
    }

    public void updateFriendlyIceAuras() {
        // Pacman/clone ice aura effect on ghosts. Tune iceFreezeTime and iceAuraSpeedPenalty above.
        if (!isPowerUpActive(powerIceType)) {
            for (Ghost ghost : ghosts) {
                ghost.iceExposureTimer = 0;
            }
            return;
        }

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            Ghost ghost = ghosts.get(i);
            if (ghost.type == ghostMetalType) {
                ghost.iceExposureTimer = 0;
                continue;
            }

            if (isGhostInFriendlyIceAura(ghost)) {
                ghost.iceExposureTimer++;
                if (ghost.iceExposureTimer >= iceFreezeTime) {
                    freezeGhostAt(i);
                }
            } else {
                ghost.iceExposureTimer = 0;
            }
        }
    }

    public void updateIceGhostAuras() {
        // Icy aura effect on Pacman. Tune iceGhostFreezeTime and iceAuraSpeedPenalty above.
        if (isPlayerInIceGhostAura()) {
            playerIceGhostExposureTimer++;
            if (playerIceGhostExposureTimer >= iceGhostFreezeTime) {
                startFrozenDeath();
            }
        } else {
            playerIceGhostExposureTimer = 0;
        }
    }

    public boolean isGhostInFriendlyIceAura(Ghost ghost) {
        if (!isPowerUpActive(powerIceType)) {
            return false;
        }

        if (isEntityInBombShapeAura(ghost.pixelX, ghost.pixelY, getPlayerCenterTileX(), getPlayerCenterTileY())) {
            return true;
        }

        for (PacClone clone : pacClones) {
            if (isEntityInBombShapeAura(ghost.pixelX, ghost.pixelY, getPacCloneCenterTileX(clone), getPacCloneCenterTileY(clone))) {
                return true;
            }
        }

        return false;
    }

    public boolean isGhostInAnyIceAura(Ghost ghost) {
        return isGhostInFriendlyIceAura(ghost) || isGhostInIceGhostAura(ghost);
    }

    public boolean isGhostInIceGhostAura(Ghost targetGhost) {
        for (Ghost ghost : ghosts) {
            if (ghost != targetGhost
                    && ghost.type == ghostIceType
                    && isEntityInBombShapeAura(
                            targetGhost.pixelX,
                            targetGhost.pixelY,
                            getGhostCenterTileX(ghost),
                            getGhostCenterTileY(ghost))) {
                return true;
            }
        }

        return false;
    }

    public boolean isTileInAnyIceAura(int tileX, int tileY) {
        if (isPowerUpActive(powerIceType)) {
            if (isTileInBombShapeAura(tileX, tileY, getPlayerCenterTileX(), getPlayerCenterTileY())) {
                return true;
            }

            for (PacClone clone : pacClones) {
                if (isTileInBombShapeAura(tileX, tileY, getPacCloneCenterTileX(clone), getPacCloneCenterTileY(clone))) {
                    return true;
                }
            }
        }

        for (Ghost ghost : ghosts) {
            if (ghost.type == ghostIceType
                    && isTileInBombShapeAura(tileX, tileY, getGhostCenterTileX(ghost), getGhostCenterTileY(ghost))) {
                return true;
            }
        }

        return false;
    }

    public boolean isTileInBombShapeAura(int tileX, int tileY, int centerTileX, int centerTileY) {
        return isTileInBombBlastShape(tileX - centerTileX, tileY - centerTileY);
    }

    public boolean isPlayerInIceGhostAura() {
        for (Ghost ghost : ghosts) {
            if (ghost.type == ghostIceType
                    && isEntityInBombShapeAura(playerPixelX, playerPixelY, getGhostCenterTileX(ghost), getGhostCenterTileY(ghost))) {
                return true;
            }
        }

        return false;
    }

    public boolean isEntityInBombShapeAura(double pixelX, double pixelY, int centerTileX, int centerTileY) {
        int tileX = clampInt((int) ((pixelX + tileSize / 2.0) / tileSize), 0, maxScreenCol - 1);
        int tileY = clampInt((int) ((pixelY + tileSize / 2.0) / tileSize), 0, maxScreenRow - 1);
        return isTileInBombBlastShape(tileX - centerTileX, tileY - centerTileY);
    }

    public boolean isEntityInMagnetAura(double pixelX, double pixelY, int centerTileX, int centerTileY) {
        int tileX = clampInt((int) ((pixelX + tileSize / 2.0) / tileSize), 0, maxScreenCol - 1);
        int tileY = clampInt((int) ((pixelY + tileSize / 2.0) / tileSize), 0, maxScreenRow - 1);
        return isTileInMagnetAuraShape(tileX - centerTileX, tileY - centerTileY);
    }

    public boolean isGhostInFriendlyMagnetAura(Ghost ghost) {
        if (!isPowerUpActive(0)) {
            return false;
        }

        if (isEntityInMagnetAura(ghost.pixelX, ghost.pixelY, getPlayerCenterTileX(), getPlayerCenterTileY())) {
            return true;
        }

        for (PacClone clone : pacClones) {
            if (isEntityInMagnetAura(ghost.pixelX, ghost.pixelY, getPacCloneCenterTileX(clone), getPacCloneCenterTileY(clone))) {
                return true;
            }
        }

        return false;
    }

    public void freezeGhostAt(int ghostIndex) {
        if (ghostIndex < 0 || ghostIndex >= ghosts.size()) {
            return;
        }

        Ghost ghost = ghosts.get(ghostIndex);
        if (ghost.type == ghostMetalType) {
            return;
        }

        int tileX = getGhostCenterTileX(ghost);
        int tileY = getGhostCenterTileY(ghost);
        frozenGhosts.add(new FrozenGhost(tileX, tileY, ghost.type));
        playGameSoundFreeze();
        ghosts.remove(ghostIndex);
    }

    public void startFrozenDeath() {
        if (playerDead || deathAnimationDone) {
            return;
        }
        if (isMetalPacmanActive()) {
            return;
        }

        soundManager.playFreeze();
        soundManager.fadeOutMusicAndStop();
        playerFrozenDeath = true;
        playerElectrocutedDeath = false;
        playerElectrocutedAshVisible = false;
        playerDead = true;
        gameStarted = false;
        directionX = 0;
        directionY = 0;
        nextDirectionX = 0;
        nextDirectionY = 0;
        electricKeyHeld = false;
        electricityChanneling = false;
        electricTiles.clear();
        targetPixelX = playerPixelX;
        targetPixelY = playerPixelY;
        deathAnimationCounter = 0;
        deathFrame = 0;
        deathAnimationDone = false;
    }

    public void checkGhostCollision() {
        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            Ghost ghost = ghosts.get(i);

            if (Math.abs(playerPixelX - ghost.pixelX) < tileSize && Math.abs(playerPixelY - ghost.pixelY) < tileSize) {
                if (isMetalPacmanActive()) {
                    if (ghost.type == ghostMetalType) {
                        bouncePacmanAndMetalGhost(ghost);
                        return;
                    }

                    if (ghost.type == ghostMagnetType && ghost.speedDashActive) {
                        damagePacmetalWithMagnetyCharge();
                    }

                    if (ghost.type == ghostIceType) {
                        iceGhostSlowTimer = iceGhostSlowDuration;
                    }
                    killGhostAt(i, getGhostEatScore());
                    ghostEatScore = Math.min(1600, ghostEatScore * 2);
                    continue;
                }

                if (ghost.type == ghostCactusType) {
                    startDeathAnimation();
                    return;
                }

                if (isPowerUpActive(powerBombType)) {
                    triggerBombExplosion();
                    return;
                }

                if (powerMode) {
                    if (ghost.type == ghostMetalType) {
                        startDeathAnimation();
                        return;
                    }
                    if (ghost.type == ghostIceType) {
                        iceGhostSlowTimer = iceGhostSlowDuration;
                    }
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

    public void checkFrozenGhostCollisions() {
        for (int i = frozenGhosts.size() - 1; i >= 0; i--) {
            FrozenGhost frozenGhost = frozenGhosts.get(i);
            double minX = frozenGhost.tileX * tileSize;
            double minY = frozenGhost.tileY * tileSize;
            double maxX = minX + tileSize;
            double maxY = minY + tileSize;

            if (playerIntersectsRect(minX, minY, maxX, maxY)) {
                tryDropSpecialGhostPower(frozenGhost.type, frozenGhost.tileX, frozenGhost.tileY);
                frozenGhosts.remove(i);
                addScore(500);
                playGameSoundEatIce();
                continue;
            }

            boolean destroyed = false;
            for (int cloneIndex = pacClones.size() - 1; cloneIndex >= 0; cloneIndex--) {
                if (pacCloneIntersectsRect(pacClones.get(cloneIndex), minX, minY, maxX, maxY)) {
                    tryDropSpecialGhostPower(frozenGhost.type, frozenGhost.tileX, frozenGhost.tileY);
                    frozenGhosts.remove(i);
                    addScore(500);
                    playGameSoundEatIce();
                    destroyed = true;
                    break;
                }
            }

            if (destroyed) {
                continue;
            }

            for (Ghost ghost : ghosts) {
                if (ghostIntersectsRect(ghost, minX, minY, maxX, maxY)) {
                    tryDropSpecialGhostPower(frozenGhost.type, frozenGhost.tileX, frozenGhost.tileY);
                    frozenGhosts.remove(i);
                    playGameSoundEatIce();
                    break;
                }
            }
        }
    }

    public void startDeathAnimation() {
        if (playerDead || deathAnimationDone) {
            return;
        }
        if (isMetalPacmanActive()) {
            return;
        }

        forceStartDeathAnimation();
    }

    public void forceStartDeathAnimation() {
        if (playerDead || deathAnimationDone) {
            return;
        }

        soundManager.playDeath();
        soundManager.fadeOutMusicAndStop();
        playerFrozenDeath = false;
        playerElectrocutedDeath = false;
        playerElectrocutedAshVisible = false;
        playerDead = true;
        gameStarted = false;
        directionX = 0;
        directionY = 0;
        nextDirectionX = 0;
        nextDirectionY = 0;
        electricKeyHeld = false;
        electricityChanneling = false;
        electricTiles.clear();
        targetPixelX = playerPixelX;
        targetPixelY = playerPixelY;
        deathAnimationCounter = 0;
        deathFrame = 0;
    }

    public void checkPacmetalWaterElectrocution() {
        if (!isMetalPacmanActive() || playerDead || deathAnimationDone) {
            return;
        }

        if (hasWaterTileAt(getPlayerCenterTileX(), getPlayerCenterTileY())) {
            startElectrocutedDeath();
        }
    }

    public void startElectrocutedDeath() {
        if (playerDead || deathAnimationDone) {
            return;
        }

        soundManager.playElectrocuted();
        soundManager.fadeOutMusicAndStop();
        powerUpTimers[powerMetalType] = 0;
        pacMetalDamaged = false;
        playerFrozenDeath = false;
        playerElectrocutedDeath = true;
        playerElectrocutedAshVisible = false;
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
        deathAnimationDone = false;
    }

    public boolean isMetalPacmanActive() {
        return isPowerUpActive(powerMetalType);
    }

    public void damagePacmanWithExplosion() {
        if (!isMetalPacmanActive()) {
            startDeathAnimation();
            return;
        }

        if (!pacMetalDamaged) {
            pacMetalDamaged = true;
            return;
        }

        forceStartDeathAnimation();
    }

    public void damagePacmetalWithMagnetyCharge() {
        if (!isMetalPacmanActive()) {
            return;
        }

        if (!pacMetalDamaged) {
            pacMetalDamaged = true;
            return;
        }

        powerUpTimers[powerMetalType] = 0;
        pacMetalDamaged = false;
    }

    public void bouncePacmanAndMetalGhost(Ghost ghost) {
        int playerTileX = getPlayerCenterTileX();
        int playerTileY = getPlayerCenterTileY();
        int ghostTileX = getGhostCenterTileX(ghost);
        int ghostTileY = getGhostCenterTileY(ghost);
        int preferredX = Integer.compare(playerTileX, ghostTileX);
        int preferredY = Integer.compare(playerTileY, ghostTileY);

        if (Math.abs(playerTileX - ghostTileX) >= Math.abs(playerTileY - ghostTileY)) {
            preferredY = 0;
            if (preferredX == 0) {
                preferredX = lastDirectionX != 0 ? -lastDirectionX : -ghost.directionX;
            }
        } else {
            preferredX = 0;
            if (preferredY == 0) {
                preferredY = lastDirectionY != 0 ? -lastDirectionY : -ghost.directionY;
            }
        }

        int[] playerDirection = getValidBounceDirection(playerTileX, playerTileY, preferredX, preferredY, directionX, directionY);
        int[] ghostDirection = getValidBounceDirection(
                ghostTileX,
                ghostTileY,
                -playerDirection[0],
                -playerDirection[1],
                ghost.directionX,
                ghost.directionY);

        setPlayerForcedDirection(playerDirection[0], playerDirection[1]);
        setGhostForcedDirection(ghost, ghostDirection[0], ghostDirection[1]);
    }

    public int[] getValidBounceDirection(int tileX, int tileY, int preferredX, int preferredY, int currentX, int currentY) {
        if ((preferredX != 0 || preferredY != 0) && canMove(tileX + preferredX, tileY + preferredY)) {
            return new int[] { preferredX, preferredY };
        }

        if ((currentX != 0 || currentY != 0) && canMove(tileX - currentX, tileY - currentY)) {
            return new int[] { -currentX, -currentY };
        }

        int[] randomDirection = getRandomValidDirection(tileX, tileY);
        if (randomDirection != null) {
            return randomDirection;
        }

        return new int[] { 0, 0 };
    }

    public void setPlayerForcedDirection(int forcedX, int forcedY) {
        directionX = forcedX;
        directionY = forcedY;
        nextDirectionX = forcedX;
        nextDirectionY = forcedY;
        if (forcedX != 0 || forcedY != 0) {
            lastDirectionX = forcedX;
            lastDirectionY = forcedY;
        }

        int tileX = getPlayerCenterTileX();
        int tileY = getPlayerCenterTileY();
        targetPixelX = (tileX + forcedX) * tileSize;
        targetPixelY = (tileY + forcedY) * tileSize;
    }

    public void setGhostForcedDirection(Ghost ghost, int forcedX, int forcedY) {
        ghost.directionX = forcedX;
        ghost.directionY = forcedY;
        int tileX = getGhostCenterTileX(ghost);
        int tileY = getGhostCenterTileY(ghost);
        ghost.targetPixelX = (tileX + forcedX) * tileSize;
        ghost.targetPixelY = (tileY + forcedY) * tileSize;
        ghost.path.clear();
    }

    public int getGhostEatScore() {
        return isPowerUpActive(3) ? ghostEatScore * 5 : ghostEatScore;
    }

    public void killGhostAt(int index, int points) {
        killGhostAt(index, points, false);
    }

    public void killGhostAt(int index, int points, boolean leaveAsh) {
        killGhostAt(index, points, leaveAsh, ghostKillSoundEat);
    }

    public void killGhostAt(int index, int points, boolean leaveAsh, int killSound) {
        killGhostAt(index, points, leaveAsh, killSound, false);
    }

    public void killGhostAt(int index, int points, boolean leaveAsh, int killSound, boolean allowMetalKill) {
        Ghost ghost = ghosts.get(index);
        if (ghost.type == ghostMetalType) {
            if (allowMetalKill) {
                armMetalGhostExplosion(ghost);
            }
            return;
        }

        if (points != 0) {
            addScore(getAdjustedGhostKillScore(ghost, points));
        }
        playGhostKillSound(killSound);
        spawnGhostDeathEffect(ghost);
        if (leaveAsh) {
            addVisualDecal(decalAshType, ghost.pixelX, ghost.pixelY);
        }
        tryDropSpecialGhostPower(ghost.type, getGhostCenterTileX(ghost), getGhostCenterTileY(ghost));
        ghosts.remove(index);
        if (ghost.type == ghostCactusType) {
            spawnCactusSpikeProjectiles(ghost.pixelX, ghost.pixelY);
        }
        if (ghost.type == ghostBombType) {
            triggerGhostBombExplosion(ghost.pixelX + tileSize / 2.0, ghost.pixelY + tileSize / 2.0);
        }
    }

    public void armMetalGhostExplosion(Ghost ghost) {
        if (ghost.fuseTimer > 0) {
            return;
        }

        ghost.fuseTimer = ghostBombFuseTime;
        ghost.electrocutedFuse = false;
        ghost.speedDashActive = false;
        ghost.directionX = 0;
        ghost.directionY = 0;
        ghost.targetPixelX = ghost.pixelX;
        ghost.targetPixelY = ghost.pixelY;
        ghost.path.clear();
        if (shouldPlayGameSound()) {
            soundManager.playBombAim();
        }
    }

    public void armMetalGhostElectrocution(Ghost ghost) {
        if (ghost.fuseTimer > 0) {
            return;
        }

        ghost.fuseTimer = framesPerSecond;
        ghost.electrocutedFuse = true;
        ghost.speedDashActive = false;
        ghost.directionX = 0;
        ghost.directionY = 0;
        ghost.targetPixelX = ghost.pixelX;
        ghost.targetPixelY = ghost.pixelY;
        ghost.path.clear();
        if (shouldPlayGameSound()) {
            soundManager.playElectrocuted();
        }
    }

    public void tryDropSpecialGhostPower(int ghostType, int tileX, int tileY) {
        int powerUpType = getPowerUpTypeForGhostType(ghostType);

        if (noPowerMode
                || noSuperGhostMode
                || powerUpType == -1
                || specialGhostPowerDropChancePercent <= 0
                || random.nextInt(100) >= specialGhostPowerDropChancePercent
                || tileX <= 0
                || tileX >= maxScreenCol - 1
                || tileY <= 0
                || tileY >= maxScreenRow - 1
                || maze[tileX][tileY]
                || hasPowerUpAt(tileX, tileY)) {
            return;
        }

        powerUps.add(new PowerUp(powerUpType, tileX, tileY));
    }

    public int getPowerUpTypeForGhostType(int ghostType) {
        if (ghostType == ghostMagnetType) {
            return 0;
        }
        if (ghostType == ghostSpikeType || ghostType == ghostCactusType) {
            return 1;
        }
        if (ghostType == ghostSpeedType) {
            return 2;
        }
        if (ghostType == ghostBonusType) {
            return 3;
        }
        if (ghostType == ghostBombType) {
            return powerBombType;
        }
        if (ghostType == ghostLaserType) {
            return powerLaserType;
        }
        if (ghostType == ghostCloneType) {
            return powerCloneType;
        }
        if (ghostType == ghostFireType) {
            return powerFireType;
        }
        if (ghostType == ghostIceType) {
            return powerIceType;
        }
        if (ghostType == ghostMetalType) {
            return powerMetalType;
        }
        if (ghostType == ghostWaveType) {
            return powerWaterType;
        }
        if (ghostType == ghostFlashType) {
            return powerElectricType;
        }

        return -1;
    }

    public void playGhostKillSound(int killSound) {
        if (!shouldPlayGameSound()) {
            return;
        }

        if (killSound == ghostKillSoundFire) {
            soundManager.playFireDie();
        } else if (killSound == ghostKillSoundSpike) {
            soundManager.playSpikeKill();
        } else {
            soundManager.playEatGhost();
        }
    }

    public void spawnCactusSpikeProjectiles(double pixelX, double pixelY) {
        cactusSpikeProjectiles.add(new CactusSpikeProjectile(pixelX, pixelY, 1, 0));
        cactusSpikeProjectiles.add(new CactusSpikeProjectile(pixelX, pixelY, -1, 0));
        cactusSpikeProjectiles.add(new CactusSpikeProjectile(pixelX, pixelY, 0, 1));
        cactusSpikeProjectiles.add(new CactusSpikeProjectile(pixelX, pixelY, 0, -1));
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
        ArrayList<int[]> blastTiles = getBombBlastTiles();
        startBombExplosionEffect(blastTiles);

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            if (ghostIntersectsBombBlast(ghosts.get(i), blastTiles)) {
                killGhostAt(i, getGhostEatScore(), true, ghostKillSoundEat, true);
            }
        }
    }

    public void triggerElectricBombExplosion() {
        powerUpTimers[powerBombType] = 0;
        powerUpTimers[powerElectricType] = 0;
        electricKeyHeld = false;
        electricityChanneling = false;
        electricTiles.clear();

        ArrayList<int[]> blastTiles = getBombBlastTiles(playerPixelX + tileSize / 2.0, playerPixelY + tileSize / 2.0, bombRadiusTiles + 1);
        startBombExplosionEffect(blastTiles, true);

        destroyPacClonesInBombBlast(blastTiles);

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            if (ghostIntersectsBombBlast(ghosts.get(i), blastTiles)) {
                killGhostAt(i, getGhostEatScore(), true, ghostKillSoundEat, true);
            }
        }
    }

    public void triggerGhostBombExplosion(double centerX, double centerY) {
        triggerGhostBombExplosion(centerX, centerY, bombRadiusTiles);
    }

    public void triggerGhostBombExplosion(double centerX, double centerY, int radiusTiles) {
        triggerGhostBombExplosion(centerX, centerY, radiusTiles, false);
    }

    public void triggerGhostBombExplosion(double centerX, double centerY, int radiusTiles, boolean destroyWallsImmediately) {
        ArrayList<int[]> blastTiles = getBombBlastTiles(centerX, centerY, radiusTiles);
        startBombExplosionEffect(blastTiles, destroyWallsImmediately);

        if (playerIntersectsBombBlast(blastTiles)) {
            damagePacmanWithExplosion();
        }

        destroyPacClonesInBombBlast(blastTiles);

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            if (ghostIntersectsBombBlast(ghosts.get(i), blastTiles)) {
                killGhostAt(i, 0, true, ghostKillSoundEat, true);
            }
        }
    }

    public void triggerPacCloneBombExplosion(PacClone clone) {
        ArrayList<int[]> blastTiles = getBombBlastTiles(clone.pixelX + tileSize / 2.0, clone.pixelY + tileSize / 2.0);
        startBombExplosionEffect(blastTiles);

        if (playerIntersectsBombBlast(blastTiles)) {
            damagePacmanWithExplosion();
        }

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            if (ghostIntersectsBombBlast(ghosts.get(i), blastTiles)) {
                killGhostAt(i, 0, true, ghostKillSoundEat, true);
            }
        }
    }

    public void triggerPacCloneBombExplosionsAndEndClonePower() {
        powerUpTimers[powerCloneType] = 0;
        ArrayList<PacClone> explodingClones = new ArrayList<>(pacClones);
        pacClones.clear();

        for (PacClone clone : explodingClones) {
            addVisualDecal(decalAshType, clone.pixelX, clone.pixelY);
            triggerPacCloneBombExplosion(clone);
        }
    }

    public void startBombExplosionEffect(ArrayList<int[]> blastTiles) {
        startBombExplosionEffect(blastTiles, false);
    }

    public void startBombExplosionEffect(ArrayList<int[]> blastTiles, boolean destroyWallsImmediately) {
        bombExplosionTiles.clear();
        bombExplosionTiles.addAll(blastTiles);
        updateBombExplosionBounds(blastTiles);
        bombExplosionTimer = bombExplosionFrameTime;
        if (shouldPlayGameSound()) {
            soundManager.playExplosion();
        }
        if (destroyWallsImmediately) {
            destroyWallsInBombBlast(blastTiles);
        } else {
            burnWallsInBombBlast(blastTiles);
        }
    }

    public void updateBombExplosionBounds(ArrayList<int[]> blastTiles) {
        if (blastTiles.isEmpty()) {
            bombExplosionMinX = 0;
            bombExplosionMinY = 0;
            bombExplosionMaxX = 0;
            bombExplosionMaxY = 0;
            return;
        }

        int minTileX = maxScreenCol;
        int minTileY = maxScreenRow;
        int maxTileX = 0;
        int maxTileY = 0;

        for (int[] tile : blastTiles) {
            minTileX = Math.min(minTileX, tile[0]);
            minTileY = Math.min(minTileY, tile[1]);
            maxTileX = Math.max(maxTileX, tile[0]);
            maxTileY = Math.max(maxTileY, tile[1]);
        }

        bombExplosionMinX = minTileX * tileSize;
        bombExplosionMinY = minTileY * tileSize;
        bombExplosionMaxX = (maxTileX + 1) * tileSize;
        bombExplosionMaxY = (maxTileY + 1) * tileSize;
    }

    public ArrayList<int[]> getBombBlastTiles() {
        return getBombBlastTiles(playerPixelX + tileSize / 2.0, playerPixelY + tileSize / 2.0);
    }

    public ArrayList<int[]> getBombBlastTiles(double centerX, double centerY) {
        return getBombBlastTiles(centerX, centerY, bombRadiusTiles);
    }

    public ArrayList<int[]> getBombBlastTiles(double centerX, double centerY, int radiusTiles) {
        ArrayList<int[]> blastTiles = new ArrayList<>();
        int centerTileX = clampInt((int) (centerX / tileSize), 0, maxScreenCol - 1);
        int centerTileY = clampInt((int) (centerY / tileSize), 0, maxScreenRow - 1);

        for (int offsetX = -radiusTiles; offsetX <= radiusTiles; offsetX++) {
            for (int offsetY = -radiusTiles; offsetY <= radiusTiles; offsetY++) {
                int tileX = centerTileX + offsetX;
                int tileY = centerTileY + offsetY;

                if (tileX >= 0 && tileX < maxScreenCol && tileY >= 0 && tileY < maxScreenRow
                        && isTileInBombBlastShape(offsetX, offsetY, radiusTiles)) {
                    blastTiles.add(new int[] { tileX, tileY });
                }
            }
        }

        return blastTiles;
    }

    public boolean isTileInBombBlastShape(int offsetX, int offsetY) {
        return isTileInBombBlastShape(offsetX, offsetY, bombRadiusTiles);
    }

    public boolean isTileInBombBlastShape(int offsetX, int offsetY, int radiusTiles) {
        int absX = Math.abs(offsetX);
        int absY = Math.abs(offsetY);

        return absX <= radiusTiles
                && absY <= radiusTiles
                && absX + absY <= radiusTiles + 1;
    }

    public void checkLaserGhostHits() {
        if (!isPowerUpActive(powerLaserType)) {
            return;
        }

        double[] beam = getLaserBeamWorldBounds();
        burnLaserTargetWall();
        meltIceTilesInRect(beam[0], beam[1], beam[2], beam[3]);

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            Ghost ghost = ghosts.get(i);
            if (ghost.type != ghostMetalType && ghostIntersectsRect(ghost, beam[0], beam[1], beam[2], beam[3])) {
                killGhostAt(i, getGhostEatScore(), true, ghostKillSoundFire);
            }
        }
    }

    public void checkPacCloneLaserHits() {
        if (!isPowerUpActive(powerLaserType)) {
            return;
        }

        ArrayList<PacClone> laserClones = new ArrayList<>(pacClones);

        for (PacClone clone : laserClones) {
            if (!pacClones.contains(clone)) {
                continue;
            }

            double[] beam = getPacCloneLaserBeamWorldBounds(clone);
            burnPacCloneLaserTargetWall(clone);
            meltIceTilesInRect(beam[0], beam[1], beam[2], beam[3]);

            for (int i = ghosts.size() - 1; i >= 0; i--) {
                if (i >= ghosts.size()) {
                    continue;
                }

                Ghost ghost = ghosts.get(i);
                if (ghost.type != ghostMetalType && ghostIntersectsRect(ghost, beam[0], beam[1], beam[2], beam[3])) {
                    killGhostAt(i, 0, true, ghostKillSoundFire);
                    if (!pacClones.contains(clone)) {
                        return;
                    }
                }
            }
        }
    }

    public void burnLaserTargetWall() {
        int tileX = getPlayerCenterTileX();
        int tileY = getPlayerCenterTileY();
        int beamDirectionX = lastDirectionX;
        int beamDirectionY = lastDirectionY;
        int[] wallTile = getLaserTargetWallTile(tileX, tileY, beamDirectionX, beamDirectionY, null, true);

        if (wallTile != null) {
            burnWallAt(wallTile[0], wallTile[1]);
        }
    }

    public void addVisualDecal(int type, double pixelX, double pixelY) {
        int tileX = clampInt((int) (pixelX / tileSize), 0, maxScreenCol - 1);
        int tileY = clampInt((int) (pixelY / tileSize), 0, maxScreenRow - 1);
        if (hasWaterTileAt(tileX, tileY)) {
            return;
        }

        visualDecals.add(new VisualDecal(type, pixelX, pixelY));
    }

    public double[] getLaserBeamWorldBounds() {
        double centerX = playerPixelX + tileSize / 2.0;
        double centerY = playerPixelY + tileSize / 2.0;
        int tileX = getPlayerCenterTileX();
        int tileY = getPlayerCenterTileY();
        int beamDirectionX = lastDirectionX;
        int beamDirectionY = lastDirectionY;

        return getDirectedLaserBeamWorldBounds(centerX, centerY, tileX, tileY, beamDirectionX, beamDirectionY, null, true);
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
            meltIceTilesInRect(beam[0], beam[1], beam[2], beam[3]);

            if (playerIntersectsRect(beam[0], beam[1], beam[2], beam[3])) {
                startDeathAnimation();
                return;
            }

            destroyPacClonesInRect(beam[0], beam[1], beam[2], beam[3]);

            for (int j = ghosts.size() - 1; j >= 0; j--) {
                if (i == j) {
                    continue;
                }

                Ghost targetGhost = ghosts.get(j);
                if (targetGhost.type != ghostMetalType && ghostIntersectsRect(targetGhost, beam[0], beam[1], beam[2], beam[3])) {
                    killGhostAt(j, 0, true, ghostKillSoundFire);
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

                for (int cloneIndex = pacClones.size() - 1; cloneIndex >= 0; cloneIndex--) {
                    if (pacCloneIntersectsRect(pacClones.get(cloneIndex), minX, minY, maxX, maxY)) {
                        destroyPacCloneAt(cloneIndex);
                        return;
                    }
                }

                for (int i = ghosts.size() - 1; i >= 0; i--) {
                    if (i >= ghosts.size()) {
                        continue;
                    }

                    Ghost ghost = ghosts.get(i);
                    if (ghost.type == ghostBombType && ghostIntersectsRect(ghost, minX, minY, maxX, maxY)) {
                        killGhostAt(i, 0, true, ghostKillSoundFire);
                        return;
                    }
                }
            } else {
                for (int i = ghosts.size() - 1; i >= 0; i--) {
                    if (i >= ghosts.size()) {
                        continue;
                    }

                    Ghost ghost = ghosts.get(i);
                    if (ghost.type != ghostMetalType && ghostIntersectsRect(ghost, minX, minY, maxX, maxY)) {
                        killGhostAt(i, 0, true, ghostKillSoundFire);
                        return;
                    }
                }
            }
        }
    }

    public void checkPacCloneGhostCollisions() {
        for (int cloneIndex = pacClones.size() - 1; cloneIndex >= 0; cloneIndex--) {
            if (cloneIndex >= pacClones.size()) {
                continue;
            }

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
                    if (ghost.type == ghostMetalType) {
                        destroyPacCloneAt(cloneIndex);
                        return;
                    }

                    if (ghost.type == ghostCactusType) {
                        addVisualDecal(decalAshType, clone.pixelX, clone.pixelY);
                        pacClones.remove(cloneIndex);
                        return;
                    }

                    if (isPowerUpActive(powerBombType)) {
                        addVisualDecal(decalAshType, clone.pixelX, clone.pixelY);
                        pacClones.remove(cloneIndex);
                        triggerPacCloneBombExplosion(clone);
                        return;
                    } else if (powerMode) {
                        if (ghost.type == ghostIceType) {
                            iceGhostSlowTimer = iceGhostSlowDuration;
                        }
                        killGhostAt(ghostIndex, getGhostEatScore());
                        ghostEatScore = Math.min(1600, ghostEatScore * 2);
                        if (cloneIndex >= pacClones.size() || pacClones.get(cloneIndex) != clone) {
                            return;
                        }
                        continue;
                    } else {
                        addVisualDecal(decalAshType, clone.pixelX, clone.pixelY);
                        pacClones.remove(cloneIndex);
                        killGhostAt(ghostIndex, 0, false);
                        return;
                    }
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
                ghost.laserDirectionY,
                ghost,
                true);
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
                beamDirectionY,
                null,
                true);
    }

    public double[] getDirectedLaserBeamWorldBounds(double centerX, double centerY, int tileX, int tileY,
            int beamDirectionX, int beamDirectionY) {
        return getDirectedLaserBeamWorldBounds(centerX, centerY, tileX, tileY, beamDirectionX, beamDirectionY, null, true);
    }

    public double[] getDirectedLaserBeamWorldBounds(double centerX, double centerY, int tileX, int tileY,
            int beamDirectionX, int beamDirectionY, Ghost ignoredGhost, boolean includePacmetalBlocker) {
        double halfWidth = laserWidth / 2.0;

        if (beamDirectionX != 0) {
            double endX = getLaserEndCoordinate(tileX, tileY, beamDirectionX, 0, ignoredGhost, includePacmetalBlocker);
            return new double[] {
                Math.min(centerX, endX),
                centerY - halfWidth,
                Math.max(centerX, endX),
                centerY + halfWidth
            };
        }

        double endY = getLaserEndCoordinate(tileX, tileY, 0, beamDirectionY, ignoredGhost, includePacmetalBlocker);
        return new double[] {
            centerX - halfWidth,
            Math.min(centerY, endY),
            centerX + halfWidth,
            Math.max(centerY, endY)
        };
    }

    public double getLaserEndCoordinate(int tileX, int tileY, int beamDirectionX, int beamDirectionY,
            Ghost ignoredGhost, boolean includePacmetalBlocker) {
        int currentTileX = tileX;
        int currentTileY = tileY;

        while (true) {
            int nextTileX = currentTileX + beamDirectionX;
            int nextTileY = currentTileY + beamDirectionY;

            if (!canMove(nextTileX, nextTileY)) {
                if (beamDirectionX != 0) {
                    return beamDirectionX > 0 ? (currentTileX + 1) * tileSize : currentTileX * tileSize;
                }
                return beamDirectionY > 0 ? (currentTileY + 1) * tileSize : currentTileY * tileSize;
            }

            if (isMetalLaserBlockerAt(nextTileX, nextTileY, ignoredGhost, includePacmetalBlocker)) {
                if (beamDirectionX != 0) {
                    return beamDirectionX > 0 ? nextTileX * tileSize : (nextTileX + 1) * tileSize;
                }
                return beamDirectionY > 0 ? nextTileY * tileSize : (nextTileY + 1) * tileSize;
            }

            currentTileX = nextTileX;
            currentTileY = nextTileY;
        }
    }

    public int[] getLaserTargetWallTile(int tileX, int tileY, int beamDirectionX, int beamDirectionY,
            Ghost ignoredGhost, boolean includePacmetalBlocker) {
        int currentTileX = tileX;
        int currentTileY = tileY;

        while (true) {
            int nextTileX = currentTileX + beamDirectionX;
            int nextTileY = currentTileY + beamDirectionY;

            if (!canMove(nextTileX, nextTileY)) {
                return new int[] { nextTileX, nextTileY };
            }

            if (isMetalLaserBlockerAt(nextTileX, nextTileY, ignoredGhost, includePacmetalBlocker)) {
                return null;
            }

            currentTileX = nextTileX;
            currentTileY = nextTileY;
        }
    }

    public boolean isMetalLaserBlockerAt(int tileX, int tileY, Ghost ignoredGhost, boolean includePacmetalBlocker) {
        if (includePacmetalBlocker
                && isMetalPacmanActive()
                && getPlayerCenterTileX() == tileX
                && getPlayerCenterTileY() == tileY) {
            return true;
        }

        for (Ghost ghost : ghosts) {
            if (ghost != ignoredGhost
                    && ghost.type == ghostMetalType
                    && getGhostCenterTileX(ghost) == tileX
                    && getGhostCenterTileY(ghost) == tileY) {
                return true;
            }
        }

        return false;
    }

    public void burnGhostLaserTargetWall(Ghost ghost) {
        setGhostLaserDirection(ghost);

        int tileX = (int) ((ghost.pixelX + tileSize / 2.0) / tileSize);
        int tileY = (int) ((ghost.pixelY + tileSize / 2.0) / tileSize);
        int beamDirectionX = ghost.laserDirectionX;
        int beamDirectionY = ghost.laserDirectionY;
        int[] wallTile = getLaserTargetWallTile(tileX, tileY, beamDirectionX, beamDirectionY, ghost, true);

        if (wallTile != null) {
            burnWallAt(wallTile[0], wallTile[1]);
        }
    }

    public void burnPacCloneLaserTargetWall(PacClone clone) {
        int tileX = (int) ((clone.pixelX + tileSize / 2.0) / tileSize);
        int tileY = (int) ((clone.pixelY + tileSize / 2.0) / tileSize);
        int beamDirectionX = clone.directionX;
        int beamDirectionY = clone.directionY;

        if (beamDirectionX == 0 && beamDirectionY == 0) {
            beamDirectionX = 1;
        }
        int[] wallTile = getLaserTargetWallTile(tileX, tileY, beamDirectionX, beamDirectionY, null, true);

        if (wallTile != null) {
            burnWallAt(wallTile[0], wallTile[1]);
        }
    }

    public boolean ghostIntersectsRect(Ghost ghost, double minX, double minY, double maxX, double maxY) {
        return ghost.pixelX < maxX
                && ghost.pixelX + tileSize > minX
                && ghost.pixelY < maxY
                && ghost.pixelY + tileSize > minY;
    }

    public boolean pacCloneIntersectsRect(PacClone clone, double minX, double minY, double maxX, double maxY) {
        return clone.pixelX < maxX
                && clone.pixelX + tileSize > minX
                && clone.pixelY < maxY
                && clone.pixelY + tileSize > minY;
    }

    public void destroyPacCloneAt(int cloneIndex) {
        if (cloneIndex < 0 || cloneIndex >= pacClones.size()) {
            return;
        }

        PacClone clone = pacClones.get(cloneIndex);
        addVisualDecal(decalAshType, clone.pixelX, clone.pixelY);
        pacClones.remove(cloneIndex);
    }

    public void destroyPacClonesInRect(double minX, double minY, double maxX, double maxY) {
        for (int cloneIndex = pacClones.size() - 1; cloneIndex >= 0; cloneIndex--) {
            if (pacCloneIntersectsRect(pacClones.get(cloneIndex), minX, minY, maxX, maxY)) {
                destroyPacCloneAt(cloneIndex);
            }
        }
    }

    public boolean ghostIntersectsBombBlast(Ghost ghost, ArrayList<int[]> blastTiles) {
        return entityIntersectsBombBlast(ghost.pixelX, ghost.pixelY, blastTiles);
    }

    public boolean playerIntersectsBombBlast(ArrayList<int[]> blastTiles) {
        return entityIntersectsBombBlast(playerPixelX, playerPixelY, blastTiles);
    }

    public boolean pacCloneIntersectsBombBlast(PacClone clone, ArrayList<int[]> blastTiles) {
        return entityIntersectsBombBlast(clone.pixelX, clone.pixelY, blastTiles);
    }

    public boolean entityIntersectsBombBlast(double pixelX, double pixelY, ArrayList<int[]> blastTiles) {
        for (int[] tile : blastTiles) {
            if (rectsIntersect(
                    pixelX,
                    pixelY,
                    pixelX + tileSize,
                    pixelY + tileSize,
                    tile[0] * tileSize,
                    tile[1] * tileSize,
                    (tile[0] + 1) * tileSize,
                    (tile[1] + 1) * tileSize)) {
                return true;
            }
        }

        return false;
    }

    public void destroyPacClonesInBombBlast(ArrayList<int[]> blastTiles) {
        for (int cloneIndex = pacClones.size() - 1; cloneIndex >= 0; cloneIndex--) {
            if (pacCloneIntersectsBombBlast(pacClones.get(cloneIndex), blastTiles)) {
                destroyPacCloneAt(cloneIndex);
            }
        }
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

    public void burnWallsInBombBlast(ArrayList<int[]> blastTiles) {
        if (burnedWalls == null) {
            burnedWalls = new boolean[maxScreenCol][maxScreenRow];
        }

        for (int[] tile : blastTiles) {
            int tileX = tile[0];
            int tileY = tile[1];

            if (!isDrawnWallTile(tileX, tileY)) {
                continue;
            }

            if (isDestructibleMazeWallTile(tileX, tileY) && burnedWalls[tileX][tileY]) {
                destroyBurnedWall(tileX, tileY);
            } else {
                burnedWalls[tileX][tileY] = true;
            }
        }
    }

    public void destroyWallsInBombBlast(ArrayList<int[]> blastTiles) {
        if (burnedWalls == null) {
            burnedWalls = new boolean[maxScreenCol][maxScreenRow];
        }

        for (int[] tile : blastTiles) {
            int tileX = tile[0];
            int tileY = tile[1];

            if (isDestructibleMazeWallTile(tileX, tileY)) {
                destroyBurnedWall(tileX, tileY);
            } else if (isDrawnWallTile(tileX, tileY)) {
                burnedWalls[tileX][tileY] = true;
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

    public boolean isDestructibleMazeWallTile(int tileX, int tileY) {
        return tileX > 0
                && tileX < maxScreenCol - 1
                && tileY > 0
                && tileY < maxScreenRow - 1
                && maze[tileX][tileY];
    }

    public boolean isOuterWallTile(int tileX, int tileY) {
        return tileX == 0 || tileX == maxScreenCol - 1 || tileY == 0 || tileY == maxScreenRow - 1;
    }

    public void destroyBurnedWall(int tileX, int tileY) {
        maze[tileX][tileY] = false;
        burnedWalls[tileX][tileY] = false;

        if (blockTextureIndexes != null) {
            blockTextureIndexes[tileX][tileY] = -1;
        }

        if (debrisIndexes != null && debrisCount > 0) {
            debrisIndexes[tileX][tileY] = random.nextInt(debrisCount);
        }
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

    public int getPacCloneCenterTileX(PacClone clone) {
        return clampInt((int) ((clone.pixelX + tileSize / 2.0) / tileSize), 0, maxScreenCol - 1);
    }

    public int getPacCloneCenterTileY(PacClone clone) {
        return clampInt((int) ((clone.pixelY + tileSize / 2.0) / tileSize), 0, maxScreenRow - 1);
    }

    public int getGhostCenterTileX(Ghost ghost) {
        return clampInt((int) ((ghost.pixelX + tileSize / 2.0) / tileSize), 0, maxScreenCol - 1);
    }

    public int getGhostCenterTileY(Ghost ghost) {
        return clampInt((int) ((ghost.pixelY + tileSize / 2.0) / tileSize), 0, maxScreenRow - 1);
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

        if (playerElectrocutedDeath) {
            deathAnimationCounter++;
            if (deathAnimationCounter >= framesPerSecond) {
                playerElectrocutedAshVisible = true;
                deathAnimationDone = true;
                handleFinalScore();
            }
            return;
        }

        if (playerFrozenDeath) {
            deathAnimationCounter++;
            if (deathAnimationCounter >= framesPerSecond) {
                deathAnimationDone = true;
                handleFinalScore();
            }
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
            if (updateElectrocutedGhost(ghost)) {
                continue;
            }

            updateGhost(ghost);

            if (checkWaveGhostIceDeath(i)) {
                continue;
            }

            if (!ghosts.contains(ghost)) {
                continue;
            }

            dropGhostFireTrail(ghost);
            dropGhostIceTile(ghost);
            dropGhostWaterTile(ghost);
            dropGhostFlashElectricTiles(ghost);
            eatPowerUpAtGhost(ghost);
            eatPelletsAtGhost(ghost);

            if (!ghosts.contains(ghost)) {
                continue;
            }

            checkWaveGhostFireCollision(ghost);

            if (!ghosts.contains(ghost)) {
                continue;
            }

            if (ghost.type == ghostMetalType && isGhostOnWaterTile(ghost)) {
                armMetalGhostElectrocution(ghost);
            }

            if (ghost.type == ghostFireType && isGhostOnWaterTile(ghost)) {
                killGhostAt(i, 0, true, ghostKillSoundFire);
                continue;
            }

            if (checkSpikeTrapCollision(ghost)) {
                killGhostAt(i, 200, false, ghostKillSoundSpike);
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

    public boolean updateElectrocutedGhost(Ghost ghost) {
        if (ghost.electrocutedTimer <= 0) {
            return false;
        }

        ghost.electrocutedTimer--;
        ghost.targetPixelX = ghost.pixelX;
        ghost.targetPixelY = ghost.pixelY;
        ghost.directionX = 0;
        ghost.directionY = 0;

        if (ghost.electrocutedTimer <= 0) {
            if (ghost.type == ghostFlashType) {
                finishFlashGhostRechargeShock(ghost);
                return true;
            }

            finishElectrocutedGhost(ghost);
        }

        return true;
    }

    public void finishFlashGhostRechargeShock(Ghost ghost) {
        chargeFlashGhostFromElectricity(ghost);
        ghost.electrocutedLargeExplosion = false;
    }

    public void finishElectrocutedGhost(Ghost ghost) {
        int index = ghosts.indexOf(ghost);
        if (index == -1) {
            return;
        }

        double centerX = ghost.pixelX + tileSize / 2.0;
        double centerY = ghost.pixelY + tileSize / 2.0;
        boolean largeExplosion = ghost.electrocutedLargeExplosion;

        addVisualDecal(decalAshType, ghost.pixelX, ghost.pixelY);
        tryDropSpecialGhostPower(ghost.type, getGhostCenterTileX(ghost), getGhostCenterTileY(ghost));
        ghosts.remove(index);

        if (largeExplosion) {
            triggerGhostBombExplosion(centerX, centerY, bombRadiusTiles + 1, true);
        }
    }

    public boolean checkWaveGhostIceDeath(int ghostIndex) {
        if (ghostIndex < 0 || ghostIndex >= ghosts.size()) {
            return false;
        }

        Ghost ghost = ghosts.get(ghostIndex);
        if (ghost.type != ghostWaveType) {
            return false;
        }

        int tileX = getGhostCenterTileX(ghost);
        int tileY = getGhostCenterTileY(ghost);
        if (hasIceTileAt(tileX, tileY) || isGhostInAnyIceAura(ghost)) {
            freezeGhostAt(ghostIndex);
            return true;
        }

        return false;
    }

    public void dropGhostFlashElectricTiles(Ghost ghost) {
        if (ghost.type != ghostFlashType || !ghost.flashyCharged || !isGhostMoving(ghost)) {
            return;
        }

        int centerTileX = getGhostCenterTileX(ghost);
        int centerTileY = getGhostCenterTileY(ghost);

        for (int offsetX = -ghostFlashElectricRadiusTiles; offsetX <= ghostFlashElectricRadiusTiles; offsetX++) {
            for (int offsetY = -ghostFlashElectricRadiusTiles; offsetY <= ghostFlashElectricRadiusTiles; offsetY++) {
                addElectricTileAt(centerTileX + offsetX, centerTileY + offsetY, ghost);
            }
        }
    }

    public void checkWaveGhostFireCollision(Ghost waveGhost) {
        if (waveGhost.type != ghostWaveType) {
            return;
        }

        double minX = waveGhost.pixelX;
        double minY = waveGhost.pixelY;
        double maxX = minX + tileSize;
        double maxY = minY + tileSize;

        for (int i = ghosts.size() - 1; i >= 0; i--) {
            if (i >= ghosts.size()) {
                continue;
            }

            Ghost ghost = ghosts.get(i);
            if (ghost != waveGhost && ghost.type == ghostFireType && ghostIntersectsRect(ghost, minX, minY, maxX, maxY)) {
                killGhostAt(i, 0, true, ghostKillSoundFire);
                return;
            }
        }
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

        if (ghost.type == ghostMetalType) {
            return updateMetalGhostState(ghost);
        }

        if (ghost.type == ghostLaserType) {
            return updateLaserGhostState(ghost);
        }

        if (ghost.type == ghostSpeedType) {
            return updateSpeedGhostState(ghost);
        }

        if (ghost.type == ghostMagnetType) {
            return updateMagnetGhostState(ghost);
        }

        if (ghost.type == ghostFlashType) {
            return updateFlashGhostState(ghost);
        }

        return false;
    }

    public boolean updateFlashGhostState(Ghost ghost) {
        ghost.flashyTimer--;
        if (ghost.flashyTimer <= 0) {
            ghost.flashyCharged = !ghost.flashyCharged;
            ghost.flashyTimer = ghost.flashyCharged ? ghostFlashChargedTime : ghostFlashExhaustedTime;
            ghost.path.clear();
        }

        if (ghost.flashyCharged) {
            return false;
        }

        ghost.targetPixelX = ghost.pixelX;
        ghost.targetPixelY = ghost.pixelY;
        ghost.directionX = 0;
        ghost.directionY = 0;
        return true;
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

        if (isPacmanOrCloneInBombGhostTriggerZone(ghost)) {
            ghost.fuseTimer = ghostBombFuseTime;
            ghost.directionX = 0;
            ghost.directionY = 0;
            ghost.targetPixelX = ghost.pixelX;
            ghost.targetPixelY = ghost.pixelY;
            if (shouldPlayGameSound()) {
                soundManager.playBombAim();
            }
            return true;
        }

        return false;
    }

    public boolean updateMetalGhostState(Ghost ghost) {
        if (isMetalGhostTouchingMagnety(ghost)) {
            killBombGhostWithoutRespawn(ghost);
            return true;
        }

        if (ghost.fuseTimer > 0) {
            ghost.fuseTimer--;
            ghost.targetPixelX = ghost.pixelX;
            ghost.targetPixelY = ghost.pixelY;

            if (ghost.fuseTimer <= 0) {
                killBombGhostWithoutRespawn(ghost);
            }

            return true;
        }

        if (isGhostInFriendlyMagnetAura(ghost)) {
            armMetalGhostExplosion(ghost);
            return true;
        }

        if (ghost.speedDashActive) {
            if (!isGhostMoving(ghost)) {
                ghost.speedDashActive = false;
                return true;
            }
            return false;
        }

        int[] magnetyDirection = getLineOfSightDirectionToGhostType(
                getGhostCenterTileX(ghost),
                getGhostCenterTileY(ghost),
                ghostMagnetType,
                ghost);

        if (magnetyDirection != null) {
            startMetalOrMagnetDash(ghost, magnetyDirection);
            return false;
        }

        return false;
    }

    public boolean updateMagnetGhostState(Ghost ghost) {
        if (ghost.speedDashActive) {
            if (!isGhostMoving(ghost)) {
                ghost.speedDashActive = false;
                return true;
            }
            return false;
        }

        int[] metalGhostDirection = getLineOfSightDirectionToGhostType(
                getGhostCenterTileX(ghost),
                getGhostCenterTileY(ghost),
                ghostMetalType,
                ghost);

        if (metalGhostDirection != null) {
            startMetalOrMagnetDash(ghost, metalGhostDirection);
            return false;
        }

        if (!isMetalPacmanActive()) {
            return false;
        }

        int[] pacmetalDirection = getLineOfSightDirectionToPlayer(getGhostCenterTileX(ghost), getGhostCenterTileY(ghost));
        if (pacmetalDirection != null) {
            startMetalOrMagnetDash(ghost, pacmetalDirection);
            return false;
        }

        return false;
    }

    public void startMetalOrMagnetDash(Ghost ghost, int[] direction) {
        int tileX = getGhostCenterTileX(ghost);
        int tileY = getGhostCenterTileY(ghost);
        int[] endTile = getDashEndTile(tileX, tileY, direction[0], direction[1]);

        if (!ghost.speedDashActive && shouldPlayGameSound()) {
            soundManager.playDetection();
        }

        ghost.directionX = direction[0];
        ghost.directionY = direction[1];
        ghost.targetPixelX = endTile[0] * tileSize;
        ghost.targetPixelY = endTile[1] * tileSize;
        ghost.speedDashActive = true;
        ghost.path.clear();
    }

    public boolean isMetalGhostTouchingMagnety(Ghost metalGhost) {
        for (Ghost ghost : ghosts) {
            if (ghost != metalGhost
                    && ghost.type == ghostMagnetType
                    && ghostIntersectsRect(
                            ghost,
                            metalGhost.pixelX,
                            metalGhost.pixelY,
                            metalGhost.pixelX + tileSize,
                            metalGhost.pixelY + tileSize)) {
                return true;
            }
        }

        return false;
    }

    public boolean isPacmanOrCloneInBombGhostTriggerZone(Ghost ghost) {
        int ghostTileX = clampInt((int) ((ghost.pixelX + tileSize / 2.0) / tileSize), 0, maxScreenCol - 1);
        int ghostTileY = clampInt((int) ((ghost.pixelY + tileSize / 2.0) / tileSize), 0, maxScreenRow - 1);

        if (isTileInBombGhostTriggerZone(getPlayerCenterTileX(), getPlayerCenterTileY(), ghostTileX, ghostTileY)) {
            return true;
        }

        for (PacClone clone : pacClones) {
            if (isTileInBombGhostTriggerZone(getPacCloneCenterTileX(clone), getPacCloneCenterTileY(clone), ghostTileX, ghostTileY)) {
                return true;
            }
        }

        return false;
    }

    public boolean isTileInBombGhostTriggerZone(int targetTileX, int targetTileY, int ghostTileX, int ghostTileY) {
        return Math.abs(targetTileX - ghostTileX) <= ghostBombTriggerRadiusTiles
                && Math.abs(targetTileY - ghostTileY) <= ghostBombTriggerRadiusTiles;
    }

    public void killBombGhostWithoutRespawn(Ghost ghost) {
        int index = ghosts.indexOf(ghost);

        if (index == -1) {
            return;
        }

        addVisualDecal(decalAshType, ghost.pixelX, ghost.pixelY);
        boolean electrocutedExplosion = ghost.electrocutedFuse;
        ghost.electrocutedFuse = false;
        ghosts.remove(index);
        triggerGhostBombExplosion(
                ghost.pixelX + tileSize / 2.0,
                ghost.pixelY + tileSize / 2.0,
                electrocutedExplosion ? bombRadiusTiles + 1 : bombRadiusTiles,
                electrocutedExplosion);
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

        int[] sightDirection = getLineOfSightDirectionToPacmanOrClone((int) (ghost.pixelX / tileSize), (int) (ghost.pixelY / tileSize));

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

        if (!ghost.speedDashActive && shouldPlayGameSound()) {
            soundManager.playDetection();
        }

        ghost.directionX = direction[0];
        ghost.directionY = direction[1];
        ghost.targetPixelX = endTile[0] * tileSize;
        ghost.targetPixelY = endTile[1] * tileSize;
        ghost.speedDashActive = true;
        ghost.restTimer = 0;
        ghost.path.clear();

        if ((tileX == getPlayerCenterTileX() && tileY == getPlayerCenterTileY())
                || isAnyPacCloneOnTile(tileX, tileY)) {
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
        if (ghost.type == ghostCloneType
                || ghost.type == ghostFireType
                || ghost.type == ghostMetalType
                || ghost.type == ghostFlashType) {
            return chooseRandomWallBounceDirection(ghost, tileX, tileY);
        }

        if (ghost.type == ghostIceType) {
            return chooseIceGhostDirection(ghost, tileX, tileY);
        }

        if (ghost.type == ghostWaveType) {
            return chooseWaveGhostDirection(ghost, tileX, tileY);
        }

        if (ghost.type == ghostSpeedType) {
            return chooseRandomWallBounceDirection(ghost, tileX, tileY);
        }

        if (ghost.type == ghostBombType || ghost.type == ghostLaserType) {
            return chooseAStarDirection(ghost, tileX, tileY);
        }

        if (ghost.type == ghostCactusType) {
            return chooseAStarDirection(ghost, tileX, tileY);
        }

        if (ghost.type == ghostBonusType) {
            return chooseBonusGhostDirection(ghost, tileX, tileY);
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
        if (ghost.type == ghostMagnetType || ghost.type == ghostSpikeType) {
            return chooseAStarDirection(ghost, tileX, tileY);
        }

        return chooseAStarDirection(ghost, tileX, tileY);
    }

    public int[] chooseBonusGhostDirection(Ghost ghost, int tileX, int tileY) {
        if (ghost.path.isEmpty()) {
            int[] pelletTile = findNearestPelletTile(tileX, tileY);

            if (pelletTile == null) {
                return chooseRandomWallBounceDirection(ghost, tileX, tileY);
            }

            ghost.path = findAStarPath(tileX, tileY, pelletTile[0], pelletTile[1]);
        }

        if (ghost.path.isEmpty()) {
            return chooseRandomWallBounceDirection(ghost, tileX, tileY);
        }

        int[] nextTile = ghost.path.remove(0);
        return new int[] { nextTile[0] - tileX, nextTile[1] - tileY };
    }

    public int[] chooseIceGhostDirection(Ghost ghost, int tileX, int tileY) {
        if (!ghost.path.isEmpty()) {
            int[] targetTile = ghost.path.get(ghost.path.size() - 1);
            if (hasIceTileAt(targetTile[0], targetTile[1])) {
                ghost.path.clear();
            }
        }

        if (ghost.path.isEmpty()) {
            int[] floorTile = findNearestUnicedFloorTile(tileX, tileY);

            if (floorTile == null) {
                return chooseRandomWallBounceDirection(ghost, tileX, tileY);
            }

            ghost.path = findAStarPath(tileX, tileY, floorTile[0], floorTile[1]);
        }

        if (ghost.path.isEmpty()) {
            return chooseRandomWallBounceDirection(ghost, tileX, tileY);
        }

        int[] nextTile = ghost.path.remove(0);
        return new int[] { nextTile[0] - tileX, nextTile[1] - tileY };
    }

    public int[] chooseWaveGhostDirection(Ghost ghost, int tileX, int tileY) {
        if (!ghost.path.isEmpty()) {
            int[] targetTile = ghost.path.get(ghost.path.size() - 1);
            if (hasWaterTileAt(targetTile[0], targetTile[1]) || hasIceTileAt(targetTile[0], targetTile[1])) {
                ghost.path.clear();
            }
        }

        if (ghost.path.isEmpty()) {
            int[] floorTile = findNearestUnwateredFloorTile(tileX, tileY);

            if (floorTile == null) {
                return chooseRandomWallBounceDirection(ghost, tileX, tileY);
            }

            ghost.path = findAStarPath(tileX, tileY, floorTile[0], floorTile[1]);
        }

        if (ghost.path.isEmpty()) {
            return chooseRandomWallBounceDirection(ghost, tileX, tileY);
        }

        int[] nextTile = ghost.path.remove(0);
        return new int[] { nextTile[0] - tileX, nextTile[1] - tileY };
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
        if (ghost.type == ghostMetalType) {
            return false;
        }

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
                playGameSoundSpikeKill();
                if (isMetalPacmanActive()) {
                    return;
                }
                startDeathAnimation();
                return;
            }
        }

        for (int cloneIndex = pacClones.size() - 1; cloneIndex >= 0; cloneIndex--) {
            PacClone clone = pacClones.get(cloneIndex);
            int cloneTileX = getPacCloneCenterTileX(clone);
            int cloneTileY = getPacCloneCenterTileY(clone);

            for (GhostSpikeTrap spikeTrap : ghostSpikeTraps) {
                if (spikeTrap.tileX == cloneTileX && spikeTrap.tileY == cloneTileY) {
                    spikeTrap.used = true;
                    playGameSoundSpikeKill();
                    destroyPacCloneAt(cloneIndex);
                    return;
                }
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
        return getLineOfSightDirectionToTile(tileX, tileY, getPlayerCenterTileX(), getPlayerCenterTileY());
    }

    public int[] getLineOfSightDirectionToPacmanOrClone(int tileX, int tileY) {
        int[] playerDirection = getLineOfSightDirectionToPlayer(tileX, tileY);

        if (playerDirection != null) {
            return playerDirection;
        }

        for (PacClone clone : pacClones) {
            int[] cloneDirection = getLineOfSightDirectionToTile(
                    tileX,
                    tileY,
                    getPacCloneCenterTileX(clone),
                    getPacCloneCenterTileY(clone));

            if (cloneDirection != null) {
                return cloneDirection;
            }
        }

        return null;
    }

    public int[] getLineOfSightDirectionToGhostType(int tileX, int tileY, int targetGhostType, Ghost ignoredGhost) {
        for (Ghost ghost : ghosts) {
            if (ghost == ignoredGhost || ghost.type != targetGhostType) {
                continue;
            }

            int[] ghostDirection = getLineOfSightDirectionToTile(
                    tileX,
                    tileY,
                    getGhostCenterTileX(ghost),
                    getGhostCenterTileY(ghost));

            if (ghostDirection != null) {
                return ghostDirection;
            }
        }

        return null;
    }

    public int[] getLineOfSightDirectionToTile(int tileX, int tileY, int targetTileX, int targetTileY) {
        if (tileX == targetTileX && tileY == targetTileY) {
            return null;
        }

        if (tileX == targetTileX) {
            int directionY = targetTileY > tileY ? 1 : -1;
            return hasClearLine(tileX, tileY, targetTileX, targetTileY, 0, directionY)
                    ? new int[] { 0, directionY }
                    : null;
        }

        if (tileY == targetTileY) {
            int directionX = targetTileX > tileX ? 1 : -1;
            return hasClearLine(tileX, tileY, targetTileX, targetTileY, directionX, 0)
                    ? new int[] { directionX, 0 }
                    : null;
        }

        return null;
    }

    public boolean isAnyPacCloneOnTile(int tileX, int tileY) {
        for (PacClone clone : pacClones) {
            if (getPacCloneCenterTileX(clone) == tileX && getPacCloneCenterTileY(clone) == tileY) {
                return true;
            }
        }

        return false;
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

    public void drawDebris(Graphics2D g2) {
        if (debrisIndexes == null || debrisCount <= 0) {
            return;
        }

        for (int x = 1; x < maxScreenCol - 1; x++) {
            for (int y = 1; y < maxScreenRow - 1; y++) {
                int debrisIndex = debrisIndexes[x][y];

                if (debrisIndex >= 0 && debrisIndex < debrisCount) {
                    drawImageAtTile(g2, debrisSprites[debrisIndex], x, y);
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

    public void drawWaterEffects(Graphics2D g2) {
        for (WaterEffect waterEffect : waterEffects) {
            double scale = waterEffect.holdTimer > 0 ? 1.0 : waterEffect.timer / (double) waterEffect.duration;
            int size = Math.max(1, (int) Math.round(tileSize * scale));
            int screenX = worldToScreenX(waterEffect.tileX * tileSize) + (tileSize - size) / 2;
            int screenY = worldToScreenY(waterEffect.tileY * tileSize) + (tileSize - size) / 2;

            g2.drawImage(waterSprite, screenX, screenY, size, size, null);
        }
    }

    public void drawCactusSpikeProjectiles(Graphics2D g2) {
        for (CactusSpikeProjectile projectile : cactusSpikeProjectiles) {
            int screenX = worldToScreenX(projectile.pixelX);
            int screenY = worldToScreenY(projectile.pixelY);
            double angle = getDirectionAngle(projectile.directionX, projectile.directionY);
            Graphics2D spikeGraphics = (Graphics2D) g2.create();

            spikeGraphics.rotate(angle, screenX + tileSize / 2.0, screenY + tileSize / 2.0);
            spikeGraphics.drawImage(cactusSpikeSprite, screenX, screenY, tileSize, tileSize, null);
            spikeGraphics.dispose();
        }
    }

    public void drawIceTiles(Graphics2D g2) {
        for (IceTile iceTile : iceTiles) {
            drawImageAtTile(g2, iceTileSprite, iceTile.tileX, iceTile.tileY);
        }
    }

    public void drawWaterTiles(Graphics2D g2) {
        int frame = (elapsedFrames / animationDelay) % waterTileSprites[0].length;

        for (WaterTile waterTile : waterTiles) {
            int variation = clampInt(waterTile.variation, 0, waterTileSprites.length - 1);
            drawImageAtTile(g2, waterTileSprites[variation][frame], waterTile.tileX, waterTile.tileY);
            if (waterTile.freezeTimer >= 0) {
                drawFreezingWaterOverlay(g2, waterTile);
            }
        }
    }

    public void drawElectricTiles(Graphics2D g2) {
        for (ElectricTile electricTile : electricTiles) {
            int frame = (electricTile.frameOffset + elapsedFrames / 2) % electricTileSprites.length;
            drawImageAtTile(g2, electricTileSprites[frame], electricTile.tileX, electricTile.tileY);
        }
    }

    public void drawFreezingWaterOverlay(Graphics2D g2, WaterTile waterTile) {
        Composite originalComposite = g2.getComposite();
        Color originalColor = g2.getColor();
        float progress = 1.0f - clampInt(waterTile.freezeTimer, 0, waterFreezeChainTime) / (float) waterFreezeChainTime;
        float alpha = 0.15f + progress * 0.45f;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setColor(new Color(0x00ffff));
        g2.fillRect(
                worldToScreenX(waterTile.tileX * tileSize),
                worldToScreenY(waterTile.tileY * tileSize),
                tileSize,
                tileSize);
        g2.setComposite(originalComposite);
        g2.setColor(originalColor);
    }

    public void drawMagnetAuras(Graphics2D g2) {
        Composite originalComposite = g2.getComposite();
        Color originalColor = g2.getColor();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        g2.setColor(new Color(0x7fff7f));

        if (isPowerUpActive(0)) {
            drawMagnetAuraAt(g2, getPlayerCenterTileX(), getPlayerCenterTileY());
            for (PacClone clone : pacClones) {
                drawMagnetAuraAt(g2, getPacCloneCenterTileX(clone), getPacCloneCenterTileY(clone));
            }
        }

        for (Ghost ghost : ghosts) {
            if (ghost.type == ghostMagnetType) {
                drawMagnetAuraAt(g2, getGhostCenterTileX(ghost), getGhostCenterTileY(ghost));
            }
        }

        g2.setComposite(originalComposite);
        g2.setColor(originalColor);
    }

    public void drawMagnetAuraAt(Graphics2D g2, int centerTileX, int centerTileY) {
        for (int offsetX = -2; offsetX <= 2; offsetX++) {
            for (int offsetY = -2; offsetY <= 2; offsetY++) {
                int tileX = centerTileX + offsetX;
                int tileY = centerTileY + offsetY;

                if (tileX >= 0 && tileX < maxScreenCol && tileY >= 0 && tileY < maxScreenRow
                        && isTileInMagnetAuraShape(offsetX, offsetY)) {
                    g2.fillRect(worldToScreenX(tileX * tileSize), worldToScreenY(tileY * tileSize), tileSize, tileSize);
                }
            }
        }
    }

    public boolean isTileInMagnetAuraShape(int offsetX, int offsetY) {
        return Math.abs(offsetX) <= 2
                && Math.abs(offsetY) <= 2
                && Math.abs(offsetX) + Math.abs(offsetY) <= 3;
    }

    public void drawIceAuras(Graphics2D g2) {
        Composite originalComposite = g2.getComposite();
        Color originalColor = g2.getColor();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f)); // Ice aura alpha: nudge this 0.5f value.
        g2.setColor(new Color(0x00ffff));

        if (isPowerUpActive(powerIceType)) {
            drawIceAuraAt(g2, getPlayerCenterTileX(), getPlayerCenterTileY());
            for (PacClone clone : pacClones) {
                drawIceAuraAt(g2, getPacCloneCenterTileX(clone), getPacCloneCenterTileY(clone));
            }
        }

        for (Ghost ghost : ghosts) {
            if (ghost.type == ghostIceType) {
                drawIceAuraAt(g2, getGhostCenterTileX(ghost), getGhostCenterTileY(ghost));
            }
        }

        g2.setComposite(originalComposite);
        g2.setColor(originalColor);
    }

    public void drawIceAuraAt(Graphics2D g2, int centerTileX, int centerTileY) {
        for (int offsetX = -bombRadiusTiles; offsetX <= bombRadiusTiles; offsetX++) {
            for (int offsetY = -bombRadiusTiles; offsetY <= bombRadiusTiles; offsetY++) {
                int tileX = centerTileX + offsetX;
                int tileY = centerTileY + offsetY;

                if (tileX >= 0 && tileX < maxScreenCol && tileY >= 0 && tileY < maxScreenRow
                        && isTileInBombBlastShape(offsetX, offsetY)) {
                    g2.fillRect(worldToScreenX(tileX * tileSize), worldToScreenY(tileY * tileSize), tileSize, tileSize);
                }
            }
        }
    }

    public void drawFrozenGhosts(Graphics2D g2) {
        for (FrozenGhost frozenGhost : frozenGhosts) {
            drawImageAtTile(g2, iceCubeSprite, frozenGhost.tileX, frozenGhost.tileY);
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
        drawRotatedTileSprite(g2, sprite, getTunnelX(), 1, Math.PI / 2);
        drawRotatedTileSprite(g2, sprite, getTunnelX(), maxScreenRow - 2, -Math.PI / 2);
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
        if ((warningPortalX == -1 && warningPortals.isEmpty()) || boardFullClearAwarded || playerDead) {
            return;
        }

        BufferedImage sprite = warnSprites[(elapsedFrames / animationDelay) % warnSprites.length];
        if (warningPortals.isEmpty()) {
            drawSpawnWarningAt(g2, sprite, warningPortalX, warningPortalY);
            return;
        }

        for (int[] portal : warningPortals) {
            drawSpawnWarningAt(g2, sprite, portal[0], portal[1]);
        }
    }

    public void drawSpawnWarningAt(Graphics2D g2, BufferedImage sprite, int tileX, int tileY) {
        int screenX = worldToScreenX(tileX * tileSize);
        int screenY = worldToScreenY(tileY * tileSize);

        if (tileX == maxScreenCol - 1) {
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
        if (bombExplosionTimer <= 0 || bombExplosionTiles.isEmpty()) {
            return;
        }

        Stroke originalStroke = g2.getStroke();
        Color originalColor = g2.getColor();

        g2.setColor(new Color(0xff0000));
        g2.setStroke(new BasicStroke(2));

        for (int[] tile : bombExplosionTiles) {
            int tileX = tile[0];
            int tileY = tile[1];
            int screenX = worldToScreenX(tileX * tileSize);
            int screenY = worldToScreenY(tileY * tileSize);

            if (!containsTile(bombExplosionTiles, tileX - 1, tileY)) {
                g2.drawLine(screenX, screenY, screenX, screenY + tileSize);
            }
            if (!containsTile(bombExplosionTiles, tileX + 1, tileY)) {
                g2.drawLine(screenX + tileSize, screenY, screenX + tileSize, screenY + tileSize);
            }
            if (!containsTile(bombExplosionTiles, tileX, tileY + 1)) {
                g2.drawLine(screenX, screenY, screenX + tileSize, screenY);
            }
            if (!containsTile(bombExplosionTiles, tileX, tileY - 1)) {
                g2.drawLine(screenX, screenY + tileSize, screenX + tileSize, screenY + tileSize);
            }
        }

        g2.setStroke(originalStroke);
        g2.setColor(originalColor);
    }

    public boolean containsTile(ArrayList<int[]> tiles, int tileX, int tileY) {
        for (int[] tile : tiles) {
            if (tile[0] == tileX && tile[1] == tileY) {
                return true;
            }
        }

        return false;
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
        if (deathAnimationDone && !playerFrozenDeath && !playerElectrocutedAshVisible) {
            return;
        }

        if (playerElectrocutedAshVisible) {
            drawPlayerAshSprite(g2);
            return;
        }

        BufferedImage sprite = playerFrozenDeath
                ? pacFrozeSprite
                : playerElectrocutedDeath
                        ? pacElectrocutedSprites[(deathAnimationCounter / 3) % pacElectrocutedSprites.length]
                        : playerDead
                                ? pacSprites[deathFrame]
                                : getActivePlayerSprite();

        drawPlayerSprite(g2, sprite);
    }

    public void drawPlayerAshSprite(Graphics2D g2) {
        int screenX = worldToScreenX(playerPixelX);
        int screenY = worldToScreenY(playerPixelY);
        g2.drawImage(decalSprites[decalAshType], screenX, screenY, tileSize, tileSize, null);
    }

    public void drawPlayerSprite(Graphics2D g2, BufferedImage sprite) {
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

        if (isPowerUpActive(powerElectricType)) {
            return pacElectricSprites[frame];
        }

        if (isPowerUpActive(powerBombType)) {
            return pacBombSprites[frame];
        }

        if (isPowerUpActive(powerMetalType)) {
            return pacMetalDamaged ? pacMetalHurtSprites[frame] : pacMetalSprites[frame];
        }

        if (powerMode) {
            return pacPowerSprites[frame];
        }

        return pacSprites[frame];
    }

    public double getPlayerAngle() {
        return getDirectionAngle(lastDirectionX, lastDirectionY);
    }

    public void drawOverScreen(Graphics2D g2) {
        if (deathAnimationDone) {
            g2.drawImage(overScreen, 0, 0, getWidth(), getHeight(), null);
            if (playerFrozenDeath) {
                drawPlayerSprite(g2, pacFrozeSprite);
            } else if (playerElectrocutedAshVisible) {
                drawPlayerAshSprite(g2);
            }
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
        if (showHudScore) {
            g2.drawString("SCORE " + score, 12, 18);
        }
        g2.drawString("LEVEL " + level, getWidth() / 2 - 38, 18);
        if (showHudTime) {
            g2.drawString("TIME " + getElapsedTimeText(), getWidth() - 115, 18);
        }

        drawBoardStats(g2);
        if (showHudActivePower) {
            drawActiveEffectTimers(g2);
        }
    }

    public void drawBoardStats(Graphics2D g2) {
        int y = 38;

        g2.setColor(Color.WHITE);
        if (showHudPelletCount) {
            g2.drawString("PELLETS " + getRemainingPelletCount(), 12, y);
        }

        if (showHudBoardState) {
            String stateText = getBoardStateText();
            if (!stateText.isEmpty()) {
                g2.setColor(menuTextColor);
                g2.drawString(stateText, getWidth() / 2 - g2.getFontMetrics().stringWidth(stateText) / 2, y);
                g2.setColor(Color.WHITE);
            }
        }

        if (showHudGhostCount) {
            String ghostText = "GHOSTS " + ghosts.size();
            g2.drawString(ghostText, getWidth() - g2.getFontMetrics().stringWidth(ghostText) - 12, y);
        }
    }

    public String getBoardStateText() {
        if (showHudBoardState && boardFullClearAwarded) {
            return "BOARD CLEAR";
        }

        if (showHudBoardState && boardClear) {
            return "EXIT OPEN";
        }

        return "";
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
        if (optionCategory == -1) {
            drawOptionCategoryMenu(g2);
        } else {
            drawOptionCategoryPage(g2);
        }
    }

    public void drawOptionCategoryMenu(Graphics2D g2) {
        String[] categories = { "GAMEPLAY", "AUDIO", "UI OPTION" };
        int centerX = getWidth() / 2;
        int startY = Math.max(150, getHeight() / 2 - 76);

        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 28));
        drawCenteredMenuText(g2, "OPTION", centerX, startY - 55);
        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 24));

        for (int i = 0; i < categories.length; i++) {
            String text = i == optionChoice ? ">" + categories[i] + "<" : categories[i];
            drawCenteredMenuText(g2, text, centerX, startY + i * 42);
        }

        String saveText = optionChoice == categories.length && optionActionChoice == 0 ? ">SAVE<" : "SAVE";
        String cancelText = optionChoice == categories.length && optionActionChoice == 1 ? ">CANCEL<" : "CANCEL";
        drawCenteredMenuText(g2, saveText + "   ||   " + cancelText, centerX, startY + categories.length * 42 + 26);
    }

    public void drawOptionCategoryPage(Graphics2D g2) {
        String[] options = getCurrentOptionLabels();
        int centerX = getWidth() / 2;
        int y = 78;
        int rowSpacing = 32;

        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 25));
        drawCenteredMenuText(g2, getOptionCategoryTitle(), centerX, 42);
        g2.setFont(new Font("Bahnschrift SemiBold", Font.BOLD, 18));

        for (int i = 0; i < options.length; i++) {
            String text = i == optionChoice ? ">" + options[i] + "<" : options[i];
            drawCenteredMenuText(g2, text, centerX, y);
            y += rowSpacing;
        }

        String backText = optionChoice == options.length ? ">BACK<" : "BACK";
        drawCenteredMenuText(g2, backText, centerX, y + 10);
    }

    public String getOptionCategoryTitle() {
        if (optionCategory == 0) {
            return "GAMEPLAY";
        }
        if (optionCategory == 1) {
            return "AUDIO";
        }
        return "UI OPTION";
    }

    public String[] getCurrentOptionLabels() {
        if (optionCategory == 0) {
            return getGameplayOptionLabels();
        }
        if (optionCategory == 1) {
            return getAudioOptionLabels();
        }
        if (optionCategory == 2) {
            return getUiOptionLabels();
        }

        return new String[0];
    }

    public String[] getGameplayOptionLabels() {
        return new String[] {
            "GHOST SPEED: " + formatDouble(draftGhostSpeed),
            "PACMAN SPEED: " + formatDouble(draftPlayerSpeed),
            "GHOST SPAWN INTERVAL: " + draftGhostSpawnSeconds,
            "GHOST PER WAVE: " + draftGhostPerWave,
            "PELLET TIME: " + draftPelletSeconds,
            "POWER TIME: " + draftPowerSeconds,
            "SPECIAL GHOST CHANCE: " + draftSpecialGhostChancePercent + "%",
            "SPECIAL POWER DROP: " + draftSpecialGhostPowerDropChancePercent + "%",
            "NO POWER MODE: " + draftNoPowerMode,
            "NO SUPER GHOST MODE: " + draftNoSuperGhostMode,
            "MAZE WIDTH: " + draftMazeWidth,
            "MAZE HEIGHT: " + draftMazeHeight
        };
    }

    public String[] getAudioOptionLabels() {
        return new String[] {
            "MASTER VOLUME: " + draftMasterVolume + "%",
            "MUSIC VOLUME: " + draftMusicVolume + "%",
            "SFX VOLUME: " + draftSfxVolume + "%"
        };
    }

    public String[] getUiOptionLabels() {
        return new String[] {
            areAnyDraftUiOptionsEnabled() ? "DISABLE ALL UI" : "ENABLE ALL UI",
            "SCORE: " + draftShowHudScore,
            "TIME: " + draftShowHudTime,
            "ACTIVE POWER: " + draftShowHudActivePower,
            "BOARD STATE: " + draftShowHudBoardState,
            "PELLET COUNT: " + draftShowHudPelletCount,
            "GHOST COUNT: " + draftShowHudGhostCount
        };
    }

    public boolean areAnyDraftUiOptionsEnabled() {
        return draftShowHudScore
                || draftShowHudTime
                || draftShowHudActivePower
                || draftShowHudBoardState
                || draftShowHudPelletCount
                || draftShowHudGhostCount;
    }

    public void setAllDraftUiOptions(boolean enabled) {
        draftShowHudScore = enabled;
        draftShowHudTime = enabled;
        draftShowHudActivePower = enabled;
        draftShowHudBoardState = enabled;
        draftShowHudPelletCount = enabled;
        draftShowHudGhostCount = enabled;
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
        boolean flipped = almanacIndex != 0
                && almanacIndex != 11
                && almanacIndex != 12
                && almanacIndex != 13
                && almanacIndex != 14
                && almanacIndex != 15
                && almanacIndex != 16
                && getMenuAnimationFrame(2) == 1;

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
        if (index == 12) {
            return ghostIceSprites[frame];
        }
        if (index == 13) {
            return ghostCactusSprites[frame];
        }
        if (index == 14) {
            return ghostMetalSprites[frame];
        }
        if (index == 15) {
            return ghostWaveSprites[frame];
        }
        if (index == 16) {
            return ghostPikaChargedSprites[getMenuAnimationFrame(ghostPikaChargedSprites.length)];
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
                g2.setColor(menuTextColor);
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
        g2.setColor(menuTextColor);
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
        soundManager.startMusic();
    }

    public void openOptions() {
        draftGhostSpeed = ghostSpeed;
        draftPlayerSpeed = playerSpeed;
        draftGhostSpawnSeconds = ghostSpawnInterval / framesPerSecond;
        draftGhostPerWave = ghostPerWave;
        draftPelletSeconds = powerPelletDuration / framesPerSecond;
        draftPowerSeconds = powerUpDuration / framesPerSecond;
        draftSpecialGhostChancePercent = specialGhostChancePercent;
        draftSpecialGhostPowerDropChancePercent = specialGhostPowerDropChancePercent;
        draftNoPowerMode = noPowerMode;
        draftNoSuperGhostMode = noSuperGhostMode;
        draftMazeWidth = normalizeOddInt(maxScreenCol, 11, 51);
        draftMazeHeight = normalizeOddInt(maxScreenRow, 11, 51);
        draftMasterVolume = masterVolume;
        draftMusicVolume = musicVolume;
        draftSfxVolume = sfxVolume;
        draftShowHudScore = showHudScore;
        draftShowHudTime = showHudTime;
        draftShowHudActivePower = showHudActivePower;
        draftShowHudBoardState = showHudBoardState;
        draftShowHudPelletCount = showHudPelletCount;
        draftShowHudGhostCount = showHudGhostCount;
        optionCategory = -1;
        optionChoice = 0;
        optionActionChoice = 0;
        screenState = STATE_OPTIONS;
    }

    public void saveOptions() {
        ghostSpeed = draftGhostSpeed;
        playerSpeed = draftPlayerSpeed;
        ghostSpawnInterval = draftGhostSpawnSeconds * framesPerSecond;
        ghostPerWave = draftGhostPerWave;
        powerPelletDuration = draftPelletSeconds * framesPerSecond;
        powerUpDuration = draftPowerSeconds * framesPerSecond;
        specialGhostChancePercent = draftSpecialGhostChancePercent;
        specialGhostPowerDropChancePercent = draftSpecialGhostPowerDropChancePercent;
        noPowerMode = draftNoPowerMode;
        noSuperGhostMode = draftNoSuperGhostMode;
        maxScreenCol = normalizeOddInt(draftMazeWidth, 11, 51);
        maxScreenRow = normalizeOddInt(draftMazeHeight, 11, 51);
        masterVolume = draftMasterVolume;
        musicVolume = draftMusicVolume;
        sfxVolume = draftSfxVolume;
        showHudScore = draftShowHudScore;
        showHudTime = draftShowHudTime;
        showHudActivePower = draftShowHudActivePower;
        showHudBoardState = draftShowHudBoardState;
        showHudPelletCount = draftShowHudPelletCount;
        showHudGhostCount = draftShowHudGhostCount;
        applyAudioVolumes(masterVolume, musicVolume, sfxVolume);
        tunnelY = maxScreenRow / 2;
        updatePanelSize();
        writeOptions();
        screenState = STATE_MENU;
    }

    public void cancelOptions() {
        draftMasterVolume = masterVolume;
        draftMusicVolume = musicVolume;
        draftSfxVolume = sfxVolume;
        draftShowHudScore = showHudScore;
        draftShowHudTime = showHudTime;
        draftShowHudActivePower = showHudActivePower;
        draftShowHudBoardState = showHudBoardState;
        draftShowHudPelletCount = showHudPelletCount;
        draftShowHudGhostCount = showHudGhostCount;
        applyAudioVolumes(masterVolume, musicVolume, sfxVolume);
        optionCategory = -1;
        screenState = STATE_MENU;
    }

    public void applyAudioVolumes(int master, int music, int sfx) {
        soundManager.setMasterVolume(master);
        soundManager.setMusicVolume(music);
        soundManager.setSfxVolume(sfx);
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
            soundManager.playMenuMove();
        } else if (isMoveDownKey(keyCode) || isMoveRightKey(keyCode)) {
            menuChoice = (menuChoice + 1) % 5;
            soundManager.playMenuMove();
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            soundManager.playMenuConfirm();
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
        int optionCount = getOptionChoiceCount();

        if (isMoveUpKey(keyCode)) {
            optionChoice = (optionChoice + optionCount - 1) % optionCount;
            soundManager.playMenuMove();
        } else if (isMoveDownKey(keyCode)) {
            optionChoice = (optionChoice + 1) % optionCount;
            soundManager.playMenuMove();
        } else if (isMoveLeftKey(keyCode)) {
            changeOption(-1);
            soundManager.playMenuMove();
        } else if (isMoveRightKey(keyCode)) {
            changeOption(1);
            soundManager.playMenuMove();
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            soundManager.playMenuConfirm();
            selectOption();
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            soundManager.playMenuConfirm();
            backOutOfOptions();
        }
    }

    public int getOptionChoiceCount() {
        if (optionCategory == -1) {
            return 4;
        }

        return getCurrentOptionLabels().length + 1;
    }

    public void changeOption(int direction) {
        if (optionCategory == -1) {
            if (optionChoice == 3) {
                optionActionChoice = optionActionChoice == 0 ? 1 : 0;
            }
            return;
        }

        if (optionChoice >= getCurrentOptionLabels().length) {
            return;
        }

        if (optionCategory == 0) {
            changeGameplayOption(direction);
        } else if (optionCategory == 1) {
            changeAudioOption(direction);
        } else if (optionCategory == 2) {
            changeUiOption();
        }
    }

    public void changeGameplayOption(int direction) {
        if (optionChoice == 0) {
            draftGhostSpeed = clampDouble(draftGhostSpeed + direction * 0.5, 1, 10);
        } else if (optionChoice == 1) {
            draftPlayerSpeed = clampDouble(draftPlayerSpeed + direction * 0.5, 1, 10);
        } else if (optionChoice == 2) {
            draftGhostSpawnSeconds = clampInt(draftGhostSpawnSeconds + direction, 1, 60);
        } else if (optionChoice == 3) {
            draftGhostPerWave = clampInt(draftGhostPerWave + direction, 1, 4);
        } else if (optionChoice == 4) {
            draftPelletSeconds = clampInt(draftPelletSeconds + direction, 1, 20);
        } else if (optionChoice == 5) {
            draftPowerSeconds = clampInt(draftPowerSeconds + direction, 1, 20);
        } else if (optionChoice == 6) {
            draftSpecialGhostChancePercent = clampInt(draftSpecialGhostChancePercent + direction * 5, 0, 100);
        } else if (optionChoice == 7) {
            draftSpecialGhostPowerDropChancePercent = clampInt(
                    draftSpecialGhostPowerDropChancePercent + direction * 5,
                    0,
                    100);
        } else if (optionChoice == 8) {
            draftNoPowerMode = !draftNoPowerMode;
        } else if (optionChoice == 9) {
            draftNoSuperGhostMode = !draftNoSuperGhostMode;
        } else if (optionChoice == 10) {
            draftMazeWidth = normalizeOddInt(draftMazeWidth + direction * 2, 11, 51);
        } else if (optionChoice == 11) {
            draftMazeHeight = normalizeOddInt(draftMazeHeight + direction * 2, 11, 51);
        }
    }

    public void changeAudioOption(int direction) {
        if (optionChoice == 0) {
            draftMasterVolume = clampInt(draftMasterVolume + direction * 5, 0, 100);
            applyAudioVolumes(draftMasterVolume, draftMusicVolume, draftSfxVolume);
        } else if (optionChoice == 1) {
            draftMusicVolume = clampInt(draftMusicVolume + direction * 5, 0, 100);
            applyAudioVolumes(draftMasterVolume, draftMusicVolume, draftSfxVolume);
        } else if (optionChoice == 2) {
            draftSfxVolume = clampInt(draftSfxVolume + direction * 5, 0, 100);
            applyAudioVolumes(draftMasterVolume, draftMusicVolume, draftSfxVolume);
        }
    }

    public void changeUiOption() {
        if (optionChoice == 0) {
            setAllDraftUiOptions(!areAnyDraftUiOptionsEnabled());
        } else if (optionChoice == 1) {
            draftShowHudScore = !draftShowHudScore;
        } else if (optionChoice == 2) {
            draftShowHudTime = !draftShowHudTime;
        } else if (optionChoice == 3) {
            draftShowHudActivePower = !draftShowHudActivePower;
        } else if (optionChoice == 4) {
            draftShowHudBoardState = !draftShowHudBoardState;
        } else if (optionChoice == 5) {
            draftShowHudPelletCount = !draftShowHudPelletCount;
        } else if (optionChoice == 6) {
            draftShowHudGhostCount = !draftShowHudGhostCount;
        }
    }

    public void selectOption() {
        if (optionCategory == -1) {
            if (optionChoice < 3) {
                optionCategory = optionChoice;
                optionChoice = 0;
                return;
            }

            if (optionActionChoice == 0) {
                saveOptions();
            } else {
                cancelOptions();
            }
            return;
        }

        if (optionChoice < getCurrentOptionLabels().length) {
            changeOption(1);
            return;
        }

        optionCategory = -1;
        optionChoice = 0;
    }

    public void backOutOfOptions() {
        if (optionCategory != -1) {
            optionCategory = -1;
            optionChoice = 0;
            return;
        }

        cancelOptions();
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
            return "BONUS";
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
        if (powerUpType == powerIceType) {
            return "ICE";
        }
        if (powerUpType == powerMetalType) {
            return "METAL";
        }
        if (powerUpType == powerWaterType) {
            return "WATER";
        }
        if (powerUpType == powerElectricType) {
            return "ELECTRIC";
        }

        return "POWER";
    }

    public Color getPowerUpHudColor(int powerUpType) {
        if (powerUpType == 0) {
            return Color.WHITE;
        }
        if (powerUpType == 1) {
            return new Color(0x7f7f7f);
        }
        if (powerUpType == 2) {
            return new Color(0x7fff7f);
        }
        if (powerUpType == 3) {
            return new Color(0xffff00);
        }
        if (powerUpType == powerBombType) {
            return new Color(0xff0000);
        }
        if (powerUpType == powerLaserType) {
            return new Color(0xff7f00);
        }
        if (powerUpType == powerCloneType) {
            return Color.BLACK;
        }
        if (powerUpType == powerFireType) {
            return new Color(0xff7f7f);
        }
        if (powerUpType == powerIceType) {
            return new Color(0x00ffff);
        }
        if (powerUpType == powerMetalType) {
            return new Color(0xbfbfbf);
        }
        if (powerUpType == powerWaterType) {
            return new Color(0x007fff);
        }
        if (powerUpType == powerElectricType) {
            return new Color(0x7fffff);
        }

        return Color.GREEN;
    }

    public String getElapsedTimeText() {
        int totalSeconds = elapsedFrames / framesPerSecond;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public void drawActiveEffectTimers(Graphics2D g2) {
        int x = 12;
        int y = 60;
        int maxX = getWidth() - 12;
        int rowHeight = 22;
        int lastTimerRowY = 82;

        if (powerMode) {
            String text = "PELLET " + getTimerSeconds(powerModeTimer);
            x = drawActiveTimerToken(g2, text, x, y, maxX, Color.CYAN, false);

            if (x == 12) {
                y += rowHeight;
            }
        }

        for (int i = 0; i < powerUpTimers.length; i++) {
            if (i == 4 || powerUpTimers[i] <= 0) {
                continue;
            }

            String text = getPowerUpName(i) + " " + getTimerSeconds(powerUpTimers[i]);
            Color color = getPowerUpHudColor(i);
            boolean whiteOutline = i == powerCloneType;

            if (x + g2.getFontMetrics().stringWidth(text) > maxX && x > 12) {
                x = 12;
                y += rowHeight;
            }

            if (y > lastTimerRowY) {
                return;
            }

            x = drawActiveTimerToken(g2, text, x, y, maxX, color, whiteOutline);
        }
    }

    public int drawActiveTimerToken(Graphics2D g2, String text, int x, int y, int maxX, Color color, boolean whiteOutline) {
        int tokenWidth = g2.getFontMetrics().stringWidth(text);

        if (x + tokenWidth > maxX && x > 12) {
            return 12;
        }

        if (whiteOutline) {
            g2.setColor(Color.WHITE);
            g2.drawString(text, x - 1, y);
            g2.drawString(text, x + 1, y);
            g2.drawString(text, x, y - 1);
            g2.drawString(text, x, y + 1);
        }

        g2.setColor(color);
        g2.drawString(text, x, y);
        return x + tokenWidth + 18;
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

        if (ghost.electrocutedTimer > 0
                || ghost.type == ghostMagnetType
                || ghost.type == ghostIceType
                || ghost.type == ghostCactusType
                || ghost.type == ghostMetalType
                || ghost.type == ghostWaveType
                || ghost.type == ghostFlashType) {
            g2.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        } else if (flipped) {
            g2.drawImage(sprite, screenX + tileSize, screenY, -tileSize, tileSize, null);
        } else {
            g2.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        }
    }

    public BufferedImage getGhostSprite(Ghost ghost) {
        if (ghost.electrocutedTimer > 0) {
            return ghostElectrocutedSprites[(ghostAnimationCounter / 3) % ghostElectrocutedSprites.length];
        }

        if (ghost.type == ghostCloneType) {
            return ghostCloneSprite;
        }
        if (ghost.type == ghostBombType) {
            if (ghost.fuseTimer > 0) {
                return ghostBombSprites[1 + (ghostAnimationCounter / animationDelay) % 2];
            }

            return ghostBombSprites[0];
        }
        if (ghost.type == ghostMetalType) {
            if (ghost.fuseTimer > 0) {
                if (ghost.electrocutedFuse) {
                    return ghostElectrocutedSprites[(ghostAnimationCounter / 3) % ghostElectrocutedSprites.length];
                }

                return ghostMetalSprites[2 + (ghostAnimationCounter / animationDelay) % 2];
            }

            return ghostMetalSprites[(ghostAnimationCounter / animationDelay) % 2];
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
        if (ghost.type == ghostIceType) {
            return ghostIceSprites[(ghostAnimationCounter / animationDelay) % ghostIceSprites.length];
        }
        if (ghost.type == ghostCactusType) {
            return ghostCactusSprites[(ghostAnimationCounter / animationDelay) % ghostCactusSprites.length];
        }
        if (ghost.type == ghostWaveType) {
            return ghostWaveSprites[(ghostAnimationCounter / animationDelay) % ghostWaveSprites.length];
        }
        if (ghost.type == ghostFlashType) {
            if (ghost.flashyCharged) {
                return ghostPikaChargedSprites[(ghostAnimationCounter / 3) % ghostPikaChargedSprites.length];
            }

            if (ghost.flashyTimer <= ghostFlashRechargeWarningTime) {
                return ghostPikaAimSprites[(ghostAnimationCounter / animationDelay) % ghostPikaAimSprites.length];
            }

            return ghostPikaExhaustedSprites[(ghostAnimationCounter / animationDelay) % ghostPikaExhaustedSprites.length];
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
            soundManager.playMenuConfirm();
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

        if (handleCameraZoomKey(keyCode)) {
            return;
        }

        if (keyCode == KeyEvent.VK_R) {
            soundManager.playMenuConfirm();
            resetGame();
            screenState = STATE_GAME;
            soundManager.startMusic();
            return;
        }

        if (keyCode == KeyEvent.VK_ESCAPE && !playerDead && !deathAnimationDone) {
            handleGameEscape();
            return;
        }

        if (keyCode == KeyEvent.VK_P && !playerDead && !deathAnimationDone) {
            paused = !paused;
            quitConfirmVisible = false;
            if (paused) {
                soundManager.stopLaser();
            }
            soundManager.setMusicPaused(paused);
            return;
        }

        if (playerDead) {
            return;
        }

        if (paused) {
            return;
        }

        if (keyCode == KeyEvent.VK_Q && isPowerUpActive(powerElectricType) && isPowerUpActive(powerBombType)) {
            gameStarted = true;
            triggerElectricBombExplosion();
            return;
        }

        if (keyCode == KeyEvent.VK_Q && isPowerUpActive(powerElectricType)) {
            gameStarted = true;
            electricKeyHeld = true;
            return;
        }

        if (keyCode == KeyEvent.VK_Q && isPowerUpActive(powerBombType)) {
            gameStarted = true;
            if (isPowerUpActive(powerCloneType)) {
                triggerPacCloneBombExplosionsAndEndClonePower();
            }
            triggerBombExplosion();
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

    public boolean handleCameraZoomKey(int keyCode) {
        if (keyCode == KeyEvent.VK_Z) {
            adjustCameraZoom(-cameraZoomStep);
            return true;
        }
        if (keyCode == KeyEvent.VK_C) {
            adjustCameraZoom(cameraZoomStep);
            return true;
        }
        if (keyCode == KeyEvent.VK_X) {
            setCameraZoomTarget(defaultCameraZoom);
            return true;
        }

        return false;
    }

    public void adjustCameraZoom(double amount) {
        setCameraZoomTarget(targetCameraZoom + amount);
    }

    public void setCameraZoomTarget(double zoom) {
        startCameraZoom = cameraZoom;
        targetCameraZoom = clampDouble(zoom, minCameraZoom, maxCameraZoom);
        cameraZoomAnimationFrame = 0;
    }

    public void updateCameraZoomAnimation() {
        if (cameraZoomAnimationFrame >= cameraZoomAnimationDuration) {
            cameraZoom = targetCameraZoom;
            return;
        }

        cameraZoomAnimationFrame++;
        double progress = cameraZoomAnimationFrame / (double) cameraZoomAnimationDuration;
        double easedProgress = (1.0 - Math.cos(progress * Math.PI)) / 2.0;
        cameraZoom = startCameraZoom + (targetCameraZoom - startCameraZoom) * easedProgress;
    }

    public void handleGameEscape() {
        soundManager.playMenuConfirm();

        if (!paused) {
            paused = true;
            quitConfirmVisible = false;
            soundManager.stopLaser();
            soundManager.setMusicPaused(true);
            return;
        }

        if (!quitConfirmVisible) {
            quitConfirmVisible = true;
            return;
        }

        resetGame();
        screenState = STATE_MENU;
        soundManager.fadeOutMusicAndStop();
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
            soundManager.playMenuConfirm();
            screenState = STATE_MENU;
            return;
        }

        if (almanacTitles.isEmpty()) {
            return;
        }

        if (isMoveLeftKey(keyCode) || isMoveUpKey(keyCode)) {
            almanacIndex = (almanacIndex + almanacTitles.size() - 1) % almanacTitles.size();
            soundManager.playMenuMove();
        } else if (isMoveRightKey(keyCode) || isMoveDownKey(keyCode)) {
            almanacIndex = (almanacIndex + 1) % almanacTitles.size();
            soundManager.playMenuMove();
        }
    }

    public void handleNameEntryKey(int keyCode) {
        if (isMoveLeftKey(keyCode)) {
            nameEntryIndex = (nameEntryIndex + nameEntry.length - 1) % nameEntry.length;
            soundManager.playMenuMove();
        } else if (isMoveRightKey(keyCode)) {
            nameEntryIndex = (nameEntryIndex + 1) % nameEntry.length;
            soundManager.playMenuMove();
        } else if (isMoveUpKey(keyCode)) {
            nameEntry[nameEntryIndex] = nameEntry[nameEntryIndex] == 'Z' ? 'A' : (char) (nameEntry[nameEntryIndex] + 1);
            soundManager.playMenuMove();
        } else if (isMoveDownKey(keyCode)) {
            nameEntry[nameEntryIndex] = nameEntry[nameEntryIndex] == 'A' ? 'Z' : (char) (nameEntry[nameEntryIndex] - 1);
            soundManager.playMenuMove();
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            soundManager.playMenuConfirm();
            saveNewHighScore();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_Q) {
            electricKeyHeld = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

}
