package com.hydratic.app.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String id;
    public String displayName;
    public String email;
    public double hydrationDailyTarget;
    public String preferredUnits;
    public boolean notifications;
    public int notificationsStartTime;
    public int notificationsEndTime;
    public int notificationsRepeat;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String id, String displayName, String email, double hydrationDailyTarget, String preferredUnits) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.hydrationDailyTarget = hydrationDailyTarget;
        this.preferredUnits = preferredUnits;
        this.notifications = false;
        this.notificationsStartTime = 9; // Default: 9 AM
        this.notificationsEndTime = 20; // Default: 8 PM
        this.notificationsRepeat = 2; // Default: Every 2 hours
    }
}
