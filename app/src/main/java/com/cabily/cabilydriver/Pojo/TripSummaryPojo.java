package com.cabily.cabilydriver.Pojo;

/**
 * Created by user14 on 9/23/2015.
 */
public class TripSummaryPojo {
    private String ride_id;
    private String ride_time;
    private String ride_date;
    private String pickup;
    private String group;
    private String datetime;


    public String getride_id() {
        return ride_id;
    }

    public void setride_id(String ride_id) {
        this.ride_id = ride_id;
    }

    public String getride_time() {
        return ride_time;
    }

    public void setride_time(String ride_time) {
        this.ride_time = ride_time;
    }

    public String getride_date() {
        return ride_date;
    }

    public void setride_date(String ride_date) {
        this.ride_date = ride_date;
    }

    public String getpickup() {
        return pickup;
    }

    public void setpickup(String pickup) {
        this.pickup = pickup;
    }

    public String getgroup() {
        return group;
    }

    public void setgroup(String group) {
        this.group = group;
    }

    public String getdatetime() {
        return datetime;
    }

    public void setdatetime(String datetime) {
        this.datetime = datetime;
    }
}
