package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A fragment that displays a Google Map to user that indicates the location of
 * members in a circle who are broadcasting their location.
 *
 * @author Steven Briggs
 * @version 2015.12.02
 */
public class MapFragment extends Fragment {

    public static final String TAG = MapFragment.class.getSimpleName();

    private Callbacks mListener;
    private List<Circle> mCircles;
    private Map<Marker, ParseUser> mUserMarkers;

    private MapView mMapView;
    private GoogleMap mMap;

    public interface Callbacks {
        GoogleApiClient requestGoogleApiClient();
        Circle onViewedCircleRequested();
    }

    /**
     * A factory method to return a new MapFragment that has been configured
     *
     * @return a new MapFragment that has been configured
     */
    public static MapFragment newInstance() {
        return new MapFragment();
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
        mUserMarkers = new HashMap<Marker, ParseUser>();
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
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
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
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMapToolbarEnabled(false);

                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        ParseUser user = mUserMarkers.get(marker);

                        View contents = View.inflate(getContext(), R.layout.item_map_user_info, null);
                        ((TextView) contents.findViewById(R.id.username)).setText(user.getUsername());
                        ((TextView) contents.findViewById(R.id.email)).setText(user.getEmail());

                        CircleImageView avatar = (CircleImageView) contents.findViewById(R.id.avatar);
                        ParseFile file = user.getParseFile("avatar");
                        if (file != null) {
                            Glide.with(getContext())
                                    .load(Uri.parse(file.getUrl()))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(avatar);
                        } else {
                            Glide.with(getContext())
                                    .fromResource()
                                    .load(R.drawable.fighting_gobblers_medium)
                                    .into(avatar);
                        }

                        return contents;
                    }
                });

                refreshLocations(mListener.onViewedCircleRequested());
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
     * Refresh the markers to be the broadcasting users in circle
     *
     * @param circle the circle to pull and display the locations for, null if the map is to be cleared
     */
    public void refreshLocations(@Nullable Circle circle) {

        // Clear all the markers off the map and any model entries
        if (circle == null || mMap == null) {
            if (mMap != null) {
                mMap.clear();
            }

            mUserMarkers.clear();
            return;
        }

        // Pull all the locations of the broadcasting users in circle
        // Display them on the map
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("circleId", circle.getObjectId());
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getBroadcastingUsers", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    mMap.clear();
                    mUserMarkers.clear();

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    for (ParseUser user : users) {
                        ParseGeoPoint gp = user.getParseGeoPoint("location");
                        LatLng latLng = new LatLng(gp.getLatitude(), gp.getLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
                        mUserMarkers.put(marker, user);
                        builder.include(latLng);
                    }

                    if (!users.isEmpty()) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
                    }
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
