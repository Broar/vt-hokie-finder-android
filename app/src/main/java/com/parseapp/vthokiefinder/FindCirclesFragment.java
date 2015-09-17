package com.parseapp.vthokiefinder;

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
                joinCircle(mCircles.get(position));
            }
        }));
    }

    /**
     * Join the current ParseUser to the specified circle
     *
     * @param circle the circle to add the user to
     */
    private void joinCircle(final Circle circle) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Circle");
        query.getInBackground(circle.getObjectId(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                // Success! Let's try to add the current user to the circle
                if (e == null) {
                    ParseRelation<ParseObject> relation = object.getRelation("members");
                    relation.add(ParseUser.getCurrentUser());
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            // Success! Let's inform the user they have joined
                            if (e == null) {
                                String msg = "Successfully joined " + circle.getName();
                                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                                refreshCircles();
                            }

                            // Failure! Let's let the user know about what went wrong
                            else {
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                // Failure! Let's let the user know about what went wrong
                else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
