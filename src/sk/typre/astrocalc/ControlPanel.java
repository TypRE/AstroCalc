package sk.typre.astrocalc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ControlPanel extends JPanel {

    private final AstroCalc astroCalc;
    private final AstroPanel astroPanel;
    private static final Font ARIAL = new Font("Arial", Font.BOLD, 18);
    private static final Color PANEL_COLOR = new Color(200, 200, 200);
    private static final Color ERROR_COLOR = new Color(255, 180, 180);
    private static final Color WARN_COLOR = new Color(255, 200, 120);
    private static final ProfileLoader profileLoader = new ProfileLoader();
    private ComboBoxModel<SettingsProfile> model;
    private boolean blockListener = false;
    private int[] latitude;
    private int[] longitude;
    private int[] obliquity;
    private long startMillis;
    private long dayDurationMillis;
    private long yearDurationMillis;
    private boolean planetClockwiseRotation;
    private boolean orbitClockwiseRotation;
    private final JPanel animPan;
    private final JPanel latPan;
    private final JPanel longPan;
    private final JPanel oblPan;
    private final JPanel planetPan;
    private final JPanel settingPan;

    public ControlPanel(AstroPanel astroPanel) {
        this.astroCalc = astroPanel.getAstroCalc();
        this.astroPanel = astroPanel;
        animPan = getAnimationPanel();
        latPan = getSliderPanel(new String[]{"Latitude", "North", "South"}, 90);
        longPan = getSliderPanel(new String[]{"Longitude", "West", "East"}, 180);
        oblPan = getSliderPanel(new String[]{"Obliquity", "Normal", "Negative"}, 90);
        planetPan = getSettingsPanel();
        settingPan = getProfilesPanel();
        model = loadProfiles();
        updateControlPanel(model.getElementAt(0));
        updateComboBox(model);

        setPreferredSize(new Dimension(400, 800));
        setSize(new Dimension(400, 800));
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createBevelBorder(1));
        addComponentsToPanel();
    }

    private void updateCurrentValues(SettingsProfile profile) {
        latitude = profile.getLatitude();
        longitude = profile.getLongitude();
        obliquity = profile.getObliquity();
        startMillis = profile.getStartMillis();
        dayDurationMillis = profile.getDayDurationMillis();
        yearDurationMillis = profile.getYearDurationMillis();
        planetClockwiseRotation = profile.isPlanetClockwiseRotation();
        orbitClockwiseRotation = profile.isOrbitClockwiseRotation();
    }

    private ComboBoxModel<SettingsProfile> loadProfiles() {
        blockListener = true;
        model = profileLoader.loadProfiles();
        if (model == null) {
            SettingsProfile def = getDefaultProfile();
            model = getSettingsCombo().getModel();
            ((MutableComboBoxModel<SettingsProfile>) model).addElement(def);
            return model;
        }
        blockListener = false;
        return model;
    }

    private SettingsProfile getDefaultProfile() {
        return new SettingsProfile(1, true);
    }

    private void updateControlPanel(SettingsProfile currentProfile) {
        blockListener = true;
        updateCurrentValues(currentProfile);
        initLatitudePanel(currentProfile);
        initLongitudePanel(currentProfile);
        initObliquityPanel(currentProfile);
        initPlanetPanel(currentProfile);
        blockListener = false;
    }

    private void updateComboBox(ComboBoxModel<SettingsProfile> model) {
        blockListener = true;
        JComboBox<SettingsProfile> combo = getSettingsCombo();
        combo.setModel(model);
        combo.setSelectedIndex(0);
        blockListener = false;
    }

    private int getLastIndex(ComboBoxModel<SettingsProfile> model) {
        int index = 0;
        int next;
        int size = model.getSize();
        for (int i = 0; i < size; i++) {
            next = model.getElementAt(i).getProfileId();
            if (next > index) {
                index = next;
            }
        }
        return index;
    }

    private void addProfile() {
        blockListener = true;
        JComboBox<SettingsProfile> combo = getSettingsCombo();
        SettingsProfile newProfile = new SettingsProfile(getLastIndex(model) + 1, false);
        updateProfile(newProfile);
        combo.addItem(newProfile);
        combo.setSelectedIndex(model.getSize() - 1);
        profileLoader.saveProfiles(combo.getModel());
        blockListener = false;
    }

    private void saveProfile() {
        blockListener = true;
        JComboBox<SettingsProfile> combo = getSettingsCombo();
        SettingsProfile selected = (SettingsProfile) combo.getSelectedItem();
        assert selected != null;
        updateProfile(selected);
        profileLoader.saveProfiles(combo.getModel());
        blockListener = false;
    }


    private void updateProfile(SettingsProfile selected) {
        selected.setDefault(false);
        selected.setLatitude(latitude);
        selected.setLongitude(longitude);
        selected.setObliquity(obliquity);
        selected.setStartMillis(startMillis);
        selected.setDayDurationMillis(dayDurationMillis);
        selected.setYearDurationMillis(yearDurationMillis);
        selected.setPlanetClockwiseRotation(planetClockwiseRotation);
        selected.setOrbitClockwiseRotation(orbitClockwiseRotation);
    }

    private void removeProfile() {
        blockListener = true;
        JComboBox<SettingsProfile> combo = getSettingsCombo();

        int index = combo.getSelectedIndex();
        if (index == 0) {
            combo.removeItemAt(0);
            if (combo.getItemCount() == 0) {
                SettingsProfile defaultProfile = getDefaultProfile();
                combo.addItem(defaultProfile);
                updateControlPanel(defaultProfile);
            } else {
                updateControlPanel(combo.getItemAt(0));
            }
        } else {
            combo.removeItemAt(index);
            updateControlPanel(combo.getItemAt(index - 1));
        }
        profileLoader.saveProfiles(combo.getModel());
        blockListener = false;
    }

    @SuppressWarnings("unchecked")
    private JComboBox<SettingsProfile> getSettingsCombo() {
        return ((JComboBox<SettingsProfile>) settingPan.getComponents()[0]);
    }

    private void selectProfileAction(ItemEvent e) {
        if (!blockListener) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                SettingsProfile selectedProfile = (SettingsProfile) e.getItem();
                updateControlPanel(selectedProfile);

            }
        }
    }

    private void addComponentsToPanel() {
        UserInterface.addGbcComponent(animPan, this, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 10);
        UserInterface.addGbcComponent(latPan, this, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 1, 10);
        UserInterface.addGbcComponent(longPan, this, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 2, 10);
        UserInterface.addGbcComponent(oblPan, this, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 3, 10);
        UserInterface.addGbcComponent(planetPan, this, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 4, 10);
        UserInterface.addGbcComponent(settingPan, this, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 5, 10);
    }

    private JPanel getSliderPanel(String[] names, int maxSpinDeg) {
        JPanel panel = getJPanel();
        JSlider slider = new JSlider();
        JComboBox<String> combo = new JComboBox<>();

        JSpinner spinDeg = new JSpinner(new SpinnerNumberModel(0, -1, maxSpinDeg, 1));
        JSpinner spinMin = new JSpinner(new SpinnerNumberModel(0, -1, 60, 1));
        JSpinner spinSec = new JSpinner(new SpinnerNumberModel(0, -1, 60, 1));

        spinDeg.setPreferredSize(new Dimension(61, 26));
        spinMin.setPreferredSize(new Dimension(61, 26));
        spinSec.setPreferredSize(new Dimension(61, 26));

        JLabel labDeg = new JLabel("°");
        JLabel labMin = new JLabel("'");
        JLabel labSec = new JLabel("\"");

        labDeg.setFont(ARIAL);
        labMin.setFont(ARIAL);
        labSec.setFont(ARIAL);

        panel.setBorder(BorderFactory.createTitledBorder(names[0]));
        panel.setLayout(new GridBagLayout());

        combo.setFocusable(false);
        combo.addItem(names[1]);
        combo.addItem(names[2]);

        slider.setPaintTicks(true);
        slider.setFocusable(false);
        slider.setBackground(PANEL_COLOR);
        slider.setMaximum((3600 * maxSpinDeg));
        slider.setMinimum(-(3600 * maxSpinDeg));

        spinDeg.addChangeListener(e -> updateAction(e.getSource(), spinDeg, spinMin, spinSec, combo, slider, names[0]));
        spinMin.addChangeListener(e -> updateAction(e.getSource(), spinDeg, spinMin, spinSec, combo, slider, names[0]));
        spinSec.addChangeListener(e -> updateAction(e.getSource(), spinDeg, spinMin, spinSec, combo, slider, names[0]));
        combo.addActionListener(e -> updateAction(e.getSource(), spinDeg, spinMin, spinSec, combo, slider, names[0]));
        slider.addChangeListener(e -> updateAction(e.getSource(), spinDeg, spinMin, spinSec, combo, slider, names[0]));

        UserInterface.addGbcComponent(spinDeg, panel, 2, 0, 20, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 10);
        UserInterface.addGbcComponent(labDeg, panel, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 10);
        UserInterface.addGbcComponent(spinMin, panel, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 2, 0, 10);
        UserInterface.addGbcComponent(labMin, panel, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 3, 0, 10);
        UserInterface.addGbcComponent(spinSec, panel, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 4, 0, 10);
        UserInterface.addGbcComponent(labSec, panel, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 5, 0, 10);
        UserInterface.addGbcComponent(combo, panel, 2, 0, 0, 0, 20, 0, 0, 1, 1, 0, 0, 6, 0, 10);
        UserInterface.addGbcComponent(slider, panel, 2, 5, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 10);

        return panel;
    }

    private JPanel getSettingsPanel() {
        JPanel settPan = getJPanel();
        JTextField startMillisField = new JTextField();
        JTextField startDateField = new JTextField();
        JTextField dayDurationField = new JTextField();
        JTextField yearDurationField = new JTextField();
        JSeparator jSeparator = new JSeparator();
        JComboBox<String> planetComboBox = new JComboBox<>();
        JComboBox<String> orbitComboBox = new JComboBox<>();
        JButton nowButton = new JButton("Now");

        JLabel startMillisLabel = new JLabel("Start Millis: ");
        JLabel startDateLabel = new JLabel("Start Date:");
        JLabel dayDurationLabel = new JLabel("Day Duration: ");
        JLabel yearDurationLabel = new JLabel("Year Duration: ");
        JLabel planetRotation = new JLabel("Planet Rotation: ");
        JLabel orbitRotation = new JLabel("Orbit Rotation: ");
        JLabel millis_date = new JLabel("ms");
        JLabel millis_day = new JLabel("ms");
        JLabel millis_year = new JLabel("ms");

        settPan.setBorder(BorderFactory.createTitledBorder("Settings"));
        settPan.setLayout(new GridBagLayout());

        nowButton.setOpaque(false);
        nowButton.setFocusable(false);
        orbitComboBox.setFocusable(false);
        planetComboBox.setFocusable(false);

        planetComboBox.addItem("Clockwise");
        planetComboBox.addItem("Counter Clockwise");
        orbitComboBox.addItem("Clockwise");
        orbitComboBox.addItem("Counter Clockwise");

        nowButton.addActionListener(e -> startMillisField.setText(String.valueOf(System.currentTimeMillis())));
        startMillisField.getDocument().addDocumentListener((DocumentInterface) e -> updateTimeAction(startMillisField, startDateField, "Millis"));
        startDateField.getDocument().addDocumentListener((DocumentInterface) e -> updateTimeAction(startMillisField, startDateField, "Date"));
        planetComboBox.addActionListener(e -> updateRotationDirection(planetComboBox, orbitComboBox, "Planet"));
        orbitComboBox.addActionListener(e -> updateRotationDirection(planetComboBox, orbitComboBox, "Orbit"));
        dayDurationField.getDocument().addDocumentListener((DocumentInterface) e -> updateDurations(dayDurationField, yearDurationField, "Day"));
        yearDurationField.getDocument().addDocumentListener((DocumentInterface) e -> updateDurations(dayDurationField, yearDurationField, "Year"));

        UserInterface.addGbcComponent(startMillisLabel, settPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 10);
        UserInterface.addGbcComponent(nowButton, settPan, 2, 0, 0, 0, 5, 0, 0, 1, 1, 0, 1, 1, 0, 10);
        UserInterface.addGbcComponent(startMillisField, settPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 0, 10);
        UserInterface.addGbcComponent(millis_date, settPan, 2, 0, 5, 0, 0, 0, 0, 1, 1, 0, 1, 3, 0, 10);
        UserInterface.addGbcComponent(startDateLabel, settPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 1, 10);
        UserInterface.addGbcComponent(startDateField, settPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 1, 10);
        UserInterface.addGbcComponent(jSeparator, settPan, 2, 5, 0, 5, 0, 0, 0, 0, 1, 1, 1, 0, 2, 10);
        UserInterface.addGbcComponent(dayDurationLabel, settPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 3, 10);
        UserInterface.addGbcComponent(dayDurationField, settPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 3, 10);
        UserInterface.addGbcComponent(millis_day, settPan, 2, 0, 5, 0, 0, 0, 0, 1, 1, 0, 1, 3, 3, 10);
        UserInterface.addGbcComponent(yearDurationLabel, settPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 4, 10);
        UserInterface.addGbcComponent(yearDurationField, settPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 4, 10);
        UserInterface.addGbcComponent(millis_year, settPan, 2, 0, 5, 0, 0, 0, 0, 1, 1, 0, 1, 3, 4, 10);
        UserInterface.addGbcComponent(planetRotation, settPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 5, 10);
        UserInterface.addGbcComponent(planetComboBox, settPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 5, 10);
        UserInterface.addGbcComponent(orbitRotation, settPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 6, 10);
        UserInterface.addGbcComponent(orbitComboBox, settPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 6, 10);

        return settPan;
    }

    private JPanel getAnimationPanel() {
        JPanel animPan = getJPanel();

        JButton next = new JButton(">");
        JButton prev = new JButton("<");
        JButton rew = new JButton("<<");
        JButton fwd = new JButton(">>");

        JSeparator separator_top = new JSeparator();

        JButton maxRising = new JButton("Max Rising");
        JButton maxFalling = new JButton("Max Falling");
        JButton srs = new JButton("Sun Rise");
        JButton noon = new JButton("Noon");
        JButton midnight = new JButton("Midnight");
        JButton sst = new JButton("Sun Set");

        animPan.setBorder(BorderFactory.createTitledBorder("Animation"));
        animPan.setLayout(new GridBagLayout());

        JComboBox<String> stepBox = new JComboBox<>();
        JComboBox<String> mulBox = new JComboBox<>();

        stepBox.addItem("Milliseconds");
        stepBox.addItem("Seconds");
        stepBox.addItem("Minutes");
        stepBox.addItem("Hours");
        stepBox.addItem("Days");
        stepBox.addItem("Sidereal Days");
        stepBox.addItem("Years");

        for (int i = 0; i <= 16; i++) {
            mulBox.addItem((0x01 << i) + "x");
        }

        next.addActionListener(e -> {
            astroCalc.getTimeManager().timeStep(stepBox.getSelectedIndex(), 0x01 << mulBox.getSelectedIndex() , true);
            astroPanel.repaint();
        });
        prev.addActionListener(e -> {
            astroCalc.getTimeManager().timeStep(stepBox.getSelectedIndex(), 0x01 << mulBox.getSelectedIndex(), false);
            astroPanel.repaint();
        });

        fwd.addActionListener(e -> {
            astroCalc.getTimeManager().toggleAnimation(stepBox.getSelectedIndex(), 0x01 << mulBox.getSelectedIndex(), true);
            astroPanel.repaint();
        });
        rew.addActionListener(e -> {
            astroCalc.getTimeManager().toggleAnimation(stepBox.getSelectedIndex(), 0x01 << mulBox.getSelectedIndex(), false);
            astroPanel.repaint();
        });

        srs.addActionListener(e -> astroCalc.getTimeManager().setSunRiseTime());
        maxRising.addActionListener(e -> astroCalc.getTimeManager().setMaxRisingTime());
        noon.addActionListener(e -> astroCalc.getTimeManager().setSolarNoonTime());
        midnight.addActionListener(e -> astroCalc.getTimeManager().setSolarMidnightTime());
        sst.addActionListener(e -> astroCalc.getTimeManager().setSunSetTime());
        maxFalling.addActionListener(e -> astroCalc.getTimeManager().setMaxFallingTime());

        UserInterface.addGbcComponent(stepBox, animPan, 2, 0, 0, 0, 0, 0, 0, 2, 1, 1, 1, 0, 0, 10);
        UserInterface.addGbcComponent(mulBox, animPan, 2, 0, 0, 0, 0, 0, 0, 2, 1, 1, 1, 2, 0, 10);
        UserInterface.addGbcComponent(rew, animPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 1, 10);
        UserInterface.addGbcComponent(prev, animPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 10);
        UserInterface.addGbcComponent(next, animPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 1, 10);
        UserInterface.addGbcComponent(fwd, animPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 3, 1, 10);
        UserInterface.addGbcComponent(separator_top, animPan, 2, 5, 0, 5, 0, 0, 0, 0, 1, 1, 1, 0, 2, 10);

        UserInterface.addGbcComponent(maxRising, animPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 4, 10);
        UserInterface.addGbcComponent(srs, animPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 3, 10);
        UserInterface.addGbcComponent(noon, animPan, 2, 0, 0, 0, 0, 0, 0, 2, 1, 0, 1, 1, 3, 10);
        UserInterface.addGbcComponent(midnight, animPan, 2, 0, 0, 0, 0, 0, 0, 2, 1, 0, 1, 1, 4, 10);
        UserInterface.addGbcComponent(sst, animPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 3, 3, 10);
        UserInterface.addGbcComponent(maxFalling, animPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 3, 4, 10);
        return animPan;
    }

    private JPanel getProfilesPanel() {
        JPanel profPan = getJPanel();
        profPan.setLayout(new GridBagLayout());
        profPan.setBorder(BorderFactory.createTitledBorder("Profiles"));

        JComboBox<SettingsProfile> comboBox = new JComboBox<>();
        comboBox.setPreferredSize(new Dimension(33, 26));

        JButton add = new JButton("Add");
        JButton save = new JButton("Save");
        JButton remove = new JButton("Remove");

        comboBox.addItemListener(this::selectProfileAction);
        add.addActionListener(e -> addProfile());
        save.addActionListener(e -> saveProfile());
        remove.addActionListener(e -> removeProfile());

        UserInterface.addGbcComponent(comboBox, profPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 5, 1, 0, 0, 10);
        UserInterface.addGbcComponent(add, profPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 10);
        UserInterface.addGbcComponent(save, profPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 0, 10);
        UserInterface.addGbcComponent(remove, profPan, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 3, 0, 10);

        return profPan;
    }

    private JPanel getJPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(PANEL_COLOR);
                g.fillRoundRect(4, 17, getWidth() - 9, getHeight() - 24, 8, 8);
                g.drawRoundRect(4, 17, getWidth() - 9, getHeight() - 24, 8, 8);
            }
        };
    }

    private void updateDurations(JTextField dayField, JTextField yearField, String source) {
        if (!blockListener) {

            long dayMillis, yearMillis;
            String dayVal = dayField.getText();
            String yearVal = yearField.getText();

            if (Util.checkNumericString(dayVal)) {
                dayMillis = (long) new BigDecimal(dayVal).doubleValue();
            } else {
                dayField.setBackground(ERROR_COLOR);
                return;
            }

            if (Util.checkNumericString(yearVal)) {
                yearMillis = (long) new BigDecimal(yearVal).doubleValue();
            } else {
                yearField.setBackground(ERROR_COLOR);
                return;
            }

            if (source.equals("Day")) {
                if (dayMillis > 0 && dayMillis < yearMillis) {
                    astroCalc.setDayDurationMillis(dayMillis, true);
                    this.dayDurationMillis = dayMillis;
                    dayField.setBackground(Color.white);
                } else {
                    if (dayMillis == yearMillis) {
                        dayField.setBackground(WARN_COLOR);
                        astroCalc.setDayDurationMillis(dayMillis, true);
                        this.dayDurationMillis = dayMillis;
                    } else {
                        dayField.setBackground(ERROR_COLOR);
                    }
                }
            } else if (source.equals("Year")) {
                if (dayMillis > 0 && dayMillis < yearMillis) {
                    astroCalc.setYearDurationMillis(yearMillis, true);
                    this.yearDurationMillis = yearMillis;
                    yearField.setBackground(Color.white);
                } else {
                    if (dayMillis == yearMillis) {
                        yearField.setBackground(WARN_COLOR);
                        astroCalc.setYearDurationMillis(yearMillis, true);
                        this.yearDurationMillis = yearMillis;
                    } else {
                        yearField.setBackground(ERROR_COLOR);
                    }
                }
            }

        }
    }

    private void updateRotationDirection(JComboBox<String> planet, JComboBox<String> orbit, String source) {
        if (!blockListener) {
            if (source.equals("Planet")) {
                boolean cw = planet.getSelectedIndex() == 0;
                astroCalc.setPlanetClockwiseRotation(cw, true);
                this.planetClockwiseRotation = cw;
            } else if (source.equals("Orbit")) {
                boolean cw = orbit.getSelectedIndex() == 0;
                astroCalc.setOrbitClockwiseRotation(cw, true);
                this.orbitClockwiseRotation = cw;
            }
        }
    }

    private void updateTimeAction(JTextField millTf, JTextField dateTf, String source) {
        if (!blockListener) {
            blockListener = true;
            long millis;
            String millStr = millTf.getText();
            String dateStr = dateTf.getText();

            if (source.equals("Millis")) {

                if (Util.checkNumericString(millStr)) {
                    millis = (long) new BigDecimal(millStr).doubleValue();

                    if (millis <= System.currentTimeMillis()) {
                        dateTf.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(millis)));
                        astroCalc.setStartMillis(millis);
                        this.startMillis = millis;
                        millTf.setBackground(Color.white);
                        dateTf.setBackground(Color.white);
                    } else {
                        millTf.setBackground(ERROR_COLOR);
                        blockListener = false;
                        return;
                    }
                } else {
                    millTf.setBackground(ERROR_COLOR);
                    blockListener = false;
                    return;
                }

            } else if (source.equals("Date")) {
                try {
                    millis = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(dateStr).getTime();
                    millTf.setText(String.valueOf(millis));
                    if (millis <= System.currentTimeMillis()) {
                        astroCalc.setStartMillis(millis);
                        this.startMillis = millis;
                        dateTf.setBackground(Color.white);
                        millTf.setBackground(Color.white);
                    } else {
                        dateTf.setBackground(ERROR_COLOR);
                        blockListener = false;
                        return;
                    }
                } catch (ParseException e) {
                    dateTf.setBackground(ERROR_COLOR);
                    blockListener = false;
                    return;
                }
            }
            blockListener = false;
        }
    }

    private void updateAction(Object source, JSpinner spinDeg, JSpinner spinMin, JSpinner spinSec, JComboBox<String> jComboBox, JSlider jSlider, String name) {
        if (!blockListener) {
            blockListener = true;
            int deg = 0, min = 0, sec = 0, sliderValue = 0;
            if (source instanceof JSpinner) {
                deg = (int) (spinDeg.getValue());
                min = (int) (spinMin.getValue());
                sec = (int) (spinSec.getValue());

                if (sec == -1) {
                    sec = 59;
                    min -= 1;
                } else if (sec == 60) {
                    min += 1;
                    sec = 0;
                }
                if (min == -1) {
                    min = 59;
                    deg -= 1;
                } else if (min == 60) {
                    deg += 1;
                    min = 0;
                }
                if (deg == -1) {
                    deg = 0;
                    min = 0;
                    sec = 0;
                } else if (deg == (name.equals("Longitude") ? 180 : 90)) {
                    min = 0;
                    sec = 0;
                }

                spinDeg.setValue(deg);
                spinMin.setValue(min);
                spinSec.setValue(sec);
                sliderValue = (deg * 3600) + (min * 60) + sec;
                if (jComboBox.getSelectedIndex() == 1) {
                    deg = -deg;
                    min = -min;
                    sec = -sec;
                    sliderValue = -sliderValue;
                }
            } else if (source instanceof JSlider) {
                sliderValue = jSlider.getValue();
                int absValue = Math.abs(sliderValue);
                deg = absValue / 3600;
                min = (absValue - deg * 3600) / 60;
                sec = absValue - ((deg * 3600) + (min * 60));
                spinDeg.setValue(deg);
                spinMin.setValue(min);
                spinSec.setValue(sec);
                if (jSlider.getValue() < 0) {
                    jComboBox.setSelectedIndex(1);
                    deg = -deg;
                    min = -min;
                    sec = -sec;
                } else {
                    jComboBox.setSelectedIndex(0);
                }
            } else if (source instanceof JComboBox) {
                deg = (int) (spinDeg.getValue());
                min = (int) (spinMin.getValue());
                sec = (int) (spinSec.getValue());
                sliderValue = (deg * 3600) + (min * 60) + sec;
                if (jComboBox.getSelectedIndex() == 1) {
                    deg = -deg;
                    min = -min;
                    sec = -sec;
                    sliderValue = -sliderValue;
                }
            }
            switch (name) {
                case "Latitude":
                    jSlider.setValue(sliderValue);
                    latitude = new int[]{deg, min, sec};
                    astroCalc.setLatitude(latitude, true);
                    break;
                case "Longitude":
                    jSlider.setValue(sliderValue);
                    longitude = new int[]{deg, min, sec};
                    astroCalc.setLongitude(longitude, true);
                    break;
                case "Obliquity":
                    jSlider.setValue(sliderValue);
                    obliquity = new int[]{deg, min, sec};
                    astroCalc.setObliquity(obliquity, true);
                    break;
            }
            blockListener = false;
        }
    }

    private void initSliderPanel(int[] values, Component[] comp) {
        int deg = values[0];
        int min = values[1];
        int sec = values[2];
        boolean south = deg < 0 || min < 0 || sec < 0;

        ((JSpinner) comp[0]).setValue(Math.abs(deg));
        ((JSpinner) comp[2]).setValue(Math.abs(min));
        ((JSpinner) comp[4]).setValue(Math.abs(sec));
        ((JComboBox<?>) comp[6]).setSelectedIndex(south ? 1 : 0);
        ((JSlider) comp[7]).setValue((deg * 3600) + (min * 60) + sec);
    }

    private void initPlanetPanel(SettingsProfile profile) {
        Component[] comp = planetPan.getComponents();
        long startMillis = profile.getStartMillis();
        long dayDurationMillis = profile.getDayDurationMillis();
        long yearDurationMillis = profile.getYearDurationMillis();
        boolean pcr = profile.isPlanetClockwiseRotation();
        boolean ocr = profile.isOrbitClockwiseRotation();

        ((JTextField) comp[2]).setText(String.valueOf(startMillis));
        ((JTextField) comp[5]).setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(startMillis)));
        ((JTextField) comp[8]).setText(String.valueOf(dayDurationMillis));
        ((JTextField) comp[11]).setText(String.valueOf(yearDurationMillis));
        ((JComboBox<?>) comp[14]).setSelectedIndex(pcr ? 0 : 1);
        ((JComboBox<?>) comp[16]).setSelectedIndex(ocr ? 0 : 1);

        astroCalc.setStartMillis(startMillis);
        astroCalc.setDayDurationMillis(dayDurationMillis, false);
        astroCalc.setYearDurationMillis(yearDurationMillis, false);
        astroCalc.setPlanetClockwiseRotation(pcr, false);
        astroCalc.setOrbitClockwiseRotation(ocr, false);

    }

    private void initLatitudePanel(SettingsProfile profile) {
        int[] latitude = profile.getLatitude();
        initSliderPanel(latitude, latPan.getComponents());
        astroCalc.setLatitude(latitude, false);
    }

    private void initLongitudePanel(SettingsProfile profile) {
        int[] longitude = profile.getLongitude();
        initSliderPanel(longitude, longPan.getComponents());
        astroCalc.setLongitude(longitude, false);
    }

    private void initObliquityPanel(SettingsProfile profile) {
        int[] obliquity = profile.getObliquity();
        initSliderPanel(obliquity, oblPan.getComponents());
        astroCalc.setObliquity(obliquity, false);
    }

}
