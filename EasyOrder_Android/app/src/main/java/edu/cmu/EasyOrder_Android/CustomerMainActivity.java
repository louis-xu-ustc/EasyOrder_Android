package edu.cmu.EasyOrder_Android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInResult;

import org.json.JSONException;
import org.json.JSONObject;

import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_USER_ID;

public class CustomerMainActivity extends AppCompatActivity implements
        CustomerMapFragment.OnFragmentInteractionListener,
        CustomerPaymentFragment.OnFragmentInteractionListener,
        CustomerPostFragment.OnFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Utils.REQUEST_PAYMENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                postPaymentNonce(result.getPaymentMethodNonce().getNonce());
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // the user canceled
                Toast.makeText(getApplicationContext(), "User Cancelled Transaction", Toast.LENGTH_SHORT).show();
            } else {
                // handle errors here, an exception may be available in
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void postPaymentNonce(String nonce) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Long id = pref.getLong(PREFERENCE_TWITTER_USER_ID, 0);
        String twitterID = id.toString();

        JSONObject input = new JSONObject();
        try {
            input.put("user_id", twitterID);
            input.put("payment_method_nonce", nonce);
        } catch (JSONException eJson) {
            Log.d("Post Payment Nonce", "Json input parse error");
        }

        Response.Listener<JSONObject> checkoutCallback = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                // TODO: Disable Pay Button or change UI to end all transaction
                Button payButton = (Button) findViewById(R.id.customer_pay_button);
                payButton.setEnabled(false);
            }
        };

        RESTAPI.getInstance(getApplicationContext())
                .makeRequest(Utils.API_BASE + "/payment/checkout/android/",
                        Request.Method.POST,
                        input,
                        checkoutCallback,
                        null);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //currently do nothing
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_customer_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.customer_setting_profile_button:
                Intent customerProfile = new Intent(CustomerMainActivity.this, CustomerProfileActivity.class);
                startActivity(customerProfile);
                finish();
                return true;
            case R.id.customer_setting_logout_button:
                // TODO
                Toast.makeText(CustomerMainActivity.this, "Log out of customer account, " +
                        "further operations to handle authentication data", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.customer_fragment_main, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new CustomerPostFragment();
                case 1:
                    return new CustomerMapFragment();
                case 2:
                    return new CustomerPaymentFragment();
                default:
                    break;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Post";
                case 1:
                    return "Map";
                case 2:
                    return "Payment";
            }
            return null;
        }
    }
}
