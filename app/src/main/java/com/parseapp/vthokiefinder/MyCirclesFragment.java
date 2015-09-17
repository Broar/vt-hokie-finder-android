package com.parseapp.vthokiefinder;

import android.view.View;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * A fragment that displays the circles the current user is a part of
 *
 * @author Steven Briggs
 * @version 2015.09.16
 */
public class MyCirclesFragment extends CircleListFragment {

    public static final String TAG = MyCirclesFragment.class.getSimpleName();

    /**
     * A factory method to return a new FindCirclesFragment that has been configured
     *
     * @return a new FindCirclesFragment that has been configured
     */
    public static MyCirclesFragment newInstance() {
        return new MyCirclesFragment();
    }

    @Override
    protected ParseQuery<ParseObject> makeQuery() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Circle");
        query.whereEqualTo("members", ParseUser.getCurrentUser());
        return query;
    }

    @Override
    protected void setCircleAdapter() {
        mRecyclerView.setAdapter(new CircleAdapter(mCircles, null));
    }
}
