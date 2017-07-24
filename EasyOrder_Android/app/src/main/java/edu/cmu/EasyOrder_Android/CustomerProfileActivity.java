package edu.cmu.EasyOrder_Android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_USER_ID;
import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_USER_IMAGE_URL;

public class CustomerProfileActivity extends AppCompatActivity {

    private ArrayAdapter historyOrderAdapter;
    private ListView mListView;
    ArrayList<Dish> historyOrderArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile_view);
        historyOrderArrayList = new ArrayList<>();
        fetchOrderDetail();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String imageURL = pref.getString(PREFERENCE_TWITTER_USER_IMAGE_URL, "");
        ImageView mProfileImage = (ImageView) findViewById(R.id.customer_profile_image);
        try {
            Picasso.with(getApplicationContext())
                    .load(imageURL)
                    .placeholder(R.drawable.default_avartar)
                    .into(mProfileImage);
        } catch (Exception e) {
            String msg = e.getMessage();
            Log.d("Load Dish Photo", msg);
        }

        mListView = (ListView) findViewById(R.id.customer_history_order);
        historyOrderAdapter = new CustomerHistoryOrderAdapter(getApplicationContext(), R.layout.customer_history_order_view, historyOrderArrayList);
        mListView.setAdapter(historyOrderAdapter);
    }

    private void fetchOrderDetail() {
        Response.Listener<JSONArray> orderCallback = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    // must use the same dishArrayList, otherwise notifyDatasetChanged cannot be useful
                    historyOrderArrayList.clear();

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject curOrder = (JSONObject) response.get(i);
                        Dish curDish = new Dish();
                        curDish.setName(curOrder.getString("dish"));
                        curDish.setPrice(curOrder.getDouble("price"));
                        curDish.setQuantity(curOrder.getInt("amount"));
                        curDish.setImage(curOrder.getString("photo"));
                        historyOrderArrayList.add(curDish);
                    }

                    historyOrderAdapter.notifyDataSetChanged();
                } catch (JSONException eJson) {
                    Log.d("Customer Profile", eJson.getMessage());
                }
            }
        };

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Long id = pref.getLong(PREFERENCE_TWITTER_USER_ID, 0);
        String twitterID = id.toString();
        RESTAPI.getInstance(getApplication().getApplicationContext())
                .makeRequest(Utils.API_BASE + "/order/history/" + twitterID + "/",
                        Request.Method.GET,
                        null,
                        orderCallback,
                        null);

    }
}
