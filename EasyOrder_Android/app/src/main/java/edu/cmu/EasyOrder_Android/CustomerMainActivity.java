package edu.cmu.EasyOrder_Android;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import static edu.cmu.EasyOrder_Android.Utils.DBG;
import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_LOGGED_IN;
import static edu.cmu.EasyOrder_Android.Utils.TAG;

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
    private double actionStartY;

    private Long notificationTimestamp = 0L;
    private Boolean mHasNotified = false;
    private Context mContext;

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
        mContext = getApplicationContext();
        pollNotification();
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
                return true;
            case R.id.customer_setting_logout_button:
                Toast.makeText(CustomerMainActivity.this, "Log out of customer account", Toast.LENGTH_SHORT).show();
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor edit = pref.edit();
                edit.putBoolean(PREFERENCE_TWITTER_LOGGED_IN, false);
                edit.apply();
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

    private void pollNotification() {
        Response.Listener<JSONObject> tokenCallback = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Boolean notify = response.getBoolean("notification");
                    if (notify) {
                        String content = response.getString("content");
                        Long timestamp = response.getLong("modified_at");
                        Log.d(TAG, "notification: " + notify + " content: " + content + " timestamp: " + timestamp);
                        notificationTimestamp = timestamp;

                        // Get a notification builder that's compatible with platform versions >= 4
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
                        // Define the notification settings.
                        builder.setSmallIcon(R.drawable.ic_launcher)
                                // In a real app, you may want to use a library like Volley
                                // to decode the Bitmap.
                                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                                        R.drawable.ic_launcher))
                                .setColor(Color.RED)
                                .setContentTitle("Geofencing")
                                .setContentText(content);

                        // Dismiss notification once the user touches it.
                        builder.setAutoCancel(true);

                        // Get an instance of the Notification manager
                        NotificationManager mNotificationManager =
                                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

                        // Issue the notification
                        mNotificationManager.notify(0, builder.build());
                        mHasNotified = true;
                    }
                } catch (JSONException eJson) {
                    Log.d("Customer Tab 2", "Json Parse Error");
                }

                if (!mHasNotified) {
                    Thread newPoll = new Thread(new Runnable() {
                        public void run() {
                            try {
                                // FIXME 20s to check notification
                                Thread.sleep(20000);
                                pollNotification();
                            } catch (InterruptedException e) {
                                Log.d(DBG, e.getMessage());
                            }
                        }
                    });
                    newPoll.start();
                }
            }
        };

        String URL = Utils.API_BASE + "/notification/";
        if (notificationTimestamp != 0L) {
            URL += notificationTimestamp;
            URL += "/";
        }

        RESTAPI.getInstance(mContext)
                .makeRequest(URL,
                        Request.Method.GET,
                        null,
                        tokenCallback,
                        null);
    }
}
