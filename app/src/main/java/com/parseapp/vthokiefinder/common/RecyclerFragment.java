package com.parseapp.vthokiefinder.common;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parseapp.vthokiefinder.R;
import com.rockerhieu.rvadapter.endless.EndlessRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A fragment that displays a list of items
 *
 * @author Steven Briggs
 * @version 2015.11.02
 */
public abstract class RecyclerFragment<T, A extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>>
        extends Fragment implements EndlessRecyclerViewAdapter.RequestToLoadMoreListener {

    public static final int DEFAULT_LIMIT = 25;

    private List<T> mItems;
    private int mPage;
    private int mLimit;

    private A mBaseAdapter;
    private EndlessRecyclerViewAdapter mAdapter;

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
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    /**
     * Refresh the items of the RecyclerView
     */
    public void onRefresh() {
        getItems().clear();
        setPage(0);
        getAdapter().onDataReady(true);
    }

    /**
     * Construct a new base adapter for the RecyclerView
     *
     * @return the newly constructed base adapter
     */
    protected abstract A buildAdapter();

    protected EndlessRecyclerViewAdapter getAdapter() {
        return mAdapter;
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
