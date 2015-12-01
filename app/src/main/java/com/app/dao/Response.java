
package com.app.dao;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Response {

    @SerializedName("total_rides")
    @Expose
    private String totalRides;
    @SerializedName("rides")
    @Expose
    private List<Ride> rides = new ArrayList<Ride>();

    /**
     * 
     * @return
     *     The totalRides
     */
    public String getTotalRides() {
        return totalRides;
    }

    /**
     * 
     * @param totalRides
     *     The total_rides
     */
    public void setTotalRides(String totalRides) {
        this.totalRides = totalRides;
    }

    /**
     * 
     * @return
     *     The rides
     */
    public List<Ride> getRides() {
        return rides;
    }

    /**
     * 
     * @param rides
     *     The rides
     */
    public void setRides(List<Ride> rides) {
        this.rides = rides;
    }

}
