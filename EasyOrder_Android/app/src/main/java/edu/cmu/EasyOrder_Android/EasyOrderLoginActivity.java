package edu.cmu.EasyOrder_Android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by yunpengx on 7/8/17.
 */

public class EasyOrderLoginActivity extends Activity {

    Button retailerLoginButton, customerLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.easy_order_login);

        retailerLoginButton = (Button) findViewById(R.id.retailer_login_button);
        customerLoginButton = (Button) findViewById(R.id.customer_login_button);

        retailerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent retailerIntent = new Intent(getApplicationContext(), RetailerMainActivity.class);
                startActivity(retailerIntent);
            }
        });

        customerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customerIntent = new Intent(getApplicationContext(), CustomerMainActivity.class);
                startActivity(customerIntent);
            }
        });
    }
}
