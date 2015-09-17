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
                openCircle(mCircles.get(position));
            }
        }));
    }

    /**
     * Open a detailed view of a Circle
     *
     * @param circle the circle to be opened
     */
    private void openCircle(final Circle circle) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Circle");
        query.getInBackground(circle.getObjectId(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                // Success! Open the view of the Circle
                if (e == null) {
                    startCircleActivity(circle);
                }

                // Failure! Let's let the user know about what went wrong
                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Start a new CircleActivity
     *
     * @param circle the circle to be displayed in the CircleActivity
     */
    private void startCircleActivity(Circle circle) {
        Intent intent = new Intent(getContext(), CircleActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CircleActivity.CIRCLE_OBJECT_ID_KEY, circle.getObjectId());
        bundle.putString(CircleActivity.CIRCLE_NAME_KEY, circle.getName());
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }
}
