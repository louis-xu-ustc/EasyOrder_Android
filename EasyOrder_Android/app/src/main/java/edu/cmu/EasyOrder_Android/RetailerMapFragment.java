package edu.cmu.EasyOrder_Android;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import static edu.cmu.EasyOrder_Android.Utils.DBG;
import static edu.cmu.EasyOrder_Android.Utils.ERR;
import static edu.cmu.EasyOrder_Android.Utils.REQUEST_PERMISSIONS_REQUEST_CODE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RetailerMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RetailerMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RetailerMapFragment extends Fragment {
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

    private ArrayAdapter<Address> mAdapter;
    private LatLng targetLatLng;

    public RetailerMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RetailerMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RetailerMapFragment newInstance(String param1, String param2) {
        RetailerMapFragment fragment = new RetailerMapFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.retailer_fragment_map, container, false);

        // Initialize Google Map
        locationManager =
                (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        mMapView = (MapView) v.findViewById(R.id.retailer_mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            Log.d("GoogleMap", "Map Initialization Error");
            Toast.makeText(getActivity(), "Map View Initialization Error", Toast.LENGTH_SHORT).show();
            return v;
        }

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
                LatLng curLatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder().target(curLatLng).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        // Register Search button callback
        Button searchButton = (Button) v.findViewById(R.id.retailer_search_location_button);
        mQuery = (EditText) v.findViewById(R.id.retailer_search_location_text);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = mQuery.getText().toString();
                new searchAddress().execute(query);
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

    private class searchAddress extends AsyncTask<String, Void, List<Address>> {
        @Override
        protected List<Address> doInBackground(String... params) {
            Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());

            try {
                List<Address> addresses = geoCoder.getFromLocationName(params[0], 5);
                if (addresses.size() > 0) {
                    return addresses;
                } else {
                    Log.d("SearchLocation", "Cannot get address from search query");
                    Toast.makeText(getActivity(), "No mapped search address found", Toast.LENGTH_SHORT).show();
                    return null;
                }
            } catch (IOException eIO) {
                Log.d("SearchLocation", "Cannot get address from search query");
                Toast.makeText(getActivity(), "No mapped search address found", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            mAdapter = new ArrayAdapter<Address>(getContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, addresses) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    Address address = getItem(position);
                    String formattedAddr = String.format("%s, %s, %s ",
                            address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "", address.getLocality(),
                            address.getCountryName());
                    textView.setText(formattedAddr);
                    return textView;
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle("Choose Location")
                    .setAdapter(mAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // place marker for target location
                            Address selectedAddr = mAdapter.getItem(which);
                            targetLatLng = new LatLng(selectedAddr.getLatitude(), selectedAddr.getLongitude());
                            // CameraPosition cameraPosition = new CameraPosition.Builder().target(curLatLng).zoom(12).build();
                            // googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            googleMap.clear();
                            String formattedAddr = String.format("%s, %s",
                                    selectedAddr.getMaxAddressLineIndex() > 0 ? selectedAddr.getAddressLine(0) : "", selectedAddr.getLocality());
                            MarkerOptions targetMarker = new MarkerOptions().title(formattedAddr).position(targetLatLng).icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            googleMap.addMarker(targetMarker);

                            // place marker for current location
                            // Check Access Location Permission
                            if (ActivityCompat.checkSelfPermission(getActivity(),
                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                Log.d("Second View", "Location Permission Not Granted");
                                return;
                            }

                            curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (curLocation == null) {
                                Log.d("Second View", "GPS Service Available");
                                return;
                            }

                            String QueryString = String.format("https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&key=%s",
                                    curLocation.getLatitude(), curLocation.getLongitude(),
                                    targetLatLng.latitude, targetLatLng.longitude,
                                    getActivity().getString(R.string.google_maps_key));

                            new searchETA().execute(QueryString);

                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private class searchETA extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject etaJson;
            try {
                etaJson = getJSONObjectFromURL(params[0]);

            } catch (IOException eIO) {
                Log.d("Second View", "Search ETA IO Error");
                return null;
            } catch (JSONException eJson) {
                Log.d("Second View", "Search ETA return value not Json");
                return null;
            }

            return etaJson;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                Log.d("Second View", "Search ETA no value returned");
                return;
            }

            int driveDistance = 0;
            try {
                JSONArray routes = jsonObject.getJSONArray("routes");
                if (routes.length() == 0) {
                    return;
                }
                JSONArray legs = ((JSONObject)routes.get(0)).getJSONArray("legs");
                driveDistance = ((JSONObject)legs.get(0)).getJSONObject("distance").getInt("value");
            } catch (JSONException eJson) {
                return;
            }

            // calculate duration with assumption of driving at 40 miles/hr
            double driveHours = (double)driveDistance / 1609.344 / 40;
            String eta = String.format("%d Hr %d Min", (int)driveHours, (int)((driveHours - (int)driveHours) * 60));
            float[] distance = new float[1];
            Location.distanceBetween(curLocation.getLatitude(), curLocation.getLongitude(),
                    targetLatLng.latitude, targetLatLng.longitude, distance);

            // place marker for current location
            String curMarkerTitle = String.format("%.2f miles - %s", distance[0] / 1609.344, eta);
            LatLng curLatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
            MarkerOptions curMarker = new MarkerOptions().title(curMarkerTitle).position(curLatLng).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            googleMap.addMarker(curMarker);

            // move and zoom google map to show all markers
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(curLatLng);
            builder.include(targetLatLng);
            LatLngBounds bounds = builder.build();
            int padding = 300; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.animateCamera(cu);
        }
    }

    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {

        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        // urlConnection.connect();
        int a = urlConnection.getResponseCode();

        BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
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
}
