package sk.typre.astrocalc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

public class AstroPanel extends JComponent implements ActionListener {

    private final int WIDTH;
    private final int HEIGHT;

    private static String[] board;
    private static final Font font = new Font("Arial", Font.BOLD, 20);
    private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat df = new DecimalFormat("0.00", symbols);
    private static final BasicStroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
    private static final BasicStroke stroke1 = new BasicStroke();
    private static final BasicStroke stroke2 = new BasicStroke(2);

    private final AstroCalc astroCalc = new AstroCalc();
    private final TimeManager timeManager = astroCalc.getTimeManager();
    private final AstroGrid astroGrid = new AstroGrid();

    private HashMap<String, Double> astroVal = new HashMap<>();
    private static final int transparency = 230;
    private static final Color grayColor = new Color(32, 32, 32);
    private static final Color linesColor = new Color(132, 128, 32);
    private static final Color orangeColor = new Color(255, 160, 0, transparency);
    private static final Color lightOrangeColor = new Color(255, 179, 85, transparency);
    private static final Color lightGrayColor = new Color(60, 60, 60, transparency);
    private static final Color greenColor = new Color(100, 255, 100, transparency);
    private static final Color lightGreenColor = new Color(180, 255, 180, transparency);
    private static final Color whiteColor = new Color(255, 255, 255, transparency);
    private static final Color lightYellowColor = new Color(255, 255, 128, transparency);
    private static final Color redColor = new Color(255, 80, 80, transparency);
    private static final Color lightRedColor = new Color(255, 128, 128, transparency);
    private static final Color magentaColor = new Color(255, 190, 190, transparency);
    private static final Color cyanColor = new Color(128, 255, 255, transparency);
    private static final Color lightBlueColor = new Color(0, 188, 255, transparency);
    private static final Color horOn = new Color(255, 255, 0);
    private static final Color horOff = new Color(32, 32, 0);
    private static final Color eqOn = new Color(255, 0, 0);
    private static final Color eqOff = new Color(32, 0, 0);
    private static final Color ecOn = new Color(0, 255, 0);
    private static final Color ecOff = new Color(0, 32, 0);
    private int viewMode = 0;
    private byte flipState = 0;
    private double orbitRotAngRad = 0, tempAngleRad, yaw = 0, tempYaw, pitch = 0, tempPitch;
    private float yawCheck = Float.MAX_VALUE, pitchCheck = Float.MAX_VALUE, haCheck, lstCheck, latCheck, lonCheck, oblCheck;
    private long timeCheck, altCheck, boardCheck, orreryCheck;
    private boolean showBoard = false, trackSun = false, showLabels = true, showHorizon = true, showEquator = true, showEcliptic = true;

    public AstroPanel(int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;
        df.setRoundingMode(RoundingMode.HALF_DOWN);
        setDoubleBuffered(true);
        setBounds(0, 0, width, height);
        setPreferredSize(new Dimension(width, height));
        addMouseMotionListener(new MouseMove());
        addMouseListener(new MouseClick());
        addButtons();
    }

    public void run() {
        setDefaultView();
        startUpdateThread();
    }

    private void startUpdateThread() {
        new Thread(() -> {
            long fps = 60;
            long update = 0;
            long begin;
            while (true) {
                begin = System.currentTimeMillis();
                if (begin >= (update + (1000 / fps))) {
                    update = System.currentTimeMillis();
                    update();
                }
                Util.threadSleep(1);
            }
        }).start();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(font);

        switch (viewMode) {
            case 0:
                drawAstro(g2, astroVal.get("AltRad"));
                break;
            case 1:
                drawOrbit(g2, astroVal.get("EcLongRad"), astroVal.get("SidHaRad"));
                break;
        }

        if (showBoard) {
            drawBoard(g2, board);
        }
        drawTime(g2);
    }

    private void setDefaultView() {
        update();
        updateTracking();
    }

    private int checkUpdateLevel(HashMap<String, Double> astroVal, double yaw, double pitch) {
        long timeCrc = Util.crcCheck(Util.toPlanetTime(astroVal.get("LocalMillis").longValue(), astroVal.get("SolarDayDurationMillis").longValue(), astroVal.get("YearDurationMillis").longValue(), true).getBytes());
        long altCrc = Util.crcCheck(Util.toString(Util.toDegrees(astroVal.get("AltRad")), false, true).getBytes());
        int level = -1;
        double smoothVal = Util.toRadians(Util.getDegree(0, 0, 1));
        double rawVal = Util.toRadians(Util.getDegree(0, 1, 0));

        double lha = astroVal.get("LocalHourAngleRad");
        double sidHa = astroVal.get("SidHaRad");
        double lat = astroVal.get("LatitudeRad");
        double lon = astroVal.get("LongitudeRad");
        double obl = astroVal.get("ObliquityRad");


        if (timeCheck != timeCrc) {
            level = 1;
        }
        if (altCheck != altCrc) {
            level = 2;
        }
        if (haCheck > lha + rawVal || haCheck < lha - rawVal) {
            level = 2;
        }
        if (lstCheck > sidHa + rawVal || lstCheck < sidHa - rawVal) {
            level = 3;
        }
        if (latCheck > lat + smoothVal || latCheck < lat - smoothVal) {
            level = 3;
        }
        if (lonCheck > lon + smoothVal || lonCheck < lon - smoothVal) {
            level = 3;
        }
        if (oblCheck > obl + smoothVal || oblCheck < obl - smoothVal) {
            level = 3;
        }
        if (yawCheck > yaw + smoothVal || yawCheck < yaw - smoothVal) {
            level = 4;
        }
        if (pitchCheck > pitch + smoothVal || pitchCheck < pitch - smoothVal) {
            level = 4;
        }

        switch (level) {
            case 1:
                timeCheck = timeCrc;
                break;
            case 2:
                timeCheck = timeCrc;
                altCheck = altCrc;
                haCheck = (float) lha;
                break;
            case 3:
                timeCheck = timeCrc;
                altCheck = altCrc;
                haCheck = (float) lha;
                lstCheck = (float) sidHa;
                latCheck = (float) lat;
                lonCheck = (float) lon;
                oblCheck = (float) obl;
                break;
            case 4:
                timeCheck = timeCrc;
                altCheck = altCrc;
                haCheck = (float) lha;
                lstCheck = (float) sidHa;
                latCheck = (float) lat;
                lonCheck = (float) lon;
                oblCheck = (float) obl;
                yawCheck = (float) yaw;
                pitchCheck = (float) pitch;
                break;
        }
        return level;
    }

    private boolean checkBoardUpdate(String[] values) {
        String sum = "";
        for (String value : values) {
            sum = sum.concat(value);
        }
        long check = Util.crcCheck(sum.getBytes());
        if (boardCheck != check) {
            boardCheck = check;
            return true;
        } else {
            return false;
        }
    }

    private void updateBoard() {
        board = getBoardStrings();
        if (checkBoardUpdate(board)) {
            repaint();
        }
    }

    private void updateTracking() {

        if (astroGrid.isStereographic()) {
            yaw = ((Util.PI2 - astroVal.get("AziRad")) + Util.PI) % Util.PI2;
            pitch = -astroVal.get("AltRad");
        } else {
            yaw = ((Util.PI2 - astroVal.get("AziRad")) + Util.PI2) % Util.PI2;
            pitch = astroVal.get("AltRad");
        }

    }

    private void updateAstro(HashMap<String, Double> astroVal, boolean forceUpdate) {
        int updateLvl = checkUpdateLevel(astroVal, yaw, pitch);

        if (forceUpdate) {
            astroGrid.update(astroVal, 4, yaw, pitch, WIDTH, HEIGHT);
        } else {
            if (updateLvl != -1 && updateLvl <= 1) {
                repaint();
            } else if (updateLvl >= 2) {
                astroGrid.update(astroVal, updateLvl, yaw, pitch, WIDTH, HEIGHT);
                repaint();
            }
        }
    }

    private void updateOrrery(HashMap<String, Double> values) {
        String sid_s = Util.toString(Util.toDegrees(values.get("SidHaRad")), false, true);
        String elo_s = Util.toString(Util.toDegrees(values.get("EcLongRad")), true, true);
        String rot_s = Util.toString(Util.toDegrees(orbitRotAngRad), false, true);

        long check = Util.crcCheck((sid_s + elo_s + rot_s).getBytes());
        if (orreryCheck != check) {
            orreryCheck = check;
            repaint();
        }
    }


    private void update() {
        astroCalc.calculate();
        astroVal = astroCalc.getAll();

        if (trackSun && viewMode == 0) {
            updateTracking();
        }
        if (board == null) {
            board = getBoardStrings();
        }
        if (showBoard) {
            updateBoard();
        }
        if (viewMode == 0) {
            updateAstro(astroVal, false);
        }
        if (viewMode == 1) {
            updateOrrery(astroVal);
        }
    }

    private String[] getBoardStrings() {
        String[] values = new String[29];
        values[0] = Util.toString(Util.toDegrees(astroVal.get("AltRad")), false, true);
        values[1] = Util.toString(Util.toDegrees(astroVal.get("AziRad")), false, true);
        values[2] = Util.toString(Util.toDegrees(astroVal.get("RaRad")) / 15, true, false);
        values[3] = Util.toString(Util.toDegrees(astroVal.get("EcLongRad")), true, true);
        values[4] = Util.toString(Util.toDegrees(astroVal.get("DecRad")), true, true);
        values[5] = Util.toString(Util.toDegrees(astroVal.get("EoTRad")) / 15, true, false);
        values[6] = Util.toString(astroVal.get("RiseTime"), true, false);
        values[7] = Util.toString(astroVal.get("SetTime"), true, false);
        values[8] = Util.toString(astroVal.get("NoonTime"), true, false);
        values[9] = Util.toString(astroVal.get("NightTime"), true, false);
        values[10] = Util.toString(astroVal.get("NoonAltDeg"), false, true);
        values[11] = Util.toString(astroVal.get("NightAltDeg"), false, true);
        values[12] = Util.toString(Util.toDegrees(astroVal.get("SunRiseAziRad")), false, true);
        values[13] = Util.toString(Util.toDegrees(astroVal.get("SunSetAziRad")), false, true);
        values[14] = Util.toString(astroVal.get("DayLengthTime"), false, false);
        values[15] = Util.toString(astroVal.get("NightLengthTime"), false, false);
        values[16] = Util.toString(Util.toDegrees(astroVal.get("LocalHourAngleRad")) / 15, false, false);
        values[17] = Util.toString(Util.toDegrees(astroVal.get("LocalHourAngleRad")), false, true);
        values[18] = Util.toString(Util.toDegrees(astroVal.get("SidHaRad")) / 15, false, false);
        values[19] = Util.toString(Util.toDegrees(astroVal.get("SidHaRad")), false, true);
        values[20] = getLatitudeString();
        values[21] = getLongitudeString();
        values[22] = getObliquityString();
        values[23] = Util.toRealTime(astroVal.get("RealTimeMillis").longValue(), true);
        values[24] = Util.toPlanetTime(astroVal.get("GlobalMillis").longValue(), astroVal.get("SolarDayDurationMillis").longValue(), astroVal.get("YearDurationMillis").longValue(), true);
        values[25] = Util.toPlanetTime(astroVal.get("LocalMillis").longValue(), astroVal.get("SolarDayDurationMillis").longValue(), astroVal.get("YearDurationMillis").longValue(), true);
        values[26] = Util.toString(24, true, false);
        values[27] = Util.toString((astroVal.get("SiderealDayDurationMillis") * 24) / astroVal.get("SolarDayDurationMillis").longValue(), true, false);
        values[28] = df.format(astroVal.get("YearSolarDays"));
        return values;
    }


    private void drawAstro(Graphics2D g2, double alt_r) {
        int fadedBlue;
        double alt_d = Util.toDegrees(alt_r);
        long multiplier = timeManager.getMultiplier();
        if (multiplier <= 524288 && multiplier >= -524288) {
            fadedBlue = 255 - (int) (255 * ((alt_d >= -18 && alt_d <= 0) ? -Math.sin(((Math.PI / 2) / 18 * alt_d)) : ((alt_d >= 0) ? 0 : 1)));
        } else {
            fadedBlue = 0;
        }

        g2.setColor(Color.black);
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(new Color(0, 0, fadedBlue));
        g2.fillOval(0, 0, WIDTH - 1, HEIGHT - 1);


        g2.setColor(grayColor);
        astroGrid.drawGround(g2);

        g2.setColor(Color.darkGray);
        g2.drawOval(0, 0, WIDTH - 1, HEIGHT - 1);

        if (showEquator) {
            g2.setColor(Color.red);
            astroGrid.drawEquatorialGrid(g2);
        }

        if (showEcliptic) {
            g2.setColor(Color.green);
            astroGrid.drawEclipticLine(g2);
        }

        g2.setColor(linesColor);
        if (showHorizon) {
            astroGrid.drawHorizontalGrid(g2);
        } else {
            astroGrid.drawHorizonLine(g2);
        }

        g2.setColor(Color.yellow);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 10));
        astroGrid.drawSun(g2, alt_d);

        if (showLabels) {
            g2.setColor(Color.white);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18));
            astroGrid.drawLabels(g2);
        }
    }

    private void drawOrbit(Graphics2D g2, final double elon_r, final double sid_r) {

        int middleX = (WIDTH / 2);
        int middleY = (HEIGHT / 2);
        int radius = (HEIGHT - 50);
        double orbitAngleRad = (elon_r + Util.toRadians(90)) % Util.PI2;

        g2.setColor(Color.black);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        //Planet coordinates
        int x = (int) (((radius / 2) * Math.sin(orbitAngleRad - orbitRotAngRad)) + middleX);
        int y = (int) (((radius / 2) * Math.cos(orbitAngleRad - orbitRotAngRad)) + middleY);

        //RedLine
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(stroke2);
        g2.setColor(Color.gray);
        g2.drawLine(middleX, middleY, x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        //Axis lines
        g2.setStroke(dashedStroke);
        g2.setColor(Color.green);
        double shift = 0;
        int x0, y0, x1, y1;
        for (int i = 0; i < 2; i++) {
            x0 = (int) (((radius / 2) * Math.cos(orbitRotAngRad + shift)) + middleX);
            y0 = (int) (((radius / 2) * Math.sin(orbitRotAngRad + shift)) + middleY);
            x1 = (int) (((radius / 2) * Math.cos(orbitRotAngRad + shift + Math.PI)) + middleX);
            y1 = (int) (((radius / 2) * Math.sin(orbitRotAngRad + shift + Math.PI)) + middleY);
            g2.drawLine(x0, y0, x1, y1);
            shift += Math.PI / 2;
        }
        g2.setStroke(stroke1);
        //Orbit Oval
        g2.setColor(Color.gray);
        g2.drawOval(middleX - radius / 2, middleY - radius / 2, radius, radius);
        g2.setColor(Color.yellow);
        g2.fillOval(middleX - 4, middleY - 4, 8, 8);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12));
        g2.drawString("Sun", middleX + 5, middleY - 5);

        //PlanetRotation
        drawPlanetRotation(g2, x, y, sid_r);

        //Planet
        g2.setColor(Color.white);
        g2.fillOval(x - 4, y - 4, 8, 8);

        //Planet label
        g2.setColor(greenColor);
        g2.drawString(Util.toString(Util.toDegrees(elon_r), true, true), x + 25, y + 5);
        g2.setColor(lightRedColor);
        g2.drawString(Util.toString(Util.toDegrees(sid_r), false, true), x + 25, y - 8);

    }

    private void drawPlanetRotation(Graphics2D g2, int planetX, int planetY, double sid_r) {
        g2.setStroke(stroke2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int xOuter, yOuter, outerRadius = 45;
        double rot, shift = 0;
        double rotRad = sid_r - orbitRotAngRad;

        for (int i = 0; i < 4; i++) {
            rot = rotRad + shift;

            xOuter = (int) (((outerRadius / 2) * Math.sin(rot))) + planetX;
            yOuter = (int) (((outerRadius / 2) * Math.cos(rot))) + planetY;

            if (i == 3) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(greenColor);
            }

            g2.drawLine(xOuter, yOuter, planetX, planetY);
            shift += Util.PI * 0.5;
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private void drawTime(Graphics2D g2) {
        g2.setColor(Color.white);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14));
        g2.drawString(("Multiplier: " + timeManager.getMultiplier() + "x"), 6, HEIGHT - 70);
        g2.drawString("Local Time: " + Util.toPlanetTime(astroVal.get("LocalMillis").longValue(), astroVal.get("SolarDayDurationMillis").longValue(), astroVal.get("YearDurationMillis").longValue(), false), 6, HEIGHT - 55);
    }

    private void drawBoard(Graphics2D g2, String[] board) {
        g2.setFont(font);
        g2.setStroke(stroke1);

        int pos = 0;
        int space = 21;
        int rectWidth = 380;
        int rectHeight = 625;
        int rectX = (WIDTH - rectWidth) / 2;
        int rectY = (HEIGHT - rectHeight) / 2;
        int textX = rectX + 12;
        int textY = rectY + 26;

        g2.setColor(lightGrayColor);
        g2.fillRoundRect(rectX, rectY, rectWidth, rectHeight, 25, 25);
        g2.setColor(whiteColor);
        g2.drawRoundRect(rectX, rectY, rectWidth, rectHeight, 25, 25);

        g2.setColor(greenColor);
        g2.drawString("Sun Altitude: " + board[0], textX, textY);
        g2.drawString("Sun Azimuth: " + board[1], textX, textY + (pos += space));
        g2.setColor(lightGreenColor);
        g2.drawString("Sun RA: " + board[2], textX, textY + (pos += space));
        g2.drawString("Ecliptic Longitude: " + board[3], textX, textY + (pos += space));
        g2.setColor(whiteColor);
        g2.drawString("Sun Declination: " + board[4], textX, textY + (pos += space));
        g2.drawString("Equation of Time: " + board[5], textX, textY + (pos += space));
        g2.setColor(orangeColor);
        g2.drawString("Sun Rise Time: " + board[6], textX, textY + (pos += space));
        g2.drawString("Sun Set Time: " + board[7], textX, textY + (pos += space));
        g2.setColor(lightOrangeColor);
        g2.drawString("Solar Noon Time: " + board[8], textX, textY + (pos += space));
        g2.drawString("Solar Midnight Time: " + board[9], textX, textY + (pos += space));
        g2.setColor(lightYellowColor);
        g2.drawString("Solar Noon Alt: " + board[10], textX, textY + (pos += space));
        g2.drawString("Solar Midnight Alt: " + board[11], textX, textY + (pos += space));
        g2.setColor(whiteColor);
        g2.drawString("Sun Rise Azimuth: " + board[12], textX, textY + (pos += space));
        g2.drawString("Sun Set Azimuth: " + board[13], textX, textY + (pos += space));
        g2.setColor(redColor);
        g2.drawString("Day Length: " + board[14], textX, textY + (pos += space));
        g2.drawString("Night Length: " + board[15], textX, textY + (pos += space));
        g2.setColor(lightRedColor);
        g2.drawString("Local Hour Angle: " + board[16], textX, textY + (pos += space));
        g2.drawString("Local Hour Angle (Deg): " + board[17], textX, textY + (pos += space));
        g2.setColor(magentaColor);
        g2.drawString("Local Sidereal Time: " + board[18], textX, textY + (pos += space));
        g2.drawString("Local Sidereal Time: (Deg) " + board[19], textX, textY + (pos += space));
        g2.setColor(whiteColor);
        g2.drawString("Observer Latitude: " + board[20], textX, textY + (pos += space));
        g2.drawString("Observer Longitude: " + board[21], textX, textY + (pos += space));
        g2.drawString("Planet Obliquity: " + board[22], textX, textY + (pos += space));
        g2.setColor(lightBlueColor);
        g2.drawString("R.Time: " + board[23], textX, textY + (pos += space));
        g2.drawString("G.Time: " + board[24], textX, textY + (pos += space));
        g2.drawString("L.Time: " + board[25], textX, textY + (pos += space));
        g2.setColor(cyanColor);
        g2.drawString("Sol. Day Duration: " + board[26], textX, textY + (pos += space));
        g2.drawString("Sid. Day Duration: " + board[27], textX, textY + (pos += space));
        g2.drawString("Year Duration: " + board[28] + " solar days.", textX, textY + pos + space);
    }

    private String getLatitudeString() {
        int[] lat = astroCalc.getLatitude();
        int hour = lat[0];
        int minute = lat[1];
        int second = lat[2];
        return Util.toString(hour, minute, second, false, true) + ((hour >= 0 && minute >= 0 && second >= 0) ? " N" : " S");
    }

    private String getLongitudeString() {
        int[] lat = astroCalc.getLongitude();
        int hour = lat[0];
        int minute = lat[1];
        int second = lat[2];
        return Util.toString(hour, minute, second, false, true) + ((hour >= 0 && minute >= 0 && second >= 0) ? " W" : " E");
    }

    private String getObliquityString() {
        int[] lat = astroCalc.getObliquity();
        int hour = lat[0];
        int minute = lat[1];
        int second = lat[2];
        return Util.toString(hour, minute, second, false, true) + ((hour >= 0 && minute >= 0 && second >= 0) ? " Norm." : " Neg.");
    }

    private void centerView() {
        if (!trackSun && viewMode == 0) {
            double yawStep = (Util.PI2 / 24);
            double pitchStep = (Util.PI / 12);

            int mul_y = (int) BigDecimal.valueOf((yaw / yawStep)).setScale(0, RoundingMode.HALF_DOWN).doubleValue();
            int mul_p = (int) BigDecimal.valueOf((pitch / pitchStep)).setScale(0, RoundingMode.HALF_DOWN).doubleValue();

            yaw = yawStep * mul_y;
            pitch = pitchStep * mul_p;
        }
        if (viewMode == 1) {
            orbitRotAngRad = 0;
        }
    }

    private class MouseMove extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            int x = e.getX() - (WIDTH / 2);
            int y = e.getY() - (HEIGHT / 2);


            if (!trackSun && viewMode == 0) {
                int x1;
                int y1;
                if (astroGrid.isStereographic()) {
                    x1 = e.getX();
                    y1 = e.getY();
                } else {
                    x1 = -e.getX();
                    y1 = -e.getY();
                }

                yaw = (yaw + Util.PI2 - (tempYaw - x1 / 180D)) % Util.PI2;
                pitch = (pitch + (tempPitch - y1 / 180D)) % Util.PI2;
                tempYaw = x1 / 180D;
                tempPitch = y1 / 180D;

                if (pitch > Util.PI * 0.5) {
                    pitch = Util.PI * 0.5;
                } else if (pitch < -Util.PI * 0.5) {
                    pitch = -Util.PI * 0.5;
                }

            } else if (viewMode == 1) {
                orbitRotAngRad = (orbitRotAngRad + (tempAngleRad - Math.atan2(x, y))) % Util.PI2;
                tempAngleRad = Math.atan2(x, y);
            }
        }
    }

    private class MouseClick extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (!trackSun && viewMode == 0) {
                int x1;
                int y1;
                if (astroGrid.isStereographic()) {
                    x1 = e.getX();
                    y1 = e.getY();
                } else {
                    x1 = -e.getX();
                    y1 = -e.getY();
                }
                tempYaw = x1 / 180D;
                tempPitch = y1 / 180D;
            }

            if (viewMode == 1) {
                int x = e.getX() - (WIDTH / 2);
                int y = e.getY() - (HEIGHT / 2);
                tempAngleRad = Math.atan2(x, y);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "Horizon":
                showHorizon = !showHorizon;
                break;
            case "Equator":
                showEquator = !showEquator;
                break;
            case "Ecliptic":
                showEcliptic = !showEcliptic;
                break;
            case "Info":
                astroCalc.setBoardVisible(showBoard = !showBoard);
                break;
            case "Reverse":
                timeManager.decreaseTimeFlow();
                break;
            case "Play":
                timeManager.play();
                break;
            case "Forward":
                timeManager.increaseTimeFlow();
                break;
            case "Now":
                timeManager.resetTimeAdjustment(1);
                break;
            case "Sky":
                viewMode = 0;
                break;
            case "Orrery":
                viewMode = 1;
                break;
            case "Track":
                trackSun = !trackSun;
                break;
            case "Center":
                centerView();
                break;
            case "Label":
                showLabels = !showLabels;
                break;
            case "Flip":
                byte val = (flipState = (byte) ((flipState += 1) % 2));
                if (val == 0) {
                    astroGrid.setStereographic(true);
                } else if (val == 1) {
                    astroGrid.setStereographic(false);
                }
                updateTracking();
                updateAstro(astroVal, true);
                break;
        }
        repaint();
    }

    private void addButton(String command, String icon, String tooltip, boolean toggle, boolean selected, boolean painted, int x, int y) {

        JToggleButton togBtn = new JToggleButton() {
            final boolean hor = command.equals("Horizon");
            final boolean eq = command.equals("Equator");
            final boolean ec = command.equals("Ecliptic");
            final BufferedImage ico = ResourcesManager.getImage(icon);

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 10));
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(hor ? (showHorizon ? horOn : horOff) : eq ? (showEquator ? eqOn : eqOff) : ec ? (showEcliptic ? ecOn : ecOff) : null);
                g2.fillRect(5, 5, getWidth() - 10, getHeight() - 10);
                g2.setColor(Color.black);
                g2.drawString(hor ? "HOR" : eq ? "EQU" : "ECL", 12, 26);
                g2.drawImage(ico, 2, 2, null);
            }
        };

        AbstractButton button = (toggle ? (painted ? togBtn : new JToggleButton()) : new JButton());

        if (painted) {
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
        } else {
            button.setIcon(ResourcesManager.getIcon(icon));
        }

        button.setToolTipText(tooltip);
        button.setFont(font);
        button.setActionCommand(command);
        button.setSelected(selected);
        button.setFocusable(false);
        button.setOpaque(false);
        button.setBounds(x, y, 45, 45);
        button.addActionListener(this);
        add(button);
    }

    private void addButtons() {
        addButton("Info", "InfoIcon.png", "Info", true, false, false, 5, 5);
        addButton("Orrery", "OrreryIcon.png", "Show orrery", false, false, false, 50, 5);
        addButton("Sky", "SkyIcon.png", "Show sky", false, false, false, 95, 5);

        addButton("Label", "LabelIcon.png", "Show labels", true, true, false, WIDTH - 185, 5);
        addButton("Center", "CenterIcon.png", "Center", false, false, false, WIDTH - 140, 5);
        addButton("Track", "TrackSunIcon.png", "Track sun", true, false, false, WIDTH - 95, 5);
        addButton("Flip", "FlipViewIcon.png", "Flip view", false, false, false, WIDTH - 50, 5);

        addButton("Reverse", "ReverseIcon.png", "Reverse time", false, false, false, 5, HEIGHT - 50);
        addButton("Play", "PlayIcon.png", "Play", false, false, false, 50, HEIGHT - 50);
        addButton("Forward", "ForwardIcon.png", "Forward time", false, false, false, 95, HEIGHT - 50);
        addButton("Now", "NowIcon.png", "Now", false, false, false, 140, HEIGHT - 50);

        addButton("Horizon", "ButtonImage.png", "Horizontal grid", true, true, true, WIDTH - 140, HEIGHT - 50);
        addButton("Equator", "ButtonImage.png", "Equatorial grid", true, true, true, WIDTH - 95, HEIGHT - 50);
        addButton("Ecliptic", "ButtonImage.png", "Ecliptic line", true, true, true, WIDTH - 50, HEIGHT - 50);
    }

    public AstroCalc getAstroCalc() {
        return astroCalc;
    }

}
