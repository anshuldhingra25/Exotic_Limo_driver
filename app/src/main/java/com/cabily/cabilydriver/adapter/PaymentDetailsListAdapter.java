package com.cabily.cabilydriver.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cabily.cabilydriver.Pojo.PaymentDetailsListPojo;
import com.cabily.cabilydriver.R;

import java.util.ArrayList;

/**
 * Created by user14 on 9/22/2015.
 */
public class PaymentDetailsListAdapter extends BaseAdapter {
    private ArrayList<PaymentDetailsListPojo> data;
    private LayoutInflater mInflater;
    private Activity context;
    private String check;

    public PaymentDetailsListAdapter(Activity c, ArrayList<PaymentDetailsListPojo> d) {
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

    @Override
    public int getViewTypeCount() {
        return 1;
    }


    public class ViewHolder {
        private TextView rideid;
        private TextView amount;
        private TextView ridedate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        String data1 = " ";
        if (convertView == null) {
            view = mInflater.inflate(R.layout.payment_details_list_design, parent, false);
            holder = new ViewHolder();
            holder.rideid = (TextView) view.findViewById(R.id.pmt_dtl_list_rideid);
            holder.amount = (TextView) view.findViewById(R.id.pmt_dtl_list_amount);
            holder.ridedate = (TextView) view.findViewById(R.id.pmt_dtl_list_date);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        holder.rideid.setText(data.get(position).getride_id());
        holder.amount.setText(data.get(position).getamount());
        holder.ridedate.setText(data.get(position).getride_date());
        return view;
    }


}

