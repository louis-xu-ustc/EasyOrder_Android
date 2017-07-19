package edu.cmu.EasyOrder_Android;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static edu.cmu.EasyOrder_Android.Utils.DBG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomerPostFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CustomerPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomerPostFragment extends Fragment {
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
    ArrayList<Dish> dishArrayList;

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
    // TODO: Rename and change types and number of parameters
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
        dishArrayList = new ArrayList<>();
        //FIXME
        fillFakeDishArrayList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.customer_fragment_post, container, false);
        mListView = (ListView) rootView.findViewById(R.id.customer_dish_list);
        dishAdapter = new CustomerDishListAdapter(getContext(), R.layout.customer_dish_list_view, dishArrayList);

        ImageButton shoppingCart = (ImageButton) rootView.findViewById(R.id.customer_order_confirm_button);
        View convertView = (View) inflater.inflate(R.layout.customer_shopping_cart_confirm_list, null);
        ListView lv = (ListView) convertView.findViewById(R.id.customer_order_confirm_list);
        shoppingCart.setTag(R.string.first_tag, convertView);
        shoppingCart.setTag(R.string.second_tag, lv);
        shoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> orderedList = new ArrayList<String>();
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
                        //TODO save to backend database
                        Toast.makeText(getActivity(), "Further operations to save to backend database!",
                                Toast.LENGTH_LONG).show();
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
        mListView.setAdapter(dishAdapter);
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

    private void fillFakeDishArrayList() {
        Dish dish1 = new Dish();
        dish1.setName("pizza");
        dish1.setPrice(10);
        //FIXME
//        dish1.setImage(ContextCompat.getDrawable(getContext(),R.drawable.pizza).toString());
        dish1.setQuantity(1);
        dish1.setRate(3);
        dishArrayList.add(dish1);

        Dish dish2 = new Dish();
        dish2.setName("salad");
        dish2.setPrice(12);
        // FIXME
//        dish2.setImage(ContextCompat.getDrawable(getContext(),R.drawable.salad).toString());
        dish2.setQuantity(0);
        dish2.setRate(4);
        dishArrayList.add(dish2);

        Dish dish3 = new Dish();
        dish3.setName("fish & chips");
        dish3.setPrice(0);
        // FIXME
//        dish2.setImage(ContextCompat.getDrawable(getContext(),R.drawable.salad).toString());
        dish3.setQuantity(0);
        dish3.setRate(2);
        dishArrayList.add(dish3);
    }
}
