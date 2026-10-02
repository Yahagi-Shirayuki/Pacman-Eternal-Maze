package game;

import java.awt.image.BufferedImage;

/** A lightweight, non-spinning sprite particle used by trails and status effects. */
public class SpriteParticle extends Particle {

    BufferedImage sprite;

    SpriteParticle(double centerX, double centerY, double velocityX, double velocityY,
            double scale, BufferedImage sprite) {
        super(centerX, centerY, velocityX, velocityY, 0.0, 0.0, scale);
        this.sprite = sprite;
    }
}
