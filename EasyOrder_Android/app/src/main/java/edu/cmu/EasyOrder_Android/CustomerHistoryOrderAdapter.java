package edu.cmu.EasyOrder_Android;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by yunpengx on 7/8/17.
 */

public class CustomerHistoryOrderAdapter extends ArrayAdapter<Dish> {
    ArrayList<Dish> dishArrayList = new ArrayList<>();

    public CustomerHistoryOrderAdapter(Context context, int textViewResourceId, ArrayList<Dish> objects) {
        super(context, textViewResourceId, objects);
        dishArrayList = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.customer_history_order_view, null);
        Dish dish = dishArrayList.get(pos);

        ImageView imageView = (ImageView) v.findViewById(R.id.history_order_dish_image);
        try {
            Picasso.with(getContext())
                    .load(Utils.BACKEND_SERVER + dish.getImage())
                    .placeholder(R.drawable.default_dish_icon) //optional
                    .into(imageView);                        //Your image view object.
        } catch (Exception e) {
            String msg = e.getMessage();
            Log.d("Load Dish Photo", msg);
        }

        TextView dishName = (TextView) v.findViewById(R.id.history_order_dish_name);
        dishName.setText(dish.getName());
        TextView dishPrice = (TextView) v.findViewById(R.id.history_order_dish_price);
        dishPrice.setText(new StringBuilder().append("$ ").append(String.valueOf(dish.getPrice())).toString());
        TextView dishQuantity = (TextView) v.findViewById(R.id.history_order_dish_quantity);
        dishQuantity.setText(String.valueOf(dish.getQuantity()));

        return v;
    }
}
