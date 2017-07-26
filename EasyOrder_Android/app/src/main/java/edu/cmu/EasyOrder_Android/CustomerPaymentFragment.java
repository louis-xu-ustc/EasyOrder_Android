package edu.cmu.EasyOrder_Android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.braintreepayments.api.dropin.DropInRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.NOTIFICATION_SERVICE;
import static edu.cmu.EasyOrder_Android.Utils.DBG;
import static edu.cmu.EasyOrder_Android.Utils.PREFERENCE_TWITTER_USER_ID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomerPaymentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CustomerPaymentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomerPaymentFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ArrayAdapter dishAdapter;
    private ListView mListView;
    private ArrayList<Dish> dishArrayList;
    private Order order;
    private TextView totalPrice;

    private Long notificationTimestamp = 0L;

    public CustomerPaymentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CustomerPaymentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CustomerPaymentFragment newInstance(String param1, String param2) {
        CustomerPaymentFragment fragment = new CustomerPaymentFragment();
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

        order = new Order();
        dishArrayList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.customer_fragment_payment, container, false);

        mListView = (ListView) rootView.findViewById(R.id.customer_order_detail_list);
        totalPrice = (TextView) rootView.findViewById(R.id.customer_order_detail_total_price);
        dishAdapter = new CustomerOrderDetailAdapter(getContext(), R.layout.customer_order_detail_list_view, dishArrayList);
        mListView.setAdapter(dishAdapter);
        fetchOrderDetail();

        Button customerPayButton = (Button) rootView.findViewById(R.id.customer_pay_button);
        customerPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preparePurchase();
            }
        });
        pollNotification();

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void fetchOrderDetail() {
        Response.Listener<JSONArray> orderCallback = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    // must use the same dishArrayList, otherwise notifyDatasetChanged cannot be useful
                    dishArrayList.clear();
                    order = new Order();

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject curOrder = (JSONObject) response.get(i);
                        Dish curDish = new Dish();
                        curDish.setName(curOrder.getString("dish"));
                        curDish.setPrice(curOrder.getDouble("price"));
                        curDish.setQuantity(curOrder.getInt("amount"));
                        dishArrayList.add(curDish);
                        order.addDish(curDish);
                    }

                    totalPrice.setText(new StringBuilder().append("$ ").append(String.valueOf(order.getTotalPrice())).toString());
                    dishAdapter.notifyDataSetChanged();
                } catch (JSONException eJson) {
                    Log.d("Customer Tab 3", eJson.getMessage());
                }
            }
        };

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Long id = pref.getLong(PREFERENCE_TWITTER_USER_ID, 0);
        String twitterID = id.toString();
        RESTAPI.getInstance(getActivity().getApplicationContext())
                .makeRequest(Utils.API_BASE + "/order/user/" + twitterID + "/",
                        Request.Method.GET,
                        null,
                        orderCallback,
                        null);

    }

    private void preparePurchase() {
        Response.Listener<String> tokenCallback = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                popupPayment(response);
            }
        };

        RESTAPI.getInstance(getActivity().getApplicationContext())
                .makeRequest(Utils.API_BASE + "/payment/client_token/",
                        Request.Method.GET,
                        null,
                        tokenCallback,
                        null);
    }

    private void popupPayment(String token) {
        DropInRequest dropInRequest = new DropInRequest().clientToken(token);
        getActivity().startActivityForResult(dropInRequest.getIntent(getContext()), Utils.REQUEST_PAYMENT_REQUEST_CODE);
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
                        notificationTimestamp = timestamp;
//                        NotificationCompat.Builder mBuilder =
//                                new NotificationCompat.Builder(getContext())
//                                        .setContentTitle("Meal Going Away")
//                                        .setContentText(content);
//                        NotificationManager mNotifyMgr =
//                                (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);
//                        mNotifyMgr.notify(0, mBuilder.build());
                    }
                } catch (JSONException eJson) {
                    Log.d("Customer Tab 3", "Json Parse Error");
                }

                Thread newPoll = new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(10000);
                            pollNotification();
                        } catch (InterruptedException e) {
                            Log.d(DBG, e.getMessage());
                        }
                    }
                });
                newPoll.start();
            }
        };

        String URL = Utils.API_BASE + "/notification/";
        if (notificationTimestamp != 0L) {
            URL += notificationTimestamp;
            URL += "/";
        }

        RESTAPI.getInstance(getActivity().getApplicationContext())
                .makeRequest(URL,
                        Request.Method.GET,
                        null,
                        tokenCallback,
                        null);
    }
}
