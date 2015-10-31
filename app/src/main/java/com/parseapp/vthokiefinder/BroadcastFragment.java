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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
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
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment that displays the user's circles and handles broadcasting features
 *
 * @author Steven Briggs
 * @version 2015.10.10
 */
public class BroadcastFragment extends ListFragment<UserCircle>
        implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = BroadcastFragment.class.getSimpleName();

    private static final int BROADCAST_NOTIFICATION_ID = 0;

    private PendingIntent mBroadcastIntent;
    private LocationRequest mLocationRequest;

    private Callbacks mListener;

    private SwitchCompat mMasterBroadcast;
    private SwipeRefreshLayout mSwipeContainer;
    private RecyclerView mRecyclerView;

    public interface Callbacks {
        GoogleApiClient requestGoogleApiClient();
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
        View view = inflater.inflate(R.layout.fragment_circle_broadcast, container, false);

        // Initialize the SwipeRefreshLayout
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        mSwipeContainer.setOnRefreshListener(this);
        mSwipeContainer.setColorSchemeColors(R.color.accent);

        // Initialize the RecyclerView of UserCircles
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(new CircleBroadcastAdapter(getItems(), new CircleBroadcastAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                switchBroadcastForUser(getItems().get(position));
            }

            @Override
            public boolean isUserBroadcasting() {
                return mMasterBroadcast.isChecked();
            }
        }));

        // Initialize the master broadcast switch
        mMasterBroadcast = (SwitchCompat) view.findViewById(R.id.masterBroadcast);
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
                        }

                        else {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        populate();

        return view;
    }

    @Override
    protected void populate() {
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser()).include("circle");
        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                if (e == null) {
                    getItems().clear();

                    for (UserCircle uc : userCircles) {
                        getItems().add(uc);
                    }

                    if (mRecyclerView.getAdapter() != null) {
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                }

                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

                if (mSwipeContainer.isRefreshing()) {
                    mSwipeContainer.setRefreshing(false);
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        populate();
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
        if (mMasterBroadcast.isChecked()) {
            GoogleApiClient googleApiClient = mListener.requestGoogleApiClient();
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, mBroadcastIntent);
            NotificationManagerCompat.from(getContext()).cancel(BROADCAST_NOTIFICATION_ID);
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
