package carcassonne.control;

import java.util.Stack;
import java.util.function.Consumer;

import carcassonne.control.state.AbstractControllerState;
import carcassonne.control.state.StatePlacing;
import carcassonne.control.state.StateWaiting;
import carcassonne.model.Player;
import carcassonne.model.Round;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.grid.GridSpot;
import carcassonne.model.tile.Tile;
import carcassonne.model.tile.TileStack;
import carcassonne.server.BroadcastMessage;
import carcassonne.server.GameStartMessage;
import carcassonne.server.MeeplePlacedMessage;
import carcassonne.server.PlacingSkippedMessage;
import carcassonne.server.TilePlacedMessage;
import carcassonne.server.WelcomeMessage;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Subscriber implements Consumer<BroadcastMessage> {

    private final MainController controller;

    public Subscriber(final MainController controller) {
        this.controller = controller;
    }

    @Override
    public void accept(BroadcastMessage message) {
        switch (message.getType()) {
        case GAME_START:
            processGameStart((GameStartMessage) message);
            break;
        case TILE_PLACED:
            processTilePlaced((TilePlacedMessage) message);
            break;
        case MEEPLE_PLACED:
            processMeeplePlaced((MeeplePlacedMessage) message);
            break;
        case PLACING_SKIPPED:
            processPlacingSkipped((PlacingSkippedMessage) message);
            break;
        case WELCOME:
            processWelcome((WelcomeMessage) message);
            break;
        default:
            throw new NotImplementedException();
        }
    }

    private void processGameStart(final GameStartMessage gameStart) {
        final Round round = gameStart.getRound();
        final AbstractControllerState currentState = controller.getState();
        final TileStack newStack = createStack(gameStart.getRound().getTileStack());
        currentState.startNewRoundWithParameters(round.getPlayerCount(), gameStart.getBoardWidth(),
                gameStart.getBoardHeight(), newStack);
        final int playerIndex = gameStart.getPlayerIndex();
        controller.setOwnPlayer(playerIndex);
        if (playerIndex == 0) {
            currentState.changeState(StatePlacing.class);
        } else {
            currentState.changeState(StateWaiting.class);
        }
    }

    private TileStack createStack(final TileStack tileStack) {
        final Stack<Tile> oldStack = tileStack.getStack();
        final Stack<Tile> newStack = new Stack<Tile>();
        newStack.setSize(oldStack.size());
        for (int i = 0; i < oldStack.size(); i++) {
            newStack.set(i, new Tile(oldStack.get(i).getType()));
        }
        return new TileStack(newStack);
    }

    private void processTilePlaced(final TilePlacedMessage message) {
        if (!isOwnOrigin(message)) {
            final AbstractControllerState currentState = controller.getState();
            final GridSpot spotMessage = message.getTile();
            final int x = spotMessage.getX();
            final int y = spotMessage.getY();
            final int rotation = spotMessage.getTile().getRotation();
            final Tile currentTile = currentState.getCurrentTile();
            for (int i = 0; i < rotation; i++) {
                currentTile.rotateRight();
            }
            final Tile receivedTile = spotMessage.getTile();
            final boolean sameTile = receivedTile.getType() == currentTile.getType()
                    && receivedTile.getRotation() == currentTile.getRotation();
            if (!sameTile) {
                throw new AssertionError();
            }
            final boolean success = currentState.getGrid().place(x, y, currentTile);
            if (!success) {
                throw new AssertionError();
            }
            final GridSpot newSpot = currentState.getGrid().getSpot(x, y);
            currentState.updateTilePlaced(newSpot);
        }
    }

    private void processMeeplePlaced(MeeplePlacedMessage message) {
        if (!isOwnOrigin(message)) {
            final AbstractControllerState currentState = controller.getState();
            final int x = message.getSpot().getX();
            final int y = message.getSpot().getY();
            final Tile tile = currentState.getGrid().getSpot(x, y).getTile();
            final Player player = currentState.getPlayer(message.getOriginator());
            final GridDirection direction = message.getDirection();
            currentState.updateMeeplePlaced(tile, player, direction);
        }
    }

    private void processPlacingSkipped(PlacingSkippedMessage message) {
        if (!isOwnOrigin(message)) {
            final AbstractControllerState currentState = controller.getState();
            currentState.skipCurrentTile();
            currentState.startNextTurn();
        }
    }

    private void processWelcome(WelcomeMessage message) {
        controller.showNumPlayersDialog(message.getNumPlayers());
    }

    private boolean isOwnOrigin(final BroadcastMessage message) {
        final int messageIndex = message.getOriginator();
        final int ownPlayer = controller.getState().getOwnPlayer();
        return messageIndex == ownPlayer;
    }
}
