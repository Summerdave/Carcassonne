package carcassonne.control.state;

import carcassonne.control.MainController;
import carcassonne.model.Meeple;
import carcassonne.model.Player;
import carcassonne.model.Round;
import carcassonne.model.grid.CoordinatePair;
import carcassonne.model.grid.Grid;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.grid.GridPattern;
import carcassonne.model.grid.GridSpot;
import carcassonne.model.tile.Tile;
import carcassonne.model.tile.TileStack;
import carcassonne.settings.GameMode;
import carcassonne.settings.GameSettings;
import carcassonne.view.main.MainGUI;
import carcassonne.view.menubar.Scoreboard;
import carcassonne.view.secondary.PlacementGUI;
import carcassonne.view.secondary.RotationGUI;

/**
 * Is the abstract state of the state machine.
 * 
 * @author Timur Saglam
 */
public abstract class AbstractControllerState {

    protected MainController controller;
    protected RotationGUI rotationGUI;
    protected PlacementGUI placementGUI;
    protected Round round;
    protected Grid grid;
    protected Scoreboard scoreboard;
    protected MainGUI mainGUI;
    private int ownPlayer = -1;

    /**
     * Constructor of the abstract state, sets the controller from the parameter,
     * registers the state at the controller and calls the <code>entry()</code>
     * method.
     * 
     * @param controller   sets the Controller
     * @param boardGUI     sets the BoardGUI
     * @param rotationGUI  sets the RotationGUI
     * @param placementGUI sets the PlacementGUI
     * @param scoreboard   sets the Scoreboard
     */
    public AbstractControllerState(MainController controller, RotationGUI rotationGUI, PlacementGUI placementGUI,
            MainGUI mainGUI) {
        this.controller = controller;
        this.rotationGUI = rotationGUI;
        this.placementGUI = placementGUI;
        this.mainGUI = mainGUI;
        scoreboard = mainGUI.getScoreboard();
    }

    /**
     * Starts new round with a specific amount of players.
     */
    public abstract void abortGame();

    /**
     * Method for the view to see whether a meeple is placeable on a specific tile.
     * 
     * @param position is the specific position on the tile.
     * @return true if a meeple can be placed on the position on the current tile.
     */
    public abstract boolean isPlaceable(GridDirection position);

    /**
     * Starts new round with a specific amount of players.
     * 
     * @param playerCount sets the amount of players.
     */
    public abstract void newRound(int playerCount);

    /**
     * Method for the view to call if a user mans a tile with a Meeple.
     * 
     * @param position is the placement position.
     */
    public abstract void placeMeeple(GridDirection position);

    public void updateMeeplePlaced(Tile tile, Player player, GridDirection position) {
        mainGUI.getBoard().resetMeeplePreview(tile);
        if (position != null) {
            tile.placeMeeple(player, position);
            mainGUI.getBoard().setMeeple(tile, position, player);
        }
        updateScores();
        processGridPatterns();
        startNextTurn();
    }

    // gives the players the points they earned.
    protected void processGridPatterns() {
        Tile tile = round.getCurrentTile();
        for (GridPattern pattern : grid.getModifiedPatterns(tile.getGridSpot())) {
            if (pattern.isComplete()) {
                for (Meeple meeple : pattern.getMeepleList()) {
                    mainGUI.getBoard().removeMeeple(meeple);
                }
                pattern.disburse();
                updateScores();
            }
        }
    }

    // starts the next turn and changes the state to state placing.
    public void startNextTurn() {
        if (round.isOver()) {
            changeState(StateGameOver.class);
        } else {
            round.nextTurn();
            mainGUI.getBoard().setCurrentPlayer(round.getActivePlayer());
            final int ownIndex = getOwnPlayer();
            final int currentIndex = round.getActivePlayer().getNumber();
            if (GameSettings.GAME_MODE == GameMode.LOCAL || currentIndex == ownIndex) {
                changeState(StatePlacing.class);
            } else {
                changeState(StateWaiting.class);
            }
        }
    }

    /**
     * Method for the view to call if a user places a tile.
     * 
     */
    public abstract boolean placeTile(CoordinatePair pair);

    public void updateTilePlaced(final GridSpot spot) {
        final int x = spot.getX();
        final int y = spot.getY();
        final Tile tile = spot.getTile();
        mainGUI.getBoard().setTile(tile, x, y);
        GridSpot newSpot = grid.getSpot(x, y);
        highlightSurroundings(newSpot);
        controller.alignGrid();
    }

    /**
     * Method for the view to call if the user wants to skip a round.
     */
    public abstract void skip();

    /**
     * Updates the round and the grid object after a new round was started.
     * 
     * @param round sets the new round.
     * @param grid  sets the new grid.
     */
    public void updateState(Round round, Grid grid) {
        this.round = round;
        this.grid = grid;
    }

    /**
     * Changes the state to a new state.
     * 
     * @param stateType is the type of the new state.
     */
    public void changeState(Class<? extends AbstractControllerState> stateType) {
        exit();
        AbstractControllerState newState = controller.changeState(stateType);
        controller.updateTilePreview(round.getCurrentTile());
        newState.entry();
    }

    /**
     * Entry method of the state.
     */
    protected abstract void entry();

    /**
     * Exit method of the state.
     */
    protected abstract void exit();

    /**
     * Starts a new round for a specific number of players.
     * 
     * @param playerCount is the specific number of players.
     */
    protected void startNewRound(int playerCount) {
        if (GameSettings.GAME_MODE == GameMode.LOCAL) {
            int gridWidth = GameSettings.BOARD_WIDTH;
            int gridHeight = GameSettings.BOARD_HEIGHT;
            startNewRoundWithParameters(playerCount, gridWidth, gridHeight, null);
            changeState(StatePlacing.class);
        } else {
            controller.getClient().requestGameStart();
        }
    }

    public synchronized void startNewRoundWithParameters(final int playerCount, final int gridWidth,
            final int gridHeight, final TileStack stack) {
        Grid newGrid = new Grid(gridWidth, gridHeight);
        final Round newRound;
        if (stack == null) {
            newRound = new Round(playerCount, newGrid, controller.getProperties());
        } else {
            newRound = new Round(playerCount, newGrid, controller.getProperties(), stack);
        }
        controller.updateStates(newRound, newGrid);
        updateScores();
        updateStackSize();
        GridSpot spot = round.getCurrentTile().getGridSpot(); // starting spot.
        mainGUI.getBoard().setTile(spot.getTile(), spot.getX(), spot.getY());
        highlightSurroundings(spot);
        round.nextTurn(); // first tile is drawn, player one is active.
        mainGUI.getBoard().setCurrentPlayer(round.getActivePlayer());
    }

    /**
     * Updates the round and the grid of every state after a new round has been
     * started.
     */
    protected void updateScores() {
        Player player;
        for (int playerNumber = 0; playerNumber < round.getPlayerCount(); playerNumber++) {
            player = round.getPlayer(playerNumber);
            scoreboard.update(player);
        }
    }

    /**
     * Updates the label which displays the current stack size.
     */
    protected void updateStackSize() {
        scoreboard.updateStackSize(round.getStackSize());
    }

    /**
     * Highlights the surroundings of a {@link GridSpot} on the main UI.
     * 
     * @param spot is the {@link GridSpot} that determines where to highlight.
     */
    protected void highlightSurroundings(GridSpot spot) {
        for (GridSpot neighbor : grid.getNeighbors(spot, true, GridDirection.directNeighbors())) {
            if (neighbor != null && neighbor.isFree()) {
                mainGUI.getBoard().setHighlight(neighbor.getX(), neighbor.getY());
            }
        }
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(final Grid grid) {
        this.grid = grid;
    }

    public Player getPlayer() {
        return round.getActivePlayer();
    }

    public void setOwnPlayer(int playerIndex) {
        this.ownPlayer = playerIndex;
    }

    public int getOwnPlayer() {
        return this.ownPlayer;
    }

    public Player getPlayer(final int i) {
        return round.getPlayer(i);
    }

    public Tile getCurrentTile() {
        return round.getCurrentTile();
    }

    public void skipCurrentTile() {
        round.skipCurrentTile();
    }

    public boolean isOwnTurn() {
        return getOwnPlayer() == round.getActivePlayer().getNumber();
    }

}
