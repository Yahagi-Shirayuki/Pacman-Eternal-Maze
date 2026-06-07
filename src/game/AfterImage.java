package game;

import java.awt.image.BufferedImage;

public class AfterImage {

    BufferedImage sprite;
    double pixelX;
    double pixelY;
    double angle;
    boolean flipped;
    int alpha = 255;

    AfterImage(BufferedImage sprite, double pixelX, double pixelY, double angle, boolean flipped) {
        this.sprite = sprite;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
        this.angle = angle;
        this.flipped = flipped;
    }
}
