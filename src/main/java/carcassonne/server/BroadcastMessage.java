package carcassonne.server;

import java.io.Serializable;

public class BroadcastMessage implements Serializable {

    private static final long serialVersionUID = 3629915333818409116L;
    private final int originator;
    private final BroadcastMessageType type;

    public BroadcastMessage(final int originator, final BroadcastMessageType type) {
        this.originator = originator;
        this.type = type;
    }

    public int getOriginator() {
        return originator;
    }

    public BroadcastMessageType getType() {
        return type;
    }

}
