package edu.cmu.EasyOrder_Android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static edu.cmu.EasyOrder_Android.Utils.DBG;
import static edu.cmu.EasyOrder_Android.Utils.ERR;
import static edu.cmu.EasyOrder_Android.Utils.GOOGLE_MAP_API;
import static edu.cmu.EasyOrder_Android.Utils.MAX_PICKUP_LOCATION_DISPLAY;
import static edu.cmu.EasyOrder_Android.Utils.PICKUP_LOCATION_ETA_INIT_VAL;
import static edu.cmu.EasyOrder_Android.Utils.REQUEST_PERMISSIONS_REQUEST_CODE;
import static edu.cmu.EasyOrder_Android.Utils.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomerMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CustomerMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomerMapFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private LocationManager locationManager;
    private Location curLocation;
    private MapView mMapView;
    private GoogleMap googleMap;
    private EditText mQuery;
    private Context mContext;

    private ArrayAdapter pickupLocationAdapter;
    private ListView mListView;
    ArrayList<PickupLocation> pickupLocationArrayList;
    private boolean mHasMapAnimated;

    private ArrayAdapter<Address> mAdapter;
    private LatLng targetLatLng;

    public CustomerMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CustomerMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CustomerMapFragment newInstance(String param1, String param2) {
        CustomerMapFragment fragment = new CustomerMapFragment();
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
        pickupLocationArrayList = new ArrayList<>();
        fetchPickupLocationsInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.customer_fragment_map, container, false);

        // pickup locations
        mListView = (ListView) v.findViewById(R.id.customer_pickup_location_list);
        mContext = getContext();
        pickupLocationAdapter = new CustomerPickupLocationListAdapter(mContext, R.layout.customer_pickup_location_list_view, pickupLocationArrayList);

        // Initialize Google Map
        locationManager =
                (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        mMapView = (MapView) v.findViewById(R.id.customer_mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            Log.d("GoogleMap", "Map Initialization Error");
            Toast.makeText(getActivity(), "Map View Initialization Error", Toast.LENGTH_SHORT).show();
            return v;
        }

        // start to update the
        mHasMapAnimated = false;
        getRetailerLocation();

        // Set Google Map Focus on current Location first
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                if (ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                // For showing a move to my location button
                googleMap.setMyLocationEnabled(true);

                curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (curLocation == null) {
                    return;
                }

                // For zooming automatically to the location of the marker
//                LatLng curLatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
//                CameraPosition cameraPosition = new CameraPosition.Builder().target(curLatLng).zoom(12).build();
//                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        });

        return v;
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

    private void updateETA(LatLng curLatLng) {
        new searchETA().execute(curLatLng);
        // move and zoom google map to show all markers
        MarkerOptions targetMarker;
        MarkerOptions curMarker = new MarkerOptions().title("Current Retailer Location").position(curLatLng).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        googleMap.clear();
        googleMap.addMarker(curMarker);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(curLatLng);
        for (int i = 0; i < pickupLocationArrayList.size(); i++) {
            LatLng targetLatLng = new LatLng(pickupLocationArrayList.get(i).getLatitude(), pickupLocationArrayList.get(i).getLongitude());
            builder.include(targetLatLng);
            targetMarker = new MarkerOptions().title("Pickup Location: " + String.valueOf(i)).position(targetLatLng).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_RED));
            googleMap.addMarker(targetMarker);
        }

        if (!mHasMapAnimated) {
            LatLngBounds bounds = builder.build();
            int padding = 200; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.animateCamera(cu);
            mHasMapAnimated = true;
        }
    }

    private class searchETA extends AsyncTask<LatLng, Void, List<JSONObject>> {
        @Override
        protected List<JSONObject> doInBackground(LatLng... params) {
            List<JSONObject> list = new ArrayList<>();
            LatLng currLatLng = params[0];

            for (int i = 0; i < pickupLocationArrayList.size(); i++) {
                PickupLocation pickupLocation = pickupLocationArrayList.get(i);
                Location targetLocation = pickupLocation.getLatLngLocation();
                //Log.d(DBG, "target " + String.valueOf(i) + " lat: " + String.valueOf(targetLocation.getLatitude()) + " lng: " + String.valueOf(targetLocation.getLongitude()));
                String QueryString = String.format("https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&key=%s",
                        currLatLng.latitude, currLatLng.longitude,
                        targetLocation.getLatitude(), targetLocation.getLongitude(),
                        GOOGLE_MAP_API);

                try {
                    JSONObject etaJson = getJSONObjectFromURL(QueryString);
                    //Log.d(DBG, etaJson.toString());
                    list.add(etaJson);
                } catch (IOException eIO) {
                    Log.d("Second View", "Search ETA IO Error");
                } catch (JSONException eJson) {
                    Log.d("Second View", "Search ETA return value not Json");
                }
            }

            Log.d(DBG, "finish doInBackground with " + String.valueOf(list.size()) + " items filled");
            return list;
        }

        @Override
        protected void onPostExecute(List<JSONObject> jsonObjectList) {
            if (jsonObjectList == null || jsonObjectList.isEmpty()) {
                Log.d("Second View", "Search ETA no value returned");
                return;
            }

            for (int i = 0; i < jsonObjectList.size(); i++) {
                try {
                    double driveMins = 0.0;
                    JSONArray routes = jsonObjectList.get(i).getJSONArray("routes");
                    if (routes.length() == 0) {
                        Log.d(DBG, "no routes found!");

                        //Log.d(DBG, "set eta: " + String.valueOf(driveMins));
                        PickupLocation updatedLoc = pickupLocationArrayList.get(i);
                        updatedLoc.setETA(driveMins);
                        pickupLocationArrayList.set(i, updatedLoc);
                        continue;
                    }
                    JSONArray legs = ((JSONObject) routes.get(0)).getJSONArray("legs");
                    int driveDistance = ((JSONObject) legs.get(0)).getJSONObject("distance").getInt("value");

                    // calculate duration with assumption of driving at 40 miles/hr
                    driveMins = (double) driveDistance / 1609.344 / 40 * 60;
                    //Log.d(DBG, "set eta: " + String.valueOf(driveMins));
                    PickupLocation updatedLoc = pickupLocationArrayList.get(i);
                    updatedLoc.setETA(driveMins);
                    pickupLocationArrayList.set(i, updatedLoc);
                } catch (JSONException eJson) {
                    eJson.printStackTrace();
                }
            }
            pickupLocationAdapter.notifyDataSetChanged();
            mListView.setAdapter(pickupLocationAdapter);
            Log.d(TAG, "Time to update ETA!");
        }
    }

    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {

        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        // urlConnection.connect();
        int a = urlConnection.getResponseCode();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();

        return new JSONObject(jsonString);
    }

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(DBG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location access permission is denied!", Toast.LENGTH_SHORT).show();
            Log.e(ERR, "Location access permission is denied!");
            return false;
        } else {
            return true;
        }
    }

    // Asks for permission
    private void askPermission() {
        Log.d(DBG, "askPermission()");
        ActivityCompat.requestPermissions(
                getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(DBG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                } else {
                    // Permission denied
                }
                break;
            }
        }
    }

    private void getRetailerLocation() {
        Response.Listener<JSONObject> locationCallback = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    double lat = response.getDouble("latitude");
                    double lng = response.getDouble("longitude");
                    LatLng currRetailerLatLng = new LatLng(lat, lng);
                    Log.d(DBG, "curr lat: " + String.valueOf(lat) + " lng: " + String.valueOf(lng));
                    updateETA(currRetailerLatLng);

                } catch (JSONException eJson) {
                    Log.d(DBG, eJson.getMessage());
                }

                Thread newGet = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // FIXME 20s to update the location
                            Thread.sleep(20000);
                            getRetailerLocation();
                        } catch (InterruptedException e) {
                            Log.d(DBG, e.getMessage());
                        }
                    }

                });
                newGet.start();
            }
        };

        RESTAPI.getInstance(getContext())
                .makeRequest(Utils.API_BASE + "/current_location/",
                        Request.Method.GET,
                        null,
                        locationCallback,
                        null);
    }

    /**
     * fetch all pickup locations from the server
     */
    private void fetchPickupLocationsInfo() {
        Response.Listener<JSONArray> pickupLocationCallback = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    pickupLocationArrayList.clear();
                    // only display the latest three pickup locations
                    int displayLimit = Math.min(response.length(), MAX_PICKUP_LOCATION_DISPLAY);
                    for (int i = response.length() - 1, cnt = 0; i >= 0; i--, cnt++) {
                        if (cnt >= displayLimit) break;
                        JSONObject curPickupLocation = (JSONObject) response.get(i);
                        PickupLocation location = new PickupLocation();
                        double lat = curPickupLocation.getDouble("latitude");
                        double lng = curPickupLocation.getDouble("longitude");
                        String addr = getAddress(lat, lng);
                        location.setLatitude(lat);
                        location.setLongitude(lng);
                        location.setLocation(addr);
                        // will keep updating in getRetailerLocation
                        location.setETA(PICKUP_LOCATION_ETA_INIT_VAL);
                        pickupLocationArrayList.add(location);
                    }
                    pickupLocationAdapter.notifyDataSetChanged();
                    mListView.setAdapter(pickupLocationAdapter);
                    Log.d(DBG, "fetch Pickup Locations Info finished with " + String.valueOf(pickupLocationArrayList.size()) + " items filled");
                } catch (JSONException eJson) {
                    Log.d("Retailer Tab 2", eJson.getMessage());
                }
            }
        };

        RESTAPI.getInstance(getActivity().getApplicationContext())
                .makeRequest(Utils.API_BASE + "/pickup_locations/",
                        Request.Method.GET,
                        null,
                        pickupLocationCallback,
                        null);
    }

    private String getAddress(double lat, double lng) {
        String address = null;
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            address = obj.getAddressLine(0);
            Log.d(TAG, "Address" + address);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return address;
    }

}
