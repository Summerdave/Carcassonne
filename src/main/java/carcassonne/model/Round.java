package carcassonne.model;

import java.util.LinkedList;
import java.util.List;

import carcassonne.model.grid.Grid;
import carcassonne.model.tile.Tile;
import carcassonne.model.tile.TileStack;
import carcassonne.settings.GameSettings;

/**
 * An object of the round class simulates a game round. It does not actively control the game. It represents the round
 * and its information in an object.
 * @author Timur Saglam
 */
public class Round {

    private int activePlayerIndex;
    private Tile currentTile;
    private Grid grid;
    private Player[] players;
    private final int playerCount;
    private final TileStack tileStack;

    /**
     * Simple constructor that creates the grid, the tile stack and the players.
     * @param playerCount is the amount of players of the round.
     * @param grid is the grid of the round.
     * @param settings are the {@link GameSettings}.
     */
    public Round(int playerCount, Grid grid, GameSettings settings) {
        this.grid = grid;
        this.playerCount = playerCount;
        tileStack = new TileStack(playerCount, !settings.isChaosMode()); // TODO (HIGH) remove from round?
        createPlayers(settings);
        currentTile = grid.getFoundation().getTile();
    }

    /**
     * Getter for the active players of the round.
     * @return the players whose turn it is.
     */
    public Player getActivePlayer() {
        return players[activePlayerIndex];
    }

    /**
     * Getter for the current tile, that was drawn from the tile stack.
     * @return the current tile of the turn.
     */
    public Tile getCurrentTile() {
        return currentTile;
    }

    /**
     * Returns the current tile to the stack.
     */
    public void skipCurrentTile() {
        tileStack.putBack(currentTile);
    }

    /**
     * Getter for a specific players of the round.
     * @param playerNumber is the number of the specific players.
     * @return returns the players.
     */
    public Player getPlayer(int playerNumber) {
        return players[playerNumber];
    }

    /**
     * Getter for the amount of players in the round.
     * @return the amount of players.
     */
    public int getPlayerCount() {
        return playerCount;
    }

    /**
     * Returns amounts of tiles left on the stack.
     * @return the stack size.
     */
    public int getStackSize() {
        return tileStack.getSize();
    }

    /**
     * Method determines the winning players by the highest score.
     * @return a list of names of the winning players.
     */
    public List<String> getWinningPlayers() {
        List<String> winnerList = new LinkedList<>();
        int maxScore = 0;
        for (Player player : players) {
            if (player.getScore() >= maxScore) {
                if (player.getScore() > maxScore) {
                    winnerList.clear();
                }
                winnerList.add(player.getName());
                maxScore = player.getScore();
            }
        }
        return winnerList;
    }

    /**
     * Checks whether the game round is over. A game round is over if the grid is full or the stack of tiles is empty (no
     * tiles left).
     * @return true if the game is over.
     */
    public boolean isOver() {
        return grid.isFull() || tileStack.isEmpty();
    }

    /**
     * Method the starts the turn of the next players a draws a tile from the stack.
     */
    public void nextTurn() {
        activePlayerIndex = ++activePlayerIndex % players.length;
        currentTile = tileStack.drawTile();
    }
    
    public void setGrid(final Grid grid) {
        this.grid = grid;
    }

    /**
     * creates the players objects and sets the first players as active players.
     * @param playerCount is the number of players in the range of [1, <code>GameOptions.MAXIMAL_PLAYERS]</code>.
     */
    private void createPlayers(GameSettings settings) {
        if (playerCount <= 1 || playerCount > GameSettings.MAXIMAL_PLAYERS) {
            throw new IllegalArgumentException(playerCount + " is not a valid players count");
        }
        players = new Player[playerCount]; // initialize the players array.
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player(i, settings); // create the players.
        }
        activePlayerIndex = -1; // first player can only start after first tile is drawn via nextTurn()
    }
}
