package game;

import java.awt.image.BufferedImage;

/**
 * Creates desaturated copies of sprites and other transparent images.
 *
 * <p>The source image is never modified. A saturation value of {@code 0.0f}
 * produces a full grayscale image, while {@code 1.0f} preserves the original
 * colors. Alpha is copied unchanged.</p>
 */
public final class GrayscaleRenderer {

    private GrayscaleRenderer() {
    }

    public static BufferedImage toGrayscale(BufferedImage source) {
        return withSaturation(source, 0.0f);
    }

    public static BufferedImage withSaturation(BufferedImage source, float saturation) {
        if (source == null) {
            return null;
        }

        float clampedSaturation = Math.max(0.0f, Math.min(1.0f, saturation));
        BufferedImage destination = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xff;
                int red = (argb >>> 16) & 0xff;
                int green = (argb >>> 8) & 0xff;
                int blue = argb & 0xff;

                int luminance = clampChannel(Math.round(
                        red * 0.299f + green * 0.587f + blue * 0.114f));
                int outputRed = blendChannel(luminance, red, clampedSaturation);
                int outputGreen = blendChannel(luminance, green, clampedSaturation);
                int outputBlue = blendChannel(luminance, blue, clampedSaturation);

                destination.setRGB(x, y,
                        (alpha << 24)
                                | (outputRed << 16)
                                | (outputGreen << 8)
                                | outputBlue);
            }
        }

        return destination;
    }

    private static int blendChannel(int grayscaleValue, int originalValue, float saturation) {
        return clampChannel(Math.round(
                grayscaleValue + (originalValue - grayscaleValue) * saturation));
    }

    private static int clampChannel(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
