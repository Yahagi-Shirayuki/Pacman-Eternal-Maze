package game;

public class FrozenGhost {
    int tileX;
    int tileY;
    int type;
    double pixelX;
    double pixelY;
    int directionX;
    int directionY;
    boolean moving;
    boolean fromClone;
    boolean mortisBuffed;
    boolean bonusAwarded;
    int playerSafetyTimer;

    FrozenGhost(int type, double pixelX, double pixelY, boolean fromClone) {
        this.type = type;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
        this.fromClone = fromClone;
    }
}
