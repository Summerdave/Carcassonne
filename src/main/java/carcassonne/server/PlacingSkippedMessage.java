package carcassonne.server;

public class PlacingSkippedMessage extends BroadcastMessage {

    private static final long serialVersionUID = 7933685006168848364L;

    public PlacingSkippedMessage(int originator) {
        super(originator, BroadcastMessageType.PLACING_SKIPPED);
    }

}
