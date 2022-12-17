package sk.typre.astrocalc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
public class ResourcesManager {

    private static final HashMap<String, Icon> iconMap = new HashMap<>();
    private static final HashMap<String, BufferedImage> imageMap = new HashMap<>();
    private static final String[] icons = {"InfoIcon.png", "TrackSunIcon.png", "FlipViewIcon.png", "CenterIcon.png", "LabelIcon.png", "OrreryIcon.png", "SkyIcon.png", "ReverseIcon.png", "ForwardIcon.png", "PlayIcon.png", "NowIcon.png"};
    private static final String[] images = {"AstroCalcImage.png", "ButtonImage.png"};

    static {
        loadImages();
    }

    private static void loadImages() {
        try {
            for (String icon : icons) {
                iconMap.put(icon, new ImageIcon(ImageIO.read(Objects.requireNonNull(ResourcesManager.class.getResourceAsStream("/sk/typre/astrocalc/resources/" + icon)))));
            }
            for (String image : images) {
                imageMap.put(image, ImageIO.read(Objects.requireNonNull(ResourcesManager.class.getResourceAsStream("/sk/typre/astrocalc/resources/" + image))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Icon getIcon(String key) {
        return iconMap.get(key);
    }

    public static BufferedImage getImage(String key) {
        return imageMap.get(key);
    }
}
