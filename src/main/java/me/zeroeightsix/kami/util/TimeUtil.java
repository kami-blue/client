package me.zeroeightsix.kami.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author S-B99
 * Updated by S-B99 on 06/02/20
 */
public class TimeUtil {
    /*
     * Get current time
     */
    public static String time(SimpleDateFormat format) {
        Date date = new Date(System.currentTimeMillis());
        return format.format(date);
    }

    public enum TimeType {
        HHMM, HHMMSS, HH
    }

    public enum TimeUnit {
        h24, h12
    }

    private static String formatTimeString(TimeType timeType) {
        switch (timeType) {
            case HHMM: return ":mm";
            case HHMMSS: return ":mm:ss";
            default: return "";
        }
    }

    public static SimpleDateFormat dateFormatter(TimeUnit timeUnit, TimeType timeType) {
        SimpleDateFormat formatter;
        switch (timeUnit) {
            case h12:
                formatter = new SimpleDateFormat("hh" + formatTimeString(timeType), Locale.UK); break;
            case h24:
                formatter = new SimpleDateFormat("HH" + formatTimeString(timeType), Locale.UK); break;
            default:
                throw new IllegalStateException("Unexpected value: " + timeUnit);
        }
        return formatter;
    }

    public static String getFinalTime(ColourUtils.ColourCode colourCode2, ColourUtils.ColourCode colourCode1, TimeUnit timeUnit, TimeType timeType) {
        String formatted = ColourUtils.getStringColour(colourCode2) + ":" + ColourUtils.getStringColour(colourCode1);
        String locale = "";
        String time = time(TimeUtil.dateFormatter(TimeUnit.h24, TimeType.HH));
        if (timeUnit == TimeUnit.h12) {
            if ((Integer.parseInt(time)) - 12 >= 0) { // checks if the 24 hour time minus 12 is negative or 0, if it is it's pm
                locale = " pm";
            } else {
                locale = " am";
            }
        }
        return ColourUtils.getStringColour(colourCode1) + time(dateFormatter(timeUnit, timeType)).replace(":", formatted) + ColourUtils.getStringColour(colourCode2) + locale;
    }
}
