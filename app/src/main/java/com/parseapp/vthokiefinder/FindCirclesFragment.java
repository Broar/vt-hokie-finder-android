package com.parseapp.vthokiefinder;

import android.view.View;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * A fragment that displays the circles the current user can possibly join
 *
 * @author Steven Briggs
 * @version 2015.09.16
 */
public class FindCirclesFragment extends CircleListFragment {

    public static final String TAG = FindCirclesFragment.class.getSimpleName();

    /**
     * A factory method to return a new FindCirclesFragment that has been configured
     *
     * @return a new FindCirclesFragment that has been configured
     */
    public static FindCirclesFragment newInstance() {
        return new FindCirclesFragment();
    }

    @Override
    protected ParseQuery<ParseObject> makeQuery() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Circle");
        query.whereNotEqualTo("members", ParseUser.getCurrentUser());
        return query;
    }

    @Override
    protected void setCircleAdapter() {
        mRecyclerView.setAdapter(new CircleAdapter(mCircles, new CircleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                openCircle(mCircles.get(position), CircleDetailActivity.JOIN_ACTION);
            }
        }));
    }
}
