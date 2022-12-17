package sk.typre.astrocalc;

import javax.swing.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Util {
    public static final double PI2 = 6.283185307179586476925286766559;
    public static final double PI = 3.1415926535897932384626433832795;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final Calendar CAL = Calendar.getInstance();
    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat DF = new DecimalFormat("0.00", SYMBOLS);
    private static final Pattern PATTERN = Pattern.compile("\\d+");

    static {
        DF.setRoundingMode(RoundingMode.DOWN);
    }

    public static long crcCheck(byte[] value) {
        Checksum crc = new CRC32();
        crc.update(value, 0, value.length);
        return crc.getValue();
    }

    public static double getDegree(int arc, int min, int sec) {
        return (arc + min / 60D + sec / 3600D);
    }

    public static double toDegrees(double angleRad) {
        return (180.0 * angleRad / PI) % 360;
    }

    public static double toRadians(double angleDeg) {
        return (PI * angleDeg / 180.0) % PI2;
    }

    public static double getRadianAngle(long elapsedMillis, long totalMillis) {
        return (Util.PI2 * ((elapsedMillis % totalMillis) / (double) totalMillis)) % Util.PI2;
    }

    public static long getRadianMillis(double radian, double totalMillis) {
        return (long) ((totalMillis * radian) / Util.PI2);
    }

    public static double radianCorrection(double radian) {
        return (radian < 0) ? (radian % PI2) + PI2 : radian % PI2;
    }

    public static String toString(double hour, boolean showDecimals, boolean degrees) {
        if (Double.isInfinite(hour)) {
            return "∞";
        }
        double minute = ((hour - (int) hour) * 60);
        double second = ((minute - (int) minute) * 60);
        String hourPrefix = (((hour < 0)) ? "-" : "") + (((hour < 10) && (hour > -10)) ? "0" : "");
        String minutePrefix = (((minute < 0)) ? "-" : "") + (((minute < 10) && (minute > -10)) ? "0" : "");
        String secondPrefix = (((second < 0)) ? "-" : "") + (((second < 10) && (second > -10)) ? "0" : "");
        return hourPrefix + Math.abs((int) hour) + ((degrees) ? "° " : "h:") + minutePrefix + Math.abs((int) minute) + ((degrees) ? "' " : "m:") + secondPrefix + ((showDecimals) ? DF.format(Math.abs(second)) : Math.abs((int) second)) + ((degrees) ? "\"" : "s");
    }

    public static String toString(int hour, int minute, int second, boolean showDecimals, boolean degrees) {
        if (Double.isInfinite(hour)) {
            return "∞";
        }
        String hourPrefix = (((hour < 10) && (hour > -10)) ? "0" : "");
        String minutePrefix = (((minute < 10) && (minute > -10)) ? "0" : "");
        String secondPrefix = (((second < 10) && (second > -10)) ? "0" : "");
        return hourPrefix + Math.abs(hour) + ((degrees) ? "° " : "h:") + minutePrefix + Math.abs(minute) + ((degrees) ? "' " : "m:") + secondPrefix + ((showDecimals) ? DF.format(Math.abs(second)) : Math.abs(second)) + ((degrees) ? "\"" : "s");
    }

    public static String toRealTime(long millis, boolean full) {
        CAL.setTimeInMillis(millis);
        int hour = CAL.get(Calendar.HOUR_OF_DAY);
        int minute = CAL.get(Calendar.MINUTE);
        int second = CAL.get(Calendar.SECOND);
        String date = SIMPLE_DATE_FORMAT.format(CAL.getTime());
        String time = (hour < 10 ? "0" : "") + hour + "h:" + (minute < 10 ? "0" : "") + minute + "m:" + (second < 10 ? "0" : "") + second + "s";
        if (full) {
            return date + " - " + time;
        } else {
            return time;
        }
    }

    public static String toPlanetTime(long elapsed, long dayMillis, long yearMillis, boolean full) {
        double hour = ((elapsed % dayMillis) * 24D) / dayMillis;
        int day = (int) (((elapsed / dayMillis) % Math.abs((double) yearMillis / dayMillis)) + 1);
        int year = (int) ((dayMillis * (elapsed / dayMillis)) / yearMillis);
        String time = toString(((hour < 0) ? 24 + hour : hour), false, false);
        if (full) {
            return day + "d/" + year + "y - " + time;
        } else {
            return time;
        }
    }


    public static double millisToHour(long millis, long solarDayDurationMillis) {
        double time = ((millis % solarDayDurationMillis) * 24D) / solarDayDurationMillis;
        return (time < 0) ? 24 + time : time;
    }

    public static void threadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkNumericString(String str) {
        return PATTERN.matcher(str).matches();
    }

    public static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }


}
