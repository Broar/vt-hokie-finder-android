package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract fragment that displays Circles in a list
 *
 * @author Steven Briggs
 * @version 2015.09.16
 */
public abstract class CircleListFragment extends Fragment implements OnRefreshListener {

    protected RecyclerView mRecyclerView;
    protected SwipeRefreshLayout mSwipeContainer;
    protected ArrayList<Circle> mCircles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCircles = new ArrayList<Circle>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_circles_list, container, false);

        // Initialize the SwipeRefreshLayout
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeContainer.setOnRefreshListener(this);
        mSwipeContainer.setColorSchemeColors(R.color.accent);

        // Initialize the RecyclerView
        mRecyclerView = (RecyclerView) view.findViewById(R.id.circles);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        setCircleAdapter();
        refreshCircles();

        return view;
    }

    @Override
    public void onRefresh() {
        refreshCircles();
    }

    /**
     * Construct a new ParseQuery to search for Circles
     *
     * @return a configured ParseQuery that searches for Circles
     */
    protected abstract ParseQuery<ParseObject> makeQuery();

    /**
     * Set the CircleAdapter for the RecyclerView
     */
    protected abstract void setCircleAdapter();

    /**
     * Refresh the list of circles
     */
    protected void refreshCircles() {
        // Construct a new ParseQuery according to subclass implementation
        ParseQuery<ParseObject> query = makeQuery();

        // Perform the query on the Parse class of Circles
        mCircles.clear();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                // Success! Let's add the results to our collection of circles and display them
                if (e == null) {
                    for (ParseObject o : objects) {
                        mCircles.add(new Circle(o.getObjectId(), o.getString("name")));
                    }

                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }

                // Failure! Let's inform the user about what went wrong
                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

                // Stop the refresh animation if necessary
                if (mSwipeContainer.isRefreshing()) {
                    mSwipeContainer.setRefreshing(false);
                }
            }
        });
    }
}
