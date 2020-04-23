package carcassonne.server;

import java.io.Serializable;

public class Response implements Serializable {

    private static final long serialVersionUID = -2578642297265177863L;
    private final ResponseCode code;
    private final ResponseType type;

    public Response(final ResponseCode code) {
        this(code, ResponseType.GENERAL);
    }

    public Response(final ResponseCode code, final ResponseType type) {
        this.code = code;
        this.type = type;
    }

    public ResponseCode getCode() {
        return code;
    }

    public ResponseType getType() {
        return type;
    }

}
