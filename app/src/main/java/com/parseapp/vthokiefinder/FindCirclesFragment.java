package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;

/**
 * A fragment that displays the circles the current user can possibly join
 *
 * @author Steven Briggs
 * @version 2015.10.03
 */
public class FindCirclesFragment extends CircleListFragment {

    public static final String TAG = FindCirclesFragment.class.getSimpleName();

    private Callbacks mListener;

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

    /**
     * A factory method to return a new FindCirclesFragment that has been configured
     *
     * @return a new FindCirclesFragment that has been configured
     */
    public static FindCirclesFragment newInstance() {
        return new FindCirclesFragment();
    }

    @Override
    protected void setCircleAdapter() {
        getRecyclerView().setAdapter(new CircleAdapter(mCircles, new CircleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                mListener.onCircleClicked(getCircles().get(position), false);
            }
        }));
    }

    @Override
    protected void refreshCircles() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getCirclesToJoin", params, new FunctionCallback<List<ParseObject>>() {
            @Override
            public void done(List<ParseObject> circles, ParseException e) {
                getCircles().clear();

                for (ParseObject c : circles) {
                    getCircles().add(new Circle(c.getObjectId(), c.getString("name")));
                }

                getRecyclerView().getAdapter().notifyDataSetChanged();
                stopRefresh();
            }
        });
    }
}
