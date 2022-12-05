package com.cookandroid.project_energizor;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class user_location {
    public int DeviceNo;
    public String Time;
    public float latitude;
    public float longitude;

    public user_location() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public user_location(int DeviceNo, String Time, float latitude, float longitude) {
        this.DeviceNo = DeviceNo;
        this.Time = Time;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
