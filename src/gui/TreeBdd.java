/*
 * Creation : 6 nov. 2017
 */
package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;

import bdd.Bdd;
import bdd.BddConnexion;
import bdd.TreeModelBdd;
import bean.XmlInfo;
import utils.Preference;

public final class TreeBdd extends JTree {

    private static final long serialVersionUID = 1L;

    private final TreeModelBdd treeModelBdd;
    private static final File pathDb = new File(Preference.getPreference(Preference.KEY_PATH_FOLDER_DB));

    private Connection bddConnexion = null;

    public TreeBdd(DefaultMutableTreeNode node) {

        treeModelBdd = new TreeModelBdd(node, pathDb);
        setModel(treeModelBdd);

        setShowsRootHandles(true);

        setCellRenderer(new MyTreeRenderer());

        addTreeWillExpandListener(new TreeWillExpandListener() {

            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                // Non utilise
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                int niveau = ((DefaultMutableTreeNode) event.getPath().getLastPathComponent()).getLevel();
                if (niveau == 1) {
                    if (bddConnexion != null) {
                        BddConnexion.close();
                        System.out.println("Base fermee");
                    } else {
                        System.out.println("Pas de base de connectee");
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (TreeBdd.this.getPathForLocation(e.getX(), e.getY()) != null) {
                    if (TreeBdd.this.getSelectionPath() != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent();
                        int niveau = node.getLevel();

                        if (niveau == 1 & node.getChildCount() < 1) {
                            if (e.getClickCount() > 1) {
                                BddConnexion.setDbPath(Preference.getPreference(Preference.KEY_PATH_FOLDER_DB) + "/" + node + "/" + node);
                                bddConnexion = BddConnexion.getInstance();
                                TreeBdd.this.getModel().populateFromBdd(bddConnexion, node);
                            }
                        }
                    }

                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() & TreeBdd.this.getSelectionPath() != null) {
                    if (TreeBdd.this.getSelectionPath() == TreeBdd.this.getPathForLocation(e.getX(), e.getY())) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        JMenuItem menuItem;

                        int niveau = ((DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent()).getLevel();

                        switch (niveau) {
                        case 0:
                            menuItem = new JMenuItem(new AbstractAction("Nouvelle BDD") {

                                private static final long serialVersionUID = 1L;

                                @Override
                                public void actionPerformed(ActionEvent e) {

                                    String BddName = JOptionPane.showInputDialog("Nom de la BDD :", "");

                                    if (BddName != null) {
                                        if (!BddName.equals("")) {

                                            // Creation d'un nouveau dossier pour la base
                                            try {
                                                Files.createDirectories(
                                                        Paths.get(Preference.getPreference(Preference.KEY_PATH_FOLDER_DB) + "/" + BddName));

                                                BddConnexion.setDbPath(
                                                        Preference.getPreference(Preference.KEY_PATH_FOLDER_DB) + "/" + BddName + "/" + BddName); // C:/Users/U354706/Desktop/Tmp/
                                                bddConnexion = BddConnexion.getInstance();

                                                if (bddConnexion != null) {
                                                    TreeBdd.this.getModel().insertNodeInto(new DefaultMutableTreeNode(new Bdd(BddName), true),
                                                            (DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent(),
                                                            ((DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent())
                                                                    .getChildCount());
                                                }
                                            } catch (IOException e1) {
                                                e1.printStackTrace();
                                            }

                                        }

                                    }

                                }
                            });
                            popupMenu.add(menuItem);
                            break;
                        case 1:

                            if (bddConnexion != null) {
                                menuItem = new JMenuItem(new AbstractAction("Nouveau dossier") {

                                    private static final long serialVersionUID = 1L;

                                    @Override
                                    public void actionPerformed(ActionEvent e) {

                                        String FolderName = JOptionPane.showInputDialog("Nom du dossier :", "");

                                        if (FolderName != null) {
                                            if (!FolderName.equals("")) {

                                                @SuppressWarnings("unchecked")
                                                Enumeration<DefaultMutableTreeNode> eNode = ((DefaultMutableTreeNode) TreeBdd.this.getSelectionPath()
                                                        .getLastPathComponent()).depthFirstEnumeration();
                                                while (eNode.hasMoreElements()) {
                                                    DefaultMutableTreeNode node = eNode.nextElement();
                                                    if (node.toString().equalsIgnoreCase(FolderName)) {
                                                        System.out.println("Le dossier existe deja");
                                                        return;
                                                    }
                                                }

                                                BddConnexion.setDbPath(Preference.getPreference(Preference.KEY_PATH_FOLDER_DB) + "/"
                                                        + TreeBdd.this.getSelectionPath().getLastPathComponent() + "/"
                                                        + TreeBdd.this.getSelectionPath().getLastPathComponent()); // C:/Users/U354706/Desktop/Tmp/
                                                bddConnexion = BddConnexion.getInstance();
                                                // Creation de la table dans la base
                                                BddConnexion.createTable(bddConnexion, FolderName);
                                                // Insertion d'un nouveau noeud dans l'arbre
                                                TreeBdd.this.getModel().insertNodeInto(new DefaultMutableTreeNode(FolderName.toLowerCase(), true),
                                                        (DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent(),
                                                        ((DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent())
                                                                .getChildCount());
                                                // Creation d'un nouveau dossier dans le repertoire de la base
                                                try {
                                                    Files.createDirectories(Paths.get(BddConnexion.getDbParentFolder() + "/" + FolderName));
                                                } catch (IOException e1) {
                                                    e1.printStackTrace();
                                                }

                                            }
                                        }
                                    }
                                });
                                popupMenu.add(menuItem);
                            }

                            menuItem = new JMenuItem(new AbstractAction("Supprimer BDD") {

                                private static final long serialVersionUID = 1L;

                                @Override
                                public void actionPerformed(ActionEvent e) {

                                    if (TreeBdd.this.getSelectionPath() != null) {
                                        File folderSup = new File(Preference.getPreference(Preference.KEY_PATH_FOLDER_DB) + "/"
                                                + TreeBdd.this.getSelectionPath().getLastPathComponent());

                                        if (folderSup.exists()) {
                                            System.out.println("Dossier à supprimer : " + folderSup);
                                            if (bddConnexion != null) {
                                                BddConnexion.close();
                                                bddConnexion = null;
                                            }

                                            if (folderSup.isDirectory()) {
                                                System.out.println("C'est un dossier");
                                                for (File f : folderSup.listFiles()) {
                                                    f.delete();
                                                }
                                                if (folderSup.delete()) {
                                                    TreeBdd.this.getModel().removeNodeFromParent(
                                                            (DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent());

                                                    JOptionPane.showMessageDialog(null, "Bdd supprimee");
                                                }
                                            }
                                        }
                                    }

                                }
                            });
                            popupMenu.add(menuItem);

                            break;
                        case 2:
                            menuItem = new JMenuItem(new AbstractAction("Supprimer dossier") {

                                private static final long serialVersionUID = 1L;

                                @Override
                                public void actionPerformed(ActionEvent e) {

                                    if (TreeBdd.this.getSelectionPath() != null) {

                                        File folderSup = new File(
                                                BddConnexion.getDbParentFolder() + "/" + TreeBdd.this.getSelectionPath().getLastPathComponent());

                                        if (folderSup.isDirectory()) {
                                            System.out.println("XmlFolder à supprimer : " + folderSup);
                                            for (File f : folderSup.listFiles()) {
                                                f.delete();
                                            }
                                            if (folderSup.delete()) {
                                                BddConnexion.setDbPath(Preference.getPreference(Preference.KEY_PATH_FOLDER_DB) + "/"
                                                        + ((DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent())
                                                                .getParent()
                                                        + "/" + ((DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent())
                                                                .getParent());
                                                bddConnexion = BddConnexion.getInstance();
                                                System.out.println(TreeBdd.this.getModel().removeXmlFolder(bddConnexion,
                                                        (DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent()));
                                                ((DefaultTreeModel) TreeBdd.this.getModel()).removeNodeFromParent(
                                                        (DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent());
                                            }
                                        }

                                    }

                                }
                            });
                            popupMenu.add(menuItem);
                            menuItem = new JMenuItem(new AbstractAction("Nouveau XML") {

                                private static final long serialVersionUID = 1L;

                                private XmlInfo xmlInfo;

                                @Override
                                public void actionPerformed(ActionEvent e) {

                                    xmlInfo = new XmlInfo();
                                    xmlInfo.setName("Xml");

                                    TreeBdd.this.getModel().insertNodeInto(new DefaultMutableTreeNode(xmlInfo, false),
                                            (DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent(),
                                            ((DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent()).getChildCount());

                                }
                            });
                            popupMenu.add(menuItem);
                            break;
                        case 3:
                            menuItem = new JMenuItem(new AbstractAction("Supprimer") {

                                private static final long serialVersionUID = 1L;

                                @Override
                                public void actionPerformed(ActionEvent e) {

                                    TreeBdd.this.getModel()
                                            .removeNodeFromParent((DefaultMutableTreeNode) TreeBdd.this.getSelectionPath().getLastPathComponent());
                                }
                            });
                            popupMenu.add(menuItem);
                            break;

                        }
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

        });
    }

    @Override
    public TreeModelBdd getModel() {
        return (TreeModelBdd) super.getModel();
    }

    private final class MyTreeRenderer extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 1L;

        private static final String ICON_DB = "/db_icon_16.png";
        private static final String ICON_XML = "/xml_icon_16.png";
        private static final String ICON_WP_FOLDER = "/wpfolder_icon_16.png";
        private static final String ICON_DB_FOLDER = "/dbfolder_icon_16.png";

        private JLabel component;
        private DefaultMutableTreeNode node;

        @Override
        public void setBackgroundSelectionColor(Color newColor) {
            super.setBackgroundSelectionColor(Color.BLACK);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {

            component = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            node = (DefaultMutableTreeNode) value;

            switch (node.getLevel()) {
            case 0:
                component.setIcon(new ImageIcon(getClass().getResource(ICON_DB_FOLDER)));
                break;
            case 1:
                component.setIcon(new ImageIcon(getClass().getResource(ICON_DB)));
                break;
            case 2:
                component.setIcon(new ImageIcon(getClass().getResource(ICON_WP_FOLDER)));
                break;
            case 3:
                component.setIcon(new ImageIcon(getClass().getResource(ICON_XML)));
                break;
            }

            if (hasFocus) {
                component.setBorder(new LineBorder(Color.RED, 1));
            } else {
                component.setBorder(null);
            }

            return component;
        }
    }

}
