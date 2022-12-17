package sk.typre.astrocalc;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;

public class ProfileLoader {
    private final File astroCfg = new File(new File("AstroCalc.cfg").getAbsolutePath());

    private boolean checkCfgFile(File file) {
        try {
            if (!file.exists()) {
                return file.createNewFile();
            } else {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Util.showErrorMessage("Profile loader error.");
            System.exit(0);
        }
        return false;
    }

    public void saveProfiles(ComboBoxModel<SettingsProfile> model) {
        if (checkCfgFile(astroCfg)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(astroCfg, false))) {
                oos.writeObject(model);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
                Util.showErrorMessage("Profile loader error.");
                System.exit(0);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public ComboBoxModel<SettingsProfile> loadProfiles() {
        ComboBoxModel<SettingsProfile> model = null;
        if (astroCfg.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(astroCfg.toPath()))) {
                model = ((ComboBoxModel<SettingsProfile>) ois.readObject());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Util.showErrorMessage("Corrupted profile file.");
                System.exit(0);
            }

            if (model.getElementAt(0).getVersion() == 1) {
                return model;
            } else {
                Util.showErrorMessage("Incorrect profile version.");
                System.exit(0);
            }
        }
        return null;
    }
}
