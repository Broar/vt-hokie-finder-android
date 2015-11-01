package com.parseapp.vthokiefinder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rockerhieu.rvadapter.endless.EndlessRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment that displays a list of items whose type is specified by T
 *
 * @author Steven Briggs
 * @version 2015.10.31
 */
public abstract class ListFragment<T> extends Fragment implements EndlessRecyclerViewAdapter.RequestToLoadMoreListener {

    public static final int DEFAULT_LIMIT = 10;

    private List<T> mItems;
    private int mPage;
    private int mLimit;

    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItems = new ArrayList<T>();
        mPage = 0;
        mLimit = DEFAULT_LIMIT;
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

        setHasOptionsMenu(true);
        return view;
    }

    protected List<T> getItems() {
        return mItems;
    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected int getNextPage() {
        return mPage * mLimit;
    }

    protected int getPage() {
        return mPage;
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
