package carcassonne.view;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import carcassonne.model.Player;
import carcassonne.model.terrain.TerrainType;
import carcassonne.settings.GameSettings;

/**
 * This is the Carcassonne paint shop! It paints meeple images and tile
 * highlights!
 * 
 * @author Timur Saglam
 */
public class PaintShop {
    private static final int MAXIMAL_ALPHA = 255;

    private final Map<TerrainType, BufferedImage> imageMap;
    private final Map<TerrainType, BufferedImage> templateMap;
    private final BufferedImage highlightImage;
    private final BufferedImage emblemImage;

    /**
     * Basic constructor, creates base images and templates.
     */
    public PaintShop() {
        imageMap = buildImageMap(false);
        templateMap = buildImageMap(true);
        highlightImage = loadImage(GameSettings.HIGHLIGHT_PATH);
        emblemImage = loadImage(GameSettings.EMBLEM_PATH);
    }

    /**
     * Returns a custom colored meeple.
     * 
     * @param meepleType is the type of the meeple.
     * @param color      is the custom color.
     * @return the colored meeple.
     */
    public ImageIcon getColoredMeeple(TerrainType meepleType, Color color) {
        return paintMeeple(meepleType, color.getRGB());
    }

    /**
     * Returns a custom colored meeple.
     * 
     * @param meepleType is the type of the meeple.
     * @param player     is the {@link Player} whose color is used.
     * @return the colored meeple.
     */
    public ImageIcon getColoredMeeple(TerrainType meepleType, Player player) {
        return getColoredMeeple(meepleType, player.getColor());
    }

    /**
     * Returns a custom colored highlight image.
     * 
     * @param player determines the color of the highlight.
     * @return the highlighted tile.
     */
    public ImageIcon getColoredHighlight(Player player) {
        BufferedImage tileImage = loadImage(GameSettings.NULL_TILE_PATH);
        return colorMaskBased(tileImage, highlightImage, player.getColor());
    }

    /**
     * Adds the emblem image to the top right of any tile image.
     * 
     * @param originalTile is the original tile image without the emblem.
     * @return a copy of the image with an emblem.
     */
    public ImageIcon addEmblem(BufferedImage originalTile) {
        BufferedImage copy = deepCopy(originalTile);
        for (int x = 0; x < emblemImage.getWidth(); x++) {
            for (int y = 0; y < emblemImage.getHeight(); y++) {
                Color emblemPixel = new Color(emblemImage.getRGB(x, y), true);
                Color imagePixel = new Color(copy.getRGB(x, y), true);
                Color blendedColor = blend(imagePixel, emblemPixel, false);
                copy.setRGB(x, y, blendedColor.getRGB());
            }
        }
        return new ImageIcon(copy);
    }

    // prepares the base images and templates
    private Map<TerrainType, BufferedImage> buildImageMap(boolean isTemplate) {
        Map<TerrainType, BufferedImage> map = new HashMap<>();
        for (TerrainType terrainType : TerrainType.basicTerrain()) {
            BufferedImage meepleImage = loadImage(GameSettings.getMeeplePath(terrainType, isTemplate));
            map.put(terrainType, meepleImage);
        }
        return map;
    }

    // Colors a meeple with RGB color.
    private ImageIcon paintMeeple(TerrainType meepleType, int color) {
        BufferedImage image = deepCopy(imageMap.get(meepleType));
        BufferedImage template = templateMap.get(meepleType);
        for (int x = 0; x < template.getWidth(); x++) {
            for (int y = 0; y < template.getHeight(); y++) {
                if (template.getRGB(x, y) == Color.BLACK.getRGB()) {
                    image.setRGB(x, y, color);
                }
            }
        }
        return new ImageIcon(image);
    }

    private ImageIcon colorMaskBased(BufferedImage imageToColor, BufferedImage maskImage, Color targetColor) {
        BufferedImage image = deepCopy(imageToColor);
        for (int x = 0; x < maskImage.getWidth(); x++) {
            for (int y = 0; y < maskImage.getHeight(); y++) {
                Color maskPixel = new Color(maskImage.getRGB(x, y), true);
                Color targetPixel = new Color(targetColor.getRed(), targetColor.getGreen(), targetColor.getBlue(),
                        maskPixel.getAlpha());
                Color imagePixel = new Color(image.getRGB(x, y), true);
                Color blendedColor = blend(imagePixel, targetPixel, true);
                image.setRGB(x, y, blendedColor.getRGB());
            }
        }
        return new ImageIcon(image);
    }

    /**
     * Blends to colors correctly based on alpha composition. Either blends both
     * colors or applies the second on the first one.
     * 
     * @param first        is the first color to be applied.
     * @param second       is the second color to be applied.
     * @param blendEqually applies the second on the first one of true, blends on
     *                     alpha values if false.
     * @return the blended color.
     */
    private Color blend(Color first, Color second, boolean blendEqually) {
        double totalAlpha = blendEqually ? first.getAlpha() + second.getAlpha() : MAXIMAL_ALPHA;
        double firstWeight = blendEqually ? first.getAlpha() : MAXIMAL_ALPHA - second.getAlpha();
        firstWeight /= totalAlpha;
        double secondWeight = second.getAlpha() / totalAlpha;
        double red = firstWeight * first.getRed() + secondWeight * second.getRed();
        double green = firstWeight * first.getGreen() + secondWeight * second.getGreen();
        double blue = firstWeight * first.getBlue() + secondWeight * second.getBlue();
        int alpha = Math.max(first.getAlpha(), second.getAlpha());
        return new Color((int) red, (int) green, (int) blue, alpha);
    }

    // copies a image to avoid side effects.
    private BufferedImage deepCopy(BufferedImage image) {
        ColorModel model = image.getColorModel();
        boolean isAlphaPremultiplied = model.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(model, raster, isAlphaPremultiplied, null);
    }

    private BufferedImage loadImage(URL path) {
        try {
            return ImageIO.read(path);
        } catch (IOException exception) {
            exception.printStackTrace();
            GameMessage.showError("ERROR: Could not load image loacted at " + path);
            return null;
        }
    }
}