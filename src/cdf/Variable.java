package cdf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import gui.TableView;
import paco.TableModelView;

public abstract class Variable {

    private final String longName;
    private final String shortName;
    private final String category;
    private final String swFeatureRef;
    private final String[] swUnitRef;
    private final String[][] swCsHistory;

    private static TableView tableView;
    private static JPanel panel;

    private static final HashMap<String, Integer> maturite = new HashMap<String, Integer>(6) {

        private static final long serialVersionUID = 1L;
        {
            put("---", 0);
            put("changed", 0);
            put("prelimcalibrated", 25);
            put("calibrated", 50);
            put("checked", 75);
            put("completed", 100);
        }
    };

    public Variable(String shortName, String longName, String category, String swFeatureRef, String[] swUnitRef, String[][] swCsHistory) {
        this.shortName = shortName;
        this.longName = longName;
        this.category = category;
        this.swFeatureRef = swFeatureRef;
        this.swUnitRef = swUnitRef;
        this.swCsHistory = swCsHistory;
    }

    public final String getShortName() {
        return shortName;
    }

    public final String getLongName() {
        return longName;
    }

    public final String getCategory() {
        return category;
    }

    public final String getSwFeatureRef() {
        return swFeatureRef;
    }

    public final String[] getSwUnitRef() {
        return swUnitRef;
    }

    public final String[][] getSwCsHistory() {
        return swCsHistory;
    }

    public final int getLastScore() {
        return swCsHistory.length > 0 ? maturite.get(swCsHistory[swCsHistory.length - 1][2].toLowerCase()) : 0;
    }

    @Override
    public boolean equals(Object paramObject) {
        return this.getShortName().equals(((Variable) paramObject).getShortName());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Nom : " + this.shortName + "\n");
        sb.append("Description : " + this.longName + "\n");
        sb.append("Fonction : " + this.swFeatureRef + "\n");
        return sb.toString();
    }

    public abstract double getChecksum();

    public abstract String toMFormat(boolean transpose);

    public abstract String[][] getValues();

    public final JComponent showValues() {

        if (tableView == null) {
            tableView = new TableView(new TableModelView());
        }

        tableView.getModel().setData(getValues());
        tableView.getDefaultRenderer(Object.class).colorMap(this);
        TableView.adjustCells(tableView);

        if (panel == null) {
            panel = new JPanel(new GridLayout(1, 1));
            panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            panel.add(tableView);
        }

        return panel;
    }

    public final void copyTxtToClipboard() {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new TxtTransfert(this.toString()), null);
    }

    private final class TxtTransfert implements Transferable {
        private final String s;

        public TxtTransfert(String s) {
            this.s = s;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return s;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.stringFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.stringFlavor.equals(flavor);
        }

    }

    public final void copyImgToClipboard() {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = img.createGraphics();
        panel.printAll(g);
        g.dispose();
        clipboard.setContents(new ImgTransfert(img), null);

    }

    private final class ImgTransfert implements Transferable {
        private final Image img;

        public ImgTransfert(Image img) {
            this.img = img;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return img;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

    }
}
