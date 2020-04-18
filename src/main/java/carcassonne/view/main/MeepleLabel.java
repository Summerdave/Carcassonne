package carcassonne.view.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import carcassonne.control.MainController;
import carcassonne.model.Player;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.terrain.TerrainType;
import carcassonne.settings.GameConstants;
import carcassonne.settings.GameSettings;
import carcassonne.view.PaintShop;

/**
 * Special {@link JLabel} for showing meeples.
 * 
 * @author Timur Saglam
 */
public class MeepleLabel {
    private final ImageIcon imageEmpty;
    private final PaintShop paintShop;
    private Player player;
    private final MouseAdapter mouseAdapter;
    private TerrainType terrain;
    private final JLabel label;
    private boolean preview;
    private int width;
    private int height;

    /**
     * Creates a blank meeple label.
     * 
     * @param paintShop  is the paint shop for the meeple generation.
     * @param controller is the {@link MainController} of the game.
     * @param direction  is the {@link GridDirection} where the meeple label sits on
     *                   the tile.
     * @param frame      is the main {@link JFrame} to repaint after setting icons.
     */
    public MeepleLabel(final PaintShop paintShop, final MainController controller, final GridDirection direction,
            final Component frame, final int width, final int height) {
        label = new JLabel();
        this.width = width;
        this.height = height;
        imageEmpty = new ImageIcon(GameSettings.getMeeplePath(TerrainType.OTHER, false));
        preview = false;
        reset();
        this.paintShop = paintShop;
        mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    controller.requestMeeplePlacement(direction);
                }
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                setMeepleIcon();
                frame.repaint();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                setPreviewIcon();
                frame.repaint();
            }
        };
        if (GameConstants.DEBUG_MODE) {
            label.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
        }
    }

    /**
     * Refreshes its icon by getting the newest image from the {@link PaintShop}.
     */
    public void refresh() {
        if (terrain != TerrainType.OTHER && !preview) {
            setMeepleIcon();
        }
    }

    /**
     * Resets the label, which means it displays nothing.
     */
    public void reset() {
        terrain = TerrainType.OTHER;
        setIcon(imageEmpty);
        label.removeMouseListener(mouseAdapter);
    }

    /**
     * Sets the icon of the meeple label according to the {@link Player} and terrain
     * type.
     * 
     * @param terrain is the terrain type and affects the meeple type.
     * @param player  is the {@link Player}, which affects the color.
     */
    public void setIcon(TerrainType terrain, Player player) {
        this.terrain = terrain;
        this.player = player;
        preview = false;
        refresh();
    }

    /**
     * Sets the specific {@link TerrainType} as meeple placement preview, which
     * means a transparent image of the correlating meeple.
     * 
     * @param terrain is the specific {@link TerrainType}.
     * @param player  is the {@link Player} who is currently active.
     */
    public void setPreview(TerrainType terrain, Player player) {
        this.terrain = terrain;
        this.player = player;
        preview = true;
        label.addMouseListener(mouseAdapter);
        setPreviewIcon();
    }

    /**
     * Grants access to the {@link JLabel} itself.
     * 
     * @return the {@link JLabel}
     */
    public JLabel getLabel() {
        return label;
    }

    private void setMeepleIcon() {
        setIcon(paintShop.getColoredMeeple(terrain, player));
    }

    private void setPreviewIcon() {
        setIcon(new ImageIcon(GameSettings.getMeeplePath(terrain, false)));
    }

    private void setIcon(final ImageIcon icon) {
        if (icon != null) {
            final ImageIcon rescaled = new ImageIcon(
                    icon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
            label.setIcon(rescaled);
        }
    }
}
