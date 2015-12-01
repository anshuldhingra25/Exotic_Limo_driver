package com.app.parser;

import com.app.dao.LoginDetails;
import com.app.dao.ViewTripDetails;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by user88 on 10/23/2015.
 */
public class ViewTripParser extends  BaseParser{

    ViewTripDetails details;
    @Override
    public Object parse(String response)  {
        details = new ViewTripDetails();
        try {
            JSONObject object = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return details;
    }
}
