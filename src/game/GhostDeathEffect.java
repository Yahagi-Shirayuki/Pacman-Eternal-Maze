package game;

import java.util.ArrayList;

public class GhostDeathEffect {

    double pixelX;
    double pixelY;
    double targetPixelX;
    double targetPixelY;
    int deathFrameTimer;
    ArrayList<int[]> path = new ArrayList<>();

    GhostDeathEffect(double pixelX, double pixelY, ArrayList<int[]> path, int deathFrameTimer) {
        this.pixelX = pixelX;
        this.pixelY = pixelY;
        this.targetPixelX = pixelX;
        this.targetPixelY = pixelY;
        this.path = path;
        this.deathFrameTimer = deathFrameTimer;
    }

    boolean isShowingDeathFrame() {
        return deathFrameTimer > 0;
    }
}
