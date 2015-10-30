package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * An abstract class that displays a list of friends
 *
 * @author Steven Briggs
 * @version 2015.10.29
 */
public abstract class FriendListFragment extends Fragment {

    public static final String TAG = FriendListFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ArrayList<Friend> mFriends;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFriends = new ArrayList<Friend>();
    }

    protected View inflateFragment(int resId, LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(resId, container, false);

        // Initialize the RecyclerView
        mRecyclerView = (RecyclerView) view.findViewById(R.id.friends);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);

        // Disable the SwipeRefreshLayout temporarily
        view.findViewById(R.id.swipeContainer).setEnabled(false);

        setHasOptionsMenu(true);
        return view;
    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected ArrayList<Friend> getFriends() {
        return mFriends;
    }
}
