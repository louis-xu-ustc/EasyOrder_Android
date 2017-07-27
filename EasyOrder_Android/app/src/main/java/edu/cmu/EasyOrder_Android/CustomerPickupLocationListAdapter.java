package edu.cmu.EasyOrder_Android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by yunpengx on 7/8/17.
 */

public class CustomerPickupLocationListAdapter extends ArrayAdapter<PickupLocation> {
    ArrayList<PickupLocation> pickupLocationArrayList = new ArrayList<>();

    public CustomerPickupLocationListAdapter(Context context, int textViewResourceId, ArrayList<PickupLocation> objects) {
        super(context, textViewResourceId, objects);
        pickupLocationArrayList = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.customer_pickup_location_list_view, null);
        PickupLocation pickupLocation = pickupLocationArrayList.get(pos);

        TextView location = (TextView) v.findViewById(R.id.customer_pickup_location);
        location.setText(pickupLocation.getLocaiton());

        TextView ETA = (TextView) v.findViewById(R.id.customer_pickup_location_ETA);
        ETA.setText(String.valueOf(pickupLocation.getETA()));

        return v;
    }
}
