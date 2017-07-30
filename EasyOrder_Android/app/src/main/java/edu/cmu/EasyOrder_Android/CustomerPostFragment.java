package edu.cmu.EasyOrder_Android;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import static edu.cmu.EasyOrder_Android.RetailerMapFragment.DISPLAY_UI_DIALOG;
import static edu.cmu.EasyOrder_Android.RetailerMapFragment.DISPLAY_UI_TOAST;
import static edu.cmu.EasyOrder_Android.Utils.DBG;
import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_LOGGED_IN;
import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_USER_ID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomerPostFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CustomerPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomerPostFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ArrayAdapter dishAdapter;
    private ListView mListView;
    private ArrayList<Dish> dishArrayList;

    private Dialog auth_dialog;
    private WebView web;
    private SharedPreferences pref;
    private Twitter twitter;
    private RequestToken requestToken;
    private AccessToken accessToken;
    private String oauth_url, oauth_verifier, profile_url;
    private UIHandler uiHandler;
    private String imageUrl;

    public CustomerPostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CustomerPostFragment.
     */
    public static CustomerPostFragment newInstance(String param1, String param2) {
        CustomerPostFragment fragment = new CustomerPostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        dishArrayList = new ArrayList<>();
        fetchDishInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.customer_fragment_post, container, false);
        mListView = (ListView) rootView.findViewById(R.id.customer_dish_list);
        dishAdapter = new CustomerDishListAdapter(getContext(), R.layout.customer_dish_list_view, dishArrayList);
        mListView.setAdapter(dishAdapter);

        mListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
//                        Toast.makeText(getContext(), "click item, id: " + String.valueOf(id) + " pos: " +
//                                String.valueOf(position), Toast.LENGTH_SHORT).show();

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(true);
                        builder.setTitle("Post Twitter ?");
                        Dish dish = (Dish) dishAdapter.getItem(position);
                        imageUrl = Utils.BACKEND_SERVER + dish.getImage();
                        final String message = "@08723Mapp [Team7] " + dish.getName() + " is really delicious!";
                        builder.setMessage(message);
                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                postTwitterMessage(message);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                        return true;
                    }
                });

        ImageButton shoppingCart = (ImageButton) rootView.findViewById(R.id.customer_order_confirm_button);
        View convertView = (View) inflater.inflate(R.layout.customer_shopping_cart_confirm_list, null);
        ListView lv = (ListView) convertView.findViewById(R.id.customer_order_confirm_list);
        shoppingCart.setTag(R.string.first_tag, convertView);
        shoppingCart.setTag(R.string.second_tag, lv);
        shoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> orderedList = new ArrayList<String>();
                final boolean hasEffectiveOrder = false;
                for (Dish dish : dishArrayList) {
                    if (dish.getQuantity() > 0) {
                        orderedList.add(dish.toString());
                    }
                }
                String[] dishInfo = new String[orderedList.size()];
                dishInfo = orderedList.toArray(dishInfo);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View vv = (View) v.getTag(R.string.first_tag);
                // fix the specified child already has a parent bug
                if (vv.getParent() != null) {
                    ((ViewGroup) vv.getParent()).removeView(vv);
                }
                builder.setView(vv);
                builder.setTitle("Order Detail:");
                ListView lv = (ListView) v.getTag(R.string.second_tag);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dishInfo);
                lv.setAdapter(adapter);
                builder.setCancelable(false);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        placeOrder();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void postTwitterMessage(String message) {
        if (!pref.getBoolean(PREFERENCE_TWITTER_LOGGED_IN, false)) {
            new TokenGet().execute(); //no Token obtained, first time use
        } else {
            new PostTweet().execute(message); //when Tokens are obtained , ready to Post
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
                auth_dialog = new Dialog(getContext());
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
                            Toast.makeText(getContext(), "Sorry !, Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Log.d(DBG, auth_dialog.toString());
                auth_dialog.show();
                auth_dialog.setCancelable(true);
            } else {
                Toast.makeText(getContext(), "Sorry !, Error or Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class PostTweet extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(pref.getString("CONSUMER_KEY", ""));
            builder.setOAuthConsumerSecret(pref.getString("CONSUMER_SECRET", ""));
            AccessToken accessToken = new AccessToken(pref.getString("ACCESS_TOKEN", ""), pref.getString("ACCESS_TOKEN_SECRET", ""));
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
            String message = args[0];
            // if entire message is lengthier than 140 characters
            message = message.substring(0, Math.min(message.length(), 140));
            StatusUpdate status = new StatusUpdate(message);
            if (imageUrl != null) {
                try {
                    URL url = new URL(imageUrl);
                    URLConnection urlConnection = url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    status.setMedia("image.png", in);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d(DBG, "twitter to post: " + status);
            try {
                twitter4j.Status response = twitter.updateStatus(status);
                return response.toString();
            } catch (TwitterException te) {
                te.printStackTrace();
                HandlerThread uiThread = new HandlerThread("UIHandler");
                uiThread.start();
                uiHandler = new UIHandler(uiThread.getLooper());

                int errorCode = te.getErrorCode();
                String errorMessage;
                if (errorCode == 401) {
                    // case 1. If no access token granted
                    errorMessage = "Not granted";
                    handleUIRequest(DISPLAY_UI_DIALOG, null);
                } else if (errorCode == 187) {
                    // case 2. in case of sending duplicated message
                    // --> parse Error code and display error message using Toast
                    errorMessage = "This twitter message is duplicated, please check your twitter stream!";
                    handleUIRequest(DISPLAY_UI_TOAST, errorMessage);

                } else {
                    // other case: in case of error--> parse Error message and display proper error message using Toast
                    errorMessage = te.getErrorMessage();
                    handleUIRequest(DISPLAY_UI_TOAST, errorMessage);
                }
                Log.e("ERR", "errorCode: " + errorCode + " errorMessage: " + errorMessage);
                return null;
            }
        }

        protected void onPostExecute(String res) {
            if (res != null) {
                //progress.dismiss();
                // case 3. in case of Success -> display success message using Toast
                Toast.makeText(getContext(), "Tweet successfully Posted", Toast.LENGTH_SHORT).show();
            } else {
                //progress.dismiss();
                Toast.makeText(getContext(), "Error while tweeting !", Toast.LENGTH_SHORT).show();
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
                profile_url = user.getOriginalProfileImageURL();
                edit.putString("NAME", user.getName());
                edit.putString("IMAGE_URL", user.getOriginalProfileImageURL());
                edit.commit();
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean response) {
            if (response) {
                //progress.hide(); after login, tweet Post right away
                new PostTweet().execute();
            }
        }
    }

    private final class UIHandler extends Handler {
        public UIHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISPLAY_UI_TOAST: {
                    Context context = getContext();
                    Toast t = Toast.makeText(context, (String) msg.obj, Toast.LENGTH_LONG);
                    t.show();
                }
                break;
                case DISPLAY_UI_DIALOG:
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("You are not granted!");
                    alertDialog.setMessage("Please login in to Twitter first!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    break;
                default:
                    break;
            }
        }
    }

    protected void handleUIRequest(int id, String message) {
        Message msg = uiHandler.obtainMessage(id);
        msg.obj = message;
        uiHandler.sendMessage(msg);
    }

    private void placeOrder() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        Long id = pref.getLong(PREFERENCE_TWITTER_USER_ID, 0);
        String twitterID = id.toString();

        JSONObject input = new JSONObject();
        JSONArray orders = new JSONArray();
        boolean hasEffectiveOrder = false;
        try {
            input.put("twitterID", twitterID);
            for (Dish dish : dishArrayList) {
                if (dish.getQuantity() > 0) {
                    JSONObject jsonDish = new JSONObject();
                    jsonDish.put("dish", dish.getId());
                    jsonDish.put("amount", dish.getQuantity());
                    orders.put(jsonDish);
                    hasEffectiveOrder = true;
                }
            }

            input.put("order", orders);
        } catch (JSONException eJson) {
            Log.d("Place Order", "json input parse error");
        }

        if (!hasEffectiveOrder) {
            Toast.makeText(getContext(), "Your Order is empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        Response.Listener<JSONObject> placeCallback = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getContext(), "Your Order Has Been Placed!", Toast.LENGTH_SHORT).show();
                // clear all the order info to zero
                for (int i = 0; i < dishArrayList.size(); i++) {
                    Dish dish = dishArrayList.get(i);
                    if (dish.getQuantity() > 0) {
                        dish.setQuantity(0);
                        dishArrayList.set(i, dish);
                    }
                }
                dishAdapter.notifyDataSetChanged();
                mListView.setAdapter(dishAdapter);
            }
        };
        RESTAPI.getInstance(getActivity().getApplicationContext())
                .makeRequest(Utils.API_BASE + "/order/bunch/",
                        Request.Method.POST,
                        input,
                        placeCallback,
                        null);

    }

    private void fetchDishInfo() {
        Response.Listener<JSONArray> dishCallback = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    dishArrayList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject curDish = (JSONObject) response.get(i);
                        Dish dish = new Dish();
                        dish.setName(curDish.getString("name"));
                        dish.setPrice(curDish.getDouble("price"));
                        dish.setRate(curDish.getDouble("rate"));
                        dish.setImage(curDish.getString("photo"));
                        dish.setId(curDish.getInt("id"));
                        dishArrayList.add(dish);
                    }
                    dishAdapter.notifyDataSetChanged();
                } catch (JSONException eJson) {
                    Log.d("Customer Tab 3", eJson.getMessage());
                }
            }
        };

        RESTAPI.getInstance(getActivity().getApplicationContext())
                .makeRequest(Utils.API_BASE + "/dish/",
                        Request.Method.GET,
                        null,
                        dishCallback,
                        null);
    }
}
