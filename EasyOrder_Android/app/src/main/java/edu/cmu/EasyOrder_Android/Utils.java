package edu.cmu.EasyOrder_Android;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yunpengx on 7/9/17.
 */

public class Utils {
    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    public static final int REQUEST_PAYMENT_REQUEST_CODE = 1012;
    public static final String DBG = "DBG";
    public static final String ERR = "ERR";

    public static final String BACKEND_SERVER = "http://192.168.0.102:9000";
    // public static final String BACKEND_SERVER = "http://54.202.127.83";
    public static final String API_BASE = BACKEND_SERVER + "/backend";
}
