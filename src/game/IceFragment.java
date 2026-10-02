package game;

public class IceFragment extends Particle {

    int spriteIndex;

    IceFragment(double centerX, double centerY, double velocityX, double velocityY,
            double angle, double angularVelocity, double scale, int spriteIndex) {
        super(centerX, centerY, velocityX, velocityY, angle, angularVelocity, scale);
        this.spriteIndex = spriteIndex;
    }
}
