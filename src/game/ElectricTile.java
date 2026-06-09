package game;

public class ElectricTile {
    int tileX;
    int tileY;
    int frameOffset;
    Ghost sourceGhost;

    ElectricTile(int tileX, int tileY, int frameOffset, Ghost sourceGhost) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.frameOffset = frameOffset;
        this.sourceGhost = sourceGhost;
    }
}
