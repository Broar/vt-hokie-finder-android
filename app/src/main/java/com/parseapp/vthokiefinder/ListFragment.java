package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rockerhieu.rvadapter.endless.EndlessRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A fragment that displays a list of items
 *
 * @author Steven Briggs
 * @version 2015.11.02
 */
public abstract class ListFragment<T, A extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> extends Fragment implements
        EndlessRecyclerViewAdapter.RequestToLoadMoreListener,
        SwipeRefreshLayout.OnRefreshListener {

    public static final int DEFAULT_LIMIT = 10;

    private List<T> mItems;
    private int mPage;
    private int mLimit;

    private A mBaseAdapter;
    private EndlessRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItems = new ArrayList<T>();
        mPage = 0;
        mLimit = DEFAULT_LIMIT;
        mBaseAdapter = buildAdapter();
        mAdapter = new EndlessRecyclerViewAdapter(getContext(), mBaseAdapter, this);
    }

    /**
     * Inflate the view of the fragment by using the specified layout
     *
     * @param resId the resource id of the layout to be inflated
     * @param inflater the inflater
     * @param container the view that contains the fragment
     * @return the view that has been inflated with the specified layout
     */
    protected View inflateFragment(int resId, LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(resId, container, false);

        // Initialize the RecyclerView
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);

        // Initialize the SwipeRefreshLayout
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        // The swipe container is an optional layout item
        if (mSwipeContainer != null) {
            mSwipeContainer.setOnRefreshListener(this);
        }

        return view;
    }

    @Override
    public void onRefresh() {
        mItems.clear();
        mPage = 0;
        mAdapter.restartAppending();
    }

    /**
     * Construct a new adapter for the RecyclerView
     *
     * @return the newly constructed adapter
     */
    protected abstract A buildAdapter();

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected EndlessRecyclerViewAdapter getAdapter() {
        return mAdapter;
    }

    protected void setRefresh(boolean refresh) {
        mSwipeContainer.setRefreshing(refresh);
    }

    protected A getBaseAdapter() {
        return mBaseAdapter;
    }

    protected List<T> getItems() {
        return mItems;
    }

    protected int getPage() {
        return mPage;
    }

    protected int getNextPage() {
        return mPage * mLimit;
    }

    protected void nextPage() {
        mPage++;
    }

    protected void setPage(int page) {
        mPage = page;
    }

    protected int getLimit() {
        return mLimit;
    }

    protected void setLimit(int limit) {
        mLimit = limit;
    }
}
