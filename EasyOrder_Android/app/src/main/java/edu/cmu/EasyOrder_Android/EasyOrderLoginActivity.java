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

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_ACCESS_TOKEN;
import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_ACCESS_TOKEN_SECRET;
import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_LOGGED_IN;
import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_USER_ID;
import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_USER_IMAGE_URL;
import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_USER_NAME;
import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_USER_SCREEN_NAME;
import static edu.cmu.EasyOrder_Android.Utils.TAG;
import static edu.cmu.EasyOrder_Android.Utils.TWITTER_CONSUMER_KEY;
import static edu.cmu.EasyOrder_Android.Utils.TWITTER_CONSUMER_SECRET;

/**
 * Created by yunpengx on 7/8/17.
 */

public class EasyOrderLoginActivity extends Activity {


    private Dialog auth_dialog;
    private WebView web;
    private SharedPreferences pref;

    private Twitter twitter;
    private RequestToken requestToken = null;
    private AccessToken accessToken;
    private String oauth_url = "", oauth_verifier = "";

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

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(pref.getString("CONSUMER_KEY", ""))
                .setOAuthConsumerSecret(pref.getString("CONSUMER_SECRET", ""))
                .setOAuthAccessToken(null)
                .setOAuthAccessTokenSecret(null);
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();

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
                edit.putString(PREFERENCE_TWITTER_ACCESS_TOKEN, accessToken.getToken());
                edit.putString(PREFERENCE_TWITTER_ACCESS_TOKEN_SECRET, accessToken.getTokenSecret());
                edit.putBoolean(PREFERENCE_TWITTER_LOGGED_IN, true);

                User user = twitter.showUser(accessToken.getUserId());
                edit.putLong(PREFERENCE_TWITTER_USER_ID, user.getId());
                edit.putString(PREFERENCE_TWITTER_USER_NAME, user.getName());
                edit.putString(PREFERENCE_TWITTER_USER_SCREEN_NAME, user.getScreenName());
                edit.putString(PREFERENCE_TWITTER_USER_IMAGE_URL, user.getOriginalProfileImageURL());
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
        Long userID = pref.getLong(PREFERENCE_TWITTER_USER_ID, 0);
        String userName = pref.getString(PREFERENCE_TWITTER_USER_NAME, "");
        String userScreenName = pref.getString(PREFERENCE_TWITTER_USER_SCREEN_NAME, "");
        String userImage = pref.getString(PREFERENCE_TWITTER_USER_IMAGE_URL, "");


        if (isRetailerLoginButtonClicked) {
            isRetailerLoginButtonClicked = false;

            createUserInServer(userID, userName);

            Toast.makeText(this.getApplicationContext(), "retailer login successful, further operations\n" +
                    "user ID: " + userID + "\n" +
                    "user name: " + userName + "\n" +
                    "screen name: " + userScreenName, Toast.LENGTH_SHORT).show();

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

            createUserInServer(userID, userName);

            Toast.makeText(this.getApplicationContext(), "customer login successful, further operations\n" +
                    "user ID: " + userID + "\n" +
                    "user name: " + userName + "\n" +
                    "screen name: " + userScreenName, Toast.LENGTH_SHORT).show();

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

    private void createUserInServer(Long userID, String name) {

        JSONObject user = new JSONObject();
        try {
            user.put("twitterID", userID.toString());
            user.put("name", name);
        } catch (JSONException eJson) {
            Log.d("Login Activity", "JSON Add data Error");
            return;
        }

        Response.Listener<JSONObject> orderCallback = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Do Nothing
            }
        };

        RESTAPI.getInstance(getApplicationContext())
                .makeRequest(Utils.API_BASE + "/user/",
                        Request.Method.POST,
                        user,
                        orderCallback,
                        null);
    }
}
