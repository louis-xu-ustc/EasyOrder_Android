package edu.cmu.EasyOrder_Android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class RetailerProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retailer_profile_view);
        ImageView retailerImage = (ImageView) findViewById(R.id.retailer_profile_image);
        // TODO use data from database
        retailerImage.setImageResource(R.drawable.default_avartar);
        TextView retailerName = (TextView) findViewById(R.id.retailer_profile_name);
        // TODO use data from database
        retailerName.setText("Fulai");
    }
}