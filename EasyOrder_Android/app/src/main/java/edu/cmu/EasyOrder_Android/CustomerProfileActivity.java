package edu.cmu.EasyOrder_Android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class CustomerProfileActivity extends AppCompatActivity {

    private ArrayAdapter historyOrderAdapter;
    private ListView mListView;
    ArrayList<Dish> historyOrderArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile_view);
        historyOrderArrayList = new ArrayList<>();
        //FIXME
        fillFakeHistoryOrderArrayList();

        mListView = (ListView) findViewById(R.id.customer_history_order);
        historyOrderAdapter = new CustomerHistoryOrderAdapter(getApplicationContext(), R.layout.customer_history_order_view, historyOrderArrayList);
        mListView.setAdapter(historyOrderAdapter);
    }

    private void fillFakeHistoryOrderArrayList() {
        Dish dish1 = new Dish();
        dish1.setName("pizza");
        dish1.setPrice(10);
        //FIXME
//        dish1.setImage(ContextCompat.getDrawable(getContext(),R.drawable.pizza).toString());
        dish1.setQuantity(1);
        dish1.setRate(3);
        historyOrderArrayList.add(dish1);

        Dish dish2 = new Dish();
        dish2.setName("salad");
        dish2.setPrice(12);
        // FIXME
//        dish2.setImage(ContextCompat.getDrawable(getContext(),R.drawable.salad).toString());
        dish2.setQuantity(0);
        dish2.setRate(4);
        historyOrderArrayList.add(dish2);

        Dish dish3 = new Dish();
        dish3.setName("fish & chips");
        dish3.setPrice(0);
        // FIXME
//        dish2.setImage(ContextCompat.getDrawable(getContext(),R.drawable.salad).toString());
        dish3.setQuantity(0);
        dish3.setRate(2);
        historyOrderArrayList.add(dish3);
    }
}
