package edu.cmu.EasyOrder_Android;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
        //FIXME
        fillFakeOrderArrayList();
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
                //TODO add action to notify all unpaid users
                Toast.makeText(getActivity(), "Further operation to notify all unpaid users!",
                        Toast.LENGTH_LONG).show();
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

    private void fillFakeOrderArrayList() {
        Order order1 = new Order();
        order1.setUserName("Yangjie Jie");
        order1.setIfNotify(true);
        orderArrayList.add(order1);

        Order order2 = new Order();
        order2.setUserName("Yunpeng Xu");
        order2.setIfNotify(false);
        orderArrayList.add(order2);
    }
}
