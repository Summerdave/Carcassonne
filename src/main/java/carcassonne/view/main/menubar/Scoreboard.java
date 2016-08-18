package carcassonne.view.main.menubar;

import javax.swing.JLabel;

import carcassonne.control.GameOptions;

/**
 * Is the scoreboard class of the game. Manages a score label for each player.
 * @author Timur Saglam
 */
public class Scoreboard {
    private JLabel[] scoreLabel;
    private final GameOptions options;

    /**
     * Standard constructor. Creates score board.
     */
    public Scoreboard() {
        options = GameOptions.getInstance();
        scoreLabel = new JLabel[options.maximalPlayers];
        for (int i = 0; i < scoreLabel.length; i++) {
            scoreLabel[i] = new JLabel();
            update(i, 0);
            scoreLabel[i].setForeground(options.getPlayerColor(i));
            scoreLabel[i].setVisible(false);
        }

    }

    /**
     * Updates a label of the scoreboard.
     * @param playerNumber is the number of the player whose label should get updated.
     * @param points is the amount of points the players has.
     */
    public void update(int playerNumber, int points) {
        String playerName = "Player ";
        for (int i = 0; i <= playerNumber; i++) {
            playerName += "I";
        }
        scoreLabel[playerNumber].setText("[" + playerName + ": " + points + "]    ");
    }

    /**
     * Only shows the specified amount of labels.
     * @param playerCount is the amount of players to show labels for.
     */
    public void rebuild(int playerCount) {
        for (int i = 0; i < playerCount; i++) {
            scoreLabel[i].setVisible(true);
        }
    }

    /**
     * Enables all the scoreboard labels.
     */
    public void enable() {
        for (JLabel label : scoreLabel) {
            label.setVisible(true);
        }
    }

    /**
     * Disables all the scoreboard labels.
     */
    public void disable() {
        for (JLabel label : scoreLabel) {
            label.setVisible(false);
        }
    }

    /**
     * Grants access to the labels themselves.
     * @return the array of labels.
     */
    public JLabel[] getLabels() {
        return scoreLabel;
    }

}
