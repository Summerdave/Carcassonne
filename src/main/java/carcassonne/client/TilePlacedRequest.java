package carcassonne.client;

import carcassonne.model.Player;
import carcassonne.model.grid.GridSpot;

public class TilePlacedRequest extends Request {

    private static final long serialVersionUID = 5844131356712041723L;
    private final GridSpot tilePlaced;

    public TilePlacedRequest(final GridSpot tilePlaced, final Player activePlayer) {
        super(RequestType.PLACE_TILE, activePlayer.getNumber());
        this.tilePlaced = tilePlaced;
    }

    public GridSpot getTilePlaced() {
        return tilePlaced;
    }

}
