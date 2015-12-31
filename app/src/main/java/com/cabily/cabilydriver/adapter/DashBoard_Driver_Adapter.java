/*
package com.cabily.cabilydriver.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cabily.cabilydriver.Pojo.Driver_Dashborad_Pojo;
import com.cabily.cabilydriver.Pojo.PaymentDetailsListPojo;
import com.cabily.cabilydriver.R;

import java.util.ArrayList;

*/
/**
 * Created by user88 on 12/22/2015.
 *//*

public class DashBoard_Driver_Adapter extends BaseAdapter {
    private ArrayList<Driver_Dashborad_Pojo> data;
    private LayoutInflater mInflater;
    private Activity context;
    private String check;

    public DashBoard_Driver_Adapter(Activity c, ArrayList<Driver_Dashborad_Pojo> d) {
        context = c;
        mInflater = LayoutInflater.from(context);
        data = d;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {

        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        private TextView dashboard_driver_last_trip;
        private TextView dashborad_tripride_time;
        private TextView dashborad_trip_date;
        private TextView dashborad_trip_earnings;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        String data1 = " ";
        if (convertView == null) {
            view = mInflater.inflate(R.layout.dashboard_driver_single, parent, false);
            holder = new ViewHolder();
            holder.dashboard_driver_last_trip = (TextView) view.findViewById(R.id.dashboard_driver_last_trip);
            holder.dashborad_tripride_time = (TextView) view.findViewById(R.id.dashboard_ride_time);
            holder.dashborad_trip_date = (TextView) view.findViewById(R.id.dashboard_last_trip_ride_date);
            holder.dashborad_trip_earnings = (TextView) view.findViewById(R.id.netAmount_price);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.dashboard_driver_last_trip.setText(data.get(position).getDashboard_last_trip());
        holder.dashborad_tripride_time.setText(data.get(position).getDashboard_last_trip_ride_time());
        holder.dashborad_trip_date.setText(data.get(position).getDashboard_last_trip_ride_date());
        holder.dashborad_trip_earnings.setText(data.get(position).getDashboard_last_trip_earnings());

        return view;
    }
}
*/
