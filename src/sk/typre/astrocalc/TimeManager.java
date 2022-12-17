package sk.typre.astrocalc;

public class TimeManager {

    private final AstroCalc astroCalc;
    private long elapsedMillisGlobal;
    private long elapsedMillisLocal;
    private long startMillis;
    private long solarDayDurationMillis;
    private long yearDurationMillis;
    private double yearSolarDays;
    private double longitudeRad;
    private double addSiderealDays = 0;
    private long addMilliseconds = 0;
    private long addSeconds = 0;
    private long addMinutes = 0;
    private long addHours = 0;
    private long addDays = 0;
    private long addYears = 0;
    private boolean pcr;
    private boolean ocr;
    private boolean animation;
    private boolean animForward;
    private int animIndex;
    private int step;

    private long elapsed, skipped, shift, timeModifier, multiplier = 1;

    public TimeManager(AstroCalc astroCalc) {
        this.astroCalc = astroCalc;
    }

    public void timeTick() {
        animationTick();
        long modifier = ((System.currentTimeMillis() * multiplier) - skipped) + shift;
        elapsed = System.currentTimeMillis() - startMillis;
        if (multiplier != 1) {
            if ((elapsed + modifier) >= 0) {
                timeModifier = modifier;
            } else {
                setTime(0);
            }
        }
        elapsedMillisGlobal = calculateGlobalMillis();
        elapsedMillisLocal = elapsedMillisGlobal + (pcr ? getLongMillisShift() : -getLongMillisShift());
    }


    private void animationTick() {
        if (animation) {
            timeStep(animIndex, step, animForward);
        }
    }

    public void toggleAnimation(int index, int step, boolean forward) {
        animIndex = index;
        this.step = step;
        animation = !animation;
        animForward = forward;
        if (multiplier != -1) {
            setTimeMultiplier(-1);
        }
    }

    private long calculateGlobalMillis() {
        long year = (yearDurationMillis * addYears);
        long day = (solarDayDurationMillis * addDays);
        double hour = ((solarDayDurationMillis / 24D) * addHours);
        double minute = ((solarDayDurationMillis / 1440D) * addMinutes);
        double second = ((solarDayDurationMillis / 86400D) * addSeconds);
        return (long) ((elapsed + timeModifier) + year + day + hour + minute + second + addMilliseconds + addSiderealDays);
    }

    public void setTime(long millis) {
        resetTimeAdjustment(-1);
        shift = elapsed = -(System.currentTimeMillis() - startMillis) + millis;
        skipped = System.currentTimeMillis() * multiplier;
    }


    public void timeStep(int index, int step, boolean forward) {
        int modifier = forward ? 1 : -1;
        switch (index) {
            case 0:
                addMilliseconds += step * modifier;
                break;
            case 1:
                addSeconds += step * modifier;
                break;
            case 2:
                addMinutes += step * modifier;
                break;
            case 3:
                addHours += step * modifier;
                break;
            case 4:
                addDays += step * modifier;
                break;
            case 5:
                addSiderealDays += getSiderealDayDurationMillis() * step * modifier;
                break;
            case 6:
                addYears += step * modifier;
                break;
        }

        if (calculateGlobalMillis() < 0) {
            setTime(0);
        }
    }

    public void setSolarNoonTime() {
        setTime(astroCalc.getSolarNoonMillis(getCurrentLocalDayMillis(), true, false));
    }

    public void setSolarMidnightTime() {
        setTime(astroCalc.getSolarNoonMillis(getCurrentLocalDayMillis(), false, false));
    }

    public void setSunRiseTime() {
        long millis = (long) astroCalc.getDayEventsData(getCurrentLocalDayMillis(), true, false)[0];
        if (millis > 0) {
            setTime(millis);
        }
    }

    public void setMaxRisingTime() {
        long millis = (long) astroCalc.getDayEventsData(getCurrentLocalDayMillis(), true, true)[0];
        if (millis > 0) {
            setTime(millis);
        }
    }

    public void setSunSetTime() {
        long millis = (long) astroCalc.getDayEventsData(getCurrentLocalDayMillis(), false, false)[0];
        if (millis > 0) {
            setTime(millis);
        }
    }

    public void setMaxFallingTime() {
        long millis = (long) astroCalc.getDayEventsData(getCurrentLocalDayMillis(), false, true)[0];
        if (millis > 0) {
            setTime(millis);
        }
    }

    public double getSiderealDayDurationMillis() {
        return ((double) solarDayDurationMillis / (((double) yearDurationMillis / (double) solarDayDurationMillis) + ((pcr == ocr) ? 1 : -1))) * ((double) yearDurationMillis / (double) solarDayDurationMillis);
    }

    public void setPlanetClockwiseRotation(boolean planetClockwiseRotation) {
        this.pcr = planetClockwiseRotation;
    }

    public void setOrbitClockwiseRotation(boolean orbitClockwiseRotation) {
        this.ocr = orbitClockwiseRotation;
    }

    public void decreaseTimeFlow() {
        if (multiplier == 1) {
            multiplier -= 2;
        } else if (multiplier < 0) {
            multiplier <<= 1;
        } else if (multiplier > 0) {
            multiplier >>= 1;
        }
        skipped = System.currentTimeMillis() * multiplier;
        shift = timeModifier;
    }

    public void setTimeMultiplier(long multiplier) {
        this.multiplier = multiplier;
        skipped = System.currentTimeMillis() * multiplier;
        shift = timeModifier;
    }

    public void resetTimeAdjustment(long mul) {
        animation = false;
        multiplier = mul;
        timeModifier = 0;

        addMilliseconds = 0;
        addSeconds = 0;
        addMinutes = 0;
        addHours = 0;
        addDays = 0;
        addSiderealDays = 0;
        addYears = 0;
    }

    public void play() {
        if (multiplier == 1) {
            setTimeMultiplier(-1);
        } else {
            setTimeMultiplier(1);
        }
    }

    public void increaseTimeFlow() {
        if (multiplier == -1) {
            multiplier += 2;
        } else if (multiplier < 0) {
            multiplier >>= 1;
        } else if (multiplier > 0) {
            multiplier <<= 1;
        }
        skipped = System.currentTimeMillis() * multiplier;
        shift = timeModifier;
    }

    private long getLongMillisShift() {
        return (long) ((solarDayDurationMillis * longitudeRad) / Util.PI2);
    }

    public long getMultiplier() {
        return multiplier;
    }

    public double getYearSolarDays() {
        return yearSolarDays;
    }

    public long getSolarDayDurationMillis() {
        return solarDayDurationMillis;
    }

    public long getYearDurationMillis() {
        return yearDurationMillis;
    }

    public long getElapsedMillisGlobal() {
        return elapsedMillisGlobal;
    }

    public long getElapsedMillisLocal() {
        return elapsedMillisLocal;
    }

    public long getRealTimeMillis() {
        return elapsedMillisGlobal + startMillis;
    }

    public long getCurrentLocalDayMillis() {
        long longitudeMillisShift = (long) ((solarDayDurationMillis * longitudeRad) / Util.PI2);
        return (solarDayDurationMillis * getLocalElapsedDays()) + (pcr & ocr ? -longitudeMillisShift : !pcr & !ocr ? longitudeMillisShift : pcr ? -longitudeMillisShift : longitudeMillisShift);
    }

    public int getLocalElapsedDays() {
        return (int) ((elapsedMillisLocal / solarDayDurationMillis) + 1);
    }

    public void setDayDurationMillis(long millis) {
        solarDayDurationMillis = millis;
        yearSolarDays = Math.abs(((double) yearDurationMillis / solarDayDurationMillis));
    }

    public void setYearDurationMillis(long millis) {
        yearDurationMillis = millis;
        yearSolarDays = Math.abs((double) yearDurationMillis / solarDayDurationMillis);
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public void setLongitudeRad(double longitudeRad) {
        this.longitudeRad = longitudeRad;
    }


}
