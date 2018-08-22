package com.mad.project.database;

/**
 * Created by Darren on 27/05/2018.
 */

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Records {

    public String recordID, name, type, date, time, location;
    public double amount;

    // Default constructor required for calls to
    // DataSnapshot.getValue(Records.class)
    public Records() {
    }

    public Records(String recordID, String name, String date, String time, String type, double amount, String location) {
        this.recordID = recordID;
        this.name = name;
        this.date = date;
        this.time = time;
        this.type = type;
        this.amount = amount;
        this.location = location;
    }


}