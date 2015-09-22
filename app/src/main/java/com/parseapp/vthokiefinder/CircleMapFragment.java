package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * A fragment that displays a Google Map to user that indicates the location of
 * members in a circle who are broadcasting their location.
 *
 * @author Steven Briggs
 * @version 2015.09.22
 */
public class CircleMapFragment extends Fragment {

    public static final String TAG = CircleMapFragment.class.getSimpleName();

    private MapView mapView;
    private GoogleMap map;

    /**
     * A factory method to return a new CircleMapFragment that has been configured
     *
     * @return a new CircleMapFragment that has been configured
     */
    public static CircleMapFragment newInstance() {
        return new CircleMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        // Get an instance of a GoogleMap and perform any setup
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                addUserLocations();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * Add Markers to the GoogleMap that represent the lat/long location of each user
     */
    private void addUserLocations() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("User");
        ParseUser.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                // Success! Create Markers for each user and pin them to the map
                if (e == null) {
                    for (ParseObject o : objects) {
                        ParseGeoPoint location = o.getParseGeoPoint("location");

                        if (location != null) {
                            map.addMarker(new MarkerOptions().position(
                                    new LatLng(location.getLatitude(), location.getLongitude())));
                        }
                    }
                }

                // Failure! Let the user know what went wrong
                else {
                    Snackbar.make(mapView, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
