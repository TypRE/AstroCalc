package sk.typre.astrocalc;

import java.util.HashMap;

public class AstroCalc {

    private final TimeManager timeManager = new TimeManager(this);

    private double eLong_r, srs_azi, sst_azi, sra_r, eot_r;
    private double alt_r;
    private double dec_r;
    private double noon_alt_d;
    private double mid_alt_d;
    private double ha_r;
    private double azi_r;
    private double lat_r;
    private double lon_r;
    private double obl_r;
    private int[] lat;
    private int[] lon;
    private int[] obl;
    private long solarNoonMillis;
    private long solarMidnightMillis;
    private long sunRiseMillis;
    private long sunSetMillis;
    private long solarDayDurationMillis;
    private long dayLengthMillis;
    private long nightLengthMillis;
    private long yearDurationMillis;
    private int calculatedDay;
    private boolean boardVisible;
    private boolean p;
    private boolean o;

    public void calculate() {
        timeManager.timeTick();
        long millis = timeManager.getElapsedMillisGlobal();
        long millis_loc = timeManager.getElapsedMillisLocal();
        eLong_r = getEclipticLongitude(millis);
        sra_r = getSunRightAscension(eLong_r);
        eot_r = getEoTCorrection(millis);
        ha_r = getLocalHourAngle(millis_loc, eot_r);
        dec_r = getDeclination(obl_r, eLong_r);
        alt_r = getAltitude(lat_r, dec_r, ha_r);
        azi_r = getAzimuth(dec_r, lat_r, ha_r, alt_r);
        if (calculatedDay != timeManager.getLocalElapsedDays() && boardVisible) {
            currentDayCalc();
            calculatedDay = timeManager.getLocalElapsedDays();
        }
    }

    private void currentDayCalc() {
        long currentLocalDayMillis = timeManager.getCurrentLocalDayMillis();
        long nextSunRiseMillis = (long) getDayEventsData(currentLocalDayMillis + solarDayDurationMillis, true, false)[1];

        double[] sunRiseData = getDayEventsData(currentLocalDayMillis, true, false);
        double[] sunSetData = getDayEventsData(currentLocalDayMillis, false, false);

        solarNoonMillis = getSolarNoonMillis(currentLocalDayMillis, true, true);
        solarMidnightMillis = getSolarNoonMillis(currentLocalDayMillis, false, true);


        if (sunRiseData[0] == -1 || sunSetData[0] == -1 || nextSunRiseMillis == -1) {
            sunRiseMillis = 0;
            sunSetMillis = 0;
            srs_azi = 0;
            sst_azi = 0;
            dayLengthMillis = 0;
            nightLengthMillis = 0;
        } else {
            sunRiseMillis = (long) sunRiseData[1];
            sunSetMillis = (long) sunSetData[1];
            srs_azi = sunRiseData[2];
            sst_azi = sunSetData[2];
            dayLengthMillis = sunSetMillis - sunRiseMillis;
            nightLengthMillis = solarDayDurationMillis - (sunSetMillis - nextSunRiseMillis);
        }
        noon_alt_d = Util.toDegrees(getSolarMaxMinAltitude(currentLocalDayMillis, true));
        mid_alt_d = Util.toDegrees(getSolarMaxMinAltitude(currentLocalDayMillis, false));
    }

    public double[] getDayEventsData(long currentLocalDayMillis, boolean rising, boolean maxMin) {
        double eLong, eLongShift, noonEcLong, azi;
        double declination = 0;
        double hourAngle = 0;
        double eqOfTime = 0;
        long noonMillis, eventMillis;

        int eot_mod = (p & o ? -1 : !p & !o ? 1 : p ? -1 : 1) * ((rising) ? 1 : -1);
        int elo_mod = (p & o ? 1 : !p & !o ? -1 : p ? -1 : 1) * ((rising) ? 1 : -1);

        noonMillis = (currentLocalDayMillis - (solarDayDurationMillis >> 1));
        noonEcLong = getEclipticLongitude(noonMillis);
        eLong = noonEcLong;
        //Accuracy loop (Calculation of variables that depends on each other).
        for (int i = 0; i < 16; i++) {
            declination = getDeclination(obl_r, eLong);
            hourAngle = (maxMin) ? getMaxMinHa(lat_r, declination) : getSetRiseHA(lat_r, declination);
            eLongShift = (((hourAngle + (eqOfTime * eot_mod)) * solarDayDurationMillis) / yearDurationMillis);
            eqOfTime = getEoTCorrection(eLong);
            eLong = (noonEcLong + (eLongShift * elo_mod));
        }
        eventMillis = noonMillis + Util.getRadianMillis(hourAngle + (eqOfTime * eot_mod), solarDayDurationMillis) * ((rising) ? -1 : 1);
        azi = getSrsSstAzi(declination, lat_r, rising, p);
        if (Double.isNaN(hourAngle) || Double.isNaN(declination)) {
            return new double[]{-1, -1, -1};
        } else {
            return new double[]{eventMillis, eventMillis - currentLocalDayMillis, azi};
        }
    }


    private double getEqOfTimeShifted(long currentLocalDayMillis) {
        double eLong;
        double eotAfterShift = 0;
        int modifier = (p & o ? -1 : !p & !o ? -1 : 1);
        double noonELong = getEclipticLongitude(currentLocalDayMillis);
        eLong = noonELong;
        //Accuracy loop (Calculating true Equation of time after shifted by its value).
        for (int i = 0; i < 16; i++) {
            eotAfterShift = getEoTCorrection(eLong);
            eLong = noonELong + ((eotAfterShift * solarDayDurationMillis) / yearDurationMillis) * modifier;
        }
        return eotAfterShift;
    }

    private double getSolarMaxMinAltitude(long currentLocalDayMillis, boolean noon) {
        long locDayMil = (noon) ? currentLocalDayMillis - (solarDayDurationMillis >> 1) : currentLocalDayMillis;
        int modifier = (p & o ? -1 : !p & !o ? -1 : 1);
        double eLong = getEclipticLongitude(locDayMil);
        double eotShift = ((getEqOfTimeShifted(locDayMil) * solarDayDurationMillis) / yearDurationMillis);
        double declination = getDeclination(obl_r, eLong + (eotShift * modifier));
        return getMaxMinAlt(noon, declination, lat_r);
    }

    public long getSolarNoonMillis(long currentLocalDayMillis, boolean noon, boolean local) {
        long millis = (noon) ? currentLocalDayMillis - (solarDayDurationMillis >> 1) : currentLocalDayMillis;
        long eotShiftMillis = Util.getRadianMillis(getEqOfTimeShifted(millis), solarDayDurationMillis);
        return (millis + (p & o ? eotShiftMillis : !p & !o ? -eotShiftMillis : p ? eotShiftMillis : -eotShiftMillis)) - ((local) ? currentLocalDayMillis : 0);
    }

    private double getEoTCorrection(double eLong) {
        if (obl_r == 0) return 0;
        return (eLong - getSunRightAscension(eLong));
    }

    private double getEoTCorrection(long elapsed) {
        double eLong = getEclipticLongitude(elapsed);
        return getEoTCorrection(eLong);
    }

    private double getMaxMinAlt(boolean noonAlt, double dec, double lat) {
        return Math.asin(Math.sin(lat) * Math.sin(dec) + Math.cos(lat) * (noonAlt ? Math.cos(dec) : -Math.cos(dec)));
    }

    private double getAltitude(double lat, double dec, double ha) {
        return Math.asin(Math.sin(lat) * Math.sin(dec) + Math.cos(lat) * Math.cos(dec) * Math.cos(ha));
    }

    private double getAzimuth(double dec, double lat, double ha, double alt) {
        double azi_c = (Math.cos(lat) * Math.sin(dec) - Math.sin(lat) * Math.cos(dec) * Math.cos(ha)) / Math.cos(alt);
        double azimuth = Math.acos(azi_c > 1 ? 1 : azi_c < -1 ? -1 : azi_c);
        return (ha < Util.PI) ? Util.PI2 - azimuth : azimuth;
    }

    private double getSrsSstAzi(double dec, double lat, boolean rise, boolean planetCwRot) {
        double azi = Math.acos(Math.sin(dec) / Math.cos(lat));
        return rise ^ planetCwRot ? azi : Util.PI2 - azi;
    }

    private double getDeclination(double obl, double eLong) {
        return Math.asin(Math.sin(obl) * Math.sin(eLong));
    }

    private double getLocalHourAngle(long elapsedMillisLocal, double eqOfTimeRad) {
        return Util.radianCorrection(getHAUncorrected(elapsedMillisLocal) + eqOfTimeRad);
    }

    private double getSunRightAscension(double eLong) {
        return Util.radianCorrection(Math.atan2(Math.cos(obl_r) * Math.sin(eLong), Math.cos(eLong)));
    }

    private double getSetRiseHA(double latitudeRad, double declination) {
        return Math.acos(-Math.tan(latitudeRad) * Math.tan(declination));
    }

    private double getMaxMinHa(double latitudeRad, double declination) {
        double out_cos = Math.tan(latitudeRad) / Math.tan(declination);
        double cross_cos = Math.tan(declination) / Math.tan(latitudeRad);
        if (latitudeRad == 0) {
            return Math.acos(0);
        }
        return Math.acos(latitudeRad > 0 ^ declination > 0 ? Math.max(out_cos, cross_cos) : Math.min(out_cos, cross_cos));
    }

    private double getHAUncorrected(long elapsed) {
        if (p) {
            return Util.PI2 - Util.getRadianAngle(elapsed + (solarDayDurationMillis >> 1), solarDayDurationMillis);
        } else {
            return Util.getRadianAngle(elapsed + (solarDayDurationMillis >> 1), solarDayDurationMillis);
        }
    }

    private double getEclipticLongitude(long elapsed) {
        if (o) {
            return Util.PI2 - Util.getRadianAngle(elapsed + (yearDurationMillis >> 1) - (yearDurationMillis >> 2), yearDurationMillis);
        } else {
            return Util.getRadianAngle(elapsed + (yearDurationMillis >> 1) + (yearDurationMillis >> 2), yearDurationMillis);
        }
    }

    public HashMap<String,Double> getAll() {
        HashMap<String, Double> all = new HashMap<>();
        all.put("AltRad", getAltitudeRad());
        all.put("AziRad", getAzimuthRad());
        all.put("RaRad", getRightAscensionRad());
        all.put("EcLongRad", getEclipticLongitudeRad());
        all.put("DecRad",getDeclinationRad());
        all.put("EoTRad",getEqOfTimeRad());
        all.put("RiseTime",getSunRiseTime());
        all.put("SetTime",getSunSetTime());
        all.put("NoonTime",getSolarNoonTime());
        all.put("NightTime",getSolarMidNightTime());
        all.put("NoonAltDeg",getNoonAltDeg());
        all.put("NightAltDeg",getMidnightAltDeg());
        all.put("SunRiseAziRad", getSunRiseAzimuthRad());
        all.put("SunSetAziRad",getSunSetAzimuthRad());
        all.put("DayLengthTime",getDayLengthTime());
        all.put("NightLengthTime",getNightLengthTime());
        all.put("LocalHourAngleRad",getLocalHourAngle());
        all.put("SidHaRad",getSiderealHourAngleRad());
        all.put("LatitudeRad",getLatitudeRad());
        all.put("LongitudeRad",getLongitudeRad());
        all.put("ObliquityRad",getObliquityRad());
        all.put("RealTimeMillis", (double) timeManager.getRealTimeMillis());
        all.put("GlobalMillis", (double) timeManager.getElapsedMillisGlobal());
        all.put("LocalMillis", (double) timeManager.getElapsedMillisLocal());
        all.put("SolarDayDurationMillis", (double) timeManager.getSolarDayDurationMillis());
        all.put("YearDurationMillis", (double) timeManager.getYearDurationMillis());
        all.put("SiderealDayDurationMillis",timeManager.getSiderealDayDurationMillis());
        all.put("YearSolarDays",timeManager.getYearSolarDays());
        return all;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public int[] getLatitude() {
        return lat;
    }

    public int[] getLongitude() {
        return lon;
    }

    public int[] getObliquity() {
        return obl;
    }

    public double getSiderealHourAngleRad() {
        return Util.radianCorrection(sra_r + ha_r);
    }

    public double getSolarNoonTime() {
        return Util.millisToHour(solarNoonMillis, solarDayDurationMillis);
    }

    public double getSolarMidNightTime() {
        return Util.millisToHour(solarMidnightMillis, solarDayDurationMillis);
    }

    public double getSunRiseTime() {
        return Util.millisToHour(sunRiseMillis, solarDayDurationMillis);
    }

    public double getSunSetTime() {
        return Util.millisToHour(sunSetMillis, solarDayDurationMillis);
    }

    public double getDayLengthTime() {
        return Util.millisToHour(dayLengthMillis, solarDayDurationMillis);
    }

    public double getNightLengthTime() {
        return Util.millisToHour(nightLengthMillis, solarDayDurationMillis);
    }

    public double getAltitudeRad() {
        return alt_r;
    }

    public double getAzimuthRad() {
        return azi_r;
    }

    public double getEclipticLongitudeRad() {
        return eLong_r;
    }

    public double getRightAscensionRad() {
        return sra_r;
    }

    public double getDeclinationRad() {
        return dec_r;
    }

    public double getEqOfTimeRad() {
        return eot_r;
    }

    public double getSunRiseAzimuthRad() {
        return srs_azi;
    }

    public double getSunSetAzimuthRad() {
        return sst_azi;
    }

    public double getLatitudeRad() {
        return lat_r;
    }

    public double getLongitudeRad() {
        return lon_r;
    }

    public double getObliquityRad() {
        return obl_r;
    }

    public double getLocalHourAngle() {
        return ha_r;
    }

    public double getNoonAltDeg() {
        return noon_alt_d;
    }

    public double getMidnightAltDeg() {
        return mid_alt_d;
    }

    public void setBoardVisible(boolean visible) {
        boardVisible = visible;
        if (visible) {
            currentDayCalc();
        }
    }

    public void setStartMillis(long startDate) {
        timeManager.setStartMillis(startDate);
    }

    public void setLatitude(int[] latitude, boolean recalculate) {
        this.lat = latitude;
        this.lat_r = (Util.PI * (latitude[0] + (latitude[1] / 60D) + (latitude[2] / 3600D)) / 180.0);
        if (recalculate && boardVisible) {
            currentDayCalc();
        }

    }

    public void setLongitude(int[] longitude, boolean recalculate) {
        this.lon = longitude;
        this.lon_r = (Util.PI * (longitude[0] + (longitude[1] / 60D) + (longitude[2] / 3600D)) / 180.0);
        timeManager.setLongitudeRad(lon_r);
        if (recalculate && boardVisible) {
            currentDayCalc();
        }
    }

    public void setObliquity(int[] obliquity, boolean recalculate) {
        this.obl = obliquity;
        this.obl_r = (Util.PI * (obliquity[0] + (obliquity[1] / 60D) + (obliquity[2] / 3600D)) / 180.0);
        if (recalculate && boardVisible) {
            currentDayCalc();
        }
    }

    public void setDayDurationMillis(long solarDayDurationMillis, boolean recalculate) {
        this.solarDayDurationMillis = solarDayDurationMillis;
        timeManager.setDayDurationMillis(solarDayDurationMillis);
        if (recalculate && boardVisible) {
            currentDayCalc();
        }
    }

    public void setYearDurationMillis(long yearDurationMillis, boolean recalculate) {
        this.yearDurationMillis = yearDurationMillis;
        timeManager.setYearDurationMillis(yearDurationMillis);
        if (recalculate && boardVisible) {
            currentDayCalc();
        }
    }

    public void setPlanetClockwiseRotation(boolean cw, boolean recalculate) {
        this.p = cw;
        timeManager.setPlanetClockwiseRotation(cw);
        if (recalculate && boardVisible) {
            currentDayCalc();
        }
    }

    public void setOrbitClockwiseRotation(boolean cw, boolean recalculate) {
        this.o = cw;
        timeManager.setOrbitClockwiseRotation(cw);
        if (recalculate && boardVisible) {
            currentDayCalc();
        }
    }

}
