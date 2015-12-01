
package com.app.dao;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ride {

    @SerializedName("ride_id")
    @Expose
    private String rideId;
    @SerializedName("ride_time")
    @Expose
    private String rideTime;
    @SerializedName("ride_date")
    @Expose
    private String rideDate;
    @SerializedName("pickup")
    @Expose
    private String pickup;

    /**
     * 
     * @return
     *     The rideId
     */
    public String getRideId() {
        return rideId;
    }

    /**
     * 
     * @param rideId
     *     The ride_id
     */
    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    /**
     * 
     * @return
     *     The rideTime
     */
    public String getRideTime() {
        return rideTime;
    }

    /**
     * 
     * @param rideTime
     *     The ride_time
     */
    public void setRideTime(String rideTime) {
        this.rideTime = rideTime;
    }

    /**
     * 
     * @return
     *     The rideDate
     */
    public String getRideDate() {
        return rideDate;
    }

    /**
     * 
     * @param rideDate
     *     The ride_date
     */
    public void setRideDate(String rideDate) {
        this.rideDate = rideDate;
    }

    /**
     * 
     * @return
     *     The pickup
     */
    public String getPickup() {
        return pickup;
    }

    /**
     * 
     * @param pickup
     *     The pickup
     */
    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

}
