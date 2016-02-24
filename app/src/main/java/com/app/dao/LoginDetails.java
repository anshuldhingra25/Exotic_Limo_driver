package com.app.dao;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 */


public class LoginDetails  extends  ServiceResponse{

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("response")
    @Expose
    private String response;
    @SerializedName("driver_image")
    @Expose
    private String driverImage;
    @SerializedName("driver_id")
    @Expose
    private String driverId;
    @SerializedName("driver_name")
    @Expose
    private String driverName;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("vehicle_number")
    @Expose
    private String vehicleNumber;
    @SerializedName("vehicle_model")
    @Expose
    private String vehicleModel;
    @SerializedName("key")
    @Expose
    private String key;

    @SerializedName("is_alive_other")
    @Expose
    private String is_alive_other;

    public String getIs_alive_other() {
        return is_alive_other;
    }

    public void setIs_alive_other(String is_alive_other) {
        this.is_alive_other = is_alive_other;
    }




    public String getSec_key() {
        return sec_key;
    }

    public void setSec_key(String sec_key) {
        this.sec_key = sec_key;
    }

    @SerializedName("sec_key")
    @Expose
    private String sec_key;

    /**
     * @return The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The response
     */
    public String getResponse() {
        return response;
    }

    /**
     * @param response The response
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * @return The driverImage
     */
    public String getDriverImage() {
        return driverImage;
    }

    /**
     * @param driverImage The driver_image
     */
    public void setDriverImage(String driverImage) {
        this.driverImage = driverImage;
    }

    /**
     * @return The driverId
     */
    public String getDriverId() {
        return driverId;
    }

    /**
     * @param driverId The driver_id
     */
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    /**
     * @return The driverName
     */
    public String getDriverName() {
        return driverName;
    }

    /**
     * @param driverName The driver_name
     */
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    /**
     * @return The email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email The email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return The vehicleNumber
     */
    public String getVehicleNumber() {
        return vehicleNumber;
    }

    /**
     * @param vehicleNumber The vehicle_number
     */
    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    /**
     * @return The vehicleModel
     */
    public String getVehicleModel() {
        return vehicleModel;
    }

    /**
     * @param vehicleModel The vehicle_model
     */
    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    /**
     * @return The key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key The key
     */
    public void setKey(String key) {
        this.key = key;
    }

}
