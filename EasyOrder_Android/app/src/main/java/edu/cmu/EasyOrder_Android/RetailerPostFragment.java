package edu.cmu.EasyOrder_Android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


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
    ArrayList<Dish> dishArrayList;

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
        //FIXME fetch data from backend database
        fillFakeDishArrayList();
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
                                //TODO
                                Toast.makeText(getContext(), "Further backend operations to handle delete post!", Toast.LENGTH_LONG).show();
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
//                //TODO add action to post more dish
//                Toast.makeText(getActivity(), "Further operation to post one more dish!",
//                        Toast.LENGTH_LONG).show();
                Intent addDishPostIntent = new Intent(getContext(), RetailerAddPostActivity.class);
                startActivity(addDishPostIntent);
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
