package com.hydratic.app.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.hydratic.app.util.Constants.FL_OZ_TO_ML_CONVERSTION_FACTOR;
import static com.hydratic.app.util.Constants.KG_TO_LB_CONVERSION_FACTOR;

public class Utils {

    public static double getDoubleWithDecimalPlaces(double doubleValue, int decimalPlaces) {
        return new BigDecimal(doubleValue).setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue();
    }

    public static double convertKgsToLbs(double amountInKgs) {
        return amountInKgs / KG_TO_LB_CONVERSION_FACTOR;
    }

    public static double convertFlOzToMl(double amountInFlOz) {
        return amountInFlOz / FL_OZ_TO_ML_CONVERSTION_FACTOR;
    }
}
