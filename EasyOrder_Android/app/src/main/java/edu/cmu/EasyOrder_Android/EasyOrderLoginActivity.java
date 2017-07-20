package edu.cmu.EasyOrder_Android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import static edu.cmu.EasyOrder_Android.Utils.DBG;

/**
 * Created by yunpengx on 7/8/17.
 */

public class EasyOrderLoginActivity extends Activity {

    private static final String TWITTER_CONSUMER_KEY = BuildConfig.CONSUMER_KEY;
    private static final String TWITTER_CONSUMER_SECRET = BuildConfig.CONSUMER_SECRET;
    private static final String TWITTER_ACCESS_TOKEN = BuildConfig.ACCESS_TOKEN;
    private static final String TWITTER_ACCESS_TOKEN_SECRET = BuildConfig.ACCESS_TOKEN_SECRET;

    private static final String TAG = "DBG";
    public static String PREFERENCE_TWITTER_LOGGED_IN = "TWITTER_LOGGED_IN";
    public static String TWITTER_USER_NAME = "NAME";
    public static String TWITTER_USER_SCREEN_NAME = "SCREEN_NAME";
    public static String TWITTER_USER_IMAGE_URL = "IMAGE_URL";

    private Dialog auth_dialog;
    private WebView web;
    private SharedPreferences pref;

    private Twitter twitter;
    private RequestToken requestToken;
    private AccessToken accessToken;
    private String oauth_url, oauth_verifier;

    private Button retailerLoginButton, customerLoginButton;
    private boolean isRetailerLoginButtonClicked, isCustomerLoginButtonClicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.easy_order_login);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor edit = pref.edit();
        edit.putString("CONSUMER_KEY", TWITTER_CONSUMER_KEY);
        edit.putString("CONSUMER_SECRET", TWITTER_CONSUMER_SECRET);
        edit.commit();

        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(pref.getString("CONSUMER_KEY", ""), pref.getString("CONSUMER_SECRET", ""));

        retailerLoginButton = (Button) findViewById(R.id.retailer_login_button);
        customerLoginButton = (Button) findViewById(R.id.customer_login_button);
        isRetailerLoginButtonClicked = false;
        isCustomerLoginButtonClicked = false;

        retailerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRetailerLoginButtonClicked = true;
                checkIn();
            }
        });

        customerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCustomerLoginButtonClicked = true;
                checkIn();

            }
        });
    }

    private void checkIn() {
        if (!pref.getBoolean(PREFERENCE_TWITTER_LOGGED_IN, false)) {
            new TokenGet().execute(); //no Token obtained, first time use
        } else {
            loginInEasyOrder();
        }
    }

    private class TokenGet extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... args) {
            try {
                requestToken = twitter.getOAuthRequestToken();
                oauth_url = requestToken.getAuthorizationURL();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return oauth_url;
        }

        @Override
        protected void onPostExecute(String oauth_url) {
            if (oauth_url != null) {
                auth_dialog = new Dialog(EasyOrderLoginActivity.this);
                auth_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                auth_dialog.setContentView(R.layout.oauth_webview);
                web = (WebView) auth_dialog.findViewById(R.id.webViewOAuth);
                web.getSettings().setJavaScriptEnabled(true);
                web.loadUrl(oauth_url);
                web.setWebViewClient(new WebViewClient() {
                    boolean authComplete = false;

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if (url.contains("oauth_verifier") && authComplete == false) {
                            authComplete = true;
                            Uri uri = Uri.parse(url);
                            oauth_verifier = uri.getQueryParameter("oauth_verifier");
                            auth_dialog.dismiss();
                            new AccessTokenGet().execute();
                        } else if (url.contains("denied")) {
                            auth_dialog.dismiss();
                            Toast.makeText(getBaseContext(), "Sorry !, Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Log.d(TAG, auth_dialog.toString());
                auth_dialog.show();
                auth_dialog.setCancelable(true);

            } else {
                Toast.makeText(getBaseContext(), "Sorry !, Error or Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class AccessTokenGet extends AsyncTask<String, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... args) {
            try {
                accessToken = twitter.getOAuthAccessToken(requestToken, oauth_verifier);
                SharedPreferences.Editor edit = pref.edit();
                edit.putString("ACCESS_TOKEN", accessToken.getToken());
                edit.putString("ACCESS_TOKEN_SECRET", accessToken.getTokenSecret());
                edit.putBoolean(PREFERENCE_TWITTER_LOGGED_IN, true);

                User user = twitter.showUser(accessToken.getUserId());
                edit.putString(TWITTER_USER_NAME, user.getName());
                edit.putString(TWITTER_USER_SCREEN_NAME, user.getScreenName());
                edit.putString(TWITTER_USER_IMAGE_URL, user.getOriginalProfileImageURL());
                edit.commit();
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean response) {
            if (response) {
                // if response correctly, then new Intent
                loginInEasyOrder();
            }
        }
    }

    private void loginInEasyOrder() {
        String userName = pref.getString(TWITTER_USER_NAME, "");
        String userScreenName = pref.getString(TWITTER_USER_SCREEN_NAME, "");
        String userImage = pref.getString(TWITTER_USER_IMAGE_URL, "");


        if (isRetailerLoginButtonClicked) {
            isRetailerLoginButtonClicked = false;
            //TODO
            Toast.makeText(this.getApplicationContext(), "retailer login successful, further operations\n" +
                    "user name: " + userName + "\n" +
                    "screen name: " + userScreenName, Toast.LENGTH_LONG).show();

            // make sure intent starts after toast finishes
            final Intent retailerIntent = new Intent(getApplicationContext(), RetailerMainActivity.class);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500); // As I am using LENGTH_LONG in Toast
                        startActivity(retailerIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }

        if (isCustomerLoginButtonClicked) {
            isCustomerLoginButtonClicked = false;
            //TODO
            Toast.makeText(this.getApplicationContext(), "customer login successful, further operations\n" +
                    "user name: " + userName + "\n" +
                    "screen name: " + userScreenName, Toast.LENGTH_LONG).show();

            // make sure intent starts after toast finishes
            final Intent customerIntent = new Intent(getApplicationContext(), CustomerMainActivity.class);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500); // As I am using LENGTH_LONG in Toast
                        startActivity(customerIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }
}
