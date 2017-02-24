package paco;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import tools.Preference;
import tools.Utilitaire;

public class Curve extends Variable {

    private String[][] values;
    private JPanel panel;
    private String[] xValues;
    private String[] zValues;

    public Curve(String shortName, String category, String swFeatureRef, String[][] swCsHistory, String[][] values) {
        super(shortName, category, swFeatureRef, swCsHistory);
        this.values = values;

        xValues = new String[values[0].length];
        for (int i = 0; i < xValues.length; i++) {
            xValues[i] = values[0][i];
        }

        zValues = new String[values[0].length];
        for (int i = 0; i < zValues.length; i++) {
            zValues[i] = values[1][i];
        }

    }

    public String[][] getValues() {
        return this.values;
    }

    public String getValue(int col, int row) {
        return Utilitaire.cutNumber(values[col][row]);
    }

    public int getDimX() {
        return values[0].length;
    }

    @Override
    public Component showView() {
        initVariable();
        return panel;
    }

    @Override
    public void exportToExcel() throws RowsExceededException, WriteException, IOException {
        WritableWorkbook workbook = Workbook.createWorkbook(new File("D:/" + this.getShortName() + ".xls"));
        WritableSheet sheet = workbook.createSheet("Export", 0);
        WritableFont arial10Bold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        WritableCellFormat arial10format = new WritableCellFormat(arial10Bold);

        sheet.addCell(new Label(0, 0, this.getShortName(), arial10format));
        for (int x = 0; x < getDimX(); x++) {
            for (int y = 0; y < 2; y++) {
                sheet.addCell(new Label(x, y + 1, this.getValue(y, x)));
            }
        }

        workbook.write();
        workbook.close();

    }

    @Override
    public void initVariable() {
        panel = new JPanel(new GridLayout(2, getDimX(), 1, 1));
        panel.setLayout(new GridLayout(2, getDimX(), 1, 1));
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        panel.addMouseListener(this);
        JLabel[] valueViewX = new JLabel[getDimX()];
        JLabel[] valueViewY = new JLabel[getDimX()];
        for (int i = 0; i < valueViewX.length; i++) {
            valueViewX[i] = new JLabel(getValue(0, i));
            panel.add(valueViewX[i]);
            valueViewX[i].setFont(new Font(null, Font.BOLD, valueViewX[i].getFont().getSize()));
            valueViewX[i].setOpaque(true);
            valueViewX[i].setBackground(Color.LIGHT_GRAY);
            valueViewX[i].setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            valueViewX[i].setHorizontalAlignment(SwingConstants.CENTER);
        }
        for (int i = 0; i < valueViewY.length; i++) {
            valueViewY[i] = new JLabel(getValue(1, i));
            panel.add(valueViewY[i]);
            valueViewY[i].setOpaque(true);
            valueViewY[i].setBackground(Color.LIGHT_GRAY);
            valueViewY[i].setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            valueViewY[i].setHorizontalAlignment(SwingConstants.CENTER);

        }

    }

    @Override
    public void exportToPicture() {

        JFileChooser fileChooser = new JFileChooser(Preference.getPreference(Preference.KEY_RESULT_LAB));
        fileChooser.setDialogTitle("Enregistement de l'image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image (*.jpg)", "jpg"));
        fileChooser.setSelectedFile(new File(".jpg"));
        int rep = fileChooser.showSaveDialog(null);

        if (rep == JFileChooser.APPROVE_OPTION) {
            File img = fileChooser.getSelectedFile();
            BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            panel.printAll(g);
            g.dispose();
            try {
                String pathImg = img.getPath();
                String extension = "";
                if (Utilitaire.getExtension(img) == null) {
                    extension = ".jpg";
                } else {
                    if (!Utilitaire.getExtension(img).equals(Utilitaire.jpg)) {
                        pathImg = img.getPath().substring(0, img.getPath().lastIndexOf("."));
                        extension = ".jpg";
                    }
                }
                ImageIO.write(image, "jpg", new File(pathImg + extension));
            } catch (IOException exp) {
                System.out.println(exp);
            }
        }

    }

    public void copyToClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        panel.printAll(g);
        g.dispose();
        clipboard.setContents(new ImgTransfert(img), null);

    }

    class ImgTransfert implements Transferable {
        private Image img;

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
