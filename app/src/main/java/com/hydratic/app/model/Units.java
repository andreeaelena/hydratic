package com.hydratic.app.model;

import static com.hydratic.app.util.Constants.IMPERIAL_UNITS;

public enum Units {
    IMPERIAL,
    METRIC;

    public static Units getUnitsFromUser(User user) {
        return user.preferredUnits.equalsIgnoreCase(IMPERIAL_UNITS)
                ? Units.IMPERIAL
                : Units.METRIC;
    }
}
