package carcassonne.client;

import carcassonne.model.Player;

public class PlacingSkippedRequest extends Request {

    private static final long serialVersionUID = -8934127222325998243L;

    public PlacingSkippedRequest(final Player player) {
        super(RequestType.PLACING_SKIPPED, player.getNumber());
    }

}
