package com.app.api;

import android.content.Context;
import android.os.AsyncTask;

import com.app.service.ServiceConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Administrator on 2/27/2016.
 */
public class DestinationMatrixApi {

    private String distanceURL = ServiceConstant.DISTANCE_MATRIX_API;
    private final String ORIGIN_STRING = "ORIGIN_STRING";
    private final String DESTINATION_STRING = "DESTINATION_STRING";
    private DistanceApiCallBack callBack;
    private Context context;


    public static interface  DistanceApiCallBack{
        public void onError(String error);
        public void onSuccess(String distanceTravel,String duration);
    }


    public DestinationMatrixApi(Context context,DistanceApiCallBack callBack) {
        this.context = context;
        this.callBack = callBack;
    }

    public void getDistanceBetweenTwoPoints(String origin, String destination) {
        distanceURL = distanceURL.replace(ORIGIN_STRING, origin);
        distanceURL = distanceURL.replace(DESTINATION_STRING, destination);
        new DistanceMatrixTask().execute("");
    }

    private class DistanceMatrixTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result ="";
            StringBuilder sb = new StringBuilder();
            try {
                InputStream in = null;
                try {
                    URL url = new URL(distanceURL);
                    URLConnection urlConnection = url.openConnection();
                    in = new BufferedInputStream(urlConnection.getInputStream());
                    InputStreamReader is = new InputStreamReader(in);
                    BufferedReader br = new BufferedReader(is);
                    String read = br.readLine();
                    while (read != null) {
                        sb.append(read);
                        read = br.readLine();
                    }
                    JSONObject jsonObject = new JSONObject(result);
                    String status = (String) jsonObject.get("status");
                    if("OK".equalsIgnoreCase(status)){
                        JSONArray rowsArray = jsonObject.getJSONArray("rows");
                        JSONObject object = (JSONObject) rowsArray.get(0);
                        JSONArray elementsArray =   object.getJSONArray("elements");
                        JSONObject distance = (JSONObject) elementsArray.get(0);
                        JSONObject distanceObject  = (JSONObject) distance.get("distance");
                        String totalDistanceTravel = (String) distanceObject.get("text");
                        JSONObject duration = (JSONObject) elementsArray.get(1);
                        JSONObject durationObject  =  (JSONObject) distance.get("duration");
                        String totalDurationInMinute = (String) distance.get("text");
                    }else{
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


}
