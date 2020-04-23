package carcassonne.control.state;

import carcassonne.control.MainController;
import carcassonne.model.grid.CoordinatePair;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.tile.Tile;
import carcassonne.view.GameMessage;
import carcassonne.view.main.MainGUI;
import carcassonne.view.secondary.PlacementGUI;
import carcassonne.view.secondary.RotationGUI;

/**
 * The specific state when a Tile can be placed.
 * 
 * @author Timur Saglam
 */
public class StatePlacing extends AbstractControllerState {

    /**
     * Constructor of the state.
     * 
     * @param controller   sets the controller.
     * @param boardGUI     sets the MainGUI
     * @param rotationGUI  sets the RotationGUI
     * @param placementGUI sets the PlacementGUI
     * @param scoreboard   sets the Scoreboard
     */
    public StatePlacing(MainController controller, RotationGUI rotationGUI, PlacementGUI placementGUI,
            MainGUI mainGUI) {
        super(controller, rotationGUI, placementGUI, mainGUI);
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#abortGame()
     */
    @Override
    public void abortGame() {
        changeState(StateGameOver.class);
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#isPlaceable()
     */
    @Override
    public boolean isPlaceable(GridDirection position) {
        return false; // can never place meeple in this state.
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#newRound()
     */
    @Override
    public void newRound(int playerCount) {
        GameMessage.showWarning("Abort the current game before starting a new one.");

    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#placeMeeple()
     */
    @Override
    public void placeMeeple(GridDirection position) {
        throw new IllegalStateException("Placing meeples in StatePlacing is not allowed.");
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#placeTile()
     */
    @Override
    public boolean placeTile(final CoordinatePair position) {
        final int x = position.getX();
        final int y = position.getY();
        Tile tile = round.getCurrentTile();
        if (grid.place(x, y, tile)) {
            controller.getClient().sendTilePlaced(tile.getGridSpot(), this.getPlayer());
            updateTilePlaced(tile.getGridSpot());
            changeState(StateManning.class);
            return true;
        }
        return false;
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#skip()
     */
    @Override
    public void skip() {
        if (round.isOver()) {
            changeState(StateGameOver.class);
        } else {
            controller.getClient().sendPlacingSkipped(round.getActivePlayer());
            round.skipCurrentTile();
            startNextTurn();
        }
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#entry()
     */
    @Override
    protected void entry() {
        Tile currentTile = round.getCurrentTile();
        // for (int i = 0; i < Math.round(Math.random() * 4 - 0.5); i++) {
        // currentTile.rotateRight(); // Random rotation with equal chance for each
        // rotation.
        // }
        rotationGUI.setTile(currentTile, round.getActivePlayer());
        updateStackSize();
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#exit()
     */
    @Override
    protected void exit() {
        rotationGUI.setVisible(false);
    }

}
