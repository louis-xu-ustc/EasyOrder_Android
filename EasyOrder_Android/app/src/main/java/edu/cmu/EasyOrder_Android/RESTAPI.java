package edu.cmu.EasyOrder_Android;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by jiajiey on 7/19/17.
 */

public class RESTAPI {
    // Use Singleton Class for RESTAPI using Volley Library

    private static RESTAPI mInstance;
    private static Context mCtx;
    private RequestQueue mRequestQueue;

    private RESTAPI(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public static synchronized RESTAPI getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RESTAPI(context);
        }
        return mInstance;
    }

    private Response.ErrorListener getDefaultErrCallback() {
        // default error handler
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    Toast.makeText(mCtx, "Error: " + new String(response.data), Toast.LENGTH_SHORT).show();
                } else {
                    String errMsg = error.getClass().getSimpleName();
                    if (!errMsg.isEmpty()) {
                        Toast.makeText(mCtx, errMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    public void makeRequest(String url, int method, JSONObject input,
                            Response.Listener<JSONObject> callback,
                            Response.ErrorListener errCallback) {

        if (errCallback == null) {
            errCallback = getDefaultErrCallback();
        }

        JsonObjectRequest strReq = new JsonObjectRequest(method, url, input, callback, errCallback);
        mRequestQueue.add(strReq);
    }

    public void makeRequest(String url, int method, JSONArray input,
                            Response.Listener<JSONArray> callback,
                            Response.ErrorListener errCallback) {

        if (errCallback == null) {
            errCallback = getDefaultErrCallback();
        }

        JsonArrayRequest strReq = new JsonArrayRequest(method, url, input, callback, errCallback);
        mRequestQueue.add(strReq);
    }

    public void makeRequest(String url, int method, String input,
                            Response.Listener<String> callback,
                            Response.ErrorListener errCallback) {

        if (errCallback == null) {
            errCallback = getDefaultErrCallback();
        }

        StringRequest strReq = new StringRequest(method, url, callback, errCallback);
        mRequestQueue.add(strReq);
    }
}
