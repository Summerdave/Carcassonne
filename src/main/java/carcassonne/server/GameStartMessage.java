package carcassonne.server;

import carcassonne.model.Round;

public class GameStartMessage extends BroadcastMessage {

    private static final long serialVersionUID = 9153708491032218456L;
    private final int boardWidth;
    private final int boardHeight;
    private final int playerIndex;
    private Round round;

    public GameStartMessage(final int originator, final int boardWidth, final int boardHeight, final Round round,
            final int playerIndex) {
        super(originator, BroadcastMessageType.GAME_START);
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.round = round;
        this.playerIndex = playerIndex;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public Round getRound() {
        return round;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

}
