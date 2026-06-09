package game;

public class WaterTile {
    int tileX;
    int tileY;
    int variation;
    int freezeTimer = -1;

    WaterTile(int tileX, int tileY, int variation) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.variation = variation;
    }
}
