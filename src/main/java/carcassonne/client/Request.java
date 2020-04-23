package carcassonne.client;

import java.io.Serializable;

public abstract class Request implements Serializable {

    private static final long serialVersionUID = 1074864367284122957L;
    private final RequestType type;
    private final Integer player;

    public Request(final RequestType type, final Integer player) {
        this.type = type;
        this.player = player;
    }

    public Integer getPlayer() {
        return player;
    }

    public RequestType getType() {
        return type;
    }
}
