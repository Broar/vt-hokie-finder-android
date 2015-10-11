package com.parseapp.vthokiefinder;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

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
public class CircleBroadcastFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = CircleBroadcastFragment.class.getSimpleName();

    private ArrayList<UserCircle> mUserCircles;
    private SwitchCompat mMasterBroadcast;
    private SwipeRefreshLayout mSwipeContainer;
    private RecyclerView mRecyclerView;

    /**
     * A factory method to return a new CircleBroadcastFragment that has been configured
     *
     * @return a new CircleBroadcastFragment that has been configured
     */
    public static CircleBroadcastFragment newInstance() {
        return new CircleBroadcastFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserCircles = new ArrayList<UserCircle>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_circle_broadcast, container, false);

        // Initialize the SwipeRefreshLayout
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        mSwipeContainer.setOnRefreshListener(this);
        mSwipeContainer.setColorSchemeColors(R.color.accent);

        // Setup the RecyclerView of UserCircles
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(new CircleBroadcastAdapter(mUserCircles, new CircleBroadcastAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                switchBroadcastForUser(mUserCircles.get(position));
            }

            @Override
            public boolean isUserBroadcasting() {
                return mMasterBroadcast.isChecked();
            }
        }));

        // Initialize the master broadcast switch
        mMasterBroadcast = (SwitchCompat) view.findViewById(R.id.masterBroadcast);
        mMasterBroadcast.setChecked(ParseUser.getCurrentUser().getBoolean("masterBroadcast"));

        // Do not allow the user to to click on the list if the master switch is not flipped
        if (!mMasterBroadcast.isChecked()) {
            mRecyclerView.setClickable(false);
        }

        mMasterBroadcast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Any clicks on the list of Circles should be enabled / disabled whenever
                // the master broadcast switch is flipped
                mRecyclerView.setClickable(isChecked);

                // Save the user's current broadcast preference to the backend
                ParseUser.getCurrentUser().put("masterBroadcast", isChecked);
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        refreshCircles();

        return view;
    }

    @Override
    public void onRefresh() {
        refreshCircles();
    }

    /**
     * Stop the refresh animation if necessary
     */
    private void stopRefresh() {
        if (mSwipeContainer.isRefreshing()) {
            mSwipeContainer.setRefreshing(false);
        }
    }

    /**
     * Refresh the list of UserCircles belonging to the current ParseUser
     */
    private void refreshCircles() {
        ParseQuery<UserCircle> query = UserCircle.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser()).include("circle");
        query.findInBackground(new FindCallback<UserCircle>() {
            @Override
            public void done(List<UserCircle> userCircles, ParseException e) {
                if (e == null) {
                    mUserCircles.clear();

                    for (UserCircle uc : userCircles) {
                        mUserCircles.add(uc);
                    }

                    if (mRecyclerView.getAdapter() != null) {
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

                stopRefresh();
            }
        });
    }

    /**
     * Switch whether or not the current ParseUser is broadcastingto a Circlespecified Circle
     userCircle * @pUserCram circle the cthe broadcasting forbroadcasting for
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
}
