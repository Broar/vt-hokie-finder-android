package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A fragment that displays a Google Map to user that indicates the location of
 * members in a circle who are broadcasting their location.
 *
 * @author Steven Briggs
 * @version 2015.09.25
 */
public class CircleMapFragment extends Fragment {

    public static final String TAG = CircleMapFragment.class.getSimpleName();

    private Callbacks mListener;

    private ScheduledThreadPoolExecutor mScheduler;
    private ScheduledFuture mFuture;

    private MapView mMapView;
    private GoogleMap mMap;

    public interface Callbacks {
        GoogleApiClient requestGoogleApi();
    }

    /**
     * A factory method to return a new CircleMapFragment that has been configured
     *
     * @return a new CircleMapFragment that has been configured
     */
    public static CircleMapFragment newInstance() {
        return new CircleMapFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            mListener = (Callbacks) activity;
        }

        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Callbacks");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScheduler =  new ScheduledThreadPoolExecutor(1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        // Temporary method of determining if GoogleApiClient is connected
        mListener.requestGoogleApi().registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                getGoogleMap();
            }

            @Override
            public void onConnectionSuspended(int i) {
                // Space purposefully left empty
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        // Reschedule the location pull tasks upon return to this Fragment
        if (mFuture == null) {
            mFuture = scheduleLocationPull();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mFuture.cancel(false);
        mFuture = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    /**
     * Get a GoogleMap instance and perform any additional setup
     */
    public void getGoogleMap() {
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                if (mFuture == null) {
                    mFuture = scheduleLocationPull();
                }
            }
        });
    }

    /**
     * Pull the lat/long locations of each user. Add these to the GoogleMaps as Markers
     */
    private void pullLocations() {
        ParseUser.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                // Success! Create Markers for each user and pin them to the map
                if (e == null) {
                    for (ParseObject u : users) {
                        ParseGeoPoint location = u.getParseGeoPoint("location");

                        if (location != null) {
                            mMap.addMarker(new MarkerOptions().position(
                                    new LatLng(location.getLatitude(), location.getLongitude())));
                        }
                    }

                    Log.d(TAG, "Pulled locations!");
                }

                // Failure! Let the user know what went wrong
                else {
                    Snackbar.make(mMapView, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Schedule a location pull to occur at a delayed rate of 10s
     *
     * @return a future for the tasks to be completed by the thread pool
     */
    private ScheduledFuture scheduleLocationPull() {
        return mScheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                pullLocations();
            }
        }, 5000L, 10000L, TimeUnit.MILLISECONDS);
    }
}
