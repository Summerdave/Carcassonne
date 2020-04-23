package carcassonne.view.main;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.OverlayLayout;

import carcassonne.control.MainController;
import carcassonne.model.Player;
import carcassonne.model.grid.CoordinatePair;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.tile.Tile;
import carcassonne.model.tile.TileType;
import carcassonne.settings.GameConstants;

/**
 * Is a simple class derived form JLabel, which stores (additionally to the
 * JLabel functions) the coordinates of the label on the label grid.
 * 
 * @author Timur Saglam
 */
public class TileLabel {
    private Tile tile;
    private final Tile defaultTile;
    private final Tile highlightTile;
    private final JLabel label;
    private final JLayeredPane layeredPane;
    private ImageIcon coloredHighlight;
    private int width;
    private int height;
    private MeepleLabel[][] meepleLabels;

    /**
     * Simple constructor calling the <codeJLabel>JLabel(ImageIcon image)</code>
     * constructor.
     * 
     * @param controller   is the controller of the GUI.
     * @param meepleLabels
     * @param x            sets the x coordinate.
     * @param y            sets the y coordinate.
     */
    public TileLabel(final MainController controller, final JComponent meepleOverlay, MeepleLabel[][] meepleLabels,
            int x, int y, int width, int height) {
        label = new JLabel();
        this.width = width;
        this.height = height;
        this.meepleLabels = meepleLabels;
        defaultTile = new Tile(TileType.Null);
        highlightTile = new Tile(TileType.Null);
        defaultTile.rotateRight();
        reset();
        if (GameConstants.DEBUG_MODE) {
            label.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
        }

        this.layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        layeredPane.setSize(width, height);
        meepleOverlay.setAlignmentX(0.5f);
        meepleOverlay.setAlignmentY(0.5f);
        layeredPane.add(meepleOverlay, Integer.valueOf(0), 0);
        label.setAlignmentX(0.5f);
        label.setAlignmentY(0.5f);
        layeredPane.add(label, Integer.valueOf(0), 1);
        if (GameConstants.DEBUG_MODE) {
            layeredPane.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
        }

        label.addMouseListener(new MouseAdapter() {
            /**
             * Method for processing mouse clicks on the <code>TileLabel</code> of the
             * class. notifies the <code>MainController</code> of the class.
             * 
             * @param e is the <code>MouseEvent</code> of the click.
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.requestTilePlacement(CoordinatePair.of(x, y));
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                if (highlightTile.equals(tile)) {
                    setIcon(coloredHighlight);
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                if (highlightTile.equals(tile)) {
                    setTile(highlightTile);
                }
            }
        });
    }

    /**
     * Shows a {@link Tile} image on this label.
     * 
     * @param tile is the {@link Tile} that provides the image.
     */
    public void setTile(Tile tile) {
        this.tile = tile;
        setIcon(tile.getIcon());
    }

    private void setIcon(final ImageIcon icon) {
        if (icon != null) {
            final ImageIcon rescaled = new ImageIcon(
                    icon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
            label.setIcon(rescaled);
        }
    }

    /**
     * Sets a colored mouseover highlight.
     * 
     * @param coloredHighlight is the {@link ImageIcon} depicting the highlight.
     */
    public void setColoredHighlight(ImageIcon coloredHighlight) {
        this.coloredHighlight = coloredHighlight;
    }

    /**
     * Enables the colored mouseover highlight.
     */
    public void highlight() {
        setTile(highlightTile);
    }

    /**
     * Disables the colored mouseover highlight.
     */
    public void reset() {
        setTile(defaultTile);
    }

    /**
     * Grants access to the {@link JLabel} of this label.
     * 
     * @return the tile {@link JLabel}.
     */
    public JLayeredPane getLayeredPane() {
        return layeredPane;
    }

    public void removeMeeple() {
        for (int x = 0; x < GameConstants.MEEPLE_FACTOR; x++) {
            for (int y = 0; y < GameConstants.MEEPLE_FACTOR; y++) {
                meepleLabels[x][y].reset();
            }
        }
    }

    public void resetMeeplePreview() {
        removeMeeple();
    }

    public void setMeeple(GridDirection position, Player owner) {
        final int x = GridDirection.xValue(position);
        final int y = GridDirection.yValue(position);
        meepleLabels[x][y].setIcon(tile.getTerrain(position), owner);
    }

    public void setMeeplePreview(GridDirection position, Player owner) {
        final int x = GridDirection.xValue(position);
        final int y = GridDirection.yValue(position);
        meepleLabels[x][y].setPreview(tile.getTerrain(position), owner);
    }
}
