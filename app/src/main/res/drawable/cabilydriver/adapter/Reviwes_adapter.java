package drawable.cabilydriver.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cabily.cabilydriver.Pojo.PaymentdetailsPojo;
import com.cabily.cabilydriver.Pojo.Reviwes_Pojo;
import com.cabily.cabilydriver.R;

import java.util.ArrayList;

/**
 * Created by user88 on 11/6/2015.
 */
public class Reviwes_adapter extends BaseAdapter {
    private ArrayList<Reviwes_Pojo> data;
    private LayoutInflater mInflater;
    private Activity context;
    private String check;
    public String Srating="";

    public Reviwes_adapter(Activity c, ArrayList<Reviwes_Pojo> d) {
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
        private TextView rating_text;
        private RatingBar rating;

    }


        @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.rating_page, parent, false);
            holder = new ViewHolder();
            holder.rating_text = (TextView) view.findViewById(R.id.rating_page_tv);
            holder.rating = (RatingBar) view.findViewById(R.id.rating_page_behaviour_rating_bar);

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

           holder.rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
               @Override
               public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                   Srating = String.valueOf(rating);
                   data.get(position).setRatings_count(Srating);

               }
           });

            holder.rating_text.setText(data.get(position).getOptions_title());







        return view;
    }
}
