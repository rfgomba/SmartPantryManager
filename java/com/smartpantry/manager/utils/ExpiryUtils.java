package com.smartpantry.manager.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Date helpers for expiry dates stored as yyyy-MM-dd text. */
public final class ExpiryUtils {

    private static final String PATTERN = "yyyy-MM-dd";
    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    private ExpiryUtils() {
        // Utility class: no instances
    }

    /** Today's date as yyyy-MM-dd. */
    public static String today() {
        return new SimpleDateFormat(PATTERN, Locale.US).format(new Date());
    }

    /**
     * True if the date is before 'today'. Because the format is yyyy-MM-dd,
     * plain text comparison gives the correct date order.
     */
    public static boolean isExpired(String expiryDate, String today) {
        return expiryDate != null && !expiryDate.trim().isEmpty()
                && expiryDate.compareTo(today) < 0;
    }

    /**
     * Days from today until the expiry date (0 = today, negative = expired).
     * @return null if the date is missing or invalid
     */
    public static Long daysUntil(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(PATTERN, Locale.US);
        format.setLenient(false);
        try {
            Date expiry = format.parse(expiryDate);
            Date today = format.parse(format.format(Calendar.getInstance().getTime()));
            if (expiry == null || today == null) {
                return null;
            }
            // Math.round copes with daylight-saving hours
            return Math.round((expiry.getTime() - today.getTime()) / (double) DAY_MS);
        } catch (ParseException e) {
            return null;
        }
    }
}
