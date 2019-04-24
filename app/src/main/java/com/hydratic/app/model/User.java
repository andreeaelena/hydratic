package com.hydratic.app.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String displayName;
    public String email;
    public double hydrationDailyTarget;
    public String volumeUnit;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String displayName, String email, double hydrationDailyTarget, String volumeUnit) {
        this.displayName = displayName;
        this.email = email;
        this.hydrationDailyTarget = hydrationDailyTarget;
        this.volumeUnit = volumeUnit;
    }
}
