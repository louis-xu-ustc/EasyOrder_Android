package edu.cmu.EasyOrder_Android;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Constants used in this sample.
 */

final class Constants {

    private Constants() {
    }

    private static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";

    static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km

    /**
     * Map for storing information about CMU campus area.
     */
    static final HashMap<String, LatLng> CMU_AREA_LANDMARKS = new HashMap<>();

    static {
        // Carnegie Mellon University
        CMU_AREA_LANDMARKS.put("Carnegie Mellon University", new LatLng(40.444663, -79.945039));

        // Hillman Library of Pittsburgh University
//        CMU_AREA_LANDMARKS.put("Hillman Library", new LatLng(40.443159, -79.953555));

        // Gesing Staium
//        CMU_AREA_LANDMARKS.put("Gesing Staium", new LatLng(40.443893, -79.938766));
    }

    // add more
}
