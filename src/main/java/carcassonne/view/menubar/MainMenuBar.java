package carcassonne.view.menubar;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import carcassonne.control.MainController;
import carcassonne.settings.GameSettings;
import carcassonne.settings.Notifiable;

/**
 * The menu bar for the main GUI.
 * @author Timur Saglam
 */
public class MainMenuBar extends JMenuBar implements Notifiable {

    private static final long serialVersionUID = -599734693130415390L;
    private static final String GAME = "Game";
    private static final String ABORT = "Abort Current Game";
    private static final String NEW_ROUND = "Start New Round";
    private static final String SETTINGS_OF = "Settings of ";
    private static final String SETTINGS = "Player Settings";
    private static final String PLAYERS = " Players";
    private static final String AMOUNT = "Amount of Players";
    private static final String CHAOS_MODE = "Enable Chaos Mode";
    private static final String OPTIONS = "Options";
    private static final String LARGE_SPACE = "          ";
    private final MainController controller;
    private final GameSettings settings;
    private JMenu menuGame;
    private JMenu menuOptions;
    private JMenu menuPlayers;
    private JMenu menuSettings;
    private JMenuItem itemNewRound;
    private JMenuItem itemAbortRound;
    private JMenuItem[] itemSettings;
    private final Scoreboard scoreboard;

    /**
     * Simple constructor creating the menu bar.
     * @param scoreboard sets the scoreboard of the menu bar.
     * @param controller sets the connection to game the controller.
     */
    public MainMenuBar(MainController controller) { // TODO (MEDIUM) make menu bar gray
        super();
        this.controller = controller;
        settings = controller.getProperties();
        settings.registerNotifiable(this);
        scoreboard = new Scoreboard(settings);
        buildMenuGame();
        buildMenuOptions();
        add(new JLabel(LARGE_SPACE));
        add(scoreboard);
    }

    /**
     * Grants access to the scoreboard of the menu bar.
     * @return the scoreboard.
     */
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
    public void notifyChange() {
        for (int i = 0; i < itemSettings.length; i++) {
            Color color = settings.getPlayerColor(i).textColor();
            String name = settings.getPlayerName(i);
            itemSettings[i].setForeground(color);
            itemSettings[i].setText(SETTINGS_OF + name);
        }
    }

    // adds labels of the scoreboard to the menu bar.
    private void add(Scoreboard scoreboard) {
        for (JLabel label : scoreboard.getLabels()) {
            add(label);
        }
    }

    private void buildMenuGame() {
        itemNewRound = new JMenuItem(NEW_ROUND);
        itemAbortRound = new JMenuItem(ABORT);
        itemAbortRound.setEnabled(false);
        itemNewRound.addMouseListener(new NewRoundMouseAdapter(controller, itemNewRound, itemAbortRound));
        itemAbortRound.addMouseListener(new AbortRoundMouseAdapter(controller, itemNewRound, itemAbortRound));
        // build menu:
        menuGame = new JMenu(GAME);
        menuGame.add(itemNewRound);
        menuGame.add(itemAbortRound);
        add(menuGame);
    }

    private void buildMenuOptions() {
        buildMenuPlayers();
        buildMenuSettings();
        notifyChange(); // set colors
        menuOptions = new JMenu(OPTIONS);
        menuOptions.add(menuPlayers);
        menuOptions.add(menuSettings);
        JCheckBoxMenuItem itemChaosMode = new JCheckBoxMenuItem(CHAOS_MODE);
        itemChaosMode.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                settings.setChaosMode(!itemChaosMode.isSelected());
            }
        });
        menuOptions.add(itemChaosMode);
        add(menuOptions);
    }

    private void buildMenuPlayers() {
        menuPlayers = new JMenu(AMOUNT);
        JRadioButtonMenuItem[] itemPlayerCount = new JRadioButtonMenuItem[GameSettings.MAXIMAL_PLAYERS - 1];
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < itemPlayerCount.length; i++) {
            itemPlayerCount[i] = new JRadioButtonMenuItem((i + 2) + PLAYERS);
            itemPlayerCount[i].addMouseListener(new MenuPlayersMouseAdapter(settings, (i + 2)));
            group.add(itemPlayerCount[i]);
            menuPlayers.add(itemPlayerCount[i]);
        }
        itemPlayerCount[0].setSelected(true);
    }

    private void buildMenuSettings() { // TODO (MEDIUM) reduce duplication
        itemSettings = new JMenuItem[GameSettings.MAXIMAL_PLAYERS];
        menuSettings = new JMenu(SETTINGS);
        for (int i = 0; i < itemSettings.length; i++) {
            itemSettings[i] = new JMenuItem();
            itemSettings[i].addMouseListener(scoreboard.getSettingsMouseListener(i));
            menuSettings.add(itemSettings[i]);
        }
    }

    public void enableStart() {
        itemNewRound.setEnabled(true);
        itemAbortRound.setEnabled(false);
    }
}
