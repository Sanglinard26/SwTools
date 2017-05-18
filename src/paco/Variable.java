package paco;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;

public abstract class Variable {

    private final String longName;
    private final String shortName;
    private final String category;
    private final String swFeatureRef;
    private final String[] swUnitRef;
    private final String[][] swCsHistory;

    private static final HashMap<String, Integer> maturite = new HashMap<String, Integer>(6);

    public Variable(String shortName, String longName, String category, String swFeatureRef, String[] swUnitRef, String[][] swCsHistory) {
        this.shortName = shortName;
        this.longName = longName;
        this.category = category;
        this.swFeatureRef = swFeatureRef;
        this.swUnitRef = swUnitRef;
        this.swCsHistory = swCsHistory;

        maturite.put("---", 0);
        maturite.put("changed", 0);
        maturite.put("prelimcalibrated", 25);
        maturite.put("calibrated", 50);
        maturite.put("checked", 75);
        maturite.put("completed", 100);
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
        if (!(swCsHistory.length < 1))
            return maturite.get(swCsHistory[swCsHistory.length - 1][2].toLowerCase());
        return 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Nom : " + this.shortName + "\n");
        sb.append("Description : " + this.longName + "\n");
        sb.append("Fonction : " + this.swFeatureRef + "\n");
        return sb.toString();
    }

    public abstract void initVariable(Boolean colored);

    public abstract Component showView(Boolean colored);

    public abstract void copyToClipboard();

    public void copyTxtToClipboard(String s) {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new TxtTransfert(s), null);
    }

    final class TxtTransfert implements Transferable {
        private String s;

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
}
