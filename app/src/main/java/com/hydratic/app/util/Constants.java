package com.hydratic.app.util;

public class Constants {

    public static final String IMPERIAL_UNITS = "imperial";
    public static final String METRIC_UNITS = "metric";
    public static final String IMPERIAL_VOLUME_UNIT = "fl oz";
    public static final String IMPERIAL_VOLUME_UNIT_SHORT = "oz";
    public static final String METRIC_VOLUME_UNIT = "ml";
    public static final String GLASS = "glass";
    public static final String CUP = "cup";
    public static final String WATER = "water";
    public static final String COFFEE = "coffee";
    public static final String TEA = "tea";
    public static final String PERCENTAGE_FORMAT = "%.0f%%";
    public static final String HYDRATION_NOTIFICATION = "hydration_notification";
    public static final String AM_FORMAT = "%d AM";
    public static final String PM_FORMAT = "%d PM";
    public static final double HYDRATION_LEVEL_DEFAULT_MALE = 125;
    public static final double HYDRATION_LEVEL_DEFAULT_FEMALE = 91;
    public static final double HYDRATION_LEVEL_FACTOR = 0.66;
    public static final int EXTRA_DAILY_ACTIVITY_WATER_AMOUNT_OZ = 12;
    public static final double KG_TO_LB_CONVERSION_FACTOR = 0.45359237;
    public static final double FL_OZ_TO_ML_CONVERSTION_FACTOR = 0.033814;

    public class DatabaseFields {
        public static final String DB_FIELD_USERS = "users";
        public static final String DB_FIELD_DRINK_LOG = "drinkLog";
        public static final String DB_FIELD_PREFERRED_UNITS = "preferredUnits";
        public static final String DB_FIELD_HYDRATION_DAILY_TARGET = "hydrationDailyTarget";
        public static final String DB_FIELD_NOTIFICATIONS = "notifications";
        public static final String DB_FIELD_NOTIFICATIONS_START_TIME = "notificationsStartTime";
        public static final String DB_FIELD_NOTIFICATIONS_END_TIME = "notificationsEndTime";
        public static final String DB_FIELD_NOTIFICATIONS_REPEAT = "notificationsRepeat";
    }

    public class Dialogs {
        public static final String DIALOG_ADD_DRINK = "add_drink_dialog";
        public static final String DIALOG_DAILY_TARGET = "daily_target_dialog";
        public static final String DIALOG_NOTIF_START_TIME = "notifications_start_time_dialog";
        public static final String DIALOG_NOTIF_END_TIME = "notifications_end_time_dialog";
        public static final String DIALOG_NOTIF_REPEAT = "notifications_repeat_dialog";
    }

    public class Extras {
        public static final String EXTRA_SELECTED_FRAGMENT_ID = "selected_fragment_id";
        public static final String EXTRA_HINT = "hint";
        public static final String EXTRA_TEXT = "text";
        public static final String EXTRA_SELECTED_ITEM = "selected_item";
        public static final String EXTRA_FORCE_STOP = "force_stop";
        public static final String EXTRA_START_TIME = "start_time";
        public static final String EXTRA_END_TIME = "end_time";
        public static final String EXTRA_REPEAT = "repeat";
        public static final String EXTRA_INITIAL_CALL = "initial_call";
    }
}
