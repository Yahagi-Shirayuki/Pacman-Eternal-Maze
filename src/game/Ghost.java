package game;

import java.util.ArrayList;

public class Ghost {

    int type;
    double pixelX;
    double pixelY;
    double targetPixelX;
    double targetPixelY;
    int directionX;
    int directionY;
    int goalX;
    int goalY;
    double speedOffset;
    int preferredTurnCount = 0;
    int chargeTimer = 0;
    int warningTimer = 0;
    int activeTimer = 0;
    int fuseTimer = 0;
    int speedRampTimer = 0;
    int restTimer = 0;
    int pelletsCollected = 0;
    boolean laserActive = false;
    boolean speedDashActive = false;
    ArrayList<int[]> path = new ArrayList<>();

    Ghost(int type, int tileX, int tileY, int tileSize) {
        this.type = type;
        this.pixelX = tileX * tileSize;
        this.pixelY = tileY * tileSize;
        this.targetPixelX = pixelX;
        this.targetPixelY = pixelY;
    }
}
