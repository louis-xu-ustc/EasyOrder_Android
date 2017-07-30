package edu.cmu.EasyOrder_Android;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static edu.cmu.EasyOrder_Android.Utils.ADD_DISH_POST;
import static edu.cmu.EasyOrder_Android.Utils.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RetailerPostFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RetailerPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RetailerPostFragment extends Fragment {
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

    public RetailerPostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RetailerPostFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RetailerPostFragment newInstance(String param1, String param2) {
        RetailerPostFragment fragment = new RetailerPostFragment();
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
        dishArrayList = new ArrayList<>();
        fetchDishInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchDishInfo();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "add dish post enter!" + " requestCode:" + requestCode + " resultCode: " + resultCode);
        if (requestCode == ADD_DISH_POST && resultCode == Activity.RESULT_OK) {
            fetchDishInfo();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.retailer_fragment_post, container, false);
        mListView = (ListView) rootView.findViewById(R.id.retailer_dish_list);
        dishAdapter = new RetailerDishListAdapter(getContext(), R.layout.retailer_dish_list_view, dishArrayList);
        mListView.setAdapter(dishAdapter);

        mListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
//                        Toast.makeText(getContext(), "click item, id: " + String.valueOf(id) + " pos: " +
//                                String.valueOf(position), Toast.LENGTH_LONG).show();

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(true);
                        builder.setTitle("Delete Dish ?");
                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                // How to remove the selected item?
                                Dish dish = (Dish) dishAdapter.getItem(position);
                                dishAdapter.remove(dish);
                                dishAdapter.notifyDataSetChanged();

                                deleteDish(dish);
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

        ImageButton postMoreDishButton = (ImageButton) rootView.findViewById(R.id.retailer_post_dish_button);
        postMoreDishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addDishPostIntent = new Intent(getContext(), RetailerAddPostActivity.class);
                startActivityForResult(addDishPostIntent, ADD_DISH_POST);
            }
        });
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
                        dish.setQuantity(curDish.getInt("num"));
                        dishArrayList.add(dish);
                    }
                    dishAdapter.notifyDataSetChanged();
                    mListView.setAdapter(dishAdapter);
                } catch (JSONException eJson) {
                    Log.d("Customer Tab 3", eJson.getMessage());
                }
            }
        };

        RESTAPI.getInstance(getActivity().getApplicationContext())
                .makeRequest(Utils.API_BASE + "/order/",
                        Request.Method.GET,
                        null,
                        dishCallback,
                        null);
    }

    private void deleteDish(Dish dish) {

        Response.Listener<JSONObject> dishCallback = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        };

        Response.ErrorListener errCallback = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    Toast.makeText(getContext(), "Error: " + new String(response.data), Toast.LENGTH_SHORT).show();
                } else {
                    // Null Response Body, as specified by 204 HTTP Status Code
                    Toast.makeText(getContext(), "Dish Deleted", Toast.LENGTH_SHORT).show();
                }
            }
        };

        RESTAPI.getInstance(getActivity().getApplicationContext())
                .makeRequest(Utils.API_BASE + "/dish/" + dish.getId() + "/",
                        Request.Method.DELETE,
                        null,
                        dishCallback,
                        errCallback);
    }
}
