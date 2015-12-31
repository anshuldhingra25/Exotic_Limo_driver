package drawable.cabilydriver.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cabily.cabilydriver.Pojo.PaymentdetailsPojo;
import com.cabily.cabilydriver.R;

import java.util.ArrayList;

/**
 * Created by user14 on 9/22/2015.
 */
public class PaymentDetailsAdapter extends BaseAdapter {

    private ArrayList<PaymentdetailsPojo> data;
    private LayoutInflater mInflater;
    private Activity context;
    private String check;

    public PaymentDetailsAdapter(Activity c, ArrayList<PaymentdetailsPojo> d) {
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
        private TextView payment;
        private TextView amount;
        private TextView receiveddate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        String data1 = " ";
        if (convertView == null) {
            view = mInflater.inflate(R.layout.payment_details_single, parent, false);
            holder = new ViewHolder();
            holder.payment = (TextView) view.findViewById(R.id.pmt_stmt_payment);
            holder.amount = (TextView) view.findViewById(R.id.pmt_stmt_amount);
            holder.receiveddate = (TextView) view.findViewById(R.id.pmt_stmt_received_date);

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        data1 = data.get(position).getpay_duration_from() + " to " + data.get(position).getpay_duration_to();

        holder.payment.setText(data1);
        holder.amount.setText(data.get(position).getamount());
        holder.receiveddate.setText(data.get(position).getpay_date());

        return view;
    }


}
