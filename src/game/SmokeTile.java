package game;

public class SmokeTile {
    int tileX;
    int tileY;
    int variant;
    int age;
    int frame;
    int frameDirection;
    int frameTimer;
    int frameHoldTimer;
    boolean frameAnimating;
    int chainTimer = -1;
    int evilFireTimer = 0;
    boolean evilFire = false;

    SmokeTile(int tileX, int tileY, int variant) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.variant = variant;
    }
}
