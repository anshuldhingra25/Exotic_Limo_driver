package com.cabily.cabilydriver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cabily.cabilydriver.Pojo.DriverAlertpojo;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by user88 on 10/13/2015.
 */
public class DriverAlertAdapterNew extends BaseAdapter {
    private ArrayList<DriverAlertpojo> data;
    private LayoutInflater mInflater;
    private Context context;

    public DriverAlertAdapterNew(Context c, ArrayList<DriverAlertpojo> d) {
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
        private TextView myride_id, myride_time, myride_date, myride_address;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
        }
        return convertView;
    }
}
