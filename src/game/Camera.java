package game;

public class Camera {

    double viewX;
    double viewY;

    public void update(double focusX, double focusY, int worldWidth, int worldHeight, double viewportWidth, double viewportHeight) {
        update(focusX, focusY, 0, 0, worldWidth, worldHeight, viewportWidth, viewportHeight);
    }

    public void update(
            double focusX,
            double focusY,
            double worldMinX,
            double worldMinY,
            int worldWidth,
            int worldHeight,
            double viewportWidth,
            double viewportHeight) {
        viewX = getViewStart(focusX, worldMinX, worldWidth, viewportWidth);
        viewY = getViewStart(focusY, worldMinY, worldHeight, viewportHeight);
    }

    private double getViewStart(double focus, double worldMin, int worldSize, double viewportSize) {
        if (worldSize <= viewportSize) {
            return worldMin - (viewportSize - worldSize) / 2.0;
        }

        return clamp(focus - viewportSize / 2.0, worldMin, worldMin + worldSize - viewportSize);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
