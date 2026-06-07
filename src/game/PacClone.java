package game;

import java.util.ArrayList;

public class PacClone {

    double pixelX;
    double pixelY;
    double targetPixelX;
    double targetPixelY;
    int directionX;
    int directionY;
    int lastFireTileX = -1;
    int lastFireTileY = -1;
    ArrayList<int[]> path = new ArrayList<>();

    PacClone(double pixelX, double pixelY) {
        this.pixelX = pixelX;
        this.pixelY = pixelY;
        this.targetPixelX = pixelX;
        this.targetPixelY = pixelY;
    }
}
