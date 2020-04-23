package carcassonne.client;

import java.io.Serializable;

import carcassonne.server.ConnectionType;

public class ConnectionTypeRequest implements Serializable {

    private static final long serialVersionUID = -5934233103349783842L;
    final ConnectionType type;

    public ConnectionTypeRequest(final ConnectionType type) {
        this.type = type;
    }

    public ConnectionType getType() {
        return type;
    }
}
