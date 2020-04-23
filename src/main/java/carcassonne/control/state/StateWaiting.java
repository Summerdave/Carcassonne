package carcassonne.control.state;

import carcassonne.control.MainController;
import carcassonne.model.grid.CoordinatePair;
import carcassonne.model.grid.GridDirection;
import carcassonne.view.GameMessage;
import carcassonne.view.main.MainGUI;
import carcassonne.view.secondary.PlacementGUI;
import carcassonne.view.secondary.RotationGUI;

public class StateWaiting extends AbstractControllerState {

    public StateWaiting(MainController controller, RotationGUI rotationGUI, PlacementGUI placementGUI,
            MainGUI mainGUI) {
        super(controller, rotationGUI, placementGUI, mainGUI);
    }

    @Override
    public void abortGame() {
        changeState(StateGameOver.class);
    }

    @Override
    public boolean isPlaceable(GridDirection position) {
        return false;
    }

    @Override
    public void newRound(int playerCount) {
        GameMessage.showWarning("Abort the current game before starting a new one.");
    }

    @Override
    public void placeMeeple(GridDirection position) {
        throw new IllegalStateException("Placing meeples in StateWaiting is not allowed.");
    }

    @Override
    public boolean placeTile(final CoordinatePair position) {
        return false;
    }

    @Override
    public void skip() {
        // do nothing.
    }

    @Override
    protected void entry() {

    }

    @Override
    protected void exit() {

    }

}
