package carcassonne.control.state;

import carcassonne.control.MainController;
import carcassonne.model.Player;
import carcassonne.model.grid.CastleAndRoadPattern;
import carcassonne.model.grid.CoordinatePair;
import carcassonne.model.grid.FieldsPattern;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.grid.GridPattern;
import carcassonne.model.terrain.TerrainType;
import carcassonne.model.tile.Tile;
import carcassonne.view.GameMessage;
import carcassonne.view.main.MainGUI;
import carcassonne.view.secondary.PlacementGUI;
import carcassonne.view.secondary.RotationGUI;

/**
 * The specific state when a Meeple can be placed.
 * 
 * @author Timur Saglam
 */
public class StateManning extends AbstractControllerState {

    /**
     * Constructor of the state.
     * 
     * @param controller   sets the controller.
     * @param boadGUI      sets the MainGUI
     * @param rotationGUI  sets the RotationGUI
     * @param placementGUI sets the PlacementGUI
     * @param scoreboard   sets the Scoreboard
     */
    public StateManning(MainController controller, RotationGUI rotationGUI, PlacementGUI placementGUI,
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
        Tile tile = round.getCurrentTile();
        TerrainType terrain = tile.getTerrain(position);
        boolean placeable = false;
        if (terrain == TerrainType.OTHER) {
            placeable = false; // you can never place on terrain other
        } else if (terrain == TerrainType.MONASTERY) {
            placeable = true; // you can always place on a monastery
        } else {
            GridPattern pattern;
            if (terrain == TerrainType.FIELDS) {
                pattern = new FieldsPattern(tile.getGridSpot(), position, grid);
            } else { // castle or road:
                pattern = new CastleAndRoadPattern(tile.getGridSpot(), position, terrain, grid);
            }
            if (pattern.isNotOccupied()) {
                placeable = true; // can place meeple
            }
            pattern.removeTileTags();
        }
        return placeable;
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
        Tile tile = round.getCurrentTile();
        Player player = round.getActivePlayer();
        if (position == null || (player.hasFreeMeeples() && isPlaceable(position))) {
            controller.getClient().sendMeeplePlaced(tile.getGridSpot(), this.getPlayer(), position);
            updateMeeplePlaced(tile, player, position);
        } else {
            GameMessage.showWarning("You can't place meeple directly on an occupied Castle or Road!");
        }
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#placeTile()
     */
    @Override
    public boolean placeTile(final CoordinatePair position) {
        return false;
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#skip()
     */
    @Override
    public void skip() {
        placeMeeple(null);
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#entry()
     */
    @Override
    protected void entry() {
        if (round.getActivePlayer().hasFreeMeeples()) {
            mainGUI.getBoard().setMeeplePreview(round.getCurrentTile(), round.getActivePlayer());
            placementGUI.setTile(round.getCurrentTile(), round.getActivePlayer());
        } else {
            GameMessage.showMessage(
                    "You have no Meeples left. Regain Meeples by completing patterns to place Meepeles again.");
            processGridPatterns();
            startNextTurn();
        }
    }

    /**
     * @see carcassonne.control.state.AbstractControllerState#exit()
     */
    @Override
    protected void exit() {
        placementGUI.setVisible(false);
    }
}
