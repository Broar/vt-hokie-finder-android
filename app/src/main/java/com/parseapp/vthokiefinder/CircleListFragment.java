package com.parseapp.vthokiefinder;

import android.content.Intent;
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
import com.parse.GetCallback;
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
     * Stop the refresh animation if necessary
     */
    protected void stopRefresh() {
        if (mSwipeContainer.isRefreshing()) {
            mSwipeContainer.setRefreshing(false);
        }
    }

    /**
     * Set the CircleAdapter for the RecyclerView
     */
    protected abstract void setCircleAdapter();

    /**
     * Refresh the list of circles
     */
    protected abstract void refreshCircles();

    /**
     * Open a detailed view of a Circle
     *
     * @param circle the circle to be opened
     * @param action the circle action the user can perform
     */
    protected void openCircle(final Circle circle, final int action) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Circle");
        query.getInBackground(circle.getObjectId(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                // Success! Open the view of the Circle
                if (e == null) {
                    startCircleActivity(circle, action);
                }

                // Failure! Let's let the user know about what went wrong
                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Start a new CircleDetailActivity
     *
     * @param circle the circle to be displayed in the CircleDetailActivity
     * @param action the circle action to be passed to the CircleDetailActivity
     */
    private void startCircleActivity(Circle circle, int action) {
        Intent intent = new Intent(getContext(), CircleDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CircleDetailActivity.CIRCLE_OBJECT_ID_KEY, circle.getObjectId());
        bundle.putString(CircleDetailActivity.CIRCLE_NAME_KEY, circle.getName());
        bundle.putInt(CircleDetailActivity.CIRCLE_ACTION_KEY, action);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected ArrayList<Circle> getCircles() {
        return mCircles;
    }
}
