package com.parseapp.vthokiefinder;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * A background service that will push the user's location data to the backend
 *
 * @author Steven Briggs
 * @version 2015.09.23
 */
public class LocationPushService extends IntentService {

    public static final String TAG = LocationPushService.class.getSimpleName();

    /**
     * Create a new LocationPushService object.
     */
    public LocationPushService() {
        super(LocationPushService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Push the user's location to the backend
        if (LocationResult.hasResult(intent)) {
            LocationResult result = LocationResult.extractResult(intent);
            final double latitude = result.getLastLocation().getLatitude();
            final double longitude = result.getLastLocation().getLongitude();
            ParseUser.getCurrentUser().put("location", new ParseGeoPoint(latitude, longitude));

            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d(TAG, "Pushed: " + latitude + " | " + longitude);
                    }

                    else {
                        Log.d(TAG, e.getMessage());
                    }
                }
            });
        }
    }
}
