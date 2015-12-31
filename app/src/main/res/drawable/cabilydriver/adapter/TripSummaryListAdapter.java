package drawable.cabilydriver.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.dao.Ride;
import com.app.dao.TripDetails;
import com.cabily.cabilydriver.R;

import java.util.ArrayList;

/**
 * Created by user14 on 9/22/2015.
 */
public class TripSummaryListAdapter extends BaseAdapter {

    private ArrayList<Ride> data;
    private LayoutInflater mInflater;
    private Activity context;
    private String check;

    public TripSummaryListAdapter(Activity c) {
        context = c;
        mInflater = LayoutInflater.from(context);
    }

    public void setDao(TripDetails tripDetails) {
        data = (ArrayList<Ride>) tripDetails.getResponse().getRides();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
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
        private TextView trip_summery_tvaddress;
        private TextView trip_summery_tvprice;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.trip_summary_list, parent, false);
            holder = new ViewHolder();
            holder.trip_summery_tvaddress = (TextView) view.findViewById(R.id.trip_summary_list_address);
            holder.trip_summery_tvprice = (TextView) view.findViewById(R.id.trip_summary_list_date_and_time);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        holder.trip_summery_tvaddress.setText(data.get(position).getPickup());
        holder.trip_summery_tvprice.setText(data.get(position).getRideTime());
        return view;
    }

}