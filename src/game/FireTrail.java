package game;

public class FireTrail {

    int tileX;
    int tileY;
    int timer;
    int age;
    boolean ghostFire;

    FireTrail(int tileX, int tileY, int timer, boolean ghostFire) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.timer = timer;
        this.ghostFire = ghostFire;
    }
}
