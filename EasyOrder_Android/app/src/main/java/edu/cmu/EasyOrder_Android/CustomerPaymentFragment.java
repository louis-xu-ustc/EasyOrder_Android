package edu.cmu.EasyOrder_Android;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


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
        fillFakeOrderDetail();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.customer_fragment_payment, container, false);

        mListView = (ListView) rootView.findViewById(R.id.customer_order_detail_list);
        totalPrice = (TextView) rootView.findViewById(R.id.customer_order_detail_total_price);
        totalPrice.setText(new StringBuilder().append("$ ").append(String.valueOf(order.getTotalPrice())).toString());
        dishAdapter = new CustomerOrderDetailAdapter(getContext(), R.layout.customer_order_detail_list_view, dishArrayList);
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

    private void fillFakeOrderDetail() {
        Dish dish = new Dish();
        dish.setName("Piazza");
        dish.setQuantity(1);
        dish.setPrice(10);
        order.addDish(dish);
        dishArrayList = order.getDishList();
    }
}
