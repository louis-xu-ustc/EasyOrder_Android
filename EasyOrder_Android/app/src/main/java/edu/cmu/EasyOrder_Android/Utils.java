package edu.cmu.EasyOrder_Android;

/**
 * Created by yunpengx on 7/9/17.
 */

public class Utils {
    public static final String TWITTER_CONSUMER_KEY = BuildConfig.CONSUMER_KEY;
    public static final String TWITTER_CONSUMER_SECRET = BuildConfig.CONSUMER_SECRET;
    public static final String TWITTER_ACCESS_TOKEN = BuildConfig.ACCESS_TOKEN;
    public static final String TWITTER_ACCESS_TOKEN_SECRET = BuildConfig.ACCESS_TOKEN_SECRET;

    public static String PREFERENCE_TWITTER_LOGGED_IN = "TWITTER_LOGGED_IN";
    public static String PREFERENCE_TWITTER_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static String PREFERENCE_TWITTER_ACCESS_TOKEN_SECRET = "ACCESS_TOKEN_SECRET";
    public static String PREFERENCE_TWITTER_USER_ID = "ID";
    public static String PREFERENCE_TWITTER_USER_NAME = "NAME";
    public static String PREFERENCE_TWITTER_USER_SCREEN_NAME = "SCREEN_NAME";
    public static String PREFERENCE_TWITTER_USER_IMAGE_URL = "IMAGE_URL";

    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    public static final int REQUEST_PAYMENT_REQUEST_CODE = 1012;
    public static final int ADD_DISH_POST = 1024;
    public static final int MAX_PICKUP_LOCATION_DISPLAY = 3;
    public static final String TAG = "TAG";
    public static final String DBG = "DBG";
    public static final String ERR = "ERR";

    //    public static final String BACKEND_SERVER = "http://192.168.0.102:9000";
    public static final String BACKEND_SERVER = "http://54.202.127.83";
    public static final String API_BASE = BACKEND_SERVER + "/backend";
}
