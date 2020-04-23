package carcassonne.control;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import carcassonne.client.Client;
import carcassonne.control.state.AbstractControllerState;
import carcassonne.control.state.StateGameOver;
import carcassonne.control.state.StateIdle;
import carcassonne.control.state.StateManning;
import carcassonne.control.state.StatePlacing;
import carcassonne.control.state.StateWaiting;
import carcassonne.model.Meeple;
import carcassonne.model.Player;
import carcassonne.model.Round;
import carcassonne.model.grid.CoordinatePair;
import carcassonne.model.grid.Grid;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.grid.GridSpot;
import carcassonne.model.tile.Tile;
import carcassonne.model.tile.TileType;
import carcassonne.settings.GameSettings;
import carcassonne.view.main.BoardGUI;
import carcassonne.view.main.MainGUI;
import carcassonne.view.main.TileLabel;
import carcassonne.view.secondary.PlacementGUI;
import carcassonne.view.secondary.RotationGUI;

/**
 * The MainController is the central class of the game. The game is started with
 * the instantiation of this class. The class gets the user input from the
 * <code>MouseAdapter</code> in the <code>view package</code>, and controls both
 * the <code>view</code> and the <code>model</code>. The <code>controller</code>
 * package also contains the state machine, which consists out of the
 * <code>MainController</code> class and the state classes. This system
 * implements the model/view/controller architecture, which is not 100% formally
 * implemented. The reason for this is that in the user input is made in Swing
 * through the <code>MouseAdapters</code>, which belong to the <code>view</code>
 * package.
 * 
 * @author Timur Saglam
 */
public class MainController {
    private MainGUI mainGUI;
    private RotationGUI rotationGUI;
    private PlacementGUI placementGUI;
    private Map<Class<? extends AbstractControllerState>, AbstractControllerState> stateMap;
    private AbstractControllerState currentState;
    private GameSettings settings;
    private final Client client;
    private final Subscriber subscriber;

    /**
     * Basic constructor. Creates the view and the model of the game.
     */
    public MainController() {
        settings = new GameSettings();
        rotationGUI = new RotationGUI(this, mainGUI);
        placementGUI = new PlacementGUI(this, mainGUI);
        stateMap = new HashMap<>();
        final BoardGUI boardGUI = createBoard();
        mainGUI = new MainGUI(this, boardGUI);
        settings.registerNotifiable(mainGUI.getScoreboard());
        settings.registerNotifiable(placementGUI);
        settings.registerNotifiable(rotationGUI);
        currentState = new StateIdle(this, rotationGUI, placementGUI, mainGUI);
        registerState(currentState);
        registerState(new StateManning(this, rotationGUI, placementGUI, mainGUI));
        registerState(new StatePlacing(this, rotationGUI, placementGUI, mainGUI));
        registerState(new StateGameOver(this, rotationGUI, placementGUI, mainGUI));
        registerState(new StateWaiting(this, rotationGUI, placementGUI, mainGUI));
        this.subscriber = new Subscriber(this);
        this.client = new Client(GameSettings.getServer(), this.subscriber);
        try {
            client.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the main GUI.
     */
    public void startGame() {
        mainGUI.setVisible(true);
    }

    /**
     * Changes the state of the controller to a new state.
     * 
     * @param stateType specifies which state is the new state.
     * @return the new state.
     */
    public AbstractControllerState changeState(Class<? extends AbstractControllerState> stateType) { // (HIGH)
        currentState = stateMap.get(stateType);
        if (currentState == null) {
            throw new IllegalStateException("State is not registered: " + stateType);
        }
        rotationGUI.refresh();
        placementGUI.refresh();
        return currentState;
    }

    /**
     * Requests to abort the round.
     */
    public void requestAbortGame() {
        currentState.abortGame();
    }

    /**
     * Method for the view to see whether a meeple is placeable on a specific tile.
     * 
     * @param position is the specific position on the tile.
     * @return true if a meeple can be placed on the position on the current tile.
     */
    public boolean requestPlacementStatus(GridDirection position) {
        return currentState.isPlaceable(position);
    }

    /**
     * Method for the view to call if a user mans a tile with a meeple.
     * 
     * @param position is the position the user wants to place on.
     */
    public void requestMeeplePlacement(GridDirection position) {
        currentState.placeMeeple(position);
    }

    /**
     * Requests to start a new round with a specific amount of players.
     */
    public void requestNewRound() {
        currentState.newRound(settings.getAmountOfPlayers());
    }

    /**
     * Method for the view to call if the user wants to skip a round.
     */
    public void requestSkip() {
        currentState.skip();
    }

    /**
     * Method for the view to call if a user places a tile.
     * 
     * @param x is the x coordinate.
     * @param y is the y coordinate.
     */
    public boolean requestTilePlacement(final CoordinatePair position) {
        return currentState.placeTile(position);
    }

    /**
     * Updates the round and the grid of every state after a new round has been
     * started.
     * 
     * @param newRound sets the new round.
     * @param newGrid  sets the new grid.
     */
    public void updateStates(Round newRound, Grid newGrid) {
        mainGUI.getScoreboard().rebuild(newRound.getPlayerCount());
        for (AbstractControllerState state : stateMap.values()) {
            state.updateState(newRound, newGrid);
        }
    }

    /**
     * Getter for the {@link GameSettings}, which grants access to the games
     * settings.
     * 
     * @return the {@link GameSettings} instance.
     */
    public GameSettings getProperties() {
        return settings;
    }

    /**
     * Registers a specific state at the controller.
     * 
     * @param state is the specific state.
     */
    private void registerState(AbstractControllerState state) {
        if (stateMap.put(state.getClass(), state) != null) {
            throw new IllegalArgumentException("Can't register two states of a kind.");
        }
    }

    public AbstractControllerState getState() {
        return currentState;
    }

    private BoardGUI createBoard() {
        final BoardGUI boardGUI = new BoardGUI(this);
        // boardGUI.addSubInterfaces(placementGUI, rotationGUI);
        settings.registerNotifiable(boardGUI);
        return boardGUI;
    }

    private void drawBoard(final Grid grid, final BoardGUI board) {
        drawBoard(grid, board, false);
    }

    private void drawBoard(final Grid grid, final BoardGUI board, final boolean drawEmptyTiles) {
        final int width = grid.getWidth();
        final int height = grid.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final GridSpot spot = grid.getSpot(x, y);
                board.getTile(x, y);
                if (spot.isOccupied()) {
                    final Tile tile = spot.getTile();
                    final Meeple meeple = tile.getMeeple();
                    board.setTile(tile, x, y);
                    if (meeple != null) {
                        final GridDirection direction = meeple.getPosition();
                        final Player owner = meeple.getOwner();
                        board.setMeeple(tile, direction, owner);
                    }
                } else if (drawEmptyTiles) {
                    board.setTile(new Tile(TileType.Null), x, y);
                }
            }
        }
        redrawHighlights(grid, board);
    }

    private void redrawHighlights(final Grid grid, final BoardGUI board) {
        final int width = grid.getWidth();
        final int height = grid.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final GridSpot spot = grid.getSpot(x, y);
                final TileLabel label = board.getTile(x, y);
                if (spot.isOccupied()) {
                    continue;
                }
                boolean adjacentToBoard = false;
                if (x > 0) {
                    adjacentToBoard |= grid.getSpot(x - 1, y).isOccupied();
                }
                if (x < width - 1) {
                    adjacentToBoard |= grid.getSpot(x + 1, y).isOccupied();
                }
                if (y > 0) {
                    adjacentToBoard |= grid.getSpot(x, y - 1).isOccupied();
                }
                if (y < height - 1) {
                    adjacentToBoard |= grid.getSpot(x, y + 1).isOccupied();
                }
                if (adjacentToBoard) {
                    label.highlight();
                }
            }
        }
    }

    public void alignGrid() {
        final Grid oldGrid = this.currentState.getGrid();
        final Grid movedGrid = moveGrid(oldGrid);
        final Grid newGrid = zoomGrid(movedGrid);

        final int boardWidth = oldGrid.getWidth() * mainGUI.getBoard().getTileSize();
        final int boardHeight = oldGrid.getHeight() * mainGUI.getBoard().getTileSize();
        final int tileWidthComputed = boardWidth / newGrid.getWidth();
        final int tileHeightComputed = boardHeight / newGrid.getHeight();
        int tileSize = Integer.min(Integer.min(tileWidthComputed, tileHeightComputed), GameSettings.TILE_SIZE);
        final BoardGUI boardGUI = new BoardGUI(this, tileSize, newGrid.getWidth(), newGrid.getHeight());
        drawBoard(newGrid, boardGUI);
        mainGUI.setBoard(boardGUI);
        rotationGUI.refresh();
        placementGUI.refresh();
        // mainGUI.pack();
    }

    private Grid moveGrid(final Grid oldGrid) {
        final CoordinatePair centeringOffsets = Grid.getOffsetForCentering(oldGrid.getSize(),
                oldGrid.getEffectiveSize());
        if (!centeringOffsets.isZero()) {
            oldGrid.move(centeringOffsets);
        }
        return oldGrid;
    }

    private Grid zoomGrid(final Grid oldGrid) {
        final boolean needsResize = oldGrid.needsResize();
        if (needsResize) {
            final int newWidth = oldGrid.getWidth() + 2;
            final int newHeight = oldGrid.getHeight() + 2;
            final Grid newGrid = oldGrid.copy(newWidth, newHeight);
            for (final AbstractControllerState state : this.stateMap.values()) {
                state.setGrid(newGrid);
            }
            return newGrid;
        }
        return oldGrid;
    }

    public Client getClient() {
        return client;
    }

    public void setOwnPlayer(int playerIndex) {
        stateMap.values().forEach(s -> s.setOwnPlayer(playerIndex));
    }

}
