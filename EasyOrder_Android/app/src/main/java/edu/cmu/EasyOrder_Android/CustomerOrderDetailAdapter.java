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

public class CustomerOrderDetailAdapter extends ArrayAdapter<Dish> {
    ArrayList<Dish> orderDetailDishArrayList = new ArrayList<>();

    public CustomerOrderDetailAdapter(Context context, int textViewResourceId, ArrayList<Dish> objects) {
        super(context, textViewResourceId, objects);
        orderDetailDishArrayList = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.customer_order_detail_list_view, null);
        Dish dish = orderDetailDishArrayList.get(pos);

        TextView dishName = (TextView) v.findViewById(R.id.customer_order_detail_dish_name);
        dishName.setText(dish.getName());

        TextView dishQuantity = (TextView) v.findViewById(R.id.customer_order_detail_dish_quantity);
        dishQuantity.setText(String.valueOf(dish.getQuantity()));

        return v;
    }
}
