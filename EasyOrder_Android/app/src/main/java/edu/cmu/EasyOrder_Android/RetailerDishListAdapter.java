package edu.cmu.EasyOrder_Android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by yunpengx on 7/8/17.
 */

public class RetailerDishListAdapter extends ArrayAdapter<Dish> {
    ArrayList<Dish> dishArrayList = new ArrayList<>();

    public RetailerDishListAdapter(Context context, int textViewResourceId, ArrayList<Dish> objects) {
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
        v = inflater.inflate(R.layout.retailer_dish_list_view, null);
        Dish dish = dishArrayList.get(pos);

        ImageView imageView = (ImageView) v.findViewById(R.id.retailer_dish_image);
        imageView.setImageResource(R.drawable.default_dish_icon);

        TextView dishName = (TextView) v.findViewById(R.id.retailer_dish_name);
        dishName.setText(dish.getName());

        TextView dishPrice = (TextView) v.findViewById(R.id.retailer_dish_price);
        dishPrice.setText(new StringBuilder().append("$ ").append(String.valueOf(dish.getPrice())).toString());

        TextView dishQuantity = (TextView) v.findViewById(R.id.retailer_dish_quantity);
        dishQuantity.setText(String.valueOf(dish.getQuantity()));

        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.retailer_ratingBar);
        ratingBar.setIsIndicator(true);
        ratingBar.setRating((float)dish.getRate());

        return v;
    }
}
