package carcassonne.model.grid;

public class CoordinatePair extends Pair<Integer, Integer>{
    private CoordinatePair(int x, int y) {
        super(Integer.valueOf(x), Integer.valueOf(y));
    }
    public static CoordinatePair of(int left, int right) {
        return new CoordinatePair(Integer.valueOf(left), Integer.valueOf(right));
    }
    public int getX() {
        return getLeft().intValue();
    }
    public int getY() {
        return getRight().intValue();
    }
    
    public boolean isZero() {
        return getX() == 0 && getY() == 0;
    }
    
    public static int manhattanDistance(CoordinatePair one, CoordinatePair two) {
        return Math.abs(one.getX()-two.getX()) + Math.abs(one.getY() - two.getY());
    }
    
    public static int maxDistance(CoordinatePair one, CoordinatePair two) {
        return Math.max(Math.abs(one.getX()-two.getX()), Math.abs(one.getY() - two.getY()));
    }
    
    public static int minDistance(CoordinatePair one, CoordinatePair two) {
        return Math.min(Math.abs(one.getX()-two.getX()), Math.abs(one.getY() - two.getY()));
    }
    
    public static CoordinatePair minus(final Pair<Integer, Integer> subtrahend, final Pair<Integer, Integer> minuend) {
        return CoordinatePair.of(subtrahend.getLeft() - minuend.getLeft(), subtrahend.getRight() - minuend.getRight());
    }

}
