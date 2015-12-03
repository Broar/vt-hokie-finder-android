package com.parseapp.vthokiefinder;


import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * A fragment that displays the user's circles and handles broadcasting features
 *
 * @author Steven Briggs
 * @version 2015.11.29
 */
public class BroadcastFragment extends RecyclerFragment<UserCircle, BroadcastAdapter> {

    public static final String TAG = BroadcastFragment.class.getSimpleName();

    private static final String IS_VIEWING_KEY = "isViewing";
    private static final int BROADCAST_NOTIFICATION_ID = 0;

    private Circle mViewedCircle;
    private PendingIntent mBroadcastIntent;
    private LocationRequest mLocationRequest;
    private Callbacks mListener;
    private SwitchCompat mMasterBroadcast;

    public interface Callbacks {
        GoogleApiClient requestGoogleApiClient();
        void onViewedCircleClicked(Circle circle);
    }

    /**
     * A factory method to return a new BroadcastFragment that has been configured
     *
     * @return a new BroadcastFragment that has been configured
     */
    public static BroadcastFragment newInstance() {
        return new BroadcastFragment();
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String circleId = savedInstanceState.getString(IS_VIEWING_KEY);

            if (circleId != null) {
                mViewedCircle = ParseObject.createWithoutData(Circle.class, circleId);
            }
        }

        // Initialize the location broadcasting service
        Intent intent = new Intent(getContext(), LocationPushService.class);
        mBroadcastIntent = PendingIntent.getService(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mLocationRequest = LocationRequest.create()
                .setInterval(10000L)
                .setFastestInterval(5000L)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflateFragment(R.layout.fragment_broadcast, inflater, container);

        // Initialize the master broadcast switch
        mMasterBroadcast = (SwitchCompat) view.findViewById(R.id.master_broadcast);
        mMasterBroadcast.setChecked(ParseUser.getCurrentUser().getBoolean("masterBroadcast"));
        mMasterBroadcast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // Save the user's current broadcast preference to the backend
                ParseUser.getCurrentUser().put("masterBroadcast", isChecked);
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            toggleBroadcast();
                        } else {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mViewedCircle != null) {
            outState.putString(IS_VIEWING_KEY, mViewedCircle.getObjectId());
        }
    }

    @Override
    protected BroadcastAdapter buildAdapter() {
        return new BroadcastAdapter(getItems(), new BroadcastAdapter.OnItemClickedListener() {
            @Override
            public void onBroadcastClicked(int position) {
                switchBroadcastForUser(getItems().get(position));
            }

            @Override
            public void onIsViewingClicked(int position, boolean isViewing) {

                // If there is a circle being viewed, then determine if the clicked circle is the same.
                // If it is, set the viewed circle to null; otherwise, just update it
                if (mViewedCircle != null) {
                    Circle clickedCircle = getItems().get(position).getCircle();
                    mViewedCircle = mViewedCircle.equals(clickedCircle) ? null : clickedCircle;
                }

                // Otherwise, just grab a reference to the viewed circle
                else {
                    mViewedCircle = getItems().get(position).getCircle();
                }

                mListener.onViewedCircleClicked(mViewedCircle);
            }
        });
    }

    @Override
    public void onLoadMoreRequested() {
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser())
                .whereEqualTo("pending", false)
                .whereEqualTo("accepted", true)
                .include("circle")
                .setSkip(getNextPage())
                .setLimit(getLimit());

        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                if (e == null) {
                    if (!userCircles.isEmpty()) {
                        getItems().addAll(userCircles);
                        getAdapter().onDataReady(true);
                    }

                    else {
                        getAdapter().onDataReady(false);
                    }

                    nextPage();
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Get the circle that the user is viewing on the map
     *
     * @return the circle the user is viewing, or null if one is not being viewed
     */
    public Circle getViewedCircle() {
        return mViewedCircle;
    }

    /**
     * Switch whether or not the current ParseUser is broadcasting to the specified Circle
     *
     * @param userCircle the circle the user wants to broadcast for
     */
    private void switchBroadcastForUser(UserCircle userCircle) {
        userCircle.setIsBroadcasting(!userCircle.isBroadcasting());
        userCircle.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Switch the user's public location broadcast on/off
     */
    private void toggleBroadcast() {
        GoogleApiClient googleApiClient = mListener.requestGoogleApiClient();

        // Switch broadcasting on
        if (googleApiClient.isConnected() && mMasterBroadcast.isChecked()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, mBroadcastIntent);
            startBroadcastNotification();
        }

        // Switch broadcasting off and cancel the ongoing notification
        else if (googleApiClient.isConnected() && !mMasterBroadcast.isChecked()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, mBroadcastIntent);
            NotificationManagerCompat.from(getContext()).cancel(BROADCAST_NOTIFICATION_ID);
        }
    }

    /**
     * Stop all location broadcasting for the user
     */
    public void stopBroadcast() {
        // Cancel the location updates and the broadcast intent
        if (mMasterBroadcast.isChecked()) {
            GoogleApiClient googleApiClient = mListener.requestGoogleApiClient();
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, mBroadcastIntent);
            NotificationManagerCompat.from(getContext()).cancel(BROADCAST_NOTIFICATION_ID);
            mBroadcastIntent.cancel();
        }
    }

    /**
     * Issue a status notification that the user is broadcasting their location
     */
    private void startBroadcastNotification() {
        // The activity should be brought back to the front if it already exists
        Intent intent = new Intent(getContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent openHomeIntent = PendingIntent.getActivity(getContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Build a sticky notification to inform the user they are broadcasting
        Notification notification = new NotificationCompat.Builder(getContext())
                .setSmallIcon(R.drawable.ic_stat_fighting_gobbler)
                .setContentTitle("Location broadcast")
                .setContentText("Your location is being broadcast to circles")
                .setContentIntent(openHomeIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .build();

        NotificationManagerCompat.from(getContext()).notify(BROADCAST_NOTIFICATION_ID, notification);
    }
}
