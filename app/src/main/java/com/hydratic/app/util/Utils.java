package com.hydratic.app.util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.hydratic.app.model.Units;
import com.hydratic.app.model.User;
import com.hydratic.app.service.NotificationsService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import static com.hydratic.app.util.Constants.AM_FORMAT;
import static com.hydratic.app.util.Constants.FL_OZ_TO_ML_CONVERSTION_FACTOR;
import static com.hydratic.app.util.Constants.IMPERIAL_VOLUME_UNIT;
import static com.hydratic.app.util.Constants.KG_TO_LB_CONVERSION_FACTOR;
import static com.hydratic.app.util.Constants.METRIC_VOLUME_UNIT;
import static com.hydratic.app.util.Constants.PM_FORMAT;

public class Utils {

    /**
     * Get a double value with the specified number of decimal places
     */
    public static double getDoubleWithDecimalPlaces(double doubleValue, int decimalPlaces) {
        return new BigDecimal(doubleValue).setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Convert kilograms to pounds
     */
    public static double convertKgsToLbs(double amountInKgs) {
        return amountInKgs / KG_TO_LB_CONVERSION_FACTOR;
    }

    /**
     * Convert fluid ounces to milliliters
     */
    public static double convertFlOzToMl(double amountInFlOz) {
        return amountInFlOz / FL_OZ_TO_ML_CONVERSTION_FACTOR;
    }

    /**
     * Convert milliliters to fluid ounces
     */
    public static double convertMlToFlOz(double amountInMl) {
        return amountInMl * FL_OZ_TO_ML_CONVERSTION_FACTOR;
    }

    /**
     * Compute the user's hydration target
     */
    public static double getHydrationTarget(User user, Units units) {
        return units == Units.METRIC
                ? Utils.convertFlOzToMl(user.hydrationDailyTarget)
                : user.hydrationDailyTarget;
    }

    /**
     * Get the volume units based on the user's preferred measurement units
     */
    public static String getVolumeUnits(Units units) {
        return units == Units.IMPERIAL
                ? IMPERIAL_VOLUME_UNIT
                : METRIC_VOLUME_UNIT;
    }

    /**
     * Get the current locale
     */
    public static Locale getLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }

    /**
     * Get a printable version of a 12 hour value that is specified in a 24 hour format
     */
    public static String getPrintable12Hours(int twentyForHours, Locale locale) {
        if (twentyForHours > 12) {
            return String.format(locale, PM_FORMAT, twentyForHours - 12);
        } else {
            return String.format(locale, AM_FORMAT, twentyForHours);
        }
    }

    /**
     * Start the notifications service
     */
    public static void startNotificationsService(Context context, User user) {
        final Intent startIntent = new Intent(context, NotificationsService.class);
        startIntent.putExtra(Constants.Extras.EXTRA_INITIAL_CALL, true);
        startIntent.putExtra(Constants.Extras.EXTRA_FORCE_STOP, false);
        startIntent.putExtra(Constants.Extras.EXTRA_START_TIME, user.notificationsStartTime);
        startIntent.putExtra(Constants.Extras.EXTRA_END_TIME, user.notificationsEndTime);
        startIntent.putExtra(Constants.Extras.EXTRA_REPEAT, user.notificationsRepeat);
        context.startService(startIntent);
    }

    /**
     * Stop the notifications service
     */
    public static void stopNotificationsService(Context context) {
        final Intent stopIntent = new Intent(context, NotificationsService.class);
        stopIntent.putExtra(Constants.Extras.EXTRA_FORCE_STOP, true);
        context.startService(stopIntent);
    }

    /**
     * Restart the notifications service
     */
    public static void restartNotificationsService(Context context, User user) {
        stopNotificationsService(context);
        startNotificationsService(context, user);
    }
}
