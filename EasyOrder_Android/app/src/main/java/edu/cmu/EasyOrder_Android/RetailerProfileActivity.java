package edu.cmu.EasyOrder_Android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_USER_IMAGE_URL;
import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_USER_NAME;

public class RetailerProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retailer_profile_view);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String imageURL = pref.getString(PREFERENCE_TWITTER_USER_IMAGE_URL, "");
        String username = pref.getString(PREFERENCE_TWITTER_USER_NAME, "Anonymous");

        TextView mProfileName = (TextView) findViewById(R.id.retailer_profile_name);
        mProfileName.setText(username);

        ImageView retailerImage = (ImageView) findViewById(R.id.retailer_profile_image);
        try {
            Picasso.with(getApplicationContext())
                    .load(imageURL)
                    .placeholder(R.drawable.default_avartar)
                    .into(retailerImage);
        } catch (Exception e) {
            String msg = e.getMessage();
            Log.d("Load Dish Photo", msg);
        }
    }
}