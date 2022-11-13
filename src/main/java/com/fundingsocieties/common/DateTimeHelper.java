package com.fundingsocieties.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeHelper {
    private DateTimeHelper() {
    }
    
    public static String getCurrentTime(final String datePattern) {
        final SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        final Date date = new Date();
        return sdf.format(date);
    }
}
