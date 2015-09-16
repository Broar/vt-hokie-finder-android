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
 * A fragment that displays all of the circles the current user is not a part of
 *
 * @author Steven Briggs
 * @version 2015.09.15
 */
public class CirclesFragment extends Fragment implements OnRefreshListener {
    public static final String TAG = CirclesFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;
    private ArrayList<String> mCircles;

    /**
     * Factory method to return a new CircleFragment object
     *
     * @return a CircleFragment object
     */
    public static CirclesFragment newInstance() {
        return new CirclesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCircles = new ArrayList<String>();
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
        mRecyclerView.setAdapter(new CirclesAdapter(mCircles));
        refreshCircles();

        return view;
    }

    @Override
    public void onRefresh() {
        refreshCircles();
    }

    /**
     * Perform an update on the list of circle names
     */
    private void refreshCircles() {
        mCircles.clear();

        // Perform a query that searches for all available circles
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Circle");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                // Success! Let's add the results to our collection of circles and display them
                if (e == null) {
                    for (ParseObject o : objects) {
                        mCircles.add(o.getString("name"));
                    }

                    // Stop the refresh animation if necessary
                    if (mSwipeContainer.isRefreshing()) {
                        mSwipeContainer.setRefreshing(false);
                    }

                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }

                // Failure! Let's inform the user about what went wrong
                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
