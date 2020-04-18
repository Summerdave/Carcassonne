package carcassonne.control.state;

import carcassonne.control.MainController;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.grid.GridPattern;
import carcassonne.view.GameMessage;
import carcassonne.view.main.MainGUI;
import carcassonne.view.secondary.PlacementGUI;
import carcassonne.view.secondary.RotationGUI;
import carcassonne.view.tertiary.GameStatisticsGUI;

/**
 * The specific state where the statistics are shown can be placed.
 * 
 * @author Timur Saglam
 */
public class StateGameOver extends AbstractControllerState {

    private GameStatisticsGUI gameStatistics;
    private MainGUI mainGUI;

    /**
     * Constructor of the state.
     * 
     * @param controller   sets the Controller
     * @param mainGUI      sets the MainGUI
     * @param rotationGUI  sets the RotationGUI
     * @param placementGUI sets the PlacementGUI
     * @param scoreboard   sets the Scoreboard
     */
    public StateGameOver(MainController controller, RotationGUI rotationGUI, PlacementGUI placementGUI,
            MainGUI mainGUI) {
        super(controller, rotationGUI, placementGUI, mainGUI);
        this.mainGUI = mainGUI;
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#abortGame()
     */
    @Override
    public void abortGame() {
        GameMessage.showWarning("You already aborted the current game. Close the game statistics to start a new game.");
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
        exit();
        changeState(StateIdle.class);
        startNewRound(playerCount);
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#placeMeeple()
     */
    @Override
    public void placeMeeple(GridDirection position) {
        throw new IllegalStateException("Placing meeples in StateGameOver is not allowed.");
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#placeTile()
     */
    @Override
    public boolean placeTile(int x, int y) {
        return false;
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#skip()
     */
    @Override
    public void skip() {
        scoreboard.disable();
        exit();
        changeState(StateIdle.class);
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#entry()
     */
    @Override
    protected void entry() {
        System.out.println("FINAL PATTERNS:"); // TODO (LOW) remove debug output
        for (GridPattern pattern : grid.getAllPatterns()) {
            System.out.println(pattern); // TODO (LOW) remove debug output
            pattern.forceDisburse();
        }
        updateScores();
        updateStackSize();
        mainGUI.resetMenuState();
        GameMessage.showMessage("The game is over. Winning player(s): " + round.getWinningPlayers());
        gameStatistics = new GameStatisticsGUI(controller, round);
        // mainGUI.addSubInterfaces(gameStatistics, placementGUI, rotationGUI);
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#exit()
     */
    @Override
    protected void exit() {
        gameStatistics.closeGUI();
    }
}