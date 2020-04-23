package carcassonne.client;

public class GameStartRequest extends Request {

    private static final long serialVersionUID = 6757081458254747481L;

    public GameStartRequest() {
        super(RequestType.GAME_START, null);
    }

}
