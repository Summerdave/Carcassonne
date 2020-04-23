package carcassonne.server;

import carcassonne.model.grid.GridDirection;
import carcassonne.model.grid.GridSpot;

public class MeeplePlacedMessage extends BroadcastMessage {

    private static final long serialVersionUID = -8116045716835468326L;
    private final GridSpot spot;
    private final GridDirection direction;

    public MeeplePlacedMessage(Integer player, GridSpot spot, GridDirection direction) {
        super(player, BroadcastMessageType.MEEPLE_PLACED);
        this.spot = spot;
        this.direction = direction;
    }

    public GridSpot getSpot() {
        return spot;
    }

    public GridDirection getDirection() {
        return direction;
    }

}
