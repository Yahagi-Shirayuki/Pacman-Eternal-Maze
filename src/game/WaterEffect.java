package game;

public class WaterEffect {
    int tileX;
    int tileY;
    int holdTimer;
    int timer;
    int duration;

    WaterEffect(int tileX, int tileY, int holdDuration, int duration) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.holdTimer = holdDuration;
        this.timer = duration;
        this.duration = duration;
    }
}
