// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;

public class TimeUtil
{
    public static String time(final SimpleDateFormat format) {
        final Date date = new Date(System.currentTimeMillis());
        return format.format(date);
    }
    
    private static String formatTimeString(final TimeType timeType) {
        switch (timeType) {
            case HHMM: {
                return ":mm";
            }
            case HHMMSS: {
                return ":mm:ss";
            }
            default: {
                return "";
            }
        }
    }
    
    public static SimpleDateFormat dateFormatter(final TimeUnit timeUnit, final TimeType timeType) {
        SimpleDateFormat formatter = null;
        switch (timeUnit) {
            case h12: {
                formatter = new SimpleDateFormat("hh" + formatTimeString(timeType), Locale.UK);
                break;
            }
            case h24: {
                formatter = new SimpleDateFormat("HH" + formatTimeString(timeType), Locale.UK);
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected value: " + timeUnit);
            }
        }
        return formatter;
    }
    
    public static String getFinalTime(final ColourUtils.ColourCode colourCode2, final ColourUtils.ColourCode colourCode1, final TimeUnit timeUnit, final TimeType timeType, final Boolean value) {
        final String formatted = ColourUtils.getStringColour(colourCode2) + ":" + ColourUtils.getStringColour(colourCode1);
        String locale = "";
        final String time = time(dateFormatter(TimeUnit.h24, TimeType.HH));
        if (timeUnit == TimeUnit.h12) {
            if (Integer.parseInt(time) - 12 >= 0) {
                locale = " pm";
            }
            else {
                locale = " am";
            }
        }
        return ColourUtils.getStringColour(colourCode1) + time(dateFormatter(timeUnit, timeType)).replace(":", formatted) + ColourUtils.getStringColour(colourCode2) + locale;
    }
    
    public enum TimeType
    {
        HHMM, 
        HHMMSS, 
        HH;
    }
    
    public enum TimeUnit
    {
        h24, 
        h12;
    }
}
