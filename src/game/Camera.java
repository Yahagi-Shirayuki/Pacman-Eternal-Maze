package game;

public class Camera {

    double viewX;
    double viewY;

    public void update(double focusX, double focusY, int worldWidth, int worldHeight, double viewportWidth, double viewportHeight) {
        viewX = getViewStart(focusX, worldWidth, viewportWidth);
        viewY = getViewStart(focusY, worldHeight, viewportHeight);
    }

    private double getViewStart(double focus, int worldSize, double viewportSize) {
        if (worldSize <= viewportSize) {
            return -(viewportSize - worldSize) / 2.0;
        }

        return clamp(focus - viewportSize / 2.0, 0, worldSize - viewportSize);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
