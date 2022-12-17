package sk.typre.astrocalc;

import javax.swing.*;
import java.awt.*;

public class UserInterface extends JFrame {
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static AstroPanel astroPanel;
    private static ControlPanel controlPanel;


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        SwingUtilities.invokeLater(UserInterface::new);
    }

    public UserInterface() {
        astroPanel = new AstroPanel(800,800);
        controlPanel = new ControlPanel(astroPanel);
        createAndShowGUI();
        astroPanel.run();
    }

    private void createAndShowGUI() {
        setTitle("AstroCalc v1.0");
        setIconImage(ResourcesManager.getImage("AstroCalcImage.png"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        Container container = getContentPane();
        container.setPreferredSize(new Dimension(1200, 800));
        addComponentsToContainer(container);
        pack();
        setLocation(screenSize.width / 2 - (getSize()).width / 2, screenSize.height / 2 - (getSize()).height / 2);
        setVisible(true);
    }

    private void addComponentsToContainer(Container container) {
        container.setLayout(new GridBagLayout());
        addGbcComponent(astroPanel, container, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 10);
        addGbcComponent(controlPanel, container, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0,10);
    }


    public static void addGbcComponent(Component component, Container container, int fill, int top, int left, int bottom, int right, int ipadx, int ipady, int gridwidth, int gridheight, double weightx, double weighty, int gridx, int gridy, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(top, left, bottom, right);
        gbc.fill = fill;
        gbc.insets = insets;
        gbc.ipadx = ipadx;
        gbc.ipady = ipady;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.anchor = anchor;
        container.add(component, gbc);
    }
}
