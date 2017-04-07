/*
 * Creation : 9 févr. 2017
 */
package visu;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;

public final class BarreProgression extends JProgressBar {

    private static final long serialVersionUID = 1L;

    public BarreProgression() {
        setUI(new MyProgressUI());
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        setValue(0);
        setStringPainted(true);
    }

}

final class MyProgressUI extends BasicProgressBarUI {

    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        super.paintDeterminate(g, c);
        c.setForeground(Color.DARK_GRAY);
    }
}
