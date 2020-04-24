package carcassonne.view.preview;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

import carcassonne.model.tile.Tile;
import carcassonne.view.main.MainGUI;

public class PreviewGUI extends JDialog {

    private static final long serialVersionUID = -6791315093417492256L;
    private final JLabel tileView;
    private final int width;
    private final int height;

    public PreviewGUI(final MainGUI ui, final Tile initial) {
        super(ui);
        tileView = new JLabel();
        width = 100;
        height = 100;
        getContentPane().add(tileView);
        setTile(initial);
        setResizable(false);
        setAlwaysOnTop(true);
        setLocation(200, 200);
        setVisible(true);
        pack();
    }

    public void setTile(final Tile tile) {
        final ImageIcon original = tile.getIcon();
        final ImageIcon rescaled = new ImageIcon(
                original.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
        tileView.setIcon(rescaled);
        revalidate();
        repaint();
        pack();
    }
}
