package carcassonne.view;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import carcassonne.control.GameOptions;
import carcassonne.control.MainController;

/**
 * Super class for all other smaller GUI beneath the main GUI.
 * @author Timur
 */
public abstract class SmallGUI extends JPanel {
    private static final long serialVersionUID = 4056347951568551115L;
    protected MainController controller;
    protected GameOptions options;
    protected JFrame frame;
    protected GridBagConstraints constraints;

    /**
     * Constructor for the class. Sets the controller of the GUI and the window title.
     * @param controller sets the controller.
     * @param title sets the window title.
     */
    public SmallGUI(MainController controller, String title) {
        super(new GridBagLayout());
        this.controller = controller;
        options = GameOptions.getInstance();
        constraints = new GridBagConstraints();
        buildFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO (later) remove exit operation

    }

    /*
     * Builds the frame and sets its properties.
     */
    private void buildFrame(String title) {
        frame = new JFrame(title);
        frame.getContentPane().add(this);
        setBackground(new Color(190, 190, 190)); // grey
        frame.setResizable(false);
    }

    /**
     * Packs and shows the frame. should be called at the end of a constructor of a subclass.
     */
    protected void finishFrame() {
        frame.pack();
        frame.setVisible(true);
    }

}