package carcassonne.client;

import carcassonne.model.Player;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.grid.GridSpot;

public class MeeplePlacedRequest extends Request {

    private static final long serialVersionUID = 768340849076455984L;
    private final GridDirection direction;
    private final GridSpot spot;

    public MeeplePlacedRequest(Player player, final GridDirection direction, final GridSpot spot) {
        super(RequestType.PLACE_MEEPLE, player.getNumber());
        this.direction = direction;
        this.spot = spot;
    }

    public GridDirection getDirection() {
        return direction;
    }

    public GridSpot getSpot() {
        return spot;
    }

}
