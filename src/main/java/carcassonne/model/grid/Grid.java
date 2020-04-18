package carcassonne.model.grid;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import carcassonne.model.tile.Tile;
import carcassonne.model.tile.TileType;

/**
 * The playing grid class.
 * 
 * @author Timur Saglam
 */
public class Grid {
    private static final TileType FOUNDATION_TYPE = TileType.CastleWallRoad;
    private final int width;
    private final int height;
    private GridSpot[][] spots;
    private GridSpot foundation;

    /**
     * Basic constructor
     * 
     * @param width  is the grid width.
     * @param height is the grid height.
     */
    public Grid(final int width, final int height) {
        this.width = width;
        this.height = height;
        spots = new GridSpot[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                spots[x][y] = new GridSpot(this, x, y);
            }
        }
        placeFoundation(FOUNDATION_TYPE);
    }

    public Grid copy(final int newWidth, final int newHeight) {
        final int diffX = newWidth - width;
        final int diffY = newHeight - height;
        if (diffX < 0 || diffY < 0) {
            throw new IllegalArgumentException("Invalid size: Can only copy into bigger grid");
        }
        if (diffX % 2 != 0 || diffY % 2 != 0) {
            throw new IllegalArgumentException("Grid must grow evenly");
        }
        final int offsetX = diffX / 2;
        final int offsetY = diffY / 2;
        final GridSpot[][] newSpots = new GridSpot[newWidth][newHeight];
        final Grid newGrid = new Grid(newWidth, newHeight, null);
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                final int xOld = x - offsetX;
                final int yOld = y - offsetY;
                if (xOld < 0 || xOld >= width || yOld < 0 || yOld >= height) {
                    newSpots[x][y] = new GridSpot(this, x, y);
                } else {
                    assert (spots[x - offsetX][y - offsetY] != null);
                    final GridSpot oldSpot = spots[x - offsetX][y - offsetY];
                    final GridSpot newSpot = new GridSpot(newGrid, x, y);
                    if (oldSpot.getTile() != null) {
                        newSpot.forcePlacement(oldSpot.getTile());
                    }
                    newSpots[x][y] = newSpot;
                }
            }
        }
        newGrid.spots = newSpots;
        return newGrid;
    }

    private Grid(int width, int height, final GridSpot[][] spots) {
        this.width = width;
        this.height = height;
        this.spots = spots;
    }

    /**
     * Returns list of all patterns on the grid.
     * 
     * @return the list of patterns.
     */
    public List<GridPattern> getAllPatterns() {
        List<GridPattern> patterns = new LinkedList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (spots[x][y].isOccupied()) {
                    patterns.addAll(spots[x][y].createPatternList());
                }
            }
        }
        for (GridPattern pattern : patterns) {
            pattern.removeTileTags(); // IMPORTANT
        }
        return patterns; // get patterns.
    }

    /**
     * Creates a list of spots that are connected to a specific spot with the
     * terrain in a specific direction on the spot.
     * 
     * @param spot is the spot on the grid where the tile is.
     * @param from is the direction the tile is connected from
     * @return the list of connected tiles.
     */
    public List<GridSpot> getConnectedTiles(GridSpot spot, GridDirection from) {
        checkParameters(spot);
        List<GridSpot> list = new LinkedList<>();
        for (GridDirection to : GridDirection.directNeighbors()) {
            if (spot.getTile().hasConnection(from, to) && from != to) { // if connected
                list.addAll(getNeighbors(spot, false, to));
            }
        }
        return list;
    }

    /**
     * Returns the spot of the first tile of round, the foundation tile.
     * 
     * @return the grid spot.
     */
    public GridSpot getFoundation() {
        return foundation;
    }

    /**
     * Getter for the grid height.
     * 
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Method checks for modified patterns on the grid. As a basis it uses the
     * coordinates of the last placed tile.
     * 
     * @param spot is the spot of the last placed tile.
     * @return the list of the modified patterns.
     */
    public List<GridPattern> getModifiedPatterns(GridSpot spot) {
        checkParameters(spot);
        if (spot.isFree()) {
            throw new IllegalArgumentException("Can't check for patterns on an free grid space");
        }
        List<GridPattern> modifiedPatterns = spot.createPatternList();
        for (GridPattern pattern : modifiedPatterns) {
            pattern.removeTileTags(); // VERY IMPORTANT!
        }
        return modifiedPatterns; // get patterns.
    }

    /**
     * Returns the neighbor of a specific {@link GridSpot} in a specific direction
     * or null of there is none.
     * 
     * @param spot      is the {@link GridSpot} from which the neighbor is
     *                  requested.
     * @param direction is the {@link GridDirection} where the neighbor is.
     * @return the neighboring {@link GridSpot} or null if there is no tile placed.
     */
    public GridSpot getNeighbor(GridSpot spot, GridDirection direction) {
        List<GridSpot> neighbors = getNeighbors(spot, false, direction);
        if (neighbors.isEmpty()) {
            return null; // return null if tile not placed or not on grid.
        } else {
            return neighbors.get(0);
        }
    }

    /**
     * Returns a list of neighbors of a specific {@link GridSpot} in specific
     * directions.
     * 
     * @param spot            is the specific {@link GridSpot}.
     * @param allowEmptySpots determines whether empty spots are included or not.
     * @param directions      determines the directions where we check for
     *                        neighbors. If no directions are given, the default
     *                        {@link GridDirection#neighbors()} is used.
     * @return the list of any neighboring {@link GridSpot}.
     */
    public List<GridSpot> getNeighbors(GridSpot spot, boolean allowEmptySpots, GridDirection... directions) {
        checkParameters(spot);
        ArrayList<GridSpot> neighbors = new ArrayList<>(directions.length);
        for (GridDirection direction : directions) {
            int newX = direction.addX(spot.getX());
            int newY = direction.addY(spot.getY());
            if (isOnGrid(newX, newY) && (allowEmptySpots || spots[newX][newY].isOccupied())) {
                neighbors.add(spots[newX][newY]); // return calculated neighbor if valid:
            }
        }
        return neighbors;
    }

    /**
     * Safe getter for tiles.
     * 
     * @param x is the x coordinate
     * @param y is the y coordinate
     * @return the spot
     * @throws IllegalArgumentException if the requested tile is out of grid.
     */
    public GridSpot getSpot(int x, int y) {
        checkParameters(x, y);
        return spots[x][y];
    }

    /**
     * Getter for the grid width.
     * 
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Checks whether a spot on the grid would close free spots off in a direction
     * if a tile would be placed there.
     * 
     * @param spot      is the spot.
     * @param direction is the direction.
     * @return true if it does.
     */
    public boolean isClosingFreeSpotsOff(GridSpot spot, GridDirection direction) {
        boolean[][] visitedPositions = new boolean[width][height];
        visitedPositions[spot.getX()][spot.getY()] = true; // mark starting point as visited
        return !findBoundary(spot, direction, visitedPositions); // start recursion
    }

    /**
     * Checks whether the grid is full.
     * 
     * @return true if full.
     */
    public boolean isFull() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (spots[x][y].isFree()) {
                    return false; // grid is not full if one position is free
                }
            }
        }
        return true;
    }

    /**
     * Checks whether a specific spot on the grid is valid.
     * 
     * @param spot is the spot
     * @return true if it is on the grid.
     */
    public boolean isOnGrid(GridSpot spot) {
        return spot != null && spot.equals(spots[spot.getX()][spot.getY()]);
    }

    /**
     * Checks whether specific coordinates are on the grid.
     * 
     * @param x is the x coordinate
     * @param y is the y coordinate
     * @return true if it is on the grid.
     */
    public boolean isOnGrid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Tries to place a tile on a spot on the grid.
     * 
     * @param x    is the x coordinate
     * @param y    is the y coordinate
     * @param tile is the tile to place
     * @return true if it was successful, false if spot is occupied.
     */
    public boolean place(int x, int y, Tile tile) {
        checkParameters(x, y);
        checkParameters(tile);
        return spots[x][y].set(tile);
    }

    public boolean needsResize() {
        final Pair<CoordinatePair, CoordinatePair> effectiveSize = getEffectiveSize();
        final Pair<CoordinatePair, CoordinatePair> actualSize = getSize();
        return CoordinatePair.minDistance(actualSize.getLeft(), effectiveSize.getLeft()) < 2
                || CoordinatePair.minDistance(actualSize.getRight(), effectiveSize.getRight()) < 3;
    }

    public static CoordinatePair getOffsetForCentering(final Pair<CoordinatePair, CoordinatePair> container,
            final Pair<CoordinatePair, CoordinatePair> child) {
        final int diffXLeft = container.getLeft().getX() - child.getLeft().getX();
        final int diffYTop = container.getLeft().getY() - child.getLeft().getY();
        final int diffXRight = container.getRight().getX() - child.getRight().getX();
        final int diffYBot = container.getRight().getY() - child.getRight().getY();
        final int horizontalDiff = diffXLeft + diffXRight;
        final int verticalDiff = diffYTop + diffYBot;
        return CoordinatePair.of(horizontalDiff / 2, verticalDiff / 2);
    }

    public Pair<CoordinatePair, CoordinatePair> getSize() {
        return Pair.of(CoordinatePair.of(0, 0), CoordinatePair.of(width, height));
    }

    public Pair<CoordinatePair, CoordinatePair> getEffectiveSize() {
        int minY = Integer.MAX_VALUE;
        int maxY = 0;
        int minX = Integer.MAX_VALUE;
        int maxX = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (spots[x][y].isOccupied()) {
                    minX = Integer.min(minX, x);
                    maxX = Integer.max(maxX, x);
                    minY = Integer.min(minY, y);
                    maxY = Integer.max(maxY, y);
                }
            }
        }
        return Pair.of(CoordinatePair.of(minX, minY), CoordinatePair.of(maxX, maxY));
    }

    private void checkParameters(GridSpot spot) {
        if (spot == null) {
            throw new IllegalArgumentException("Spot can't be null!");
        } else if (!spots[spot.getX()][spot.getY()].equals(spot)) {
            throw new IllegalArgumentException("Spot is not on the grid!");
        }
    }

    /**
     * Error checker method for other methods in this class. It just checks whether
     * specific coordinates are on the grid and throws an error if not.
     * 
     * @param x is the x coordinate
     * @param y is the y coordinate
     */
    private void checkParameters(int x, int y) {
        if (!isOnGrid(x, y)) {
            throw new IllegalArgumentException("tile coordinates are out of grid: x=" + x + " & y=" + y);
        }
    }

    /**
     * Error checker method for other methods in this class. It just checks whether
     * specific tile is not null.
     * 
     * @param tile the tile to check
     */
    private void checkParameters(Tile tile) {
        if (tile == null) {
            throw new IllegalArgumentException("Tile can't be null.");
        } else if (tile.getType() == TileType.Null) {
            throw new IllegalArgumentException("Tile from type TileType.Null can't be placed.");
        }
    }

    // method tries to find a path of free grid spaces to the grid border.
    private boolean findBoundary(GridSpot spot, GridDirection direction, boolean[][] visitedPositions) {
        int newX = direction.addX(spot.getX()); // get coordinates
        int newY = direction.addY(spot.getY()); // of free space
        if (isOnGrid(newX, newY)) { // if on grid
            if (spots[newX][newY].isOccupied()) {
                return false; // is a tile, can't go through tiles
            } else if (!visitedPositions[newX][newY]) { // if not visited
                visitedPositions[newX][newY] = true; // mark as visited
                for (GridDirection newDirection : GridDirection.directNeighbors()) { // recursion
                    if (findBoundary(spots[newX][newY], newDirection, visitedPositions)) {
                        return true; // found boundary
                    }
                }
            }
        } else { // if not on grid
            return true; // found boundary
        }
        return false; // has not found boundary
    }

    /**
     * Places a specific tile in the middle of the grid.
     * 
     * @param tileType is the type of that specific tile.
     */
    private void placeFoundation(TileType tileType) {
        int centerX = Math.round((width - 1) / 2);
        int centerY = Math.round((height - 1) / 2);
        foundation = spots[centerX][centerY];
        foundation.forcePlacement(new Tile(tileType));
    }

    public void move(CoordinatePair move) {
        // TODO Summerdave: Use Effective Grid to determine if move is legal.
        final GridSpot[][] newSpots = new GridSpot[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int xOld = x - move.getX(); // Inverse to determine old position
                final int yOld = y - move.getY();
                final GridSpot newSpot;
                if (xOld < 0 || xOld >= width || yOld < 0 || yOld >= height) {
                    newSpot = new GridSpot(this, x, y);
                } else {
                    newSpot = new GridSpot(this, x, y);
                    final GridSpot oldSpot = spots[xOld][yOld];
                    if (oldSpot.getTile() != null) {
                        newSpot.forcePlacement(oldSpot.getTile());
                    }
                }
                newSpots[x][y] = newSpot;
            }
        }
        this.spots = newSpots;
    }

}