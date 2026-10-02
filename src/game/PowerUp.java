package game;

public class PowerUp {

    int type;
    int tileX;
    int tileY;
    boolean pill;

    PowerUp(int type, int tileX, int tileY) {
        this(type, tileX, tileY, false);
    }

    PowerUp(int type, int tileX, int tileY, boolean pill) {
        this.type = type;
        this.tileX = tileX;
        this.tileY = tileY;
        this.pill = pill;
    }
}
