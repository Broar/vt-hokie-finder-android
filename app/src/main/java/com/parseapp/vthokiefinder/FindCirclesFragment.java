package com.parseapp.vthokiefinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.rockerhieu.rvadapter.endless.EndlessRecyclerViewAdapter;

import java.util.HashMap;
import java.util.List;

/**
 * A fragment that displays the circles the current user can possibly join
 *
 * @author Steven Briggs
 * @version 2015.10.31
 */
public class FindCirclesFragment extends ListFragment<Circle> implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = FindCirclesFragment.class.getSimpleName();

    private EndlessRecyclerViewAdapter mEndlessAdapter;
    private CircleAdapter mCircleAdapter;
    private SwipeRefreshLayout mSwipeContainer;


    /**
     * A factory method to return a new FindCirclesFragment that has been configured
     *
     * @return a new FindCirclesFragment that has been configured
     */
    public static FindCirclesFragment newInstance() {
        return new FindCirclesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflateFragment(R.layout.fragment_circles_list, inflater, container);

        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeContainer.setColorSchemeColors(R.color.accent);
        mSwipeContainer.setOnRefreshListener(this);

        mCircleAdapter = new CircleAdapter(getItems(), new CircleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                openCircle(getItems().get(position).getObjectId(), false);
            }
        });

        mEndlessAdapter = new EndlessRecyclerViewAdapter(getContext(), mCircleAdapter, this);
        getRecyclerView().setAdapter(mEndlessAdapter);

        return view;
    }

    @Override
    public void onRefresh() {
        getItems().clear();
        mCircleAdapter.notifyDataSetChanged();
        mEndlessAdapter.restartAppending();
    }

    /**
     * Open a detailed view of the circle specified by id
     *
     * @param id the id of the circle to be opened
     * @param isMember determines whether the current user is member of the circle
     */
    private void openCircle(String id, boolean isMember) {
        Intent intent = new Intent(getContext(), CircleDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CircleDetailActivity.CIRCLE_ID_KEY, id);
        bundle.putBoolean(CircleDetailActivity.IS_MEMBER_KEY, isMember);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    @Override
    public void onLoadMoreRequested() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        params.put("skip", getNextPage());
        params.put("limit", getLimit());

        ParseCloud.callFunctionInBackground("getCirclesToJoin", params, new FunctionCallback<List<Circle>>() {
            @Override
            public void done(List<Circle> circles, ParseException e) {
                if (e == null) {
                    if (!circles.isEmpty()) {
                        getItems().addAll(circles);
                        mCircleAdapter.notifyDataSetChanged();
                        mEndlessAdapter.onDataReady(true);
                    }

                    else {
                        mEndlessAdapter.onDataReady(false);
                    }

                    setPage(getPage() + 1);
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
}
