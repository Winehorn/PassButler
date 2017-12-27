package edu.hm.cs.ig.passbutler.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Florian Kraus on 27.12.2017.
 */

public class DateUtil {
    public static int absoluteDayDif(Date firstDate, Date secondDate) {
        long difference = Math.abs(firstDate.getTime() - secondDate.getTime());
        int daysBetween = (int) TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
        return daysBetween;
    }
}
