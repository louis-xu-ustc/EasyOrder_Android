package edu.cmu.EasyOrder_Android;

import android.Manifest;
import android.app.Activity;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;
import static edu.cmu.EasyOrder_Android.Utils.DBG;
import static edu.cmu.EasyOrder_Android.Utils.ERR;
import static edu.cmu.EasyOrder_Android.Utils.MAX_PICKUP_LOCATION_DISPLAY;
import static edu.cmu.EasyOrder_Android.Utils.REQUEST_PERMISSIONS_REQUEST_CODE;
import static edu.cmu.EasyOrder_Android.Utils.TAG;

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

    public static final int DISPLAY_UI_TOAST = 0;
    public static final int DISPLAY_UI_DIALOG = 1;

    private OnFragmentInteractionListener mListener;
    private LocationManager locationManager;
    private Location curLocation;
    private MapView mMapView;
    private GoogleMap googleMap;
    private EditText mQuery;
    private UIHandler uiHandler;
    private ArrayAdapter pickupLocationAdapter;
    private ListView mListView;
    ArrayList<PickupLocation> pickupLocationArrayList;

    private JSONArray resultArray;
    private int selectedLocationItem = 0;
    private String addressText;
    private MarkerOptions targetMarkerOptions;
    private MarkerOptions currentMarkerOptions;
    private Context mContext;

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
        pickupLocationArrayList = new ArrayList<>();
        fetchPickupLocationsInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.retailer_fragment_map, container, false);

        // pickup locations
        mListView = (ListView) v.findViewById(R.id.retailer_picking_location_list);
        mContext = getContext();
        pickupLocationAdapter = new RetailerPickupLocationListAdapter(mContext, R.layout.retailer_pickup_location_list_view, pickupLocationArrayList);
        mListView.setAdapter(pickupLocationAdapter);

        // Initialize Google Map
        locationManager =
                (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);


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

        updateRetailerLocation();

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
                URI uri;
                if (query != null && !query.equals("")) {
                    try {
                        uri = new URI(
                                "https",
                                "maps.googleapis.com",
                                "/maps/api/geocode/json",
                                "address=" + query + "&sensor=false",
                                null);
                        GetLocationDownloadTask getLocation = new GetLocationDownloadTask();
                        getLocation.execute(uri.toURL());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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

    // The types specified here are the input data type, the progress type, and the result type
    private class parseLocation extends AsyncTask<Location, Void, String> {
        @Override
        protected void onPreExecute() {
            // Runs on the UI thread before doInBackground
            // Good for toggling visibility of a progress indicator
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Location... params) {
            // Some long-running task like downloading an image.
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            Location loc = params[0];
            List<Address> addresses = null;
            try {
                // get just a single address.
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException ioException) {
                // Catch invalid latitude or longitude values.
                String msg = "IO Exception OR Network Error";
                Log.d(DBG, msg);
                return msg;
            } catch (IllegalArgumentException illegalArgumentException) {
                // Error message to post in the log
                return "Invalid_lat_long_used";
            }
            Address address = null;
            String addr = "";
            String zipcode = "";
            String city = "";
            String state = "";
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0);

                StringBuilder strReturnedAddress = new StringBuilder();
                Log.d(DBG, "AddressLine: " + address.getMaxAddressLineIndex());
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    String line = address.getAddressLine(i);
                    Log.d(DBG, "line " + i + ": " + line);
                    if (line != null && !line.isEmpty()) {
                        strReturnedAddress.append(line);
                        break;
                    }
                }
                addr = strReturnedAddress.toString();
                city = address.getLocality();
                state = address.getAdminArea();
                for (int i = 0; i < addresses.size(); i++) {
                    address = addresses.get(i);
                    if (address.getPostalCode() != null) {
                        zipcode = address.getPostalCode();
                        break;
                    }
                }

                Log.d(DBG, "addr: " + addr);
                Log.d(DBG, "city: " + city);
                Log.d(DBG, "state: " + state);
                Log.d(DBG, "zipcode: " + zipcode);
                addressText = String.format("%s, %s, %s, %s ", addr, city, state, zipcode);
                // Return the text
                return addressText;
            } else {
                return "No_address_found";
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    private class GetLocationDownloadTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection;
            try {
                url = urls[0];
                Log.d(DBG, "url: " + url);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream is = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(is);

                int data = inputStreamReader.read();
                while (data != -1) {
                    char curr = (char) data;
                    result += curr;
                    data = inputStreamReader.read();
                }
                return result;

            } catch (IOException ioException) {
                HandlerThread uiThread = new HandlerThread("UIHandler");
                uiThread.start();
                uiHandler = new UIHandler(uiThread.getLooper());
                String msg = "IO Exception or Network Error!";
                handleUIRequest(DISPLAY_UI_TOAST, msg);
            } catch (IllegalArgumentException illegalArgumentException) {
                HandlerThread uiThread = new HandlerThread("UIHandler");
                uiThread.start();
                uiHandler = new UIHandler(uiThread.getLooper());
                String msg = "Invalid argument used";
                handleUIRequest(DISPLAY_UI_TOAST, msg);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            showDialog(result);
        }
    }

    private void showDialog(String result) {
        List<String> resultList = new ArrayList<>();
        if (result != null) {
            try {
                JSONObject locationObject = new JSONObject(result);
                resultArray = locationObject.getJSONArray("results");

                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject res = resultArray.getJSONObject(i);
                    resultList.add(res.getString("formatted_address"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), "no valid address found!", Toast.LENGTH_SHORT).show();
            return;
        }

        final CharSequence[] items = resultList.toArray(new CharSequence[resultList.size()]);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Select Location:");
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "selected item: " + items[which], Toast.LENGTH_SHORT).show();
                ListView lv = ((android.support.v7.app.AlertDialog) dialog).getListView();
                lv.setTag(new Integer(which));
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ListView lv = ((android.support.v7.app.AlertDialog) dialog).getListView();
                Integer selected = (Integer) lv.getTag();
                if (selected != null) {
                    selectedLocationItem = selected;
                    Toast.makeText(getContext(), "confirm item: " + items[selected], Toast.LENGTH_SHORT).show();
                    doPostOKOperation();
                }
            }
        });
        builder.show();
    }

    private void doPostOKOperation() {
        Log.d(DBG, "sel: " + selectedLocationItem);
        double lat, lng;
        String queryAddress = "";
        Location targetLocation = null;

        try {
            JSONObject results = resultArray.getJSONObject(selectedLocationItem);
            JSONObject getmetry = results.getJSONObject("geometry").getJSONObject("location");

            targetLocation = new Location(LocationManager.GPS_PROVIDER);
            lat = getmetry.getDouble("lat");
            lng = getmetry.getDouble("lng");
            targetLocation.setLatitude(lat);
            targetLocation.setLongitude(lng);
            Log.d(DBG, "targetLocation lat: " + lat);
            Log.d(DBG, "targetLocation lng: " + lng);
            queryAddress = results.getString("formatted_address");
            Log.d(DBG, "queryAddress: " + queryAddress);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Clears all the existing markers on the map
        googleMap.clear();
        if (checkPermission()) {
            googleMap.setMyLocationEnabled(true);
        } else {
            askPermission();
            return;
        }
        // Creating an instance of GeoPoint, to display in Google Map
        LatLng targetLatLng = new LatLng(lat, lng);

        targetMarkerOptions = new MarkerOptions();
        targetMarkerOptions.position(targetLatLng);
        targetMarkerOptions.title(queryAddress);
        googleMap.addMarker(targetMarkerOptions);

        // add the newly searched result into pickup locations
        addPickupLocation(targetLocation);

        Location currentLocation = getLastKnownLocation();
        if (currentLocation == null) {
            Log.e(ERR, "Invalid current location used!");
            return;
        }
        LatLng newLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(newLatLng));
        new parseLocation().execute(currentLocation);
        currentMarkerOptions = new MarkerOptions()
                .position(newLatLng)
                .title(addressText) // replace with addressText
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        googleMap.addMarker(currentMarkerOptions);

        double currentLat = currentLocation.getLatitude();
        double currentLng = currentLocation.getLongitude();
        Log.d(DBG, "currentLocation lat: " + currentLat);
        Log.d(DBG, "currentLocation lng: " + currentLng);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        //the include method will calculate the min and max bound.
        builder.include(currentMarkerOptions.getPosition());
        builder.include(targetMarkerOptions.getPosition());
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        googleMap.animateCamera(cu);
    }

    private Location getLastKnownLocation() {
//        LocationManager mLocationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getAllProviders();
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = null;
            if (checkPermission()) {
                l = locationManager.getLastKnownLocation(provider);
            }
            if (l == null) {
                Log.d(DBG, "continue");
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(DBG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        if (ActivityCompat.checkSelfPermission((Activity) mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText((Activity) mContext, "Location access permission is denied!", Toast.LENGTH_SHORT).show();
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
                    android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle("You are not granted!");
                    alertDialog.setMessage("Please login in to Twitter first!");
                    alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEUTRAL, "OK",
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

    private void updateRetailerLocation() {
        Response.Listener<JSONObject> locationCallback = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Thread newPost = new Thread(new Runnable() {
                    public void run() {
                        try {
                            // FIXME 60s to update the location
                            Thread.sleep(60000);
                            updateRetailerLocation();
                        } catch (InterruptedException e) {
                            Log.d(DBG, e.getMessage());
                        }
                    }
                });
                newPost.start();
            }
        };

        Location curLocation = getLastKnownLocation();

        if (curLocation != null) {
            JSONObject input = new JSONObject();
            try {
                input.put("latitude", curLocation.getLatitude());
                input.put("longitude", curLocation.getLongitude());
            } catch (JSONException eJson) {
                Log.d("Retailer Tab 2", "Post location input json parse error");
            }
            RESTAPI.getInstance(getContext())
                    .makeRequest(Utils.API_BASE + "/current_location/",
                            Request.Method.PUT,
                            input,
                            locationCallback,
                            null);
        }
    }

    /**
     * add new searched result into the pickup location list
     *
     * @param newPickupLocation
     */
    private void addPickupLocation(Location newPickupLocation) {
        Log.d(TAG, "addPickupLocation " + newPickupLocation.toString());
        Response.Listener<JSONObject> addPickupLocationCallback = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // TODO should immediately update the listview after a new pickup location is uploaded
                Toast.makeText(mContext, "successfully upload a new pickup location!", Toast.LENGTH_SHORT).show();
            }
        };

        JSONObject input = new JSONObject();
        try {
            input.put("latitude", newPickupLocation.getLatitude());
            input.put("longitude", newPickupLocation.getLongitude());
        } catch (JSONException eJson) {
            Log.d("Retailer Tab 2", "Add new pickup location input json parse error");
        }

        RESTAPI.getInstance(getContext())
                .makeRequest(Utils.API_BASE + "/pickup_locations/",
                        Request.Method.POST,
                        input,
                        addPickupLocationCallback,
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
                        pickupLocationArrayList.add(location);
                    }
                    pickupLocationAdapter.notifyDataSetChanged();
                    mListView.setAdapter(pickupLocationAdapter);
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
