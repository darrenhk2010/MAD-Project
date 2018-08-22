package com.mad.project.database;

/**
 * Created by Darren on 14/05/2018.
 */

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Trips {

    public String tripID, name, currency, startdate, enddate;
    public double budget;

    public Trips() {
    }

    public Trips(String tripID, String name, double budget, String currency, String startdate, String enddate) {
        this.tripID = tripID;
        this.name = name;
        this.budget = budget;
        this.currency = currency;
        this.startdate = startdate;
        this.enddate = enddate;
    }


}