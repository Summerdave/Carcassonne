package carcassonne.server;

import carcassonne.model.Player;

public class GameStartResponse extends Response {

    private static final long serialVersionUID = 3386347937331480513L;
    private final Player self;
    private final int boardHeight;
    private final int boardWidth;

    public GameStartResponse(final ResponseCode code, final Player self, final int boardHeight, final int boardWidth) {
        super(code, ResponseType.GAME_START);
        this.self = self;
        this.boardHeight = boardHeight;
        this.boardWidth = boardWidth;
    }

    public Player getSelf() {
        return self;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

}
