package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * A fragment that displays the circles the current user is a part of
 *
 * @author Steven Briggs
 * @version 2015.09.16
 */
public class MyCirclesFragment extends CircleListFragment {

    public static final String TAG = MyCirclesFragment.class.getSimpleName();

    private Callbacks mListener;

    /**
     * A factory method to return a new FindCirclesFragment that has been configured
     *
     * @return a new FindCirclesFragment that has been configured
     */
    public static MyCirclesFragment newInstance() {
        return new MyCirclesFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            mListener = (Callbacks) activity;
        }

        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Callbacks");
        }
    }

    @Override
    protected void setCircleAdapter() {
        getRecyclerView().setAdapter(new CircleAdapter(mCircles, new CircleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                mListener.onCircleClicked(getCircles().get(position), true);
            }
        }));
    }

    @Override
    protected void refreshCircles() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("UserCircle");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.include("circle");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> userCircles, ParseException e) {
                getCircles().clear();

                for (ParseObject uc : userCircles) {
                    ParseObject circle = uc.getParseObject("circle");
                    getCircles().add(new Circle(circle.getObjectId(), circle.getString("name")));
                }

                getRecyclerView().getAdapter().notifyDataSetChanged();
                stopRefresh();
            }
        });
    }
}
