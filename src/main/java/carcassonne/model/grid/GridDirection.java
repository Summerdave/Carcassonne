package carcassonne.model.grid;

import carcassonne.model.terrain.RotationDirection;

/**
 * Enumeration for grid directions and tile positions. It is used either to
 * specify a direction on the grid from a specific tile, or to specify a
 * position on a tile.
 * 
 * @author Timur Saglam
 */
public enum GridDirection {
    TOP, RIGHT, BOTTOM, LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, TOP_LEFT, MIDDLE;

    /**
     * Adds a x coordinate and a <code> GridDirection</code>.
     * 
     * @param coordinate is the x coordinate.
     * @return the sum as an x coordinate.
     */
    public int addX(int coordinate) {
        int result = coordinate;
        if (this == TOP_RIGHT || this == RIGHT || this == BOTTOM_RIGHT) {
            result++;
        } else if (this == TOP_LEFT || this == LEFT || this == BOTTOM_LEFT) {
            result--;
        }
        return result;
    }

    /**
     * Adds a y coordinate and a <code> GridDirection</code>.
     * 
     * @param coordinate is the y coordinate.
     * @return the sum as an y coordinate.
     */
    public int addY(int coordinate) {
        int result = coordinate;
        if (this == BOTTOM_LEFT || this == BOTTOM || this == BOTTOM_RIGHT) {
            result++;
        } else if (this == TOP_LEFT || this == TOP || this == TOP_RIGHT) {
            result--;
        }
        return result;
    }

    /**
     * Checks whether the this grid direction is directly to the left of another
     * grid direction.
     * 
     * @param other is the other grid direction.
     * @return true if it is.
     */
    public boolean isLeftOf(GridDirection other) {
        return nextDirectionTo(RotationDirection.RIGHT) == other;
    }

    /**
     * Checks whether the this grid direction is directly to the right of another
     * grid direction.
     * 
     * @param other is the other grid direction.
     * @return true if it is.
     */
    public boolean isRightOf(GridDirection other) {
        return nextDirectionTo(RotationDirection.LEFT) == other;
    }

    /**
     * Checks whether the ordinal of a direction is smaller or equal than the
     * ordinal of another direction.
     * 
     * @param other is the other direction.
     * @return true if smaller or equal.
     */
    public boolean isSmallerOrEquals(GridDirection other) {
        return ordinal() <= other.ordinal();
    }

    /**
     * Gets the next direction on the specified side of the current direction.
     * 
     * @param side sets the side.
     * @return the next direction
     */
    public GridDirection nextDirectionTo(RotationDirection side) {
        if (this == MIDDLE) {
            return this;
        }
        GridDirection[] cycle = { TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, TOP_LEFT };
        int position = -2; // error case, sum with parameter side is negative
        for (int i = 0; i < cycle.length; i++) {
            if (cycle[i] == this) { // find in cycle
                position = i; // save cycle position
            }
        }
        return cycle[(cycle.length + position + side.toInt()) % cycle.length];
    }

    /**
     * Calculates the opposite <code>GridDirection</code> for a specific
     * <code>GridDirection</code>.
     * 
     * @return the opposite <code>GridDirection</code>.
     */
    public GridDirection opposite() {
        if (ordinal() <= 3) { // for TOP, RIGHT, BOTTOM and LEFT:
            return values()[smallOpposite(ordinal())];
        } else if (ordinal() <= 7) { // for TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT and TOP_LEFT:
            return values()[bigOpposite(ordinal())];
        }
        return MIDDLE; // middle is the opposite of itself.
    }

    /**
     * Returns a lower case version of the grid direction with spaces instead of
     * underscores.
     * 
     * @return the readable version.
     */
    public String toReadableString() {
        return toString().toLowerCase().replace('_', ' ');
    }

    private int bigOpposite(int ordinal) {
        return 4 + smallOpposite(ordinal - 4);
    }

    private int smallOpposite(int ordinal) {
        return (ordinal + 2) % 4;
    }

    /**
     * Generates an array of the GridDirections for a direct neighbor on the grid.
     * 
     * @return an array of TOP, RIGHT, BOTTOM and LEFT.
     */
    public static GridDirection[] directNeighbors() {
        return new GridDirection[] { TOP, RIGHT, BOTTOM, LEFT };
    }

    /**
     * Generates an array of the GridDirections for a indirect neighbor on the grid.
     * 
     * @return an array of TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT and TOP_LEFT.
     */
    public static GridDirection[] indirectNeighbors() {
        return new GridDirection[] { TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, TOP_LEFT };
    }

    /**
     * Generates an array of the GridDirections for a neighbor on the grid.
     * 
     * @return an array of all directions except MIDDLE.
     */
    public static GridDirection[] neighbors() {
        return new GridDirection[] { TOP, RIGHT, BOTTOM, LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, TOP_LEFT };
    }

    /**
     * Generates an array of the GridDirections for all positions on a tile.
     * 
     * @return an array of an array of TOP, RIGHT, BOTTOM, LEFT and MIDDLE.
     */
    public static GridDirection[] tilePositions() {
        return new GridDirection[] { TOP, RIGHT, BOTTOM, LEFT, MIDDLE };
    }

    /**
     * Generates a two dimensional array of the GridDirections for their orientation
     * on a tile.
     * 
     * @return a 2D array of an array of TOP_LEFT, LEFT, BOTTOM_LEFT, TOP, MIDDLE,
     *         BOTTOM, TOP_RIGHT, RIGHT and BOTTOM_RIGHT.
     */
    public static GridDirection[][] values2D() {
        return new GridDirection[][] { { TOP_LEFT, LEFT, BOTTOM_LEFT }, { TOP, MIDDLE, BOTTOM },
                { TOP_RIGHT, RIGHT, BOTTOM_RIGHT } };
    }

    public static int xValue(GridDirection direction) {
        switch (direction) {
        case TOP_LEFT:
        case LEFT:
        case BOTTOM_LEFT:
            return 0;
        case TOP:
        case MIDDLE:
        case BOTTOM:
            return 1;
        case TOP_RIGHT:
        case RIGHT:
        case BOTTOM_RIGHT:
            return 2;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static int yValue(GridDirection direction) {
        switch (direction) {
        case TOP_LEFT:
        case TOP:
        case TOP_RIGHT:
            return 0;
        case LEFT:
        case MIDDLE:
        case RIGHT:
            return 1;
        case BOTTOM_LEFT:
        case BOTTOM:
        case BOTTOM_RIGHT:
            return 2;
        default:
            throw new IllegalArgumentException();
        }
    }
}
