package sk.typre.astrocalc;

import java.awt.*;
import java.util.HashMap;

public class AstroGrid {

    private int[][][] equ;
    private int[][][] hor;
    private int[][][] ecl;
    private double[][][] grid;
    private double[][][] labels;
    private static final int[][][] ground = new int[2][2][0];
    private static final double[] sun = new double[3];
    private static final double maxZ = 0.000000000000001;
    private static final int sunSize = 5;

    private boolean stereographic = true;

    private double[][][] getGridVertices() {
        double[] vertex;
        double[][][] grid = new double[3][11][256];
        final double rm90 = Util.toRadians(-90);
        final double rm60 = Util.toRadians(-60);
        final double r30 = Util.toRadians(30);
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i <= 255; i++) {
                vertex = getSphericalPoint((rm60 + (r30 * j)) % Util.PI2, (i * (Util.PI2 / 255)) % Util.PI2);
                grid[0][j][i] = vertex[0];
                grid[1][j][i] = vertex[1];
                grid[2][j][i] = vertex[2];
            }
        }
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i <= 255; i++) {
                vertex = getSphericalPoint(rm90 + (((Util.PI2 / 255) * i) % Util.PI2), (j * (Util.PI / 6)) % Util.PI2);
                grid[0][j + 5][i] = vertex[0];
                grid[1][j + 5][i] = vertex[1];
                grid[2][j + 5][i] = vertex[2];
            }
        }
        return grid;
    }

    private double[] getSphericalPoint(double lat, double lon) {
        double lat_c = Math.cos(lat);
        return new double[]{1 * lat_c * Math.sin(lon), 1 * Math.sin(lat), 1 * lat_c * Math.cos(lon)};
    }

    private double[][][] rotateYaw(double[][][] vertices, double yaw) {
        double[][][] temp = new double[vertices.length][vertices[0].length][vertices[0][0].length];
        double yaw_s = Math.sin(yaw);
        double yaw_c = Math.cos(yaw);

        for (int i = 0; i < vertices[0].length; i++) {
            for (int j = 0; j < vertices[0][0].length; j++) {
                temp[0][i][j] = vertices[2][i][j] * yaw_s + vertices[0][i][j] * yaw_c;
                temp[1][i][j] = vertices[1][i][j];
                temp[2][i][j] = vertices[2][i][j] * yaw_c - vertices[0][i][j] * yaw_s;
            }
        }
        return temp;
    }

    private double[][][] rotatePitch(double[][][] vertices, double pitch) {
        double[][][] tempR = new double[vertices.length][vertices[0].length][vertices[0][0].length];
        double pit_s = Math.sin(pitch);
        double pit_c = Math.cos(pitch);

        for (int i = 0; i < vertices[0].length; i++) {
            for (int j = 0; j < vertices[0][0].length; j++) {
                tempR[0][i][j] = vertices[0][i][j];
                tempR[1][i][j] = vertices[1][i][j] * pit_c - vertices[2][i][j] * pit_s;
                tempR[2][i][j] = vertices[1][i][j] * pit_s + vertices[2][i][j] * pit_c;
            }
        }
        return tempR;
    }


    private double[][][] rotateEquatorial(double[][][] vertices, double ha, double lat, double yaw, double pitch) {
        double[][][] temp;
        temp = rotateYaw(vertices, ha);
        vertices = rotatePitch(temp, lat);
        temp = rotateYaw(vertices, yaw);
        return rotatePitch(temp, pitch);
    }

    private double[][][] rotateHorizontal(double[][][] vertices, double yaw, double pitch) {
        double[][][] temp;
        temp = rotateYaw(vertices, yaw);
        return rotatePitch(temp, pitch);
    }

    private double[][][] rotateEcliptic(double[][][] vertices, double obl_r, double sid_r, double lat_r, double yaw, double pitch) {
        double[][][] temp;
        temp = rotatePitch(vertices, -obl_r);
        vertices = rotateYaw(temp, (sid_r + Util.PI * 0.5) % Util.PI2);
        temp = rotatePitch(vertices, lat_r);
        vertices = rotateYaw(temp, yaw);

        return rotatePitch(vertices, pitch);
    }

    private double[][][] rotateSun(double[][][] sun, double lat, double yaw, double pitch) {
        double[][][] temp;
        temp = rotatePitch(sun, lat);
        sun = rotateYaw(temp, yaw);
        return rotatePitch(sun, pitch);
    }


    private double[][][] resizeVertices(double[][][] vertices, boolean stereographic, int width, int height) {
        int middleX = (width / 2);
        int middleY = (height / 2);
        double fov = Math.sqrt(3);
        double[][][] temp = new double[vertices.length][vertices[0].length][vertices[0][0].length];

        for (int i = 0; i < vertices[0].length; i++) {
            for (int j = 0; j < vertices[0][0].length; j++) {
                if (stereographic) {
                    temp[0][i][j] = (int) (middleX * -((vertices[0][i][j] / (fov - vertices[2][i][j])) * fov)) + middleX;
                    temp[1][i][j] = (int) (middleY * -((vertices[1][i][j] / (fov - vertices[2][i][j])) * fov)) + middleY;
                } else {
                    temp[0][i][j] = (int) (middleX * -vertices[0][i][j]) + middleX;
                    temp[1][i][j] = (int) (middleY * -vertices[1][i][j]) + middleY;
                }
                temp[2][i][j] = vertices[2][i][j];
            }
        }
        return temp;
    }

    //Cutoff lines behind circle
    private int[][][] cutOffLines(double[][][] vertices) {

        int[][] temp;
        int[][] indexes = new int[2][vertices[0][0].length];
        int[][][] out = new int[2][vertices[0].length][0];
        int index = 0;
        int on_len = 0;
        int off_len = 0;
        boolean on = false;
        boolean off = false;
        int smoothValue = 3;


        //Main loop for each polyline
        for (int i = 0; i < vertices[0].length; i++) {
            //check and save visible indexes
            for (int j = 0; j < vertices[0][0].length; j++) {
                if (stereographic ? vertices[2][i][j] < maxZ : vertices[2][i][j] > maxZ) {
                    on = true;
                    indexes[off ? 1 : 0][index++] = j;
                    on_len += off ? 0 : 1;
                    off_len += off ? 1 : 0;

                } else if (on) {
                    off = true;
                    index = 0;
                }
            }
            if (on_len + off_len >= 2) {
                //Build line by indexes
                temp = new int[2][on_len + off_len];
                if (!off) {
                    for (int j = 0; j < on_len; j++) {
                        temp[0][j] = (int) vertices[0][i][indexes[0][j]];
                        temp[1][j] = (int) vertices[1][i][indexes[0][j]];
                    }
                } else {
                    int j;
                    for (j = 0; j < off_len; j++) {
                        temp[0][j] = (int) vertices[0][i][indexes[1][j]];
                        temp[1][j] = (int) vertices[1][i][indexes[1][j]];
                    }
                    for (int k = 0; k < on_len; k++, j++) {
                        temp[0][j] = (int) vertices[0][i][indexes[0][k]];
                        temp[1][j] = (int) vertices[1][i][indexes[0][k]];
                    }
                }


                int coreLength = (((off_len + on_len) - 2) / smoothValue);
                int lastLength = Math.max(coreLength, 2) + Math.min(coreLength, 2);

                out[0][i] = new int[lastLength];
                out[1][i] = new int[lastLength];
                //Remove unnecessary vertices (smoothing)
                for (int ind, n = 0; n < lastLength; n++) {
                    ind = Math.min((n * smoothValue), (off_len + on_len) - 1);
                    out[0][i][n] = temp[0][ind];
                    out[1][i][n] = temp[1][ind];
                }

            }
            on = false;
            off = false;
            on_len = 0;
            off_len = 0;
            index = 0;
        }
        return out;
    }

    public void update(HashMap<String,Double> astroVal, int updateLvl, double yaw, double pitch, int width, int height) {
        if (grid == null) {
            grid = getGridVertices();
        }
        double lat = Util.toRadians(90) - astroVal.get("LatitudeRad");

        //Sun
        if (updateLvl >= 2) {
            double[][][] sun_p = new double[3][1][1];
            double[] sun_v = getSphericalPoint(astroVal.get("DecRad"), (astroVal.get("LocalHourAngleRad") + Util.PI) % Util.PI2);
            for (int i = 0; i < 3; i++)
                sun_p[i][0][0] = sun_v[i];
            double[][][] sun_res = resizeVertices(rotateSun(sun_p, lat, yaw, pitch), stereographic, width, height);
            for (int i = 0; i < 3; i++)
                sun[i] = sun_res[i][0][0];
        }
        //Equatorial + Ecliptic
        if (updateLvl >= 3) {
            double[][][] ec_line = new double[3][1][1];
            for (int i = 0; i < 3; i++)
                ec_line[i][0] = grid[i][2];
            equ = cutOffLines(resizeVertices(rotateEquatorial(grid, astroVal.get("SidHaRad"), lat, yaw, pitch), stereographic, width, height));
            ecl = cutOffLines(resizeVertices(rotateEcliptic(ec_line, astroVal.get("ObliquityRad"), astroVal.get("SidHaRad"), lat, yaw, pitch), stereographic, width, height));
        }
        //Horizon
        if (updateLvl >= 4) {
            hor = cutOffLines(resizeVertices(rotateHorizontal(grid, yaw, pitch), stereographic, width, height));
            updateGround(pitch, width, height);
            updateLabels(yaw, pitch, width, height);
        }
    }


    private void updateGround(double pitch, int width, int height) {
        int len = hor[0][2].length;
        double rad;
        int index = 0;
        double pitch_deg = Util.toDegrees(pitch);
        boolean normPitch = pitch_deg != 90 && pitch_deg != -90;

        ground[0][0] = new int[len + 129];
        ground[1][0] = new int[len + 129];

        ground[0][1] = hor[0][2];
        ground[1][1] = hor[1][2];

        if (normPitch) {
            for (int j = 0; j < len; j++) {
                ground[0][0][index] = hor[0][2][j];
                ground[1][0][index] = hor[1][2][j];
                index++;
            }
        }

        if (stereographic ? pitch_deg > -90 : pitch_deg < 90) {
            double rad_c;
            double rad_s;

            for (int j = 0; j <= 128; j++) {
                rad = ((j * (Util.PI / (normPitch ? 128 : 64))) + (stereographic ? 0 : Util.PI * 1.5)) % Util.PI2;
                rad_c = Math.cos(rad);
                rad_s = Math.sin(rad);
                ground[0][0][index] = (int) (((width / 2) * (stereographic ? rad_c : rad_s)) + (width / 2));
                ground[1][0][index] = (int) (((height / 2) * (stereographic ? rad_s : rad_c)) + (height / 2));
                index++;
            }
        }

    }

    private void updateLabels(double yaw, double pitch, int width, int height) {
        double[][][] temp = new double[3][10][1];
        double[] vertex;
        int index = 0;

        for (int i = 0; i < 8; i++) {
            vertex = getSphericalPoint(0, (Util.PI2 / 8) * i);
            temp[0][index][0] = vertex[0];
            temp[1][index][0] = vertex[1];
            temp[2][index][0] = vertex[2];
            index++;
        }
        vertex = getSphericalPoint(Util.toRadians(90), 0);
        temp[0][index][0] = vertex[0];
        temp[1][index][0] = vertex[1];
        temp[2][index][0] = vertex[2];
        index++;
        vertex = getSphericalPoint(Util.toRadians(-90), 0);
        temp[0][index][0] = vertex[0];
        temp[1][index][0] = vertex[1];
        temp[2][index][0] = vertex[2];

        temp = rotateHorizontal(temp, yaw, pitch);
        labels = resizeVertices(temp, stereographic, width, height);

    }

    public void drawHorizontalGrid(Graphics2D g2) {
        for (int j = 0; j < hor[0].length; j++) {
            g2.drawPolyline(hor[0][j], hor[1][j], hor[0][j].length);
        }
    }

    public void drawEquatorialGrid(Graphics2D g2) {
        for (int j = 0; j < equ[0].length; j++) {
            g2.drawPolyline(equ[0][j], equ[1][j], equ[0][j].length);
        }
    }

    public void drawEclipticLine(Graphics2D g2) {
        g2.drawPolyline(ecl[0][0], ecl[1][0], ecl[0][0].length);
    }

    public void drawGround(Graphics2D g2) {
        g2.fillPolygon(ground[0][0], ground[1][0], ground[0][0].length);
    }

    public void drawHorizonLine(Graphics2D g2) {
        g2.drawPolyline(ground[0][1], ground[1][1], ground[0][1].length);
    }

    public void drawSun(Graphics2D g2, double alt_d) {
        if (stereographic ? sun[2] < 0 : sun[2] > 0) {
            if (alt_d > 0) {
                g2.fillOval((int) (sun[0] - sunSize), (int) (sun[1] - sunSize), sunSize * 2, sunSize * 2);
            }
            g2.drawOval((int) (sun[0] - sunSize), (int) (sun[1] - sunSize), sunSize * 2, sunSize * 2);
            g2.drawString(Util.toString(alt_d, false, true), (int) (sun[0] + 12), (int) (sun[1] + 4));
        }
    }

    public void drawLabels(Graphics2D g2) {
        int fontWidth;
        String[] names = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "Zenith", "Nadir"};
        for (int i = 0; i < labels[0].length; i++) {
            if (i == 8) {
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12));
            }
            if (stereographic ? labels[2][i][0] < maxZ : labels[2][i][0] > maxZ) {
                fontWidth = (int) g2.getFontMetrics().getStringBounds(names[i], g2).getWidth();
                g2.drawString(names[i], (int) labels[0][i][0] - (fontWidth / 2), (int) labels[1][i][0] + 7);
            }
        }
    }

    public boolean isStereographic() {
        return stereographic;
    }

    public void setStereographic(boolean stereographic) {
        this.stereographic = stereographic;
    }


}
