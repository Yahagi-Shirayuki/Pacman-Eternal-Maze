package game;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Renders pixel sprites with a fixed palette and integer-snapped rotation.
 * The renderer deliberately avoids interpolation so rotated sprites retain a
 * crisp, grid-aligned pixel look.
 */
public final class PaletteSpriteRenderer {

    public static final Color[] ICE_FRAGMENT_PALETTE = {
            Color.WHITE,
            new Color(0x00FFFF),
            new Color(0x007FFF)
    };

    private final Color[] palette;

    public PaletteSpriteRenderer(Color[] palette) {
        if (palette == null || palette.length == 0) {
            throw new IllegalArgumentException("Palette must contain at least one color");
        }
        this.palette = palette.clone();
    }

    public BufferedImage renderSprite(BufferedImage sprite, double rotation) {
        return renderSprite(
                sprite,
                rotation,
                sprite.getWidth() / 2.0,
                sprite.getHeight() / 2.0);
    }

    public BufferedImage renderSprite(BufferedImage sprite, double rotation,
            double pivotX, double pivotY) {
        BufferedImage destination = new BufferedImage(
                sprite.getWidth(),
                sprite.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        double cosine = Math.cos(rotation);
        double sine = Math.sin(rotation);

        for (int sourceY = 0; sourceY < sprite.getHeight(); sourceY++) {
            for (int sourceX = 0; sourceX < sprite.getWidth(); sourceX++) {
                int originalPixel = sprite.getRGB(sourceX, sourceY);
                int alpha = (originalPixel >>> 24) & 0xFF;
                if (alpha == 0) {
                    continue;
                }

                double relativeX = sourceX - pivotX;
                double relativeY = sourceY - pivotY;
                double transformedX = relativeX * cosine - relativeY * sine + pivotX;
                double transformedY = relativeX * sine + relativeY * cosine + pivotY;

                int snappedX = (int) Math.round(transformedX);
                int snappedY = (int) Math.round(transformedY);
                if (snappedX < 0 || snappedX >= destination.getWidth()
                        || snappedY < 0 || snappedY >= destination.getHeight()) {
                    continue;
                }

                Color nearestColor = findNearestPaletteColor(new Color(originalPixel, true));
                int destinationPixel = (alpha << 24) | (nearestColor.getRGB() & 0x00FFFFFF);
                destination.setRGB(snappedX, snappedY, destinationPixel);
            }
        }

        return destination;
    }

    public Color findNearestPaletteColor(Color pixel) {
        Color closestColor = palette[0];
        long smallestDistance = Long.MAX_VALUE;

        for (Color paletteColor : palette) {
            long redDifference = pixel.getRed() - paletteColor.getRed();
            long greenDifference = pixel.getGreen() - paletteColor.getGreen();
            long blueDifference = pixel.getBlue() - paletteColor.getBlue();
            long distance = redDifference * redDifference
                    + greenDifference * greenDifference
                    + blueDifference * blueDifference;

            if (distance < smallestDistance) {
                smallestDistance = distance;
                closestColor = paletteColor;
            }
        }

        return closestColor;
    }
}
