package edu.cmu.EasyOrder_Android;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RetailerPaymentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RetailerPaymentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RetailerPaymentFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ArrayAdapter orderListAdapter;
    private ListView mListView;
    ArrayList<Order> orderArrayList;

    public RetailerPaymentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RetailerPaymentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RetailerPaymentFragment newInstance(String param1, String param2) {
        RetailerPaymentFragment fragment = new RetailerPaymentFragment();
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

        orderArrayList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.retailer_fragment_payment, container, false);
        mListView = (ListView) rootView.findViewById(R.id.retailer_order_list);
        orderListAdapter = new RetailerOrderListAdapter(getContext(), R.layout.retailer_order_list_view, orderArrayList);
        mListView.setAdapter(orderListAdapter);
        Button notifyAllButton = (Button) rootView.findViewById(R.id.retailer_notify_all_button);
        notifyAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            postNotification();
            }
        });
        fetchOrderDetail();
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
                    orderArrayList.clear();

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject curUser = (JSONObject) response.get(i);
                        Order order = new Order();
                        order.setUserName(curUser.getString("name"));
                        order.setIfNotify(!curUser.getBoolean("paid"));
                        orderArrayList.add(order);
                    }

                    orderListAdapter.notifyDataSetChanged();
                } catch (JSONException eJson) {
                    Log.d("Customer Tab 3", eJson.getMessage());
                }
            }
        };

        RESTAPI.getInstance(getActivity().getApplicationContext())
                .makeRequest(Utils.API_BASE + "/user/",
                        Request.Method.GET,
                        null,
                        orderCallback,
                        null);

    }

    private void postNotification() {
        Response.Listener<JSONObject> notifyCallback = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getContext(), "Notification Posted", Toast.LENGTH_SHORT).show();
            }
        };

        JSONObject input = new JSONObject();
        try {
            input.put("content", "Dear user, please get your meal ASAP");
        } catch (JSONException eJson) {
            Log.d("Customer Tab 3", "Post notification input json parse error");
        }

        RESTAPI.getInstance(getContext())
                .makeRequest(Utils.API_BASE + "/notification/",
                        Request.Method.PUT,
                        input,
                        notifyCallback,
                        null);
    }
}
