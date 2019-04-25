package com.hydratic.app.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class DrinkLog {

    public long timestamp;
    public int amount;
    public String unit;
    public String container;
    public String type;

    public DrinkLog() {
        // Default constructor required for calls to DataSnapshot.getValue(DrinkLog.class)
    }

    public DrinkLog(long timestamp, int amount, String unit, String container, String type) {
        this.timestamp = timestamp;
        this.amount = amount;
        this.unit = unit;
        this.container = container;
        this.type = type;
    }
}
