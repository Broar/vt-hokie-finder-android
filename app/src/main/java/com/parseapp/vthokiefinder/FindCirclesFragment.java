package com.parseapp.vthokiefinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
                openCircle(mCircles.get(position), CircleActivity.JOIN_ACTION);
            }
        }));
    }
}
