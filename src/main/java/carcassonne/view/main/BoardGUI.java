package carcassonne.view.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import carcassonne.control.MainController;
import carcassonne.model.Meeple;
import carcassonne.model.Player;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.grid.GridSpot;
import carcassonne.model.grid.Pair;
import carcassonne.model.tile.Tile;
import carcassonne.settings.GameConstants;
import carcassonne.settings.GameSettings;
import carcassonne.settings.Notifiable;
import carcassonne.view.PaintShop;

public class BoardGUI extends JLayeredPane implements Notifiable {

    private static final long serialVersionUID = 5684446992452298030L; // generated UID
    private static final int MEEPLE_FACTOR = 3; // Meeples per tile length.
    private static final Color GUI_COLOR = new Color(190, 190, 190);

    private TileLabel[][] labelGrid;
    private List<TileLabel> tileLabels;
    private List<MeepleLabel> meepleLabels;
    private final PaintShop paintShop;
    private final MainController controller;
    private int tileSize;
    private final int gridHeight;
    private final int gridWidth;
    private Player currentPlayer;

    public BoardGUI(final MainController controller) {
        this(controller, GameSettings.TILE_SIZE, GameSettings.BOARD_WIDTH, GameSettings.BOARD_HEIGHT);
    }

    public BoardGUI(final MainController controller, final int tileSize, final int gridWidth, final int gridHeight) {
        this.controller = controller;
        paintShop = new PaintShop();
        this.tileSize = tileSize;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.meepleLabels = new ArrayList<>();
        setSize(new Dimension(gridWidth * tileSize, gridHeight * tileSize));
        JPanel tilePanel = buildTilePanel();
        buildLayeredPane(tilePanel);
    }

    private void buildLayeredPane(JPanel tilePanel) {
        setLayout(new BorderLayout()); // TODO Summerdave: No longer required, refactor BoardGUI?
        add(tilePanel, BorderLayout.CENTER);
    }

    private Pair<JPanel, MeepleLabel[][]> buildMeeplePanel() {
        JPanel meeplePanel = new JPanel();
        if (GameConstants.DEBUG_MODE) {
            meeplePanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
        }
        meeplePanel.setPreferredSize(new Dimension(tileSize, tileSize));
        meeplePanel.setOpaque(false);
        meeplePanel.setLayout(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        final int meepleGridWidth = MEEPLE_FACTOR;
        final int meepleGridHeight = MEEPLE_FACTOR;
        final int meepleWidth = tileSize / MEEPLE_FACTOR;
        final int meepleHeight = tileSize / MEEPLE_FACTOR;
        constraints.weightx = 1; // evenly distributes meeple grid
        constraints.weighty = 1;
        final MeepleLabel[][] meepleGrid = new MeepleLabel[meepleGridWidth][meepleGridHeight];
        for (int y = 0; y < meepleGridHeight; y++) {
            for (int x = 0; x < meepleGridWidth; x++) {
                meepleGrid[x][y] = new MeepleLabel(paintShop, controller, GridDirection.values2D()[x][y], this,
                        meepleWidth, meepleHeight);
                meepleLabels.add(meepleGrid[x][y]);
                constraints.gridx = x;
                constraints.gridy = y;
                meeplePanel.add(meepleGrid[x][y].getLabel(), constraints); // add label with constraints
            }
        }
        return Pair.of(meeplePanel, meepleGrid);
    }

    /*
     * Creates the grid of labels.
     */
    private JPanel buildTilePanel() {
        JPanel tilePanel = new JPanel();
        if (GameConstants.DEBUG_MODE) {
            tilePanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3));
        }
        tilePanel.setSize(gridWidth * tileSize, gridHeight * tileSize);
        tilePanel.setBackground(GUI_COLOR);
        tilePanel.setLayout(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        tileLabels = new ArrayList<>();
        labelGrid = new TileLabel[gridWidth][gridHeight]; // build array of labels.
        constraints.weightx = 1;
        constraints.weighty = 1;
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                final Pair<JPanel, MeepleLabel[][]> meeplePanel = buildMeeplePanel();
                labelGrid[x][y] = new TileLabel(controller, meeplePanel.getLeft(), meeplePanel.getRight(), x, y,
                        tileSize, tileSize);
                tileLabels.add(labelGrid[x][y]);
                constraints.gridx = x;
                constraints.gridy = y;
                tilePanel.add(labelGrid[x][y].getLayeredPane(), constraints); // add label with constraints
            }
        }
        return tilePanel;
    }

    /**
     * Refreshes the meeple labels to get the new colors.
     */
    @Override
    public void notifyChange() {
        meepleLabels.forEach(it -> it.refresh());
        repaint();
        if (currentPlayer != null) {
            setCurrentPlayer(currentPlayer);
        }
    }

    /**
     * Rebuilds the label grid and the meeple grid if the game should be restarted.
     */
    public void rebuildGrids() {
        meepleLabels.forEach(it -> it.reset());
        tileLabels.forEach(it -> it.reset());
        repaint();
    }

    /**
     * Removes meeple on a tile on the grid.
     * 
     * @param meeple is the meeple that should be removed.
     */
    public void removeMeeple(Meeple meeple) {
        checkParameters(meeple);
        GridSpot spot = meeple.getLocation().getGridSpot();
        if (spot == null) { // make sure meeple is placed
            throw new IllegalArgumentException("Meeple has to be placed to be removed from GUI: " + meeple);
        }
        getTile(spot.getX(), spot.getY()).removeMeeple(); // call reset() on meeple
        repaint();
    }

    /**
     * Draws the tile on a specific position on the GUI.
     * 
     * @param tile is the tile.
     * @param x    is the x coordinate.
     * @param y    is the y coordinate.
     */
    public void setTile(Tile tile, int x, int y) {
        checkParameters(tile);
        checkCoordinates(x, y);
        labelGrid[x][y].setTile(tile);
    }

    /**
     * Highlights a position on the grid to indicate that the tile is a possible
     * placement spot.
     * 
     * @param x is the x coordinate.
     * @param y is the y coordinate.
     */
    public void setHighlight(int x, int y) {
        checkCoordinates(x, y);
        labelGrid[x][y].highlight();
    }

    /**
     * Draws meeple on a tile on the grid.
     * 
     * @param tile     is the tile where the meeple gets drawn.
     * @param position is the position on the tile where the meeple gets drawn.
     * @param owner    is the player that owns the meeple.
     */
    public void setMeeple(Tile tile, GridDirection position, Player owner) {
        checkParameters(tile, position, owner);
        GridSpot spot = tile.getGridSpot();
        getTile(spot.getX(), spot.getY()).setMeeple(position, owner); // setIcon() called on Meeple.
        repaint(); // This is required! Removing this will paint black background.
    }

    /**
     * Resets the meeple preview on one specific {@link Tile}.
     * 
     * @param tile is the specific {@link Tile}.
     */
    public void resetMeeplePreview(Tile tile) {
        checkParameters(tile);
        final GridSpot spot = tile.getGridSpot();
        getTile(spot.getX(), spot.getY()).resetMeeplePreview(); // call reset()
        repaint(); // This is required! Removing this will paint black background.
    }

    /**
     * Enables the meeple preview on one specific {@link Tile}.
     * 
     * @param tile          is the specific {@link Tile}.
     * @param currentPlayer determines the color of the preview.
     */
    public void setMeeplePreview(Tile tile, Player currentPlayer) {
        checkParameters(tile, currentPlayer);
        GridDirection[][] directions = GridDirection.values2D();
        final GridSpot spot = tile.getGridSpot();
        final TileLabel tileLabel = getTile(spot.getX(), spot.getY());
        for (int y = 0; y < MEEPLE_FACTOR; y++) {
            for (int x = 0; x < MEEPLE_FACTOR; x++) {
                if (tile.hasMeepleSpot(directions[x][y]) && controller.requestPlacementStatus(directions[x][y])) {
                    tileLabel.setMeeplePreview(directions[x][y], currentPlayer); // call setPreview();
                }
            }
        }
        repaint(); // This is required! Removing this will paint black background.
    }

    /**
     * Notifies the the main GUI about a (new) current player. This allows the UI to
     * adapt color schemes to the player.
     * 
     * @param currentPlayer is the current {@link Player}.
     */
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        ImageIcon newHighlight = paintShop.getColoredHighlight(currentPlayer);
        tileLabels.forEach(it -> it.setColoredHighlight(newHighlight));
    }

    public TileLabel getTile(final int x, final int y) {
        // Tiles are stored column-major
        return tileLabels.get(y + x * gridHeight);
    }

    /**
     * Enslaves sub user interfaces, they are minimized with this GUI.
     * 
     * @param userInterfaces are the user interfaces to enslave.
     */
//    public void addSubInterfaces(Component... userInterfaces) {
//        this.addWindowListener(new SubComponentAdapter(userInterfaces));
//    }

    private void checkParameters(Object... parameters) {
        for (Object parameter : parameters) {
            if (parameter == null) {
                throw new IllegalArgumentException("Parameters such as Tile, Meeple, and Player cannot be null!");
            }
        }
    }

    private void checkCoordinates(int x, int y) {
        if (x < 0 && x >= gridWidth || y < 0 && y >= gridHeight) {
            throw new IllegalArgumentException("Invalid label grid position (" + x + ", " + y + ")");
        }
    }

    public int getTileSize() {
        return tileSize;
    }
}
