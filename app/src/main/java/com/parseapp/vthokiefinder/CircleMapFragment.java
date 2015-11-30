package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A fragment that displays a Google Map to user that indicates the location of
 * members in a circle who are broadcasting their location.
 *
 * @author Steven Briggs
 * @version 2015.11.29
 */
public class CircleMapFragment extends Fragment {

    public static final String TAG = CircleMapFragment.class.getSimpleName();

    private Callbacks mListener;
    private List<Circle> mCircles;
    private Map<ParseUser, Marker> mUserMarkers;

    private ScheduledThreadPoolExecutor mScheduler;
    private ScheduledFuture mFuture;

    private MapView mMapView;
    private GoogleMap mMap;

    public interface Callbacks {
        GoogleApiClient requestGoogleApiClient();
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
        mCircles = new ArrayList<Circle>();
        mUserMarkers = new HashMap<ParseUser, Marker>();
        mScheduler =  new ScheduledThreadPoolExecutor(1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        // Temporary method of determining when GoogleApiClient is connected
        mListener.requestGoogleApiClient().registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getCirclesUserBelongsTo();
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
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        return false;
                    }
                });

                if (mFuture == null) {
                    mFuture = scheduleLocationPull();
                }
            }
        });
    }

    /**
     * Get the list of circles the current user belongs to
     */
    private void getCirclesUserBelongsTo() {
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser())
                .whereEqualTo("pending", false)
                .include("circle");

        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                if (e == null) {
                    for (UserCircle uc : userCircles) {
                        mCircles.add(uc.getCircle());
                    }
                } else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Pull the lat/long locations of each user. Add these to the GoogleMaps as Markers
     */
    private void pullLocations() {
        // Prepare the list of circle ids
        List<String> circleIds = new ArrayList<String>(mCircles.size());
        for (Circle circle : mCircles) {
            circleIds.add(circle.getObjectId());
        }

        // Request the broadcasting user of the circles contained in circleIds
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        params.put("circleIds", circleIds);
        ParseCloud.callFunctionInBackground("getBroadcastingUsersOfCircles", params, new FunctionCallback<Map<String, List<ParseUser>>>() {
            @Override
            public void done(Map<String, List<ParseUser>> broadcasters, ParseException e) {
                if (e == null) {
                    for (String circleId : broadcasters.keySet()) {
                        for (ParseUser user : broadcasters.get(circleId)) {
                            ParseGeoPoint loc = user.getParseGeoPoint("location");

                            // Create a new marker for this user if it doesn't yet exist
                            if (loc != null && !mUserMarkers.containsKey(user)) {
                                MarkerOptions options = new MarkerOptions()
                                        .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                                        .title(user.getUsername());

                                Marker marker = mMap.addMarker(options);
                                mUserMarkers.put(user, marker);
                            }

                            // Otherwise, just update the marker we already have
                            else if (loc != null && mUserMarkers.containsKey(user)) {
                                mUserMarkers.get(user).setPosition(new LatLng(loc.getLatitude(), loc.getLongitude()));
                            }
                        }
                    }
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        }, 5000L, 30000L, TimeUnit.MILLISECONDS);
    }
}
