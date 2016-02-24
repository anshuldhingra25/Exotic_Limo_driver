package com.app.service;

/**
 * Created by Administrator on 10/1/2015.
 */
public interface ServiceConstant {
    //http://project.dectar.com/fortaxi/
    //http://192.168.1.251:8081/suresh/dectarfortaxi/




/*

    //---------------------live url---------------------
     String BASE_URL ="http://project.dectar.com/fortaxi/api/v1/";
     String XMPP_HOST_URL = "67.219.149.186";
     String XMPP_SERVICE_NAME = "messaging.dectar.com";
*/

/*
//local url
    String BASE_URL = "http://192.168.1.251:8081/suresh/dectarfortaxi/api/v1/";
    String XMPP_HOST_URL  = "192.168.1.150";
    String XMPP_SERVICE_NAME = "casp83";
*/


  //  String baseurl = "http://192.168.1.251:8081/suresh/dectarfortaxi/";

    //----------testing url live------------

    String URL = "http://project.dectar.com/cabilydemo/";
    String BASE_URL = URL+"api/v1/";
    String BASE_URL_v3 = URL+"api/v3/";

    String XMPP_HOST_URL = "67.219.149.186";
    String XMPP_SERVICE_NAME = "messaging.dectar.com";

    String baseurl = "http://project.dectar.com/cabilydemo/";

    String LOGIN_URL = BASE_URL + "provider/login";
 String Register_URL = baseurl + "driver/signup";
    String UPDATE_CURRENT_LOCATION = BASE_URL + "provider/update-driver-geo";
    String UPDATE_AVAILABILITY = BASE_URL + "provider/update-availability";
    String ACCEPTING_RIDE_REQUEST = BASE_URL +"provider/accept-ride";
    String CANCELLATION_REQUEST = BASE_URL + "provider/cancellation-reason";
    String CANCEL_RIDE_REQUEST = BASE_URL + "provider/cancel-ride";
    String ARRIVED_REQUEST = BASE_URL + "provider/arrived";
    String BEGIN_RIDE_REQUEST  =BASE_URL + "provider/begin-ride";
    String END_RIDE_REQUEST  =BASE_URL + "provider/end-ride";
    String LOGOUT_REQUEST = BASE_URL + "provider/logout";
    String TRIP_LIST_REQUEST = BASE_URL+ "provider/my-trips/list";
    String TRIP_VIEW_REQUEST = BASE_URL+ "provider/my-trips/view";
    public static String loginurl = BASE_URL+"provider/login";


    String place_search_url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?types=geocode&key=AIzaSyA7EzKek44txQ083ZSFEvtEq3kipmg4v2c&input=";
    String GetAddressFrom_LatLong_url = "https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyA7EzKek44txQ083ZSFEvtEq3kipmg4v2c&placeid=";


    public static String forgotpassword = BASE_URL+"provider/forgot-password";
    public static String changepassword = BASE_URL+"provider/change-password";
    public static String driver_dashboard = BASE_URL+"provider/dashboard?";


    public static String getBankDetails = BASE_URL+"provider/get-banking-info";
    public static String saveBankDetails = BASE_URL+"provider/save-banking-info";
    public static String paymentdetails_url= BASE_URL+"provider/payment-list";
    public static String paymentdetails_lis_url= BASE_URL+"provider/payment-summary";
    public static String tripsummery_view_url= BASE_URL+"provider/my-trips/view";
    public static String tripsummery_list_url= BASE_URL+"provider/my-trips/list";
    public static String ridecancel_reason_url= BASE_URL+"provider/cancellation-reason";
    public static String ridecancel_url= BASE_URL+"provider/cancel-ride";
    public static String arrivedtrip_url= BASE_URL+"provider/arrived";
    public static String begintrip_url= BASE_URL+"provider/begin-ride";
    public static String endtrip_url= BASE_URL+"provider/end-ride";
    public static String receivecash_url= BASE_URL+"provider/receive-payment";
    public static String receivedbill_amounr_cash_url= BASE_URL+"provider/payment-received";
    public static String reviwes_options_list_url= BASE_URL+"app/review/options-list";
    public static String  submit_reviwes_url= BASE_URL+"app/review/submit";
    public static String request_paymnet_url= BASE_URL+"provider/request-payment";


    public static String check_trip_status= BASE_URL_v3+"check-trip-status";



   // http://192.168.1.251:8081/suresh/dectarfortaxi/api/v3/check-trip-status
    //----------------Push notification Key--------------------------
    String ACTION_LABEL = "action";
    String ACTION_TAG_RIDE_REQUEST = "ride_request";
    String ACTION_TAG_RIDE_CANCELLED = "ride_cancelled";
    String ACTION_TAG_RECEIVE_CASH = "receive_cash";
    String ACTION_TAG_PAYMENT_PAID = "payment_paid";

    String ACTION_ACTION_HOCKYAPPID ="a0edd6450fc641bead62a9bed17c39cc";


    public static String useragent="cabily2k15android";
    public static String  isapplication="1";
    public static String  applanguage="en";
    public static String   cabily_AppType="android";




}
