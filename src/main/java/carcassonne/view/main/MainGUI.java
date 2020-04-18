package carcassonne.view.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JFrame;

import carcassonne.control.MainController;
import carcassonne.view.menubar.MainMenuBar;
import carcassonne.view.menubar.Scoreboard;

/**
 * The main GUI class.
 * 
 * @author Timur Saglam
 */
public class MainGUI extends JFrame {

    private static final long serialVersionUID = 721988213478987914L;

    private BoardGUI boardGui;
    private MainController controller;
    private MainMenuBar menuBar;

    /**
     * Constructor of the main GUI. creates the GUI with a scoreboard.
     * 
     * @param scoreboard sets the scoreboard.
     * @param controller sets the connection to the game controller.
     * @param boardGUI
     */
    public MainGUI(MainController controller, BoardGUI boardGUI) {
        this.boardGui = boardGUI;
        this.controller = controller;
        buildFrame(boardGui);
    }

    private void buildFrame(Component board) {
        menuBar = new MainMenuBar(controller);
        setJMenuBar(menuBar);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        add(board, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    public Scoreboard getScoreboard() {
        return menuBar.getScoreboard(); // TODO (MEDIUM) Find better solution.
    }

    public void resetMenuState() {
        menuBar.enableStart(); // TODO (MEDIUM) Find better solution.
    }

    public BoardGUI getBoard() {
        return boardGui;
    }

    public void setBoard(final BoardGUI boardGUI) {
        remove(this.boardGui);
        add(boardGUI);
        revalidate();
        repaint();
        this.boardGui = boardGUI;
    }

}