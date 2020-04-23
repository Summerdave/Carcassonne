package carcassonne.server;

import carcassonne.model.grid.GridSpot;

public class TilePlacedMessage extends BroadcastMessage {

    private static final long serialVersionUID = 7730722365224704037L;
    private final GridSpot tile;

    public TilePlacedMessage(final int originator, final GridSpot tile) {
        super(originator, BroadcastMessageType.TILE_PLACED);
        this.tile = tile;
    }

    public GridSpot getTile() {
        return tile;
    }

}
