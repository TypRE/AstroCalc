package sk.typre.astrocalc;

import java.io.Serializable;

public class SettingsProfile implements Serializable, Cloneable {
    private static final long serialVersionUID = -7302189632159865173L;
    private int id;
    private int version = 1;
    private String name = "Profile";
    private boolean isDefault;
    private int[] latitude = new int[]{45, 0, 0};
    private int[] longitude = new int[]{0, 0, 0};
    private int[] obliquity = new int[]{45, 0, 0};
    private long startMillis = 1637103600000L;
    private long dayDurationMillis = 86400000L;
    private long yearDurationMillis = 34560000000L;
    private boolean planetClockwiseRotation = false;
    private boolean orbitClockwiseRotation = true;

    public SettingsProfile(int id, boolean defaultProfile) {
        this.id = id;
        this.isDefault = defaultProfile;
    }

    public int getProfileId() {
        return id;
    }

    public int[] getLatitude() {
        return latitude;
    }

    public int[] getLongitude() {
        return longitude;
    }

    public int[] getObliquity() {
        return obliquity;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public long getDayDurationMillis() {
        return dayDurationMillis;
    }

    public long getYearDurationMillis() {
        return yearDurationMillis;
    }

    public boolean isPlanetClockwiseRotation() {
        return planetClockwiseRotation;
    }

    public boolean isOrbitClockwiseRotation() {
        return orbitClockwiseRotation;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public void setLatitude(int[] latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(int[] longitude) {
        this.longitude = longitude;
    }

    public void setObliquity(int[] obliquity) {
        this.obliquity = obliquity;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public void setDayDurationMillis(long dayDurationMillis) {
        this.dayDurationMillis = dayDurationMillis;
    }

    public void setYearDurationMillis(long yearDurationMillis) {
        this.yearDurationMillis = yearDurationMillis;
    }

    public void setPlanetClockwiseRotation(boolean prc) {
        this.planetClockwiseRotation = prc;
    }

    public void setOrbitClockwiseRotation(boolean ocr) {
        this.orbitClockwiseRotation = ocr;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public String toString() {
        return name + " " + id + ((isDefault) ? " (Default)" : "");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
