package game;

public class Particle {

    double centerX;
    double centerY;
    double velocityX;
    double velocityY;
    double angle;
    double angularVelocity;
    double baseScale;
    double scale;
    double opacity = 1.0;
    int age;

    Particle(double centerX, double centerY, double velocityX, double velocityY,
            double angle, double angularVelocity, double scale) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.angle = angle;
        this.angularVelocity = angularVelocity;
        this.baseScale = scale;
        this.scale = scale;
    }
}
