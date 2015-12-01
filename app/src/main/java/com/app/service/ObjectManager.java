package com.app.service;

import com.app.dao.LoginDetails;
import com.app.parser.ViewTripParser;
import com.google.gson.Gson;

public class ObjectManager implements ServiceConstant {
    Gson gson;

    public ObjectManager() {
        gson = new Gson();
    }

    public Object getObjectForUrl(String url, String response) {
        if (url.contains(LOGIN_URL)) {
            LoginDetails loginDetails = gson.fromJson(response, LoginDetails.class);
            return loginDetails;
        } else if (url.contains(UPDATE_CURRENT_LOCATION)) {
            return response;
        } else if (url.contains(UPDATE_AVAILABILITY)) {
            return response;
        } else if (url.contains(CANCELLATION_REQUEST)) {
            return response;
        } else if (url.contains(CANCEL_RIDE_REQUEST)) {
            return response;
        } else if (url.contains(BEGIN_RIDE_REQUEST)) {
            return response;
        } else if (url.contains(ARRIVED_REQUEST)) {
            return response;
        }else if (url.contains(END_RIDE_REQUEST)) {
            return response;
        }else if (url.contains(TRIP_VIEW_REQUEST)) {
            ViewTripParser parser = new ViewTripParser();
            LoginDetails details = (LoginDetails) parser.parse(response);
            return details;
        }  else {
            return response;
        }
    }
}
