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
    int preferredTurnCount = 0;
    ArrayList<int[]> path = new ArrayList<>();

    Ghost(int type, int tileX, int tileY, int tileSize) {
        this.type = type;
        this.pixelX = tileX * tileSize;
        this.pixelY = tileY * tileSize;
        this.targetPixelX = pixelX;
        this.targetPixelY = pixelY;
    }
}
