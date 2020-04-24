package carcassonne.server;

public class WelcomeMessage extends BroadcastMessage {

    private static final long serialVersionUID = 4087070408290478578L;
    private final int numPlayers;

    public WelcomeMessage(int numPlayers) {
        super(-1, BroadcastMessageType.WELCOME);
        this.numPlayers = numPlayers;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

}
