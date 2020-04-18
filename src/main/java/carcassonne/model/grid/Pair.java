package carcassonne.model.grid;

public class Pair<L, R> {
    private final L left;
    private final R right;

    protected Pair(final L left, final R right) {
        this.left = left;
        this.right = right;
    }
    
    public static <L, R>Pair<L, R> of(L left, R right) {
        return new Pair<L, R>(left, right);
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }
    
}
