package edu.cmu.EasyOrder_Android;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static edu.cmu.EasyOrder_Android.Utils.DBG;

/**
 * Created by yunpengx on 7/8/17.
 */

public class CustomerDishListAdapter extends ArrayAdapter<Dish> {
    ArrayList<Dish> dishArrayList = new ArrayList<>();

    public CustomerDishListAdapter(Context context, int textViewResourceId, ArrayList<Dish> objects) {
        super(context, textViewResourceId, objects);
        dishArrayList = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        View view = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.customer_dish_list_view, null);
        final Dish dish = dishArrayList.get(pos);

        ImageView imageView = (ImageView) view.findViewById(R.id.customer_dish_image);
        imageView.setImageResource(R.drawable.default_dish_icon);
        TextView dishName = (TextView) view.findViewById(R.id.customer_dish_name);
        dishName.setText(dish.getName());
        TextView dishPrice = (TextView) view.findViewById(R.id.customer_dish_price);
        dishPrice.setText(new StringBuilder().append("$ ").append(String.valueOf(dish.getPrice())).toString());
        final TextView dishQuantity = (TextView) view.findViewById(R.id.customer_dish_quantity);
        dishQuantity.setText(String.valueOf(dish.getQuantity()));

        // add one more dish
        Button addButton = (Button) view.findViewById(R.id.customer_dish_add_button);
        addButton.setTag(dishQuantity);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dish.getQuantity() < Integer.MAX_VALUE) {
                    dish.setQuantity(dish.getQuantity() + 1);
                }
                TextView localDishQuantity = (TextView) v.getTag();
                localDishQuantity.setText(String.valueOf(dish.getQuantity()));
            }
        });

        // minus one dish
        Button minusButton = (Button) view.findViewById(R.id.customer_dish_minus_button);
        minusButton.setTag(dishQuantity);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dish.getQuantity() > 0) {
                    dish.setQuantity(dish.getQuantity() - 1);
                }
                TextView localDishQuantity = (TextView) v.getTag();
                localDishQuantity.setText(String.valueOf(dish.getQuantity()));
            }
        });

        // rating bar
        final RatingBar ratingBar = (RatingBar) view.findViewById(R.id.customer_ratingBar);
        ratingBar.setIsIndicator(false);
        ratingBar.setRating(dish.getRate());
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                // TODO
                Toast.makeText(getContext(), "Further backend operations for curr rating: " + rating, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
