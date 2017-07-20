package edu.cmu.EasyOrder_Android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by yunpengx on 7/8/17.
 */

public class RetailerOrderListAdapter extends ArrayAdapter<Order> {
    ArrayList<Order> orderArrayList = new ArrayList<>();

    public RetailerOrderListAdapter(Context context, int textViewResourceId, ArrayList<Order> objects) {
        super(context, textViewResourceId, objects);
        orderArrayList = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.retailer_order_list_view, null);
        Order order = orderArrayList.get(pos);

        TextView userName = (TextView) v.findViewById(R.id.retailer_user_name);
        userName.setText(order.getUserName());
        Switch switchButton = (Switch) v.findViewById(R.id.retailer_switch_button);
        switchButton.setChecked(order.getIfNofity());

        return v;
    }
}
